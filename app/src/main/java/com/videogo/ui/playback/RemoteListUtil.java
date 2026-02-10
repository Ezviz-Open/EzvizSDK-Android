/**
 * @ProjectName: null
 * @Copyright: null
 * @address: https://www.ys7.com
 * @date: 2014-6-16 下午1:53:03
 * @Description: null
 */
package com.videogo.ui.playback;

import android.text.TextUtils;

import com.videogo.device.DeviceInfoEx;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.util.LocalInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ezviz.ezopensdk.R;

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
            return minStr + ":" + secStr;
        }
    }

    public static String convToUIDuration(CloudPartInfoFile file) {
        int diffSeconds = (int) (file.getEndMillis() - file.getStartMillis()) / 1000;
        int minute = diffSeconds / 60;
        int seconds = diffSeconds % 60;

        return minute + "'" + seconds + "\"";
    }

    public static long converToSecondDuration(Calendar startTime, Calendar stopTime) {
        return (stopTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000;
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
