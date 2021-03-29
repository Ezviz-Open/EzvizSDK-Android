package com.ezviz.demo.videotalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ezviz.sdk.videotalk.EvcLocalWindowView;
import com.ezviz.sdk.videotalk.EvcMsgCallback;
import com.ezviz.sdk.videotalk.EvcParam;
import com.ezviz.sdk.videotalk.EvcParamValueEnum;
import com.ezviz.sdk.videotalk.EzvizVideoCall;
import com.ezviz.sdk.videotalk.EzvizVoiceCall;
import com.ezviz.videotalk.debug.DebugUtils;
import com.videogo.exception.BaseSdkRuntimeException;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.BuildConfig;
import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.LogUtil;

public class SafetyHatTalkActivity extends Activity {

    private final static String TAG = "@@SafetyHatTalkActivity";
    private final static boolean IS_DEBUGGING = true;
    private final static int ERROR = -1;
    private final static int REQUEST_NEEDED_PERMISSIONS = 10000;
    private final static int EXTERNAL_WRITE_PERMISSION = 100001;

    private int mRole = ERROR;
    private int mRoomID = ERROR;
    private boolean isInit = false;
    private boolean isOnlyVoice = false;

    private TalkStateEnum mCurrentTalkState;
    private Toast mCurrentToast;
    private EzvizVideoCall mEzvizVideoCall;
    private EzvizVoiceCall mEzvizVoiceCall;
    private View mPlayerView;
    private OperationManager mOperationManager;
    private ViewGroup mOperationContainer;
    private TextView mNotificationTV, mHintTV;
    private MediaPlayer mCallerRingPlayer, mAnswerRingPlayer;
    private QuitSafelyThread mTextThread;
    private Intent mInIntent;

    // 申请存储权限，用于录制
    private final Object mRecordLock = new Object();

    // 发起或者接听视频通话的参数
    private String mServer;
    private int mServerPort;
    private String mWatchSerial;
    private String mSelfId;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_NEEDED_PERMISSIONS == requestCode && grantResults.length > 0) {
            boolean isGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            if (isGranted) {
                onPermissionsGranted();
            } else {
                showInitFailedDialog("您拒绝授予视频通话功能所需的权限，该功能无法使用");
            }
        } else if (EXTERNAL_WRITE_PERMISSION == requestCode && grantResults.length > 0) {
            boolean isGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i])) {
                    isGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
            }
            if (isGranted) {
                synchronized (mRecordLock) {
                    mRecordLock.notifyAll();
                }
            }
        }

    }

    @SuppressWarnings("SameParameterValue")
    private void showInitFailedDialog(String errorMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("功能初始化失败");
        TextView contentTv = new TextView(this);
        contentTv.setPadding(200, 100, 200, 100);
        contentTv.setGravity(Gravity.CENTER);
        contentTv.setText(errorMsg);
        builder.setView(contentTv);
        builder.setPositiveButton("点击关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    // 权限相关
    @SuppressWarnings("SimplifiableIfStatement")
    private boolean checkThisPermission(String permissionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            finish();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast3s("手机版本太低，不支持视频通话功能");
                }
            });
        }
        isOnlyVoice = getIntent().getBooleanExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_USE_AUDIO, true);
        // 全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_safety_hat_talk);

        initData();

        mOperationManager = new OperationManager();

        mInIntent = getIntent();

        ObtainViews();
        if (isOnlyVoice) {
            mEzvizVoiceCall = new EzvizVoiceCall(mEvcMsgCallback);
        } else {
            mEzvizVideoCall = new EzvizVideoCall(new EvcLocalWindowView(this), mEvcMsgCallback);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canAccessCamera = checkThisPermission(Manifest.permission.CAMERA);
            boolean canAccessRecord = checkThisPermission(Manifest.permission.RECORD_AUDIO);
            if (canAccessCamera && canAccessRecord) {
                onPermissionsGranted();
            } else {
                ArrayList<String> permissionsList = new ArrayList<>();
                if (!canAccessCamera) {
                    permissionsList.add(Manifest.permission.CAMERA);
                }
                if (!canAccessRecord) {
                    permissionsList.add(Manifest.permission.RECORD_AUDIO);
                }
                String[] permissions = new String[permissionsList.size()];
                for (int i = 0; i < permissionsList.size(); i++) {
                    permissions[i] = permissionsList.get(i);
                }
                requestPermissions(permissions, REQUEST_NEEDED_PERMISSIONS);
            }
        } else {
            onPermissionsGranted();
        }

        if (BuildConfig.DEBUG) {
            DebugUtils.startSaveLogToFile(getApplicationContext());
        }
    }

    private void initData() {
        mServer = getIntent().getStringExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_SERVER);
        mServerPort = getIntent().getIntExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_SERVER_PORT, -1);
        mWatchSerial = getIntent().getStringExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_DEVICE_SERIAL);
        mSelfId = getIntent().getStringExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_SELF_ID);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        if ((KeyEvent.KEYCODE_VOLUME_UP == keyCode ||KeyEvent.KEYCODE_VOLUME_DOWN == keyCode)
//                && (TalkStateEnum.CALLER_CALLING == mCurrentTalkState || TalkStateEnum.ANSWER_BEING_CALLED == mCurrentTalkState)){
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    showToast("通话开始后才可以调整音量");
//                }
//            });
//            return true;
//        }

        return super.onKeyDown(keyCode, event);
    }

    private void onPermissionsGranted() {

        if (isInit) {
            return;
        } else {
            isInit = true;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                boolean isInitSuccess = initByRole();
                if (!isInitSuccess) {
                    LogUtil.e(TAG, "onCreate: failed to init UI!!!");
                } else {
                    // 完成不依赖role的通用初始化操作
                    ((TextureView) mPlayerView).setSurfaceTextureListener(mTextureViewListener);
                }
            }
        });
    }

    private void ObtainViews() {
        mPlayerView = findViewById(R.id.view_child_watch_video_talk_player);
        mOperationContainer = findViewById(R.id.vg_child_watch_video_talk_operation_container);
        mNotificationTV = findViewById(R.id.tv_child_watch_video_talk_notification);
    }

    public int dip2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private boolean initByRole() {
        Point screenPoint = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(screenPoint);
        ViewGroup videoContainer = findViewById(R.id.vg_child_watch_video_talk_video_container);
        ViewGroup.LayoutParams param = videoContainer.getLayoutParams();
        param.width = screenPoint.x;
        //noinspection SuspiciousNameCombination
        param.height = screenPoint.x;
        videoContainer.setLayoutParams(param);

        mInIntent = getIntent();
        mRole = mInIntent.getIntExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_ROLE, ERROR);
        if (ERROR == mRole) {
            LogUtil.e(TAG, "error init params：mRole");
            return false;
        }

