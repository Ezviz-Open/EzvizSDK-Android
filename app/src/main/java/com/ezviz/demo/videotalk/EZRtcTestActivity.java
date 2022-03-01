package com.ezviz.demo.videotalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ezviz.sdk.videotalk.meeting.EZRtcCallback;
import com.ezviz.sdk.videotalk.meeting.EZRtcParam;
import com.ezviz.sdk.videotalk.sdk.EZRtc;
import com.videogo.exception.BaseException;
import com.videogo.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;

public class EZRtcTestActivity extends AppCompatActivity implements OnStatusChangedListener{

    private final static String TAG = "@@EZRtcTestActivity";
    private final static int REQUEST_NEEDED_PERMISSIONS = 10000;

    // 相机采集参数
    private final int DEFAULT_CAMERA_HEIGHT = 960;
    private final int DEFAULT_CAMERA_WIDTH = 640;

    // 编码器输出参数
    private final int DEFAULT_ENCODER_HEIGHT = 640;
    private final int DEFAULT_ENCODER_WIDTH = 640;
    private final int DEFAULT_FPS = 15;
    private final int DEFAULT_BPS = (int) (512 * 1024 * 0.6);
    private final int DEFAULT_IFI = 2;
    private String DEFAULT_MIME = "video/avc";

    private boolean isInit = false;

    private EZRtc mEZRtc;
    private EZVideoMeetingService.StoredData mStoredData;

    private Toast mCurrentToast;
    private TextureView mLocalView;
    private TextureView mShareView;
    private TextView mShareName, tvRoomId, tvNetQuality, tvCount;
    private Button btnDiss, btnExit;

    //capture
    private CheckBox cbVideo;
    private CheckBox cbAudio;
    private CheckBox cbVideoSmall;

    private List<EZClientInfo> mClientInfoList;
    private List<EZClientInfo> mSubscribeList = new ArrayList<>();
    private RecyclerView mPlayerGridView;
    private RecyclerView mClientListView;

    //info
    private int mRoomID;
    private String mUserId;
    private String mPassword;

    //ScreenRecord
    private static final int REQUEST_MEDIA_PROJECTION = 1000;
    private Switch switchScreen, switchSubShare;

