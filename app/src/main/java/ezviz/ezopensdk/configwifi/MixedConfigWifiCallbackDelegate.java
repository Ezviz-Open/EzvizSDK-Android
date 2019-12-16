package ezviz.ezopensdk.configwifi;

import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDKListener;

import ezviz.ezopensdkcommon.common.LogUtil;
import ezviz.ezopensdkcommon.configwifi.ConfigWifiExecutingActivityPresenter;

public class MixedConfigWifiCallbackDelegate extends EZOpenSDKListener.EZStartConfigWifiCallback {

    private final static String TAG = MixedConfigWifiCallbackDelegate.class.getSimpleName();

    private ConfigWifiExecutingActivityPresenter.Callback mCallback;

    public MixedConfigWifiCallbackDelegate(ConfigWifiExecutingActivityPresenter.Callback callback){
        mCallback = callback;
    }

    @Override
    public void onStartConfigWifiCallback(String deviceSerial, EZConstants.EZWifiConfigStatus status) {
        LogUtil.d(TAG, "onStartConfigWifiCallback: " + status);
        if (mCallback == null){
            return;
        }
        switch (status){
            case DEVICE_WIFI_CONNECTED:
                mCallback.onConnectedToWifi();
                break;
            case DEVICE_PLATFORM_REGISTED:
                mCallback.onConnectedToPlatform();
                break;
            case TIME_OUT:
                mCallback.onTimeout();
                break;
            default:break;
        }
    }

}
