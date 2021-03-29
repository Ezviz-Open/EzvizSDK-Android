@file:Suppress("UNUSED_PARAMETER")

package com.ezviz.demo.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ezviz.demo.videotalk.MultiTestActivity
import ezviz.ezopensdk.R
import ezviz.ezopensdk.debug.TestActivityForFullSdk
import ezviz.ezopensdkcommon.common.RootActivity

class MoreFeaturesEntranceActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_features_entrance)
    }

    fun onClickDebug(v: View){
        startActivity(Intent(this, TestActivityForFullSdk::class.java))
    }

    fun onClickGetOriginStream(v: View){
        ActivityLaunchManager.openStreamControlActivity(this)
    }

    fun onClickWatchVideoTalk(v: View){
        showToast("暂未启用")
    }

    fun onClickMultiVideoTalk(v: View) {
        Intent(this, MultiTestActivity::class.java).run {
            startActivity(this)
        }
    }

    fun onClickGetStreamFromHub(view: View) {
        Intent(this, CollectDeviceInfoActivity::class.java).run {
            startActivity(this)
        }
    }

}
