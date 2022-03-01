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

import android.os.Environment;
import android.support.annotation.NonNull;

import com.videogo.openapi.EZOpenSDK;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import ezviz.ezopensdk.BuildConfig;
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

                    saveLogToFile(thread, throwable);

                    if (!BuildConfig.DEBUG){
                        restartApp(EzvizApplication.this);
                    }
            }
        });
    }

    public void saveLogToFile(Thread th, Throwable throwable)  {
        final File logFile = new File(getExternalFilesDir(null)+"/0_OpenSDK/crash.txt");

        PrintWriter printWriter = null;

        File logFileFolder = logFile.getParentFile();
        boolean exist = logFileFolder.exists();
        if (!exist){
            exist = logFileFolder.mkdirs();
        }

        try {
            printWriter = new PrintWriter(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while(cause != null){
            cause.printStackTrace(printWriter);
            cause.getCause();
        }
//        String result = info.toString();
        printWriter.close();

    }

}
