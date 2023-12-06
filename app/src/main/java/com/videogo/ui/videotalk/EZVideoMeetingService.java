package com.videogo.ui.videotalk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ezviz.sdk.videotalk.meeting.EZRtcParam;
import com.ezviz.sdk.videotalk.sdk.EZRtc;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

//@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class EZVideoMeetingService extends Service {

    public static final int SCREEN_RECORDER_CREATE = 1;
    public static final int CONFLUENCE_INIT = 2;

    public static final String SCREEN_PARAM_TYPE = "type";
    public static final String SCREEN_PARAM_CODE = "result_code";
    public static final String SCREEN_PARAM_REQUEST = "request_code";
    public static final String SCREEN_PARAM_DATA = "data";


    public static final String LAUNCH_FROM_NOTIFICATION = "launch_from_notification";

    private EZRtc mEZRtc;

    //用于存储Activity退出后的数据
    private StoredData storedData = new StoredData();

    public static class StoredData{
        public List<EZClientInfo> mClientList = new ArrayList<>();
        public String mAppId;
        public int mRoomId;
        public String userId;
        public EZRtcParam.NetQuality selfNetQuality = EZRtcParam.NetQuality.BAV_NETWORK_QUALITY_UNKNOWN;
        public String sharedUserId;

        //自己当前的状态
        public boolean videoState;
        public boolean smallVideoState;
        public boolean audioState;
        public boolean shareSwitch;
    }

    class MyBinder extends Binder{

        public EZRtc getRtc(){
            return mEZRtc;
        }

        public StoredData getData(){
            return storedData;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            int type = intent.getIntExtra(SCREEN_PARAM_TYPE, -1);
            switch (type){
                case SCREEN_RECORDER_CREATE:
                    createScreenRecorder(intent);
                    break;
                case CONFLUENCE_INIT:
                    createVideoMeeting();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(true);
        }
    }

    private void createVideoMeeting(){
        mEZRtc = new EZRtc(this);
        startForeground(110, createNotificationChannel());
    }

    private void createScreenRecorder(Intent intent){
        int resultCode = intent.getIntExtra(SCREEN_PARAM_CODE, -1);
        int requestCode = intent.getIntExtra(SCREEN_PARAM_REQUEST, -1);
        Intent data = intent.getParcelableExtra(SCREEN_PARAM_DATA);
        mEZRtc.onActivityResult(requestCode, resultCode, data, this);
    }

    private Notification createNotificationChannel(){
        Notification.Builder builder = new Notification.Builder(getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(getApplicationContext(), EZRtcTestActivity.class); //点击后跳转的界面，可以设置跳转数据
        nfIntent.putExtra(LAUNCH_FROM_NOTIFICATION, true);

        builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, nfIntent, PendingIntent.FLAG_CANCEL_CURRENT)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.videogo_icon)) // 设置下拉列表中的图标(大图标)
                //.setContentTitle("SMI InstantView") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.videogo_icon) // 设置状态栏内的小图标
                .setContentText("正在通话中......") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "录屏", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        return notification;
    }

}
