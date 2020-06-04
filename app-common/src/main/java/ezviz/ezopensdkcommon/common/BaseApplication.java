package ezviz.ezopensdkcommon.common;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.alibaba.android.arouter.launcher.ARouter;

import ezviz.ezopensdkcommon.debug.LogFileUtil;

public class BaseApplication extends Application {

    public static Application mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

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

    public static void restartApp(Context context){
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            System.exit(0);
        }
    }

}
