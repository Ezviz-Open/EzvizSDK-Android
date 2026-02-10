/**
 * 预览页面
 */
package com.videogo.ui.realplay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.videogo.EzvizApplication;

import ezviz.ezopensdkcommon.common.RootActivity;

import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.global.GlobalKit;
import com.videogo.global.ValueKeys;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZConstants.EZPTZAction;
import com.videogo.openapi.EZConstants.EZPTZCommand;
import com.videogo.openapi.EZConstants.EZRealPlayConstants;
import com.videogo.openapi.EZConstants.EZVideoLevel;
import com.videogo.openapi.EZConstants.EZFecPlaceType;
import com.videogo.openapi.EZConstants.EZFecCorrectType;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.EzvizAPI;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceDetailPublicInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZDevicePtzAngleInfo;
import com.videogo.openapi.bean.EZPMPlayPrivateTokenInfo;
import com.videogo.openapi.bean.EZVideoQualityInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.stream.EZPlayWaterMarkConfig;
import com.videogo.ui.devicelist.EZDeviceListActivity;
import com.videogo.ui.common.EZBusinessTool;
import com.videogo.ui.common.ScreenOrientationHelper;
import com.videogo.ui.realplay.fec.FecViewLayoutHelper;
import com.videogo.ui.realplay.popWindow.PTZPopWindow;
import com.videogo.ui.realplay.popWindow.TalkPopWindow;
import com.videogo.ui.realplay.popWindow.WatermarkPopWindow;
import com.videogo.util.AudioPlayUtil;
import com.videogo.util.DataManager;
import com.videogo.util.EZDateTimeUtil;
import com.videogo.util.EZUtils;
import com.videogo.util.SpTool;
import com.videogo.util.VerifyCodeInput;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.RotateViewUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.PtzControlAngleView;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;
import com.videogo.widget.bottommenu.BottomMenuFragment;
import com.videogo.widget.loading.LoadingTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.videogo.global.DemoConfig;

import ezviz.ezopensdk.R;

