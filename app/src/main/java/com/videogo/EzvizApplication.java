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

import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;

import ezviz.ezopensdk.demo.SdkInitParams;
import ezviz.ezopensdk.demo.SdkInitTool;
import ezviz.ezopensdkcommon.common.BaseApplication;


public class EzvizApplication extends BaseApplication {

    public static SdkInitParams mInitParams;
    public static EzvizApplication mEzvizApplication;

    public static EZOpenSDK getOpenSDK() {
        if (EzvizAPI.getInstance() != null && EzvizAPI.getInstance().isUsingGlobalSDK()){
            return EZGlobalSDK.getInstance();
        }else{
            return EZOpenSDK.getInstance();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

}
