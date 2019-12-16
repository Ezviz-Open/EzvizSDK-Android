/* 
 * @ProjectName VideoGoJar
 * @Copyright null
 * 
 * @FileName EzvizApplication.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-7-12
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo;

import com.google.gson.Gson;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;

import ezviz.ezopensdk.demo.SdkInitParams;
import ezviz.ezopensdk.demo.SdkInitTool;
import ezviz.ezopensdk.demo.SpTool;
import ezviz.ezopensdk.demo.ValueKeys;
import ezviz.ezopensdkcommon.common.BaseApplication;


public class EzvizApplication extends BaseApplication {

    public static String mAppKey;
    public static String mAccessToken;
    public static String mOpenApiServer;
    public static String mOpenAuthApiServer;

    public static EzvizApplication mEzvizApplication;

    public static EZOpenSDK getOpenSDK() {
        return EZOpenSDK.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
        initSDK();
        EzvizAPI.getInstance().setServerUrl(mOpenApiServer, mOpenAuthApiServer);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    private void init() {
        SpTool.init(getApplicationContext());
        if (!loadLastSdkInitParams()){
            LoadDefaultSdkInitParams();
        }
    }

    private boolean loadLastSdkInitParams() {
        String sdkInitParamStr = SpTool.obtainValue(ValueKeys.SDK_INIT_PARAMS);
        if (sdkInitParamStr != null){
            SdkInitParams sdkInitParams = new Gson().fromJson(sdkInitParamStr, SdkInitParams.class);
            if (sdkInitParams != null && sdkInitParams.appKey != null){
                mAppKey = sdkInitParams.appKey;
                mAccessToken = sdkInitParams.accessToken;
                mOpenApiServer = sdkInitParams.openApiServer;
                mOpenAuthApiServer = sdkInitParams.openAuthApiServer;
                return true;
            }
        }
        return false;
    }

    private void LoadDefaultSdkInitParams(){
        mAppKey = "26810f3acd794862b608b6cfbc32a6b8";
        mAccessToken = "";
        mOpenApiServer = "https://open.ys7.com";
        mOpenAuthApiServer = "https://openauth.ys7.com";
    }

    private void initSDK() {
        {
            SdkInitParams initParams = new SdkInitParams();
            initParams.appKey = mAppKey;
            SdkInitTool.initSdk(this, initParams);
        }

    }

}
