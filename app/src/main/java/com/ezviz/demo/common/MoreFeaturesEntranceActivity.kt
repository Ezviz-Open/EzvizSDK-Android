@file:Suppress("UNUSED_PARAMETER")

package com.ezviz.demo.common

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.ezviz.demo.videotalk.ConfluenceActivity
import com.ezviz.demo.videotalk.ConfluenceTestEntranceActivity
import com.ezviz.demo.videotalk.EZVideoMeetingService
import com.ezviz.demo.videotalk.MultiTestActivity
import com.videogo.ui.LanDevice.LanDeviceActivity
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

    fun onClickMeeting(view: View) {

        //判断当前会议是否正在进行中
        if (isServiceRunning(EZVideoMeetingService::class.java.name)){
            val nfIntent = Intent(this, ConfluenceActivity::class.java) //点击后跳转的界面，可以设置跳转数据
            nfIntent.putExtra(EZVideoMeetingService.LAUNCH_FROM_NOTIFICATION, true)
            startActivity(nfIntent)
        }else{
            Intent(this, ConfluenceTestEntranceActivity::class.java).run {
                startActivity(this)
            }
        }

    }

    fun onClickLanDevice(view: View) {
        Intent(this, LanDeviceActivity::class.java).run {
            startActivity(this)
        }

    }

    /**
     * 用来判断服务是否运行.
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    private fun isServiceRunning(className: String): Boolean {
        var isRunning = false
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val serviceList = activityManager.getRunningServices(30)
        if (serviceList.size <= 0) {
            return false
        }
        for (i in serviceList.indices) {
            Log.d(TAG, "SERVICE NAME " + serviceList[i].service.className)
            if (serviceList[i].service.className == className) {
                isRunning = true
                break
            }
        }
        return isRunning
    }

}
