package com.videogo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ezviz.opensdk.auth.EZAuthAPI;
import com.google.gson.Gson;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;

import java.util.List;

import ezviz.ezopensdk.BuildConfig;
import ezviz.ezopensdk.R;
import ezviz.ezopensdk.demo.SdkInitParams;
import ezviz.ezopensdk.demo.SdkInitTool;
import ezviz.ezopensdk.demo.ServerAreasEnum;
import ezviz.ezopensdk.demo.SpTool;
import ezviz.ezopensdk.demo.ValueKeys;
import ezviz.ezopensdkcommon.common.RootActivity;

import static com.videogo.EzvizApplication.getOpenSDK;
import static com.videogo.constant.Constant.OAUTH_SUCCESS_ACTION;

public class MainActivity extends RootActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private ServerAreasEnum mCurrentServerArea = null;
    private EditText mApiET;
    private EditText mAuthET;
    private EditText mAppKeyET = null;
    private EditText mAccessTokenET = null;
    public static SdkInitParams mInitParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initUI();

        View view = findViewById(R.id.page_container);
        if (view != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(mInitParams.appKey)) {
                        showToast("AppKey is empty!");
                        return;
                    }
                    if (LocalInfo.getInstance().getEZAccesstoken() == null || LocalInfo.getInstance().getEZAccesstoken().getAccessToken() == null) {
                        String tip = "AccessToken is empty!";
                        LogUtil.i(TAG, tip);
//                        showToast(tip);
                        return;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startCheckLoginValidity();
                        }
                    }).start();
                }
            });
        }

        findViewById(R.id.btn_sdk_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EzvizAPI.getInstance().isLogin()) {
                    jumpToCameraListActivity();
                    finish();
                } else {
                    getOpenSDK().openLoginPage();
                }
            }
        });
        findViewById(R.id.btn_ezviz_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EZAuthAPI.isEzvizAppInstalledWithType(MainActivity.this, EZAuthAPI.EZAuthPlatform.EZVIZ)) {
                    /**
                     * ezviz_login
                     */
                    EZAuthAPI.sendAuthReq(MainActivity.this, EZAuthAPI.EZAuthPlatform.EZVIZ);
                } else {
                    Toast.makeText(MainActivity.this, "uninstalled or version is not newest", Toast.LENGTH_LONG).show();
                }
            }
        });
        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * logout
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getOpenSDK().logout();
                    }
                }).start();

            }
        });
        findViewById(R.id.btn_support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SupportActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        SpTool.init(getApplicationContext());
        if (!loadLastSdkInitParams()) {
            LoadDefaultSdkInitParams();
        }
        mInitParams.accessToken = null;
        SdkInitTool.initSdk(getApplication(), mInitParams);
    }

    private boolean loadLastSdkInitParams() {
        String sdkInitParamStr = SpTool.obtainValue(ValueKeys.SDK_INIT_PARAMS);
        if (sdkInitParamStr != null) {
            mInitParams = new Gson().fromJson(sdkInitParamStr, SdkInitParams.class);
            return mInitParams != null && mInitParams.appKey != null;
        }
        return false;
    }

    private void LoadDefaultSdkInitParams() {
        mInitParams = SdkInitParams.createBy(null);
        mInitParams.appKey = "26810f3acd794862b608b6cfbc32a6b8";
        mInitParams.openApiServer = "https://open.ys7.com";
        mInitParams.openAuthApiServer = "https://openauth.ys7.com";
    }

    /**
     * 检查AccessToken是否有效
     */
    private void startCheckLoginValidity() {
        showLoginAnim(true);
        if (checkAppKeyAndAccessToken()) {
            jumpToCameraListActivity();
            finish();
        }
        showLoginAnim(false);
    }

    /**
     * 通过萤石账号进行体验
     */
    public void onClickLoginByEzvizAccount(View view) {
        if (mCurrentServerArea == null || mCurrentServerArea.defaultOpenAuthAppKey == null) {
            toast("Error occurred! Please try to use demo with appKey & accessToken.");
            return;
        }
        mSdkInitParams = SdkInitParams.createBy(mCurrentServerArea);
        mSdkInitParams.openApiServer = getValidText(mApiET.getText().toString());
        mSdkInitParams.openAuthApiServer = getValidText(mAuthET.getText().toString());
        SdkInitTool.initSdk(getApplication(), mSdkInitParams);
        registerLoginResultReceiver();
        getOpenSDK().openLoginPage();
    }

    private SdkInitParams mSdkInitParams = null;
    private BroadcastReceiver mLoginResultReceiver = null;

    private void registerLoginResultReceiver() {
        if (mLoginResultReceiver == null) {
            mLoginResultReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "login success by h5 page");
                    unregisterLoginResultReceiver();
                    saveLastSdkInitParams(mSdkInitParams);
                    jumpToCameraListActivity();
                    finish();
                }
            };
            IntentFilter filter = new IntentFilter(OAUTH_SUCCESS_ACTION);
            registerReceiver(mLoginResultReceiver, filter);
            Log.i(TAG, "registered login result receiver");
        }
    }

    private void unregisterLoginResultReceiver() {
        if (mLoginResultReceiver != null) {
            unregisterReceiver(mLoginResultReceiver);
            mLoginResultReceiver = null;
            Log.i(TAG, "unregistered login result receiver");
        }
    }

    /**
     * 通过AccessToken进行体验
     */
    public void onClickStartExperience(View view) {
        if (checkLoginInfo()) {
            SdkInitParams sdkInitParams = SdkInitParams.createBy(mCurrentServerArea);
            sdkInitParams.openApiServer = getValidText(mApiET.getText().toString());
            sdkInitParams.openAuthApiServer = getValidText(mAuthET.getText().toString());
            sdkInitParams.appKey = getValidText(mAppKeyET.getText().toString());
            sdkInitParams.accessToken = getValidText(mAccessTokenET.getText().toString());
            SdkInitTool.initSdk(getApplication(), sdkInitParams);
//            switchServerToCurrent();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    showLoginAnim(true);
                    if (checkAppKeyAndAccessToken()) {
                        // 保存相关信息
                        SdkInitParams sdkInitParams = SdkInitParams.createBy(mCurrentServerArea);
                        sdkInitParams.appKey = getValidText(mAppKeyET.getText().toString());
                        saveLastSdkInitParams(sdkInitParams);
                        // 跳转到主界面
                        jumpToCameraListActivity();
                    }
                    showLoginAnim(false);
                }
            }).start();

        }
    }

    private String getValidText(String origin) {
        if (origin == null) {
            return null;
        }
        StringBuilder target = new StringBuilder();
        for (char ch : origin.toCharArray()) {
            if (ch == '\0' || ch == '\n' || ch == 160) {
                break;
            } else {
                target.append(ch);
            }
        }
        return target.toString();
    }

    private void jumpToCameraListActivity() {
        Intent toCameraListIntent = new Intent(getApplicationContext(), EZCameraListActivity.class);
        EditText specifiedDeviceEt = (EditText) findViewById(R.id.et_specified_device);
        if (specifiedDeviceEt != null && !TextUtils.isEmpty(specifiedDeviceEt.getText().toString())) {
            toCameraListIntent.putExtra(ValueKeys.DEVICE_SERIAL.name(), specifiedDeviceEt.getText().toString());
        }
        MainActivity.this.startActivity(toCameraListIntent);
        MainActivity.this.finish();
    }

    private ViewGroup mLoginAnimVg = null;
    private boolean isShowLoginAnim = false;

    private void showLoginAnim(final boolean show) {
        if (mLoginAnimVg == null) {
            mLoginAnimVg = (ViewGroup) findViewById(R.id.vg_login_anim);
        }
        if (mLoginAnimVg == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isShowLoginAnim = show;
                if (show) {
                    mLoginAnimVg.setVisibility(View.VISIBLE);
                } else {
                    mLoginAnimVg.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    /**
     * 获取上次sdk初始化的参数
     */
    private SdkInitParams getLastSdkInitParams() {
        String lastSdkInitParamsStr = SpTool.obtainValue(ValueKeys.SDK_INIT_PARAMS);
        if (lastSdkInitParamsStr == null) {
            return null;
        } else {
            return new Gson().fromJson(lastSdkInitParamsStr, SdkInitParams.class);
        }
    }

    /**
     * 保存上次sdk初始化的参数
     */
    private void saveLastSdkInitParams(SdkInitParams sdkInitParams) {
        // 不保存AccessToken
        sdkInitParams.accessToken = null;
        SpTool.storeValue(ValueKeys.SDK_INIT_PARAMS, sdkInitParams.toString());
    }

    /**
     * 通过调用服务接口判断AppKey和AccessToken且有效
     *
     * @return 是否依旧有效
     */
    private boolean checkAppKeyAndAccessToken() {
        boolean isValid = false;
        try {
            EzvizApplication.getOpenSDK().getDeviceList(0, 1);
            isValid = true;
        } catch (BaseException e) {
            e.printStackTrace();
            Log.e(TAG, "Error code is " + e.getErrorCode());
            int errCode = e.getErrorCode();
            String errMsg;
            switch (errCode) {
                case 400031:
                    errMsg = getApplicationContext().getString(R.string.tip_of_bad_net);
                    break;
                default:
                    errMsg = getApplicationContext().getString(R.string.login_expire);
                    break;
            }
            toast(errMsg);
        }
        return isValid;
    }

    private void switchServerToCurrent() {
        EzvizAPI.getInstance().setServerUrl(mCurrentServerArea.openApiServer, mCurrentServerArea.openAuthApiServer);
        Log.e(TAG, "switched server area!!!\n" + mCurrentServerArea.toString());
    }

    private boolean checkLoginInfo() {
        if (mAppKeyET.getText().toString().equals("")) {
            toast("AppKey不能为空");
            return false;
        }
        if (mAccessTokenET.getText().toString().equals("")) {
            toast("AccessToken不能为空");
            return false;
        }
        return true;
    }

    private void initUI() {
        // 设置服务器区域下拉框显示和监听
        Spinner areaServerSp = (Spinner) findViewById(R.id.sp_server_area);
        if (areaServerSp != null) {
            ServerAreasSpAdapter adapter = new ServerAreasSpAdapter(getApplicationContext(), ServerAreasEnum.getAllServers());
            areaServerSp.setAdapter(adapter);
            areaServerSp.setOnItemSelectedListener(mServerAreasOnItemCLickLister);
        }

        mAppKeyET = (EditText) findViewById(R.id.et_app_key);
        mAccessTokenET = (EditText) findViewById(R.id.et_access_token);
        mApiET = findViewById(R.id.et_api_url);
        mAuthET = findViewById(R.id.et_auth_server);

        SdkInitParams sdkInitParams = getLastSdkInitParams();
        if (sdkInitParams != null) {
            mAppKeyET.setText(sdkInitParams.appKey);
            try {
                mAccessTokenET.setText(EZOpenSDK.getInstance().getEZAccessToken().getAccessToken());
            } catch (Exception e) {
                LogUtil.d(TAG, "failed to load AccessToken");
            }
            for (int position = 0; position < ServerAreasEnum.getAllServers().size(); position++) {
                if (sdkInitParams.serverAreaId == ServerAreasEnum.getAllServers().get(position).id) {
                    if (areaServerSp != null) {
                        areaServerSp.setSelection(position);
                    }
                }
            }
        }

        TextView sdkVerTv = (TextView) findViewById(R.id.tv_sdk_ver);
        if (sdkVerTv != null) {
            sdkVerTv.setText("SDK Version: " + BuildConfig.VERSION_NAME);
        }
    }

    private AdapterView.OnItemSelectedListener mServerAreasOnItemCLickLister = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mCurrentServerArea = ServerAreasEnum.getAllServers().get(position);
            mApiET.setText(mCurrentServerArea.openApiServer);
            mAuthET.setText(mCurrentServerArea.openAuthApiServer);

            // http://canal.hikvision.com.cn/browse/BUGOL-11702
//            mAppKeyET.setText("26810f3acd794862b608b6cfbc32a6b8");
//            mAccessTokenET.setText("at.77ytj1783a7bajs0a09u0a0u64tiolca-26k51q60pv-1sb9ipj-kxcdnnmap");
            // 李海涛appKey
            mAppKeyET.setText("680948cc41c44fbaac23d8b47be4028b");
            mAccessTokenET.setText("at.2xwvzh454swdqgj26jscn4cl6o71kd3r-77cacr5ymf-1befzkz-psipk4zq7");
            // 温氏appKey
//            mAppKeyET.setText("5aeb97218dc24acd8003c8dd6f8055c0");
//            mAccessTokenET.setText("at.9irsiwajbgyt3ka11gbxp1nf8shq0h67-9ri4e1i5i1-08f5vpv-yezskuhfu");
            // 程骏appKey
            mAppKeyET.setText("fd82f9a6f0154aa2aa9284ae7af25a5b");
            mAccessTokenET.setText("");

            // 仅预置了appKey的区域才展示萤石账号登录按钮
            if (mCurrentServerArea.defaultOpenAuthAppKey != null) {
                showEzvizAccountLoginTv(true);
            } else {
                showEzvizAccountLoginTv(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            showEzvizAccountLoginTv(false);
        }

        private void showEzvizAccountLoginTv(boolean show) {
            View loginTv = MainActivity.this.findViewById(R.id.tv_ezviz_account_login);
            if (loginTv == null) {
                return;
            }
            if (show) {
                loginTv.setVisibility(View.VISIBLE);
            } else {
                loginTv.setVisibility(View.INVISIBLE);
            }
        }

    };

    private class ServerAreasSpAdapter implements SpinnerAdapter {

        private Context mContext;
        private List<ServerAreasEnum> mServerAreaArray;

        public ServerAreasSpAdapter(@NonNull Context context, @NonNull List<ServerAreasEnum> serverList) {
            mContext = context;
            mServerAreaArray = serverList;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getDropDownServerAreaItemView(position, convertView);
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            int cnt = 0;
            if (mServerAreaArray != null) {
                cnt = mServerAreaArray.size();
            }
            return cnt;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getServerAreaItemView(position, convertView);
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        private View getServerAreaItemView(int position, View convertView) {
            TextView itemTv = (TextView) convertView;
            if (itemTv == null) {
                itemTv = new TextView(mContext);
                itemTv.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                // dp转px(applyDimension的用途是根据当前数值单位和屏幕像素密度将指定数值转换为Android标准尺寸单位px)
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mContext.getResources().getDisplayMetrics());
                itemTv.setHeight(height);
                itemTv.setGravity(Gravity.CENTER_VERTICAL);
            }
            itemTv.setText(mServerAreaArray.get(position).areaName);
            return itemTv;
        }

        private View getDropDownServerAreaItemView(int position, View convertView) {
            TextView itemTv = (TextView) getServerAreaItemView(position, convertView);
            itemTv.setGravity(Gravity.CENTER);
            return itemTv;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterLoginResultReceiver();
    }

    @Override
    public void onBackPressed() {
        if (isShowLoginAnim) {
            toast(getApplicationContext().getString(R.string.cancel_init_sdk));
            showLoginAnim(false);
        } else {
            super.onBackPressed();
        }
    }
}
