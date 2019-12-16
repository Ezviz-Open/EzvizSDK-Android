/* 
 * @ProjectName VideoGo
 * @Copyright null
 * 
 * @FileName DeviceUtil.java
 * @Description 这里对文件进行描述
 * 
 * @author chengjuntao
 * @data 2014-7-14
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.devicelist;

import android.text.TextUtils;


public class DeviceUtil {
    public static boolean isSupportWifi(String model) {
        // CS-C1-1WPFR
        if (TextUtils.isEmpty(model)) {
            return false;
        }

        String[] arryString = model.split("-");

        if (arryString == null || arryString.length < 3) {
            return false;
        }

        if (!arryString[2].contains("W")) {
            return false;
        }

        if (arryString[1].contains("D") || arryString[1].contains("R") || arryString[1].contains("N")) {
            return false;
        }
        return true;
    }

    public static boolean isSupportNetWork(String model) {
        // CS-C1-1WPFR
        if (TextUtils.isEmpty(model)) {
            return false;
        }

        String[] arryString = model.split("-");

        if (arryString == null || arryString.length < 3) {
            return false;
        }
        if (arryString[1].contains("F") || arryString[1].contains("A")) {
            return false;
        }
        return true;
    }
}
