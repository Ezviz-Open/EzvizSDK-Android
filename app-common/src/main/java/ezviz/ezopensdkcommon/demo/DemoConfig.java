package ezviz.ezopensdkcommon.demo;

import android.os.Environment;

import ezviz.ezopensdkcommon.common.BaseApplication;

public class DemoConfig {

    /**
     * 是否打开调试页面
     */
    public static final boolean isNeedJumpToTestPage = false;

    /**
     * 文件保存位置
     */
    private static final String DEMO_FOLDER = BaseApplication.mInstance.getExternalCacheDir() + "/0_OpenSDK";

    public static String getDemoFolder(){
        return DEMO_FOLDER;
    }

    public static String getRecordsFolder(){
        return DEMO_FOLDER + "/Records";
    }

    public static String getCapturesFolder(){
        return DEMO_FOLDER + "/Captures";
    }

    public static String getStreamsFolder(){
        return DEMO_FOLDER + "/Streams";
    }

}
