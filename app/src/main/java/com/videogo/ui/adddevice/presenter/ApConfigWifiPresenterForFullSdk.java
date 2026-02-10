package com.videogo.ui.adddevice.presenter;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.ezviz.sdk.configwifi.EZConfigWifiErrorEnum;
import com.ezviz.sdk.configwifi.EZConfigWifiInfoEnum;
import com.ezviz.sdk.configwifi.ap.WiFiConnecter;
import com.videogo.EzvizApplication;
import com.videogo.wificonfig.APWifiConfig;

import ezviz.ezopensdkcommon.common.Config;
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
        String wifiSSID = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_SSID);
        String wifiPwd = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_PASSWORD);
        String deviceSerial = configParam.getStringExtra(IntentConstants.DEVICE_SERIAL);
        String deviceVerifyCode = configParam.getStringExtra(IntentConstants.DEVICE_VERIFY_CODE);
        boolean autoConnect /*是否自动连接到设备热点*/ = !configParam.getBooleanExtra(IntentConstants.USE_MANUAL_AP_CONFIG,false);

        /**************************** 以下代码按需使用 ***************************/
        // 只有配网SDK会传如下两个值，EZOpenSDK不会传
        String deviceHotspotSSID /*设备热点名称，可以为空*/ = configParam.getStringExtra(IntentConstants.DEVICE_HOTSPOT_SSID);
        String deviceHotspotPwd /*设备热点密码，可以为空*/ = configParam.getStringExtra(IntentConstants.DEVICE_HOTSPOT_PWD);

        /**
         * 注意：如果你的设备热点是EZVIZ_开头的，deviceHotspotSSID和deviceHotspotPwd可传空；
         * 如果不是，这两个参数一定要传入对应的设备热点名和设备热点密码，否则配网失败
         *
         * 萤石基线设备的热点名称前缀都是EZVIZ_，定制设备的热点名称不确定；Config.DeviceHotspotPrefix在SeriesNumSearchActivity.java中会赋值
         * 如果确定项目接入的设备都是萤石基线设备，可忽略如下if判断逻辑
         */
        if (!Config.DeviceHotspotPrefix.equals("EZVIZ") && TextUtils.isEmpty(deviceHotspotSSID)) {
            deviceHotspotSSID = Config.DeviceHotspotPrefix + "_" + deviceSerial;
            deviceHotspotPwd = Config.DeviceHotspotPrefix + "_" + deviceVerifyCode;
        }
        /**************************** 以上代码按需使用 ***************************/

        // 开始配网
        EzvizApplication.getOpenSDK().startAPConfigWifiWithSsid(wifiSSID, wifiPwd,
                deviceSerial, deviceVerifyCode,
                deviceHotspotSSID, deviceHotspotPwd,
                autoConnect, mConfigCallback);
        /**
         * 关于AP配网过程中连接设备热点不断弹出的问题说明
         * 系统在wifi连接到设备的AP 热点后，大概过了30s，系统启动外网的检测机制
         当发现该设备热点wifi无外网时，给该设备热点打上一个无外网的标签并且缓存到系统里面。
         当App调用API主动连接wifi名相同的热点时，系统读取到缓存认为这个wifi无网络，直接强行关闭弹窗，重新弹窗。

         * 解决方案：
         手动去wifi管理页面，把缓存的相同wifi名删除掉即可。
         */
    }

    @Override
    public void stopConfigWifi() {
        EzvizApplication.getOpenSDK().stopAPConfigWifiWithSsid();
    }

    private APWifiConfig.APConfigCallback mConfigCallback = new APWifiConfig.APConfigCallback() {
        @Override
        public void onSuccess() {// SDK将WiFi信息发送给设备后回调此方法（WiFi密码可能错误，设备不一定联网成功）
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
            // SDK将WiFi信息发送给设备后，SDK内部会开启轮询设备是否联网成功并注册到平台，如果注册到平台了会回调此code
            if (code == EZConfigWifiInfoEnum.CONNECTED_TO_PLATFORM.code) {
                mCallback.onConnectedToPlatform();
            }
        }

        @Override
        public void OnError(int code) {// AP配网过程中的错误回调
            if (mCallback == null) {
                return;
            }
            LogUtil.e(TAG, "OnError: " + code);
            boolean solved = false;
            if (code == EZConfigWifiErrorEnum.CONFIG_TIMEOUT.code) {
                solved = true;
                mCallback.onTimeout();
            } else if (code == EZConfigWifiErrorEnum.PHONE_NOT_CONNECTED_TO_TARGET_WIFI.code) {
                // TODO: 摄像机要连接的WiFi和手机当前连接的WiFi不一致，SDK内部检测会回调此消息，不用处理
            } else if (code == EZConfigWifiErrorEnum.USER_REFUSED_CONNECTION_REQUEST.code) {
                // TODO: 在Android10及以上系统，使用NetworkSpecifier尝试自动连接到设备热点时，被用户主动拒绝。
            } else if (code == WiFiConnecter.PARAM_ERROR) {
                // TODO: 参数错误
            } else if (code == WiFiConnecter.PASSWORD_ERROR) {
                // TODO: 设备ap热点密码错误
            } else if (code == WiFiConnecter.CONNECT_ERROR) {
                // TODO: 连接ap热点异常
            } else if (code == WiFiConnecter.SCAN_ERROR) {
                // TODO: 搜索WiFi热点错误
            } else if (code == WiFiConnecter.COUNTOUT_ERROR) {
                // TODO: WiFi热点连接错误
            } else {
                // TODO: 其他错误码请查看 EZConfigWifiErrorEnum类
            }

            if (!solved) {
                mCallback.onConfigError(code, null);
            }
        }
    };

}
