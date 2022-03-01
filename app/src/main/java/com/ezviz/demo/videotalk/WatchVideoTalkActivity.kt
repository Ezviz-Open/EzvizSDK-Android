package com.ezviz.demo.videotalk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import com.ezviz.sdk.videotalk.*
import com.ezviz.sdk.videotalk.EvcParamValueEnum.EvcOperationEnum
import com.ezviz.videotalk.JNAApi
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.LogUtil
import kotlinx.android.synthetic.main.activity_video_talk.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.thread

class WatchVideoTalkActivity : Activity(){

    companion object {
        /*视频通话服务器域名*/
        private const val SERVER_DOMAIN = "vtm.ys7.com"
        /*视频通话服务器地址*/
        private const val SERVER_PORT = 8554
        /*对应手表联系人ID*/
        private const val SELF_ID = "1234567891"
        private const val TAG = "@@VideoTalkActivity"
        private const val ERROR = -1
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 不支持AP19以下版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            finish()
            runOnUiThread { toast3s("手机版本太低，不支持视频通话功能") }
        }
        setContentView(R.layout.activity_video_talk)
        initData()
        initViews()
        initListeners()
        // 实例化视频通话对象
        EzvizVideoCall(mCameraView as EvcLocalWindowView?, mEvcMsgCallback).run {
            mEzvizVideoCall = this
            setLogPrintEnable(true)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickCreateRoom(view: View) {
        thread { createCall() }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickJoinRoom(view: View) {
        thread { answerCall() }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickLeaveRoom(view: View) {
        thread { mEzvizVideoCall?.stopVideoTalk() }
    }

    /**
     * 初始化通话参数
     */
    private fun initData() {
        mWatchSerial = intent.getStringExtra(InIntentKeysAndValues.KEY_DEVICE_SERIAL)
        mServerDomain = SERVER_DOMAIN
        mServerPort = SERVER_PORT
        mSelfId = SELF_ID
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        mCameraView = findViewById(R.id.view_child_watch_video_talk_camera)
        mPlayerView = findViewById(R.id.view_child_watch_video_talk_player)
    }

    /**
     * 初始化监听器
     */
    private fun initListeners() {
        mPlayerView!!.surfaceTextureListener = mSurfaceTextureListener
        btn_hat_video_caller.setOnClickListener {
            Intent(this@WatchVideoTalkActivity, SafetyHatTalkActivity::class.java).run {
                // 加入通话房间的角色是呼叫方
                putExtra(InIntentKeysAndValues.KEY_ROLE, InIntentKeysAndValues.VALUE_CALLER)
                putExtra(InIntentKeysAndValues.KEY_DEVICE_SERIAL, mWatchSerial)
                putExtra(InIntentKeysAndValues.KEY_SELF_ID, mSelfId)
                putExtra(InIntentKeysAndValues.KEY_SERVER, mServerDomain)
                putExtra(InIntentKeysAndValues.KEY_SERVER_PORT, mServerPort)
                putExtra(InIntentKeysAndValues.KEY_USE_AUDIO, false)
                startActivity(this)
            }
        }
        btn_hat_audio_caller.setOnClickListener {
            Intent(this@WatchVideoTalkActivity, SafetyHatTalkActivity::class.java).run {
                // 加入通话房间的角色是呼叫方
                putExtra(InIntentKeysAndValues.KEY_ROLE, InIntentKeysAndValues.VALUE_CALLER)
                putExtra(InIntentKeysAndValues.KEY_DEVICE_SERIAL, mWatchSerial)
                putExtra(InIntentKeysAndValues.KEY_SELF_ID, mSelfId)
                putExtra(InIntentKeysAndValues.KEY_SERVER, mServerDomain)
                putExtra(InIntentKeysAndValues.KEY_SERVER_PORT, mServerPort)
                putExtra(InIntentKeysAndValues.KEY_USE_AUDIO, true)
                startActivity(this)
            }
        }
    }

    fun dip2px(dipValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    private fun createCall() {
        mCurrentTalkState = TalkStateEnum.CALLER_CALLING
        startVideoTalk(EvcOperationEnum.CALL, 0)
    }

    private fun answerCall() {
        updateRoomId()
        if (isValidRoomId()){
            startVideoTalk(EvcOperationEnum.ANSWER, mInputtedRoomID)
        }
    }

    private fun refuseTalk() {
        updateRoomId()
        if (isValidRoomId()){
            startVideoTalk(EvcOperationEnum.REFUSE, mInputtedRoomID)
        }
    }

    private fun updateRoomId(){
        val inputted = video_talk_et_room_id.text
        return try {
            mInputtedRoomID = inputted.toString().toInt()
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            showToast("无效的房间号")
        }
    }

    private fun isValidRoomId(): Boolean{
        return mInputtedRoomID >= 0
    }

    private fun startVideoTalk(operation: EvcOperationEnum, roomId: Int) {
        if (!hasNeededPermissions()){
            return
        }
        val param = EvcParam()
        param.operation = operation
        param.roomId = roomId
        param.serverIp = mServerDomain
        param.serverPort = mServerPort
        param.selfClientType = EvcParamValueEnum.EvcClientType.ANDROID_PHONE
        param.otherClientType = EvcParamValueEnum.EvcClientType.CHILD_WATCH
        param.streamType = EvcParamValueEnum.EvcStreamType.VIDEO_TALK
        param.otherId = mWatchSerial
        param.selfId = mSelfId
        mEzvizVideoCall?.startVideoTalk(param)
    }

    /**
     * 发起或者加入通话前检查权限
     */
    private fun hasNeededPermissions(): Boolean {
        if(isLackOfCameraPermission){
            showToast(EvcErrorMessage.LACK_OF_CAMERA_PERMISSION)
            return false
        }
        if(isLackOfRecordAudioPermission){
            showToast(EvcErrorMessage.LACK_OF_RECORD_AUDIO_PERMISSION)
            return false
        }
        return true
    }

    private fun showToast(error: EvcErrorMessage) {
        showToast("${error.code}\n${error.desc}")
    }

    private fun showToast(toastMessage: String) {
        LogUtil.e(TAG, toastMessage)
        runOnUiThread {
            toast3s(toastMessage)
        }
    }

    private fun toast3s(toastMessage: String) {
        runOnUiThread {
            if (mCurrentToast != null) {
                mCurrentToast!!.cancel()
                mCurrentToast = null
            }
            val toast = Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG)
            toast.show()
            mCurrentToast = toast
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    if (mCurrentToast === toast) {
                        mCurrentToast!!.cancel()
                    }
                }
            }, 3000)
        }
    }

