package ezviz.ezopensdk.configwifi;

import android.app.Application;
import android.content.Intent;

import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;

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
        EZOpenSDK.getInstance().startConfigWifi(app, deviceSerial, routerWifiName, routerWifiPwd, EZConstants.EZWiFiConfigMode.EZWiFiConfigSmart, new MixedConfigWifiCallbackDelegate(mCallback));
    }

    @Override
    public void stopConfigWifi() {
        EZOpenSDK.getInstance().stopConfigWiFi();
    }
}
