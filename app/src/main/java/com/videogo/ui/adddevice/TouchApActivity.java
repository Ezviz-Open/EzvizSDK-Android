package com.videogo.ui.adddevice;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ezviz.http.exception.EzConfigWifiException;
import com.ezviz.http.model.AccessDeviceInfo;
import com.ezviz.http.model.DeviceTokenInfo;
import com.ezviz.http.model.EzWifiInfo;
import com.ezviz.sdk.configwifi.touchAp.GetAccessDeviceInfoCallback;
import com.ezviz.sdk.configwifi.touchAp.GetDeviceWifiListCallback;
import com.ezviz.sdk.configwifi.touchAp.GetTokenCallback;
import com.ezviz.sdk.configwifi.touchAp.QueryPlatformBindStatusCallback;
import com.ezviz.sdk.configwifi.touchAp.StartNewApConfigCallback;
import com.ezviz.sdk.configwifi.touchAp.TouchApApi;
import com.videogo.EzvizApplication;
import com.videogo.util.Utils;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;
import ezviz.ezopensdkcommon.common.RouteNavigator;

@Route(path = RouteNavigator.TOUCH_AP_WIFICONFIG_PAGE)
public class TouchApActivity extends RootActivity implements View.OnClickListener {

    // WiFi名称
    private final static String SSID_Name = "ezviz_mobile_AV";
    // WiFi密码
    private final static String SSID_Pwd = "chengjun7";

    private DeviceTokenInfo tokenInfo;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int searchCount;
    private final static int SEARCH_MAX_COUNT = 20;

