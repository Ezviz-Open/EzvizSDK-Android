package com.videogo.ui.devicelist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.ExtraException;
import com.videogo.openapi.bean.EZProbeDeviceInfoResult;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LocalValidate;
import com.videogo.util.LogUtil;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;

import ezviz.ezopensdk.R;
import ezviz.ezopensdk.configwifi.ApConfigWifiPresenterForFullSdk;
import ezviz.ezopensdk.configwifi.SmartConfigWifiPresenterForFullSdk;
import ezviz.ezopensdk.configwifi.SoundWaveConfigWifiPresenterForFullSdk;
import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.common.RootActivity;
import ezviz.ezopensdkcommon.configwifi.AutoWifiNetConfigActivity;
import ezviz.ezopensdkcommon.configwifi.AutoWifiPrepareStepOneActivity;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiExecutingActivityPresenter;

import static com.videogo.EzvizApplication.getOpenSDK;

public class SeriesNumSearchActivity extends RootActivity implements OnClickListener/*, OnAuthListener*/ {

    private static final String TAG = "SeriesNumSearchActivity";

    protected static final int MSG_QUERY_CAMERA_FAIL = 0;

    protected static final int MSG_QUERY_CAMERA_SUCCESS = 1;

    private static final int MSG_LOCAL_VALIDATE_SERIALNO_FAIL = 8;

    private static final int MSG_LOCAL_VALIDATE_CAMERA_PSW_FAIL = 9;

    private static final int MSG_ADD_CAMERA_SUCCESS = 10;

    private static final int MSG_ADD_CAMERA_FAIL = 12;

    // private static final int SHOW_DIALOG_ADD_FINISHED = 15;

    private static final int SHOW_DIALOG_SET_WIFI = 16;


    public static final String BUNDLE_TYPE = "type";

    public static final String BUNDE_SERIANO = "SerialNo";

    public static final String BUNDE_VERYCODE = "very_code";

    public static final String BUNDLE_ISACTIVATED = "activated";

    private static final String BUNDE_VERYCODE_VALUE = "old";

    private static final String BUNDE_DIALOG_TIP = "tip";

    private final int MODIFYPSD_FAIL_DIALOG_ID = 25;

    private EditText mSeriesNumberEt = null;

    private MessageHandler mMsgHandler = null;

    private WaitDialog mWaitDlg = null;

    private LocalValidate mLocalValidate = null;

    private String mSerialNoStr = "";

    private View mQueryingCameraRyt;

    private View errorPage;

    private View mCameraListLy;

    private TextView mDeviceName = null;

    private ImageView mDeviceIcon = null;

    private Button mAddButton = null;

    // type - 0 Enter the serial number manually， type - 1 Two-dimensional code scanning
    private int mType = 0;

    private Bundle mBundle;

    private String mVerifyCode = null;

    private boolean mHasShowInputPswDialog = false;

    private LocalInfo mLocalInfo = null;

    private View mBtnNext;

    private View mActivateHint;

    private TextView mTitle;

    private View mInputLinearlayout;

    private TextView mTvStatus;

    private TextView mConnectTip;

    private TextView mFailedMsg;

    private String mDeviceType;
    private DeviceModel mDeviceModel;

    
    private boolean isActivated; // Used to determine whether the jump from the activation page
    
    private EZProbeDeviceInfoResult mEZProbeDeviceInfo = null;

