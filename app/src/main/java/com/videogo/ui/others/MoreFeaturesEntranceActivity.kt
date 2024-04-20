@file:Suppress("UNUSED_PARAMETER")

package com.videogo.ui.others

import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.videogo.constant.Constant
import com.videogo.global.ValueKeys
import com.videogo.openapi.EZGlobalSDK
import com.videogo.openapi.EZOpenSDK
import com.videogo.openapi.EzvizAPI
import com.videogo.ui.LanDevice.LanDeviceActivity
import com.videogo.ui.others.streamctrl.OriginStreamControlActivity
import com.videogo.ui.videotalk.ConfluenceTestEntranceActivity
import com.videogo.ui.videotalk.EZRtcTestActivity
import com.videogo.ui.videotalk.EZVideoMeetingService
import com.videogo.ui.videotalk.MultiTestActivity
import com.videogo.util.LocalInfo
import com.videogo.util.SpTool
import com.videogo.util.Utils
import com.videogo.util.VerifyCodeInput.VerifyCodeInputListener
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.RootActivity
import kotlinx.android.synthetic.main.activity_more_features_entrance.*

class MoreFeaturesEntranceActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_features_entrance)
        title_bar.setTitle("萤石功能测试")
        title_bar.addBackButton(View.OnClickListener { onBackPressed() })
    }

    fun onClickDebug(v: View){
        startActivity(Intent(this, TestActivityForFullSdk::class.java))
    }

    fun onClickGetOriginStream(v: View){
        Intent(this, OriginStreamControlActivity::class.java).run {
            startActivity(this)
        }
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
            val nfIntent = Intent(this, EZRtcTestActivity::class.java) //点击后跳转的界面，可以设置跳转数据
            nfIntent.putExtra(EZVideoMeetingService.LAUNCH_FROM_NOTIFICATION, true)
            startActivity(nfIntent)
        } else {
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

    fun onClickP2pTest(view: View) {
        Intent(this, EZP2pTestActivity::class.java).run {
            startActivity(this)
        }
    }

    fun onClickAutoTest(view: View) {
        AlertDialog.Builder(this)
            .setTitle("是否开启自动化测试")
            .setMessage("萤石SDK测试人员专用，开启后本应用中的账号登录和退出功能将禁用")
            .setPositiveButton("确定") { _, _ ->
                SpTool.storeBooleanValue(ValueKeys.AUTO_TEST, true)
            }
            .setNegativeButton("取消", null)
            .create()
            .show()
    }

    fun onClickP2pOpen(view: View) {
        if (EzvizAPI.getInstance().isUsingGlobalSDK) {
            // 设置是否支持P2P取流,详见api
            EZGlobalSDK.enableP2P(true)
        } else {
            EZOpenSDK.enableP2P(true)
        }
        showToast("p2p已开启")
    }

    fun onClickP2pClose(view: View) {
        if (EzvizAPI.getInstance().isUsingGlobalSDK) {
            // 设置是否支持P2P取流,详见api
            EZGlobalSDK.enableP2P(false)
        } else {
            EZOpenSDK.enableP2P(false)
        }
        showToast("p2p已关闭")
    }

    fun onClickCloudRecordSpaceId(view: View) {
        dialogWithEditText("请输入spaceId") {
            SpTool.storeValue(ValueKeys.SDK_CLOUD_SPACEID, it)
            showToast("SpaceId设置成功")
        }
    }

    /**
     * 用来判断服务是否运行.
     *
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
