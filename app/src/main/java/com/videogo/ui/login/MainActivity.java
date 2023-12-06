package com.videogo.ui.login;

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

import com.google.gson.Gson;
import com.videogo.EzvizApplication;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;

import java.util.List;

import ezviz.ezopensdk.BuildConfig;
import ezviz.ezopensdk.R;

import com.videogo.util.SpTool;
import com.videogo.global.ValueKeys;
import ezviz.ezopensdkcommon.common.RootActivity;
import static com.videogo.EzvizApplication.getOpenSDK;
import static com.videogo.constant.Constant.OAUTH_SUCCESS_ACTION;
import static com.videogo.ui.login.ServerAreasEnum.ASIA_CHINA;

public class MainActivity extends RootActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private ServerAreasEnum mCurrentServerArea;
    private EditText mApiET;
    private EditText mAuthET;
    private EditText mAppKeyET;
    private EditText mAccessTokenET;
    private EditText mSpecifiedDeviceET;
    // token登录参数
    public static SdkInitParams mInitParams;
    // 萤石云账号登录参数
    private SdkInitParams mSdkInitParams;
    // 萤石账号登录成功回调广播
    private BroadcastReceiver mLoginResultReceiver;
    
    // JuneCheng's AppKey
    private final static String APPKEY_JC = "fd82f9a6f0154aa2aa9284ae7af25a5b";
    private final static String TOKEN_JC = "at.6tse7wq5b6xm8oa0arsmia2k3wiuublv-3617saq39l-0c51ty8-gb0z1efmn";
    // 开发者账号信息
    private final static String APPKEY_DEV = "26810f3acd794862b608b6cfbc32a6b8";
    private final static String TOKEN_DEV = "at.a4jghxw474bh6e1s18kvzhz25d733toq-7p38y4ttfh-0wrsfvh-fyffgyprx";
    private final static String DEVICE_DEV = "BA7248908";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initUI();
        autoLogin();

        mAppKeyET.setText(APPKEY_DEV);
        mAccessTokenET.setText(TOKEN_DEV);
        mSpecifiedDeviceET.setText(DEVICE_DEV);
    }

    /**
     * 通过AccessToken进行体验
     */
    public void onClickStartExperience(View view) {
        if (checkLoginInfo()) {
            SdkInitParams sdkInitParams = SdkInitParams.createBy(mCurrentServerArea);
            sdkInitParams.appKey = getValidText(mAppKeyET.getText().toString());
            sdkInitParams.accessToken = getValidText(mAccessTokenET.getText().toString());
            sdkInitParams.openApiServer = getValidText(mApiET.getText().toString());
            sdkInitParams.openAuthApiServer = getValidText(mAuthET.getText().toString());
            sdkInitParams.specifiedDevice = getValidText(mSpecifiedDeviceET.getText().toString());
            SdkInitTool.initSdk(getApplication(), sdkInitParams);
            new Thread(() -> {
                showLoginAnim(true);
                if (checkAppKeyAndAccessToken()) {
                    // 保存相关信息
                    saveLastSdkInitParams(sdkInitParams);
                    jumpToCameraListActivity();
                }
                showLoginAnim(false);
            }).start();
        }
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

    public void jcTestClick(View view) {
        mSpecifiedDeviceET.setText("");
        SdkInitParams sdkInitParams = SdkInitParams.createBy(ASIA_CHINA);
        sdkInitParams.appKey = APPKEY_JC;
        sdkInitParams.accessToken = TOKEN_JC;
        sdkInitParams.specifiedDevice = "";
        SdkInitTool.initSdk(getApplication(), sdkInitParams);
        new Thread(() -> {
            showLoginAnim(true);
            if (checkAppKeyAndAccessToken()) {
                // 保存相关信息
                saveLastSdkInitParams(sdkInitParams);
                jumpToCameraListActivity();
            }
            showLoginAnim(false);
        }).start();
    }

    /**
     * 跳转设备列表页面
     */
    private void jumpToCameraListActivity() {
        Intent toCameraListIntent = new Intent(getApplicationContext(), EZCameraListActivity.class);
        startActivity(toCameraListIntent);
        finish();
    }

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

    private void initData() {
        SpTool.init(getApplicationContext());
        if (!loadLastSdkInitParams()) {
            loadDefaultSdkInitParams();
        }
        mInitParams.accessToken = null;
        SdkInitTool.initSdk(getApplication(), mInitParams);
    }

    /**
     * 是否能获取到之前的初始化参数
     *
     * @return 是否能获取到之前的初始化参数
     */
    private boolean loadLastSdkInitParams() {
        String sdkInitParamStr = SpTool.obtainValue(ValueKeys.SDK_INIT_PARAMS);
        if (sdkInitParamStr != null) {
            mInitParams = new Gson().fromJson(sdkInitParamStr, SdkInitParams.class);
            return mInitParams != null && mInitParams.appKey != null;
        }
        return false;
    }

    /**
     * 设置默认参数
     */
    private void loadDefaultSdkInitParams() {
        mInitParams = SdkInitParams.createBy(null);
        mInitParams.appKey = "fd82f9a6f0154aa2aa9284ae7af25a5b";
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


    private ViewGroup mLoginAnimVg = null;
    private boolean isShowLoginAnim = false;

    private void showLoginAnim(final boolean show) {
        if (mLoginAnimVg == null) {
            mLoginAnimVg = findViewById(R.id.vg_login_anim);
        }
        if (mLoginAnimVg == null) {
            return;
        }
        runOnUiThread(() -> {
            isShowLoginAnim = show;
            mLoginAnimVg.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        });
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

    /**
     * 验证是否输入 AppKey 和 AccessToken
     */
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
        Spinner areaServerSp = findViewById(R.id.sp_server_area);
        if (areaServerSp != null) {
            ServerAreasSpAdapter adapter = new ServerAreasSpAdapter(getApplicationContext(), ServerAreasEnum.getAllServers());
            areaServerSp.setAdapter(adapter);
            areaServerSp.setOnItemSelectedListener(mServerAreasOnItemCLickLister);
        }
        mAppKeyET = findViewById(R.id.et_app_key);
        mAccessTokenET = findViewById(R.id.et_access_token);
        mApiET = findViewById(R.id.et_api_url);
        mAuthET = findViewById(R.id.et_auth_server);
        mSpecifiedDeviceET = findViewById(R.id.et_specified_device);

        SdkInitParams sdkInitParams = getLastSdkInitParams();
        if (sdkInitParams != null) {
            mAppKeyET.setText(sdkInitParams.appKey);
            try {
                mAccessTokenET.setText(EZOpenSDK.getInstance().getEZAccessToken().getAccessToken());
                mApiET.setText(sdkInitParams.openApiServer);
                mAuthET.setText(sdkInitParams.openAuthApiServer);
                mSpecifiedDeviceET.setText(sdkInitParams.specifiedDevice);
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

        TextView sdkVerTv = findViewById(R.id.tv_sdk_ver);
        if (sdkVerTv != null) {
            sdkVerTv.setText("SDK Version: " + BuildConfig.VERSION_NAME);
        }
    }

    /**
     * 自动登录
     */
    private void autoLogin() {
        View view = findViewById(R.id.page_container);
        if (view != null) {
            view.post(() -> {
                if (TextUtils.isEmpty(mInitParams.appKey)) {
                    showToast("AppKey is empty!");
                    return;
                }
                if (LocalInfo.getInstance().getEZAccesstoken() == null || LocalInfo.getInstance().getEZAccesstoken().getAccessToken() == null) {
                    String tip = "AccessToken is empty!";
                    LogUtil.i(TAG, tip);
                    return;
                }
                new Thread(() -> startCheckLoginValidity()).start();
            });
        }
    }

    private AdapterView.OnItemSelectedListener mServerAreasOnItemCLickLister = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mCurrentServerArea = ServerAreasEnum.getAllServers().get(position);
            mApiET.setText(mCurrentServerArea.openApiServer);
            mAuthET.setText(mCurrentServerArea.openAuthApiServer);
            // 仅预置了appKey的区域才展示萤石账号登录按钮
            showEzvizAccountLoginTv(mCurrentServerArea.defaultOpenAuthAppKey != null);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            showEzvizAccountLoginTv(false);
        }

        private void showEzvizAccountLoginTv(boolean show) {
            View loginTv = findViewById(R.id.tv_ezviz_account_login);
            loginTv.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
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
