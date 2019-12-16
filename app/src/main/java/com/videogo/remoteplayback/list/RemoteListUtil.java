/**
 * @ProjectName: null
 * @Copyright: null
 * @address: https://www.ys7.com
 * @date: 2014-6-16 下午1:53:03
 * @Description: null
 */
package com.videogo.remoteplayback.list;

import android.text.TextUtils;

import com.videogo.device.DeviceInfoEx;
import com.videogo.util.LocalInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ezviz.ezopensdkcommon.R;

//import com.videogo.util.MD5Util;

public class RemoteListUtil {
    private static LocalInfo mLocalInfo = LocalInfo.getInstance();
    private static final String PM = mLocalInfo.getContext().getResources().getString(R.string.pm);
    private static final String AM = mLocalInfo.getContext().getResources().getString(R.string.am);
    private static final String MON = mLocalInfo.getContext().getResources().getString(R.string.month);
    private static final String DAY = mLocalInfo.getContext().getResources().getString(R.string.day);

    public static String convToUIBeginTime(Calendar beginCalender) {
        int i = beginCalender.get(Calendar.HOUR_OF_DAY);
        int m = beginCalender.get(Calendar.MINUTE);

        String uiStr = "";
        if (i > 12) {
            uiStr = PM + (i - 12) + ":" + (m < 10 ? "0" + m : "" + m);
        } else if (i == 12) {
            uiStr = AM + "12:" + (m < 10 ? "0" + m : "" + m);
        } else {
            uiStr = AM + (i < 10 ? "0" + i : "" + i) + ":" + (m < 10 ? "0" + m : "" + m);
        }
        return uiStr;
    }

    public static String convToUIDuration(int diffSeconds) {
        int min = diffSeconds / 60;
        String minStr = "";
        int sec = diffSeconds % 60;
        String secStr = "";
        String hStr = "";

        if (min >= 59) {
            int hour = min / 60;
            int temp = min % 60;
            if (hour < 10) {
                if (hour > 0) {
                    hStr = "0" + hour;
                } else {
                    hStr = "00";
                }
            } else {
                hStr = "" + hour;
            }
            if (temp < 10) {
                if (temp > 0) {
                    minStr = "0" + temp;
                } else {
                    minStr = "00";
                }
            } else {
                minStr = "" + temp;
            }
            if (sec < 10) {
                if (sec > 0) {
                    secStr = "0" + sec;
                } else {
                    secStr = "00";
                }
            } else {
                secStr = "" + sec;
            }
            return hStr + ":" + minStr + ":" + secStr;
        } else {
            hStr = "00";
            if (min < 10) {
                if (min > 0) {
                    minStr = "0" + min;
                } else {
                    minStr = "00";
                }
            } else {
                minStr = "" + min;
            }
            if (sec < 10) {
                if (sec > 0) {
                    secStr = "0" + sec;
                } else {
                    secStr = "00";
                }
            } else {
                secStr = "" + sec;
            }
            return hStr + ":" + minStr + ":" + secStr;
        }
    }

    public static String converTime(Calendar calenderTime) {
        // libCASClient 内部统一进行时间转换
        // 20130605T001020Z->2013-06-25T00:10:20
        // 调用libCASClient接口时必须使用时间格式：20130605T001020Z
        // 请和各位同步更新
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd,HHmmss.");
        String now = sdf.format(calenderTime.getTime());
        String aString = now.replace(',', 'T');
        String bString = aString.replace('.', 'Z');
        return bString;
    }

    public static String converToMonthAndDay(Date queryDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(queryDate);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return month + MON + day + DAY;
    }

    public static String getCloudListItemPicUrl(String srcPicUrl, String keyCheckSum, String passwd) {
        if (!TextUtils.isEmpty(keyCheckSum)) {
            return srcPicUrl + "&x=200" + "&decodekey=" + passwd;
        } else {
            return srcPicUrl + "&x=200";
        }
    }

    public static String getEncryptRemoteListPicPasswd(DeviceInfoEx deviceInfoEx, String cloudKeyChecksum) {

        if (deviceInfoEx == null || TextUtils.isEmpty(cloudKeyChecksum)) {
            return null;
        }
        return null;
    }

}
