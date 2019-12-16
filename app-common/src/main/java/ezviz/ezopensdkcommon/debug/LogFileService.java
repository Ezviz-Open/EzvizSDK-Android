package ezviz.ezopensdkcommon.debug;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 将当前app的指定日志级别的log写入到目标文件
 * 请务必保证app具有以下权限
 <!--日志读取权限-->
 <uses-permission android:name="android.permission.READ_LOGS" />
 <!--sd卡写入权限-->
 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 */
public class LogFileService {

    private final static String TAG = "@@@" + LogFileService.class.getSimpleName();
    private static boolean started = false;

    public static boolean isStarted(){
        return started;
    }

    public static void start(String logFileNameWithPath){

        Log.w(TAG, "start()");

        final File logFile = new File(logFileNameWithPath);

        // step1: 检查日志文件写入功能是否已经开启
        if (started){
            Log.e(TAG, "LogFileService has started, do not call LogFileService.start() again!");
            return;
        }

        // step2: 检查并创建日志文件所在目录
        if (logFile.isDirectory() && !logFile.delete()){
            Log.e(TAG, "logFile exist, but is a directory!");
            return;
        }
        File logFileFolder = logFile.getParentFile();
        boolean exist = logFileFolder.exists();
        if (!exist){
            exist = logFileFolder.mkdirs();
        }
        if (!exist){
            Log.e(TAG, "logFileFolder can not be created!");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                started = true;
                try {
                    int currentPid = android.os.Process.myPid();
                    Log.w(TAG, "save log of pid" + "(" + currentPid + ")" + " to log file"+
                            "(" + logFile.getPath() +")");
                    // 日志来源
                    String logcatCommand = "logcat *:D --pid=" + currentPid;
                    Log.w(TAG, "logcatCommand is: " + logcatCommand);
                    Process process = Runtime.getRuntime().exec(logcatCommand);
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
                    // 日志输出
                    FileOutputStream fos = new FileOutputStream(logFile, true);
                    String line;
                    Log.w(TAG, "start to write log");
                    while (started) {
                        if ((line = bufferedReader.readLine()) != null){
                            fos.write((line + "\n").getBytes());
                            fos.flush();
                        }
                    }
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void stop(){
        Log.w(TAG, "stop()");
        if (started){
            started = false;
            Log.w(TAG, "stop to write log");
        }
    }
}
