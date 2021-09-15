package com.ezviz.demo.videotalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
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

import com.ezviz.mediarecoder.configuration.CameraConfiguration;
import com.ezviz.mediarecoder.configuration.VideoConfiguration;
import com.ezviz.mediarecoder.ui.CameraLivingView;
import com.ezviz.mediarecoder.utils.LogUtil;
import com.ezviz.videotalk.EZVideoMeeting;
import com.ezviz.videotalk.EZVideoMeetingCallBack;
import com.ezviz.videotalk.JNAApi;
import com.ezviz.videotalk.videomeeting.VideoMeetingParam;
import com.ezviz.videotalk.videomeeting.ConstVideoMeeting;
import com.ezviz.videotalk.debug.DebugUtils;
import com.ezviz.videotalk.debug.LogFileService;
import com.ezviz.videotalk.jna.BavClientAudioAvailable;
import com.ezviz.videotalk.jna.BavClientShareAvailable;
import com.ezviz.videotalk.jna.BavClientVideoAvailable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.BuildConfig;
import ezviz.ezopensdk.R;

public class ConfluenceActivity extends AppCompatActivity implements OnStatusChangedListener {

    private final static String TAG = "@@ConfluenceActivity";
    private final static int ERROR = -1;
    private final static int REQUEST_NEEDED_PERMISSIONS = 10000;
    private final static int EXTERNAL_WRITE_PERMISSION = 100001;

    public final static String KEY_ROOM_ID = "room_id";
    public final static String KEY_CLIENT_ID = "client_id";
    public final static String KEY_NICK_ID = "nick_id";
    public final static String KEY_PASSWD_ID = "passwd_id";
    public final static String KEY_STS_IP_ID = "sts_ip_id";
    public final static String KEY_STS_PORT_ID = "ts_port_id";
    public final static String KEY_VC_IP_ID = "vc_ip_id";
    public final static String KEY_VC_PORT_ID = "vc_port_id";

    private static boolean isShared = false;

    /*
     *
     * ****调试服务器地址****
     * 线上：vtmhz.ys7.com"
     * test5：test5.ys7.com"
     *
     * */
    // 视频通话服务器地址和端口
    private final static String SERVER = "test12.ys7.com";
    private final static int SERVER_PORT = 8554;

    // 会控服务器地址和端口
    private final static String VC_SERVER = "test12.ys7.com";
    private final static int VC_SERVER_PORT = 443;

    // 相机采集参数
    private final int DEFAULT_CAMERA_HEIGHT = 960;
    private final int DEFAULT_CAMERA_WIDTH = 640;

    // 编码器输出参数
    private final int DEFAULT_ENCODER_HEIGHT = 640;
    private final int DEFAULT_ENCODER_WIDTH = 640;
    private final int DEFAULT_FPS = 15;
    private final int DEFAULT_BPS = (int) (512 * 1024 * 0.6);
    private final int DEFAULT_MAX_BPS = (int) (800 * 1024 * 0.6);
    private final int DEFAULT_IFI = 2;
    private String DEFAULT_MIME = "video/avc";

    private int mRoomID = ERROR;
    private int mClientID = ERROR;
    private boolean isInit = false;

    private Toast mCurrentToast;
    private EZVideoMeeting mEZVideoMeeting;
    private CameraLivingView mCameraView;
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

    //ScreenRecord
    private static final int REQUEST_MEDIA_PROJECTION = 1000;
    private Switch switchScreen, switchSubShare;