//        mEzvizVideoCall.setLogCallBack(new EZLogCallback() {
//            @Override
//            public void onRcvLog(int logType, String log) {
//                String logMore = "log type " + logType + ", " + "log is: " + log;
//                Log.i(TAG, logMore);
//            }
//        });

        if (mRole == WatchVideoTalkActivity.InIntentKeysAndValues.VALUE_CALLER) {
            createCall();
        }
        if (mRole == WatchVideoTalkActivity.InIntentKeysAndValues.VALUE_ANSWER) {
            mRoomID = mInIntent.getIntExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_ROOM_ID, ERROR);
            if (ERROR == mRoomID) {
                LogUtil.e(TAG, "error init params：mRoomId");
                return false;
            }
            answerCall();
        }
        return true;
    }

    private synchronized void createCall() {
        mCurrentTalkState = TalkStateEnum.CALLER_CALLING;
        changeUiByTalkState();
        if (isOnlyVoice) {
            startVoiceTalk();
        } else {
            startVideoTalk(EvcParamValueEnum.EvcOperationEnum.getOperationByCode(WatchVideoTalkActivity.InIntentKeysAndValues.VALUE_CALLER), 0);
        }

        mTextThread = new BreathTextThread(mNotificationTV);
        mTextThread.start();

        // 搭配MediaPlayer的setAudioStreamType(AudioManager.STREAM_VOICE_CALL)使用，实现通过通话的形式播放声音
        AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
/*
            manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
*/
            manager.setSpeakerphoneOn(true);
        }

        // 播放呼叫提示声
        mCallerRingPlayer = new MediaPlayer();
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.video_talk_caller_ring);
        try {
            mCallerRingPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mCallerRingPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mCallerRingPlayer.setLooping(true);
            mCallerRingPlayer.prepare();
            mCallerRingPlayer.start();
        } catch (IOException e) {
            mCallerRingPlayer = null;
            e.printStackTrace();
        }


    }

    private synchronized void answerCall() {
        mCurrentTalkState = TalkStateEnum.ANSWER_BEING_CALLED;
        changeUiByTalkState();

        // 搭配MediaPlayer的setAudioStreamType(AudioManager.STREAM_VOICE_CALL)使用，实现通过通话的形式播放声音
        AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (manager != null) {
            manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
/*
            manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
*/
            manager.setSpeakerphoneOn(true);
        }

        // 播放被呼叫提示音
        mAnswerRingPlayer = new MediaPlayer();
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.video_talk_answer_ring);
        try {
            mAnswerRingPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mAnswerRingPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mAnswerRingPlayer.setLooping(true);
            mAnswerRingPlayer.prepare();
            mAnswerRingPlayer.start();
        } catch (IOException e) {
            mAnswerRingPlayer = null;
            e.printStackTrace();
        }

    }

    private void startVideoTalk(EvcParamValueEnum.EvcOperationEnum operation, int roomId) {
        final EvcParam param = new EvcParam();
        // 安全帽音视频对讲时，本地视频采集关闭
        param.enableVideo = false;
        param.enableAudio = true;
        param.operation = operation;
        param.roomId = roomId;
        param.serverIp = mServer;
        param.serverPort = mServerPort;
        param.streamType = EvcParamValueEnum.EvcStreamType.VIDEO_TALK;
        param.selfClientType = EvcParamValueEnum.EvcClientType.ANDROID_PHONE;
        param.otherClientType = EvcParamValueEnum.EvcClientType.CHILD_WATCH;

        // 呼叫手表需要填写
        if (EvcParamValueEnum.EvcOperationEnum.CALL == operation) {
            param.otherId = mWatchSerial;
            param.selfId = mSelfId;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                mEzvizVideoCall.startVideoTalk(param);
            }
        }).start();
    }

    private void startVoiceTalk() {
//        EZPlayer mEZPlayer = EZOpenSDK.getInstance().createPlayer(mWatchSerial, 1);
//        mEZPlayer.openSound();
//        mEZPlayer.setSpeakerphoneOn(true);
////        mEZPlayer.setHandler(new VoiceHandler());
//        mEZPlayer.startVoiceTalk();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mEzvizVoiceCall.startVideoTalk(SafetyHatTalkActivity.this, mWatchSerial);
                } catch (final BaseSdkRuntimeException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(e.errMsg);
                            finish();
                        }
                    });
                }
            }
        }).start();
    }


    private void refuseTalk() {
        startVideoTalk(EvcParamValueEnum.EvcOperationEnum.REFUSE, mRoomID);
    }

    @SuppressLint("SetTextI18n")
    private void changeUiByTalkState() {
        if (TalkStateEnum.CALLER_CALLING == mCurrentTalkState) {
            ImageView imgOfRemote = findViewById(R.id.iv_child_watch_video_talk_player_head_image);
            Glide.with(SafetyHatTalkActivity.this)
                    .load(mInIntent.getStringExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_HEAD_PORTRAIT_LOCAL))
                    .placeholder(R.drawable.video_talk_sdk_placeholder_images_user)
                    .into(imgOfRemote);
            mNotificationTV.setText("等待" + mWatchSerial + "接受...");
            mOperationContainer.removeAllViews();
            mOperationContainer.addView(mOperationManager.getHangUpVG().view);
            mOperationContainer.addView(mOperationManager.getToggleAudioVG().view);
        }

        if (TalkStateEnum.ANSWER_BEING_CALLED == mCurrentTalkState) {
            // 对方头像：圆角处理
            ImageView imgOfRemote = findViewById(R.id.iv_child_watch_video_talk_player_head_image);
            Glide.with(SafetyHatTalkActivity.this)
                    .load(mInIntent.getStringExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_HEAD_PORTRAIT_REMOTE))
                    .placeholder(R.drawable.video_talk_sdk_placeholder_images_user)
                    .into(imgOfRemote);

