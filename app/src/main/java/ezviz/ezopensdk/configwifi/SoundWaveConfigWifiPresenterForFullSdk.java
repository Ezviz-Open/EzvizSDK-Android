package ezviz.ezopensdk.configwifi;

import android.app.Application;
import android.content.Intent;

import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;

import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiExecutingActivityPresenter;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiTypeConstants;

public class SoundWaveConfigWifiPresenterForFullSdk extends ConfigWifiExecutingActivityPresenter {

    private final static String TAG = SoundWaveConfigWifiPresenterForFullSdk.class.getSimpleName();

    public SoundWaveConfigWifiPresenterForFullSdk(){
        mType = ConfigWifiTypeConstants.FULL_SDK_SOUND_WAVE;
    }

    @Override
    public void startConfigWifi(Application app, final Intent configParam) {
        // 准备参数
        String routerWifiName = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_SSID);
        String routerWifiPwd = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_PASSWORD);
        String deviceSerial = configParam.getStringExtra(IntentConstants.DEVICE_SERIAL);
        // 开始配网
        EZOpenSDK.getInstance().startConfigWifi(app, deviceSerial, routerWifiName, routerWifiPwd, EZConstants.EZWiFiConfigMode.EZWiFiConfigWave, new MixedConfigWifiCallbackDelegate(mCallback));
    }

    @Override
    public void stopConfigWifi() {
        EZOpenSDK.getInstance().stopConfigWiFi();
    }
}
