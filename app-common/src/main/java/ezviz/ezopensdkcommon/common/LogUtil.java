package ezviz.ezopensdkcommon.common;

import android.util.Log;

public class LogUtil {


    public static void debugLog(String tag, String content) {
        //托管给xlog
        if (Config.LOGGING) {
//            com.tencent.mars.xlog.Log.d(tag, content);
            Log.i(tag, content);
        }
    }

    public static void errorLog(String tag, String content) {
        // error日志直接打印出来，不检查标志位
        Log.e(tag, content);
//        com.tencent.mars.xlog.Log.e(tag, content);
    }

    public static void infoLog(String tag, String content) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.i(tag, content);
//            com.tencent.mars.xlog.Log.i(tag, content);
        }
    }

    public static void verboseLog(String tag, String content) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.v(tag, content);
//            com.tencent.mars.xlog.Log.v(tag, content);
        }
    }

    public static void warnLog(String tag, String content) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.w(tag, content);
//            com.tencent.mars.xlog.Log.w(tag, content);
        }
    }

    public static void debugLog(String tag, String content, Exception e) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.d(tag, content, e);
//            com.tencent.mars.xlog.Log.d(tag, content,e);
        }
    }

    public static void errorLog(String tag, String content, Exception e) {
        // error日志直接打印出来，不检查标志位
        Log.e(tag, content, e);
//        com.tencent.mars.xlog.Log.e(tag, content,e);
//
    }

    public static void warnLog(String tag, String content, Exception e) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.w(tag, content, e);
//            com.tencent.mars.xlog.Log.w(tag, content,e.getCause());

        }
    }

    public static void warnLog(String tag, Exception ex) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.w(tag, ex);
//            com.tencent.mars.xlog.Log.w(tag, "warnLog",ex.getCause());
        }
    }

    public static void d(String tag, String content) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.d(tag, content);
//            com.tencent.mars.xlog.Log.d(tag, content);
//
        }
    }

    public static void e(String tag, String content) {
        // error日志直接打印出来，不检查标志位
        Log.e(tag, content);
//        com.tencent.mars.xlog.Log.e(tag, content);

    }

    public static void i(String tag, String content) {
        // info日志直接打印出来，不检查标志位
        Log.i(tag, content);
//        com.tencent.mars.xlog.Log.i(tag, content);

    }

    public static void v(String tag, String content) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.v(tag, content);
//            com.tencent.mars.xlog.Log.v(tag, content);
//
        }
    }

    public static void w(String tag, String content) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.w(tag, content);
//            com.tencent.mars.xlog.Log.w(tag, content);
//
        }
    }

    public static void d(String tag, String content, Exception e) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.d(tag, content, e);
//            com.tencent.mars.xlog.Log.d(tag, content,e.getCause());
//
        }
    }


    public static void printErrStackTrace(String tag, Throwable throwable) {
        //托管给xlog
        if (Config.LOGGING && throwable != null && throwable.getMessage() != null) {
//            com.tencent.mars.xlog.Log.printErrStackTrace(tag, throwable,"printErrStackTrace");
                Log.d(tag, throwable.getMessage());
        }
    }
    public static void printErrStackTrace(String tag, Throwable throwable,String format,Object... obj) {
        //托管给xlog
        if (Config.LOGGING) {
//            com.tencent.mars.xlog.Log.printErrStackTrace(tag, throwable,format,obj);
            Log.d(tag, throwable.getMessage());
        }
    }

    public static void e(String tag, String content, Exception e) {
        // error日志直接打印出来，不检查标志位
        Log.e(tag, content, e);
//        com.tencent.mars.xlog.Log.e(tag, content,e.getCause());

    }

    public static void w(String tag, String content, Exception e) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.w(tag, content, e);
//            com.tencent.mars.xlog.Log.w(tag, content,e.getCause());
//
        }
    }

    public static void w(String tag, Exception ex) {
        //托管给xlog
        if (Config.LOGGING) {
            Log.w(tag, ex);
//            com.tencent.mars.xlog.Log.w(tag, "w",ex.getCause());
//
        }
    }
}
