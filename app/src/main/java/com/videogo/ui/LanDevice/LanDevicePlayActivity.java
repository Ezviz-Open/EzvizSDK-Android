package com.videogo.ui.LanDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.videogo.EzvizApplication;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZPlayer;
import com.videogo.util.LogUtil;
import ezviz.ezopensdk.R;

public class LanDevicePlayActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = LanDeviceActivateActivity.class.getName();
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    EZPlayer mEZPlayer;

    private int mUserId;
    private int mChannelNo;
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hcplay);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count++%2 == 0){
                    mEZPlayer.stopRealPlay();
                }else{
                    mEZPlayer.startRealPlay();
                }
            }
        });
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        Intent intent =  getIntent();
        mUserId = intent.getIntExtra("iUserId",-1);
        mChannelNo = intent.getIntExtra("iChannelNumber",1);
        mEZPlayer = EzvizApplication.getOpenSDK().createPlayerWithUserId(mUserId,mChannelNo,1);
        mEZPlayer.setSurfaceHold(mSurfaceHolder);
        mEZPlayer.setHandler(mHandler);
        mEZPlayer.startRealPlay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(null);
        }
    }

    @Override
    protected void onStop() {
        mEZPlayer.stopRealPlay();
        mEZPlayer.release();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtil.d(TAG,"play description what = "+msg.what);
            switch (msg.what) {

                case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                    // TODO: 2017/8/18 play  succes
                    break;
                case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                    // TODO: 2017/8/18 play  fail
                    break;
                case EZConstants.MSG_VIDEO_SIZE_CHANGED:
                    // TODO: 2017/8/18 play  video size changed
                    break;
                default:
                    break;
            }
        }
    };
}
