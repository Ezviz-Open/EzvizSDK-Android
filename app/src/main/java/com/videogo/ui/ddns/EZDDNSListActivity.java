/* 
 * @ProjectName VideoGoJar
 * @Copyright null
 * 
 * @FileName CameraListActivity.java
 * @Description 这里对文件进行描述
 * 
 * @author xia xingsuo
 * @data 2015-11-5
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.ddns;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.ezviz.hcnetsdk.EZLoginDeviceInfo;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.bean.EZHiddnsDeviceInfo;
import com.videogo.openapi.EZHCNetDeviceSDK;
import com.videogo.scan.main.CaptureActivity;
import com.videogo.ui.LanDevice.LanDevicePlayActivity;
import com.videogo.ui.LanDevice.SelectLandeviceDialog;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshFooter.Style;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.WaitDialog;
import com.videogo.widget.pulltorefresh.IPullToRefresh.Mode;
import com.videogo.widget.pulltorefresh.IPullToRefresh.OnRefreshListener;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.LoadingLayoutCreator;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.Orientation;
import com.videogo.widget.pulltorefresh.PullToRefreshListView;
import ezviz.ezopensdk.R;
import java.util.Date;
import java.util.List;

import static com.videogo.EzvizApplication.getOpenSDK;

public class EZDDNSListActivity extends Activity implements OnClickListener{
    protected static final String TAG = "CameraListActivity";
    public final static int REQUEST_CODE = 100;
    public final static int RESULT_CODE = 101;
    private final static int SHOW_DIALOG_DEL_DEVICE = 1;

    //private EzvizAPI mEzvizAPI = null;
    private BroadcastReceiver mReceiver = null;

    private PullToRefreshListView mListView = null;
    private View mNoMoreView;
    private EZDDNSListAdapter mAdapter = null;

    private LinearLayout mNoCameraTipLy = null;
    private LinearLayout mGetCameraFailTipLy = null;
    private TextView mCameraFailTipTv = null;
    private Button mAddBtn;
    private Button mUserBtn;
    private TextView mMyDevice;
    private TextView mShareDevice;
    private Button mPushCheckBtn;

    private boolean bIsFromSetting = false;

    public final static int TAG_CLICK_PLAY = 1;
    public final static int TAG_CLICK_REMOTE_PLAY_BACK = 2;
    public final static int TAG_CLICK_SET_DEVICE = 3;
    public final static int TAG_CLICK_ALARM_LIST = 4;

    private int mClickType;

    private final static int LOAD_MY_DEVICE = 0;
    private final static int LOAD_SHARE_DEVICE = 1;
    private int mLoadType = LOAD_MY_DEVICE;

    private Handler mHandler = new Handler();

    private AlertDialog mLoginDialog;

    private int mCurrentSelectPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ddns_device_ist_page);
        initData();
        initView();
        Utils.clearAllNotification(this);
    }

    private void initView() {
        mMyDevice = (TextView) findViewById(R.id.text_my);
        mShareDevice = (TextView) findViewById(R.id.text_share);
        mAddBtn = (Button) findViewById(R.id.btn_add);
        mUserBtn = (Button) findViewById(R.id.btn_user);
        mUserBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popLogoutDialog();
            }
        });

        mAddBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EZDDNSListActivity.this, CaptureActivity.class);
                startActivity(intent);
            }
        });

        mShareDevice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareDevice.setTextColor(getResources().getColor(R.color.orange_text));
                mMyDevice.setTextColor(getResources().getColor(R.color.black_text));
                mAdapter.clearAll();
                mLoadType = LOAD_SHARE_DEVICE;
                getDDNSDeviceListInfoList(true);
            }
        });

        mMyDevice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareDevice.setTextColor(getResources().getColor(R.color.black_text));
                mMyDevice.setTextColor(getResources().getColor(R.color.orange_text));
                mAdapter.clearAll();
                mLoadType = LOAD_MY_DEVICE;
                getDDNSDeviceListInfoList(true);
            }
        });
        mNoMoreView = getLayoutInflater().inflate(R.layout.no_device_more_footer, null);
        mAdapter = new EZDDNSListAdapter(this);
        mListView = (PullToRefreshListView) findViewById(R.id.camera_listview);
        mListView.setLoadingLayoutCreator(new LoadingLayoutCreator() {

            @Override
            public LoadingLayout create(Context context, boolean headerOrFooter, Orientation orientation) {
                if (headerOrFooter)
                    return new PullToRefreshHeader(context);
                else
                    return new PullToRefreshFooter(context, Style.EMPTY_NO_MORE);
            }
        });
        mListView.setMode(Mode.BOTH);
        mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView, boolean headerOrFooter) {
                getDDNSDeviceListInfoList(headerOrFooter);
            }
        });
        mListView.getRefreshableView().addFooterView(mNoMoreView);
        mAdapter.setOnItemClickListener(new EZDDNSListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick( int position) {
                mCurrentSelectPosition = position;
                showLoginDialog(mAdapter.getItem(position));
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.getRefreshableView().removeFooterView(mNoMoreView);

        mNoCameraTipLy = (LinearLayout) findViewById(R.id.no_camera_tip_ly);
        mGetCameraFailTipLy = (LinearLayout) findViewById(R.id.get_camera_fail_tip_ly);
        mCameraFailTipTv = (TextView) findViewById(R.id.get_camera_list_fail_tv);
    }

    private void initData() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                LogUtil.d(TAG, "onReceive:" + action);
                if (action.equals(Constant.ADD_DEVICE_SUCCESS_ACTION)) {
                    refreshButtonClicked();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ADD_DEVICE_SUCCESS_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bIsFromSetting || (mAdapter != null && mAdapter.getCount() == 0)) {
            refreshButtonClicked();
            bIsFromSetting = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void getDDNSDeviceListInfoList(boolean headerOrFooter) {
        if (this.isFinishing()) {
            return;
        }
        new GetDDNSDeviceListInfoListTask(headerOrFooter).execute();


    }

    private class GetDDNSDeviceListInfoListTask extends AsyncTask<Void, Void, List<EZHiddnsDeviceInfo>> {
        private boolean mHeaderOrFooter;
        private int mErrorCode = 0;

        public GetDDNSDeviceListInfoListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mListView.setFooterRefreshEnabled(true);
            if (mHeaderOrFooter) {
                mListView.setVisibility(View.VISIBLE);
                mNoCameraTipLy.setVisibility(View.GONE);
                mGetCameraFailTipLy.setVisibility(View.GONE);
            }
            mListView.getRefreshableView().removeFooterView(mNoMoreView);
        }

        @Override
        protected List<EZHiddnsDeviceInfo> doInBackground(Void... params) {
            if (EZDDNSListActivity.this.isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(EZDDNSListActivity.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }

            if (EZGlobalSDK.getInstance() != null) {
                try {
                    List<EZHiddnsDeviceInfo> result = null;
                    if (mLoadType == LOAD_MY_DEVICE) {
                        if (mHeaderOrFooter) {
                            result = EZGlobalSDK.getInstance().getDDNSDeviceList(20, 0);
                        } else {
                            result = EZGlobalSDK.getInstance().getDDNSDeviceList(20,
                                (mAdapter.getCount() / 20) + (mAdapter.getCount() % 20 > 0 ? 1 : 0));
                        }
                    } else if (mLoadType == LOAD_SHARE_DEVICE) {
                        if (mHeaderOrFooter) {
                            result = EZGlobalSDK.getInstance().getShareDDNSDeviceList(20, 0);
                        } else {
                            result = EZGlobalSDK.getInstance().getShareDDNSDeviceList(20,
                                (mAdapter.getCount() / 20) + (mAdapter.getCount() % 20 > 0 ? 1 : 0));
                        }
                    }

                    return result;
                } catch (BaseException e) {
                    ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                    mErrorCode = errorInfo.errorCode;
                    LogUtil.d(TAG, errorInfo.toString());

                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<EZHiddnsDeviceInfo> result) {
            super.onPostExecute(result);
            mListView.onRefreshComplete();
            if (EZDDNSListActivity.this.isFinishing()) {
                return;
            }

            if (result != null) {
                if (mHeaderOrFooter) {
                    CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
                    for (LoadingLayout layout : mListView.getLoadingLayoutProxy(true, false).getLayouts()) {
                        ((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
                    }
                    mAdapter.clearItem();
                }
                if (mAdapter.getCount() == 0 && result.size() == 0) {
                    mListView.setVisibility(View.GONE);
                    mNoCameraTipLy.setVisibility(View.VISIBLE);
                    mGetCameraFailTipLy.setVisibility(View.GONE);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                } else if (result.size() < 10) {
                    mListView.setFooterRefreshEnabled(false);
                    mListView.getRefreshableView().addFooterView(mNoMoreView);
                } else if (mHeaderOrFooter) {
                    mListView.setFooterRefreshEnabled(true);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }
                addCameraList(result);
                mAdapter.notifyDataSetChanged();
            }

            if (mErrorCode != 0) {
                onError(mErrorCode);
            }
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                    ActivityUtils.handleSessionException(EZDDNSListActivity.this);
                    break;
                default:
                    if (mAdapter.getCount() == 0) {
                        mListView.setVisibility(View.GONE);
                        mNoCameraTipLy.setVisibility(View.GONE);
                        mCameraFailTipTv.setText(Utils.getErrorTip(EZDDNSListActivity.this, R.string.get_camera_list_fail, errorCode));
                        mGetCameraFailTipLy.setVisibility(View.VISIBLE);
                    } else {
                        Utils.showToast(EZDDNSListActivity.this, R.string.get_camera_list_fail, errorCode);
                    }
                    break;
            }
        }
    }

    private void addCameraList(List<EZHiddnsDeviceInfo> result) {
        int count = result.size();
        EZHiddnsDeviceInfo item = null;
        for (int i = 0; i < count; i++) {
            item = result.get(i);
            mAdapter.addItem(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_list_refresh_btn:
            case R.id.no_camera_tip_ly:
                refreshButtonClicked();
                break;
            default:
                break;
        }
    }

    private void refreshButtonClicked() {
        mListView.setVisibility(View.VISIBLE);
        mNoCameraTipLy.setVisibility(View.GONE);
        mGetCameraFailTipLy.setVisibility(View.GONE);
        mListView.setMode(Mode.BOTH);
        mListView.setRefreshing();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case SHOW_DIALOG_DEL_DEVICE:
                break;
        }
        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.update_exit).setIcon(R.drawable.exit_selector);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (dialog != null) {
            removeDialog(id);
            TextView tv = (TextView) dialog.findViewById(android.R.id.message);
            tv.setGravity(Gravity.CENTER);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {// 得到被点击的item的itemId
            case 1:// 对应的ID就是在add方法中所设定的Id
                popLogoutDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void popLogoutDialog() {
        Builder exitDialog = new Builder(EZDDNSListActivity.this);
        exitDialog.setTitle(R.string.exit);
        exitDialog.setMessage(R.string.exit_tip);
        exitDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LogoutTask().execute();
            }
        });
        exitDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        exitDialog.show();
    }

    private class LogoutTask extends AsyncTask<Void, Void, Void> {
        private Dialog mWaitDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZDDNSListActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getOpenSDK().logout();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();
            ActivityUtils.goToLoginAgain(EZDDNSListActivity.this);
            finish();
        }
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
                        showLoginDialog(mAdapter.getItem(mCurrentSelectPosition));
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
    private void toPlayActivity(final EZLoginDeviceInfo ezLoginDeviceInfo){
        if (ezLoginDeviceInfo.getLoginId() >= 0) {
            //multi-channel
            if (ezLoginDeviceInfo.getByChanNum() + ezLoginDeviceInfo.getByIPChanNum() > 1) {
                SelectLandeviceDialog selectLandeviceDialog = new SelectLandeviceDialog();
                selectLandeviceDialog.setLoginDeviceInfo(ezLoginDeviceInfo);
                selectLandeviceDialog.setCameraItemClick(new SelectLandeviceDialog.CameraItemClick() {
                    @Override
                    public void onCameraItemClick(int playChannelNo) {
                        Intent intent = new Intent(EZDDNSListActivity.this, LanDevicePlayActivity.class);
                        intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mAdapter.getItem(mCurrentSelectPosition).getSubSerial());
                        intent.putExtra(IntentConsts.EXTRA_CHANNEL_NO, playChannelNo);
                        intent.putExtra("iUserId", ezLoginDeviceInfo.getLoginId());
                        startActivity(intent);
                    }
                });
                selectLandeviceDialog.show(getFragmentManager(), "onLanPlayClick");

                //Single channel // no channel
            } else if (ezLoginDeviceInfo.getByChanNum() + ezLoginDeviceInfo.getByIPChanNum() == 1) {
                if (ezLoginDeviceInfo.getByChanNum() > 0) {
                    Intent intent = new Intent(EZDDNSListActivity.this, LanDevicePlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mAdapter.getItem(mCurrentSelectPosition).getSubSerial());
                    intent.putExtra(IntentConsts.EXTRA_CHANNEL_NO, ezLoginDeviceInfo.getByStartChan());
                    intent.putExtra("iUserId", ezLoginDeviceInfo.getLoginId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(EZDDNSListActivity.this, LanDevicePlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, mAdapter.getItem(mCurrentSelectPosition).getSubSerial());
                    intent.putExtra(IntentConsts.EXTRA_CHANNEL_NO, ezLoginDeviceInfo.getByStartDChan());
                    intent.putExtra("iUserId", ezLoginDeviceInfo.getLoginId());
                    startActivity(intent);
                }
            } else {
                showNotSupportViewDailog();
            }
        }
    }

    private void showNotSupportViewDailog() {
        new AlertDialog.Builder(this).setMessage(R.string.device_not_support_view)
            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
    }
    public void showLoginDialog(final EZHiddnsDeviceInfo hiddnsDeviceInfo) {
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
                                    final EZLoginDeviceInfo ezLoginDeviceInfo = EZHCNetDeviceSDK.getInstance().loginDeviceWithUerName(name, pwd, hiddnsDeviceInfo.getDeviceIp(),hiddnsDeviceInfo.getUpnpMappingMode() == 0?hiddnsDeviceInfo.getHiddnsCmdPort():hiddnsDeviceInfo.getMappingHiddnsCmdPort());
                                    if (ezLoginDeviceInfo != null) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                onLoginSuccess(ezLoginDeviceInfo);
                                            }
                                        });
                                    }else{
                                        onLoaginFailed(-1,"login fail");
                                    }
                                } catch (final BaseException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onLoaginFailed(e.getErrorCode(),e.getMessage());
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(EZDDNSListActivity.this,"username or passwor is null",Toast.LENGTH_LONG).show();
                        showLoginDialog(hiddnsDeviceInfo);
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
}
