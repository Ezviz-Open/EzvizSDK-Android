package com.videogo.devicemgt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.videogo.EzvizApplication;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZDeviceVersion;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LogUtil;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;
import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

public class EZDeviceSettingActivity extends RootActivity {
    private final String TAG = "EZDeviceSettingActivity";
    private static final int REQUEST_CODE_BAIDU = 1;

    private static final int REQUEST_CODE_MODIFY_DEVICE_NAME = 2;

    private static final int SHOW_DIALOG_SAFE_MODE = 0;
    private static final int SHOW_DIALOG_CLOSE_SAFE_MODE = 1;
    private final static int SHOW_DIALOG_DEL_DEVICE = 3;
    private final static int SHOW_DIALOG_OFFLINE_NOTIFY = 4;
    private final static int SHOW_DIALOG_WEB_SETTINGS_ENCRYPT = 6;
    private final static int SHOW_DIALOG_WEB_SETTINGS_DEFENCE = 7;
    private TitleBar mTitleBar;
    private ViewGroup mDeviceInfoLayout;
    private TextView mDeviceNameView;
    private TextView mDeviceTypeSnView;

    private ViewGroup mDeviceSNLayout;
    
    private ViewGroup mDefenceLayout;
    private TextView mDefenceView;
    private TextView mDefenceStateView;

    private ViewGroup mDefencePlanParentLayout;
    private View mDefencePlanArrowView;

    private ViewGroup mStorageLayout;
    private View mStorageNoticeView;
    private ViewGroup mVersionLayout;
    private TextView mVersionView;
    private View mVersionNewestView;
    private View mVersionNoticeView;
    private View mVersionArrowView;

    private ViewGroup mEncryptParentLayout;
    private Button mEncryptButton;
    private ViewGroup mModifyPasswordLayout;

    /* 设备删除 */
    private View mDeviceDeleteView;
    private OnClickListener mOnClickListener;