    private EditText mVerifyCodeEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_camera_by_series_number_page);

        init();
        initTitleBar();
        findViews();
        initUI();
        setListener();
        getData();
    }

    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitle = mTitleBar.setTitle(R.string.result_txt);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void init() {
        mLocalValidate = new LocalValidate();
        mMsgHandler = new MessageHandler();

        mWaitDlg = new WaitDialog(SeriesNumSearchActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
        // mWaitDlg.setCancelable(false);

        mBundle = getIntent().getExtras();
        if (mBundle != null) {
            mType = mBundle.getInt(BUNDLE_TYPE);
            if (mType == 0) {
                mSerialNoStr = "";
            } else if (mType == 1) {
                mSerialNoStr = mBundle.getString(BUNDE_SERIANO);
                mVerifyCode = mBundle.getString(BUNDE_VERYCODE);
            }
            isActivated = mBundle.getBoolean(BUNDLE_ISACTIVATED, false);
            mDeviceType = mBundle.getString(AutoWifiNetConfigActivity.DEVICE_TYPE);
            mDeviceModel = DeviceModel.getDeviceModel(mDeviceType);
        }
        LogUtil.d(TAG, "mSerialNoStr = " + mSerialNoStr + ",mVerifyCode = " + mVerifyCode + ",deviceType="
                + mDeviceModel);
        mLocalInfo = LocalInfo.getInstance();

//        mTriggerHelper = new UnbindDeviceTriggerHelper(this, R.id.unbind_button);
    }


    private void findViews() {
        mSeriesNumberEt = (EditText) findViewById(R.id.seriesNumberEt);
        mVerifyCodeEt = (EditText) findViewById(R.id.verifycodeEt);
        if (mSerialNoStr != null) {
            mSeriesNumberEt.setText(mSerialNoStr);
        }
        if (mVerifyCode != null) {
            mVerifyCodeEt.setText(mVerifyCode);
        }
        mInputLinearlayout = findViewById(R.id.inputLinearlayout);
        mQueryingCameraRyt = findViewById(R.id.queryingCameraRyt);
        errorPage = findViewById(R.id.errorPage);
        mCameraListLy = findViewById(R.id.cameraListLy);

        mDeviceIcon = (ImageView) findViewById(R.id.deviceIcon);
        mDeviceName = (TextView) findViewById(R.id.deviceName);
        mTvStatus = (TextView) findViewById(R.id.tvStatus);
        mAddButton = (Button) findViewById(R.id.addBtn);
        mBtnNext = findViewById(R.id.btnNext);
        mActivateHint = findViewById(R.id.activateHint);

        mFailedMsg = (TextView) findViewById(R.id.failedMsg);

        mConnectTip = (TextView) findViewById(R.id.connectTip);

        ImageView searchAnim = (ImageView) findViewById(R.id.searchAnim);
        ((AnimationDrawable) searchAnim.getBackground()).start();
    }


    private void initUI() {
        if (mTvStatus != null){
            mTvStatus.setPadding(100,0, 100, 0);
        }
        if (mType == 1) {
            mInputLinearlayout.setVisibility(View.GONE);
        } else {
            showInputSerialNo();
        }
    }


    private void setListener() {
        mSeriesNumberEt.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constant.SERIAL_NO_LENGTH)});
        mAddButton.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mActivateHint.setOnClickListener(this);
    }

    private void getData() {
        if (mType == 1) {
            searchCameraBySN();
        }
    }

    @Override
    public void onClick(View view) {
        mVerifyCode = mVerifyCodeEt.getText().toString().trim();
        if (TextUtils.isEmpty(mVerifyCode)){
            Toast.makeText(this,"VerifyCode can not be empty",Toast.LENGTH_LONG).show();
            return;
        }
        switch (view.getId()) {
            case R.id.searchBtn:
                // Only manually search when the click will, clear the verification code, two-dimensional code over will not be empty
                // If the scanned serial number is the same as the entered serial number, the verification code will not be cleared
                final String serialNo = mSeriesNumberEt.getText().toString().trim();
                if (mSerialNoStr == null || !mSerialNoStr.equals(serialNo)) {
                    mVerifyCode = null;
                    mDeviceType = "";
                    mDeviceModel = null;
                }

                searchCameraBySN();
                break;
            case R.id.addBtn:
                addQueryCamera();
                break;
            case R.id.btnNext:
                Intent intent;
                // demo code here only show wifi configuration
                // Determine the type of equipment (you can determine whether only wireless or wired) jump the corresponding page
                intent = new Intent(this, AutoWifiPrepareStepOneActivity.class);
                intent.putExtra(BUNDE_SERIANO, mSeriesNumberEt.getText().toString());
                intent.putExtra(BUNDE_VERYCODE, mVerifyCodeEt.getText().toString());
                if (mEZProbeDeviceInfo != null && mEZProbeDeviceInfo.getEZProbeDeviceInfo() != null){
                    if (mEZProbeDeviceInfo.getEZProbeDeviceInfo().getSupportAP() == 2 || mEZProbeDeviceInfo.getEZProbeDeviceInfo().getSupportAP() == 1){
                        ConfigWifiExecutingActivityPresenter.addPresenter(new ApConfigWifiPresenterForFullSdk());
                        intent.putExtra("support_Ap", true);
                    }
                    if (mEZProbeDeviceInfo.getEZProbeDeviceInfo().getSupportWifi() == 3){
                        ConfigWifiExecutingActivityPresenter.addPresenter(new SmartConfigWifiPresenterForFullSdk());
                        intent.putExtra("support_Wifi", true);
                    }
                    if (mEZProbeDeviceInfo.getEZProbeDeviceInfo().getSupportSoundWave() == 1){
                        ConfigWifiExecutingActivityPresenter.addPresenter(new SoundWaveConfigWifiPresenterForFullSdk());
                        intent.putExtra("support_sound_wave", true);
                    }
                }else{
                    intent.putExtra("support_unknow_mode", true);
                }
                intent.putExtra("device_type", mDeviceType);
                intent.putExtra(IntentConstants.USING_FULL_EZVIZ_SDK, true);
                startActivity(intent);
                break;
            case R.id.myRetry:
                searchCameraBySN();
                break;
            case R.id.activateHint:
                if (ConnectionDetector.getConnectionType(this) != ConnectionDetector.WIFI) {
                    // 配置wifi
                    showWifiRequiredDialog();
                } else {
//                    ActivateActivity.launch(this, mSerialNoStr, mVerifyCode, mType, mDeviceType);
                    // finish();
                }
                break;
            default:
                break;
        }
    }

    private void showWifiRequiredDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.auto_wifi_dialog_title_wifi_required)
                .setMessage(R.string.please_open_wifi_network_sadp)
                .setNegativeButton(R.string.connect_wlan, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        // Jump the wifi settings interface
                        if (android.os.Build.VERSION.SDK_INT > 10) {
                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                        } else {
                            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    }
                }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).create().show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mBundle = intent.getExtras();
        if (mBundle != null) {
            if (mBundle.containsKey(IntentConsts.EXTRA_DEVICE_INFO)) {
//                mTriggerHelper.onNewIntent(intent);
                return;
            }

            mType = mBundle.getInt(BUNDLE_TYPE);
            if (mType == 0) {
                mSerialNoStr = "";
            } else if (mType == 1) {
                mSerialNoStr = mBundle.getString(BUNDE_SERIANO);
                mVerifyCode = mBundle.getString(BUNDE_VERYCODE);
            }
            isActivated = mBundle.getBoolean(BUNDLE_ISACTIVATED, false);
            mDeviceType = mBundle.getString(AutoWifiNetConfigActivity.DEVICE_TYPE);
            mDeviceModel = DeviceModel.getDeviceModel(mDeviceType);
            //isFromRouterIntroduce = mBundle.getBoolean(RouterIntroduceActivity.IS_FROM_ROUTER_INTRODUCE);
        }
        getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
    }


    public void searchCameraBySN() {
        hideKeyBoard();
        final String serialNo = mSeriesNumberEt.getText().toString().trim();
        mSerialNoStr = serialNo; // wwc add
        mLocalValidate = new LocalValidate();
        try {
            mLocalValidate.localValidatSerialNo(serialNo);
        } catch (BaseException e) {
            sendMessage(MSG_LOCAL_VALIDATE_SERIALNO_FAIL, e.getErrorCode());
            LogUtil.e(TAG, "searchCameraBySN-> local validate serial no fail, errCode:" + e.getErrorCode());
            return;
        }

        // Local network detection
        if (!ConnectionDetector.isNetworkAvailable(SeriesNumSearchActivity.this)) {
            showErrorPage(R.string.query_camera_fail_network_exception, 0);
            return;
        }

        showQueryingCamera();

        new Thread() {
            public void run() {

                mEZProbeDeviceInfo = getOpenSDK().probeDeviceInfo(serialNo,mDeviceType);
                if (mEZProbeDeviceInfo != null){
                    if (mEZProbeDeviceInfo.getBaseException() == null){
                        // TODO: 2018/6/25 添加设备
                        sendMessage(MSG_QUERY_CAMERA_SUCCESS);
                    }else{
                        switch (mEZProbeDeviceInfo.getBaseException().getErrorCode()){

                            case 120023:
                                // TODO: 2018/6/25  设备不在线，未被用户添加 （这里需要网络配置）
                            case 120002:
                                // TODO: 2018/6/25  设备不存在，未被用户添加 （这里需要网络配置）
                            case 120029:
                                // TODO: 2018/6/25  设备不在线，已经被自己添加 (这里需要网络配置)
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LogUtil.i(TAG, "probeDeviceInfo fail :" + mEZProbeDeviceInfo.getBaseException().getErrorCode());
                                        sendMessage(MSG_QUERY_CAMERA_FAIL, mEZProbeDeviceInfo.getBaseException().getErrorCode());
                                    }
                                });
                                break;

                            case 120020:
                                // TODO: 2018/6/25 设备在线，已经被自己添加 (给出提示)
                                sendMessage(MSG_QUERY_CAMERA_FAIL, mEZProbeDeviceInfo.getBaseException().getErrorCode());
                                break;

                            case 120022:
                                // TODO: 2018/6/25  设备在线，已经被别的用户添加 (给出提示)
                            case 120024:
                                // TODO: 2018/6/25  设备不在线，已经被别的用户添加 (给出提示)
                                sendMessage(MSG_QUERY_CAMERA_FAIL, mEZProbeDeviceInfo.getBaseException().getErrorCode());
                                break;

                            default:
                                // TODO: 2018/6/25 请求异常
                                showToast("Request failed = " + mEZProbeDeviceInfo.getBaseException().getErrorCode());
                                break;
                        }
                    }
                }else{
                    sendMessage(MSG_QUERY_CAMERA_FAIL, ErrorCode.ERROR_WEB_NET_EXCEPTION);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissWaitDialog();
                    }
                });
            }
        }.start();
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
    };


    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCAL_VALIDATE_SERIALNO_FAIL:
                    handleLocalValidateSerialNoFail(msg.arg1);
                    break;
                case MSG_LOCAL_VALIDATE_CAMERA_PSW_FAIL:
                    handleLocalValidateCameraPswFail(msg.arg1);
                    break;
                case MSG_QUERY_CAMERA_SUCCESS:
                    handleQueryCameraSuccess();
                    break;
                case MSG_QUERY_CAMERA_FAIL:
                    handleQueryCameraFail(msg.arg1);
                    break;
                case MSG_ADD_CAMERA_SUCCESS:
                    handleAddCameraSuccess();
                    break;
                case MSG_ADD_CAMERA_FAIL:
                    handleAddCameraFail(msg.arg1);
                    break;
                default:
                    LogUtil.e(TAG, "not solove the message" + msg.toString());
                    break;
            }
        }
    }


    private void handleAddCameraSuccess() {
        mWaitDlg.dismiss();

        Intent intent = new Intent(SeriesNumSearchActivity.this, AutoWifiConnectingActivity.class);
        intent.putExtra(SeriesNumSearchActivity.BUNDE_SERIANO, mSerialNoStr);
        intent.putExtra(AutoWifiConnectingActivity.FROM_PAGE,
                AutoWifiConnectingActivity.FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY);
        startActivity(intent);
        
        mHasShowInputPswDialog = false;
    }

    private void handleAddCameraFail(int errCode) {
        mWaitDlg.dismiss();
        mWaitDlg.hide();
        switch (errCode) {
            case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                showToast(R.string.add_camera_fail_network_exception);
                break;
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:
                ActivityUtils.handleSessionException(SeriesNumSearchActivity.this);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                ActivityUtils.handleSessionException(SeriesNumSearchActivity.this);
                break;
            case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                showToast(R.string.add_camera_fail_server_exception);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_VERIFY_CODE_ERROR:
                mVerifyCode = null;
                {
                    //The verification code is legal but wrong
                    Bundle args = new Bundle();
                    args.putString(BUNDE_DIALOG_TIP, getString(R.string.added_camera_verycode_fail_title_txt));

                    // Pop-up prompts
                    if (!isFinishing() && mHasShowInputPswDialog) {
                        showDialog(MODIFYPSD_FAIL_DIALOG_ID, args);
                    } else {
                        showInputCameraVerifyCodeDlg();
                    }
                }
                break;
            case ErrorCode.ERROR_WEB_DEVICE_SO_TIMEOUT:
                showToast(R.string.device_so_timeout);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT:
                showToast(R.string.query_camera_fail_not_exit);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE:
                showToast(R.string.camera_not_online);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_VALICATECODE_ERROR:
                LogUtil.d(TAG, "Add camera failure verification code error = " + errCode);
                mVerifyCode = "";
                break;
            default:
                showToast(R.string.add_camera_fail_server_exception, errCode);
                LogUtil.e(TAG, "handleAddCameraFail->unkown error, errCode:" + errCode);
                //
                // mVerifyCode = null;
                break;
        }
    }

    private void handleLocalValidateCameraPswFail(int errCode) {
        switch (errCode) {
            case ExtraException.CAMERA_PASSWORD_IS_NULL:
                showToast(R.string.camera_password_is_null);
                break;
            default:
                showToast(R.string.camera_password_error, errCode);
                LogUtil.e(TAG, "handleLocalValidateCameraPswFail-> unkown error, errCode:" + errCode);
                break;
        }
        handleCmaeraPswError();
    }

    private void handleAddCameraFailByVerCode() {
        // showToast(R.string.add_camera_verify_code_error);
        showInputCameraVerifyCodeDlg();
    }

    private void handleAddCameraFailByPsw() {
        // showToast(R.string.camera_password_error);
        showInputCameraPswDlg();
    }


    private void handleLocalValidateSerialNoFail(int errCode) {
        switch (errCode) {
            case ExtraException.SERIALNO_IS_NULL:
                showToast(R.string.serial_number_is_null);
                break;
            case ExtraException.SERIALNO_IS_ILLEGAL:
                showToast(R.string.serial_number_put_the_right_no);
                break;
            default:
                showToast(R.string.serial_number_error, errCode);
                LogUtil.e(TAG, "handleLocalValidateSerialNoFail-> unkown error, errCode:" + errCode);
                break;
        }
    }


    private void handleQueryCameraSuccess() {
        if (mEZProbeDeviceInfo != null) {
            LogUtil.i(TAG, "handleQueryCameraSuccess, description:" );
            showAddButton();
        }

//        showCameraList();
//        mDeviceName.setText(mEZProbeDeviceInfo.getSubSerial());
//        mDeviceIcon.setImageResource(getDeviceIcon(""));
    }

    private int getDeviceIcon(String model) {
        DeviceModel deviceModel = DeviceModel.getDeviceModel(model);
        if (deviceModel == null)
            return DeviceModel.OTHER.getDrawable2ResId();
        else
            return deviceModel.getDrawable2ResId();
    }

    private void showAddButton() {
    	LogUtil.i(TAG, "enter showAddButton");
        showCameraList();
        mBtnNext.setVisibility(View.GONE);
        mActivateHint.setVisibility(View.GONE);
        mAddButton.setVisibility(View.VISIBLE);
        mConnectTip.setVisibility(View.GONE);
        mTvStatus.setVisibility(View.GONE);
    }

    private void handleQueryCameraFail(final int errCode) {
        mWaitDlg.dismiss();
        switch (errCode) {
            case ErrorCode.ERROR_WEB_PASSWORD_ERROR:
                handleCmaeraPswError();
                break;
            case ErrorCode.ERROR_WEB_DEVICE_VERSION_UNSUPPORT:
            case ErrorCode.ERROR_WEB_DEVICE_UNSUPPORT:
                showErrorPage(R.string.seek_camera_fail_device_not_support_shipin7, 0);
                break;
            case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                showErrorPage(R.string.query_camera_fail_network_exception, 0);
                break;
            case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                showErrorPage(R.string.query_camera_fail_server_exception, 0);
                break;
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:
                ActivityUtils.handleSessionException(SeriesNumSearchActivity.this);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                showErrorPage(R.string.check_feature_code_fail, errCode);
                //ActivityUtils.handleHardwareError(SeriesNumSearchActivity.this, null);
                ActivityUtils.handleSessionException(SeriesNumSearchActivity.this);
                break;
            case ErrorCode.ERROR_INNER_PARAM_ERROR:
                showErrorPage(R.string.query_camera_fail_network_exception_or_server_exception, 0);
                break;

            // 设备已被他人添加，不可操作
            // The device is not online and has been added by others, you can not operation is.
            case ErrorCode.ERROR_WEB_DEVICE_ONLINE_ADDED:
            case ErrorCode.ERROR_WEB_DEVICE_OFFLINE_ADDED:
                showTipOfAddedByOther();
                break;

            //  设备已被自己添加，但处于在线状态不可操作
            // The device has been added by itself and device is online
            case ErrorCode.ERROR_WEB_DEVICE_NOT_ADD:
                showCameraList();
                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setTextColor(getResources().getColor(R.color.common_text));
                mTvStatus.setText(getString(R.string.tip_of_added_by_yourself_and_online));
                mBtnNext.setVisibility(View.GONE);
                mAddButton.setVisibility(View.GONE);
                mConnectTip.setVisibility(View.GONE);
                break;

            // 设备不在线，可以进行wifi配置
            // The device is offline, you can config wifi for it.
            case ErrorCode.ERROR_WEB_DEVICE_ADD_OWN_AGAIN:
            case ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE:
            case ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT:
            case ErrorCode.ERROR_WEB_DEVICE_OFFLINE_NOT_ADD:
                showWifiConfig();
                break;
            default:
                showErrorPage(R.string.query_camera_fail, errCode);
                LogUtil.e(TAG, "handleQueryCameraFail-> unkown error, errCode:" + errCode);
                break;
        }
    }

    public void showTipOfAddedByOther() {
        showCameraList();
        mTvStatus.setVisibility(View.VISIBLE);
        mTvStatus.setTextColor(getResources().getColor(R.color.common_text));
        mTvStatus.setText(R.string.scan_device_add_by_others);
        mBtnNext.setVisibility(View.GONE);
        mAddButton.setVisibility(View.GONE);
        mConnectTip.setVisibility(View.GONE);
        // mTriggerHelper.onDeviceBoundByOthers(mSearchDevice);
    }

    private void handleCmaeraPswError() {
        // showInputCameraPswDlg();
    }


    private void showInputCameraPswDlg() {
        mHasShowInputPswDialog = true;
        LayoutInflater factory = LayoutInflater.from(SeriesNumSearchActivity.this);
        final View passwordErrorLayout = factory.inflate(R.layout.password_error_layout, null);
        final EditText newPassword = (EditText) passwordErrorLayout.findViewById(R.id.new_password);
        newPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constant.PSW_MAX_LENGTH)});

        final TextView message1 = (TextView) passwordErrorLayout.findViewById(R.id.message1);
        message1.setText(getString(R.string.realplay_password_error_message1));

        mVerifyCode = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesNumSearchActivity.this);
        builder.setTitle(R.string.serial_add_password_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mHasShowInputPswDialog = false;
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mHasShowInputPswDialog = false;
            }
        });

        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Make sure to modify the name
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

    private boolean verifyLegality(String verifyCodeString) {
        if (verifyCodeString.equalsIgnoreCase("")) {
            showInputCameraVerifyCodeDlg();
            return false;
        }
        // if (verifyCodeString.length() != 6) {
        // showToast(R.string.applicati_not_support_illegal_verify);
        // return false;
        // }
        return true;
    }


    public void addQueryCamera() {
    	if(!TextUtils.isEmpty(mVerifyCode)){
    		addQueryCameraAddVerifyCode();
    	} else {
    		showInputCameraVerifyCodeDlg();
    	}
    }

    private void addQueryCameraAddVerifyCode() {

        // Local network detection
        if (!ConnectionDetector.isNetworkAvailable(SeriesNumSearchActivity.this)) {
            showToast(R.string.add_camera_fail_network_exception);
            return;
        }

        mWaitDlg.show();

        new Thread() {
            public void run() {

                try {
                    boolean result = getOpenSDK().addDevice(mSerialNoStr, mVerifyCode);

                    /***********If necessary, the developer needs to save this code***********/
                    // 添加成功过后
                    sendMessage(MSG_ADD_CAMERA_SUCCESS);
                } catch (BaseException e) {
                    ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                    LogUtil.d(TAG, errorInfo.toString());

                    sendMessage(MSG_ADD_CAMERA_FAIL, errorInfo.errorCode);
                    LogUtil.e(TAG, "add camera fail");
                }

            }
        }.start();
    }

    private void showInputSerialNo() {
        showKeyBoard();

        mTitle.setText(R.string.serial_input_text);
        errorPage.setVisibility(View.GONE);
        mCameraListLy.setVisibility(View.GONE);
        mQueryingCameraRyt.setVisibility(View.GONE);
        mInputLinearlayout.setVisibility(View.VISIBLE);
    }

    private void showKeyBoard() {
        mSeriesNumberEt.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSeriesNumberEt, InputMethodManager.SHOW_FORCED);
    }

    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSeriesNumberEt.getWindowToken(), 0);
    }

    private void showQueryingCamera() {
        mInputLinearlayout.setVisibility(View.GONE);
        errorPage.setVisibility(View.GONE);
        mCameraListLy.setVisibility(View.GONE);
        mTitle.setText(R.string.scan_device_search);
        mQueryingCameraRyt.setVisibility(View.VISIBLE);
    }

    private void showCameraList() {
    	LogUtil.i(TAG, "enter showCameraList");
        mTitle.setText(R.string.result_txt);
        mActivateHint.setVisibility(View.GONE);
        errorPage.setVisibility(View.GONE);
        mCameraListLy.setVisibility(View.VISIBLE);
        mQueryingCameraRyt.setVisibility(View.GONE);
        mInputLinearlayout.setVisibility(View.GONE);

        if (mDeviceModel != null) {
            // Update the image of the search camera
            mDeviceIcon.setImageResource(mDeviceModel.getDrawable2ResId());
        } else {
            // Update the image of the search camera
            mDeviceIcon.setImageResource(DeviceModel.OTHER.getDrawable2ResId());
        }
        // Device name processing
        mDeviceName.setText(mSeriesNumberEt.getText().toString().trim());
    }

    private void showErrorPage(int errorMsgId, int errorCode) {
        mInputLinearlayout.setVisibility(View.GONE);
        errorPage.setVisibility(View.VISIBLE);
        if (errorMsgId > 0) {
            mFailedMsg.setText(errorMsgId);
        }
        if (errorCode > 0) {
            mFailedMsg.append("," + errorCode);
        }
        mCameraListLy.setVisibility(View.GONE);
        mQueryingCameraRyt.setVisibility(View.GONE);

    }

    private void showWifiConfig() {
    	boolean bShowActivation = false;
        showCameraList();
        mBtnNext.setVisibility(View.VISIBLE);
        if (!bShowActivation) { // If it is fluorite equipment or jump from the activation page, do not display the activation prompt, otherwise the activation prompt
            mActivateHint.setVisibility(View.GONE);
        } else {
            mActivateHint.setVisibility(View.VISIBLE);
        }
        mTvStatus.setVisibility(View.VISIBLE);
        mConnectTip.setVisibility(View.VISIBLE);
        mAddButton.setVisibility(View.GONE);
        mTvStatus.setTextColor(getResources().getColor(R.color.scan_yellow));
        mTvStatus.setText(R.string.scan_network_unavailible);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        Dialog dialog = null;
        switch (id) {
            case MODIFYPSD_FAIL_DIALOG_ID: {
                String tipTxt = "";
                String type = "";
                if (args != null) {
                    tipTxt = args.getString(BUNDE_DIALOG_TIP);
                    type = args.getString(BUNDLE_TYPE);
                }
                final String typeFinal = type;
                if (!SeriesNumSearchActivity.this.isFinishing()) {
                    dialog = new AlertDialog.Builder(SeriesNumSearchActivity.this).setMessage(tipTxt)
                            .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mHasShowInputPswDialog = false;
                                }
                            }).setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (typeFinal == null || typeFinal.equals("")) {
                                        handleAddCameraFailByVerCode();
                                    } else {
                                        handleAddCameraFailByPsw();
                                    }
                                }
                            }).create();
                }
            }
                break;
            default:
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case MODIFYPSD_FAIL_DIALOG_ID:
                // Modify the display layout
                if (dialog != null) {
                    TextView tv = (TextView) dialog.findViewById(android.R.id.message);
                    tv.setGravity(Gravity.CENTER);
                }
                break;
            case SHOW_DIALOG_SET_WIFI:
                // Modify the display layout
                if (dialog != null) {
                    removeDialog(SHOW_DIALOG_SET_WIFI);
                    TextView tv = (TextView) dialog.findViewById(android.R.id.message);
                    tv.setGravity(Gravity.CENTER);
                }
                break;
            default:
                break;
        }
    }

    private void showInputCameraVerifyCodeDlg() {
        mHasShowInputPswDialog = true;

        mVerifyCode = null;
        // 从布局中加载视图
        LayoutInflater factory = LayoutInflater.from(SeriesNumSearchActivity.this);
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


        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesNumSearchActivity.this);
        builder.setTitle(R.string.camera_detail_verifycode_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mHasShowInputPswDialog = false;
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mHasShowInputPswDialog = false;
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

    @Override
    public void onBackPressed() {
        //
        if (mType == 0 && mInputLinearlayout.getVisibility() != View.VISIBLE) {
            showInputSerialNo();
        } else {
            hideKeyBoard();
            finish();
        }
    }
}
