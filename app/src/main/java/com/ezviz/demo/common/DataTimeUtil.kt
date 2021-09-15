package com.ezviz.demo.common

import java.text.SimpleDateFormat
import java.util.*

/**
 * 此处简要说明此文件用途
 * Created on 2020/5/21
 */
object DataTimeUtil {

    fun getSimpleTimeInfoForTmpFile(): String?{
        SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.getDefault()).apply {
            return format(Date(System.currentTimeMillis()))
        }
    }

}