@SuppressLint({"NonConstantResourceId"})
public class EZRealPlayActivity extends RootActivity implements OnClickListener, SurfaceHolder.Callback,
        Handler.Callback, VerifyCodeInput.VerifyCodeInputListener {

    // region --- 全局变量|global variable ---

    private static final String TAG = EZRealPlayActivity.class.getSimpleName();

    public static final int MSG_PLAY_UI_UPDATE = 200;// UI消息
    public static final int MSG_HIDE_PTZ_ANGLE = 204;// 云台角度比例尺隐藏消息

    private String mRtspUrl = null;// 通过rtsp协议url进行预览

    private AudioPlayUtil mAudioPlayUtil = null;
    private LocalInfo mLocalInfo = null;
    private Handler mHandler = null;

    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    private int mStatus = RealPlayStatus.STATUS_INIT;
    private boolean mIsOnStop = false;// 是否停止录制中
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mForceOrientation = 0;
    // 状态栏Rect
    private Rect mRealPlayRect = null;

    private LinearLayout mRealPlayPageLy;// 预览页面
    private TitleBar mPortraitTitleBar;// 竖屏导航栏
    private TitleBar mLandscapeTitleBar;// 横屏导航栏
    private RelativeLayout mRealPlayPlayRl;// 预览UI父视图

    private SurfaceView mRealPlaySv;
    private SurfaceView mRealPlaySecondSv;
    private SurfaceHolder mRealPlaySh;
    private SurfaceHolder mRealPlaySecondSh;
    private CustomTouchListener mRealPlayTouchListener;// 播放器手势监听器
    private CustomTouchListener mRealPlayTouchListener2;// 双目设备第二个播放器手势监听器
    // 矫正模式分屏
    private RelativeLayout realPlayPtzRL;
    private SurfaceView mRealPlaySv1;
    private SurfaceView mRealPlaySv2;
    private SurfaceView mRealPlaySv3;
    private SurfaceView mRealPlaySv4;
    private SurfaceView mRealPlaySv5;
    private SurfaceView mRealPlaySv6;

    private ImageView mCoverImage;// 封面
    private ImageView mCoverImage2;// 双目设备第二个封面
    private PtzControlAngleView mPtzControlAngleViewVer;// 云台垂直比例尺
    private PtzControlAngleView mPtzControlAngleViewHor;// 云台水平比例尺
    private boolean isDevicePtzAngleInited;// 设备云台比例尺是否初始化过，如果是NO，接收到数据后需要初始化下
    private TextView streamTypeTv;// 取流方式
    private TextView mRealPlayMoreStreamInfoTv;// 更多码流信息开关
    private LinearLayout mRealPlayMoreStreamInfoLL;// 更多码流信息父视图

    private RelativeLayout mRealPlayLoadingRl;// 预览加载父视图
    private TextView mRealPlayTipTv;// 错误信息提示文本
    private ImageView mRealPlayPlayIv;// 播放视图正中间播放按钮
    private LoadingTextView mRealPlayPlayLoading;// 播放加载进程UI

    private LinearLayout mRealPlayControlRl;// 播放器下方的工具条
    private ImageButton mRealPlayBtn;// 播放/暂停
    private ImageButton mRealPlaySoundBtn;// 声音开关
    private Button mRealPlayQualityBtn;// 清晰度设置
    private TextView mRealPlayFlowTv;// 预览每秒接收到的码流大小显示
    private CheckTextButton mFullscreenButton;// 放大

    private LinearLayout mRealPlayRecordLy;// 录像过程中时长显示布局
    private ImageView mRealPlayRecordIv;// 录像过程中的红色小圆点
    private TextView mRealPlayRecordTv;// 录像过程中的录制时长

    private int mControlDisplaySec = 0;// 计时，5秒后关闭某些弹出框
    private boolean isRecording = false;// 是否正常录像中
    private int mRecordSecond = 0;// 录制时长

    // 以下为竖屏状态下，操控栏UI
    private HorizontalScrollView mRealPlayOperateBar;// 操控栏
    private ImageButton mRealPlayPtzBtn;// 云台
    private ImageButton mRealPlayTalkBtn;// 对讲
    private ImageButton mRealPlayWatermarkBtn;// 水印设置
    private ImageButton mRealPlayCaptureBtn;// 截图
    private ImageButton mRealPlaySnapshotUplaodBtn;// 云录制截图上传
    private View mRealPlayRecordContainer;// 录像容器
    private ImageButton mRealPlayRecordBtn;// 录像未录制状态图标
    private ImageButton mRealPlayRecordStartBtn;// 录像录制中图标
    private RotateViewUtil mRecordRotateViewUtil;// 录像图标点击动画效果
    // 以上为竖屏状态下，操控栏UI

    private boolean mIsOnPtz = false;// 云台操作中
    private PTZPopWindow mPtzPopupWindow;// 云台控制pop
    private boolean mIsOnTalk = false;// 对讲中
    private boolean mIsStartingTalk = false;// 是否在启动对讲，开启预览的时候需要设置为false
    private TalkPopWindow mTalkPopupWindow;// 对讲pop

    private boolean isWaterMarkShow = false;// 是否显示水印
    private EZPlayWaterMarkConfig waterMarkConfig;// 水印设置

    private PopupWindow mFecPopupWindow;// 鱼眼矫正模式pop
    private Button[] fecCorrectTypeButtons;// 鱼眼设备矫正模式按钮数组
    private EZFecPlaceType fecPlaceType;// 鱼眼安装模式
    private EZFecCorrectType fecCorrectType;// 鱼眼矫正模式
    private FecViewLayoutHelper fecViewLayoutHelper;// 鱼眼辅助类

    private WaitDialog mWaitDialog;// 设置清晰度dialog

    private RealPlayBroadcastReceiver mBroadcastReceiver;// 监听手机息屏广播
    private Timer mUpdateTimer;// 预览成功后，一秒刷新一次（预览每秒接收到的码流大小、录像计时、弹出框隐藏）
    private TimerTask mUpdateTimerTask;
    private ScreenOrientationHelper mScreenOrientationHelper;

    private EZPlayer mEZPlayer = null;
    private CheckTextButton mFullScreenTitleBarBackBtn;// 全屏返回

    private boolean isQulityModeAuto = false;// 当前设备本地清晰度 是否是"自动"
    private EZVideoLevel mCurrentQulityMode = EZVideoLevel.VIDEO_LEVEL_HD;// 当前设备清晰度，默认高清
    // 以下为设备列表页面通过Bundle传递的数据
    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;
    // 以上为设备列表页面通过Bundle传递的数据
    private boolean isMultiChannelDevice;// 是否是双目设备，如果接入的设备没有双目设备，可以忽略相关逻辑
    private EZDeviceDetailPublicInfo mDeviceDetailPublicInfo;// 取流详情数据

    private long mStreamFlow = 0;// 播放接收到的码流总大小
    private long mStartTime = 0;// 取流开始时间

    // 视频宽高
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean isFromPermissionSetting;// true为应用权限管理返回

    // endregion

    // region --- 页面启动方法|Page startup method ---

    public static void launch(Context context, EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        Intent intent = new Intent(context, EZRealPlayActivity.class);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
        context.startActivity(intent);
    }

    public static void launch(Context context, String deviceSerial, int cameraNo) {
        EZCameraInfo cameraInfo = new EZCameraInfo();
        cameraInfo.setDeviceSerial(deviceSerial);
        cameraInfo.setCameraNo(cameraNo);
        launch(context, new EZDeviceInfo(), cameraInfo);
    }

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_realplay_page);
        // 保持屏幕常亮 Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initData();// 数据初始化
        initView();// 视图初始化设置
        initEZPlayer();// 播放器初始化
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如下代码
        initFecView();
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如上代码
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromPermissionSetting) {
            checkPermissions();
            isFromPermissionSetting = false;
        }
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return;
        }
        // 关闭软键盘
        new Handler().postDelayed(() -> {
            if (mRealPlaySv != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mRealPlaySv.getWindowToken(), 0);
            }
        }, 200);

        LogUtil.i(TAG, "onResume real play status:" + mStatus);
        if (mStatus == RealPlayStatus.STATUS_PAUSE || mStatus == RealPlayStatus.STATUS_DECRYPT) {
            playerStartRealPlay();// 开始播放
        }
        mIsOnStop = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mScreenOrientationHelper != null) {
            mScreenOrientationHelper.postOnStop();
        }

        if (mCameraInfo == null && mRtspUrl == null) {
            return;
        }

        closePtzPopupWindow();
        closeTalkPopupWindow(true);
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            setRealPlayResult(false);
            mIsOnStop = true;
            mStatus = RealPlayStatus.STATUS_PAUSE;
        } else {
            setStopLoading();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEZPlayer != null) {
            mEZPlayer.release();
        }
        mHandler.removeMessages(MSG_HIDE_PTZ_ANGLE);
        mHandler = null;

        if (mBroadcastReceiver != null) {
            // 取消锁屏广播的注册 Cancel the registration of the lock screen broadcast
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        mScreenOrientationHelper = null;
    }

    @Override
    public void finish() {
        if (mCameraInfo != null) {
            // 更新设备列表 该设备的清晰度
            Intent intent = new Intent();
            intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mCameraInfo.getDeviceSerial());
            intent.putExtra(IntentConsts.EXTRA_CAMERA_NO, mCameraInfo.getCameraNo());
            intent.putExtra("video_level", mCameraInfo.getVideoLevel().getVideoLevel());
            setResult(EZDeviceListActivity.RESULT_CODE, intent);
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        exit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;

        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    // endregion

    // region --- 播放器消息回调|Player message callback ---

    @Override
    public boolean handleMessage(Message msg) {
        if (this.isFinishing()) {
            return false;
        }
        LogUtil.i(TAG, "handleMessage:" + msg.what);
        switch (msg.what) {
            // 以下消息为SDK发送的消息，用于取流相关消息回调
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:// 播放成功
                handlePlaySucceed();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:// 播放失败
                handlePlayFailed((ErrorInfo) msg.obj);
                break;
            case EZRealPlayConstants.MSG_PTZ_GET_SUCCESS:// 设备云台角度数据回调
                handleDevicePtzAngleInfo(msg.obj);
                break;
            case EZRealPlayConstants.MSG_PRIVATE_TOKEN_GET_SUCCESS:// 智能门锁开门token回调
                // 如果门锁设备没有回调开门token的话，请确认该设备的远程开锁开关是否打开。萤石云视频App入口：设备设置-开锁设置-远程开锁-开启远程开锁
                EZPMPlayPrivateTokenInfo tokenInfo = (EZPMPlayPrivateTokenInfo)msg.obj;
                LogUtil.e(TAG, "token--->" + tokenInfo.getToken());
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_SUCCESS:// 对讲成功
                handleVoiceTalkSucceed();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_STOP:// 对讲结束
                handleVoiceTalkStoped();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_FAIL:// 对讲失败
                handleVoiceTalkFailed((ErrorInfo) msg.obj);
                break;
            case EZConstants.MSG_VIDEO_SIZE_CHANGED:// 播放器尺寸变化
                try {
                    String temp = (String) msg.obj;
                    String[] strings = temp.split(":");
                    mVideoWidth = Integer.parseInt(strings[0]);
                    mVideoHeight = Integer.parseInt(strings[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EZRealPlayConstants.MSG_VIDEO_LEVEL_AUTO_IMPROVE:// 清晰度升档，EZPlayer.enableDeviceAutoVideolevel开关打开后才会有此回调
                if (isQulityModeAuto && !isRecording) {// 录制的时候不能切换，涉及到分辨率的变化，会影响转码
                    EZVideoLevel higherVideoLevel = EZBusinessTool.getHigherVideoLevel(mCurrentQulityMode, mDeviceDetailPublicInfo);
                    if (higherVideoLevel.getVideoLevel() > mCurrentQulityMode.getVideoLevel()) {
                        setQualityModeAuto(higherVideoLevel);
                    }
                }
                break;
            case EZRealPlayConstants.MSG_VIDEO_LEVEL_AUTO_REDUCE:// 清晰度降档，EZPlayer.enableDeviceAutoVideolevel开关打开后才会有此回调
                if (isQulityModeAuto && !isRecording) {
                    EZVideoLevel lowerVideoLevel = EZBusinessTool.getLowerVideoLevel(mCurrentQulityMode, mDeviceDetailPublicInfo);
                    if (lowerVideoLevel.getVideoLevel() < mCurrentQulityMode.getVideoLevel()) {
                        setQualityModeAuto(lowerVideoLevel);
                    }
                }
                break;
            // 以上消息为SDK发送的消息，用于取流相关消息回调

            // 以下消息为该页面发送的消息，用于UI更新
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:// 设置清晰度成功
                handleSetVideoModeSuccess();
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:// 设置清晰度失败
                handleSetVideoModeFail(msg.arg1);
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_AUTO_SUCCESS:// 设置自动清晰度成功
                getDeviceDetailInfoAndRefreshUI();
                break;
            case MSG_PLAY_UI_UPDATE:// 预览成功后UI更新，一秒刷新一次
                updateRealPlayUI();
                break;
            case MSG_HIDE_PTZ_ANGLE:// 云台角度比例尺隐藏消息
                mPtzControlAngleViewVer.setVisibility(View.GONE);
                mPtzControlAngleViewHor.setVisibility(View.GONE);
                break;
            // 以上消息为该页面发送的消息，用于UI更新
            default:
                // do nothing
                break;
        }
        return false;
    }

    /**
     * 播放失败后处理
     *
     * @param errorInfo 错误信息
     */
    private void handlePlayFailed(ErrorInfo errorInfo) {
        LogUtil.d(TAG, "handlePlayFail. realPlay failed. error info is " + errorInfo.toString());
        mStatus = RealPlayStatus.STATUS_STOP;
        setRealPlayResult(false);
        // 获取设备详情 & 刷新视图
        getDeviceDetailInfoAndRefreshUI();
        switch (errorInfo.errorCode) {
            // 收到这两个错误码，可以弹出对话框，让用户输入密码后，重新取流预览
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {
                VerifyCodeInput.VerifyCodeInputDialog(this, this).show();
            }
            break;
            default:// 显示错误码信息
                String errorInfoText = Utils.getErrorTip(this, R.string.realplay_play_fail, errorInfo.errorCode);
                mRealPlayTipTv.setVisibility(View.VISIBLE);
                mRealPlayTipTv.setText(errorInfoText);
                showToast(errorInfo.toString());
                break;
        }
    }

    /**
     * 播放成功后处理
     */
    private void handlePlaySucceed() {
        LogUtil.d(TAG, "handlePlaySucceed");
        mStatus = RealPlayStatus.STATUS_PLAY;
        // 1.视图状态设置
        setRealPlayResult(true);
        // 2.设置取流方式 & 更多 UI
        showStreamFetchType();
        mRealPlayMoreStreamInfoTv.setVisibility(View.VISIBLE);
        mRealPlayMoreStreamInfoLL.setVisibility(View.GONE);
        // 3.获取设备详情更新清晰度
        mDeviceDetailPublicInfo = this.mEZPlayer.getDeviceDetailInfo();
        mCameraInfo.setVideoLevel(mDeviceDetailPublicInfo.videoLevel);
        mCurrentQulityMode = mCameraInfo.getVideoLevel();
        setVideoLevelUI();
        // 4.声音设置  Sound setting
        setRealPlaySound();
        // 5.开启计时，每秒刷新UI
        startUpdateTimer();

        updateOrientation();
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如下代码
        if (fecViewLayoutHelper != null) {
            fecViewLayoutHelper.openFecCorrect(fecCorrectType, fecPlaceType);
        }
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如上代码
    }

    /**
     * 云台角度比例尺更新
     * @param obj
     */
    private void handleDevicePtzAngleInfo(Object obj) {
        if (mPtzControlAngleViewHor.getVisibility() == View.VISIBLE || mPtzControlAngleViewVer.getVisibility() == View.VISIBLE) {
            EZDevicePtzAngleInfo info = (EZDevicePtzAngleInfo) obj;
            mPtzControlAngleViewHor.setAngle(info.getHorStartAng(), info.getHorEndAng(), info.getHorCurAng());
            mPtzControlAngleViewVer.setAngle(info.getVerStartAng(), info.getVerEndAng(), info.getVerCurAng());
        }
        if (!isDevicePtzAngleInited) {
            isDevicePtzAngleInited = true;
            EZDevicePtzAngleInfo info = (EZDevicePtzAngleInfo) obj;
            mPtzControlAngleViewHor.setAngle(info.getHorStartAng(), info.getHorEndAng(), info.getHorCurAng());
            mPtzControlAngleViewVer.setAngle(info.getVerStartAng(), info.getVerEndAng(), info.getVerCurAng());
        }
    }

    /**
     * 对讲成功后处理
     */
    private void handleVoiceTalkSucceed() {
        openTalkPopupWindow(true);
        mRealPlayTalkBtn.setEnabled(true);
    }

    /**
     * 对讲结束后处理
     */
    private void handleVoiceTalkStoped() {
        if (mIsOnTalk) {
            mIsOnTalk = false;
            setForceOrientation(0);
        }
        if (mStatus == RealPlayStatus.STATUS_PLAY && mEZPlayer != null) {
            if (mLocalInfo.isSoundOpen()) {
                mEZPlayer.openSound();
            } else {
                mEZPlayer.closeSound();
            }
        }
    }

    /**
     * 对讲失败后处理
     */
    private void handleVoiceTalkFailed(ErrorInfo errorInfo) {
        LogUtil.d(TAG, "Talkback failed. " + errorInfo.toString());
        closeTalkPopupWindow(true);
        if (errorInfo.errorCode == ErrorCode.ERROR_CHANNEL_NO_SUPPORT_TALKBACK) {
            Utils.showToast(EZRealPlayActivity.this, R.string.device_no_support_talkback, errorInfo.errorCode);
        } else {
            Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail, errorInfo.errorCode);
        }
    }

    // endregion

    // region --- Click点击事件|Click event ---

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.realplay_play_btn || id == R.id.realplay_play_iv) {// 播放器下方的工具条中第一个播放/暂停按钮 || 播放器正中央的暂停/播放按钮
            onRealPlayClick();
        } else if (id == R.id.realplay_sound_btn) {// 声音控制Click
            onSoundBtnClick();
        } else if (id == R.id.realplay_quality_btn) {// 清晰度设置Click
            openQualityWindow();
        } else if (id == R.id.realplay_ptz_btn) {// 云台Click
            openPtzPopupWindow();
        } else if (id == R.id.realplay_talk_btn) {// 对讲Click
            checkAndRequestPermission();
        } else if (id == R.id.realplay_watermark_btn) {// 水印Click
            openWatermarkPopupWindow();
        } else if (id == R.id.realplay_previously_btn) {// 截图Click
            onCapturePicBtnClick();
        } else if (id == R.id.realplay_video_btn || id == R.id.realplay_video_start_btn) {// 录像开始Click 或 录像结束Click
            onRecordBtnClick();
        } else if (id == R.id.realplay_snapshot_upload_btn) {// 云录制截图上传
            onCapturePicUploadBtnClick();
        } else if (id == R.id.tv_stream_info_more) {// 更多 流数据展示
            showStreamInfoMore();
        }
    }

    /**
     * 暂停/播放Action
     */
    private void onRealPlayClick() {
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            // 暂停前获取最后一帧作为封面
            Bitmap bitmap = (isWaterMarkShow && !isMultiChannelDevice) ?
                    mEZPlayer.captureRenderPicture(mRealPlaySv.getWidth(), mRealPlaySv.getHeight()) : mEZPlayer.capturePicture();
            mCoverImage.setImageBitmap(bitmap);
            if (isMultiChannelDevice) {
                Bitmap bitmap2 = mEZPlayer.capturePicture(1);
                mCoverImage2.setImageBitmap(bitmap2);
            }
            setRealPlayResult(false);
        } else {
            playerStartRealPlay();
        }
    }

    /**
     * 声音开关Action
     */
    private void onSoundBtnClick() {
        if (mLocalInfo.isSoundOpen()) {
            mLocalInfo.setSoundOpen(false);
            mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_off_selector);
        } else {
            mLocalInfo.setSoundOpen(true);
            mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_selector);
        }
        setRealPlaySound();
    }

    /**
     * 截图Action
     */
    private void onCapturePicBtnClick() {
        if (mEZPlayer == null) {
            return;
        }
        Thread thr = new Thread() {
            @Override
            public void run() {
                mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);
                Bitmap bmp = (isWaterMarkShow && !isMultiChannelDevice) ?
                        mEZPlayer.captureRenderPicture(mRealPlaySv.getWidth(), mRealPlaySv.getHeight()) : mEZPlayer.capturePicture();
                saveCapturePictrue(bmp);
                if (isMultiChannelDevice) {// 双目设备截图
                    Bitmap bmp2 = mEZPlayer.capturePicture(1);
                    saveCapturePictrue(bmp2);
                }
                super.run();
            }
        };
        thr.start();
    }

    private void saveCapturePictrue(Bitmap bmp) {
        if (bmp != null) {
            try {
                final String strCaptureFile = DemoConfig.getCapturesFolder() + "/" + System.currentTimeMillis() + ".jpg";
                LogUtil.e(TAG, "captured picture file path is " + strCaptureFile);

                if (TextUtils.isEmpty(strCaptureFile)) {
                    bmp.recycle();
                    bmp = null;
                    return;
                }
                // 将截图bitmap保存至文件
                EZUtils.saveCapturePictrue(strCaptureFile, bmp);
                // TODO 将文件保存至相册，需要申请动态权限WRITE_EXTERNAL_STORAGE，由开发者自行实现
                // EZUtils.savePicture2Album(EZRealPlayActivity.this, bmp);
                showToast(getResources().getString(R.string.already_saved_to) + strCaptureFile);
            } catch (InnerException e) {
                e.printStackTrace();
            } finally {
                if (bmp != null) {
                    bmp.recycle();
                    bmp = null;
                    return;
                }
            }
        } else {
            showToast("抓图失败, 请重试或检查是否开启了硬件解码");
        }
    }

    private String mCurrentRecordPath = null;// 录像文件保存路径

    /**
     * 录像录制Action
     */
    private void onRecordBtnClick() {
        mControlDisplaySec = 0;
        if (isRecording) {
            stopRealPlayRecord();
            return;
        }
        if (mEZPlayer != null) {
            /**
             * 此路径必须指定为沙盒路径；不能指定为相册路径，部分机型、新系统上有限制
             * This path must be specified as a sandbox path; Cannot be specified as album path, some models and new systems have restrictions
             */
            final String strRecordFile = DemoConfig.getRecordsFolder() + "/" + System.currentTimeMillis() + ".mp4";
            LogUtil.i(TAG, "recorded video file path is " + strRecordFile);
            // 设置录制回调
            mEZPlayer.setStreamDownloadCallback(new EZOpenSDKListener.EZStreamDownloadCallback() {
                @Override
                public void onSuccess(String filepath) {
                    LogUtil.i(TAG, "EZStreamDownloadCallback onSuccess " + filepath);
                    dialog("Record result", "saved to " + mCurrentRecordPath);
                    // TODO 将录制的视频保存到相册，需要申请动态权限WRITE_EXTERNAL_STORAGE，由开发者自行实现
                    // EZUtils.saveVideo2Album(EZRealPlayActivity.this, new File(filepath));
                }

                @Override
                public void onError(EZOpenSDKListener.EZStreamDownloadError code) {
                    LogUtil.e(TAG, "EZStreamDownloadCallback onError " + code.name());
                }
            });
            boolean result;
            if (isWaterMarkShow && !isMultiChannelDevice) {// 水印录制
                result = mEZPlayer.startRenderRecordWithFile(strRecordFile);
            } else {// 普通录制
                result = mEZPlayer.startLocalRecordWithFile(strRecordFile);
            }
            if (result) {
                isRecording = true;
                mCurrentRecordPath = strRecordFile;
                mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
                handleRecordSuccess();
            } else {
                handleRecordFail();
            }
        }
    }

    /**
     * 停止录像录制
     */
    private void stopRealPlayRecord() {
        if (mEZPlayer == null || !isRecording) {
            return;
        }
        // 设置录像按钮为check状态 | Set the recording button to the check status
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, mRealPlayRecordStartBtn,
                        mRealPlayRecordBtn, 0, 90);
            } else {
                mRealPlayRecordStartBtn.setVisibility(View.GONE);
                mRealPlayRecordBtn.setVisibility(View.VISIBLE);
            }
        } else {
            mRealPlayRecordStartBtn.setVisibility(View.GONE);
            mRealPlayRecordBtn.setVisibility(View.VISIBLE);
        }
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        if (isWaterMarkShow && !isMultiChannelDevice) {// 停止水印录制
            mEZPlayer.stopRenderRecord();
        } else {// 停止普通录制
            mEZPlayer.stopLocalRecord();
        }
        // 计时按钮不可见 | The timed button is not visible
        mRealPlayRecordLy.setVisibility(View.GONE);
        isRecording = false;
    }

    /**
     * 云录制截图上传Action
     */
    private void onCapturePicUploadBtnClick() {
        if (mEZPlayer == null) {
            return;
        }
        Thread thr = new Thread() {
            @Override
            public void run() {
                Bitmap bmp = mEZPlayer.capturePicture();
                if (bmp != null) {
                    try {
                        String storageId = EzvizApplication.getOpenSDK().uploadSDKCloudRecordImage(bmp);
                        if (!TextUtils.isEmpty(storageId)) {
                            copyDialog("上传成功，storageId:", storageId);
                        }
                    } catch (BaseException e) {
                        showToast("上传失败" + e.getErrorCode());
                        e.printStackTrace();
                    }
                } else {
                    showToast("抓图失败, 请重试或检查是否开启了硬件解码");
                }
                super.run();
            }
        };
        thr.start();
    }

    // endregion

    // region --- 清晰度设置相关|Clarity settings related ---

    /**
     * 设置视频清晰度UI
     */
    private void setVideoLevelUI() {
        if (mCameraInfo == null || mEZPlayer == null || mDeviceInfo == null) {
            return;
        }
        mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1);

        /**************
         * 本地数据保存 需要更新之前获取到的设备列表信息，开发者自己设置
         *
         * Local data saved need to be updated before the obtained device list information, the developer's own settings
         * *********************/
        mCameraInfo.setVideoLevel(mCurrentQulityMode.getVideoLevel());
        mRealPlayQualityBtn.setText(EZBusinessTool.getDeviceQualityTitle(this, mCurrentQulityMode.getVideoLevel()));
    }

    /**
     * 清晰度设置成功后处理
     */
    private void handleSetVideoModeSuccess() {
        setVideoLevelUI();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 停止对讲 Stop talk
        closeTalkPopupWindow(true);
        // 停止播放 Stop play
        playerStopRealPlay();
        SystemClock.sleep(500);
        // 开始播放 Start play
        playerStartRealPlay();
    }

    /**
     * 清晰度设置失败后处理
     */
    private void handleSetVideoModeFail(int errorCode) {
        setVideoLevelUI();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.showToast(EZRealPlayActivity.this, R.string.realplay_set_vediomode_fail, errorCode);
    }

    /**
     * 打开清晰度设置弹出框
     */
    private void openQualityWindow() {
        if (mEZPlayer == null) {
            return;
        }
        boolean isSupportDeviceAutoVideolevel = EZBusinessTool.isSupportDeviceAutoVideolevel(mDeviceInfo, mCameraInfo);
        // 当前清晰度
        String currentVideoQuality = EZBusinessTool.getDeviceQualityTitle(this, mCurrentQulityMode.getVideoLevel());

        List<EZVideoQualityInfo> videoQualityInfos = Arrays.asList(
                new EZVideoQualityInfo(getResources().getString(R.string.quality_flunet), EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()),
                new EZVideoQualityInfo(getResources().getString(R.string.quality_balanced), EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()),
                new EZVideoQualityInfo(getResources().getString(R.string.quality_hd), EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()));
        // 如果设备有上报清晰度数据，使用上报的数据
        if (mDeviceDetailPublicInfo != null && mDeviceDetailPublicInfo.videoQualityInfos != null) {
            videoQualityInfos = mDeviceDetailPublicInfo.videoQualityInfos;
        }

        List<String> videoQualitys = new ArrayList<>();
        for (int i = 0; i < videoQualityInfos.size(); i++) {
            String qualityName = videoQualityInfos.get(i).getVideoQualityName();
            videoQualitys.add(qualityName != null ? qualityName: "");
        }
        // 如果设备支持，清晰度列表中加入"自动"选项 或 "自动(具体清晰度)"
        if (isSupportDeviceAutoVideolevel) {
            if (isQulityModeAuto) {
                videoQualitys.add(getResources().getString(R.string.quality_auto_select, currentVideoQuality));
            } else {
                videoQualitys.add(getResources().getString(R.string.quality_auto));
            }
        }
        List<EZVideoQualityInfo> finalVideoQualityInfos = videoQualityInfos;
        // 自动清晰度时文本：自动(具体清晰度)    非自动清晰度时文本：具体清晰度
        String selectVideoQuality =
                isQulityModeAuto ? getResources().getString(R.string.quality_auto_select, currentVideoQuality) : currentVideoQuality;
        new BottomMenuFragment(this)
                .setHeadTitle(getResources().getString(R.string.quality_select_tip))
                .addMenuItems(videoQualitys)
                .setSelectContent(selectVideoQuality)
                .setOnItemClickListener(position -> {
                    isQulityModeAuto = videoQualitys.get(position).contains(getResources().getString(R.string.quality_auto));
                    // 本地缓存设备当前状态是否是"自动清晰度"，不支持的不用管理状态
                    if (isSupportDeviceAutoVideolevel) {
                        SpTool.storeBooleanValue(ValueKeys.QUALITY_AUTO + "_" + mCameraInfo.getDeviceSerial(), isQulityModeAuto);
                    }
                    // 选择"自动"，重新取流，接收到通知消息后再切换清晰度；
                    if (isQulityModeAuto) {
                        // 停止播放 Stop play
                        playerStopRealPlay();
                        SystemClock.sleep(500);
                        mEZPlayer.enableDeviceAutoVideoLevel();
                        // 开始播放 Start play
                        playerStartRealPlay();
                    } else {// 选择具体的清晰度时，切换清晰度
                        EZCameraInfo cameraInfo = new EZCameraInfo();
                        cameraInfo.setVideoLevel(finalVideoQualityInfos.get(position).getVideoLevel());
                        setQualityMode(cameraInfo.getVideoLevel());
                    }
                })
                .show();
    }

    /**
     * 设置清晰度请求
     * @param mode
     */
    private void setQualityMode(final EZVideoLevel mode) {
        // 检查网络是否可用 Check if the network is available
        if (!ConnectionDetector.isNetworkAvailable(EZRealPlayActivity.this)) {
            // 提示没有连接网络 Prompt not to connect to the network
            Utils.showToast(EZRealPlayActivity.this, R.string.realplay_set_fail_network);
            return;
        }

        if (mEZPlayer != null) {
            mWaitDialog.setWaitText(this.getString(R.string.setting_video_level));
            mWaitDialog.show();

            Thread thr = new Thread(() -> {
                try {
                    EzvizApplication.getOpenSDK().setVideoLevel(
                            mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), mode.getVideoLevel());
                    mCurrentQulityMode = mode;
                    Message msg = Message.obtain();
                    msg.what = EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS;
                    mHandler.sendMessage(msg);
                    LogUtil.i(TAG, "setQualityMode success");
                } catch (BaseException e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL;
                    mHandler.sendMessage(msg);
                    LogUtil.i(TAG, "setQualityMode fail");
                }

            });
            thr.start();
        }
    }

    /**
     * 设置自动清晰度请求
     * @param mode
     */
    private void setQualityModeAuto(final EZVideoLevel mode) {
        if (mEZPlayer != null) {
            Thread thr = new Thread(() -> {
                try {
                    EzvizApplication.getOpenSDK().setVideoLevelAuto(mCameraInfo.getDeviceSerial(),
                            mCameraInfo.getCameraNo(), mode.getVideoLevel());
                    mCurrentQulityMode = mode;
                    Message msg = Message.obtain();
                    msg.what = EZRealPlayConstants.MSG_SET_VEDIOMODE_AUTO_SUCCESS;
                    mHandler.sendMessage(msg);
                    LogUtil.i(TAG, "setQualityModeAuto success");
                } catch (BaseException e) {
                    e.printStackTrace();
                    LogUtil.i(TAG, "setQualityModeAuto fail");
                    // 失败不用处理
                }

            });
            thr.start();
        }
    }

    // endregion

    // region --- 云台控制相关|PTZ control related ---

    private void openPtzPopupWindow() {
        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
        mPtzPopupWindow = new PTZPopWindow(this, mRealPlayPlayRl, height, mPtzControlAngleViewHor, mPtzControlAngleViewVer);
        mPtzPopupWindow.setPTZPopupWindowListener(new PTZPopWindow.OnPTZPopupWindowListener() {
            @Override
            public void onPtzPopWindowOpen(boolean open) {
                mIsOnPtz = open;
            }

            @Override
            public void onPtzOption(EZPTZCommand command, EZPTZAction action) {
                ptzOption(command, action);
            }
        });
        mPtzPopupWindow.openPtzPopupWindow();
    }

    /**
     * 云台操作请求
     * @param command 方向
     * @param action 开始or停止
     */
    private void ptzOption(final EZPTZCommand command, final EZPTZAction action) {
        new Thread(() -> {
            boolean ptz_result = false;
            try {
                ptz_result = EzvizApplication.getOpenSDK().controlPTZ(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), command,
                        action, EZConstants.PTZ_SPEED_FAST);
                if (action == EZPTZAction.EZPTZActionSTOP) {
                    Message msg = Message.obtain();
                    msg.what = MSG_HIDE_PTZ_ANGLE;
                    mHandler.sendMessage(msg);
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
            LogUtil.i(TAG, "controlPTZ ptzCtrl result: " + ptz_result);
        }).start();
    }

    private void closePtzPopupWindow() {
        if (mPtzPopupWindow != null) {
            mPtzPopupWindow.closePtzPopupWindow();
        }
    }

    // endregion

    // region --- 对讲相关|VoiceTalk related ---

    /**
     * 对讲Action
     */
    private void selectTalkbackItems() {
        List<String> talkbackTypes = Arrays.asList(
                getResources().getString(R.string.talkback_current_device),
                getResources().getString(R.string.talkback_nvr_channel_device));
        new BottomMenuFragment(this)
                .setHeadTitle(getResources().getString(R.string.talkback_select_tip))
                .addMenuItems(talkbackTypes)
                .setOnItemClickListener(position -> {
                    if (position == 0) {// 当前设备对讲
                        startVoiceTalk(true);
                    } else {// 选择NVR-通道设备对讲
                        startVoiceTalk(false);
                    }
                })
                .show();
    }

    /**
     * 发起对讲
     * @param isDeviceTalkBack true:当前设备对讲  false:nvr下的ipc设备对讲
     */
    private void startVoiceTalk(boolean isDeviceTalkBack) {
        LogUtil.d(TAG, "startVoiceTalk");
        if (mCameraInfo == null) {
            LogUtil.d(TAG, "mCameraInfo is null");
            return;
        }
        mIsOnTalk = true;
        mIsStartingTalk = true;

        updateOrientation();

        Utils.showToast(this, R.string.start_voice_talk);
        mRealPlayTalkBtn.setEnabled(false);
        // 对讲开启前，关闭播放器的声音；否则画面码流和对讲码流中同时播放声音，导致回音
        // Turn off the sound of the player before starting the intercom; Otherwise,
        // playing sound simultaneously in both the picture stream and the intercom stream
        if (mEZPlayer != null) {
            mEZPlayer.closeSound();
        }
        if (!EzvizAPI.getInstance().isUsingGlobalSDK() && EZOpenSDK.isEnableSDKWithTKToken()) {
            mEZPlayer.setStreamToken(GlobalKit.getInstance().talkStreamToken);
        }
        mEZPlayer.setVoiceTalkLoudnessCallback(v -> {
            LogUtil.d(TAG, "VoiceTalkLoudness--->" + v);
            runOnUiThread(() -> {
                if (mTalkPopupWindow != null) {
                    mTalkPopupWindow.setTalkLocalLoudness(v);
                }
            });
        }, 0.15f);
        mEZPlayer.startVoiceTalk(isDeviceTalkBack);
    }

    /**
     * 停止对讲
     */
    private void stopVoiceTalk() {
        if (mCameraInfo == null || mEZPlayer == null) {
            return;
        }
        LogUtil.d(TAG, "stopVoiceTalk");

        mEZPlayer.stopVoiceTalk();
        handleVoiceTalkStoped();
    }

    private void openTalkPopupWindow(boolean showAnimation) {
        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
        mTalkPopupWindow = new TalkPopWindow(this, mRealPlayPlayRl, height,
                EZBusinessTool.isSupportTalk(mDeviceInfo, mCameraInfo) == EZConstants.EZTalkbackCapability.EZTalkbackFullDuplex);
        mTalkPopupWindow.setTalkPopupWindowListener(new TalkPopWindow.OnTalkPopupWindowListener() {
            @Override
            public void onVoiceTalkStatusChanged(boolean pressed) {
                mEZPlayer.setVoiceTalkStatus(pressed);
            }

            @Override
            public void onVoiceTalkRemoteMuted(boolean muted) {
                mEZPlayer.setTalkRemoteMuted(muted);
            }

            @Override
            public void onVoiceTalkVoiceChangeClick(EZConstants.EZVoiceChangeType voiceChangeType) {
                mEZPlayer.startVoiceChange(voiceChangeType, (isSuccess, errorInfo) -> runOnUiThread(() -> {
                    if (isSuccess) {
                        showToast("voice change success");
                    } else {
                        String info = errorInfo != null ? "" + errorInfo.errorCode : "";
                        showToast("voice change failed. " + info);
                    }
                }));
            }

            public void onStopVoiceTalk() {
                stopVoiceTalk();
            }
        });
        mTalkPopupWindow.openTalkPopupWindow(showAnimation);
    }

    public void closeTalkPopupWindow(boolean stopTalk) {
        if (mTalkPopupWindow != null) {
            mTalkPopupWindow.closeTalkPopupWindow(stopTalk);
        }
    }

    // endregion

    // region --- 播放器手势监听|Player gesture monitoring ---

    private void initTouchListener() {
        // 播放器手势监听器（单击、双击、拖拽、缩放）
        mRealPlayTouchListener = new CustomTouchListener() {
            @Override
            public boolean canZoom(float scale) {
                return mStatus == RealPlayStatus.STATUS_PLAY;
            }

            @Override
            public boolean canDrag(int direction) {
                if (mStatus != RealPlayStatus.STATUS_PLAY) {
                    return false;
                }
                if (mEZPlayer != null && mDeviceInfo != null) {
                    // 出界判断 Out of bounds
                    if (DRAG_LEFT == direction || DRAG_RIGHT == direction) {
                        // 左移/右移出界判断 Left / right out of bounds
                        if (EZBusinessTool.isSupportPTZ(mDeviceInfo, mCameraInfo)) {
                            return true;
                        }
                    } else if (DRAG_UP == direction || DRAG_DOWN == direction) {
                        // 上移/下移出界判断  Move up / down to judge
                        if (EZBusinessTool.isSupportPTZ(mDeviceInfo, mCameraInfo)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onSingleClick() {
                onRealPlaySvClick();
            }

            @Override
            public void onDoubleClick(View v, MotionEvent e) {
                LogUtil.d(TAG, "onDoubleClick:");
                changeZoomStatus(v, e);
            }

            @Override
            public void onZoom(View view, float scale) {
                LogUtil.d(TAG, "onZoom:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null && EZBusinessTool.isSupportZoom(mDeviceInfo, mCameraInfo)) {
                    startZoom(view, scale);
                }
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.d(TAG, "onDrag:" + direction);
                if (mEZPlayer != null) {
                    startDrag(direction, distance, rate);
                }
            }

            @Override
            public void onEnd(View view, int mode) {
                LogUtil.d(TAG, "onEnd:" + mode);
                if (mEZPlayer != null) {
                    stopDrag(false);
                }
                if (mEZPlayer != null && mDeviceInfo != null && EZBusinessTool.isSupportZoom(mDeviceInfo, mCameraInfo)) {
                    stopZoom(view);
                }
            }

            @Override
            public void onZoomChange(View view, float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.d(TAG, "onZoomChange:");
                try {
                    if (scale == 1) {
                        mEZPlayer.setDisplayRegion(false, null, null);
                    } else {
                        mEZPlayer.setDisplayRegion(true, oRect, curRect);
                    }
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }

        };

        mRealPlayTouchListener2 = new CustomTouchListener() {
            @Override
            public boolean canZoom(float scale) {
                return mStatus == RealPlayStatus.STATUS_PLAY;
            }

            @Override
            public boolean canDrag(int direction) {
                if (mStatus != RealPlayStatus.STATUS_PLAY) {
                    return false;
                }
                if (mEZPlayer != null && mDeviceInfo != null) {
                    // 出界判断 Out of bounds
                    if (DRAG_LEFT == direction || DRAG_RIGHT == direction) {
                        // 左移/右移出界判断 Left / right out of bounds
                        if (EZBusinessTool.isSupportPTZ(mDeviceInfo, mCameraInfo)) {
                            return true;
                        }
                    } else if (DRAG_UP == direction || DRAG_DOWN == direction) {
                        // 上移/下移出界判断  Move up / down to judge
                        if (EZBusinessTool.isSupportPTZ(mDeviceInfo, mCameraInfo)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onSingleClick() {
                onRealPlaySvClick();
            }

            @Override
            public void onDoubleClick(View v, MotionEvent e) {
                LogUtil.d(TAG, "onDoubleClick:");
                changeZoomStatus(v, e);
            }

            @Override
            public void onZoom(View view, float scale) {
                LogUtil.d(TAG, "onZoom:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null && EZBusinessTool.isSupportZoom(mDeviceInfo, mCameraInfo)) {
                    startZoom(view, scale);
                }
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.d(TAG, "onDrag:" + direction);
                if (mEZPlayer != null) {
                    startDrag(direction, distance, rate);
                }
            }

            @Override
            public void onEnd(View view, int mode) {
                LogUtil.d(TAG, "onEnd:" + mode);
                if (mEZPlayer != null) {
                    stopDrag(false);
                }
                if (mEZPlayer != null && mDeviceInfo != null && EZBusinessTool.isSupportZoom(mDeviceInfo, mCameraInfo)) {
                    stopZoom(view);
                }
            }

            @Override
            public void onZoomChange(View view, float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.d(TAG, "onZoomChange:");
                int streamId = 1;
                try {
                    if (scale == 1) {
                        mEZPlayer.setDisplayRegion(false, null, null, streamId);
                    } else {
                        mEZPlayer.setDisplayRegion(true, oRect, curRect, streamId);
                    }
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }

        };
    }

    public void startDrag(int direction, float distance, float rate) {
    }

    public void stopDrag(boolean control) {
    }

    private void startZoom(View view, float scale) {
        if (mEZPlayer == null) {
            return;
        }
        hideLandscapeTitleBar(false);
    }

    private void stopZoom(View view) {

    }

    /**
     * 点击播放视图，更新UI
     */
    private void onRealPlaySvClick() {
        if (mEZPlayer != null && mCameraInfo != null && mDeviceInfo != null) {
            if (mDeviceInfo.getStatus() != 1) {
                return;
            }
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRealPlayControlRlVisibility();
            } else {
                setRealPlayFullOperateBarVisibility();
            }
        } else if (mRtspUrl != null) {
            setRealPlayControlRlVisibility();
        }
    }

    /**
     * 竖屏时点击播放视图，更新UI
     */
    private void setRealPlayControlRlVisibility() {
        if (mLandscapeTitleBar.getVisibility() == View.VISIBLE || mRealPlayControlRl.getVisibility() == View.VISIBLE) {
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                }
            } else {
                mLandscapeTitleBar.setVisibility(View.GONE);
            }
            mControlDisplaySec = 0;
        }
    }

    /**
     * 横屏时点击播放视图，更新UI
     */
    private void setRealPlayFullOperateBarVisibility() {
        if (mLandscapeTitleBar.getVisibility() == View.VISIBLE) {// 可见时隐藏相关UI
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {// 不可见时显示导航栏
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            mControlDisplaySec = 0;
        }
    }

    // endregion

    // region -- 电子放大|电子放大 ---

    private boolean hasZoomIn;
    private boolean hasZoomIn2;

    /**
     * 未放大情况下，以双击点位置为坐标原点将画面放大2倍
     * 已放大情况下，取消画面放大效果
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    private void changeZoomStatus(View v, MotionEvent e) {
        int streamId = (v == mRealPlaySv) ? 0 : 1;
        if (streamId == 0) {
            if (hasZoomIn) {
                int invalid = -1;
                mEZPlayer.setDisplayRegion(invalid, invalid, invalid, invalid, streamId);
            } else {
                setDisplayRegion(v, e, streamId);
            }
            hasZoomIn = !hasZoomIn;
        } else if (streamId == 1) {
            if (hasZoomIn2) {
                int invalid = -1;
                mEZPlayer.setDisplayRegion(invalid, invalid, invalid, invalid, streamId);
            } else {
                setDisplayRegion(v, e, streamId);
            }
            hasZoomIn2 = !hasZoomIn2;
        }
    }

    private void setDisplayRegion(View v, MotionEvent e, int streamId) {
        // x轴方向
        double xOffsetRateOfAnchor = (e.getX() / (double) v.getWidth()) - 0.5;
        int left = (int) (mVideoWidth / 4 * 1 + xOffsetRateOfAnchor * mVideoWidth);
        int right = (int) (mVideoWidth / 4 * 3 + +xOffsetRateOfAnchor * mVideoWidth);
        if (left < 0) { // left超出边界，需要修正
            left = 0;
            right = mVideoWidth / 2;
        }
        if (right > mVideoWidth) { // right超出边界，需要修正
            right = mVideoWidth;
            left = mVideoWidth / 2;
        }
        // y轴方向
        double yOffsetRateOfAnchor = (e.getY() / (double) v.getHeight()) - 0.5;
        int top = (int) (mVideoHeight / 4 * 1 + yOffsetRateOfAnchor * mVideoHeight);
        int bottom = (int) (mVideoHeight / 4 * 3 + +yOffsetRateOfAnchor * mVideoHeight);
        if (top < 0) { // top超出边界，需要修正
            top = 0;
            bottom = mVideoHeight / 2;
        }
        if (bottom > mVideoHeight) { // bottom超出边界，需要修正
            bottom = mVideoHeight;
            top = mVideoHeight / 2;
        }
        // 设置坐标
        mEZPlayer.setDisplayRegion(left, top, right, bottom, streamId);
    }

    // endregion

    // region --- 验证码校验|CAPTCHA Verification ---

    @Override
    public void onInputVerifyCode(final String verifyCode) {
        LogUtil.d(TAG, "verify code is " + verifyCode);
        DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), verifyCode);
        if (mEZPlayer != null) {
            mEZPlayer.setPlayVerifyCode(verifyCode);
            playerStartRealPlay();
        }
    }

    // endregion

    // region --- Surface生命周期|Surface lifecycle ---

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mRealPlaySv.getHolder() == holder) {
            if (mEZPlayer != null) {
                mEZPlayer.setSurfaceHold(holder);
            }
            mRealPlaySh = holder;
        } else if (mRealPlaySecondSv.getHolder() == holder) {
            if (mEZPlayer != null) {
                mEZPlayer.setSurfaceHold(holder, 1);
            }
            mRealPlaySecondSh = holder;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mRealPlaySv.getHolder() == holder) {
            if (mEZPlayer != null) {
                mEZPlayer.setSurfaceHold(holder);
            }
            mRealPlaySh = holder;
        } else if (mRealPlaySecondSv.getHolder() == holder) {
            if (mEZPlayer != null) {
                mEZPlayer.setSurfaceHold(holder, 1);
            }
            mRealPlaySecondSh = holder;
        }

        if (mStatus == RealPlayStatus.STATUS_INIT) {
            // 开始播放
            playerStartRealPlay();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mRealPlaySv.getHolder() == holder) {
            if (mEZPlayer != null) {
                mEZPlayer.setSurfaceHold(null);
            }
            mRealPlaySh = null;
        } else if (mRealPlaySecondSv.getHolder() == holder) {
            if (mEZPlayer != null) {
                mEZPlayer.setSurfaceHold(null, 1);
            }
            mRealPlaySecondSh = null;
        }
    }

    // endregion

    // region --- 鱼眼矫正模式相关|Fish eye correction mode related ---

    /** 鱼眼设备专用设置 */
    private void initFecView() {
        if (FecViewLayoutHelper.isFecDevice(mDeviceInfo)) {
            // 鱼眼设备显示"查看模式" & 调整画面比例为1:1
            mFullscreenButton.setVisibility(View.GONE);// 鱼眼设备不支持全屏，隐藏全屏按钮
            ImageButton viewTypeBtn = (ImageButton) findViewById(R.id.realplay_viewtype_btn);
            viewTypeBtn.setVisibility(View.VISIBLE);
            viewTypeBtn.setOnClickListener(v -> openFecViewModePopupWindow(mRealPlayPlayRl));

            fecPlaceType = EZFecPlaceType.EZ_FEC_PLACE_CEILING;// demo中默认顶装
            fecCorrectType = EZFecCorrectType.EZ_FEC_CORRECT_FISH;// demo中默认鱼眼（原始码流）
            mRealRatio = 1;

            realPlayPtzRL = findViewById(R.id.play_ptz_rl);
            mRealPlaySv1 = (SurfaceView) findViewById(R.id.realplay_sv1);
            mRealPlaySv2 = (SurfaceView) findViewById(R.id.realplay_sv2);
            mRealPlaySv3 = (SurfaceView) findViewById(R.id.realplay_sv3);
            mRealPlaySv4 = (SurfaceView) findViewById(R.id.realplay_sv4);
            mRealPlaySv5 = (SurfaceView) findViewById(R.id.realplay_sv5);
            mRealPlaySv6 = (SurfaceView) findViewById(R.id.realplay_sv6);
            ViewGroup playWindowVg = (ViewGroup) findViewById(R.id.vg_play_window);

            // fecViewLayoutHelper.fecPopupWindow 需要延后设置，全局搜索查看
            fecViewLayoutHelper = new FecViewLayoutHelper(this);
            fecViewLayoutHelper.playerView = mRealPlaySv;
            fecViewLayoutHelper.playWindowVg = playWindowVg;
            fecViewLayoutHelper.player = mEZPlayer;
            fecViewLayoutHelper.playPtzRL = realPlayPtzRL;
            SurfaceView[] surfaceViews = new SurfaceView[]{mRealPlaySv1, mRealPlaySv2, mRealPlaySv3, mRealPlaySv4,
                    mRealPlaySv5, mRealPlaySv6};
            fecViewLayoutHelper.setSurfaceViews(surfaceViews);
            playWindowVg.post(() -> fecViewLayoutHelper.setPlayViewAspectRadioWith1V1());
        }
    }

    /**
     * 打开鱼眼矫正模式操作弹出框
     * @param parent
     */
    private void openFecViewModePopupWindow(View parent) {
        closeFecViewModePopupWindow();

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_fec_wnd, null, true);
        layoutView.findViewById(R.id.fec_close_btn).setOnClickListener(v -> closeFecViewModePopupWindow());

        Button placeWallBtn = layoutView.findViewById(R.id.fec_place_wall);
        Button placeFloorBtn = layoutView.findViewById(R.id.fec_place_floor);
        Button placeCeilingBtn = layoutView.findViewById(R.id.fec_place_ceiling);
        Button correctFishBtn = layoutView.findViewById(R.id.fec_correct_fish);
        Button correct4PtzBtn = layoutView.findViewById(R.id.fec_correct_4ptz);
        Button correct5PtzBtn = layoutView.findViewById(R.id.fec_correct_5ptz);
        Button correctFull5PtzBtn = layoutView.findViewById(R.id.fec_correct_full5ptz);
        Button correctLatBtn = layoutView.findViewById(R.id.fec_correct_lat);
        Button correctARCHorBtn = layoutView.findViewById(R.id.fec_correct_arc_hor);
        Button correctARCVerBtn = layoutView.findViewById(R.id.fec_correct_arc_ver);
        Button correctWideAngleBtn = layoutView.findViewById(R.id.fec_correct_wide_angle);
        Button correct180Btn = layoutView.findViewById(R.id.fec_correct_180);
        Button correct360Btn = layoutView.findViewById(R.id.fec_correct_360);
        Button correctCycBtn = layoutView.findViewById(R.id.fec_correct_cyc);
        fecCorrectTypeButtons = new Button[] {
                correct4PtzBtn, correct5PtzBtn, correctFull5PtzBtn,
                correctLatBtn, correctARCHorBtn, correctARCVerBtn,
                correctWideAngleBtn, correct180Btn, correct360Btn,
                correctCycBtn
        };

        placeWallBtn.setOnClickListener(mOnFecWndClickListener);
        placeFloorBtn.setOnClickListener(mOnFecWndClickListener);
        placeCeilingBtn.setOnClickListener(mOnFecWndClickListener);
        correctFishBtn.setOnClickListener(mOnFecWndClickListener);
        correct4PtzBtn.setOnClickListener(mOnFecWndClickListener);
        correct5PtzBtn.setOnClickListener(mOnFecWndClickListener);
        correctFull5PtzBtn.setOnClickListener(mOnFecWndClickListener);
        correctLatBtn.setOnClickListener(mOnFecWndClickListener);
        correctARCHorBtn.setOnClickListener(mOnFecWndClickListener);
        correctARCVerBtn.setOnClickListener(mOnFecWndClickListener);
        correctWideAngleBtn.setOnClickListener(mOnFecWndClickListener);
        correct180Btn.setOnClickListener(mOnFecWndClickListener);
        correct360Btn.setOnClickListener(mOnFecWndClickListener);
        correctCycBtn.setOnClickListener(mOnFecWndClickListener);
        // 设置按钮的可见和可用状态
        int wallTypeValue = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_WALL, mDeviceInfo);
        int floorTypeValue = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_FLOOR, mDeviceInfo);
        int ceilingTypeValue = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_CEILING, mDeviceInfo);
        placeWallBtn.setVisibility(wallTypeValue > 0 ? View.VISIBLE : View.GONE);
        placeFloorBtn.setVisibility(floorTypeValue > 0 ? View.VISIBLE : View.GONE);
        placeCeilingBtn.setVisibility(ceilingTypeValue > 0 ? View.VISIBLE : View.GONE);
        setFecCorrectTypeBtnsEnable(fecPlaceType);

        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
        mFecPopupWindow = new PopupWindow(layoutView, LayoutParams.MATCH_PARENT, height, true);
        mFecPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mFecPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        mFecPopupWindow.setFocusable(false);
        mFecPopupWindow.setOutsideTouchable(false);
        mFecPopupWindow.showAsDropDown(parent);
        mFecPopupWindow.update();

        fecViewLayoutHelper.fecPopupWindow = mFecPopupWindow;
    }

    /**
     * 根据安装模式和能力集设置哪些矫正模式可用
     */
    private void setFecCorrectTypeBtnsEnable(EZFecPlaceType fecPlaceType) {
        int supportValue = FecViewLayoutHelper.getSupportInt(fecPlaceType, mDeviceInfo);
        FecViewLayoutHelper.setFecCorrectButtonsState(fecCorrectTypeButtons, supportValue);
    }

    /**
     * 鱼眼矫正模式的点击事件
     */
    private OnClickListener mOnFecWndClickListener = v -> {
        // 没有在播放，拦截
        if (mStatus != RealPlayStatus.STATUS_PLAY) {
            return;
        }
        int id = v.getId();
        if (id == R.id.fec_place_wall || id == R.id.fec_place_floor || id == R.id.fec_place_ceiling) {// 壁装 || 底装 || 顶装
            fecPlaceType = EZFecPlaceType.values()[Integer.parseInt(String.valueOf(v.getTag()))];
            setFecCorrectTypeBtnsEnable(fecPlaceType);
        } else if (id == R.id.fec_correct_fish || id == R.id.fec_correct_4ptz || id == R.id.fec_correct_5ptz ||
                id == R.id.fec_correct_full5ptz || id == R.id.fec_correct_lat || id == R.id.fec_correct_arc_hor ||
                id == R.id.fec_correct_arc_ver || id == R.id.fec_correct_wide_angle || id == R.id.fec_correct_180 ||
                id == R.id.fec_correct_360 || id == R.id.fec_correct_cyc) {
            // 默认鱼眼|| 4分屏 || 5分屏 || 全景5分屏 || 维度拉伸 || ARC || ARCV || 广角 || 180°全景 || 360°全景 || 柱状
            fecCorrectType = FecViewLayoutHelper.getFecCorrectTypeFromTag(Integer.parseInt(String.valueOf(v.getTag())));
            fecViewLayoutHelper.openFecCorrect(fecCorrectType, fecPlaceType);
        }
    };

    /**
     * 关闭鱼眼查看模式操作弹出框
     */
    private void closeFecViewModePopupWindow() {
        if (mFecPopupWindow != null) {
            dismissPopWindow(mFecPopupWindow);
            mFecPopupWindow = null;
            fecViewLayoutHelper.fecPopupWindow = null;// fecViewLayoutHelper中的窗口对象置空，必须
            setForceOrientation(0);
        }
    }

    // endregion

    // region --- 水印录制弹框|Watermark recording pop-up box ---

    public void openWatermarkPopupWindow() {
        if (waterMarkConfig == null) {
            waterMarkConfig = new EZPlayWaterMarkConfig();
            waterMarkConfig.fontArray = new String[] {"水印信息：", "杭州萤石网络有限公司", "股票代码：688475"};
            waterMarkConfig.fontAlignment = EZPlayWaterMarkConfig.EZWaterMarkTextAlignment.EZWaterMarkTextAlignmentCenter;
            waterMarkConfig.fontRotateAngle = 45f;
            waterMarkConfig.fontColorAdapt = true;
            waterMarkConfig.windowAdaptMode= EZPlayWaterMarkConfig.EZWaterMarkWindowAdaptMode.EZWaterMarkWindowAdaptModeFontRowColumn;
        }
        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
        WatermarkPopWindow watermarkPopWindow = new WatermarkPopWindow(this, mRealPlayPlayRl, height, mRealPlaySv, waterMarkConfig);
        watermarkPopWindow.setWatermarkPopupWindowListener(new WatermarkPopWindow.OnWatermarkPopupWindowListener() {
            @Override
            public void onWaterMarkInfoChanged() {
                isWaterMarkShow = true;
                mEZPlayer.setWaterMarkFont(waterMarkConfig);
            }

            @Override
            public void onClearWaterMarkInfo() {
                isWaterMarkShow = false;
                mEZPlayer.clearWaterMarkFont();
            }
        });
        watermarkPopWindow.openWatermarkPopupWindow();
    }

    // endregion

    // region --- 自定义方法|custom method ---

    private void initEZPlayer() {
        if (mCameraInfo != null) {
            // 1.创建播放器（Android 预览、对讲共用一个EZPlayer）
            mEZPlayer = EzvizApplication.getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
            // 2.配置播放器：设置回调，设置验证码，设置播放视图等配置
            mEZPlayer.setHandler(mHandler);
            // 判断设备是否加密，加密就从demo的归档文件中获取设备验证码填入到播放器的验证码接口里
            if (mDeviceInfo.getIsEncrypt() == 1) {
                mEZPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
            }
            mEZPlayer.setSurfaceHold(mRealPlaySh);// 设置SurfaceHold
            if (isMultiChannelDevice) {// 双目设备设置第二个SurfaceHold
                mEZPlayer.setSurfaceHold(mRealPlaySecondSh, 1);
            }
            if (isQulityModeAuto) {
                mEZPlayer.enableDeviceAutoVideoLevel();
            }
            // 取流前要切换清晰度，会增加出画面的耗时；一般不用设置，如果业务需要选择设置
            mEZPlayer.setVideoQuality(GlobalKit.getInstance().videoQuality);
            // 设置解码方式
            mEZPlayer.setHardDecode(SpTool.obtainBooleanValue(ValueKeys.VideoPlayHardDecodingStatus + "_" + mCameraInfo.getDeviceSerial()));
        } else if (mRtspUrl != null) {
            mEZPlayer = EzvizApplication.getOpenSDK().createPlayerWithUrl(mRtspUrl);
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);
        }
    }

    /**
     * 开始播放
     */
    private void playerStartRealPlay() {
        mIsStartingTalk = false;
        // 增加手机客户端操作信息记录 | Increase the mobile client operation information record
        LogUtil.d(TAG, "startRealPlay");
        if (mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }
        // 检查网络是否可用 | Check if the network is available
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络 | Prompt not to connect to the network
            setRealPlayResult(false);
            mRealPlayTipTv.setText(getString(R.string.realplay_play_fail_becauseof_network));
            return;
        }

        mStatus = RealPlayStatus.STATUS_START;
        setRealPlayLoadingUI();
        updateLoadingProgress(0);

        mRealPlaySv.setVisibility(View.VISIBLE);
        if (!EzvizAPI.getInstance().isUsingGlobalSDK() && EZOpenSDK.isEnableSDKWithTKToken()) {
            mEZPlayer.setStreamToken(GlobalKit.getInstance().previewStreamToken);
        }
        mEZPlayer.startRealPlay();
    }

    /**
     * 停止播放
     */
    private void playerStopRealPlay() {
        LogUtil.d(TAG, "stopRealPlay");
        mStatus = RealPlayStatus.STATUS_STOP;

        stopUpdateTimer();
        if (mEZPlayer != null) {
            stopRealPlayRecord();
            mEZPlayer.stopRealPlay();
        }
        mStreamFlow = 0;
    }

    /**
     * 退出当前页面
     */
    private void exit() {
        closePtzPopupWindow();
        closeTalkPopupWindow(true);
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            playerStopRealPlay();
            setRealPlayResult(false);
        }
        mHandler.removeMessages(MSG_HIDE_PTZ_ANGLE);
        if (mBroadcastReceiver != null) {
            // Cancel the registration of the lock screen broadcast
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        finish();
    }

    private void initData() {
        // 初始化mLocalInfo，记录屏幕尺寸信息
        mLocalInfo = LocalInfo.getInstance();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
        // handler消息处理初始化 & 工具类初始化
        mHandler = new Handler(this);
        mAudioPlayUtil = AudioPlayUtil.getInstance(getApplication());
        mRecordRotateViewUtil = new RotateViewUtil();
        // 注册息屏广播
        mBroadcastReceiver = new RealPlayBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);// 当解除锁屏的时候
        filter.addAction(Intent.ACTION_SCREEN_OFF);// 当按下电源键，屏幕变黑的时候
        registerReceiver(mBroadcastReceiver, filter);
        // 传递参数获取
        Intent intent = getIntent();
        if (intent != null) {
            mCameraInfo = intent.getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
            mDeviceInfo = intent.getParcelableExtra(IntentConsts.EXTRA_DEVICE_INFO);
            boolean isSupportDeviceAutoVideolevel = EZBusinessTool.isSupportDeviceAutoVideolevel(mDeviceInfo, mCameraInfo);
            isQulityModeAuto = isSupportDeviceAutoVideolevel &&
                    SpTool.obtainBooleanValue(ValueKeys.QUALITY_AUTO + "_" + mCameraInfo.getDeviceSerial());
            isMultiChannelDevice = EZBusinessTool.isSupportMultiChannel(mDeviceInfo, mCameraInfo);
            if (isMultiChannelDevice) {// 双目设备调整画面比例
                mRealRatio = Constant.LIVE_MULTI_VIEW_RATIO;
            }
            if (mCameraInfo != null) {
                mCurrentQulityMode = (mCameraInfo.getVideoLevel());
            }
            mRtspUrl = intent.getStringExtra(IntentConsts.EXTRA_RTSP_URL);
            LogUtil.d(TAG, "rtspUrl:" + mRtspUrl);
        }
    }

    /**
     * 播放成功后，启动定时器，一秒刷新一次
     */
    private void startUpdateTimer() {
        stopUpdateTimer();
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 1.计时5秒，5秒后关闭清晰度设置弹框
                if (mLandscapeTitleBar != null && mRealPlayControlRl != null
                        && (mLandscapeTitleBar.getVisibility() == View.VISIBLE || mRealPlayControlRl.getVisibility() == View.VISIBLE)
                        && mControlDisplaySec < 5) {
                    mControlDisplaySec ++;
                }
                // 2.录像录制计时
                if (mEZPlayer != null && isRecording) {
                    mRecordSecond ++;
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_PLAY_UI_UPDATE);
                }
            }
        };
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    /**
     * 停止计时器
     */
    private void stopUpdateTimer() {
        mHandler.removeMessages(MSG_PLAY_UI_UPDATE);
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if (mUpdateTimerTask != null) {
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    /**
     * 锁屏广播，锁屏时，关闭云台和对讲弹出框，并停止预览
     */
    private class RealPlayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                closePtzPopupWindow();
                closeTalkPopupWindow(true);
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    playerStopRealPlay();
                    mStatus = RealPlayStatus.STATUS_PAUSE;
                    setRealPlayResult(false);
                }
            }
        }
    }

    // endregion

    // region --- UI ---

    /**
     * 导航栏初始化
     */
    private void initTitleBar() {
        // 竖屏TitleBar
        mPortraitTitleBar = (TitleBar) findViewById(R.id.title_bar_portrait);
        mPortraitTitleBar.addBackButton(v -> {
            closePtzPopupWindow();
            closeTalkPopupWindow(true);
            if (mStatus != RealPlayStatus.STATUS_STOP) {
                playerStopRealPlay();
                setRealPlayResult(false);
            }
            finish();
        });
        // 横屏TitleBar
        mLandscapeTitleBar = (TitleBar) findViewById(R.id.title_bar_landscape);
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                getResources().getDrawable(R.drawable.message_back_selector));
        mLandscapeTitleBar.setOnTouchListener((v, event) -> false);
        // 横屏返回键添加
        mFullScreenTitleBarBackBtn = new CheckTextButton(this);
        mFullScreenTitleBarBackBtn.setBackground(getResources().getDrawable(ezviz.ezopensdkcommon.R.drawable.common_title_back_selector));
        mLandscapeTitleBar.addLeftView(mFullScreenTitleBarBackBtn);

        String title = mCameraInfo.getCameraName();
        if (isMultiChannelDevice) {
            title = TextUtils.isEmpty(mDeviceInfo.getDeviceName()) ?
                    String.format("%s(%s)", mDeviceInfo.getCategory(), mDeviceInfo.getDeviceSerial()) : mDeviceInfo.getDeviceName();
        }
        mPortraitTitleBar.setTitle(title);
        mLandscapeTitleBar.setTitle(title);
    }

    /**
     * 获取状态栏Rect
     */
    private void initRealPlayPageLy() {
        mRealPlayPageLy = (LinearLayout) findViewById(R.id.realplay_page_ly);
        /** 测量状态栏高度 Measure the status bar height**/
        ViewTreeObserver viewTreeObserver = mRealPlayPageLy.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(() -> {
            if (mRealPlayRect == null) {
                // 获取状况栏高度
                mRealPlayRect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(mRealPlayRect);
            }
        });
    }

    private void initView() {
        initTitleBar();
        initRealPlayPageLy();
        initLoadingUI();
        initTouchListener();
        mFullscreenButton = (CheckTextButton) findViewById(R.id.fullscreen_button);
        mScreenOrientationHelper = new ScreenOrientationHelper(this, mFullscreenButton, mFullScreenTitleBarBackBtn);
        mRealPlayPlayRl = (RelativeLayout) findViewById(R.id.realplay_play_rl);
        mRealPlaySv = (SurfaceView) findViewById(R.id.realplay_sv);
        mRealPlaySh = mRealPlaySv.getHolder();
        mRealPlaySh.addCallback(this);
        // 双目设备
        if (isMultiChannelDevice) {
            mRealPlaySecondSv = (SurfaceView) findViewById(R.id.realplay_second_sv);
            mRealPlaySecondSh = mRealPlaySecondSv.getHolder();
            mRealPlaySecondSh.addCallback(this);
            mRealPlaySecondSv.setVisibility(View.VISIBLE);
            mFullscreenButton.setVisibility(View.INVISIBLE);
        }

        mRealPlaySv.setOnTouchListener(mRealPlayTouchListener);
        if (isMultiChannelDevice) {
            mRealPlaySecondSv.setOnTouchListener(mRealPlayTouchListener2);
        }

        mCoverImage = findViewById(R.id.coverImage);
        if (isMultiChannelDevice) {
            mCoverImage2 = findViewById(R.id.coverImage2);
        }
        mPtzControlAngleViewVer = findViewById(R.id.vertical_angle);
        mPtzControlAngleViewHor = findViewById(R.id.horizontal_angle);
        streamTypeTv = findViewById(R.id.tv_stream_type);
        mRealPlayMoreStreamInfoTv = findViewById(R.id.tv_stream_info_more);
        mRealPlayMoreStreamInfoLL = findViewById(R.id.vg_play_info_more);

        mRealPlayControlRl = (LinearLayout) findViewById(R.id.realplay_control_rl);
        mRealPlayBtn = (ImageButton) findViewById(R.id.realplay_play_btn);
        mRealPlaySoundBtn = (ImageButton) findViewById(R.id.realplay_sound_btn);
        mRealPlayQualityBtn = (Button) findViewById(R.id.realplay_quality_btn);
        mRealPlayFlowTv = (TextView) findViewById(R.id.realplay_flow_tv);
        mRealPlayFlowTv.setText("0k/s");
        mRealPlayRecordLy = (LinearLayout) findViewById(R.id.realplay_record_ly);
        mRealPlayRecordIv = (ImageView) findViewById(R.id.realplay_record_iv);
        mRealPlayRecordTv = (TextView) findViewById(R.id.realplay_record_tv);

        if (mRtspUrl == null) {
            initOperateBarUI();
            mRealPlayOperateBar.setVisibility(View.VISIBLE);
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
            mRealPlayPlayRl.setBackgroundColor(getResources().getColor(R.color.common_bg));
        }

        setRealPlaySvLayout();

        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDialog.setCancelable(false);
    }

    /**
     * 初始化操控视图（云台、对讲、截图、录制）
     */
    private void initOperateBarUI() {
        mRealPlayOperateBar = (HorizontalScrollView) findViewById(R.id.ezopen_realplay_operate_bar);
        if (EzvizAPI.getInstance().isUsingGlobalSDK()) {// 海外不支持云录制截图上传
            findViewById(R.id.realplay_snapshot_upload_btn_ly).setVisibility(View.GONE);
        }
        mRealPlayPtzBtn = (ImageButton) findViewById(R.id.realplay_ptz_btn);
        mRealPlayTalkBtn = (ImageButton) findViewById(R.id.realplay_talk_btn);
        mRealPlayWatermarkBtn = (ImageButton) findViewById(R.id.realplay_watermark_btn);
        mRealPlayCaptureBtn = (ImageButton) findViewById(R.id.realplay_previously_btn);
        mRealPlaySnapshotUplaodBtn = (ImageButton) findViewById(R.id.realplay_snapshot_upload_btn);
        mRealPlayRecordContainer = findViewById(R.id.realplay_video_container);
        mRealPlayRecordBtn = (ImageButton) findViewById(R.id.realplay_video_btn);
        mRealPlayRecordStartBtn = (ImageButton) findViewById(R.id.realplay_video_start_btn);
        mRealPlayTalkBtn.setEnabled(false);
        mRealPlayOperateBar.setVisibility(View.VISIBLE);
    }

    /**
     * 竖屏 & 横屏切换
     */
    private void onOrientationChanged() {
        setRealPlaySvLayout();

        updateOperatorUI();
        updateTalkUI();
        updatePtzUI();
    }

    /**
     * 更新操控栏UI
     */
    private void updateOperatorUI() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            fullScreen(false);
            updateOrientation();
            mPortraitTitleBar.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.GONE);
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mRtspUrl == null) {
                mRealPlayPageLy.setBackgroundColor(getResources().getColor(R.color.common_bg));
                mRealPlayOperateBar.setVisibility(View.VISIBLE);
                if (isRecording) {
                    mRealPlayRecordBtn.setVisibility(View.GONE);
                    mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
                } else {
                    mRealPlayRecordBtn.setVisibility(View.VISIBLE);
                    mRealPlayRecordStartBtn.setVisibility(View.GONE);
                }
            }
        } else {
            fullScreen(true);
            mPortraitTitleBar.setVisibility(View.GONE);
            // hide the
            mRealPlayControlRl.setVisibility(View.GONE);
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            if (mRtspUrl == null) {
                mRealPlayOperateBar.setVisibility(View.GONE);
                mRealPlayPageLy.setBackgroundColor(getResources().getColor(R.color.black_bg));
            }
        }

        if (mStatus == RealPlayStatus.STATUS_START) {
            showControlRlAndFullOperateBar();
        }
    }

    /**
     * 更新云台UI
     */
    private void updatePtzUI() {
        if (!mIsOnPtz) {
            return;
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mHandler.post(() -> openPtzPopupWindow());
        } else {
            closePtzPopupWindow();
            mIsOnPtz = true;
        }
    }

    /**
     * 更新对讲UI
     */
    private void updateTalkUI() {
        if (!mIsOnTalk) {
            return;
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mHandler.post(() -> openTalkPopupWindow(false));
        } else {
            closeTalkPopupWindow(false);
        }
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 声音开关 - Player声音设置
     */
    private void setRealPlaySound() {
        if (mEZPlayer == null) {
            return;
        }
        if (mRtspUrl == null) {
            if (mLocalInfo.isSoundOpen()) {
                mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_selector);
                mEZPlayer.openSound();
            } else {
                mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_off_selector);
                mEZPlayer.closeSound();
            }
        }
    }

    /**
     * 重置相关UI
     */
    private void setRealPlayLoadingUI() {
        mStartTime = System.currentTimeMillis();
        setStartloading();
        mRealPlayBtn.setBackgroundResource(R.drawable.play_stop_selector);

        if (mCameraInfo != null && mDeviceInfo != null) {
            mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1);
            mRealPlayPtzBtn.setEnabled(false);
            mRealPlayWatermarkBtn.setEnabled(false);
            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlaySnapshotUplaodBtn.setEnabled(false);
            mRealPlayRecordBtn.setEnabled(false);
        }

        showControlRlAndFullOperateBar();
    }

    private void showControlRlAndFullOperateBar() {
        if (mRtspUrl != null || mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                }
            } else {
                mLandscapeTitleBar.setVisibility(View.GONE);
            }
        } else {
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
        }
        mControlDisplaySec = 0;
    }

    /**
     * 播放器播放 成功 / 失败
     * @param isSuccess 播放器播放是否成功
     */
    private void setRealPlayResult(boolean isSuccess) {
        if (isSuccess && mStartTime > 0) {
            long mStopTime = System.currentTimeMillis();
            showToast("cost: " + (mStopTime - mStartTime) + " ms");
            mStartTime = 0;
        }
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPtzBtn.setEnabled(isSuccess);
        mRealPlayWatermarkBtn.setEnabled(isSuccess && !isMultiChannelDevice);
        // 未在预览时也能发起对讲
        mRealPlayTalkBtn.setEnabled(
                EZBusinessTool.isSupportTalk(mDeviceInfo, mCameraInfo) != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport);
        mRealPlayCaptureBtn.setEnabled(isSuccess);
        mRealPlaySnapshotUplaodBtn.setEnabled(isSuccess);
        mRealPlayRecordBtn.setEnabled(isSuccess);
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如下代码
        if (fecViewLayoutHelper != null) {
            fecViewLayoutHelper.resetFecType();
            closeFecViewModePopupWindow();
        }
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如上代码
        mCoverImage.setVisibility(isSuccess ? View.GONE : View.VISIBLE);
        if (isMultiChannelDevice) {
            mCoverImage2.setVisibility(isSuccess ? View.GONE : View.VISIBLE);
        }
        // 设置水印
        if (isSuccess && isWaterMarkShow && waterMarkConfig != null) {
            mEZPlayer.setWaterMarkFont(waterMarkConfig);
        }
        // 播放器左下角播放按钮设置
        mRealPlayBtn.setBackgroundResource(isSuccess ? R.drawable.play_stop_selector : R.drawable.play_play_selector);
        if (!isSuccess) {
            playerStopRealPlay();
        }

        hideLandscapeTitleBar(true);
    }

    /**
     * 更新显示预览每秒接收到的码流大小
     */
    private void updateStreamFlow() {
        // 更新流量数据 | Update traffic data
        long streamFlow = mEZPlayer.getStreamFlow();
        long streamFlowUnit = streamFlow - mStreamFlow;
        if (streamFlowUnit < 0)
            streamFlowUnit = 0;
        float fKBUnit = (float) streamFlowUnit / (float) Constant.KB;
        String descUnit = String.format("%.2f k/s ", fKBUnit);
        mRealPlayFlowTv.setText(descUnit);
        mStreamFlow = streamFlow;
    }

    private void updateOrientation() {
        if (mIsOnTalk) {
            if (mEZPlayer != null && mDeviceInfo != null &&
                    EZBusinessTool.isSupportTalk(mDeviceInfo, mCameraInfo) != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                setForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (mStatus == RealPlayStatus.STATUS_PLAY) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }
    }

    private void setOrientation(int sensor) {
        if (mForceOrientation != 0) {
            LogUtil.d(TAG, "setOrientation mForceOrientation:" + mForceOrientation);
            return;
        }

        if (sensor == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            mScreenOrientationHelper.enableSensorOrientation();
        else
            mScreenOrientationHelper.disableSensorOrientation();
    }

    public void setForceOrientation(int orientation) {
        if (mForceOrientation == orientation) {
            LogUtil.d(TAG, "setForceOrientation no change");
            return;
        }
        mForceOrientation = orientation;
        if (mForceOrientation != 0) {
            if (mForceOrientation != mOrientation) {
                if (mForceOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mScreenOrientationHelper.portrait();
                } else {
                    mScreenOrientationHelper.landscape();
                }
            }
            mScreenOrientationHelper.disableSensorOrientation();
        } else {
            updateOrientation();
        }
    }

    /**
     * 设置取流方式
     */
    private void showStreamFetchType() {
        int type = mEZPlayer.getStreamFetchType();
        LogUtil.i(TAG, "getStreamFetchType: " + type);
        String streamTypeMsg = getApplicationContext().getString(R.string.stream_type) + EZBusinessTool.getStreamType(type);
        streamTypeTv.setText(streamTypeMsg);
        streamTypeTv.setVisibility(View.VISIBLE);
    }

    /**
     * 显示更多码流信息
     */
    private void showStreamInfoMore() {
        mRealPlayMoreStreamInfoTv.setVisibility(View.GONE);
        mRealPlayMoreStreamInfoLL.setVisibility(View.VISIBLE);
        if (mDeviceDetailPublicInfo == null) {
            return;
        }
        String ratioMsg = "resolution ratio: " + mDeviceDetailPublicInfo.videoWidth + "x" + mDeviceDetailPublicInfo.videoHeight;
        ((TextView)findViewById(R.id.tv_resolution_ratio)).setText(ratioMsg);// 分辨率
        String encodeTypeMsg = "videoEncode type: " + EZBusinessTool.getVideoEncodeType(mDeviceDetailPublicInfo.videoEncodeType);
        ((TextView)findViewById(R.id.tv_encode_type)).setText(encodeTypeMsg);// 视频编码格式
        String decodeTypeMsg = "decode type: " + EZBusinessTool.getDecodeType(mEZPlayer.getDecodeType());
        ((TextView) findViewById(R.id.tv_decode_type)).setText(decodeTypeMsg);// 解码方式
    }

    /**
     * 获取设备详情信息 & 更新UI
     */
    private void getDeviceDetailInfoAndRefreshUI() {
        mDeviceDetailPublicInfo = this.mEZPlayer.getDeviceDetailInfo();
        if (mDeviceDetailPublicInfo != null) {
            mCameraInfo.setVideoLevel(mDeviceDetailPublicInfo.videoLevel);
        }
        mCurrentQulityMode = mCameraInfo.getVideoLevel();
        setVideoLevelUI();
        if (mRealPlayMoreStreamInfoLL.getVisibility() == View.VISIBLE) {
            showStreamInfoMore();
        }
    }

    /**
     * 录像录制启动成功后UI刷新
     */
    private void handleRecordSuccess() {
        if (mCameraInfo == null) {
            return;
        }
        if (!mIsOnStop) {
            mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, mRealPlayRecordBtn,
                    mRealPlayRecordStartBtn, 0, 90);
        } else {
            mRealPlayRecordBtn.setVisibility(View.GONE);
            mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
        }
        isRecording = true;
        mRealPlayRecordLy.setVisibility(View.VISIBLE);
        mRealPlayRecordTv.setText("00:00");
        mRecordSecond = 0;
    }

    /**
     * 录像录制启动失败后UI刷新
     */
    private void handleRecordFail() {
        Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_record_fail);
        if (isRecording) {
            stopRealPlayRecord();
        }
    }

    /**
     * 隐藏横屏时的TitleBar
     * @param excludeLandscapeTitle
     */
    private void hideLandscapeTitleBar(boolean excludeLandscapeTitle) {
        if (excludeLandscapeTitle && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
        } else {
            mLandscapeTitleBar.setVisibility(View.GONE);
        }
    }

    /**
     * 更新预览UI（清晰度设置弹框 & 流量使用 & 录制时长）
     */
    private void updateRealPlayUI() {
        if (mControlDisplaySec == 5) {
            mControlDisplaySec = 0;
            hideLandscapeTitleBar(false);
        }
        updateStreamFlow();
        if (isRecording) {
            updateRecordTime();
        }
        showStreamFetchType();
    }

    /**
     * 录像过程中计时UI更新
     */
    private void updateRecordTime() {
        if (mRealPlayRecordIv.getVisibility() == View.VISIBLE) {
            mRealPlayRecordIv.setVisibility(View.INVISIBLE);
        } else {
            mRealPlayRecordIv.setVisibility(View.VISIBLE);
        }
        mRealPlayRecordTv.setText(EZDateTimeUtil.convToMinSec(mRecordSecond));
    }

    /**
     * 设置播放器视图布局
     */
    private void setRealPlaySvLayout() {
        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo
                .getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);

        LayoutParams svLp = new LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ViewGroup playWindowVg = (ViewGroup) findViewById(R.id.vg_play_window);
        playWindowVg.setLayoutParams(svLp);

        if (mRtspUrl == null) {
            // do nothing
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        }
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        if (isMultiChannelDevice) {
            mRealPlayTouchListener2.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        }
    }

    private void initLoadingUI() {
        mRealPlayLoadingRl = (RelativeLayout) findViewById(R.id.realplay_loading_rl);
        mRealPlayTipTv = (TextView) findViewById(R.id.realplay_tip_tv);
        mRealPlayPlayIv = (ImageView) findViewById(R.id.realplay_play_iv);
        mRealPlayPlayLoading = (LoadingTextView) findViewById(R.id.realplay_loading);
        mRealPlayPlayIv.setOnClickListener(this);
    }

    /**
     * 更新加载进程
     * @param progress
     */
    private void updateLoadingProgress(final int progress) {
        mRealPlayPlayLoading.setTag(Integer.valueOf(progress));
        mRealPlayPlayLoading.setText(progress + "%");
        mHandler.postDelayed(() -> {
            if (mRealPlayPlayLoading != null) {
                Integer tag = (Integer) mRealPlayPlayLoading.getTag();
                if (tag != null && tag.intValue() == progress) {
                    Random r = new Random();
                    mRealPlayPlayLoading.setText((progress + r.nextInt(20)) + "%");
                }
            }
        }, 500);
    }

    private void setStartloading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.VISIBLE);
        mRealPlayPlayIv.setVisibility(View.GONE);
    }

    public void setStopLoading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.VISIBLE);
    }

    public void setLoadingFail(String errorStr) {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setText(errorStr);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
    }

    private void setLoadingSuccess() {
        mRealPlayLoadingRl.setVisibility(View.INVISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
    }

    // endregion

    // region --- 动态权限申请|Dynamic permission application ---

    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermission();
        } else {
            afterHasPermission();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<>();
        if (!(checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }
        if (!(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.RECORD_AUDIO);
        }
        // 权限都已经有了
        if (lackedPermission.size() == 0) {
            afterHasPermission();
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && hasAllPermissionsGranted(grantResults)) {
            afterHasPermission();
        } else {
            try {
                showPermissionDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 权限设置
     */
    private void showPermissionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。")
                .setPositiveButton("去设置", (dialog1, which) -> {
                    isFromPermissionSetting = true;
                    dialog1.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog12, which) -> {
                    dialog12.dismiss();
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.black));
        // 设置居中，解决Android9.0 AlertDialog不居中问题
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.width = (int) (LocalInfo.getInstance().getScreenWidth() * 0.9);
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);
    }

    private void afterHasPermission() {
        // 有麦克风权限后，弹出对讲方式选择框
        selectTalkbackItems();
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    // endregion
}
