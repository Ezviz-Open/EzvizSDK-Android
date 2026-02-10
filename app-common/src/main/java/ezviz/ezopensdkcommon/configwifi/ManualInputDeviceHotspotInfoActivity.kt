package ezviz.ezopensdkcommon.configwifi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import ezviz.ezopensdkcommon.common.Config
import ezviz.ezopensdkcommon.common.IntentConstants
import ezviz.ezopensdkcommon.common.RootActivity
import ezviz.ezopensdkcommon.databinding.ActivityManualInputDeviceHotspotInfoBinding

class ManualInputDeviceHotspotInfoActivity : RootActivity() {

    private lateinit var binding: ActivityManualInputDeviceHotspotInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualInputDeviceHotspotInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        app_common_device_hotspot_name.setText("")
    }

    fun onClickNext(view: View) {
        var deviceHotspotName = binding.appCommonDeviceHotspotName.text.toString()
        var deviceHotspotPwd = binding.appCommonDeviceHotspotPwd.text.toString()
        if (TextUtils.isEmpty(deviceHotspotName)){
            deviceHotspotName = Config.DeviceHotspotPrefix + "_" + intent.getStringExtra(IntentConstants.DEVICE_SERIAL)
        }
        if (TextUtils.isEmpty(deviceHotspotPwd)){
            deviceHotspotPwd = Config.DeviceHotspotPrefix + "_" + intent.getStringExtra(IntentConstants.DEVICE_VERIFY_CODE)
        }
        val jumpIntent = Intent(this, ConfigWifiExecutingActivity::class.java)
        jumpIntent.putExtras(intent)
        jumpIntent.putExtra(IntentConstants.DEVICE_HOTSPOT_SSID, deviceHotspotName)
        jumpIntent.putExtra(IntentConstants.DEVICE_HOTSPOT_PWD, deviceHotspotPwd)
        startActivity(jumpIntent)
    }

}
