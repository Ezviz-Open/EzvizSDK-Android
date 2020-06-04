/*
 * @ProjectName VideoGo
 * @Copyright null
 *
 * @FileName Utils.java
 * @Description This class is the Utils of other classes.
 *
 * @author Fangzhihua
 * @data 2012-10-10
 *
 * @note
 * @note
 *
 * @warning
 */
package ezviz.ezopensdkcommon.common;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is the Utils of other classes.
 *
 * @author Fangzhihua
 * @data 2012-10-10
 */
public class Utils {
    //private static String TAG = "Utils";

    /**
     * <p>
     * 获取网络类型 (原有方法增加注释)
     * </p>
     *
     * @param context
     * @return
     * @author hanlifeng 2014-5-5 下午3:37:51
     */
    public static String getNetTypeName(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo curNetwork = connectivity.getActiveNetworkInfo();
            if (curNetwork != null) {
                int nType = curNetwork.getType();
                if (nType == ConnectivityManager.TYPE_WIFI) {
                    return curNetwork.getTypeName();
                } else if (nType == ConnectivityManager.TYPE_MOBILE) {
                    return curNetwork.getSubtypeName();
                } else {
                    return "UNKNOWN";
                }
            }
        }
        return "UNKNOWN";
    }

    /**
     * <p>
     * 获取客户端版本信息(原有方法增加注释)
     * </p>
     *
     * @param context
     * @return
     * @author hanlifeng 2014-5-5 下午3:34:35
     */
    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }

    public static void showToast(Context context, String text) {
        if (context == null) {
            return;
        }
        if (text != null && !text.equals("")) {
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            try {
                toast.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showToast(Context context, int id, int errCode) {
        if (context == null) {
            return;
        }
        String text = context.getString(id);
        if (errCode != 0) {
            text = text + " (" + errCode + ")";
        }
        if (text != null && !text.equals("")) {
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            try {
                toast.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showToast(Context context, int id) {
        if (context == null) {
            return;
        }
        String text = context.getString(id);
        if (text != null && !text.equals("")) {
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            try {
                toast.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getErrorTip(Context context, int id, int errCode) {
        StringBuffer errorTip = new StringBuffer();
        errorTip.append(context.getString(id));
        if (errCode != 0) {
            errorTip.append(" (").append(errCode).append(")");
        }
        return errorTip.toString();
    }

    public static String getCPUSerial() {
        String str = "", strCPU = "", cpuAddress = "0000000000000000";
        Process pp = null;
        InputStreamReader ir = null;
        LineNumberReader input = null;
        try {
            // 读取CPU信息
            pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            ir = new InputStreamReader(pp.getInputStream());
            input = new LineNumberReader(ir);
            // 查找CPU序列号
            for (int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    // 查找到序列号所在行
                    if (str.indexOf("serial") > -1) {
                        // 提取序列号
                        strCPU = TextUtils.substring(str, str.indexOf(":") + 1, str.length());
                        // 去空格
                        cpuAddress = strCPU.trim();
                        break;
                    }
                } else {
                    // 文件结尾
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                input = null;
            }
            if (ir != null) {
                try {
                    ir.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                ir = null;
            }
            if (pp != null) {
                pp.destroy();
                pp = null;
            }
        }
        return cpuAddress;

    }

    public static String getAndroidID(Context context) {
        String androidID = android.provider.Settings.System.getString(context.getContentResolver(), "android_id");
        LogUtil.d("androidid", androidID);
        return androidID;
    }

    public static void clearAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }


    /**
     * @param is
     * @return
     * @see
     * @since V1.0
     */
    public static String inputStreamToString(InputStream is) {
        BufferedReader in = null;
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            in = new BufferedReader(new InputStreamReader(is));
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {

            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
                in = null;
            }
        }
        return buffer.toString();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void showLog(Context context, String content) {
        if (Config.LOGGING) {
            try {
                Toast.makeText(context, content, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static Calendar parseTimeToCalendar(String strTime) {
        if (strTime == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(date);
        return timeCalendar;
    }

    public static String OSD2Time(Calendar OSDTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(OSDTime.getTimeInMillis());
    }

    public static long get19TimeInMillis(String createTime) {
        Calendar calendar = convert19Calender(createTime);

        return calendar != null ? calendar.getTimeInMillis() : 0;
    }


    public static Calendar convert14Calender(String stringTime) {
        if (stringTime == null || stringTime.length() < 14 || !isNumeric(stringTime)) {
            return null;
        }

        final String year = stringTime.substring(0, 4);
        final String month = stringTime.substring(4, 6);
        final String day = stringTime.substring(6, 8);
        final String hour = stringTime.substring(8, 10);
        final String minute = stringTime.substring(10, 12);
        final String second = stringTime.substring(12, 14);

        try {
            GregorianCalendar calendar = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month) - 1,
                    Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minute), Integer.parseInt(second));
            return calendar;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Calendar convert16Calender(String szStartTime) {
        if (szStartTime == null || szStartTime.length() < 15) {
            return null;
        }
        final String year = szStartTime.substring(0, 4);
        final String month = szStartTime.substring(4, 6);
        final String day = szStartTime.substring(6, 8);
        final String hour = szStartTime.substring(9, 11);
        final String minute = szStartTime.substring(11, 13);
        final String second = szStartTime.substring(13, 15);

        try {
            GregorianCalendar calendar = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month) - 1,
                    Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minute), Integer.parseInt(second));
            return calendar;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Calendar convert19Calender(String createTime) {
        if (createTime == null || createTime.length() < 19) {
            return null;
        }

        try {
            int year = Integer.parseInt(createTime.substring(0, 4));
            int month = Integer.parseInt(createTime.substring(5, 7));
            int day = Integer.parseInt(createTime.substring(8, 10));
            int hourOfDay = Integer.parseInt(createTime.substring(11, 13));
            int minute = Integer.parseInt(createTime.substring(14, 16));
            int second = Integer.parseInt(createTime.substring(17, 19));
            GregorianCalendar calendar = new GregorianCalendar(year, month - 1,
                    day, hourOfDay, minute, second);
            return calendar;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String calendar2String(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(calendar.getTime());
        return dateStr;
    }

    public static String date2String(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(date);
        return dateStr;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private static String trimSpaces(String IP) {// 去掉IP字符串前后所有的空格
        while (IP.startsWith(" ")) {
            IP = IP.substring(1, IP.length()).trim();
        }
        while (IP.endsWith(" ")) {
            IP = IP.substring(0, IP.length() - 1).trim();
        }
        return IP;
    }

    public static boolean isIp(String IP) {// 判断是否是一个IP
        if (IP == null || IP.isEmpty()) {
            return false;
        }
        boolean b = false;
        IP = trimSpaces(IP);
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }
        return b;
    }


    /**
     * 从Assets中读取图片
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            //e.printStackTrace();
            LogUtil.printErrStackTrace("getImageFromAssetsFile",e.fillInStackTrace());
        }

        return image;

    }

    public static Drawable getDrawableFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        InputStream is = null;
        try {
            is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
            is = null;
        } catch (IOException e) {
            //e.printStackTrace();
            LogUtil.printErrStackTrace("getDrawableFromAssetsFile",e.fillInStackTrace());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                is = null;
            }
        }

        if (image != null) {
            byte[] chunk = image.getNinePatchChunk();
            if (chunk != null) {
                boolean result = NinePatch.isNinePatchChunk(chunk);
                NinePatchDrawable patchy = new NinePatchDrawable(image, chunk, new Rect(), null);
                return patchy;
            } else {
                return new BitmapDrawable(image);
            }
        }
        return null;

    }

    /**
     * 设置Selector。
     */
    public static StateListDrawable newSelector(Context context, Drawable normal, Drawable pressed, Drawable focused,
                                                Drawable unable) {
        StateListDrawable bg = new StateListDrawable();
        // View.PRESSED_ENABLED_STATE_SET
        bg.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, pressed);
        // View.ENABLED_FOCUSED_STATE_SET
        bg.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}, focused);
        // View.ENABLED_STATE_SET
        bg.addState(new int[]{android.R.attr.state_enabled}, normal);
        // View.FOCUSED_STATE_SET
        bg.addState(new int[]{android.R.attr.state_focused}, focused);
        // View.WINDOW_FOCUSED_STATE_SET
        bg.addState(new int[]{android.R.attr.state_window_focused}, unable);
        // View.EMPTY_STATE_SET
        bg.addState(new int[]{}, normal);
        return bg;
    }

    /**
     * 对TextView设置不同状态时其文字颜色。
     */
    public static ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
        int[] colors = new int[]{pressed, focused, normal, focused, unable, normal};
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[2] = new int[]{android.R.attr.state_enabled};
        states[3] = new int[]{android.R.attr.state_focused};
        states[4] = new int[]{android.R.attr.state_window_focused};
        states[5] = new int[]{};
        ColorStateList colorList = new ColorStateList(states, colors);
        return colorList;
    }

    public static String getUrlValue(String url, String startStr, String endStr) {
        if (url == null || startStr == null) {
            return null;
        }
        int startIndex = url.indexOf(startStr);
        if (startIndex < 0) {
            return null;
        }
        int endIndex = endStr != null ? url.indexOf(endStr, startIndex) : url.length();
        if (startIndex >= endIndex) {
            endIndex = url.length();
        }

        try {
            return url.substring(startIndex + startStr.length(), endIndex);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }


    // content of file /sdcard/videogo_test_cfg:
    // deviceSerial:427734168
    public static void parseTestConfigFile(String filePath, Map<String, String> map) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String lineStr;
            lineStr = br.readLine();
            while (lineStr != null) {
                String[] values = lineStr.split("\\$");
                if (values.length == 2) {
                    map.put(values[0], values[1]);
                }
                lineStr = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static boolean isEZOpenProtocol(String url){
        if (url.startsWith("ezopen://")){
            return true;
        }
        return false;
    }


    private static Map<String, String[]> getParamsMap(String queryString, String enc) {
        Map<String, String[]> paramsMap = new HashMap<String, String[]>();
        if (queryString != null && queryString.length() > 0) {
            int ampersandIndex, lastAmpersandIndex = 0;
            String subStr, param, value;
            String[] paramPair, values, newValues;
            do {
                ampersandIndex = queryString.indexOf('&', lastAmpersandIndex) + 1;
                if (ampersandIndex > 0) {
                    subStr = queryString.substring(lastAmpersandIndex, ampersandIndex - 1);
                    lastAmpersandIndex = ampersandIndex;
                } else {
                    subStr = queryString.substring(lastAmpersandIndex);
                }
                paramPair = subStr.split("=");
                param = paramPair[0];
                value = paramPair.length == 1 ? "" : paramPair[1];
                try {
                    value = URLDecoder.decode(value, enc);
                } catch (UnsupportedEncodingException ignored) {
                }
                if (paramsMap.containsKey(param)) {
                    values = paramsMap.get(param);
                    int len = values.length;
                    newValues = new String[len + 1];
                    System.arraycopy(values, 0, newValues, 0, len);
                    newValues[len] = value;
                } else {
                    newValues = new String[] { value };
                }
                paramsMap.put(param, newValues);
            } while (ampersandIndex > 0);
        }
        return paramsMap;
    }

    /**
     * 根据URL获得文件名
     *
     * @param url URL
     */
    public static String getFileNameByUrl(String url) {
        String name = null;
        if (url != null) {
            int start = url.lastIndexOf("/");
            int end = url.lastIndexOf("?");
            name = url.substring(start == -1 ? 0 : start + 1, end == -1 ? url.length() : end);
        }
        return name;
    }
}
