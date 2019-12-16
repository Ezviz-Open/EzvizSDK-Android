package ezviz.ezopensdkcommon.common;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.alibaba.android.arouter.launcher.ARouter;

import ezviz.ezopensdkcommon.debug.LogFileUtil;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化ARouter
        ARouter.openDebug();
        ARouter.openLog();
        ARouter.init(this);

        // 初始化日志文件进程
        if (isMainProcess()){
            LogFileUtil.startSaveLogToFile(getApplicationContext());
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (isMainProcess()){
            LogFileUtil.stopSaveLogToFile();
        }
    }

    /**
     * 获取当前进程名
     */
    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

    /**
     * 包名判断是否为主进程
     */
    private boolean isMainProcess() {
        return getApplicationContext().getPackageName().equals(getCurrentProcessName());
    }

}
