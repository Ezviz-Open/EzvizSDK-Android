package com.videogo.ui.message;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import ezviz.ezopensdkcommon.common.RootActivity;
import com.videogo.alarm.AlarmLogInfoManager;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.bean.EZAlarmInfo;
import com.videogo.ui.message.remoteplayback.EZRemotePlayBackActivity;
import com.videogo.util.DataManager;
import com.videogo.util.EZUtils;
import com.videogo.util.VerifyCodeInput;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.TitleBar;

import ezviz.ezopensdk.R;

public class EZMessageImageActivity2 extends RootActivity implements VerifyCodeInput.VerifyCodeErrorListener{

    private static final long HIDE_BAR_DELAY = 2000;
    private static final int MSG_HIDE_BAR = 1;
    public static final int ERROR_WEB_NO_ERROR = 100000; // /< 没有错误
    public static final int ERROR_WEB_NO_DATA = 100000 - 2; // /< 数据为空或不存在

    private TitleBar mTitleBar;
    private CompoundButton mTitleMenuButton;

    private ViewGroup mMenuLayout;
    private TextView mMenuPlayView;
    private TextView mMenuDownloadView;
    private TextView mMenuShareView;

    private ViewGroup mBottomBar;
    private TextView mMessageTypeView;
    private TextView mMessageTimeView;
    private TextView mMessageFromView;
    private Button mVideoButton;

    private LocalInfo mLocalInfo;

    private AlarmLogInfoManager mAlarmLogInfoManager;


    private ImageView mAlarmImageView;

    private AlertDialog mAlertDialog;

    private MyVerifyCodeInputListener mMyVerifyCodeInputListener;

    private EZAlarmInfo mEZAlarmInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_message_image_page);
        mMyVerifyCodeInputListener = new MyVerifyCodeInputListener();

        findViews();
        initData();
        initTitleBar();
        initViews();
        setListner();
    }


    private void findViews() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);

        mMenuLayout = (ViewGroup) findViewById(R.id.menu_layout);
        mMenuPlayView = (TextView) findViewById(R.id.menu_play);
        //mMenuDownloadView = (TextView) findViewById(R.id.menu_download);
        //mMenuShareView = (TextView) findViewById(R.id.menu_share);

        mBottomBar = (ViewGroup) findViewById(R.id.bottom_bar);
        mMessageTypeView = (TextView) findViewById(R.id.message_type);
        mMessageTimeView = (TextView) findViewById(R.id.message_time);
        mMessageFromView = (TextView) findViewById(R.id.message_from);
        mVideoButton = (Button) findViewById(R.id.video_button);
        mAlarmImageView = (ImageView) findViewById(R.id.alarm_image);
    }


    private void initData() {
//        mMessageCtrl = MessageCtrl.getInstance();
        mAlarmLogInfoManager = AlarmLogInfoManager.getInstance();

        mEZAlarmInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_ALARM_INFO);
        if (mEZAlarmInfo == null){
            LogUtil.d("EZMessageImageActivity2","mEZAlarmInfo is null");
            finish();
            return;
        }

        mLocalInfo = LocalInfo.getInstance();
    }


    private void initTitleBar() {
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        mTitleBar.setTitle(R.string.ez_event_message_detail);
    }


    private void initViews() {
//        mAdapter = new ImageAdapter();
//        mGallery.setAdapter(mAdapter);
//        mGallery.setFlingEnable(false);
//        if (mCurrentIndex >= 0) {
//            mGallery.setSelection(mCurrentIndex);
            setupAlarmInfo(mEZAlarmInfo);
            setAlarmImage();
    }

    private void setAlarmImage(){
        EZUtils.loadImage(this, mAlarmImageView, mEZAlarmInfo,this);
    }

    @Override
    public void verifyCodeError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAlertDialog == null){
                    mAlertDialog = VerifyCodeInput.VerifyCodeInputDialog(EZMessageImageActivity2.this,mMyVerifyCodeInputListener);
                }
                if (!mAlertDialog.isShowing()){
                    mAlertDialog.show();
                }
            }
        });
    }

    class MyVerifyCodeInputListener implements VerifyCodeInput.VerifyCodeInputListener{

        @Override
        public void onInputVerifyCode(String verifyCode) {
            DataManager.getInstance().setDeviceSerialVerifyCode(mEZAlarmInfo.getDeviceSerial(),verifyCode);
            setAlarmImage();
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    private void setListner() {
        OnClickListener clickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.video_button:
//                            RemotePlayBackUtils.goToMessageVideoActivity(EZMessageImageActivity2.this, alarm, true);
//                            overridePendingTransition(R.anim.window_anim_slide_in_right, R.anim.window_anim_fade_out);

//                            if (relAlarm != null && relAlarm.getEnumAlarmType() == AlarmType.DETECTOR_IPC_LINK)
//                                alarmInfo = relAlarm;

                            Intent intent = new Intent(EZMessageImageActivity2.this, EZRemotePlayBackActivity.class);
                            intent.putExtra(IntentConsts.EXTRA_ALARM_INFO, mEZAlarmInfo);

                            startActivity(intent);
                            break;
                    }
            }

        };

        mVideoButton.setOnClickListener(clickListener);
        mMenuPlayView.setOnClickListener(clickListener);
        //mMenuDownloadView.setOnClickListener(clickListener);
        //mMenuShareView.setOnClickListener(clickListener);
