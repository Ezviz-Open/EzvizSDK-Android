package com.videogo.ui.realplay.preview

import android.os.Handler
import android.os.Looper
import com.videogo.EzvizApplication
import com.videogo.openapi.bean.EZCameraInfo
import com.videogo.openapi.bean.EZDeviceInfo
import ezviz.ezopensdkcommon.common.LogUtil
import kotlin.concurrent.thread

/**
 * 此处简要说明此文件用途
 * Created by zhuwen6 on 2020/4/23
 */
class MultiScreenPreviewActivityPresenter {

    fun setCallback(callback: Callback?) {
        mCallback = callback
    }

    fun getDeviceList(){
        LogUtil.i(TAG, "getDeviceList")
        thread {
            try {
                val deviceList = EzvizApplication.getOpenSDK().getDeviceList(0, 50)
                val cameraList = ArrayList<EZCameraInfo>()
                for (deviceInfo in deviceList){
                    deviceInfo.cameraInfoList?.run {
                        cameraList.addAll(this)
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    mCallback?.onReceiveDeviceAndCameraList(deviceList, cameraList)
                }
            } catch (e:Exception) {
                print(e.message)
            }
        }
    }

    interface Callback{
        fun onReceiveDeviceAndCameraList(deviceList:List<EZDeviceInfo>, cameraList: List<EZCameraInfo>)
    }

    private var mCallback: Callback? = null

    companion object{
        private val TAG = MultiScreenPreviewActivityPresenter::class.java.simpleName
    }

}