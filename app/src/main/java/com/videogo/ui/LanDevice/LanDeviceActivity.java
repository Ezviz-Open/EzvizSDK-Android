package com.videogo.ui.LanDevice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.ezviz.hcnetsdk.EZLoginDeviceInfo;
import com.ezviz.hcnetsdk.EZSADPDeviceInfo;
import com.google.gson.Gson;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZHCNetDeviceSDK;
import com.videogo.util.LogUtil;
import ezviz.ezopensdk.R;
import java.util.ArrayList;

public class LanDeviceActivity extends Activity {
    private static final String TAG = "LanDeviceActivity";

    private ListView mListView;
    private ArrayList<EZSADPDeviceInfo> mArrayList = new ArrayList<EZSADPDeviceInfo>();

    private LandeviceAdapter mLandeviceAdapter;
    protected static int REQUEST_ACTIVATE = 0x0001;

    private AlertDialog mLoginDialog;

    private int mCurrentPosition;

    private EZLoginDeviceInfo mEZloginDeviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc);
        mListView = (ListView) findViewById(R.id.list_device);
        EZHCNetDeviceSDK.getInstance().startLocalSearch(new EZHCNetDeviceSDK.SadpDeviceFoundListener() {
            @Override
            public void onDeviceFound(final EZSADPDeviceInfo sadp_device_info) {
                LogUtil.d(TAG,
                    "onDeviceFound  " + sadp_device_info.getDeviceSerial() + "  " + sadp_device_info.getDeviceSerial());
                Gson gson = new Gson();
                LogUtil.d(TAG, "onDeviceFound  " + gson.toJson(sadp_device_info));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < mArrayList.size(); i++) {
                            String serial = sadp_device_info.getDeviceSerial();
                            String oldserial = mArrayList.get(i).getDeviceSerial();
                            if (serial.equals(oldserial)) {
                                return;
                            }
                        }
                        mLandeviceAdapter.add(sadp_device_info);
                    }
                });
            }
        });

        mLandeviceAdapter = new LandeviceAdapter(this, mArrayList);
        mListView.setAdapter(mLandeviceAdapter);
        mLandeviceAdapter.setOnItemClickListener(new LandeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mCurrentPosition = position;
                if (!mLandeviceAdapter.getItem(mCurrentPosition).isActived()) {
                    Intent intent = new Intent(LanDeviceActivity.this, LanDeviceActivateActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mLandeviceAdapter.getItem(mCurrentPosition).getDeviceSerial());
                    startActivityForResult(intent, REQUEST_ACTIVATE);
                } else {
                    showLoginDialog(mCurrentPosition);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        EZHCNetDeviceSDK.getInstance().stopLocalSearch();
        super.onDestroy();
    }

    private void showNotSupportViewDailog() {
        new AlertDialog.Builder(this).setMessage(R.string.device_not_support_view)
            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ACTIVATE && resultCode == RESULT_OK) {
            mLandeviceAdapter.getItem(mCurrentPosition).setActived(true);
            showLoginDialog(mCurrentPosition);
        }
    }

    private void toPlayActivity(final EZLoginDeviceInfo loginDeviceInfo) {
        if (loginDeviceInfo == null || loginDeviceInfo.getLoginId() < 0) {
            if (!mLandeviceAdapter.getItem(mCurrentPosition).isActived()) {
                Intent intent = new Intent(LanDeviceActivity.this, LanDeviceActivateActivity.class);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mLandeviceAdapter.getItem(mCurrentPosition).getDeviceSerial());
                startActivityForResult(intent, REQUEST_ACTIVATE);
            } else {
                showLoginDialog(mCurrentPosition);
            }
            return;
        }
        if (loginDeviceInfo.getByChanNum() + loginDeviceInfo.getByIPChanNum() > 1) {
            SelectLandeviceDialog selectLandeviceDialog = new SelectLandeviceDialog();
            selectLandeviceDialog.setLoginDeviceInfo(loginDeviceInfo);
            selectLandeviceDialog.setCameraItemClick(new SelectLandeviceDialog.CameraItemClick() {
                @Override
                public void onCameraItemClick(int playChannelNo) {
                    Intent intent = new Intent(LanDeviceActivity.this, LanDevicePlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mLandeviceAdapter.getItem(mCurrentPosition).getDeviceSerial());
                    intent.putExtra(IntentConsts.EXTRA_CHANNEL_NO, playChannelNo);
                    intent.putExtra("iUserId", loginDeviceInfo.getLoginId());
                    startActivity(intent);
                }
            });
            selectLandeviceDialog.show(getFragmentManager(), "onLanPlayClick");

            //Single channel // no channel
        } else if (loginDeviceInfo.getByChanNum() + loginDeviceInfo.getByIPChanNum() == 1) {
            if (loginDeviceInfo.getByChanNum() > 0) {
                Intent intent = new Intent(LanDeviceActivity.this, LanDevicePlayActivity.class);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mLandeviceAdapter.getItem(mCurrentPosition).getDeviceSerial());
                intent.putExtra(IntentConsts.EXTRA_CHANNEL_NO, loginDeviceInfo.getByStartChan());
                intent.putExtra("iUserId", loginDeviceInfo.getLoginId());
                startActivity(intent);
            } else {
                Intent intent = new Intent(LanDeviceActivity.this, LanDevicePlayActivity.class);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mLandeviceAdapter.getItem(mCurrentPosition).getDeviceSerial());
                intent.putExtra(IntentConsts.EXTRA_CHANNEL_NO, loginDeviceInfo.getByStartDChan());
                intent.putExtra("iUserId", loginDeviceInfo.getLoginId());
                startActivity(intent);
            }
        } else {
            showNotSupportViewDailog();
        }
    }

    public void showLoginDialog(final int position) {
        if (mLoginDialog != null && mLoginDialog.isShowing()) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        View safeview = inflater.inflate(R.layout.lan_device_login_dialog, null);
        final EditText loginName = (EditText) safeview.findViewById(R.id.login_name);
        final EditText loginPwd = (EditText) safeview.findViewById(R.id.login_pwd);
        mLoginDialog = new AlertDialog.Builder(this).setTitle(R.string.lan_device_login_title)
            .setView(safeview)
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            })
            .setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String name = loginName.getText().toString();
                    final String pwd = loginPwd.getText().toString();
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pwd)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final EZLoginDeviceInfo ezLoginDeviceInfo = EZHCNetDeviceSDK.getInstance()
                                        .loginDeviceWithUerName(name, pwd, mLandeviceAdapter.getItem(mCurrentPosition).getLocalIp(),
                                            mLandeviceAdapter.getItem(mCurrentPosition).getLocalPort());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onLoginSuccess(ezLoginDeviceInfo);
                                        }
                                    });
                                } catch (final BaseException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onLoaginFailed(e.getErrorCode(), e.getMessage());
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(LanDeviceActivity.this, "username or passwor is null", Toast.LENGTH_LONG).show();
                        showLoginDialog(mCurrentPosition);
                    }
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                }
            })
            .setCancelable(false)
            .create();
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(loginName.getText().toString().trim()) && !TextUtils.isEmpty(
                    loginPwd.getText().toString().trim())) {
                    mLoginDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    mLoginDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        };
        loginName.addTextChangedListener(textWatcher);
        loginPwd.addTextChangedListener(textWatcher);
        mLoginDialog.show();
    }

    public void onLoginSuccess(EZLoginDeviceInfo ezLoginDeviceInfo) {
        toPlayActivity(ezLoginDeviceInfo);
    }

    public void onLoaginFailed(int errorCode, String errorMsg) {
        int ERROR_NET_DVR_PASSWORD_ERROR =
            1; //username or password is incorrect. The user name or password entered when registering is incorrect.
        int ERROR_NET_DVR_PASSWORD_ERROR2 =
            1100; //username or password is incorrect. The user name or password entered when registering is incorrect.
        if (errorCode == ERROR_NET_DVR_PASSWORD_ERROR || errorCode == ERROR_NET_DVR_PASSWORD_ERROR2) {
            new AlertDialog.Builder(this).setMessage(R.string.hc_net_account_pwd_error)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showLoginDialog(mCurrentPosition);
                    }
                })
                .show();
        } else {
            new AlertDialog.Builder(this).setMessage(getString(R.string.hc_net_error) + errorCode)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
        }
    }
}
