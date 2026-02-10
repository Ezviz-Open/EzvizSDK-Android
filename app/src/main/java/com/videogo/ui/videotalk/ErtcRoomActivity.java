/**
 * 视频通话页面
 */
package com.videogo.ui.videotalk;

import androidx.appcompat.widget.AppCompatImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ez.baselib.utils.LogUtil;
import com.ez.basertc.RTCConstant;
import com.ez.basertc.RTCListener;
import com.ez.basertc.view.VideoCanvasView;
import com.ez.ertcengine.ERTCEngine;
import com.ez.network.BaseError;
import com.videogo.EzvizApplication;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.ui.playback.RemoteListUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

@SuppressLint({"DefaultLocale", "HandlerLeak", "NewApi"})
public class ErtcRoomActivity extends RootActivity implements View.OnClickListener {

    // region --- 全局变量|global variable ---

    private static final String TAG = ErtcRoomActivity.class.getSimpleName();

    private TextView tvDevSerial;// 设备序列号
    private TextView tvVideoTalkTime;// 视频通话时长

    private LinearLayout viewBottom;// 底部视图
    // 麦克风控制
    private LinearLayout llMicro;
    private AppCompatImageView ivMicroState;
    private TextView tvMicroState;
    // 声音控制
    private LinearLayout llSound;
    private AppCompatImageView ivSoundState;
    private TextView tvSoundState;
    // 挂断
    private LinearLayout llHangUp;
    // 转换摄像头
    private LinearLayout llSwitchCamera;
    private AppCompatImageView ivSwitchCamera;
    private TextView tvSwitchCamera;
    // 摄像头已开/摄像头已关
    private LinearLayout llCamera;
    private AppCompatImageView ivCameraState;
    private TextView tvCameraState;

    private VideoCanvasView vcsLocalTextureView;
    private VideoCanvasView vcsRemoteTextureView;

    private boolean mMicroOn = true;// 麦克风开关状态
    private boolean mSpeakerOn = true;// 扬声器开关状态
    private boolean mCameraOn = true;// 相机开关状态

    // 视频通话时间
    private int mVideoTalkSecond = 0;
    // 定时器
    private Timer mUpdateTimer = null;
    // 定时器执行的任务
    private TimerTask mUpdateTimerTask = null;
    // 当前全屏显示本地画面
    private boolean isFullScreenLocalVideo = true;
    // 设备端是否已进入房间
    private boolean isRemoteUserEnterRoom;
    // 记录设备端镜头的开启状态，onUserVideoAvailable回调的状态跟当前的一致时不做处理
    private Map<String, Boolean> remoteUserMap;

    // 以下为上一个页面传递的数据
    private RTCConstant.EnterParam localUserInfo = null;
    private EZCameraInfo mCameraInfo = null;
    private String ertcDeviceToken = null;
    private boolean isClientCallDevice = false;
    // 以上为上一个页面传递的数据

    // endregion

    // region --- 页面启动方法|Page startup method ---

