package ezviz.ezopensdkcommon.configwifi;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigWifiExecutingActivityPresenter {

    private static List<ConfigWifiExecutingActivityPresenter> mAvailablePresenterList = new ArrayList<>();

    protected Callback mCallback;
    protected String mType;

    public String getType(){
        return mType;
    }

    public void setCallback(Callback mCallback) {
        this.mCallback = mCallback;
    }

    public static void addPresenter(ConfigWifiExecutingActivityPresenter presenter){
        // 移除同类presenter，避免互相干扰
        clearPresenter(presenter.mType);
        mAvailablePresenterList.add(presenter);
    }

    public static ConfigWifiExecutingActivityPresenter getPresenter(String type){
        for (ConfigWifiExecutingActivityPresenter presenter: mAvailablePresenterList){
            if (!TextUtils.isEmpty(type) && type.equals(presenter.mType)){
                return presenter;
            }
        }
        return null;
    }

    private static void clearPresenter(String type){
        List<ConfigWifiExecutingActivityPresenter> mFoundList = new ArrayList<>();
        for (ConfigWifiExecutingActivityPresenter presenter: mAvailablePresenterList){
            if (!TextUtils.isEmpty(type) && type.equals(presenter.mType)){
                mFoundList.add(presenter);
            }
        }
        mAvailablePresenterList.removeAll(mFoundList);
    }

    public abstract void startConfigWifi(Application app, Intent configParam);
    public abstract void stopConfigWifi();

    public interface Callback{
        void onConnectedToWifi();

        /**
         * 设备已经上线，仅供完整SDK使用
         */
        void onConnectedToPlatform();

        void onConfigInfo(int info);
        void onConfigError(int code, String msg);
        void onTimeout();
    }

}
