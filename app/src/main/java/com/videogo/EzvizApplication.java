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

import android.support.annotation.NonNull;

import com.videogo.openapi.EZOpenSDK;

import ezviz.ezopensdk.demo.SdkInitParams;
import ezviz.ezopensdkcommon.common.BaseApplication;


public class EzvizApplication extends BaseApplication {

    public static SdkInitParams mInitParams;

    public static EZOpenSDK getOpenSDK(){
        EZOpenSDK ezOpenSDK = EZOpenSDK.getInstance();
        return ezOpenSDK;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                throwable.printStackTrace();
                // 抓取到异常时，立即重启应用
                restartApp(EzvizApplication.this);
            }
        });
    }

}
