package ezviz.ezopensdkcommon.debug;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

public class LogFileUtil {

    private final static String TAG = "@@@" + LogFileUtil.class.getSimpleName();

    /**
     * 保存日志文件：启动
     */
    public static void startSaveLogToFile(Context context){
        // 检查存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            Log.e(TAG, "startSaveLogToFile failed: lack of Manifest.permission.WRITE_EXTERNAL_STORAGE");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        String time = String.format(Locale.CHINA, "log_%04d%02d%02d_%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY));
        final String logFileNameWithPath = Environment.getExternalStorageDirectory().getPath() + "/0_OpenSDK/" + time + ".txt";
        LogFileService.start(logFileNameWithPath);
    }

    /**
     * 保存日志文件：停止
     */
    public static void stopSaveLogToFile(){
        LogFileService.stop();
    }

    /**
     * 保存日志文件：获取当前状态
     */
    public static boolean isSavingLogToFile(){
        return LogFileService.isStarted();
    }

}
