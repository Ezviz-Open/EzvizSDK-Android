package ezviz.ezopensdk.configwifi;

import android.app.Application;
import android.content.Intent;

import com.ezviz.sdk.configwifi.EZConfigWifiInfoEnum;
import com.videogo.EzvizApplication;
import com.videogo.wificonfig.APWifiConfig;

import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.common.LogUtil;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiExecutingActivityPresenter;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiTypeConstants;

public class ApConfigWifiPresenterForFullSdk extends ConfigWifiExecutingActivityPresenter {

    private final static String TAG = ApConfigWifiPresenterForFullSdk.class.getSimpleName();

    public ApConfigWifiPresenterForFullSdk() {
        mType = ConfigWifiTypeConstants.FULL_SDK_AP;
    }

    @Override
    public void startConfigWifi(Application app, final Intent configParam) {
        // 准备参数
        String routerWifiName = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_SSID);
        String routerWifiPwd = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_PASSWORD);
        String deviceSerial = configParam.getStringExtra(IntentConstants.DEVICE_SERIAL);
        String deviceVerifyCode = configParam.getStringExtra(IntentConstants.DEVICE_VERIFY_CODE);
        String deviceHotspotSSID /*设备热点名称，可以为空*/ = configParam.getStringExtra(IntentConstants.DEVICE_HOTSPOT_SSID);
        String deviceHotspotPwd /*设备热点密码，可以为空*/ = configParam.getStringExtra(IntentConstants.DEVICE_HOTSPOT_PWD);
        boolean autoConnect /*是否自动连接到设备热点*/ = !configParam.getBooleanExtra(IntentConstants.USE_MANUAL_AP_CONFIG,
                false);
        // 注意：如果你的设备热点是EZVIZ_开头的，deviceHotspotSSID和deviceHotspotPwd可传空；如果不是，这两个参数一定要传入对应的设备热点名和设备热点密码，否则配网失败
        // 开始配网
        EzvizApplication.getOpenSDK().startAPConfigWifiWithSsid(routerWifiName, routerWifiPwd,
                deviceSerial, deviceVerifyCode,
                deviceHotspotSSID, deviceHotspotPwd,
                autoConnect, mConfigCallback);
        // 关于AP配网过程中连接设备热点不断弹出的问题说明：系统在wifi连接到设备的AP 热点后，大概过了30s，系统启动外网的检测机制，当发现该设备热点wifi无外网时，给该设备热点打上一个无外网的标签并且缓存到系统里面。当App调用API主动连接wifi名相同的热点时，系统读取到缓存认为这个wifi无网络，直接强行关闭弹窗，重新弹窗。
        // 解决方案：手动去wifi管理页面，把缓存的相同wifi名删除掉即可。
    }

    @Override
    public void stopConfigWifi() {
        EzvizApplication.getOpenSDK().stopAPConfigWifiWithSsid();
    }

    private APWifiConfig.APConfigCallback mConfigCallback = new APWifiConfig.APConfigCallback() {
        @Override
        public void onSuccess() {
            if (mCallback == null) {
                return;
            }
            mCallback.onConnectedToWifi();
        }

        @Override
        public void onInfo(int code, String message) {
            if (mCallback == null) {
                return;
            }
            if (code == EZConfigWifiInfoEnum.CONNECTED_TO_PLATFORM.code) {
                mCallback.onConnectedToPlatform();
            }
        }

        @Override
        public void OnError(int code) {
            if (mCallback == null) {
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
            if (!solved) {
                mCallback.onConfigError(code, null);
            }
        }
    };

}
