package ezviz.ezopensdk.debug

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.View
import com.ezviz.sdk.configwifi.finder.DeviceFindCallback
import com.videogo.RootActivity
import com.videogo.openapi.EZGlobalSDK
import com.videogo.openapi.EZOpenSDK
import com.videogo.util.LogUtil
import com.videogo.wificonfig.APWifiConfig
import com.videogo.wificonfig.ConfigWifiErrorEnum
import ezviz.ezopensdk.R
import kotlinx.android.synthetic.main.activity_test_for_sdk.*

class TestActivityForFullSdk : RootActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_for_sdk)

//        sv_test.holder.addCallback(mSvListener)
//
//        tv_test.surfaceTextureListener = mTvListener
//
//        mPlayer.setHandler(mHandler)
//        mPlayer.setPlayVerifyCode(deviceVerifyCode)
//        mPlayer.startRealPlay()

        /** * Enable/Disable SDK logs. */
        EZGlobalSDK.showSDKLog(true);
        /** * Supports P2P streaming or not. See the API for details. */
        EZGlobalSDK.enableP2P(false);
        /** * Replace the APP_KEY as the one you applied.  */
        EZGlobalSDK.initLib(application, APP_KEY);

    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.stopRealPlay()
    }

    private val mPlayer = EZOpenSDK.getInstance().createPlayer(deviceSerial, 1)

    private val mTvListener = object : TextureView.SurfaceTextureListener{

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            mPlayer.setSurfaceEx(null)
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            mPlayer.setSurfaceEx(surface)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

        }

    }

    private val mSvListener = object : SurfaceHolder.Callback{
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            mPlayer.setSurfaceHold(null)
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            mPlayer.setSurfaceHold(holder)
        }

    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message?) {
            LogUtil.e(TAG, "msg is: " + msg.toString())
        }
    }


    fun onClickTest(view: View) {
        showToast("无操作")
    }

    fun onClickStop(view: View) {
        EZGlobalSDK.getInstance().stopAPConfigWifiWithSsid()
    }

    fun onClickStart(view: View) {
        EZGlobalSDK.getInstance().stopAPConfigWifiWithSsid()
        EZGlobalSDK.getInstance().startAPConfigWifiWithSsid(routerWifiName, routerWifiPwd, deviceSerial, deviceVerifyCode,
                deviceHotspotSsid, deviceHotspotPwd, true, object : APWifiConfig.APConfigCallback() {
            override fun onSuccess() {
                LogUtil.e(TAG, "onSuccess")
            }

            override fun OnError(code: Int) {
                LogUtil.e(TAG, "OnError-$code")
                EZOpenSDK.getInstance().stopAPConfigWifiWithSsid()
            }

            override fun onErrorNew(error: ConfigWifiErrorEnum?) {
                LogUtil.e(TAG, "OnError-$error")
            }
        })
    }

    private val mCallback = object : DeviceFindCallback() {
        override fun onFind(deviceSerial: String?) {
            LogUtil.e(TAG, "found $deviceSerial from platform")
        }

        override fun onError(code: Int, msg: String?) {
            LogUtil.e(TAG, "occurred error while querying from platform, error code is $code")
        }
    }

}
