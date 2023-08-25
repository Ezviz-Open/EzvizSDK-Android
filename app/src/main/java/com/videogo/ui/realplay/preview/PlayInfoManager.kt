package com.videogo.ui.realplay.preview

/**
 * 此处简要说明此文件用途
 * Created on 2020/4/24
 */
object PlayInfoManager {

    private val mMap = HashMap<String, PlayInfo>()

    fun getAll(): HashMap<String, PlayInfo>{
        return mMap
    }

    fun get(key: String): PlayInfo?{
        return mMap[key]
    }

    fun put(playInfo: PlayInfo){
        mMap[playInfo.key] = playInfo
    }

}