    private TextView mCurrentVersionTextView;
    private Button mDefenceToggleButton;
    private TextView mDeviceSerialTextView;
    private String mValidateCode;
    private EZDeviceVersion mDeviceVersion = null;
    private EZDeviceInfo mEZDeviceInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 页面统计
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_setting_page);

        findViews();
        initData();
        initTitleBar();
        initViews();
    }

    private void findViews() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);

        mDeviceInfoLayout = (ViewGroup) findViewById(R.id.device_info_layout);
        mDeviceNameView = (TextView) findViewById(R.id.device_name);
        mDeviceTypeSnView = (TextView) findViewById(R.id.device_type_sn);

        mDeviceSNLayout = (ViewGroup) findViewById(R.id.ez_device_serial_layout);

        mDefenceLayout = (ViewGroup) findViewById(R.id.defence_layout);
        mDefenceView = (TextView) findViewById(R.id.defence);
        mDefenceStateView = (TextView) findViewById(R.id.defence_state);

        mDefencePlanParentLayout = (ViewGroup) findViewById(R.id.defence_plan_parent_layout);
        mDefencePlanArrowView = findViewById(R.id.defence_plan_arrow);
        mDefenceToggleButton = (Button) findViewById(R.id.defence_toggle_button);


        mStorageLayout = (ViewGroup) findViewById(R.id.storage_layout);
        mStorageNoticeView = findViewById(R.id.storage_notice);
        mVersionLayout = (ViewGroup) findViewById(R.id.version_layout);
        mVersionView = (TextView) findViewById(R.id.version);
        mVersionNewestView = findViewById(R.id.version_newest);
        mVersionNoticeView = findViewById(R.id.version_notice);
        mVersionArrowView = findViewById(R.id.version_arrow);
        mCurrentVersionTextView = (TextView) findViewById(R.id.current_version);

        mEncryptParentLayout = (ViewGroup) findViewById(R.id.encrypt_parent_layout);
        mEncryptButton = (Button) findViewById(R.id.encrypt_button);
        mModifyPasswordLayout = (ViewGroup) findViewById(R.id.modify_password_layout);

        mDeviceDeleteView = findViewById(R.id.device_delete);
        mDeviceSerialTextView = (TextView) findViewById(R.id.ez_device_serial);
    }

    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("Bundle");
        mEZDeviceInfo = bundle.getParcelable(IntentConsts.EXTRA_DEVICE_INFO);
        if (mEZDeviceInfo == null){
            showToast(R.string.device_have_not_added);
            finish();
        }
    }

    private void initTitleBar() {
        mTitleBar.setTitle(R.string.ez_setting);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initViews() {
        if (mEZDeviceInfo != null) {
            mOnClickListener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent;
                    switch (v.getId()) {
                        case R.id.device_info_layout:
                            intent = new Intent(EZDeviceSettingActivity.this, ModifyDeviceNameActivity.class);
                            intent.putExtra(IntentConsts.EXTRA_NAME, mEZDeviceInfo.getDeviceName());
                            intent.putExtra(IntentConsts.EXTRA_DEVICE_ID,mEZDeviceInfo.getDeviceSerial());
                            startActivityForResult(intent, REQUEST_CODE_MODIFY_DEVICE_NAME);
                            break;
                        case R.id.defence_layout:
                        case R.id.defence_toggle_button:
                        	new SetDefenceTask().execute(!(mEZDeviceInfo.getDefence() != 0));
                        	break;

                        case R.id.defence_plan_button:
                            setDefencePlanNew(false);
                            break;

                        case R.id.defence_plan_status_retry:
                            setDefencePlanNew(false);
                            break;

                        case R.id.defence_plan_set_layout:
                            if (mDefencePlanArrowView.getVisibility() == View.VISIBLE) {
                            }
                            setDefencePlanNew(false);
                            break;

                        case R.id.defence_plan_retry:
                            setDefencePlanNew(false);
                            break;

                        case R.id.storage_layout:
                            break;

                        case R.id.version_layout:
                            intent = new Intent(EZDeviceSettingActivity.this, EZUpgradeDeviceActivity.class);
                            intent.putExtra("deviceSerial", mEZDeviceInfo.getDeviceSerial());
                            startActivity(intent);
                            break;

                        case R.id.encrypt_button:
                            gotoSetSafeMode();
                            break;

                        case R.id.modify_password_layout:
                            gotoModifyPassword();
                            break;
                            
                        case R.id.device_delete:
                            showDialog(SHOW_DIALOG_DEL_DEVICE);
                            break;

                        default:
                            break;
                    }
                }
            };

            new GetDeviceInfoTask().execute();

            // 防护计划设置
            setupSafeModePlan(true);

            mDeviceDeleteView.setOnClickListener(mOnClickListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mCloudStateHelper.onResume();
        setupDeviceInfo();
        setupParentLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mCloudStateHelper.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_BAIDU) {
                setupBaiduDeviceInfo(true, true);
            }
            if (requestCode == REQUEST_CODE_MODIFY_DEVICE_NAME){
                String name = data.getStringExtra(IntentConsts.EXTRA_NAME);
                if (!TextUtils.isEmpty(name)){
                    mEZDeviceInfo.setDeviceName(name);
                }else{
                    LogUtil.d(TAG,"modify device name is null");
                }
            }
        }
    }

    private void setupDeviceInfo() {
        if (mEZDeviceInfo != null) {
            // 设备图片部分
            // 设备名字部分
//            String typeSn = String.format("%s(%s)",
//                    TextUtils.isEmpty(mDeviceModel.getDisplay()) ? mDevice.getFullModel() : mDeviceModel.getDisplay(),
//                    mDevice.getDeviceID());
            String typeSn = mEZDeviceInfo.getDeviceName();
            mDeviceSerialTextView.setText(mEZDeviceInfo.getDeviceSerial());

            mDeviceNameView.setText(TextUtils.isEmpty(typeSn)?"":typeSn);

            mDeviceTypeSnView.setVisibility(View.GONE);

            mDeviceInfoLayout.setOnClickListener(mOnClickListener);
            mDeviceSNLayout.setOnClickListener(mOnClickListener);

            mDefencePlanParentLayout.setVisibility(View.GONE);

            boolean bSupportDefence = true;
            if(bSupportDefence) {
            	mDefenceView.setText(R.string.detail_defend_c1_c2_f1);
                mDefenceStateView.setTextColor(getResources().getColorStateList(R.color.on_off_text_selector));
//                mDefenceStateView.setText(mDevice.isDefenceOn() ? R.string.on : R.string.off);
//                mDefenceStateView.setEnabled(mEZCameraInfo.getDefence() == 1);
                boolean isDefenceEnable = (mEZDeviceInfo.getDefence() != 0);
                mDefenceToggleButton.setBackgroundResource(isDefenceEnable ? R.drawable.autologin_on
                        : R.drawable.autologin_off);
                mDefenceToggleButton.setOnClickListener(mOnClickListener);
            
				mDefenceLayout.setVisibility(View.VISIBLE);
//				mDefenceLayout.setTag(supportMode);
//				mDefenceLayout.setOnClickListener(mOnClickListener); // dont allow to click the list
           }

            // 存储状态部分

            {

                mStorageNoticeView.setVisibility(View.VISIBLE);

                // TODO
                mStorageLayout.setVisibility(View.VISIBLE);
                mStorageLayout.setOnClickListener(mOnClickListener);
            }

            // 版本部分
            if (mEZDeviceInfo.getStatus() == 1 && mDeviceVersion != null) {
                boolean bHasUpgrade = (mDeviceVersion.getIsNeedUpgrade() != 0);
                mCurrentVersionTextView.setText(mDeviceVersion.getCurrentVersion());
                mVersionView.setText(mDeviceVersion.getNewestVersion());
                if (!bHasUpgrade){
                    mVersionNewestView.setVisibility(View.VISIBLE);
                } else {
                    mVersionNewestView.setVisibility(View.GONE);
                }

//                bHasUpgrade = true;// TODO stub
                if(bHasUpgrade) {
                    mVersionNoticeView.setVisibility(View.VISIBLE);
                    mVersionArrowView.setVisibility(View.VISIBLE);
                    mVersionLayout.setOnClickListener(mOnClickListener);
                } else {
                    mVersionNoticeView.setVisibility(View.GONE);
                    mVersionArrowView.setVisibility(View.GONE);
                    mVersionLayout.setOnClickListener(null);
                }
                mVersionLayout.setVisibility(View.VISIBLE);
            } else {
                mVersionLayout.setVisibility(View.GONE);
            }

            // 视频图片加密部分
            boolean bSupportEncrypt = true;
            //if (mDevice.getSupportEncrypt() == DeviceConsts.NOT_SUPPORT || !mDevice.isOnline()) {
            if(!bSupportEncrypt) {
                mEncryptParentLayout.setVisibility(View.GONE);
            } else {
                mEncryptButton
                        .setBackgroundResource((mEZDeviceInfo.getIsEncrypt() == 1) ? R.drawable.autologin_on
                                : R.drawable.autologin_off);
                mEncryptButton.setOnClickListener(mOnClickListener);

                mModifyPasswordLayout.setOnClickListener(mOnClickListener);
                boolean bSupportChangePwd = false;
//                if ((mEZCameraInfo.getEncryptStatus() != 1))
                       // || mDevice.getSupportChangeSafePasswd() == DeviceConsts.NOT_SUPPORT) {
                if(!bSupportChangePwd) {
                    mModifyPasswordLayout.setVisibility(View.GONE);
                } else {
                    mModifyPasswordLayout.setVisibility(View.VISIBLE);
                }

                mEncryptParentLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupParentLayout() {
        // 在线父框架
//        mOnlineParentLayout.setVisibility(mOnlineTimeLayout.getVisibility() & mOfflineNotifyLayout.getVisibility());
    }

    private void setupSafeModePlan(boolean fromServer) {}

    private void setDefencePlanNew(boolean visible) {
    }

    private void setupBaiduDeviceInfo(boolean fromServer, boolean reload) {}

    private void gotoModifyPassword() {
    }

    private void gotoSetSafeMode() {
        if (mEZDeviceInfo.getIsEncrypt() == 1) {
            // 如果开启，则关闭
            if (!isFinishing()) {
                showDialog(SHOW_DIALOG_CLOSE_SAFE_MODE);
            }
        } else {
            // 如果关闭，则开启
//            showDialog(SHOW_DIALOG_SAFE_MODE);
            openSafeMode();
        }
    }

    private void openSafeMode() {
        // 首先判断有没有密码，如果有的话，就不需要设置了
//        if (TextUtils.isEmpty(mDevice.getEncryptPwd()) || mDevice.getEncryptPwd().equals("null")) {
//            Intent intent = new Intent(DeviceSettingActivity.this, DeviceEncryptPasswordActivity.class);
//            intent.putExtra("deviceID", mDevice.getDeviceID());
//            startActivity(intent);
//        } else {
//            new OpenEncryptTask().execute();
//        }
            new OpenEncryptTask().execute(true);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case SHOW_DIALOG_SAFE_MODE: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setNegativeButton(R.string.certain, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openSafeMode();
                    }
                });

                {
                    builder.setMessage(getString(R.string.detail_safe_btn_tip));
                }

                dialog = builder.create();
            }
                break;
            case SHOW_DIALOG_CLOSE_SAFE_MODE: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        openVideoEncryptDialog();
                    }
                });

                builder.setMessage(getString(R.string.detail_safe_close_btn_tip));

                dialog = builder.create();
            	}
                break;

            case SHOW_DIALOG_DEL_DEVICE:
                dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.detail_del_device_btn_tip))
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteDeviceTask().execute();
                            }
                        }).create();
                break;

            case SHOW_DIALOG_OFFLINE_NOTIFY:
                break;
            case SHOW_DIALOG_WEB_SETTINGS_ENCRYPT:
            {
                dialog = new AlertDialog.Builder(this).setMessage(getResources().getString(R.string.option_support_web))
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
            }
            break;
            case SHOW_DIALOG_WEB_SETTINGS_DEFENCE:
            {
                dialog = new AlertDialog.Builder(this).setMessage(getResources().getString(R.string.option_support_web))
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
            }
            break;
            default:
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (dialog != null) {
            removeDialog(id);
//            TextView tv = (TextView) dialog.findViewById(android.R.id.message);
//            tv.setGravity(Gravity.CENTER);
        }
    }

    private void openVideoEncryptDialog() {
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup smsVerifyView = (ViewGroup) mLayoutInflater.inflate(R.layout.device_video_encrypt_dialog, null, true);
        final EditText etSmsCode = (EditText) smsVerifyView.findViewById(R.id.ez_sms_code_et);

        new  AlertDialog.Builder(EZDeviceSettingActivity.this)  
        .setTitle(R.string.input_device_verify_code)
        .setIcon(android.R.drawable.ic_dialog_info)   
        .setView(smsVerifyView)
        .setPositiveButton(R.string.ez_dialog_btn_disable_video_encrypt, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String sms = null;
            	if(etSmsCode != null) {
            		sms = etSmsCode.getEditableText().toString();
            	}
            	mValidateCode = sms;
            	if(!TextUtils.isEmpty(mValidateCode)) {
            		new OpenEncryptTask().execute(false); //disable video encryption
            	}
            }
            
        })   
        .setNegativeButton(R.string.cancel, null)
        .show();  
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(IntentConsts.EXTRA_DEVICE_INFO, mEZDeviceInfo);
        setResult(RESULT_OK, data);
        super.finish();
    }

    private class GetDeviceInfoTask extends AsyncTask<Void, Void, Boolean> {

        private int mErrorCode = 0;

        @Override
        protected Boolean doInBackground(Void... params) {
        	try {
        		mDeviceVersion = EzvizApplication.getOpenSDK().getDeviceVersion(mEZDeviceInfo.getDeviceSerial());
        		return true;
        	} catch (BaseException e) {;
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d(TAG, errorInfo.toString());
        	}
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                setupDeviceInfo();
                setupParentLayout();
            } else {
                switch (mErrorCode) {
                    case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                        break;
                    case ErrorCode.ERROR_WEB_SESSION_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private class DeviceLoginTask extends AsyncTask<Void, Void, Boolean> {

        private Dialog mWaitDialog;

        private int mErrorCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZDeviceSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

//            if (result) {
////                Intent intent = new Intent(DeviceSettingActivity.this, DeviceWifiListActivity.class);
////                intent.putExtra("deviceId", mDevice.getDeviceID());
////                startActivity(intent);
//            } else if (mErrorCode == HCNetSDKException.NET_DVR_PASSWORD_ERROR) {
//                showInputDevicePswDlg();
//            } else {
//                showToast(R.string.device_wifi_set_no_in_subnet);
//            }
        }
    }

    private class SetDefenceTask extends AsyncTask<Boolean, Void, Boolean> {
        private Dialog mWaitDialog;
        private int mErrorCode = 0;
        boolean bSetDefence;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mWaitDialog = new WaitDialog(EZDeviceSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			bSetDefence = (Boolean) params[0];
			Boolean result = false;
			try {
				result = EzvizApplication.getOpenSDK().setDefence(mEZDeviceInfo.getDeviceSerial(), bSetDefence?EZConstants.EZDefenceStatus.EZDefence_IPC_OPEN:
                        EZConstants.EZDefenceStatus.EZDefence_IPC_CLOSE);
			} catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d(TAG, errorInfo.toString());

				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mWaitDialog.dismiss();
			if(result) {
				mEZDeviceInfo.setDefence(bSetDefence ? 1 : 0);
				setupDeviceInfo();
			} else {
				switch (mErrorCode) {
                case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                    showToast(R.string.encrypt_password_open_fail_networkexception);
                    break;
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                    ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                    break;
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                    break;
                default:
                    showToast(R.string.encrypt_password_open_fail, mErrorCode);
                    break;
				}
			}
		}

    }
    private class OpenEncryptTask extends AsyncTask<Boolean, Void, Boolean> {
        private boolean bAction;
        private Dialog mWaitDialog;

        private int mErrorCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZDeviceSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
        	boolean isEnableEncrypt = params[0];
            bAction = isEnableEncrypt;
            try {
            	EzvizApplication.getOpenSDK().setDeviceEncryptStatus(mEZDeviceInfo.getDeviceSerial(),mValidateCode,isEnableEncrypt);

                return true;
            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d(TAG, errorInfo.toString());

                LogUtil.e(TAG, "error description: " + e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

            if (result) {
                showToast(R.string.encrypt_password_open_success);
//                mDevice.setIsEncrypt(1);
//                bAction = !bAction;
                mEZDeviceInfo.setIsEncrypt(bAction ? 1 : 0);
                mEncryptButton.setBackgroundResource(bAction ? R.drawable.autologin_on : R.drawable.autologin_off);
//                if (mDevice.getSupportChangeSafePasswd() != DeviceConsts.NOT_SUPPORT)
//                    mModifyPasswordLayout.setVisibility(View.VISIBLE);
            } else {
                switch (mErrorCode) {
                    case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                        showToast(R.string.encrypt_password_open_fail_networkexception);
                        break;
                    case ErrorCode.ERROR_WEB_SESSION_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    default:
                        showToast(R.string.encrypt_password_open_fail, mErrorCode);
                        break;
                }
            }
        }
    }

    private class DeleteDeviceTask extends AsyncTask<Void, Void, Boolean> {

        private Dialog mWaitDialog;

        private int mErrorCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZDeviceSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!ConnectionDetector.isNetworkAvailable(EZDeviceSettingActivity.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return false;
            }

            try {
//            	EZCameraInfo cameraInfo = params[0];
                EzvizApplication.getOpenSDK().deleteDevice(mEZDeviceInfo.getDeviceSerial());
                return true;
            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d(TAG, errorInfo.toString());

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

            if (result) {
                showToast(R.string.detail_del_device_success);
                Intent intent = new Intent(EZDeviceSettingActivity.this, EZCameraListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                switch (mErrorCode) {
                    case ErrorCode.ERROR_WEB_SESSION_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                        showToast(R.string.alarm_message_del_fail_network_exception);
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_VALICATECODE_ERROR:
                        showToast(R.string.verify_code_error);
                    default:
                        showToast(R.string.alarm_message_del_fail_txt, mErrorCode);
                        break;
                }
            }
        }
    }

}