    private EditText wifiSsidEt, wifiPwdEt, devSerialEt, configTokenEt;
    private TextView logPrintTv;
    private Dialog waitDialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch_ap_config);
        initTitleBar();

        wifiSsidEt = findViewById(R.id.wifiSsidEt);
        wifiPwdEt = findViewById(R.id.wifiPwdEt);
        devSerialEt = findViewById(R.id.devSerialEt);
        configTokenEt = findViewById(R.id.tokenEt);
        logPrintTv = findViewById(R.id.log_print);
        waitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        findViewById(R.id.getDeviceInfoBtn).setOnClickListener(this);
        findViewById(R.id.getDeviceWifiListBtn).setOnClickListener(this);
        findViewById(R.id.getTokenBtn).setOnClickListener(this);
        findViewById(R.id.apConfigWifiBtn).setOnClickListener(this);

        wifiSsidEt.setText(SSID_Name);
        wifiPwdEt.setText(SSID_Pwd);
    }

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle("TouchAPConfig");
        mTitleBar.addBackButton(v -> onBackPressed());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getTokenBtn:// 获取配网token
                waitDialog.show();
                EzvizApplication.getOpenSDK().getNewApConfigToken(new GetTokenCallback() {
                    @Override
                    public void onSuccess(final DeviceTokenInfo deviceTokenInfo) {
                        waitDialog.dismiss();
                        runOnUiThread(() -> {
                            tokenInfo = deviceTokenInfo;
                            configTokenEt.setText(deviceTokenInfo.token);
                            logPrintTv.setText(TouchApApi.responseData);
                        });

                    }

                    @Override
                    public void onError(final EzConfigWifiException ezConfigWifiException) {
                        runOnUiThread(() -> Utils.showToast(TouchApActivity.this,
                                "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: " + ezConfigWifiException.message));
                        waitDialog.dismiss();
                    }
                });
                break;
            case R.id.apConfigWifiBtn:// 发起配网
                /**
                 * 步骤0：解除对指定ip的网络限制
                 * res/xml文件夹下创建network_security_config.xml，
                 * 并在AndroidManifest.xml中的application下配置android:networkSecurityConfig="@xml/network_security_config"
                 */

                stopTimer();
                final String wifiSsid = wifiSsidEt.getText().toString();
                final String wifiPwd = wifiPwdEt.getText().toString();
                final String configToken = configTokenEt.getText().toString();

                if (TextUtils.isEmpty(wifiSsid)) {
                    Utils.showToast(TouchApActivity.this, "wifi用户名不能为空");
                    return;
                }
                if (TextUtils.isEmpty(wifiPwd)) {
                    Utils.showToast(TouchApActivity.this, "wifi密码不能为空");
                    return;
                }
                // 步骤1：getNewApConfigToken获取配网所需的tokenInfo；获取成功后手动切换连接设备热点
                if (TextUtils.isEmpty(configToken)) {
                    Utils.showToast(TouchApActivity.this, "请先获取配网token");
                    return;
                }
                waitDialog.show();
                // 步骤2：连接设备热点(手动去设置里连接)

                // 步骤3：连接上设备热点后，获取设备的序列号，查询设备配网结果用
                EzvizApplication.getOpenSDK().getAccessDeviceInfo(new GetAccessDeviceInfoCallback() {
                    @Override
                    public void onSuccess(final AccessDeviceInfo accessDeviceInfo) {
                        runOnUiThread(() -> {
                            devSerialEt.setText(accessDeviceInfo.devSubserial);
                            logPrintTv.setText(TouchApApi.responseData);

                            // 步骤4：发起配网请求，设备配网成功后会自动绑定到账号下
                            EzvizApplication.getOpenSDK().startNewApConfigWithToken(configToken, wifiSsid
                                    , wifiPwd, tokenInfo.registerUrl, new StartNewApConfigCallback() {
                                @Override
                                public void onResponse(int statusCode, String statusDesc) {
                                    waitDialog.dismiss();
                                    runOnUiThread(() -> {
                                        logPrintTv.setText(TouchApApi.responseData);
                                        // 步骤5
                                        startSearchDeviceTimer();
                                    });
                                }

                                @Override
                                public void onError(final EzConfigWifiException ezConfigWifiException) {
                                    // 将信息发送给设备后，设备关闭热点去连接网络，无回调给App，会回调onError，也需要去发起轮询
                                    runOnUiThread(() -> {
                                        // 步骤5
                                        startSearchDeviceTimer();
                                        Log.e(TAG, "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: "
                                                + ezConfigWifiException.message);
                                    });
                                    waitDialog.dismiss();
                                }
                            });
                        });
                    }

                    @Override
                    public void onError(final EzConfigWifiException ezConfigWifiException) {
                        runOnUiThread(() -> {
                            Utils.showToast(TouchApActivity.this,
                                    "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: " + ezConfigWifiException.message);
                            waitDialog.dismiss();
                        });
                    }
                });
                break;
            case R.id.getDeviceInfoBtn:// 获取设备信息
                waitDialog.show();
                EzvizApplication.getOpenSDK().getAccessDeviceInfo(new GetAccessDeviceInfoCallback() {
                    @Override
                    public void onSuccess(final AccessDeviceInfo accessDeviceInfo) {
                        waitDialog.dismiss();
                        runOnUiThread(() -> {
                            devSerialEt.setText(accessDeviceInfo.devSubserial);
                            logPrintTv.setText(TouchApApi.responseData);
                        });
                    }

                    @Override
                    public void onError(final EzConfigWifiException ezConfigWifiException) {
                        waitDialog.dismiss();
                        runOnUiThread(() -> Utils.showToast(TouchApActivity.this,
                                "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: " + ezConfigWifiException.message));
                    }
                });
                break;
            case R.id.getDeviceWifiListBtn:// 获取设备WiFi列表
                waitDialog.show();
                EzvizApplication.getOpenSDK().getAccessDeviceWifiList(new GetDeviceWifiListCallback() {

                    @Override
                    public void onSuccess(List<EzWifiInfo> ezWifiInfoList) {
                        waitDialog.dismiss();
                        runOnUiThread(() -> logPrintTv.setText(TouchApApi.responseData));
                    }

                    @Override
                    public void onError(final EzConfigWifiException ezConfigWifiException) {
                        waitDialog.dismiss();
                        runOnUiThread(() -> Utils.showToast(TouchApActivity.this,
                                "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: " + ezConfigWifiException.message));
                    }
                });
                break;
        }
    }

    /**
     * 设备联网成功或者未知错误(某些型号设备无返回值)的时候发起轮询设备的绑定情况
     */
    private void startSearchDeviceTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                searchDeviceFromService();
            }
        };
        mTimer.schedule(mTimerTask, 0, 5000);
    }

    private void searchDeviceFromService() {
        searchCount++;
        if (searchCount >= SEARCH_MAX_COUNT) {
            mTimer.cancel();
            mTimer = null;
            return;
        }
        String deviceSerial = devSerialEt.getText().toString();
        EzvizApplication.getOpenSDK().queryPlatformBindStatus(deviceSerial, new QueryPlatformBindStatusCallback() {
            @Override
            public void onSuccess(boolean isBindSuccess) {
                if (isBindSuccess) {
                    Log.e(TAG, "queryPlatformBindStatus success");
                    dialog("config success","device has been successfully connected to the network and successfully bind to account");
                    stopTimer();
                } else {
                    Log.e(TAG, "");
                }
            }

            @Override
            public void onError(final EzConfigWifiException ezConfigWifiException) {
                Log.e(TAG, "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: " + ezConfigWifiException.message);
            }
        });
    }

    private void stopTimer() {
        if (mTimer == null) {
            return;
        }
        mTimer.cancel();
        mTimer = null;
        searchCount = 0;
    }

}
