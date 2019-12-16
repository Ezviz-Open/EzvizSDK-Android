package ezviz.ezopensdkcommon.configwifi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import ezviz.ezopensdkcommon.R
import ezviz.ezopensdkcommon.common.IntentConstants
import ezviz.ezopensdkcommon.common.RootActivity
import kotlinx.android.synthetic.main.activity_manual_input_device_hotspot_info.*

class ManualInputDeviceHotspotInfoActivity : RootActivity() {

    private val mEzvizDeviceHotspotPrefix = "EZVIZ_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_input_device_hotspot_info)
    }

    fun onClickNext(view: View){
        var deviceHotspotName = app_common_device_hotspot_name.text.toString()
        var deviceHotspotPwd = app_common_device_hotspot_pwd.text.toString()
        if (TextUtils.isEmpty(deviceHotspotName)){
            deviceHotspotName = mEzvizDeviceHotspotPrefix + intent.getStringExtra(IntentConstants.DEVICE_SERIAL)
        }
        if (TextUtils.isEmpty(deviceHotspotPwd)){
            deviceHotspotPwd = mEzvizDeviceHotspotPrefix + intent.getStringExtra(IntentConstants.DEVICE_VERIFY_CODE)
        }
        val jumpIntent = Intent(this, ConfigWifiExecutingActivity::class.java)
        jumpIntent.putExtras(intent)
        jumpIntent.putExtra(IntentConstants.DEVICE_HOTSPOT_SSID, deviceHotspotName)
        jumpIntent.putExtra(IntentConstants.DEVICE_HOTSPOT_PWD, deviceHotspotPwd)
        startActivity(jumpIntent)
    }

}
