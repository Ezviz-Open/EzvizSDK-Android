package ezviz.ezopensdk.debug

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.videogo.openapi.EzvizAPI
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.RootActivity
import kotlinx.android.synthetic.main.activity_test_for_sdk.*

class TestActivityForFullSdk : RootActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_for_sdk)

        /*以下调试代码不可删除*/
        tv_sdk_info.text = "以下调试代码不可删除\n" +
                "isUsingGlobalSDK: " + EzvizAPI.getInstance()?.isUsingGlobalSDK + "\n" +
                "以上调试代码不可删除"
        /*以上调试代码不可删除*/
    }

    fun onClickTest(view: View) {
        showToast("无操作")
//        startActivity(Intent(this, OriginStreamControlActivity::class.java))
//        EZStreamClientManager.create(applicationContext).clearPreconnectInfo("C92140427")
    }

    fun onClickStop(view: View) {
        showToast("无操作")
    }

    fun onClickStart(view: View) {
        showToast("无操作")
    }

    companion object{
        const val TAG = "@@zhuwen"
        const val APP_KEY = "test1234"

        const val routerWifiName = "test"
        const val routerWifiPwd = "12345687"
        const val deviceSerial = "C54348757"
        const val deviceVerifyCode = "FTFPKL"
        private const val configWifiPrefix = "SoftAp_"
        const val deviceHotspotSsid = configWifiPrefix + deviceSerial
        const val deviceHotspotPwd = configWifiPrefix + deviceVerifyCode
    }

}
