package com.videogo.ui.devicelist;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.videogo.EzvizApplication;
import ezviz.ezopensdkcommon.common.RootActivity;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.device.DeviceInfoEx;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.bean.EZProbeDeviceInfoResult;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import java.util.Timer;
import java.util.TimerTask;


import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.configwifi.AutoWifiNetConfigActivity;

import static com.videogo.EzvizApplication.getOpenSDK;

public class AutoWifiConnectingActivity extends RootActivity implements OnClickListener {

    public static final String SUPPORT_NET_WORK = "support_net_work";
    public static final String FROM_PAGE = "from_page";

    public static final int FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY = 1;

    private static final String TAG = "AutoWifiConnectingActivity";

    private static final int MSG_ADD_CAMERA_SUCCESS = 10;

    private static final int MSG_ADD_CAMERA_FAIL = 12;

    private static final int STATUS_WIFI_CONNETCTING = 100;

    private static final int STATUS_REGISTING = 101;

    private static final int STATUS_ADDING_CAMERA = 102;

    private static final int STATUS_ADD_CAMERA_SUCCESS = 103;

    private final static int MSG_OPEN_CLOUD_STORYED_SUCCESS = 104;

    private final static int MSG_OPEN_CLOUD_STORYED_FAIL = 105;

    private static final int ERROR_WIFI_CONNECT = 1000;

    private static final int ERROR_REGIST = 1001;

    private static final int ERROR_ADD_CAMERA = 1002;
    private static final int MAX_TIME_STEP_ONE_WIFI = 60;
    private static final int MAX_TIME_STEP_TWO_REGIST = 60;
    private static final int MAX_TIME_STEP_THREE_ADD = 15;

    private static int ADD_CAMERA_TIMES = 3;

    // Return button
    private View btnBack;

    // title
    private TextView tvTitle;

    // Add the camera to the container
    private View addCameraContainer;

    // Wired connection of containers
    private View lineConnectContainer;

    // status
    private TextView tvStatus;

    // Retry button
    private View btnRetry;

    // Wired connection
    private Button btnLineConnect;

    // connection succeeded
    private View btnLineConnetOk;

    // Complete button
    private View btnFinish;

    // Cloud service is open
    private CheckBox ckbCloundService;

    // understand more
    private View tvMore;

    private String serialNo;

    private String wifiPassword = "";

    private String wifiSSID = "";

    private int errorStep = 0;

    private LocalInfo mLocalInfo;

    private MessageHandler mMsgHandler;

    String mVerifyCode = "";

    private DeviceInfoEx mDeviceInfoEx;

    private ImageView imgAnimation;

    private AnimationDrawable animWaiting;

    private String maskIpAddress;

    private Timer overTimeTimer;

    private MulticastLock lock;