//        mGallery.setOnClickListener(clickListener);

//        mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                mCurrentIndex = position;
//                if (mCurrentIndex < mAlarmList.size()) {
//                    HikStat.onEvent(EZMessageImageActivity2.this, HikAction.ACTION_MESSAGE_slide);
//                    AlarmLogInfoEx alarm = mAlarmList.get(mCurrentIndex);
//                    setupAlarmInfo(mCurrentIndex, alarm);
//                    if (mAdapter.isEncrypted(position)) {
//                        AlarmType alarmType = alarm.getEnumAlarmType();
//                        AlarmLogInfo relAlarm = alarm.getRelationAlarms();
//                        if (relAlarm != null && relAlarm.getEnumAlarmType() == AlarmType.DETECTOR_IPC_LINK) {
////                            showInputSafePassword(relAlarm);
//                        } else if (alarmType.hasCamera()) {
////                            showInputSafePassword(alarm);
//                        }
//                    }
//                } else
//                    setBarVisibility(false);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

        // mImageView.setOnActionListener(new OnActionListener() {
        //
        // @Override
        // public void onDoubleClick(View v) {
        // setBarVisibility(false);
        // }
        //
        // @Override
        // public void onDrag(View v) {
        // }
        //
        // @Override
        // public void onZoom(View v) {
        // setBarVisibility(false);
        // }
        // });

        mTitleBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mBottomBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void setupAlarmInfo(EZAlarmInfo alarm) {
        AlarmType alarmType = AlarmType.BODY_ALARM;//alarm.getEnumAlarmType();
        mMessageTypeView.setText( getString(alarmType.getTextResId()));
        mMessageFromView.setText(getText(R.string.from) + alarm.getAlarmName());
        mMessageTimeView.setText(alarm.getAlarmStartTime());

        setButtonEnable(alarm);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            LayoutParams layoutParams = (LayoutParams) mMessageTypeView.getLayoutParams();

            layoutParams = (LayoutParams) mMessageTimeView.getLayoutParams();
            layoutParams.topMargin = 0;
            layoutParams.leftMargin = Utils.dip2px(this, 15);
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.addRule(RelativeLayout.BELOW, 0);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.message_type);

            layoutParams = (LayoutParams) mMessageFromView.getLayoutParams();
            layoutParams.topMargin = 0;
            layoutParams.leftMargin = Utils.dip2px(this, 15);
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.addRule(RelativeLayout.BELOW, 0);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.message_time);
            mMessageFromView.setSingleLine(true);
            mMessageFromView.setEllipsize(TruncateAt.END);

//            mVideoButton.setBackgroundResource(R.drawable.full_video_button_selector);
//            mVideoButton.setTextColor(getResources().getColorStateList(R.color.message_full_video_button_selector));
            layoutParams = (LayoutParams) mVideoButton.getLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;

            mBottomBar.setPadding(mBottomBar.getPaddingLeft(), mBottomBar.getPaddingTop(),
                    mBottomBar.getPaddingRight(), Utils.dip2px(this, 10));

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            LayoutParams layoutParams = (LayoutParams) mMessageTypeView.getLayoutParams();

            layoutParams = (LayoutParams) mMessageTimeView.getLayoutParams();
            layoutParams.topMargin = Utils.dip2px(this, 3);
            layoutParams.leftMargin = 0;
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.addRule(RelativeLayout.BELOW, R.id.message_type);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);

            layoutParams = (LayoutParams) mMessageFromView.getLayoutParams();
            layoutParams.topMargin = Utils.dip2px(this, 3);
            layoutParams.leftMargin = 0;
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.addRule(RelativeLayout.BELOW, R.id.message_time);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            mMessageFromView.setSingleLine(false);
            mMessageFromView.setEllipsize(null);

            mVideoButton.setBackgroundResource(R.drawable.login_btn_selector);
