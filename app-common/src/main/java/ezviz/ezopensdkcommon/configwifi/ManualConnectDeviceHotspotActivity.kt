@file:Suppress("UNUSED_PARAMETER")

package ezviz.ezopensdkcommon.configwifi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import com.ezviz.sdk.configwifi.WiFiUtils
import com.hikvision.wifi.configuration.BaseUtil
import ezviz.ezopensdkcommon.R
import ezviz.ezopensdkcommon.common.IntentConstants.*
import ezviz.ezopensdkcommon.common.RootActivity
import kotlinx.android.synthetic.main.activity_manual_connect_device_hotspot.*

class ManualConnectDeviceHotspotActivity : RootActivity() {

    private var mSSID: String? = null
    private var mPWD: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_connect_device_hotspot)
        initUi()
    }

    override fun initUi() {
        super.initUi()
        intent?.apply {
            val prefix = "EZVIZ_"
            mSSID = getStringExtra(DEVICE_HOTSPOT_SSID)
            if(TextUtils.isEmpty(mSSID)){
                mSSID = prefix + getStringExtra(DEVICE_SERIAL)
            }
            mPWD = getStringExtra(DEVICE_HOTSPOT_PWD)
            if(TextUtils.isEmpty(mPWD)){
                mPWD = prefix + getStringExtra(DEVICE_VERIFY_CODE)
            }
            tv_ssid.text = getString(R.string.device_hotspot_ssid) + mSSID
            tv_pwd.text = getString(R.string.device_hotspot_pwd) + mPWD
        }
    }

    override fun onResume() {
        super.onResume()
        checkWifiInfo()
    }

    private fun checkWifiInfo() { // 优先使用getCurrentWifiSsid方法获取wifi名
        var wifiName = WiFiUtils.getCurrentWifiSsid(this)
        // 如上述方式无效，则使用getWifiSSID方法进行获取
        if (!isValidWifiSSID(wifiName)) {
            wifiName = BaseUtil.getWifiSSID(this)
        }
        val isConnected = isValidWifiSSID(wifiName)
        showToast("Connected to device hotspot: $isConnected")
        if (isConnected) {
            intent?.apply {
                putExtra(USE_MANUAL_AP_CONFIG, true)
                ConfigWifiExecutingActivity.launch(mContext, this)
            }
        }
    }

    private fun isValidWifiSSID(wifiName: String): Boolean {
        return mSSID != null && mSSID.equals(wifiName)
    }

    fun onclickGoToWifiSetting(view: View) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
    }

    companion object {
        fun launch(context: Context, intent: Intent?) {
            val newIntent = Intent(context, ManualConnectDeviceHotspotActivity::class.java)
            newIntent.putExtras(intent!!)
            newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(newIntent)
        }
    }

}