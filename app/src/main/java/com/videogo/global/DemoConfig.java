package com.videogo.global;

import com.videogo.EzvizApplication;

/**
 * Demo常用配置
 */
public class DemoConfig {

    // 开发者账号信息
    public final static String APPKEY_DEV = "";
    public final static String TOKEN_DEV = "";
    public final static String DEVICE_DEV = "";

    // JuneCheng's AppKey
    public final static String APPKEY_JC = "";
    public final static String TOKEN_JC = "";
    public final static String DEVICE_JC = "";
    // 配网测试账号
    public final static String WIFI_CONFIG_DEVICE = "";
    public final static String WIFI_CONFIG_VERIFY_CODE = "";

    /**
     * 缓存文件保存位置
     */
    private static final String DEMO_CACHE_FOLDER = EzvizApplication.mInstance.getExternalCacheDir() + "/0_OpenSDK";

    /**
     * 文件保存位置
     */
    private static final String DEMO_FILE_FOLDER = EzvizApplication.mInstance.getExternalFilesDir(null) + "/0_OpenSDK";

    public static String getDemoCacheFolder() {
        return DEMO_CACHE_FOLDER;
    }

    /**
     * 录制 & 录像下载路径
     */
    public static String getRecordsFolder() {
        return DEMO_CACHE_FOLDER + "/Records";
    }

    /**
     * 截图路径
     */
    public static String getCapturesFolder() {
        return DEMO_CACHE_FOLDER + "/Captures";
    }

    /**
     * 码流路径
     */
    public static String getStreamsFolder() {
        return DEMO_CACHE_FOLDER + "/Streams";
    }

    /**
     * 日志路径
     */
    public static String getLogsFolder() {
        return DEMO_FILE_FOLDER + "/Logs";
    }

    /**
     * 崩溃日志路径
     */
    public static String getCrashLogsFolder() {
        return DEMO_FILE_FOLDER + "/CrashLogs";
    }

    /**
     * Sadp日志路径
     */
    public static String getSadpLogsFolder() {
        return DEMO_FILE_FOLDER + "/SadpLogs";
    }
}