    private val mEvcMsgCallback: EvcMsgCallback = object : EvcMsgCallback() {
        override fun onMessage(code: Int, desc: String) {
            showToast("onMessage: $code\n$desc")
            if (isFinishing) {
                return
            }
            if (code == 50017) {
                runOnUiThread {
                    AlertDialog.Builder(this@WatchVideoTalkActivity).setMessage(R.string.video_talk_call_is_accepted).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50103 || code == 50106) {
                runOnUiThread {
                    AlertDialog.Builder(this@WatchVideoTalkActivity).setMessage(R.string.video_talk_watch_is_busy).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50104) {
                runOnUiThread {
                    AlertDialog.Builder(this@WatchVideoTalkActivity).setMessage(R.string.video_talk_watch_temperature_high_reject).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50203) {
                runOnUiThread {
                    AlertDialog.Builder(this@WatchVideoTalkActivity).setMessage(R.string.video_talk_watch_temperature_high_hang).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50105) {
                runOnUiThread {
                    AlertDialog.Builder(this@WatchVideoTalkActivity).setMessage(R.string.video_talk_is_playing).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            }else if (code == 10152) {
                isLackOfRecordAudioPermission = true
            }else if (code == 20153) {
                isLackOfCameraPermission = true
            }
        }

        override fun onRcvLucidMsg(msg: String) {
            runOnUiThread {
                try {
                    val json = JSONObject(msg)
                    val type = json.optInt("type", -1)
                    if (type == 0) {
                        showToast(getString(R.string.video_talk_watch_temperature_high_warn))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

//        override fun onJoinRoom(roomId: Int, clientId: Int, username: String) {
////            TODO("Not yet implemented")
//        }

        override fun onJoinRoom(joinInfo: JNAApi.BavJoinInfo) {
//            TODO("Not yet implemented")
        }

        override fun onClientStat(clientId: Int, volume: Int) {
//            TODO("Not yet implemented")
        }

        override fun onClientUpdated(bavJoinInfo: JNAApi.BavJoinInfo?) {
//            TODO("Not yet implemented")
        }

        override fun onQuitRoom(roomId: Int, clientId: Int) {
//            debug("create room, roomId $roomId clientId: $clientId")
        }

        override fun onRoomCreated(roomId: Int) {
            showToast("onRoomCreated: $roomId")
        }

        override fun onCallEstablished(width: Int, height: Int, clientId: Int) {
            runOnUiThread {
                mEzvizVideoCall?.setDisplay(Surface(mPlayerView?.surfaceTexture), clientId)
                if (InIntentKeysAndValues.VALUE_CALLER == mRole) {
                    mCurrentTalkState = TalkStateEnum.CALLER_TALKING
                }
                if (InIntentKeysAndValues.VALUE_ANSWER == mRole) {
                    mCurrentTalkState = TalkStateEnum.ANSWER_TALKING
                }
            }
        }

        override fun onOtherRefused() {
            showToast("对方已拒绝")
            finish()
        }

        override fun onOtherNoneAnswered() {
            if (mCurrentTalkState == TalkStateEnum.CALLER_CALLING && !isFinishing) {
                toast3s(getString(R.string.video_talk_sdk_toast_nobody))
                finish()
            } else if (TalkStateEnum.ANSWER_TALKING == mCurrentTalkState) {
                toast3s(getString(R.string.video_talk_sdk_toast_hang_up))
                refuseTalk()
                finish()
            }
        }

        override fun onOtherHangedUp() {
            toast3s(getString(R.string.video_talk_sdk_toast_hang_up))
            finish()
        }

        override fun onBadNet(delayTimeMs: Int) {
            showToast(getString(R.string.video_talk_signal_weak))
        }
    }

    enum class TalkStateEnum {
        //呼叫方
        CALLER_CALLING,  /*呼叫中*/
        CALLER_TALKING,  /*通话中*/
        CALLER_TALKED,  /*通话后*/ //被呼方
        ANSWER_BEING_CALLED,  /*呼叫中*/
        ANSWER_TALKING,  /*通话中*/
        ANSWER_TALKED /*通话后*/
    }

    @Suppress("unused")
    object InIntentKeysAndValues {
        const val KEY_ROLE = "role"
        const val VALUE_CALLER = 0
        const val VALUE_ANSWER = 1
        const val VALUE_REFUSE = 2
        const val KEY_ROOM_ID = "room_id"
        const val KEY_NICK_NAME = "nick_name"
        const val KEY_HEAD_PORTRAIT_REMOTE = "head_portrait_remote"
        const val KEY_HEAD_PORTRAIT_LOCAL = "head_portrait_remote"
        const val KEY_DEVICE_SERIAL = "device_serial"
        const val KEY_TOKEN = "token"
        const val KEY_SERVER = "server"
        const val KEY_SERVER_PORT = "server_port"
        const val KEY_SELF_ID = "caller_id"
        const val KEY_USE_AUDIO = "key_use_audio"
        const val KEY_ENABLE_VIDEO = "key_enable_video"
        const val KEY_ENABLE_AUDIO = "key_enable_audio"
    }

    override fun onBackPressed() {
        if (TalkStateEnum.CALLER_TALKING == mCurrentTalkState || TalkStateEnum.ANSWER_TALKING == mCurrentTalkState) {
            val ensureDialog = AlertDialog.Builder(this)
                    .setTitle("退出将结束视频聊天")
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("确定") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .create()
            ensureDialog.show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mEzvizVideoCall != null) {
            mEzvizVideoCall?.stopVideoTalk()
        }
        if (TalkStateEnum.CALLER_TALKING == mCurrentTalkState
                || TalkStateEnum.CALLER_CALLING == mCurrentTalkState) {
            if (TalkStateEnum.CALLER_TALKING == mCurrentTalkState) {
                toast3s(getString(R.string.video_talk_sdk_toast_hang_up))
            }
            mCurrentTalkState = TalkStateEnum.CALLER_TALKED
            finish()
        }
        if (TalkStateEnum.ANSWER_TALKING == mCurrentTalkState
                || TalkStateEnum.ANSWER_BEING_CALLED == mCurrentTalkState) {
            if (TalkStateEnum.ANSWER_TALKING == mCurrentTalkState) {
                toast3s(getString(R.string.video_talk_sdk_toast_hang_up))
            }
            mCurrentTalkState = TalkStateEnum.ANSWER_TALKED
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mEzvizVideoCall != null) {
            mEzvizVideoCall?.release()
        }
    }

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener{
        var mLastSurface: SurfaceTexture? = null
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            LogUtil.d(TAG, "onSurfaceTextureAvailable")
            mEzvizVideoCall?.setDisplay(Surface(surface))
            if (mLastSurface == null){
                mEzvizVideoCall?.setDisplay(Surface(surface))
                mLastSurface = surface;
            }else{
                mEzvizVideoCall?.refreshWindow()
            }
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            // do nothing
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            mEzvizVideoCall?.setDisplay(null)
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            mEzvizVideoCall?.setDisplay(Surface(surface))
        }

    }

    private var mRole = ERROR
    private var mInputtedRoomID = ERROR
    private var mCurrentTalkState: TalkStateEnum? = null
    private var mCurrentToast: Toast? = null
    private var mEzvizVideoCall: EzvizVideoCall? = null
    private var mCameraView: View? = null
    private var mPlayerView: TextureView? = null
    // 发起或者接听视频通话的参数
    private var mServerDomain: String? = null
    private var mServerPort = 0
    private var mWatchSerial: String? = null
    private var mSelfId: String? = null
    // 应用权限信息
    private var isLackOfCameraPermission = false
    private var isLackOfRecordAudioPermission = false

}