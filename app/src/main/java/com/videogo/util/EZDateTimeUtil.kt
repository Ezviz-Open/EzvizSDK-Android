package com.videogo.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * 此处简要说明此文件用途
 * Created on 2020/5/21
 */
object EZDateTimeUtil {

    fun getSimpleTimeInfoForTmpFile(): String?{
        SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.getDefault()).apply {
            return format(Date(System.currentTimeMillis()))
        }
    }

}