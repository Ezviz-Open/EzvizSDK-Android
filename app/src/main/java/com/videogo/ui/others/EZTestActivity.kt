@file:Suppress("UNUSED_PARAMETER")

package com.videogo.ui.others

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.videogo.global.GlobalKit
import com.videogo.global.ValueKeys
import com.videogo.openapi.EZConstants
import com.videogo.ui.LanDevice.LanDeviceActivity
import com.videogo.util.DataManager
import com.videogo.util.SpTool
import ezviz.ezopensdk.databinding.ActivityMoreFeaturesEntranceBinding
import ezviz.ezopensdkcommon.common.RootActivity
import kotlin.math.floor

class EZTestActivity : RootActivity() {

    private lateinit var binding: ActivityMoreFeaturesEntranceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreFeaturesEntranceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.setTitle("萤石功能测试")
        binding.titleBar.addBackButton(View.OnClickListener { onBackPressed() })
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

    fun onClickStreamTypeSetting(view: View) {
        Intent(this, EZStreamTypeSettingActivity::class.java).run {
            startActivity(this)
        }
    }

    fun onClickCloudRecordSpaceId(view: View) {
        dialogWithEditText("请输入spaceId") {
            SpTool.storeValue(ValueKeys.SDK_CLOUD_SPACEID, it)
            showToast("SpaceId设置成功")
        }
    }

    fun onClickTKToken(view: View) {
        Intent(this, EZTKTokenActivity::class.java).run {
            startActivity(this)
        }
    }

    fun onClickInterfaceTest(view: View) {
        Intent(this, EZInterfaceTestActivity::class.java).run {
            startActivity(this)
        }
    }

    fun onClickVerifycodeClear(view: View) {
        AlertDialog.Builder(this)
            .setTitle("是否清空所有设备验证码")
            .setPositiveButton("确定") { _, _ ->
                DataManager.getInstance().clearAllDeviceSerialVerifyCode();
                showToast("所有设备验证码已清空")
            }
            .setNegativeButton("取消", null)
            .create()
            .show()
    }

    fun onClickVideoQuality(view: View) {
        val max = 2;
        val min = 0
        // 取 0 1 2 随机数
        when ((floor(Math.random() * (max - min + 1)) + min).toInt()) {
            0 -> {
                GlobalKit.getInstance().videoQuality = EZConstants.EZVideoQuality.VIDEO_PRIORITY_NONE
                showToast("已恢复默认，取消性能优先或画质优先")
            }
            1 -> {
                GlobalKit.getInstance().videoQuality = EZConstants.EZVideoQuality.VIDEO_PERFORMANCE_PRIORITY
                showToast("已设置为性能优先")
            }
            2 -> {
                GlobalKit.getInstance().videoQuality = EZConstants.EZVideoQuality.VIDEO_QUALITY_PRIORITY
                showToast("已设置为画质优先")
            }
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