//            mVideoButton.setTextColor(getResources().getColorStateList(R.color.message_video_button_selector));
            layoutParams = (LayoutParams) mVideoButton.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = Utils.dip2px(this, 39);

            mBottomBar.setPadding(mBottomBar.getPaddingLeft(), mBottomBar.getPaddingTop(),
                    mBottomBar.getPaddingRight(), Utils.dip2px(this, 30));
        }

//        if (mShareDialog != null)
//            mShareDialog.onOrientationChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (mOpenService != null)
//            mOpenService.loadOnActivityResult(requestCode, resultCode, data);
    }

    private void setButtonEnable(EZAlarmInfo alarm) {
        AlarmType alarmType = AlarmType.BODY_ALARM;
//        AlarmLogInfo relAlarm = alarm.getRelationAlarms();

        mVideoButton.setEnabled(true);
    }

    private void setVideoButtonEnable(EZAlarmInfo alarm) {/*
        String deviceSerial = alarm.getDeviceSerial();

        DeviceInfoEx deviceInfoEx = DeviceManager.getInstance().getDeviceInfoExById(deviceSerial);
        CameraInfoEx cameraInfoEx = CameraManager.getInstance().getAddedCamera(deviceSerial);

        if (deviceInfoEx == null || cameraInfoEx == null) {
            // 设备已删除
            mMenuPlayView.setEnabled(false);
            mVideoButton.setEnabled(false);
        } else {
            boolean status = deviceInfoEx.isOnline();
            if (status) {
                mMenuPlayView.setEnabled(true);
                mVideoButton.setEnabled(true);
            } else {
                DeviceInfoEx deviceInfoBelong = DeviceManager.getInstance().getDeviceInfoExById(
                        deviceInfoEx.getBelongSerial());

                if ((VideoGoNetSDK.getInstance().getUserCloudStatus() && deviceInfoEx.getSupportCloud() == 1)
                        || (deviceInfoBelong != null && deviceInfoBelong.isOnline() && (deviceInfoBelong.getModelType() == DeviceInfoEx.TYPE_R1 || deviceInfoBelong
                                .getModelType() == DeviceInfoEx.TYPE_N1))) {
                    mVideoButton.setEnabled(true);
                    mMenuPlayView.setEnabled(false);
                } else {
                    mVideoButton.setEnabled(false);
                    mMenuPlayView.setEnabled(false);
                }
            }

//            if (!TextUtils.isEmpty(alarm.getAlarmLogId())
//                    && (deviceInfoEx.getCloudServiceStatus() == DeviceConsts.OFF
//                            || deviceInfoEx.getCloudType() != DeviceConsts.CLOUD_TYPE_YS || (alarm.getRecState() & 1) == 0)
//                    && Utils.getN1orR1(deviceInfoEx.getBelongSerial()) == null && (alarm.getRecState() & 1 << 2) == 0) {
//                mVideoButton.setEnabled(false);
//            }
        }
    */}

    @Override
    public void finish() {
        super.finish();
    }



    /*
     * (non-Javadoc)
     * @see com.videogo.ui.devicemgt.GetDeviceOpSmsCodeTask.GetDeviceOpSmsCodeListener#
     * onGetDeviceOpSmsCodeSuccess()
     */
//    @Override
    public void onGetDeviceOpSmsCodeSuccess() {
//        closeSafePasswordDialog();
//        mIsDecrypt = true;
//        AlarmLogInfoEx alarm = mAlarmList.get(mCurrentIndex);
//        DeviceInfoEx device = DeviceManager.getInstance().getDeviceInfoExById(alarm.getDeviceSerial());
//        if (device != null) {
//            device.setDecryptPassword(false);
//        }
//        startActivity((new Intent(this, DecryptViaSmsVerifyActivity.class).putExtra(IntentConstants.EXTRA_DEVICE_ID,
//                alarm.getDeviceSerial())));
//        overridePendingTransition(R.anim.fade_up, R.anim.alpha_fake_fade);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.videogo.ui.devicemgt.GetDeviceOpSmsCodeTask.GetDeviceOpSmsCodeListener#onGetDeviceOpSmsCodeFail
     * (int)
     */
//    @Override
    public void onGetDeviceOpSmsCodeFail(int errorCode) {
//        showToast(R.string.register_get_verify_code_fail, errorCode);
    }
}