    EZOpenSDKListener.EZStartConfigWifiCallback mEZStartConfigWifiCallback =
        new EZOpenSDKListener.EZStartConfigWifiCallback() {
            @Override
            public void onStartConfigWifiCallback(String deviceSerial, final EZConstants.EZWifiConfigStatus status) {
                AutoWifiConnectingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status == EZConstants.EZWifiConfigStatus.DEVICE_WIFI_CONNECTING) {

                        } else if (status == EZConstants.EZWifiConfigStatus.DEVICE_WIFI_CONNECTED) {
                            if (isWifiConnected) {
                                LogUtil.i(TAG, "defiveFindHandler: receiver WIFI while isWifiConnected is true");
                                return;
                            }
                            LogUtil.d(TAG, "Received WIFI on device connection  " + serialNo);
                            isWifiOkBonjourget = true;
                            isWifiConnected = true;
                            t2 = System.currentTimeMillis();
                            changeStatuss(STATUS_REGISTING);
                        } else if (status == EZConstants.EZWifiConfigStatus.DEVICE_PLATFORM_REGISTED) {
                            LogUtil.d(TAG, "Received PLAT information on device connection " + serialNo);
                            if (isPlatConnected) {
                                LogUtil.i(TAG, "defiveFindHandler: receiver PLAT while isPlatConnected is true");
                                return;
                            }
                            isPlatBonjourget = true;
                            isPlatConnected = true;
                            t3 = System.currentTimeMillis();
                            cancelOvertimeTimer();
                            changeStatuss(STATUS_ADDING_CAMERA);
                            stopWifiConfigOnThread();
                        }
                    }
                });
            }
        };

    private boolean isWifiConnected = false;
    private boolean isPlatConnected = false;

    private boolean isPlatBonjourget = false;
    private boolean isWifiOkBonjourget = false;

    private long t1 = 0;
    private long t2 = 0;
    private long t3 = 0;
    private long t4 = 0;
    private long t5 = 0;

    private View btnCancel;

    private View llyCloundService;

    // private WaitDialog mWaitDlg;

    private int fromPage;

    private boolean isSupportNetWork;

    private boolean isSupportWifi;

    private View tvDeviceWifiConfigTip;

    private String deviceType;

    // private long time;
    private long recordConfigStartTime = 0;
    private int searchErrorCode = 0;

    private View connectStateContainer;

    private View llyStatus1;

    private View llyStatus2;

    private View llyStatus3;

    private View helpTop;

    private View help;

    private View tvSuccess;

    //    private UnbindDeviceTriggerHelper mTriggerHelper;

    private WifiInfo mWifiInfo;

    private String mac;

    private int speed;

    private int strength;
    // Whether to unzip the error if it is unbundled error is not reported
    private boolean isUnbindDeviceError = false;
    private EZProbeDeviceInfoResult mEZProbeDeviceInfo = null;

    // return 0 means success, camera info be saved in mEZProbeDeviceInfo
    // return other value means fail, result is the error code
    private int probeDeviceInfo(String deviceSerial) {
        mEZProbeDeviceInfo = getOpenSDK().probeDeviceInfo(serialNo,deviceType);
            if (mEZProbeDeviceInfo != null) {
                if (mEZProbeDeviceInfo.getBaseException() != null){
                    return mEZProbeDeviceInfo.getBaseException().getErrorCode();
                }
                return 0;
            }
            return 1;//unknown error
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_wifi_connecting);
        // 唤醒，常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        findViews();
        fromPage = getIntent().getIntExtra(FROM_PAGE, 0);
        initUI();
        setListener();
        if (fromPage == FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY) {
            //            mDeviceInfoEx = DeviceManager.getInstance().getDeviceInfoExById(serialNo);
            changeStatuss(STATUS_ADD_CAMERA_SUCCESS);
        } else if (!isSupportWifi) {
            lineConnectClick();
            btnBack.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);
        } else {
            connectCamera();
        }
    }

    private void init() {

        serialNo = getIntent().getStringExtra(SeriesNumSearchActivity.BUNDE_SERIANO);
        mVerifyCode = getIntent().getStringExtra(SeriesNumSearchActivity.BUNDE_VERYCODE);
        wifiPassword = getIntent().getStringExtra(AutoWifiNetConfigActivity.WIFI_PASSWORD);
        deviceType = getIntent().getStringExtra(AutoWifiNetConfigActivity.DEVICE_TYPE);
        wifiSSID = getIntent().getStringExtra(AutoWifiNetConfigActivity.WIFI_SSID);
        isSupportNetWork = getIntent().getBooleanExtra(SUPPORT_NET_WORK, true);
        // 支持声波或者SmartConfig，均判定为设备支持wifi
        isSupportWifi = getIntent().getBooleanExtra(IntentConsts.EXTRA_SUPPORT_SMART_CONFIG, true)
                || getIntent().getBooleanExtra(IntentConsts.EXTRA_SUPPORT_SOUND_WAVE, true);
        LogUtil.d(TAG, "serialNo = "
            + serialNo
            + ",mVerifyCode = "
            + mVerifyCode
            + ",wifiSSID = "
            + wifiSSID
            + ",isSupportNetWork "
            + isSupportNetWork
            + ",isSupportWifi "
            + isSupportWifi
            + ",isFromDeviceSetting = "
            + ",deviceType="
            + deviceType);
        mMsgHandler = new MessageHandler();
        mLocalInfo = LocalInfo.getInstance();

        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
        // 路由器的mac地址
        mac = (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
        if (mWifiInfo != null) {
            speed = mWifiInfo.getLinkSpeed();
            strength = mWifiInfo.getRssi();
        }
    }

    private void findViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCancel = findViewById(R.id.cancel_btn);
        tvTitle = (TextView) findViewById(R.id.tvTitle);

        addCameraContainer = findViewById(R.id.addCameraContainer);
        lineConnectContainer = findViewById(R.id.lineConnectContainer);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        btnRetry = (TextView) findViewById(R.id.btnRetry);
        btnLineConnect = (Button) findViewById(R.id.btnLineConnet);
        btnLineConnetOk = findViewById(R.id.btnLineConnetOk);
        imgAnimation = (ImageView) findViewById(R.id.imgAnimation);
        btnFinish = findViewById(R.id.btnFinish);
        ckbCloundService = (CheckBox) findViewById(R.id.ckbCloundService);
        tvMore = findViewById(R.id.tvMore);
        llyCloundService = findViewById(R.id.llyCloundService);

        connectStateContainer = findViewById(R.id.connectStateContainer);
        llyStatus1 = findViewById(R.id.llyStatus1);
        llyStatus2 = findViewById(R.id.llyStatus2);
        llyStatus3 = findViewById(R.id.llyStatus3);
        helpTop = findViewById(R.id.helpTop);
        help = findViewById(R.id.help);
        tvDeviceWifiConfigTip = findViewById(R.id.tvDeviceWifiConfigTip);
        tvSuccess = findViewById(R.id.tvSuccess);
    }

    @SuppressLint("HandlerLeak")
    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (isFinishing()) {
                        return;
                    }
                    TextView timer = (TextView) msg.obj;
                    int now = Integer.parseInt(timer.getText().toString()) - 1;
                    if (now >= 0) {
                        timer.setText("" + now);
                        Message newMsg = obtainMessage();
                        newMsg.what = 0;
                        newMsg.obj = timer;
                        sendMessageDelayed(newMsg, 1000);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private boolean isLineConnecting;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //        mTriggerHelper.onActivityResult(requestCode, resultCode, intent);
    }

    private void showStatus(int status) {
        connectStateContainer.setVisibility(View.VISIBLE);
        TextView tip = (TextView) llyStatus1.findViewById(R.id.tip);
        View successIcon = llyStatus1.findViewById(R.id.successIcon);
        final TextView timer = (TextView) llyStatus1.findViewById(R.id.timer);
        TextView tip2 = (TextView) llyStatus2.findViewById(R.id.tip);
        View successIcon2 = llyStatus2.findViewById(R.id.successIcon);
        final TextView timer2 = (TextView) llyStatus2.findViewById(R.id.timer);
        TextView tip3 = (TextView) llyStatus3.findViewById(R.id.tip);
        View successIcon3 = llyStatus3.findViewById(R.id.successIcon);
        final TextView timer3 = (TextView) llyStatus3.findViewById(R.id.timer);
        llyStatus1.setVisibility(View.VISIBLE);
        llyStatus2.setVisibility(View.VISIBLE);
        llyStatus3.setVisibility(View.VISIBLE);
        successIcon.setVisibility(View.INVISIBLE);
        successIcon2.setVisibility(View.INVISIBLE);
        successIcon3.setVisibility(View.INVISIBLE);
        timer.setVisibility(View.INVISIBLE);
        timer2.setVisibility(View.INVISIBLE);
        timer3.setVisibility(View.INVISIBLE);
        tip.setText(R.string.auto_wifi_tip_connecting_wifi);
        tip2.setText(R.string.auto_wifi_tip_connecting_server);
        tip3.setText(R.string.auto_wifi_tip_binding_account);
        tip.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.botton_text_size)));
        tip2.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.botton_text_size)));
        tip3.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.botton_text_size)));
        tip.setTextColor(getResources().getColor(R.color.upgrade_gray));
        tip2.setTextColor(getResources().getColor(R.color.upgrade_gray));
        tip3.setTextColor(getResources().getColor(R.color.upgrade_gray));
        tip3.setVisibility(View.VISIBLE);
        // 连接wifi
        if (STATUS_WIFI_CONNETCTING == status) {
            tip.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.tab_text_size)));
            tip.setTextColor(getResources().getColor(R.color.black));
            tip.setText(R.string.auto_wifi_tip_connecting_wifi_ing);
            timer.setVisibility(View.VISIBLE);

            timer.setText(MAX_TIME_STEP_ONE_WIFI + "");
            timer2.setText(MAX_TIME_STEP_THREE_ADD + "");
            timer3.setText(MAX_TIME_STEP_THREE_ADD + "");

            Message msg = timerHandler.obtainMessage();
            msg.what = 0;
            msg.obj = timer;
            timerHandler.sendMessageDelayed(msg, 1000);
        } else if (STATUS_REGISTING == status) {
            timer2.setText(MAX_TIME_STEP_TWO_REGIST + "");
            timer3.setText(MAX_TIME_STEP_THREE_ADD + "");
            successIcon.setVisibility(View.VISIBLE);
            tip2.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.tab_text_size)));
            tip2.setTextColor(getResources().getColor(R.color.black));
            tip.setText(R.string.auto_wifi_tip_connecting_wifi_ok);
            tip2.setText(R.string.auto_wifi_tip_connecting_server_ing);
            timer2.setVisibility(View.VISIBLE);
            Message msg = timerHandler.obtainMessage();
            msg.what = 0;
            msg.obj = timer2;
            timerHandler.sendMessageDelayed(msg, 1000);
        } else if (STATUS_ADDING_CAMERA == status) {
            timer3.setText(MAX_TIME_STEP_THREE_ADD + "");
            if (isLineConnecting) {
                llyStatus1.setVisibility(View.GONE);
                llyStatus2.setVisibility(View.GONE);
            }
            successIcon.setVisibility(View.VISIBLE);
            successIcon2.setVisibility(View.VISIBLE);
            successIcon3.setVisibility(View.INVISIBLE);
            tip3.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.tab_text_size)));
            tip3.setTextColor(getResources().getColor(R.color.black));
            tip.setText(R.string.auto_wifi_tip_connecting_wifi_ok);
            tip2.setText(R.string.auto_wifi_tip_connecting_server_ok);
            tip3.setText(R.string.auto_wifi_tip_binding_account_ing);
            timer3.setVisibility(View.VISIBLE);
            //
            Message msg = timerHandler.obtainMessage();
            msg.what = 0;
            msg.obj = timer3;
            timerHandler.sendMessageDelayed(msg, 1000);
            // } else if (STATUS_LINE_CONNECTING == status) {
            // connectStateContainer.setVisibility(View.VISIBLE);
            // llyStatus1.setVisibility(View.GONE);
            // llyStatus2.setVisibility(View.GONE);
            // llyStatus3.setVisibility(View.VISIBLE);
            // tip3.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.twenty)));
            // tip3.setTextColor(getResources().getColor(R.color.black));
            // tip3.setText(R.string.auto_wifi_tip_binding_account_ing);
            // timer3.setVisibility(View.VISIBLE);
            // Message description = timerHandler.obtainMessage();
            // timerHandler.sendMessage(description);
            // description.what = 0;
            // description.obj = timer3;
            // timerHandler.sendMessageDelayed(description, 1000);
        } else if (STATUS_ADD_CAMERA_SUCCESS == status
            || ERROR_WIFI_CONNECT == status
            || ERROR_REGIST == status
            || ERROR_ADD_CAMERA == status) {
            connectStateContainer.setVisibility(View.GONE);
        } else {

        }
    }

    private void initUI() {
        if (fromPage == FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY) {
            tvTitle.setText(R.string.auto_wifi_title_add_device);
        } else {
            // 一切为了转圈
            // if (TextUtils.isEmpty(deviceType)) {
            tvTitle.setText(R.string.auto_wifi_title_add_device2);
            // } else {
            // tvTitle.setText(R.string.auto_wifi_title_add_device1);
            // }
        }
    }

    private void setListener() {
        btnBack.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnLineConnect.setOnClickListener(this);
        btnLineConnetOk.setOnClickListener(this);
        btnRetry.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        tvMore.setOnClickListener(this);
        help.setOnClickListener(this);
    }

    private void connectCamera() {
        changeStatuss(STATUS_WIFI_CONNETCTING);
    }

    private void start() {

        isWifiConnected = false;
        isPlatConnected = false;
        isWifiOkBonjourget = false;
        isPlatBonjourget = false;
        //Detection 5 seconds ahead of search
        LogUtil.i(TAG, "in start: startOvertimeTimer");
        startOvertimeTimer((MAX_TIME_STEP_ONE_WIFI - 5) * 1000, new Runnable() {
            public void run() {
                final Runnable success = new Runnable() {
                    public void run() {
                        if (isPlatConnected) {
                            return;
                        }
                        // save wifipassword
                        if (!isLineConnecting && !TextUtils.isEmpty(mac) && !"NULL".equals(mac)) {
                            //                            LocalInfo.getInstance().setWifiPassword(mac, wifiPassword);
                        }
                        isPlatConnected = true;
                        t4 = System.currentTimeMillis();
                        changeStatuss(STATUS_ADDING_CAMERA);
                        LogUtil.d(TAG,
                            "start Timeout from the server to obtain the device information is successful");
                    }
                };
                final Runnable fail = new Runnable() {
                    public void run() {
                        t4 = System.currentTimeMillis();
                        LogUtil.d(TAG, "Timeout from the server to get device information failed");
                        stopWifiConfigOnThread();
                        addCameraFailed(isWifiOkBonjourget ? ERROR_REGIST : ERROR_WIFI_CONNECT, searchErrorCode);
                    }
                };

                Thread thr = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.i(TAG, "in start, begin probeDeviceInfo");
                        int result = probeDeviceInfo(serialNo);

                        LogUtil.i(TAG, "in start, got probeDeviceInfo");
                        if (result == 0 && mEZProbeDeviceInfo != null) {
                            LogUtil.i(TAG, "in start, probeDeviceInfo success," + mEZProbeDeviceInfo);
                            runOnUiThread(success);
                            // TODO
                        } else if (result == ErrorCode.ERROR_WEB_DEVICE_ONLINE_NOT_ADD) {
                            LogUtil.i(TAG, "in start, probeDeviceInfo error:ERROR_WEB_DIVICE_ONLINE_NOT_ADD");
                            runOnUiThread(success);
                        } else {
                            LogUtil.i(TAG, "in start, probeDeviceInfo camera not online");
                            runOnUiThread(fail);
                        }
                    }
                });
                thr.start();
            }
        });
        boolean  support_sound_wave = getIntent().getBooleanExtra("support_sound_wave", false);
        boolean  support_Wifi = getIntent().getBooleanExtra("support_Wifi", false);
        boolean isStartedConfigWifi = true;
        if (support_Wifi){
            getOpenSDK().stopConfigWiFi();
            getOpenSDK().startConfigWifi(AutoWifiConnectingActivity.this, serialNo, wifiSSID, wifiPassword,
                    EZConstants.EZWiFiConfigMode.EZWiFiConfigSmart, mEZStartConfigWifiCallback);
        }else if(support_sound_wave){
            getOpenSDK().startConfigWifi(AutoWifiConnectingActivity.this, serialNo, wifiSSID, wifiPassword,
                    EZConstants.EZWiFiConfigMode.EZWiFiConfigWave, mEZStartConfigWifiCallback);
        }else{
            isStartedConfigWifi = false;
        }
        if (isStartedConfigWifi){
            DeviceOnlineStatusMonitor.start(serialNo, mEZStartConfigWifiCallback);
        }
    }

    private synchronized void stopWifiConfigOnThread() {

        // Stop configuration, stop bonjour service
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                getOpenSDK().stopConfigWiFi();
                LogUtil.d(TAG,
                    "stopBonjourOnThread .cost time = " + (System.currentTimeMillis() - startTime) + "ms");
            }
        }).start();
        LogUtil.d(TAG, "stopBonjourOnThread ..................");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();

                break;
            case R.id.cancel_btn:
                cancelOnClick();
                break;
            case R.id.btnRetry:
                retryOnclick();
                break;
            case R.id.btnLineConnet:
                lineConnectClick();
                break;
            case R.id.btnLineConnetOk:
                lineConnectOkClick();
                break;
            case R.id.btnFinish:
                finishOnClick();
                break;
            case R.id.tvMore:
                moreOnClick();
                break;
            case R.id.help:
                helpOnclick();
                break;

            default:
                break;
        }
    }

    private void helpOnclick() {

    }

    private void moreOnClick() {

    }

    private void finishOnClick() {
        // Cloud storage switch, the device is not online or not the latest device or the user does not open the cloud storage service is not see this control
        if (llyCloundService.getVisibility() == View.VISIBLE && ckbCloundService.isChecked()) {
            enableCloudStoryed();
        } else {
            closeActivity();
        }
    }

    private void enableCloudStoryed() {
        // Local network detection
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            showToast(R.string.save_encrypt_password_fail_network_exception);
            return;
        }

        // mWaitDlg.show();
        showWaitDialog(R.string.start_cloud);

        new Thread() {
            @Override
            public void run() {
            }

            ;
        }.start();
    }

    private void cancelOnClick() {
        btnCancel.setVisibility(View.GONE);
        lineConnectContainer.setVisibility(View.GONE);
        addCameraContainer.setVisibility(View.VISIBLE);

        // if (TextUtils.isEmpty(mVerifyCode)) {
        tvTitle.setText(R.string.auto_wifi_title_add_device2);
        // } else {
        // tvTitle.setText(R.string.auto_wifi_title_add_device1);
        // }
        helpTop.setVisibility(View.VISIBLE);
        help.setVisibility(View.VISIBLE);
    }

    private void retryOnclick() {
        helpTop.setVisibility(View.GONE);
        help.setVisibility(View.GONE);
        switch (errorStep) {
            case ERROR_WIFI_CONNECT:
                changeStatuss(STATUS_WIFI_CONNETCTING);
                break;
            case ERROR_REGIST:
                changeStatuss(STATUS_ADDING_CAMERA);
                break;
            case ERROR_ADD_CAMERA:
                recordConfigStartTime = System.currentTimeMillis();
                changeStatuss(STATUS_ADDING_CAMERA);
                break;
            default:
                break;
        }
    }

    private void lineConnectClick() {
        helpTop.setVisibility(View.GONE);
        help.setVisibility(View.GONE);
        connectStateContainer.setVisibility(View.GONE);
        btnCancel.setVisibility(View.VISIBLE);
        lineConnectContainer.setVisibility(View.VISIBLE);

        if (btnLineConnect.getVisibility() == View.VISIBLE) {
            tvTitle.setText(R.string.auto_wifi_line_connect_title);
        } else if (TextUtils.isEmpty(deviceType)) {
            tvTitle.setText(R.string.auto_wifi_network_add_device2);
        } else {
            tvTitle.setText(R.string.auto_wifi_network_add_device1);
        }
        addCameraContainer.setVisibility(View.GONE);
    }

    private void lineConnectOkClick() {
        isLineConnecting = true;
        cancelOnClick();
        help.setVisibility(View.GONE);
        helpTop.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnLineConnect.setVisibility(View.GONE);
        changeStatuss(STATUS_ADDING_CAMERA);
        // if (TextUtils.isEmpty(deviceType)) {
        tvTitle.setText(R.string.auto_wifi_title_add_device2);
        // } else {
        // tvTitle.setText(R.string.auto_wifi_title_add_device1);
        // }
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.auto_wifi_dialog_connecting_msg)
            .setPositiveButton(R.string.update_exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            })
            .setNegativeButton(R.string.wait, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .create()
            .show();
    }

    private void changeStatuss(int Status) {
        tvStatus.setVisibility(View.GONE);
        tvStatus.setText("");
        switch (Status) {
            case STATUS_WIFI_CONNETCTING:
                imgAnimation.setVisibility(View.VISIBLE);
                tvStatus.setText(R.string.auto_wifi_connecting_msg1);
                imgAnimation.setImageResource(R.drawable.connect_wifi_bg);
                animWaiting = (AnimationDrawable) imgAnimation.getDrawable();
                animWaiting.start();
                btnRetry.setVisibility(View.GONE);
                btnLineConnect.setVisibility(View.GONE);
                showStatus(STATUS_WIFI_CONNETCTING);
                recordConfigStartTime = System.currentTimeMillis();

                t1 = System.currentTimeMillis();
                t2 = 0;
                t3 = 0;
                t4 = 0;
                t5 = 0;
                searchErrorCode = 0;
                addCameraError = -1;
                start();
                break;
            case STATUS_REGISTING:
                LogUtil.i(TAG, "change status to REGISTING");
                // tvStatus.setText(R.string.auto_wifi_connecting_msg2);
                // if (isFromDeviceSetting) {
                // tvStatus.setText(R.string.device_wifi_connecting);
                // imgAnimation.setImageResource(R.drawable.divce_config_wifi_wait);
                // } else {
                // }
                // 检测
                cancelOvertimeTimer();
                LogUtil.i(TAG, "in STATUS_REGISTING: startOvertimeTimer");
                startOvertimeTimer((MAX_TIME_STEP_TWO_REGIST - 5) * 1000, new Runnable() {
                    public void run() {
                        getOpenSDK().stopConfigWiFi();
                        final Runnable success = new Runnable() {
                            public void run() {
                                if (isPlatConnected) {
                                    return;
                                }
                                // save wifipassword
                                if (!isLineConnecting && !TextUtils.isEmpty(mac) && !"NULL".equals(mac)) {
                                    //                                    LocalInfo.getInstance().setWifiPassword(mac, wifiPassword);
                                }
                                isPlatConnected = true;
                                t4 = System.currentTimeMillis();
                                changeStatuss(STATUS_ADDING_CAMERA);
                                LogUtil.d(TAG,
                                    "STATUS_REGISTING Timeout from the server to obtain the device information is successful");
                            }
                        };
                        final Runnable fail = new Runnable() {
                            public void run() {
                                t4 = System.currentTimeMillis();
                                LogUtil.d(TAG, "Timeout from the server to get device information failed");
                                stopWifiConfigOnThread();
                                addCameraFailed(ERROR_REGIST, searchErrorCode);
                            }
                        };

                        Thread thr = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.i(TAG, "in change status STATUS_REGISTING, begin probeDeviceInfo");
                                int result = probeDeviceInfo(serialNo);

                                LogUtil.i(TAG, "in change status STATUS_REGISTING, got probeDeviceInfo");
                                if (result == 0 && mEZProbeDeviceInfo != null) {
                                    LogUtil.i(TAG, "in change status STATUS_REGISTING, probeDeviceInfo success, "
                                        + mEZProbeDeviceInfo);
                                    runOnUiThread(success);
                                } else if (result == ErrorCode.ERROR_WEB_DEVICE_ONLINE_NOT_ADD) {
                                    LogUtil.i(TAG, "in change status STATUS_REGISTING, "
                                        + " probeDeviceInfo error:ERROR_WEB_DIVICE_ONLINE_NOT_ADD");
                                    runOnUiThread(success);
                                } else {
                                    LogUtil.i(TAG,
                                        "in change status STATUS_REGISTING, probeDeviceInfo camera not online");
                                    runOnUiThread(fail);
                                }
                            }
                        });
                        thr.start();
                    }
                });
                imgAnimation.setImageResource(R.drawable.register_server_bg);
                animWaiting = (AnimationDrawable) imgAnimation.getDrawable();
                animWaiting.start();
                btnRetry.setVisibility(View.GONE);
                btnLineConnect.setVisibility(View.GONE);
                showStatus(STATUS_REGISTING);
                break;
            case STATUS_ADDING_CAMERA:
                addCameraError = -1;
                tvStatus.setVisibility(View.GONE);
                tvStatus.setText("");
                // tvStatus.setText(R.string.auto_wifi_connecting_msg3);
                // if (isFromDeviceSetting) {
                // tvStatus.setText(R.string.device_wifi_connecting);
                // imgAnimation.setImageResource(R.drawable.divce_config_wifi_wait);
                // } else {
                // }
                imgAnimation.setImageResource(R.drawable.auto_wifi_link_account_bg);
                animWaiting = (AnimationDrawable) imgAnimation.getDrawable();
                animWaiting.start();
                btnRetry.setVisibility(View.GONE);
                btnLineConnect.setVisibility(View.GONE);

                // save wifipassword
                if (!isLineConnecting && !TextUtils.isEmpty(mac) && !"NULL".equals(mac)) {
                    //                                LocalInfo.getInstance().setWifiPassword(mac, wifiPassword);
                }

                LogUtil.d(TAG, "The server gets the device information successfully");
                t4 = System.currentTimeMillis();
                // From the device to switch the wifi interface to the processing
                addQueryCamera();

                showStatus(STATUS_ADDING_CAMERA);
                break;
            case STATUS_ADD_CAMERA_SUCCESS:
                t5 = System.currentTimeMillis();
                // From the device to switch the wifi interface to the processing
                recordConfigTimeAndError();
                btnFinish.setVisibility(View.VISIBLE);
                boolean bX1orX2 =
                    false;//mDeviceInfoEx.getEnumModel() == DeviceModel.X1 || mDeviceInfoEx.getEnumModel() == DeviceModel.X2;
                if (bX1orX2) {
                    imgAnimation.setImageResource(R.drawable.success_img);
                } else {
                    imgAnimation.setImageResource(R.drawable.success);
                }
                // DeviceModel.A1
                // 是否支持营销wifi，只有support_wifi_2.4G=1的时候才生效：1-支持，0-不支持
                // tvStatus.setVisibility(View.VISIBLE);
                //                if (mDeviceInfoEx.getSupportWifiPortal() != DeviceConsts.NOT_SUPPORT) {
                //                    // tvStatus.setText(R.string.add_camera_success_tip);
                //                }

                showStatus(STATUS_ADD_CAMERA_SUCCESS);
                break;
            default:
                break;
        }
    }

    private void recordConfigTimeAndError() {
        // 非有线连接，不是来自添加页面，不是来自设置界面，不是解绑错误
        if (!isLineConnecting && fromPage != FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY && !isUnbindDeviceError) {
        }
    }

    private void startOvertimeTimer(long time, final Runnable run) {
        LogUtil.i(TAG, "Enter startOvertimeTimer: " + run);

        if (overTimeTimer != null) {
            LogUtil.i(TAG, " overTimeTimer.cancel: " + overTimeTimer);
            overTimeTimer.cancel();
            overTimeTimer = null;
        }
        overTimeTimer = new Timer();
        overTimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.d(TAG, "startOvertimeTimer");
                runOnUiThread(run);
                DeviceOnlineStatusMonitor.stop();
            }
        }, time);
        LogUtil.i(TAG, " startOvertimeTimer: timer:" + overTimeTimer + " runnable:" + run);
    }

    private void cancelOvertimeTimer() {
        LogUtil.i(TAG, "Enter cancelOvertimeTimer: ");
        if (overTimeTimer != null) {
            LogUtil.i(TAG, " cancelOvertimeTimer: " + overTimeTimer);
            overTimeTimer.cancel();
        }
    }

    private int addCameraError = -1;

    private void addCameraFailed(int errorStep, int errorCode) {
        this.errorStep = errorStep;
        addCameraError = errorCode;
        tvStatus.setVisibility(View.VISIBLE);
        // Failed to clear the read seconds instant
        if (timerHandler != null) {
            timerHandler.removeMessages(0);
        }
        switch (errorStep) {
            case ERROR_WIFI_CONNECT:
                showStatus(ERROR_WIFI_CONNECT);
                btnRetry.setVisibility(View.VISIBLE);
                // Failed to clear the read seconds instant
                if (isSupportNetWork) {
                    btnLineConnect.setVisibility(View.VISIBLE);
                }
                // Source device switch wifi
                btnLineConnect.setText(R.string.ez_auto_wifi_line_connect);
                imgAnimation.setImageResource(R.drawable.failure_wifi);
                tvStatus.setText(R.string.ez_auto_wifi_connecting_failed);
                helpTop.setVisibility(View.VISIBLE);
                help.setVisibility(View.VISIBLE);
                // stopBonjourOnThread();
                recordConfigTimeAndError();
                break;
            case ERROR_REGIST:
                showStatus(ERROR_REGIST);
                // stopBonjourOnThread();
                btnRetry.setVisibility(View.VISIBLE);
                btnLineConnect.setVisibility(View.GONE);
                imgAnimation.setImageResource(R.drawable.failure_server);
                tvStatus.setText(R.string.auto_wifi_register_failed);
                recordConfigTimeAndError();
                break;
            case ERROR_ADD_CAMERA:
                showStatus(ERROR_ADD_CAMERA);
                btnRetry.setVisibility(View.VISIBLE);
                btnLineConnect.setVisibility(View.GONE);
                imgAnimation.setImageResource(R.drawable.failure_account);
                if (errorCode == ErrorCode.ERROR_WEB_DEVICE_EXCEPTION) {
                    // Device exception
                    tvStatus.setText(
                        getString(R.string.auto_wifi_add_device_failed) + "(" + getString(R.string.device_error) + ")");
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADD_OWN_AGAIN) {
                    // The device has been added by itself
                    // showToast(R.string.query_camera_fail_repeat_error);
                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed2) + "(" + getString(
                        R.string.auto_wifi_device_you_added_already) + ")");
                    btnRetry.setVisibility(View.GONE);
                    btnFinish.setVisibility(View.VISIBLE);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADDED) {
                    // TODO
                    // The device has been added
                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed2) + "(" + getString(
                        R.string.auto_wifi_device_added_already) + ")");
                    btnRetry.setVisibility(View.GONE);
                    btnFinish.setVisibility(View.VISIBLE);
                    //                } else if (errorCode == ErrorCode.ERROR_WEB_NET_EXCEPTION) {
                    //                    // network anomaly
                    //                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed2) + "("
                    //                            + getString(R.string.network_exception) + ")");
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE) {
                    // The device is not online
                    tvStatus.setText(R.string.add_device_failed_not_online);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_VERIFY_CODE_ERROR) {
                    // Verification code error
                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed2)
                        + "("
                        + getString(R.string.verify_code_error)
                        + ")");
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT) {
                    // The device does not exist
                    tvStatus.setText(R.string.auto_wifi_device_not_exist);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADDED_BT_OTHER) {
                    // The device has been added by others
                    tvStatus.setText(R.string.auto_wifi_device_added_by_others);
                    btnRetry.setVisibility(View.GONE);
                    btnFinish.setVisibility(View.VISIBLE);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_OFFLINE_NOT_ADD) {
                    // The device is not online and is not added
                    tvStatus.setText(R.string.ez_add_device_failed_not_online);
                } else if (errorCode > 0) {
                    tvStatus.setText(getErrorTip(R.string.auto_wifi_add_device_failed, errorCode));
                } else {
                    tvStatus.setText(R.string.auto_wifi_add_device_failed);
                }
                recordConfigTimeAndError();
                break;
            default:
                break;
        }
    }

    public void addQueryCamera() {
        // 从设备切换wifi界面来的处理
        //        if (mSearchDevice == null || mSearchDevice.getAvailableChannelCount() <= 0) {
        //            // 将摄像头添加到用户下面
        //            // showToast("该设备已经被添加");
        //            LogUtil.d(TAG, "该设备已被添加");
        ////            addCameraFailed(ERROR_ADD_CAMERA, ErrorCode.ERROR_WEB_SET_EMAIL_REPEAT_ERROR);
        //            return;
        //        }
        LogUtil.d(TAG, "Add a camera： mVerifyCode = " + mVerifyCode);
        //mj        
        //boolean stub = mSearchDevice.getReleaseVersion() != null && !mSearchDevice.getReleaseVersion().contains("DEFAULT");
        boolean stub = false;
        if (stub) {
            if (!TextUtils.isEmpty(mVerifyCode)) {
                // First click if you already have a verification code, then add it directly
                addQueryCameraAddVerifyCode();
            } else {
                LogUtil.d(TAG, "Add a camera： showInputCameraVerifyCodeDlg mVerifyCode = " + mVerifyCode);
                showInputCameraVerifyCodeDlg();
            }
        } else {
            if (!(TextUtils.isEmpty(mVerifyCode))) {
                addQueryCameraAddVerifyCode();
            } else {
                showInputCameraPswDlg();
            }
        }
    }

    private void addQueryCameraAddVerifyCode() {
        // Local network detection
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            showToast(R.string.add_camera_fail_network_exception);
            return;
        }
        new Thread() {
            public void run() {
                int count = ADD_CAMERA_TIMES;
                while (count > 0) {
                    // Increase the mobile client operation information record
                    boolean isSuccessfulToAdd = false;
                    try {
                        getOpenSDK().addDevice(serialNo, mVerifyCode);
                        isSuccessfulToAdd = true;
                    } catch (BaseException e) {
                        ErrorInfo errorInfo = e.getErrorInfo();
                        // 设备在线，已被自己添加
                        if (errorInfo.errorCode == 120017){
                            isSuccessfulToAdd = true;
                        }else{
                            count--;
                            if (count <= 0) {
                                sendMessage(MSG_ADD_CAMERA_FAIL, errorInfo.errorCode);
                            }
                        }
                    }
                    if (isSuccessfulToAdd){
                        sendMessage(MSG_ADD_CAMERA_SUCCESS);
                        count = -1;
                    }else{
                        showToast("failed to add device!");
                    }
                }
            }
        }.start();
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_CAMERA_SUCCESS:
                    handleAddCameraSuccess();
                    break;
                case MSG_ADD_CAMERA_FAIL:
                    handleAddCameraFail(msg.arg1);
                    break;
                case MSG_OPEN_CLOUD_STORYED_SUCCESS:
                    openCloudSuccess();
                    break;
                case MSG_OPEN_CLOUD_STORYED_FAIL:
                    openCloudFailed(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleAddCameraFail(int errCode) {
        switch (errCode) {
            case 120010:
                LogUtil.d(TAG, "Add camera failure verification code error = " + errCode);
                mVerifyCode = "";
                break;
            default:
                break;
        }
        addCameraFailed(ERROR_ADD_CAMERA, errCode);
    }

    public void openCloudFailed(int arg1) {
        // mWaitDlg.dismiss();
        dismissWaitDialog();
        LogUtil.e(TAG, "Add cloud storage failed, error code：" + arg1);
        new AlertDialog.Builder(this).setTitle(R.string.enable_cloud_fause)
            .setMessage(R.string.enable_cloud_fause_retry)
            .setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.dismiss();
                    enableCloudStoryed();
                    // mWaitDlg.show();
                    showWaitDialog(R.string.start_cloud);
                }
            })
            .setPositiveButton(R.string.not_now, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    closeActivity();
                }
            })
            .setCancelable(false)
            .create()
            .show();
    }

    public void openCloudSuccess() {
        mDeviceInfoEx.setCloudServiceStatus(1);
        // mWaitDlg.dismiss();
        dismissWaitDialog();
        // showToast(id);
        closeActivity();
    }

    public void handleAddCameraSuccess() {
        changeStatuss(STATUS_ADD_CAMERA_SUCCESS);
    }

    private void sendMessage(int msgCode) {
        if (mMsgHandler != null) {
            Message msg = Message.obtain();
            msg.what = msgCode;
            mMsgHandler.sendMessage(msg);
        } else {
            LogUtil.e(TAG, "sendMessage-> mMsgHandler object is null");
        }
    }

    private void sendMessage(int msgCode, int errorCode) {
        if (mMsgHandler != null) {
            Message msg = Message.obtain();
            msg.what = msgCode;
            msg.arg1 = errorCode;
            mMsgHandler.sendMessage(msg);
        } else {
            LogUtil.e(TAG, "sendMessage-> mMsgHandler object is null");
        }
    }

    ;

    private void showInputCameraVerifyCodeDlg() {
        mVerifyCode = null;

        LayoutInflater factory = LayoutInflater.from(this);
        final View passwordErrorLayout = factory.inflate(R.layout.verifycode_layout, null);
        final EditText newPassword = (EditText) passwordErrorLayout.findViewById(R.id.new_password);
        newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        newPassword.setFocusable(true);

        final TextView message1 = (TextView) passwordErrorLayout.findViewById(R.id.message1);
        // StringBuffer sb = new StringBuffer();
        // sb.append("<font color=White >").append(getString(R.string.realplay_verifycode_error_message0))
        // .append("</font>").append("<font color= White>").append(mSearchDevice.getSubSerial()).append("</font>")
        // .append("<font color=White >").append(getString(R.string.realplay_verifycode_error_message1))
        // .append("</font>").append("<font color= White>")
        // .append(getString(R.string.realplay_verifycode_error_message2)+getString(R.string.realplay_verifycode_error_message3)).append("</font>");
        message1.setText(R.string.realplay_verifycode_error_message0);

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.camera_detail_verifycode_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(newPassword.getWindowToken(), 0);
            }
        });

        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mVerifyCode = newPassword.getText().toString();
                if (verifyLegality(mVerifyCode)) {
                    addQueryCameraAddVerifyCode();
                } else {
                    mVerifyCode = null;
                }
            }
        });
        if (!isFinishing()) {
            Dialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();
        }
    }

    private boolean verifyLegality(String verifyCodeString) {
        if (verifyCodeString.equalsIgnoreCase("")) {
            showInputCameraVerifyCodeDlg();
            return false;
        }
        return true;
    }

    private void showInputCameraPswDlg() {

        LayoutInflater factory = LayoutInflater.from(this);
        final View passwordErrorLayout = factory.inflate(R.layout.password_error_layout, null);
        final EditText newPassword = (EditText) passwordErrorLayout.findViewById(R.id.new_password);
        newPassword.setFilters(new InputFilter[] { new InputFilter.LengthFilter(Constant.PSW_MAX_LENGTH) });

        final TextView message1 = (TextView) passwordErrorLayout.findViewById(R.id.message1);
        message1.setText(getString(R.string.realplay_password_error_message1));

        mVerifyCode = null;

        // TextView titleView = new TextView(this);
        // titleView.setTextSize(R.dimen.button_text_size);
        // titleView.setText(getString(R.string.serial_add_password_error_title));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.serial_add_password_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(newPassword.getWindowToken(), 0);
            }
        });

        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 确定修改名称
                String password = newPassword.getText().toString();
                if (pswLegality(password)) {
                    mVerifyCode = newPassword.getText().toString();
                    addQueryCameraAddVerifyCode();
                }
            }
        });
        if (!isFinishing()) {
            Dialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();
        }
    }

    private boolean pswLegality(String pswString) {
        if (pswString.equalsIgnoreCase("")) {
            showInputCameraPswDlg();
            return false;
        }
        return true;
    }

    private void closeActivity() {
        // start the EZCameraList here
        Intent intent = new Intent(this, EZCameraListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (tvDeviceWifiConfigTip.getVisibility() == View.VISIBLE) {
/*mj            CustomApplication obg = (CustomApplication) getApplication();
            HashMap<String, Activity> activitis = obg.getSingleActivities();

            if (activitis.get(AutoWifiNetConfigActivity.class.getName()) != null) {
                activitis.get(AutoWifiNetConfigActivity.class.getName()).finish();
            }
            if (activitis.get(ResetIntroduceActivity.class.getName()) != null) {
                activitis.get(ResetIntroduceActivity.class.getName()).finish();
            }
            finish();*/
            // Has been completed
        } else if (btnFinish.getVisibility() == View.VISIBLE) {
            closeActivity();
            // Wired connection interface
        } else if (btnCancel.getVisibility() == View.VISIBLE) {
            cancelOnClick();
            // If you are configuring (including wired and wireless)
        } else if (connectStateContainer.getVisibility() == View.VISIBLE) {
            showConfirmDialog();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null) {
            timerHandler.removeMessages(0);
        }
        cancelOvertimeTimer();
        stopWifiConfigOnThread();
    }

    private static class DeviceOnlineStatusMonitor{

        private static EZOpenSDKListener.EZStartConfigWifiCallback mCallback = null;
        private static String mDeviceSerial = null;
        private static boolean isMonitoring = false;
        private static Thread mCurrentThread = null;

        static void start(final String deviceSerial, EZOpenSDKListener.EZStartConfigWifiCallback callback){
            if (isMonitoring){
                return;
            }
            LogUtil.d(TAG, "start to monitor device status");
            isMonitoring = true;
            mCallback = callback;
            mDeviceSerial = deviceSerial;
            mCurrentThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isMonitoring){
                        boolean isOnline =false;
                        EZProbeDeviceInfoResult result = getOpenSDK().probeDeviceInfo(mDeviceSerial,null);
                        // online && not added by anyone
                        if (result.getBaseException() == null){
                            isOnline = true;
                            // online && added by current account
                        }else if(result.getBaseException().getErrorCode() == 120020){
                            isOnline = true;
                        }
                        LogUtil.d(TAG, "device is online? " + isOnline);
                        if (isOnline){
                            mCallback.onStartConfigWifiCallback(deviceSerial, EZConstants.EZWifiConfigStatus.DEVICE_PLATFORM_REGISTED);
                            stop();
                        }else{
                            try {
                                Thread.sleep(10 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    LogUtil.d(TAG, "finish to monitor device status");
                }
            });
            mCurrentThread.start();
        }

        static void stop(){
            if (!isMonitoring){
                return;
            }
            LogUtil.d(TAG, "stop to monitor device status");
            if (mCurrentThread != null){
                mCurrentThread.interrupt();
                mCurrentThread = null;
            }
            isMonitoring = false;
            mDeviceSerial = null;
            mCallback = null;
        }

    }

}
