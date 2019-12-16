package ezviz.ezopensdkcommon.configwifi;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ezviz.sdk.configwifi.EZConfigWifiErrorEnum;

import ezviz.ezopensdkcommon.R;
import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.common.LogUtil;
import ezviz.ezopensdkcommon.common.RootActivity;
import ezviz.ezopensdkcommon.common.RouteNavigator;

public class ConfigWifiExecutingActivity extends RootActivity implements ConfigWifiExecutingActivityPresenter.Callback{

    private final static String TAG = ConfigWifiExecutingActivity.class.getSimpleName();

    private ConfigWifiExecutingActivityPresenter mPresenter;
    private View mConfigResultView;
    private View mConfigSuccessView;
    private View mConfigFailView;
    private TextView mConfigErrorInfoTv;
    private String mAllErrorInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wifi_executing);

        mPresenter = ConfigWifiExecutingActivityPresenter.getPresenter(getIntent()
                .getStringExtra(IntentConstants.SELECTED_PRESENTER_TYPE));
        if (mPresenter == null){
            LogUtil.e(TAG, "failed to init!");
            return;
        }
        initUI();
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                showExecutingUi();
                startConfig();
            }
        });
    }

    /**
     * 重试
     */
    public void onClickRetryConfigWifi(View view) {
        showExecutingUi();
        startConfig();
    }

    /**
     * 配网成功，根据使用sdk不同，跳转到对应页面
     */
    public void onClickToConfigAnother(View view) {
        // 如果是在使用配网sdk，则跳转到配网开始页
        if (getIntent().getBooleanExtra(IntentConstants.USING_CONFIG_WIFI_SDK, false)){
            ARouter.getInstance().build(RouteNavigator.CONFIG_WIFI_MAIN_PAGE)
                    .navigation(this, new NavCallback() {
                        @Override
                        public void onArrival(Postcard postcard) {
                            exitPage();
                        }

                        @Override
                        public void onLost(Postcard postcard) {
                            // do nothing
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        exitPage();
    }

    public void onClickExit(View view) {
        exitPage();
    }

    private void exitPage(){
        stopConfig();
        finish();
    }

    @Override
    public void onConnectedToWifi() {
        LogUtil.i(TAG, "onConnectedToWifi");
        // 仅使用配网sdk时，才展示配网成功的ui
        if (getIntent().getBooleanExtra(IntentConstants.USING_CONFIG_WIFI_SDK, false)){
            showConfigSuccessUi();
        }
    }

    @Override
    public void onConnectedToPlatform() {
        LogUtil.i(TAG, "onConnectedToPlatform");
        ARouter.getInstance().build(RouteNavigator.ADD_DEVICE_PAGE)
                .withString(IntentConstants.DEVICE_SERIAL, getIntent().getStringExtra(IntentConstants.DEVICE_SERIAL))
                .withString(IntentConstants.DEVICE_VERIFY_CODE, getIntent().getStringExtra(IntentConstants.DEVICE_VERIFY_CODE))
                .navigation(this, new NavCallback() {
                    @Override
                    public void onArrival(Postcard postcard) {
                        exitPage();
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        // do nothing
                    }
                });
    }

    @Override
    public void onConfigInfo(int info) {

    }

    @Override
    public void onConfigError(int code, String msg) {
        String errorInfo = "***"  + "error code is: " + code +  ", error msg is: " + msg + "\n";
        LogUtil.e(TAG, errorInfo);
        // 仅展示新定义的错误码
        for (EZConfigWifiErrorEnum error : EZConfigWifiErrorEnum.values()){
            if (code == error.code){
                if (mAllErrorInfo == null){
                    mAllErrorInfo = "" + errorInfo;
                }else{
                    mAllErrorInfo += errorInfo;
                }
                break;
            }
        }
    }

    @Override
    public void onTimeout() {
        stopConfig();
        showConfigFailUi();
    }

    private void startConfig(){
        if (mPresenter != null){
            mPresenter.setCallback(this);
            mPresenter.startConfigWifi(getApplication(), getIntent());
        }
    }

    private void stopConfig(){
        if (mPresenter != null){
            mPresenter.setCallback(null);
            mPresenter.stopConfigWifi();
        }
    }

    private void initUI() {
        mConfigResultView = findViewById(R.id.app_common_vg_config_result);
        mConfigSuccessView = findViewById(R.id.app_common_config_result_success);
        mConfigFailView = findViewById(R.id.app_common_config_result_fail);
        mConfigErrorInfoTv = findViewById(R.id.app_common_all_config_error_info);
    }

    private void showExecutingUi(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConfigResultView.setVisibility(View.GONE);
                mConfigSuccessView.setVisibility(View.GONE);
                mConfigFailView.setVisibility(View.GONE);
                mAllErrorInfo = null;
            }
        });
    }

    private void showConfigSuccessUi(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConfigResultView.setVisibility(View.VISIBLE);
                mConfigSuccessView.setVisibility(View.VISIBLE);
                mConfigFailView.setVisibility(View.GONE);
                mAllErrorInfo = null;
            }
        });
    }

    private void showConfigFailUi(){
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               mConfigErrorInfoTv.setText(mAllErrorInfo);
               mConfigResultView.setVisibility(View.VISIBLE);
               mConfigSuccessView.setVisibility(View.GONE);
               mConfigFailView.setVisibility(View.VISIBLE);
               mAllErrorInfo = null;
           }
       });
    }

}
