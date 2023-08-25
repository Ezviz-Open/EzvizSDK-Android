package com.videogo.ui.adddevice.presenter;

import android.app.Application;
import android.content.Intent;

import com.videogo.EzvizApplication;
import com.videogo.openapi.EZConstants;

import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiExecutingActivityPresenter;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiTypeConstants;

public class SmartConfigWifiPresenterForFullSdk extends ConfigWifiExecutingActivityPresenter {

    private final static String TAG = SmartConfigWifiPresenterForFullSdk.class.getSimpleName();

    public SmartConfigWifiPresenterForFullSdk(){
        mType = ConfigWifiTypeConstants.FULL_SDK_SMART_CONFIG;
    }

    @Override
    public void startConfigWifi(Application app, final Intent configParam) {
        // 准备参数
        String routerWifiName = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_SSID);
        String routerWifiPwd = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_PASSWORD);
        String deviceSerial = configParam.getStringExtra(IntentConstants.DEVICE_SERIAL);
        // 开始配网
        EzvizApplication.getOpenSDK().startConfigWifi(app, deviceSerial, routerWifiName, routerWifiPwd, EZConstants.EZWiFiConfigMode.EZWiFiConfigSmart, new MixedConfigWifiCallbackDelegate(mCallback));
    }

    @Override
    public void stopConfigWifi() {
        EzvizApplication.getOpenSDK().stopConfigWiFi();
    }
}
