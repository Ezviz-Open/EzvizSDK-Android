package com.videogo.ui.devicelist

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.videogo.ui.playback.EZPlayBackListActivity
import com.videogo.ui.realplay.EZRealPlayActivity
import ezviz.ezopensdk.databinding.ActivityHubDebugBinding
import ezviz.ezopensdkcommon.common.RootActivity

class EZHubDebugActivity : RootActivity() {

    private lateinit var binding: ActivityHubDebugBinding

    /*设备信息*/
    var mDeviceSerial = ""
    var mCameraNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHubDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onclickPreView(view: View) {
        if (initAndCheckPlayParam()) {
            EZRealPlayActivity.launch(this, mDeviceSerial, mCameraNumber)
        }
    }

    fun onclickPlayback(view: View) {
        if (initAndCheckPlayParam()) {
            EZPlayBackListActivity.launch(this, mDeviceSerial, mCameraNumber)
        }
    }

    private fun initAndCheckPlayParam(): Boolean {
        try {
            mDeviceSerial = binding.etDeviceSerial.text.toString()
            mCameraNumber = binding.etCameraNo.text.toString().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (TextUtils.isEmpty(mDeviceSerial) || mCameraNumber < 0) {
            showToast("无效的设备信息")
            return false
        }
        return true
    }

}