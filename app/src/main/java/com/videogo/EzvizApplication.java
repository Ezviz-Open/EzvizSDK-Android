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
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;

import ezviz.ezopensdk.demo.SdkInitParams;
import ezviz.ezopensdk.demo.SdkInitTool;
import ezviz.ezopensdk.demo.SpTool;
import ezviz.ezopensdk.demo.ValueKeys;
import ezviz.ezopensdkcommon.common.BaseApplication;


public class EzvizApplication extends BaseApplication {

    public static SdkInitParams mInitParams;
    public static EzvizApplication mEzvizApplication;

    public static EZOpenSDK getOpenSDK() {
        if (EzvizAPI.getInstance().isUsingGlobalSDK()){
            return EZGlobalSDK.getInstance();
        }else{
            return EZOpenSDK.getInstance();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
        initSDK();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    /**
     * 获取持久化保存的参数
     */
    private void init() {
        SpTool.init(getApplicationContext());
        if (!loadLastSdkInitParams()){
            LoadDefaultSdkInitParams();
        }
    }

    private boolean loadLastSdkInitParams() {
        String sdkInitParamStr = SpTool.obtainValue(ValueKeys.SDK_INIT_PARAMS);
        if (sdkInitParamStr != null){
            mInitParams = new Gson().fromJson(sdkInitParamStr, SdkInitParams.class);
            return mInitParams != null && mInitParams.appKey != null;
        }
        return false;
    }

    private void LoadDefaultSdkInitParams(){
        mInitParams = SdkInitParams.createBy(null);
        mInitParams.appKey = "26810f3acd794862b608b6cfbc32a6b8";
        mInitParams.accessToken = "";
        mInitParams.openApiServer = "https://open.ys7.com";
        mInitParams.openAuthApiServer = "https://openauth.ys7.com";
    }

    /**
     * 初始化SDK
     */
    private void initSDK() {
        SdkInitTool.initSdk(this, mInitParams);
    }

}
