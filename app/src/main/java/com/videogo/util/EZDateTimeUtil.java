package com.videogo.util;

/**
 * Copyright (C) 2025 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2025/8/11 16:28
 */
public class EZDateTimeUtil {

    /**
     * 将总秒数转换为分秒。例如：65秒 -> 01:05
     * @param seconds
     * @return
     */
    public static String convToMinSec(int seconds) {
        // 计算分秒
        int minitue = seconds / 60;
        int second = seconds % 60;
        String formatTime = String.format("%02d:%02d", minitue, second);

        return formatTime;
    }
}
