package ezviz.ezopensdk.configwifi;

import android.app.Application;
import android.content.Intent;

import com.ezviz.sdk.configwifi.EZConfigWifiInfoEnum;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.wificonfig.APWifiConfig;

import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.common.LogUtil;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiExecutingActivityPresenter;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiTypeConstants;

public class ApConfigWifiPresenterForFullSdk extends ConfigWifiExecutingActivityPresenter {

    private final static String TAG = ApConfigWifiPresenterForFullSdk.class.getSimpleName();

    public ApConfigWifiPresenterForFullSdk(){
        mType = ConfigWifiTypeConstants.FULL_SDK_AP;
    }

    @Override
    public void startConfigWifi(Application app, final Intent configParam) {

        // 准备参数
        String routerWifiName = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_SSID);
        String routerWifiPwd = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_PASSWORD);
        String deviceSerial = configParam.getStringExtra(IntentConstants.DEVICE_SERIAL);
        String deviceVerifyCode = configParam.getStringExtra(IntentConstants.DEVICE_VERIFY_CODE);
        // 开始配网
        EZOpenSDK.getInstance().startAPConfigWifiWithSsid(routerWifiName, routerWifiPwd, deviceSerial, deviceVerifyCode, new APWifiConfig.APConfigCallback() {
            @Override
            public void onSuccess() {
                if (mCallback == null){
                    return;
                }
                mCallback.onConnectedToWifi();
            }

            @Override
            public void onInfo(int code, String message) {
                if (mCallback == null){
                    return;
                }
                if (code == EZConfigWifiInfoEnum.CONNECTED_TO_PLATFORM.code){
                    mCallback.onConnectedToPlatform();
                }
            }

            @Override
            public void OnError(int code) {
                if (mCallback == null){
                    return;
                }
                LogUtil.e(TAG, "OnError: " + code);
                boolean solved = false;
                switch (code) {
                    case 15:
                        solved = true;
                        mCallback.onTimeout();
                        // TODO: 2018/7/24 超时
                        break;
                    case 1:
                        // TODO: 2018/7/24 参数错误
                        break;
                    case 2:
                        // TODO: 2018/7/24 设备ap热点密码错误
                        break;
                    case 3:
                        // TODO: 2018/7/24  连接ap热点异常
                        break;
                    case 4:
                        // TODO: 2018/7/24 搜索WiFi热点错误
                        break;
                    default:
                        // TODO: 2018/7/24 未知错误
                        break;
                }
                if (!solved){
                    mCallback.onConfigError(code, null);
                }
            }
        });

    }

    @Override
    public void stopConfigWifi() {
        EZOpenSDK.getInstance().stopAPConfigWifiWithSsid();
    }
}