    //service
    private boolean isRecovery;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            EZVideoMeetingService.MyBinder myBinder = (EZVideoMeetingService.MyBinder) iBinder;
            mEZRtc = myBinder.getRtc();
            mEZRtc.setMsgCallback(new EZRtcTestActivity.TalkStateCallBack());
            mStoredData = myBinder.getData();
            mClientInfoList = mStoredData.mClientList;
            if (isRecovery){
                mRoomID = mStoredData.mRoomId;
                mUserId = mStoredData.userId;
                showContent();
            }else {
                initByRole();
                new Thread(() -> {
                    if (mRoomID == -1){
                        int limit = getIntent().getIntExtra(InIntentKeysAndValues.KEY_LIMIT, 100);
                        try {
                            mRoomID = mEZRtc.orderRoom(mUserId, mPassword, limit);
                        } catch (BaseException e) {
                            e.printStackTrace();
                            toast("预定房间失败" + e.getErrorCode());
                            stopService();
                            finish();
                            return;
                        }
                    }
                    mStoredData.mRoomId = mRoomID;
                    mStoredData.userId = mUserId;
                    try {
                        mEZRtc.enterRoom(mRoomID, mPassword, mUserId);
                    } catch (BaseException e) {
                        e.printStackTrace();
                        toast("加入房间失败" + e.getErrorCode());
                        stopService();
                        finish();
                    }
                }).start();

            }
            initLocalView();
            initRecycleView();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
//            mEZVideoMeeting = null;
        }
    };

    private CompoundButton.OnCheckedChangeListener subShareChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                mShareView.setVisibility(View.VISIBLE);
                if (mShareView.isAvailable()){
                    mEZRtc.setScreenShareWindow(new Surface(mShareView.getSurfaceTexture()));
                }
            }else{
                mShareView.setVisibility(View.GONE);
                mEZRtc.setScreenShareWindow(null);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_NEEDED_PERMISSIONS == requestCode && grantResults.length > 0){
            boolean isGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }
            if (isGranted){
                onPermissionsGranted();
            }else{
                showInitFailedDialog("您拒绝授予视频通话功能所需的权限，该功能无法使用");
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void showInitFailedDialog(String errorMsg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("功能初始化失败");
        TextView contentTv = new TextView(this);
        contentTv.setPadding(200,100,200,100);
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
    private boolean checkThisPermission(String permissionName){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED;
        }else{
            return true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isRecovery = getIntent().getBooleanExtra("service", false);
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            finish();
            runOnUiThread(() -> toast3s("手机版本太低，不支持视频通话功能"));
        }
        setContentView(R.layout.activity_confluence);
        ObtainViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean canAccessCamera = checkThisPermission(Manifest.permission.CAMERA);
            boolean canAccessRecord = checkThisPermission(Manifest.permission.RECORD_AUDIO);
            boolean canWriteSD = checkThisPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (canAccessCamera && canAccessRecord && canWriteSD){
                onPermissionsGranted();
            }else{
                ArrayList<String> permissionsList = new ArrayList<>();
                if (!canAccessCamera){
                    mLocalView.setVisibility(View.GONE);
                    permissionsList.add(Manifest.permission.CAMERA);
                }
                if (!canAccessRecord){
                    mLocalView.setVisibility(View.GONE);
                    permissionsList.add(Manifest.permission.RECORD_AUDIO);
                }
                if (!canWriteSD){
                    mLocalView.setVisibility(View.GONE);
                    permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                String[] permissions = new String[permissionsList.size()];
                for (int i=0; i<permissionsList.size(); i++){
                    permissions[i] = permissionsList.get(i);
                }
                requestPermissions(permissions, REQUEST_NEEDED_PERMISSIONS);
            }
        }else{
            onPermissionsGranted();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK){
            Intent service = new Intent(this, EZVideoMeetingService.class);
            service.putExtra(EZVideoMeetingService.SCREEN_PARAM_TYPE, EZVideoMeetingService.SCREEN_RECORDER_CREATE);
            service.putExtra(EZVideoMeetingService.SCREEN_PARAM_CODE, resultCode);
            service.putExtra(EZVideoMeetingService.SCREEN_PARAM_REQUEST, requestCode);
            service.putExtra(EZVideoMeetingService.SCREEN_PARAM_DATA, data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
        }else {
            switchScreen.setChecked(false);
        }

    }

    private void onPermissionsGranted(){
        init();
    }

    private void init() {
        if (isInit){
            return;
        }else{
            isInit = true;
        }

        mPassword = getIntent().getStringExtra(InIntentKeysAndValues.KEY_PASSWORD);
        mRoomID = getIntent().getIntExtra(InIntentKeysAndValues.KEY_ROOM_ID, -1);
        mUserId = getIntent().getStringExtra(InIntentKeysAndValues.KEY_USER_ID);

        connectToService();
        mLocalView.setVisibility(View.VISIBLE);

    }

    private void initRecycleView(){
        PlayerAdapter playerAdapter = new PlayerAdapter(mSubscribeList);
        playerAdapter.setOnStatusChangedListener(this);
        mPlayerGridView.setLayoutManager(new GridLayoutManager(this, 3));
        mPlayerGridView.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                int pos = parent.getChildAdapterPosition(view);
                int column = (pos) % 3 + 1;// 计算这个child 处于第几列

                outRect.top = 3;
                outRect.bottom = 3;
                //注意这里一定要先乘 后除  先除数因为小于1然后强转int后会为0
                outRect.left = (column-1) * 3 / 3; //左侧为(当前条目数-1)/总条目数*divider宽度
                outRect.right = (3-column)* 3 / 3 ;//右侧为(总条目数-当前条目数)/总条目数*divider宽度
            }

        });
        mPlayerGridView.setAdapter(playerAdapter);
        ClientAdapter clientAdapter = new ClientAdapter(mClientInfoList);
        clientAdapter.setOnStatusChangedListener(this);
        mClientListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mClientListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mClientListView.setAdapter(clientAdapter);
    }

    private void ObtainViews() {
        mLocalView = findViewById(R.id.view_child_watch_video_talk_camera);
        tvRoomId = findViewById(R.id.tv_room);
        tvCount = findViewById(R.id.tv_count);
        tvNetQuality = findViewById(R.id.tv_net_quality);
        btnDiss = findViewById(R.id.btn_diss);
        btnExit = findViewById(R.id.btn_exit);

        mPlayerGridView = findViewById(R.id.player_list);
        mClientListView = findViewById(R.id.client_list);

        cbVideo = findViewById(R.id.cb_video);
        cbAudio = findViewById(R.id.cb_audio);
        cbVideoSmall = findViewById(R.id.cb_video_small);

        switchScreen = findViewById(R.id.switch_share_screen);
        switchSubShare = findViewById(R.id.switch_look_share);

        mShareName = findViewById(R.id.tv_share_name);
        mShareView = findViewById(R.id.texture_share);
        mShareView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                LogUtil.d(TAG, "onSurfaceTextureAvailable");
                mEZRtc.setScreenShareWindow(new Surface(surfaceTexture));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                LogUtil.d(TAG, "onSurfaceTextureDestroyed");
                if (mEZRtc != null){
                    mEZRtc.setScreenShareWindow(null);
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });

    }

    private void showContent(){
        tvRoomId.setText(String.format(Locale.CHINA, "roomId:%d, userId:%s", mRoomID, mUserId));
        cbVideo.setChecked(mStoredData.videoState);
        cbVideoSmall.setChecked(mStoredData.smallVideoState);
        cbVideoSmall.setEnabled(mStoredData.videoState);
        cbAudio.setChecked(mStoredData.audioState);
        switchScreen.setChecked(mStoredData.shareSwitch);

        btnDiss.setEnabled(true);
        btnExit.setEnabled(true);
        cbVideo.setEnabled(true);
        cbAudio.setEnabled(true);
        switchScreen.setEnabled(true);

        if (TextUtils.isEmpty(mStoredData.sharedUserId)){
            mShareView.setVisibility(View.GONE);
            mShareName.setText("无人分享");
        }else {
            mShareName.setText(mStoredData.sharedUserId);
            mShareName.append("正在分享");
            switchSubShare.setEnabled(true);
        }
        tvNetQuality.setText("网络" + mStoredData.selfNetQuality.getDesc());
        tvCount.setText("共" + (mClientInfoList.size() + 1) + "人");

        initListener();
    }

    private void initListener() {
        btnDiss.setOnClickListener(view -> {
            AlertDialog ensureDialog = new AlertDialog.Builder(EZRtcTestActivity.this)
                    .setTitle("确定解散房间吗？")
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("确定", (dialog, which) -> {
                        dialog.dismiss();
                        dissolveRoom();
                        stopService();
                        finish();
                    })
                    .create();
            ensureDialog.show();
        });

        btnExit.setOnClickListener(view -> {
            AlertDialog ensureDialog = new AlertDialog.Builder(EZRtcTestActivity.this)
                    .setTitle("确定退出房间吗？")
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("确定", (dialog, which) -> {
                        dialog.dismiss();
                        exitRoom();
                        stopService();
                        finish();
                    })
                    .create();
            ensureDialog.show();
        });

        cbVideo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mEZRtc != null){
                cbVideoSmall.setEnabled(isChecked);
                mEZRtc.enableLocalVideo(isChecked);
            }
        });

        cbVideoSmall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mEZRtc != null){
                mEZRtc.enableLocalSmallVideo(isChecked);
            }
        });

        cbAudio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mEZRtc != null){
                mEZRtc.enableLocalAudio(isChecked, success -> {
                    if (!success){
                        toast("会议说话人数达到上限，你不能说话了");
                        cbAudio.setChecked(false);
                    }
                });
            }
        });

        switchSubShare.setOnCheckedChangeListener(subShareChangeListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            switchScreen.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b){
                    mEZRtc.startScreenShareWithName(EZRtcTestActivity.this, REQUEST_MEDIA_PROJECTION, "Screen_Crasher", new EZRtc.OnApplyResultListener() {
                        @Override
                        public void onResult(boolean success) {
                            if (!success){
                                toast("会议分享人数达到上限，你不能分享了");
                                switchScreen.setChecked(false);
                            }
                        }
                    });
                }else {
                    mEZRtc.stopScreenShare();
                }
            });
        }
    }

    private boolean initByRole() {

        EZRtc.setLogPrintEnable(true);
        EZRtcParam.EncodeParam encodeParam = EZRtcParam.EncodeParam.createDefault();

        encodeParam.width = getIntent().getIntExtra(InIntentKeysAndValues.KEY_PARAM_WIDTH, DEFAULT_CAMERA_WIDTH);
        encodeParam.height = getIntent().getIntExtra(InIntentKeysAndValues.KEY_PARAM_HEIGHT, DEFAULT_CAMERA_HEIGHT);
        encodeParam.bitrate = getIntent().getIntExtra(InIntentKeysAndValues.KEY_PARAM_BITRATE, DEFAULT_BPS);
        encodeParam.fps = getIntent().getIntExtra(InIntentKeysAndValues.KEY_PARAM_FPS, DEFAULT_FPS);
        encodeParam.ifi = DEFAULT_IFI;
        mEZRtc.setVideoEncodeParam(encodeParam);

        EZRtcParam.EncodeParam smallEncodeParam = EZRtcParam.EncodeParam.createDefault();
        smallEncodeParam.width = encodeParam.width / 2;
        smallEncodeParam.height = encodeParam.height / 2;
        smallEncodeParam.bitrate = encodeParam.bitrate / 4;
        smallEncodeParam.fps = encodeParam.fps;
        smallEncodeParam.ifi = DEFAULT_IFI;
        mEZRtc.setSmallVideoEncodeParam(smallEncodeParam);

        EZRtcParam.ScreenEncodeParam shareEncodeParam = new EZRtcParam.ScreenEncodeParam();
        shareEncodeParam.bitrate = DEFAULT_BPS * 4;
        shareEncodeParam.fps = 10;
        shareEncodeParam.ifi = DEFAULT_IFI;
        mEZRtc.setScreenShareEncodeParam(shareEncodeParam);

        mEZRtc.setAudioEncodeType(getIntent().getBooleanExtra(InIntentKeysAndValues.KEY_PARAM_OPUS, false) ? 1 : 0);

        return true;
    }

    private void initLocalView(){
        if (mLocalView.isAvailable()){
            mEZRtc.setLocalWindow(new Surface(mLocalView.getSurfaceTexture()), mLocalView.getWidth(), mLocalView.getHeight());
        }
        mLocalView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mEZRtc.setLocalWindow(new Surface(surface), width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mEZRtc.setLocalWindow(new Surface(surface), width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mEZRtc.setLocalWindow(null, 0, 0);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void connectToService(){
        isRecovery = getIntent().getBooleanExtra(EZVideoMeetingService.LAUNCH_FROM_NOTIFICATION, false);
        Intent service = new Intent(this, EZVideoMeetingService.class);
        if (!isRecovery) {
            service.putExtra(EZVideoMeetingService.SCREEN_PARAM_TYPE, EZVideoMeetingService.CONFLUENCE_INIT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
        }

        bindService(service, mServiceConnection, 0);
    }

    private void debug(String info){
        String debugInfo = "debug msg: " + info;
        toast(debugInfo);
        LogUtil.e(TAG, debugInfo);
    }

    @SuppressWarnings("SameParameterValue")
    private void toast(final String toastMessage){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentToast != null){
                    mCurrentToast.cancel();
                    mCurrentToast = null;
                }
                mCurrentToast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
                mCurrentToast.show();
            }
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void toast3s(final String toastMessage){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentToast != null){
                    mCurrentToast.cancel();
                    mCurrentToast = null;
                }
                final Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
                toast.show();
                mCurrentToast = toast;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mCurrentToast == toast){
                            mCurrentToast.cancel();
                        }
                    }
                }, 3000);
            }
        });

    }

    @Override
    public void onSurfaceSet(String userId, EZRtcParam.StreamType type, Surface surface) {
        mEZRtc.setRemoteWindow(surface, userId, type);
    }

    @Override
    public void onUnSubscribe(String userId, EZRtcParam.StreamType type) {
        if (type != EZRtcParam.StreamType.NONE){
            mEZRtc.setRemoteWindow(null, userId, type);
        }
    }

    @Override
    public void onSubscribe(String userId) {
        EZClientInfo clientInfo = EZClientInfo.findClient(userId, mClientInfoList);
        if (clientInfo.subscribeType == EZRtcParam.StreamType.NONE){
            int position = mSubscribeList.indexOf(clientInfo);
            mSubscribeList.remove(clientInfo);
            mPlayerGridView.getAdapter().notifyItemRemoved(position);
        }else{
            if (!mSubscribeList.contains(clientInfo)){
                mSubscribeList.add(clientInfo);
                mPlayerGridView.getAdapter().notifyItemInserted(mSubscribeList.size() - 1);
            }else {
                mPlayerGridView.getAdapter().notifyItemChanged(mSubscribeList.indexOf(clientInfo));
            }
        }

    }

    private class TalkStateCallBack implements EZRtcCallback {

        @Override
        public void onUserJoinRoom(String userId) {

            runOnUiThread(() -> {
                EZClientInfo clientInfo = new EZClientInfo();
                clientInfo.userId = userId;
                EZClientInfo.insertOrReplace(clientInfo, mClientInfoList);
                mClientListView.getAdapter().notifyDataSetChanged();
                tvCount.setText("共" + (mClientInfoList.size() + 1) + "人");
            });

        }

        @Override
        public void onUserQuitRoom(String userId) {
            runOnUiThread(() -> {
                int pos = EZClientInfo.delete(userId, mClientInfoList);
                int pos2 = EZClientInfo.delete(userId, mSubscribeList);
                if (pos != -1){
                    Objects.requireNonNull(mClientListView.getAdapter()).notifyItemRemoved(pos);
                }
                if (pos2 != -1){
                    Objects.requireNonNull(mPlayerGridView.getAdapter()).notifyItemRemoved(pos2);
                }
                tvCount.setText("共" + (mClientInfoList.size() + 1) + "人");
            });

        }

        @Override
        public void onFirstFrameDisplayed(int width, int height, String userId) {
            toast(String.format(Locale.CHINA, "userId[%s] res[%d*%d]", userId, width, height));
        }

        @Override
        public void onUserVolume(String userId, int volume) {
            runOnUiThread(() -> {
                EZClientInfo clientInfo = EZClientInfo.findClient(userId, mClientInfoList);
                clientInfo.volume = volume;
                mClientListView.getAdapter().notifyItemChanged(mClientInfoList.indexOf(clientInfo));
            });
        }

        @Override
        public void onUserVideoStateChanged(String userId, int state) {
            runOnUiThread(() -> {
                EZClientInfo clientInfo = EZClientInfo.findClient(userId, mClientInfoList);
                if (clientInfo == null){
                    return;
                }
                clientInfo.mVideoAvailable = state;
                EZRtcParam.StreamType lastSubscribeType = clientInfo.subscribeType;
                switch (clientInfo.mVideoAvailable){
                    case 0:
                        if (clientInfo.subscribeType != EZRtcParam.StreamType.NONE){
                            clientInfo.subscribeType = EZRtcParam.StreamType.NONE;
                        }
                        break;
                    case 1:
                        if (clientInfo.subscribeType == EZRtcParam.StreamType.SUB){
                            clientInfo.subscribeType = EZRtcParam.StreamType.NONE;
                        }
                        break;
                    case 2:
                        break;
                }

                mClientListView.getAdapter().notifyItemChanged(mClientInfoList.indexOf(clientInfo));
                if (lastSubscribeType != clientInfo.subscribeType){
                    int deletePos = EZClientInfo.delete(clientInfo.userId, mSubscribeList);
                    mPlayerGridView.getAdapter().notifyItemRemoved(deletePos);
                    mEZRtc.setRemoteWindow(null, clientInfo.userId, lastSubscribeType);
                }

            });

        }

        @Override
        public void onUserAudioStateChanged(String userId, boolean enable) {
            runOnUiThread(() -> {
                EZClientInfo clientInfo = EZClientInfo.findClient(userId, mClientInfoList);
                if (clientInfo != null){
                    clientInfo.mAudioAvailable = enable;
                    mClientListView.getAdapter().notifyItemChanged(mClientInfoList.indexOf(clientInfo));
                }
            });
        }

        @Override
        public void onShareStateChanged(String userId, boolean enable) {
            runOnUiThread(() -> {
                switchSubShare.setEnabled(enable);
                if (!enable){
                    mStoredData.sharedUserId = null;
                    mShareView.setVisibility(View.GONE);
                    mShareName.setText("无人分享");
                    switchSubShare.setOnCheckedChangeListener(null);
                    switchSubShare.setChecked(false);
                }else {
                    mStoredData.sharedUserId = userId;
                    switchSubShare.setOnCheckedChangeListener(subShareChangeListener);
                    EZClientInfo clientInfo = EZClientInfo.findClient(userId, mClientInfoList);
                    if (clientInfo != null){
                        mShareName.setText(clientInfo.userId + "正在分享");
                    }else {
                        mShareName.setText(userId);
                        mShareName.append("正在分享");
                    }
                }
            });

        }

        @Override
        public void onUserNetQualityChanged(String userId, EZRtcParam.NetQuality netQuality) {
            runOnUiThread(() -> {
                EZClientInfo clientInfo = EZClientInfo.findClient(userId, mClientInfoList);
                if (clientInfo != null){
                    clientInfo.netQuality = netQuality;
                    mClientListView.getAdapter().notifyItemChanged(mClientInfoList.indexOf(clientInfo));
                }else if (userId.equals(mUserId)){
                    mStoredData.selfNetQuality = netQuality;
                    tvNetQuality.setText("网络" + netQuality.getDesc());
                }
            });
        }

        @Override
        public void onJoinSucceed() {
            runOnUiThread(() -> {
                toast("加入房间成功");
                showContent();
            });
        }

        @Override
        public void onMoveOut(EZRtcParam.MoveRoomReason reason) {
            runOnUiThread(() -> showAlertDialog("退出房间:" + reason.getDesc()));
        }


        @Override
        public void onError(final int eventCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAlertDialog(eventCode);
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        if (mEZRtc == null){
            super.onBackPressed();
            return;
        }
        AlertDialog ensureDialog = new AlertDialog.Builder(this)
                .setTitle("退出当前页面后，会议将退到后台")
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    for (EZClientInfo clientInfo : mClientInfoList){
                        mEZRtc.setRemoteWindow(null, clientInfo.userId, clientInfo.subscribeType);
                        clientInfo.subscribeType = EZRtcParam.StreamType.NONE;
                    }

                    mStoredData.videoState = cbVideo.isChecked();
                    mStoredData.smallVideoState = cbVideoSmall.isChecked();
                    mStoredData.audioState = cbAudio.isChecked();
                    mStoredData.shareSwitch = switchScreen.isChecked();

                    setResult(RESULT_OK);
                    finish();
                })
                .create();
        ensureDialog.show();
    }

    private void showAlertDialog(String content){
        stopService();
        AlertDialog ensureDialog = new AlertDialog.Builder(this)
                .setTitle(content)
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    exitRoom();
                    finish();
                })
                .create();
        ensureDialog.show();
    }

    private void showAlertDialog(int code){
        showAlertDialog("error:" + code);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEZRtc != null){
            mEZRtc.release();
        }
        unbindService(mServiceConnection);
    }

    private void stopService(){
        Intent intent = new Intent(EZRtcTestActivity.this, EZVideoMeetingService.class);
        stopService(intent);
    }

    private void exitRoom(){
        if(mEZRtc != null){
            mEZRtc.stopScreenShare();
            mEZRtc.enableLocalAudio(false, null);
            mEZRtc.enableLocalVideo(false);
            mEZRtc.enableLocalVideo(false);
            mEZRtc.exitRoom();
        }
    }

    private void dissolveRoom(){
        if(mEZRtc != null){
            mEZRtc.stopScreenShare();
            mEZRtc.enableLocalAudio(false, null);
            mEZRtc.enableLocalVideo(false);
            mEZRtc.enableLocalVideo(false);
            mEZRtc.dissolveRoom();
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class InIntentKeysAndValues {
        public final static String KEY_ROOM_ID = "room_id";
        public final static String KEY_USER_ID = "user_id";
        public final static String KEY_PASSWORD = "key_password";
        public final static String KEY_LIMIT = "key_limit";

        public final static String KEY_PARAM_WIDTH = "key_param_width";
        public final static String KEY_PARAM_HEIGHT = "key_param_height";
        public final static String KEY_PARAM_BITRATE = "key_param_bitrate";
        public final static String KEY_PARAM_FPS = "key_param_fps";

        public final static String KEY_PARAM_OPUS = "key_param_opus";
    }

}