    //service
    private boolean isRecovery;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            EZVideoMeetingService.MyBinder myBinder = (EZVideoMeetingService.MyBinder) iBinder;
            mEZVideoMeeting = myBinder.getVideoMeeting();
            mEZVideoMeeting.setMsgCallBack(new TalkStateCallBack());
            initByRole();
            mClientInfoList = myBinder.getClientList();
            if (isRecovery){
                mRoomID = mEZVideoMeeting.getRoomId();
                mClientID = mEZVideoMeeting.getSelfId();
                showContent();
            }else {
                enterRoom();
            }
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
                    mEZVideoMeeting.setRemoteWindow(new Surface(mShareView.getSurfaceTexture()), 0, ConstVideoMeeting.StreamState.BAV_SUB_STREAM_BIG_VIDEO);
                }
            }else{
                mShareView.setVisibility(View.GONE);
                mEZVideoMeeting.setRemoteWindow(null, 0, ConstVideoMeeting.StreamState.BAV_SUB_STREAM_BIG_VIDEO);
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast3s("手机版本太低，不支持视频通话功能");
                }
            });
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
                    mCameraView.setVisibility(View.GONE);
                    permissionsList.add(Manifest.permission.CAMERA);
                }
                if (!canAccessRecord){
                    mCameraView.setVisibility(View.GONE);
                    permissionsList.add(Manifest.permission.RECORD_AUDIO);
                }
                if (!canWriteSD){
                    mCameraView.setVisibility(View.GONE);
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

        if(BuildConfig.DEBUG){
//            DebugUtils.startSaveLogToFile(getApplicationContext());
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "startSaveLogToFile failed: lack of Manifest.permission.WRITE_EXTERNAL_STORAGE");
            } else {
                Calendar calendar = Calendar.getInstance();
                String time = String.format(Locale.CHINA, "video_talk_log_%04d%02d%02d_%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY));
                Log.i(TAG, "year = " + time);
                String logFileNameWithPath = Environment.getExternalStorageDirectory().getPath() + "/1_confluence/" + time + ".txt";
                LogFileService.start(logFileNameWithPath);
            }
        }

        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        switchScreen.setSelected(true);
    }

    private void onPermissionsGranted(){
        init();
        createCall();
    }

    private void enterRoom(){
        VideoMeetingParam VideoMeetingParam = new VideoMeetingParam();
        VideoMeetingParam.roomId = mRoomID;
        VideoMeetingParam.password = getIntent().getStringExtra(KEY_PASSWD_ID);
        VideoMeetingParam.customerId = getIntent().getStringExtra(KEY_NICK_ID);
        VideoMeetingParam.stsServer = getIntent().getStringExtra(KEY_STS_IP_ID);
        VideoMeetingParam.stsPort = getIntent().getIntExtra(KEY_STS_PORT_ID, 0);
        VideoMeetingParam.vcServer = getIntent().getStringExtra(KEY_VC_IP_ID);
        VideoMeetingParam.vcPort = getIntent().getIntExtra(KEY_VC_PORT_ID, 0);
        VideoMeetingParam.clientId = mClientID;
        VideoMeetingParam.debugFilePath = Environment.getExternalStorageDirectory().getPath() + "/1_confluence";
        mEZVideoMeeting.enterMeeting(VideoMeetingParam);
    }

    private void init() {
        if (isInit){
            return;
        }else{
            isInit = true;
        }

        mRoomID = getIntent().getIntExtra(KEY_ROOM_ID, ERROR);
        mClientID = getIntent().getIntExtra(KEY_CLIENT_ID, ERROR);

        connectToService();
        mCameraView.setVisibility(View.VISIBLE);

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
        mCameraView = findViewById(R.id.view_child_watch_video_talk_camera);
        tvRoomId = findViewById(R.id.tv_room);
        tvCount = findViewById(R.id.tv_count);
        tvNetQuality = findViewById(R.id.tv_net_quality);
        btnDiss = findViewById(R.id.btn_diss);
        btnExit = findViewById(R.id.btn_exit);
        btnDiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ensureDialog = new AlertDialog.Builder(ConfluenceActivity.this)
                        .setTitle("确定解散房间吗？")
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
                                mEZVideoMeeting.stopVideo();
                                mEZVideoMeeting.stopSmallVideo();
                                mEZVideoMeeting.stopAudio();
                                mEZVideoMeeting.stopShareScreen();
                                mEZVideoMeeting.dissolveMeeting();
                                stopService();
                                finish();
                            }
                        })
                        .create();
                ensureDialog.show();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog ensureDialog = new AlertDialog.Builder(ConfluenceActivity.this)
                        .setTitle("确定退出房间吗？")
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
                                exitRoom();
                                stopService();
                                finish();
                            }
                        })
                        .create();
                ensureDialog.show();
            }
        });

        mPlayerGridView = findViewById(R.id.player_list);
        mClientListView = findViewById(R.id.client_list);

        cbVideo = findViewById(R.id.cb_video);
        cbAudio = findViewById(R.id.cb_audio);
        cbVideoSmall = findViewById(R.id.cb_video_small);
        cbVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mEZVideoMeeting != null){
                    cbVideoSmall.setEnabled(isChecked);
                    if (isChecked){
                        mEZVideoMeeting.startVideo();
                    }else {
                        mEZVideoMeeting.stopVideo();
                    }
                }
            }
        });

        cbVideoSmall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mEZVideoMeeting != null){
                    if (isChecked){
                        mEZVideoMeeting.startSmallVideo();
                    }else {
                        mEZVideoMeeting.stopSmallVideo();
                    }
                }
            }
        });

        cbAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mEZVideoMeeting != null){
                    if (isChecked){
                        mEZVideoMeeting.startAudio(new EZVideoMeeting.OnAudioStartListener() {
                            @Override
                            public void onAudioStartResult(int result) {
                                if (result != 1){
                                    toast("会议说话人数达到上限，你不能说话了");
                                    cbAudio.setChecked(false);
                                }
                            }
                        });
                    }else {
                        mEZVideoMeeting.stopAudio();
                    }
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            switchScreen = findViewById(R.id.switch_share_screen);
            switchScreen.setChecked(isShared);
            switchScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b){
                        mEZVideoMeeting.startShareScreen(ConfluenceActivity.this, REQUEST_MEDIA_PROJECTION, "Screen_Crasher", new EZVideoMeeting.OnScreenStartListener() {
                            @Override
                            public void onScreenShareResult(int result) {
                                if (result != 1){
                                    toast("会议分享人数达到上限，你不能分享了");
                                    switchScreen.setChecked(false);
                                    mEZVideoMeeting.stopShareScreen();
                                }else {
                                    isShared = true;
                                }
                            }
                        });
                    }else {
                        mEZVideoMeeting.stopShareScreen();
                        isShared = false;
                    }
                }
            });
        }

        switchSubShare = findViewById(R.id.switch_look_share);
        switchSubShare.setOnCheckedChangeListener(subShareChangeListener);

        mShareName = findViewById(R.id.tv_share_name);
        mShareView = findViewById(R.id.texture_share);
        mShareView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                LogUtil.d(TAG, "onSurfaceTextureAvailable");
                mEZVideoMeeting.setRemoteWindow(new Surface(surfaceTexture), 0, ConstVideoMeeting.StreamState.BAV_SUB_STREAM_BIG_VIDEO);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                LogUtil.d(TAG, "onSurfaceTextureDestroyed");
                if (mEZVideoMeeting != null){
                    mEZVideoMeeting.setRemoteWindow(null, 0, ConstVideoMeeting.StreamState.BAV_SUB_STREAM_BIG_VIDEO);
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });

    }

    private void showContent(){
        tvRoomId.setText(String.format(Locale.CHINA, "roomId:%d, clientId:%d", mRoomID, mClientID));
        btnDiss.setEnabled(true);
        btnExit.setEnabled(true);
        cbVideo.setEnabled(true);
        cbAudio.setEnabled(true);
        switchScreen.setEnabled(true);
        if (mEZVideoMeeting.getShareId() < 0){
            mShareView.setVisibility(View.GONE);
            mShareName.setText("无人分享");
        }else {
            EZClientInfo clientInfo = EZClientInfo.findClient(mEZVideoMeeting.getShareId(), mClientInfoList);
            if (clientInfo != null){
                mShareName.setText(clientInfo.name + "正在分享");
            }else {
                mShareName.setText(String.valueOf(mEZVideoMeeting.getShareId()));
                mShareName.append("正在分享");
            }
            switchSubShare.setEnabled(true);
        }
        tvNetQuality.setText("网络" + mEZVideoMeeting.getNetQuality().getDesc());
        tvCount.setText("共" + (mClientInfoList.size() + 1) + "人");
    }

    private boolean initByRole() {

        mEZVideoMeeting.setCameraLivingView(mCameraView);
        mEZVideoMeeting.setLogPrintEnable(true);

        CameraConfiguration.Builder cameraCfgBld = new CameraConfiguration.Builder().setFps(DEFAULT_FPS).setPreview(DEFAULT_CAMERA_HEIGHT, DEFAULT_CAMERA_WIDTH).setOrientation(CameraConfiguration.Orientation.PORTRAIT);
        mEZVideoMeeting.setPreviewConfig(cameraCfgBld.build());
        VideoConfiguration.Builder encoderCfgBld = new VideoConfiguration.Builder().setMime(DEFAULT_MIME).setSize(DEFAULT_ENCODER_WIDTH, DEFAULT_ENCODER_HEIGHT).setBps(DEFAULT_BPS,DEFAULT_MAX_BPS).setFps(DEFAULT_FPS).setIfi(DEFAULT_IFI);
        mEZVideoMeeting.setVideoConfig(encoderCfgBld.build());

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        VideoConfiguration.Builder encoderShare= new VideoConfiguration.Builder().setMime(DEFAULT_MIME).setSize(dm.widthPixels, dm.heightPixels).setBps(DEFAULT_BPS * 2,DEFAULT_MAX_BPS * 2).setFps(DEFAULT_FPS).setIfi(DEFAULT_IFI);
        mEZVideoMeeting.setShareScreenConfig(encoderShare.build());

        VideoConfiguration.Builder encoderCfgBldSmall = new VideoConfiguration.Builder().setMime(DEFAULT_MIME).setSize(DEFAULT_ENCODER_WIDTH / 2, DEFAULT_ENCODER_HEIGHT / 2).setBps(DEFAULT_BPS,DEFAULT_MAX_BPS / 2).setFps(DEFAULT_FPS).setIfi(DEFAULT_IFI);
        mEZVideoMeeting.setSmallVideoConfig(encoderCfgBldSmall.build());

        return true;
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

    private synchronized void createCall() {
        // 搭配MediaPlayer的setAudioStreamType(AudioManager.STREAM_VOICE_CALL)使用，实现通过通话的形式播放声音
        AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (manager != null){
/*
            manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
*/
            manager.setSpeakerphoneOn(true);
        }

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
    public void onSurfaceSet(int clientId, ConstVideoMeeting.StreamState type, Surface surface) {
        mEZVideoMeeting.setRemoteWindow(surface, clientId, type);
    }

    @Override
    public void onUnSubscribe(int clientId, ConstVideoMeeting.StreamState type) {
        if (type != ConstVideoMeeting.StreamState.BAV_STREAM_INVALID){
            mEZVideoMeeting.setRemoteWindow(null, clientId, type);
        }
    }

    @Override
    public void onSubscribe(int clientId) {
        EZClientInfo clientInfo = EZClientInfo.findClient(clientId, mClientInfoList);
        if (clientInfo.subscribeType == ConstVideoMeeting.StreamState.BAV_STREAM_INVALID){
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

    private class TalkStateCallBack implements EZVideoMeetingCallBack {

        @Override
        public void onJoinRoom(final JNAApi.BavJoinInfo joinInfo) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EZClientInfo clientInfo = new EZClientInfo();
                    clientInfo.id = joinInfo.m_uClientId;
                    clientInfo.name = new String(joinInfo.m_sCustomId);
                    clientInfo.roomId = joinInfo.m_uRoomId;
                    EZClientInfo.insertOrReplace(clientInfo, mClientInfoList);
                    mClientListView.getAdapter().notifyDataSetChanged();
                    tvCount.setText("共" + (mClientInfoList.size() + 1) + "人");
                }
            });

        }

        @Override
        public void onQuitRoom(int roomId, final int clientId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int pos = EZClientInfo.delete(clientId, mClientInfoList);
                    int pos2 = EZClientInfo.delete(clientId, mSubscribeList);
                    if (pos != -1){
                        Objects.requireNonNull(mClientListView.getAdapter()).notifyItemRemoved(pos);
                    }
                    if (pos2 != -1){
                        Objects.requireNonNull(mPlayerGridView.getAdapter()).notifyItemRemoved(pos2);
                    }
                    tvCount.setText("共" + (mClientInfoList.size() + 1) + "人");
                }
            });

        }

        @Override
        public void onFirstFrameDisplayed(int width, int height, final int clientId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    btnShareScreen.setEnabled(true);
                }
            });
        }

        @Override
        public void onBadNet(int delayTimeMs, int clientId) {
            debug("bad net, clientId[" + clientId + "] delayTimeMs is " + delayTimeMs + " ms");
        }

        @Override
        public void onClientVolume(final int clientId, final int volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EZClientInfo clientInfo = EZClientInfo.findClient(clientId, mClientInfoList);
                    clientInfo.volume = volume;
                    mClientListView.getAdapter().notifyItemChanged(mClientInfoList.indexOf(clientInfo));
                }
            });
        }

        @Override
        public void onClientVideoAvailable(final BavClientVideoAvailable bavClientVideoAvailable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EZClientInfo clientInfo = EZClientInfo.findClient(bavClientVideoAvailable.m_uClientId, mClientInfoList);
                    if (clientInfo == null){
                        return;
                    }
                    clientInfo.mVideoAvailable = bavClientVideoAvailable.m_sAvailable;
                    ConstVideoMeeting.StreamState lastSubscribeType = clientInfo.subscribeType;
                    switch (clientInfo.mVideoAvailable){
                        case 0:
                            if (clientInfo.subscribeType != ConstVideoMeeting.StreamState.BAV_STREAM_INVALID){
                                clientInfo.subscribeType = ConstVideoMeeting.StreamState.BAV_STREAM_INVALID;
                            }
                            break;
                        case 1:
                            if (clientInfo.subscribeType == ConstVideoMeeting.StreamState.BAV_SUB_STREAM_MIN_VIDEO){
                                clientInfo.subscribeType = ConstVideoMeeting.StreamState.BAV_STREAM_INVALID;
                            }
                            break;
                        case 2:
                            break;
                    }

                    mClientListView.getAdapter().notifyItemChanged(mClientInfoList.indexOf(clientInfo));
                    if (lastSubscribeType != clientInfo.subscribeType){
                        int deletePos = EZClientInfo.delete(clientInfo.id, mSubscribeList);
                        mPlayerGridView.getAdapter().notifyItemRemoved(deletePos);
                        mEZVideoMeeting.setRemoteWindow(null, clientInfo.id, lastSubscribeType);
                    }

                }
            });

        }

        @Override
        public void onClientAudioAvailable(final BavClientAudioAvailable bavClientAudioAvailable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EZClientInfo clientInfo = EZClientInfo.findClient(bavClientAudioAvailable.m_uClientId, mClientInfoList);
                    if (clientInfo != null){
                        clientInfo.mAudioAvailable = bavClientAudioAvailable.m_sAvailable;
                        mClientListView.getAdapter().notifyItemChanged(mClientInfoList.indexOf(clientInfo));
                    }
                }
            });

        }

        @Override
        public void onShareStateChange(final BavClientShareAvailable bavClientShareAvailable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switchSubShare.setEnabled(bavClientShareAvailable.m_sAvailable == 1);
                    if (bavClientShareAvailable.m_sAvailable == 0){
                        mShareView.setVisibility(View.GONE);
                        mShareName.setText("无人分享");
                        switchSubShare.setOnCheckedChangeListener(null);
                        switchSubShare.setChecked(false);
                    }else {
                        switchSubShare.setOnCheckedChangeListener(subShareChangeListener);
                        EZClientInfo clientInfo = EZClientInfo.findClient(bavClientShareAvailable.m_uClientId, mClientInfoList);
                        if (clientInfo != null){
                            mShareName.setText(clientInfo.name + "正在分享");
                        }else {
                            mShareName.setText(String.valueOf(bavClientShareAvailable.m_uClientId));
                            mShareName.append("正在分享");
                        }
                    }
                }
            });

        }

        @Override
        public void onClientUpdated(JNAApi.BavJoinInfo bavJoinInfo) {

        }

        @Override
        public void onClientNetQuality(final int clientId, final ConstVideoMeeting.NetQuality netQuality) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EZClientInfo clientInfo = EZClientInfo.findClient(clientId, mClientInfoList);
                    if (clientInfo != null){
                        clientInfo.netQuality = netQuality;
                        mClientListView.getAdapter().notifyItemChanged(mClientInfoList.indexOf(clientInfo));
                    }else if (clientId == mClientID){
                        tvNetQuality.setText("网络" + netQuality.getDesc());
                    }
                }
            });
        }

        @Override
        public void onJoinSucceed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast("加入房间成功");
                    showContent();
                }
            });
        }

        @Override
        public void onMoveOut(final ConstVideoMeeting.MoveRoomReason reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAlertDialog("退出房间:" + reason.getDesc());
                }
            });
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

        @Override
        public void onNotify(int eventCode) {
//            showAlertDialog(eventCode);
        }

    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class InIntentKeysAndValues {
        public final static String KEY_ROLE = "role";
        public final static int VALUE_CALLER = 0;
        public final static int VALUE_ANSWER = 1;
        final static int VALUE_REFUSE = 2;

        public final static String KEY_ROOM_ID = "room_id";

        public final static String KEY_NICK_NAME = "nick_name";

        public final static String KEY_HEAD_PORTRAIT_REMOTE = "head_portrait_remote";

        public final static String KEY_HEAD_PORTRAIT_LOCAL = "head_portrait_remote";

        public final static String KEY_DEVICE_SERIAL = "device_serial";

        public final static String KEY_TOKEN = "token";

        public final static String KEY_SERVER = "server";

        public final static String KEY_SERVER_PORT = "server_port";
    }


    @Override
    public void onBackPressed() {
        if (mEZVideoMeeting == null){
            super.onBackPressed();
            return;
        }
        AlertDialog ensureDialog = new AlertDialog.Builder(this)
                .setTitle("退出当前页面后，会议将退到后台")
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
                        for (EZClientInfo clientInfo : mClientInfoList){
                            mEZVideoMeeting.setRemoteWindow(null, clientInfo.id, clientInfo.subscribeType);
                            clientInfo.subscribeType = ConstVideoMeeting.StreamState.BAV_STREAM_INVALID;
                        }
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .create();
        ensureDialog.show();
    }

    private void showAlertDialog(String content){
        stopService();
        AlertDialog ensureDialog = new AlertDialog.Builder(this)
                .setTitle(content)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        exitRoom();
                        finish();
                    }
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
        unbindService(mServiceConnection);
        if (BuildConfig.DEBUG && DebugUtils.isSavingLogToFile()){
            DebugUtils.stopSaveLogToFile();
        }
    }

    private void stopService(){
        Intent intent = new Intent(ConfluenceActivity.this, EZVideoMeetingService.class);
        stopService(intent);
    }

    private void exitRoom(){
        if(mEZVideoMeeting != null){
            mEZVideoMeeting.stopShareScreen();
            mEZVideoMeeting.stopAudio();
            mEZVideoMeeting.stopVideo();
            mEZVideoMeeting.stopSmallVideo();
            mEZVideoMeeting.exitMeeting();
        }
    }

}
