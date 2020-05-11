package com.ezviz.demo.common

import ezviz.ezopensdkcommon.common.BaseApplication

/**
 * 此处简要说明此文件用途
 * Created by zhuwen6 on 2020/4/12
 */
object FolderPathManager {

    fun getOriginStreamFolder(): String {
        return "${BaseApplication.mInstance.externalCacheDir}/Streams"
    }

}