//            // 自己头像：高斯模糊处理(高斯模糊程度不够，自己再缩放)
//            GlideApp.with(this)
//                    .load(mInIntent.getStringExtra(WatchVideoTalkActivity.InIntentKeysAndValues.KEY_HEAD_PORTRAIT_LOCAL))
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .transform(new BlurTransformation(this, 25))
//                    .into(imgOfLocal);
//            coverVG.setVisibility(View.VISIBLE);
            mNotificationTV.setText(mWatchSerial + "来电...");

            mOperationContainer.removeAllViews();
            mOperationContainer.addView(mOperationManager.getReceiveVG().view);
            View nullView = new View(this);
            nullView.setLayoutParams(new ViewGroup.LayoutParams(dip2px(50.0f), 0));
            mOperationContainer.addView(nullView);
            mOperationContainer.addView(mOperationManager.getRefuseVG().view);
        }

        if ((TalkStateEnum.ANSWER_TALKING == mCurrentTalkState)
                || (TalkStateEnum.CALLER_TALKING == mCurrentTalkState)) {
            mNotificationTV.setText("00 : 00");

            mOperationContainer.removeAllViews();
            mOperationContainer.addView(mOperationManager.getHangUpVG().view);
            mOperationContainer.addView(mOperationManager.getToggleAudioVG().view);
        }
    }

    private void debug(String info) {
        String debugInfo = "debug msg: " + info;
        showToast(debugInfo);
        LogUtil.e(TAG, debugInfo);
    }

    @SuppressWarnings("SameParameterValue")
    private void showToast(final String toastMessage) {
        LogUtil.e(TAG, toastMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentToast != null) {
                    mCurrentToast.cancel();
                    mCurrentToast = null;
                }
                mCurrentToast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
                mCurrentToast.show();
            }
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void toast3s(final String toastMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentToast != null) {
                    mCurrentToast.cancel();
                    mCurrentToast = null;
                }
                final Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
                toast.show();
                mCurrentToast = toast;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mCurrentToast == toast) {
                            mCurrentToast.cancel();
                        }
                    }
                }, 3000);
            }
        });

    }

    private EvcMsgCallback mEvcMsgCallback = new EvcMsgCallback() {

        @Override
        public void onMessage(int code, String desc) {
            //50021
            showToast("onMessage" + code + "\n" + desc);
            if (isFinishing()) {
                return;
            }
            if (code == 80050017) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(SafetyHatTalkActivity.this).setMessage(R.string.video_talk_call_is_accepted).setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).show();
                    }
                });
            } else if (code == 80050103 || code == 80050106) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(SafetyHatTalkActivity.this).setMessage(R.string.video_talk_watch_is_busy).setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).show();
                    }
                });
            } else if (code == 80050104) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(SafetyHatTalkActivity.this).setMessage(R.string.video_talk_watch_temperature_high_reject).setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).show();
                    }
                });
            } else if (code == 80050203) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(SafetyHatTalkActivity.this).setMessage(R.string.video_talk_watch_temperature_high_hang).setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).show();
                    }
                });
            } else if (code == 80050105) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(SafetyHatTalkActivity.this).setMessage(R.string.video_talk_is_playing).setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).show();
                    }
                });
            } else if (code >= 80050006 && code <= 80050205) {
                finish();
            }
        }

        @Override
        public void onRcvLucidMsg(String msg) {
            final String finalMsg = msg;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject json = new JSONObject(finalMsg);
                        int type = json.optInt("type", -1);
                        if (type == 0) {
                            mHintTV.setText(R.string.video_talk_watch_temperature_high_warn);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onJoinRoom(int roomId, int clientId, String username) {

        }

        @Override
        public void onQuitRoom(int roomId, int clientId) {
            debug("create room, roomId " + roomId + " clientId: " + clientId);
        }

        @Override
        public void onRoomCreated(final int roomId) {
            mRoomID = roomId;
            if (IS_DEBUGGING) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        debug("create room, roomId " + roomId);
                        TextView tv = findViewById(R.id.debug_room_id);
                        tv.setText("RoomID: " + roomId);
                    }
                });
            }
        }

        @Override
        public void onCallEstablished(int width, int height, final int clientId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mEzvizVideoCall!= null) {
                        mEzvizVideoCall.setDisplay(new Surface(((TextureView) mPlayerView).getSurfaceTexture()), clientId);
                    }
                    if (WatchVideoTalkActivity.InIntentKeysAndValues.VALUE_CALLER == mRole) {
                        mCurrentTalkState = TalkStateEnum.CALLER_TALKING;
                        mCallerRingPlayer.stop();
                        mCallerRingPlayer.release();
                    }
                    if (WatchVideoTalkActivity.InIntentKeysAndValues.VALUE_ANSWER == mRole) {
                        mCurrentTalkState = TalkStateEnum.ANSWER_TALKING;
                        mAnswerRingPlayer.stop();
                        mAnswerRingPlayer.release();
                    }

                    changeUiByTalkState();
                    ImageView remoteImgView = findViewById(R.id.iv_child_watch_video_talk_player_head_image);
                    remoteImgView.setVisibility(View.GONE);

                    if (mTextThread != null) {
                        mTextThread.quit();
                    }
                    mTextThread = new TalkTimerThread(mNotificationTV);
                    mTextThread.start();
                }
            });
        }

        @Override
        public void onOtherRefused() {
            showToast("对方已拒绝");
            finish();
        }

        @Override
        public void onOtherNoneAnswered() {
            if (mCurrentTalkState == TalkStateEnum.CALLER_CALLING && !isFinishing()) {
                toast3s(getString(R.string.video_talk_sdk_toast_nobody));
                if (mCallerRingPlayer != null) {
                    mCallerRingPlayer.stop();
                }
                finish();
            } else if (TalkStateEnum.ANSWER_TALKING == mCurrentTalkState) {

                toast3s(getString(R.string.video_talk_sdk_toast_hang_up));
                refuseTalk();
                finish();
            }
        }

        @Override
        public void onOtherHangedUp() {
            toast3s(getString(R.string.video_talk_sdk_toast_hang_up));
            finish();
        }

        @Override
        public void onBadNet(int delayTimeMs) {
            LogUtil.d("SafeHatTalkActivity", "onBadNet: "+ delayTimeMs);
            showToast(getString(R.string.video_talk_signal_weak));
        }

    };

    private TextureView.SurfaceTextureListener mTextureViewListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mEzvizVideoCall != null) {
                mEzvizVideoCall.setDisplay(new Surface(surface));
            }
        }

        SurfaceTexture mLastSurface = null;

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (mEzvizVideoCall != null) {
                if (mLastSurface == null) {
                    mEzvizVideoCall.setDisplay(new Surface(surface));
                    mLastSurface = surface;
                } else {
                    mEzvizVideoCall.refreshWindow();
                }
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mEzvizVideoCall != null) {
                mEzvizVideoCall.setDisplay(null);
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @SuppressLint("InflateParams")
    private class OperationManager {

        private boolean isShowVideo = true, isEnableAudio = true;
        private Operation mReceiveVG, mRefuseVG, mHangUpVG, mToggleVideo, mToggleAudio;

        private OperationManager() {
        }

        Operation getReceiveVG() {
            if (mReceiveVG == null) {
                mReceiveVG = new Operation();
                mReceiveVG.image.setImageResource(R.drawable.video_talk_sdk_video_answer);
                mReceiveVG.text.setText("通话");
                mReceiveVG.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCurrentTalkState = TalkStateEnum.ANSWER_TALKING;
                        changeUiByTalkState();

                        mAnswerRingPlayer.stop();
                        mAnswerRingPlayer.release();
                        mAnswerRingPlayer = null;
                        if (mTextThread != null) {
                            mTextThread.quit();
                        }
                        startVideoTalk(EvcParamValueEnum.EvcOperationEnum.ANSWER, mRoomID);
                    }
                });
            }
            return mReceiveVG;
        }

        Operation getRefuseVG() {
            if (mRefuseVG == null) {
                mRefuseVG = new Operation();
                mRefuseVG.image.setImageResource(R.drawable.video_talk_sdk_video_hangup);
                mRefuseVG.text.setText("拒绝");
                mRefuseVG.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        refuseTalk();
                        if (mTextThread != null) {
                            mTextThread.quit();
                        }
                        if (mAnswerRingPlayer != null) {
                            mAnswerRingPlayer.stop();
                            mAnswerRingPlayer.release();
                            mAnswerRingPlayer = null;
                        }
                    }
                });
            }
            return mRefuseVG;
        }

        Operation getHangUpVG() {
            if (mHangUpVG == null) {
                mHangUpVG = new Operation();
                mHangUpVG.image.setImageResource(R.drawable.video_talk_sdk_video_hangup);
                mHangUpVG.text.setText("挂断");
                mHangUpVG.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TalkStateEnum.CALLER_TALKING == mCurrentTalkState
                                || TalkStateEnum.ANSWER_TALKING == mCurrentTalkState) {
                            toast3s(getString(R.string.video_talk_sdk_toast_hang_up));
                            finish();
                        }
                        if (TalkStateEnum.CALLER_CALLING == mCurrentTalkState) {
                            mCallerRingPlayer.stop();
                            finish();
                        }
                    }
                });
            }
            return mHangUpVG;
        }

        Operation getToggleVideoVG() {
            if (mToggleVideo == null) {
                mToggleVideo = new Operation();
                mToggleVideo.image.setImageResource(R.drawable.video_talk_sdk_video_record_on);
                mToggleVideo.text.setText("关闭视频");
                mToggleVideo.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isShowVideo) {
                            isShowVideo = false;
                            if (mEzvizVideoCall != null) {
                                mEzvizVideoCall.stopVideo();
                            }
                            mToggleVideo.text.setText("打开视频");
                        } else {
                            isShowVideo = true;
                            if (mEzvizVideoCall != null) {
                                mEzvizVideoCall.startVideo();
                            }
                            mToggleVideo.text.setText("关闭视频");
                        }
                    }
                });
            }
            return mToggleVideo;
        }

        Operation getToggleAudioVG() {
            if (mToggleAudio == null) {
                mToggleAudio = new Operation();
                mToggleAudio.image.setImageResource(R.drawable.video_talk_sdk_video_record_on);
                mToggleAudio.text.setText("关闭音频");
                mToggleAudio.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isEnableAudio) {
                            isEnableAudio = false;
                            if (mEzvizVideoCall != null) {
                                mEzvizVideoCall.stopAudio();
                            } else {
                                mEzvizVoiceCall.stopAudio();
                            }
                            mToggleAudio.text.setText("打开音频");
                        } else {
                            isEnableAudio = true;
                            if (mEzvizVideoCall != null) {
                                mEzvizVideoCall.startAudio();
                            } else {
                                mEzvizVoiceCall.openAudio();
                            }
                            mToggleAudio.text.setText("关闭音频");
                        }
                    }
                });
            }
            return mToggleAudio;
        }

        private class Operation {
            private View view;
            private ImageView image;
            private TextView text;

            Operation() {
                LayoutInflater inflater = SafetyHatTalkActivity.this.getLayoutInflater();
                ViewGroup root = findViewById(R.id.vg_child_watch_video_talk_operation_container);
                view = inflater.inflate(R.layout.layout_child_watch_video_talk_operation,
                        root, false);
                image = view.findViewById(R.id.image);
                text = view.findViewById(R.id.text);
            }

        }
    }

    private abstract class QuitSafelyThread extends Thread {
        boolean isRunning = true;

        void quit() {
            isRunning = false;
        }
    }

    /**
     * 透明度值范围 0~255
     * <p>
     * MAX_ALPHA 最大透明度
     * MIN_ALPHA 最小透明度
     * DELTA_ABS 透明度每次变化范围
     * <p>
     * 通过控制以上三个参数就可以调整文字呼吸效果
     */
    private class BreathTextThread extends QuitSafelyThread {

        private TextView mTextView;

        private final static int MAX_ALPHA = 200;
        private final static int MIN_ALPHA = 50;
        private final static int DELTA_ABS = 10;
        private int mDelta;
        private int mAlpha = MAX_ALPHA;

        BreathTextThread(TextView textView) {
            mTextView = textView;
        }

        @Override
        public void run() {
            while (isRunning) {
                if (mAlpha >= MAX_ALPHA) {
                    mAlpha = MAX_ALPHA;
                    mDelta = -DELTA_ABS;
                }
                if (mAlpha <= MIN_ALPHA) {
                    mAlpha = MIN_ALPHA;
                    mDelta = DELTA_ABS;
                }
                mAlpha += mDelta;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setTextColor(Color.argb(mAlpha, 0, 0, 0));
                    }
                });

                try {
                    int ringDuration = 1000;
                    int count = (MAX_ALPHA - MIN_ALPHA) / DELTA_ABS;
                    sleep(ringDuration / count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setTextColor(Color.argb(MAX_ALPHA, 0, 0, 0));
                }
            });
        }
    }

    private class TalkTimerThread extends QuitSafelyThread {

        private TextView mTextView;
        private int mHour;
        private int mMinute;
        private int mSecond;

        TalkTimerThread(TextView textView) {
            mTextView = textView;
            mHour = mMinute = mSecond = 0;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            final StringBuilder builder = new StringBuilder();
            while (isRunning) {

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mSecond++;

                if (mSecond == 60) {
                    mMinute++;
                    mSecond = 0;
                    if (mHintTV == null && mMinute >= 5) {
                        mHintTV = new TextView(SafetyHatTalkActivity.this);
                        mHintTV.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                R.color.video_talk_sdk_hint_color_gray));
                        mHintTV.setTextSize(14.0f);
                        mHintTV.setText("小提示：长时间通话会加快手表电量消耗和发热");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ViewGroup vg = findViewById(R.id.vg_child_watch_video_talk_notification_container);
                                mHintTV.setGravity(Gravity.CENTER_HORIZONTAL);
                                vg.addView(mHintTV);
                            }
                        });
                    }
                    if (mMinute == 60) {
                        mHour++;
                        mMinute = 0;
                        if (mHour >= 24) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTextView.setText("are you tired ?");
                                }
                            });
                            return;
                        }
                    }
                }

                builder.delete(0, builder.toString().length());
                if (mHour != 0) {
                    if (mHour < 10) {
                        builder.append("0");
                    }
                    builder.append(mHour).append(" : ");
                }
                if (mMinute < 10) {
                    builder.append("0");
                }
                builder.append(mMinute).append(" : ");
                if (mSecond < 10) {
                    builder.append("0");
                }
                builder.append(mSecond);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText(builder.toString());
                    }
                });
            }
        }
    }

    public enum TalkStateEnum {

        //呼叫方
        CALLER_CALLING, /*呼叫中*/
        CALLER_TALKING, /*通话中*/
        CALLER_TALKED, /*通话后*/

        //被呼方
        ANSWER_BEING_CALLED, /*呼叫中*/
        ANSWER_TALKING, /*通话中*/
        ANSWER_TALKED /*通话后*/
    }

    @Override
    public void onBackPressed() {
        if (TalkStateEnum.CALLER_TALKING == mCurrentTalkState || TalkStateEnum.ANSWER_TALKING == mCurrentTalkState) {
            AlertDialog ensureDialog = new AlertDialog.Builder(this)
                    .setTitle("退出将结束视频聊天")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .create();
            ensureDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

//        if (mEzvizVideoCall != null) {
//            mEzvizVideoCall.stopVideoTalk();
//        }
//        if (mEzvizVoiceCall != null) {
//            mEzvizVoiceCall.stopVoiceTalk();
//        }
//
//        if (TalkStateEnum.CALLER_CALLING == mCurrentTalkState && mCallerRingPlayer != null) {
//            mCallerRingPlayer.stop();
//        }
//
//        if (TalkStateEnum.ANSWER_BEING_CALLED == mCurrentTalkState && mAnswerRingPlayer != null) {
//            mAnswerRingPlayer.stop();
//        }
//
//        if (TalkStateEnum.CALLER_TALKING == mCurrentTalkState
//                || TalkStateEnum.CALLER_CALLING == mCurrentTalkState) {
//            if (TalkStateEnum.CALLER_TALKING == mCurrentTalkState) {
//                toast3s(getString(R.string.video_talk_sdk_toast_hang_up));
//            }
//            mCurrentTalkState = TalkStateEnum.CALLER_TALKED;
//            finish();
//        }
//
//        if (TalkStateEnum.ANSWER_TALKING == mCurrentTalkState
//                || TalkStateEnum.ANSWER_BEING_CALLED == mCurrentTalkState) {
//            if (TalkStateEnum.ANSWER_TALKING == mCurrentTalkState) {
//                toast3s(getString(R.string.video_talk_sdk_toast_hang_up));
//            }
//            mCurrentTalkState = TalkStateEnum.ANSWER_TALKED;
//            finish();
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mEzvizVideoCall != null) {
            mEzvizVideoCall.release();
        }
        if (mEzvizVoiceCall != null) {
            mEzvizVoiceCall.stopVoiceTalk();
            mEzvizVoiceCall.release();
        }

        if (mAnswerRingPlayer != null) {
            mAnswerRingPlayer.release();
            mAnswerRingPlayer = null;
        }

        if (mCallerRingPlayer != null) {
            mCallerRingPlayer.release();
            mCallerRingPlayer = null;
        }

        if (mTextThread != null) {
            mTextThread.quit();
        }

        if (BuildConfig.DEBUG && DebugUtils.isSavingLogToFile()) {
            DebugUtils.stopSaveLogToFile();
        }
    }

}