    public static void launch(Context context, RTCConstant.EnterParam localUserInfo, EZCameraInfo cameraInfo, String ertcDeviceToken,
                              boolean isClientCallDevice, boolean isAudioOn, boolean isVideoOn) {
        Intent intent = new Intent(context, ErtcRoomActivity.class);
        intent.putExtra(IntentConsts.EXTRA_ROOM_INFO, localUserInfo);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
        intent.putExtra(IntentConsts.EXTRA_ERTC_DEVICE_TOKEN, ertcDeviceToken);
        intent.putExtra(IntentConsts.EXTRA_CLIENT_CALL_DEVICE, isClientCallDevice);
        intent.putExtra(IntentConsts.EXTRA_ROOM_AUDIO, isAudioOn);
        intent.putExtra(IntentConsts.EXTRA_ROOM_VIDEO, isVideoOn);
        context.startActivity(intent);
    }

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ertc_room);
        initData();
        initView();

        tvDevSerial.setText(localUserInfo.roomId);
        toggleMicro();// 麦克风默认关闭状态
        startUpdateTimer();

        // 设置大码流视频参数
        RTCConstant.ERTCVideoEncParam videoParam = new RTCConstant.ERTCVideoEncParam();
        videoParam.videoResolution = RTCConstant.ERTCVideoResolution.ERTCVideoResolution_1280_720;
        videoParam.videoFps = 10;
        videoParam.videoBitrate = 500 * 1024;
        ErtcHelper.ertcEngine.setVideoEncoderParam(videoParam, true);

        // 开关本地视频
        ErtcHelper.ertcEngine.enableLocalVideo(true);

        // 使用硬件回音消除，必须
        ErtcHelper.ertcEngine.enableHardAec(true);

        // 码流保存，开发调试用，线上必须关闭
        ErtcHelper.ertcEngine.setERTCDebugAudioLog(true);

        // 设置填充模式及本地预览
        vcsLocalTextureView.setRenderType(RTCConstant.ERTC_VIDEO_RENDER_MODE_FILL);
        ErtcHelper.ertcEngine.setLocalView(vcsLocalTextureView);

        // 设置事件监听
        setRTCListener();

        // 进入房间
        ErtcHelper.ertcEngine.enterRoom(localUserInfo, RTCConstant.Scene.VideoCall);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdateTimer();
    }

    // endregion

    // region --- Click点击事件|Click event ---

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llMicro) { // 麦克风开关
            toggleMicro();
        } else if (id == R.id.llSound) { // 声音开关
            toggleSound();
        } else if (id == R.id.llHangUp) { // 挂断
            toggleHangUp();
        } else if (id == R.id.llSwitchCamera) { // 摄像头切换
            ErtcHelper.ertcEngine.switchCamera();
        } else if (id == R.id.llCamera) { // 摄像头开关
            toggleCamera();
        }
    }

    /**
     * 麦克风开关
     */
    public void toggleMicro() {
        mMicroOn = !mMicroOn;
        ErtcHelper.ertcEngine.enableLocalAudio(mMicroOn);
        if (mMicroOn) {
            ivMicroState.setImageResource(R.drawable.ys_mt_svg_audio_on);
            tvMicroState.setText(R.string.ys_mt_imm_btn_audio_on);
        } else {
            ivMicroState.setImageResource(R.drawable.ys_mt_svg_audio_off);
            tvMicroState.setText(R.string.ys_mt_imm_btn_audio_off);
        }
    }

    /**
     * 声音开关
     */
    private void toggleSound() {
        mSpeakerOn = !mSpeakerOn;
        ErtcHelper.ertcEngine.setSpeakerPhoneOn(mSpeakerOn);
        if (mSpeakerOn) {
            ivSoundState.setImageResource(R.drawable.ys_mt_svg_sound_speaker);
            tvSoundState.setText(R.string.ys_mt_imm_btn_sound_speaker);
            showToast(R.string.ys_mt_room_toast_sound_speaker);
        } else {
            ivSoundState.setImageResource(R.drawable.ys_mt_svg_sound_earpiece);
            tvSoundState.setText(R.string.ys_mt_imm_btn_sound_earpiece);
            showToast(R.string.ys_mt_room_toast_sound_earpiece);
        }
    }

    /**
     * 挂断
     */
    private void toggleHangUp() {
        // 客户端呼叫设备 && 设备未接听，取消邀请设备入会
        if (isClientCallDevice && !isRemoteUserEnterRoom) {
            cancelInviteDeviceEnterMeeting();
        }
        releaseERTC("endMeeting 会议结束");
        finish();
    }

    /**
     * 摄像头开关
     */
    public void toggleCamera() {
        mCameraOn = !mCameraOn;
        if (ErtcHelper.ertcEngine != null) {
            ErtcHelper.ertcEngine.enableLocalVideo(mCameraOn);
            if (mCameraOn) {
                ivSwitchCamera.setImageResource(R.drawable.ys_mt_svg_switch_camera_enable);
                tvSwitchCamera.setTextColor(getResources().getColor(R.color.white));
                ivCameraState.setImageResource(R.drawable.ys_mt_svg_video_on);
                tvCameraState.setText(R.string.ys_mt_imm_btn_video_on);
            } else {
                ivSwitchCamera.setImageResource(R.drawable.ys_mt_svg_switch_camera_disable);
                tvSwitchCamera.setTextColor(getResources().getColor(R.color.gray));
                ivCameraState.setImageResource(R.drawable.ys_mt_svg_video_off);
                tvCameraState.setText(R.string.ys_mt_imm_btn_video_off);
            }
        }
    }

    /**
     * 本地画面点击
     */
    public void toggleLocalView() {
        // 全屏显示的是本地画面
        if (isFullScreenLocalVideo) {// 全屏显示本地画面，远程画面在小窗口中显示时，显示底部视图
            // 显示/隐藏底部视图
            showOrHideBottomView();
        } else {
            switchVcsTexturePosition();
        }
    }

    /**
     * 设备端画面点击
     */
    public void toggleRemoteView() {
        if (isFullScreenLocalVideo) {// 全屏显示本地画面，远程画面在小窗口中显示时，切换窗口
            switchVcsTexturePosition();
        } else {
            // 显示/隐藏底部视图
            showOrHideBottomView();
        }
    }

    /**
     * 交换两个播放器的位置
     */
    private void switchVcsTexturePosition() {
        isFullScreenLocalVideo = !isFullScreenLocalVideo;
        ViewGroup.LayoutParams localLayoutParams = vcsLocalTextureView.getLayoutParams();
        ViewGroup.LayoutParams remoteLayoutParams = vcsRemoteTextureView.getLayoutParams();
        vcsLocalTextureView.setLayoutParams(remoteLayoutParams);
        vcsRemoteTextureView.setLayoutParams(localLayoutParams);

        if (isFullScreenLocalVideo) {
            vcsRemoteTextureView.bringToFront();
        } else {
            vcsLocalTextureView.bringToFront();
        }
    }

    /**
     * 显示或隐藏视频通话工具栏
     */
    private void showOrHideBottomView() {
        boolean visible = viewBottom.getVisibility() == View.VISIBLE;
        if (visible) {
            viewBottom.setVisibility(View.GONE);
        } else {
            viewBottom.setVisibility(View.VISIBLE);
        }
    }

    // endregion

    // region --- ERTC事件监听|ERTC event monitoring ---

    private void setRTCListener() {
        ErtcHelper.ertcEngine.setRTCListener(new RTCListener() {

            /**
             * 用户加入房间成功，加入房间成功后将收到回调
             */
            @Override
            public void onEnterRoomSuccess() {
                super.onEnterRoomSuccess();
                // 进入房间成功
                LogUtil.d(TAG, "ErtcRoomEventListener onEnterRoomSuccess");
                // 客户端发起视频通话，邀请设备入会
                if (isClientCallDevice) {
                    inviteDeviceEnterMeeting();
                }
            }

            /**
             * 用户退出房间，非主动退出房间才会收到该回调
             * @param reason 退出房间原因
             */
            @Override
            public void onExitRoom(int reason) {
                super.onExitRoom(reason);
                // 退出房间成功
                LogUtil.d(TAG, "ErtcRoomEventListener onExitRoom");
            }

            /**
             * 远端用户进入房间成功回调，进入房间时将会收到所有已在房间内用户加入房间的回调
             * @param userId 用户id
             */
            @Override
            public void onRemoteUserEnterRoom(String userId) {
                super.onRemoteUserEnterRoom(userId);
                LogUtil.d(TAG, "ErtcRoomEventListener onRemoteUserEnterRoom RemoteUser加入房间成功 userId:" + userId);
                runOnUiThread(() -> {
                    isRemoteUserEnterRoom = true;
                    vcsRemoteTextureView.setVisibility(View.VISIBLE);
                });
            }

            /**
             * 远端用户离开房间
             * @param userId 用户ID
             * @param reason 离开原因，0表示用户主动退出房间，1表示用户超时退出。
             */
            @Override
            public void onRemoteUserLeaveRoom(String userId, int reason) {
                super.onRemoteUserLeaveRoom(userId, reason);
                LogUtil.d(TAG, "ErtcRoomEventListener onRemoteUserLeaveRoom RemoteUser离开房间成功 userId:" + userId);
                runOnUiThread(() -> {
                    showToast(R.string.ys_mt_room_toast_video_end);
                    toggleHangUp();
                });
            }

            /**
             * 远端用户打开或关闭视频流的回调
             * @param userId 远端用户id
             * @param available true-视频可用 false-视频不可用
             * @param streamType 码流类型
             */
            @Override
            public void onUserVideoAvailable(String userId, boolean available, int streamType) {
                super.onUserVideoAvailable(userId, available, streamType);
                LogUtil.d(TAG, "ErtcRoomEventListener onUserVideoAvailable userId:" + userId + ", available:" + available);
                runOnUiThread(() -> {
                    Boolean value = remoteUserMap.get(userId);
                    if (value != null && value.booleanValue() == available) {
                        LogUtil.d(TAG, "ErtcRoomEventListener duplicate message, filtered");
                        return;
                    }

                    remoteUserMap.put(userId, available);
                    if (available) {
                        vcsRemoteTextureView.setRenderType(RTCConstant.ERTC_VIDEO_RENDER_MODE_FIT);
                        ErtcHelper.ertcEngine.setRemoteView(userId, RTCConstant.ERTC_VIDEO_STREAM_TYPE_BIG, vcsRemoteTextureView);
                    } else {
                        ErtcHelper.ertcEngine.setRemoteView(userId, RTCConstant.ERTC_VIDEO_STREAM_TYPE_BIG, null);
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                super.onError(errorCode);
                // ERTC错误码文档：https://open.ys7.com/help/1825
                LogUtil.d(TAG, "ErtcRoomEventListener onError errorCode:" + errorCode);
                runOnUiThread(() -> {
                    if (errorCode == BaseError.ERR_SERVICE_ACCESSTOKEN_INVALID) {
                        showToast("ertcToken异常，请更新token后重新入会。");
                    } else {
                        showToast("errorCode:" + errorCode);
                    }
                    toggleHangUp();
                });
            }
        });
    }

    // endregion

    // region --- 自定义方法|custom method ---

    private void inviteDeviceEnterMeeting() {
        // 客户端发起视频通话，邀请设备入会
        new Thread(() -> {
            try {
                EzvizApplication.getOpenSDK().inviteDeviceEnterMeeting(localUserInfo.appId, ertcDeviceToken, localUserInfo.roomId,
                        mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), localUserInfo.userId);
            } catch (BaseException e) {
                e.printStackTrace();
                showToast("邀请设备入会失败");
                runOnUiThread(() -> {
                    toggleHangUp();
                });
            }
        }).start();
    }

    private void cancelInviteDeviceEnterMeeting() {
        // 客户端发起视频通话，取消邀请设备入会
        new Thread(() -> {
            try {
                EzvizApplication.getOpenSDK().cancelInviteDeviceEnterMeeting(localUserInfo.appId, ertcDeviceToken, localUserInfo.roomId,
                        mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), localUserInfo.userId);
            } catch (BaseException e) {
                e.printStackTrace();
                showToast("取消邀请设备入会失败");
            }
        }).start();
    }

    // 启动计时器
    private void startUpdateTimer() {
        stopUpdateTimer();
        // 开始录像计时
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                mVideoTalkSecond ++;
                runOnUiThread(() -> {
                    String convToUIDuration = RemoteListUtil.convToUIDuration(mVideoTalkSecond);
                    tvVideoTalkTime.setText(convToUIDuration);
                    // 15秒后设备仍未进入会议的话，视频通话失败，退出房间
                    if (mVideoTalkSecond > 15 && !isRemoteUserEnterRoom) {
                        showToast("设备端无人接听，视频通话结束");
                        stopUpdateTimer();
                        toggleHangUp();
                    }
                });
            }
        };
        // 延时1000ms后执行，1000ms执行一次
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    // 停止定时器
    private void stopUpdateTimer() {
        mVideoTalkSecond = 0;
        // 停止录像计时
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if (mUpdateTimerTask != null) {
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    public void releaseERTC(String cause) {
        LogUtil.i(TAG, "release===cause[" + cause + "]");
        final ERTCEngine ertcEngine = ErtcHelper.ertcEngine;
        if (ertcEngine != null) {
            ertcEngine.setLocalView(null);
            ertcEngine.setLocalViewChanged(null);
            ertcEngine.enableLocalVideo(false);
            ertcEngine.setRTCListener(null);
            ertcEngine.setRtcStatsListener(null);
            new Thread(() -> {
                ertcEngine.exitRoom();
                ERTCEngine.destroyEngine();
            }).start();
        }
    }

    // endregion

    // region ---init ---

    private void initData() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        localUserInfo = (RTCConstant.EnterParam) getIntent().getSerializableExtra(IntentConsts.EXTRA_ROOM_INFO);
        mCameraInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
        ertcDeviceToken = getIntent().getStringExtra(IntentConsts.EXTRA_ERTC_DEVICE_TOKEN);
        isClientCallDevice = getIntent().getBooleanExtra(IntentConsts.EXTRA_CLIENT_CALL_DEVICE, false);
        remoteUserMap = new HashMap<>();
    }

    private void initView() {
        tvDevSerial = findViewById(R.id.tvDevSerial);
        tvVideoTalkTime = findViewById(R.id.tvVideoTalkTime);

        viewBottom = findViewById(R.id.viewBottom);
        llMicro = findViewById(R.id.llMicro);
        ivMicroState = findViewById(R.id.ivMicroState);
        tvMicroState = findViewById(R.id.tvMicroState);
        llSound = findViewById(R.id.llSound);
        ivSoundState = findViewById(R.id.ivSoundState);
        tvSoundState = findViewById(R.id.tvSoundState);
        llHangUp = findViewById(R.id.llHangUp);
        llSwitchCamera = findViewById(R.id.llSwitchCamera);
        ivSwitchCamera = findViewById(R.id.ivSwitchCamera);
        tvSwitchCamera = findViewById(R.id.tvSwitchCamera);
        llCamera = findViewById(R.id.llCamera);
        ivCameraState = findViewById(R.id.ivCameraState);
        tvCameraState = findViewById(R.id.tvCameraState);
        llMicro.setOnClickListener(this);
        llSound.setOnClickListener(this);
        llHangUp.setOnClickListener(this);
        llCamera.setOnClickListener(this);
        llSwitchCamera.setOnClickListener(this);

        vcsLocalTextureView = findViewById(R.id.vcsLocalTextureView);
        vcsRemoteTextureView = findViewById(R.id.vcsRemoteTextureView);
        vcsLocalTextureView.setOnClickListener(v -> toggleLocalView());
        vcsRemoteTextureView.setOnClickListener(v -> toggleRemoteView());
    }

    // endregion

}