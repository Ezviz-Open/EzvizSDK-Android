@file:Suppress("UNUSED_PARAMETER")

package com.videogo.ui.others

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.videogo.ui.playback.EZPlayBackListActivity
import com.videogo.ui.realplay.EZRealPlayActivity
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.RootActivity
import kotlinx.android.synthetic.main.activity_collect_play_info.*

class CollectDeviceInfoActivity : RootActivity() {

    /*设备信息*/
    var mDeviceSerial = ""
    var mCameraNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_play_info)
    }

    fun onclickPreView(view: View) {
        if (initAndCheckPlayParam()){
            EZRealPlayActivity.launch(this, mDeviceSerial, mCameraNumber)
        }
    }

    fun onclickPlayback(view: View) {
        if (initAndCheckPlayParam()){
            EZPlayBackListActivity.launch(this, mDeviceSerial, mCameraNumber)
        }
    }

    private fun initAndCheckPlayParam(): Boolean {
        try {
            mDeviceSerial = et_device_serial.text.toString()
            mCameraNumber = et_camera_no.text.toString().toInt()
        }catch (e: Exception){
            e.printStackTrace()
        }
        if (TextUtils.isEmpty(mDeviceSerial) || mCameraNumber < 0){
            showToast("无效的设备信息")
            return false
        }
        return true
    }

}
