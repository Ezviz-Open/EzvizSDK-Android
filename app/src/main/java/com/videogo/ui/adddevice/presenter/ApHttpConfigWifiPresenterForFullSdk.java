package com.videogo.ui.adddevice.presenter;

import android.app.Application;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.ezviz.http.exception.EzConfigWifiException;
import com.ezviz.http.model.DeviceTokenInfo;
import com.ezviz.sdk.configwifi.EZConfigWifiErrorEnum;
import com.ezviz.sdk.configwifi.touchAp.GetTokenCallback;
import com.ezviz.sdk.configwifi.touchAp.QueryPlatformBindStatusCallback;
import com.videogo.EzvizApplication;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EzvizAPI;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.wificonfig.APWifiConfig;

import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdkcommon.common.Config;
import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiExecutingActivityPresenter;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiTypeConstants;

public class ApHttpConfigWifiPresenterForFullSdk extends ConfigWifiExecutingActivityPresenter {

    private final static String TAG = ApHttpConfigWifiPresenterForFullSdk.class.getSimpleName();

    private String deviceSerial;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private int searchCount;
    private final static int SEARCH_MAX_COUNT = 20;

    public ApHttpConfigWifiPresenterForFullSdk() {
        mType = ConfigWifiTypeConstants.FULL_SDK_APHTTP;
    }

    @Override
    public void startConfigWifi(Application app, final Intent configParam) {
        // 准备参数
        String wifiSSID = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_SSID);
        String wifiPwd = configParam.getStringExtra(IntentConstants.ROUTER_WIFI_PASSWORD);
        deviceSerial = configParam.getStringExtra(IntentConstants.DEVICE_SERIAL);
        String deviceVerifyCode = configParam.getStringExtra(IntentConstants.DEVICE_VERIFY_CODE);
        // 爱加设备的热点名称为CAMGO_xxxxxxxxx，密码为空
        String deviceHotspotSSID = Config.DeviceHotspotPrefix + "_" + deviceSerial;
        String deviceHotspotPwd = "";
        boolean autoConnect /*是否自动连接到设备热点*/ = !configParam.getBooleanExtra(IntentConstants.USE_MANUAL_AP_CONFIG, false);
        // 开始配网
        EzvizApplication.getOpenSDK().getNewApConfigToken(new GetTokenCallback() {
            @Override
            public void onSuccess(final DeviceTokenInfo deviceTokenInfo) {
                if (mActivity == null || !EzvizAPI.getInstance().isUsingGlobalSDK()) {
                    return;
                }
                mActivity.runOnUiThread(() -> {
                    EZGlobalSDK ezGlobalSDK = (EZGlobalSDK) EzvizApplication.getOpenSDK();
                    String lbsDomain = deviceTokenInfo.registerUrl;
                    ezGlobalSDK.startAPHttpConfigWifiWithToken(deviceTokenInfo.token, wifiSSID, wifiPwd,
                            deviceHotspotSSID, deviceHotspotPwd, lbsDomain, autoConnect,
                            new APWifiConfig.APConfigCallback() {
                        @Override
                        public void onSuccess() {
                            LogUtil.d(TAG, "startAPHttpConfigWifiWithToken onSuccess");
                            startSearchDeviceTimer();
                        }

                        @Override
                        public void OnError(int code) {
                            LogUtil.e(TAG, "startAPHttpConfigWifiWithToken OnError, error code: " + code);
                            if (code == EZConfigWifiErrorEnum.CONFIG_TIMEOUT.code) {
                                if (mCallback != null) {
                                    mCallback.onTimeout();
                                }
                            }
                        }
                    });
                });

            }

            @Override
            public void onError(final EzConfigWifiException ezConfigWifiException) {
                if (mCallback != null) {
                    mCallback.onConfigError(ezConfigWifiException.errorCode, ezConfigWifiException.message);
                }
            }
        });

    }

    @Override
    public void stopConfigWifi() {
        EZGlobalSDK ezGlobalSDK = (EZGlobalSDK) EzvizApplication.getOpenSDK();
        ezGlobalSDK.stopAPHttpConfigWifiWithToken();
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
            if (mCallback != null) {
                mCallback.onTimeout();
            }
            return;
        }
        EZGlobalSDK ezGlobalSDK = (EZGlobalSDK) EzvizApplication.getOpenSDK();
        ezGlobalSDK.queryPlatformBindStatus(deviceSerial, new QueryPlatformBindStatusCallback() {
            @Override
            public void onSuccess(boolean isBindSuccess) {
                if (isBindSuccess) {
                    Log.e(TAG, "queryPlatformBindStatus success");
                    stopTimer();
                    if (mCallback != null) {
                        mCallback.onConnectedToPlatform();
                    }
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
