package com.ezviz.demo.common

import android.content.Context
import android.content.Intent
import com.ezviz.demo.streamctrl.OriginStreamControlActivity

/**
 * 此处简要说明此文件用途
 * Created by zhuwen6 on 2020/4/11
 */
object ActivityLaunchManager{

    fun openStreamControlActivity(context: Context){
        OriginStreamControlActivity.launch(context)
    }

}