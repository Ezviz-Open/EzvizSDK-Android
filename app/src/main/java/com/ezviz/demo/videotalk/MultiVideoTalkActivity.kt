package com.ezviz.demo.videotalk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import com.ezviz.demo.videotalk.widget.VideoTalkView
import com.ezviz.sdk.videotalk.*
import com.ezviz.sdk.videotalk.EvcParamValueEnum.EvcOperationEnum
import com.ezviz.sdk.videotalk.sdk.EZMeetingCall
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.LogUtil
import ezviz.ezopensdkcommon.common.Utils
import kotlinx.android.synthetic.main.activity_multi_video_talk.*
import kotlinx.android.synthetic.main.activity_multi_video_talk_test.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.thread

class MultiVideoTalkActivity : Activity() {

    companion object {
        private const val TAG = "@@MultiVideoTalkActivity"
        /*视频通话服务器域名*/
//        private const val SERVER_DOMAIN = "vtm.ys7.com"
        private const val SERVER_DOMAIN = "test12.ys7.com"

        /*视频通话服务器地址*/
        private const val SERVER_PORT = 8554

        /*对应手表联系人ID*/
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
        setContentView(R.layout.activity_multi_video_talk)
        initData()
        initListeners()
        // 实例化视频通话对象
        /**
         * EvcLocalWindowView 本机视图
         * nickname 当前设备在会议中显示的名称
         * enableCamera 进入会议时是否开启相机
         * enableMic 进入会议时是否开启麦克风
         * EZMeetingCall.CallBack 会议通话回调
         */
        EZMeetingCall(camera_view_my, mSelfId?:"", check_video_my.isChecked, check_mic_my.isChecked, mMettingCallback).run {
            mEZMeetingCall = this
            this.setLogPrintEnable(true)
        }

        Handler().postDelayed({
            if (mIsCall) {
                createMeeting()
            } else {
                joinMeeting()
            }
        }, 1000)
    }

    /**
     * 初始化通话参数
     */
    private fun initData() {
        mServerDomain = SERVER_DOMAIN
        mServerPort = SERVER_PORT
        mIsCall = intent.getBooleanExtra("is_call", true)
        mDeviceSerial = intent.getStringExtra("device_id")
        mChannelId = intent.getIntExtra("channel_id", 1)
        mSelfId = intent.getStringExtra("username")
        mPassword = intent.getStringExtra("password")
        mInputtedRoomID = intent.getIntExtra("room_id", 0)
        mIsCallDevice = intent.getBooleanExtra("is_call_device", false)

        check_video_my.isChecked = intent.getBooleanExtra("enable_video", false)
        check_mic_my.isChecked = intent.getBooleanExtra("enable_audio", true)

        if (!mIsCall) {
            room_id_text.text = mInputtedRoomID.toString()
        }
        username_text.text = mSelfId
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickLeaveRoom(view: View) {
        finish()
    }


    /**
     * 初始化监听器
     */
    private fun initListeners() {
        check_video_my.setOnCheckedChangeListener{ _: CompoundButton, isChecked: Boolean ->
            mEZMeetingCall?.enableCamera(isChecked)
        }
        check_mic_my.setOnCheckedChangeListener { _, isChecked ->
            mEZMeetingCall?.enableMic(isChecked)
        }
        camera_switch_btn.setOnClickListener {
            mEZMeetingCall?.switchCamera()
        }
    }

    private fun createMeeting() {
        if (mIsCallDevice) {
            mEZMeetingCall?.createMeetingWithDevice(mDeviceSerial?:"", mChannelId, mPassword)
        } else {
            mEZMeetingCall?.createMeeting(mPassword)
        }
    }

    private fun joinMeeting() {
        if (isValidRoomId()) {
            mEZMeetingCall?.joinMeeting(mInputtedRoomID, mPassword)
        }
    }



    private fun isValidRoomId(): Boolean {
        return mInputtedRoomID >= 0
    }

    override fun onPause() {
        super.onPause()
        mEZMeetingCall?.enableCamera(false)
    }

    override fun onRestart() {
        super.onRestart()
        mEZMeetingCall?.enableCamera(true)
    }

    /**
     * 发起或者加入通话前检查权限
     */
    private fun hasNeededPermissions(): Boolean {
        if (isLackOfCameraPermission) {
            showToast(EvcErrorMessage.LACK_OF_CAMERA_PERMISSION)
            return false
        }
        if (isLackOfRecordAudioPermission) {
            showToast(EvcErrorMessage.LACK_OF_RECORD_AUDIO_PERMISSION)
            return false
        }
        return true
    }

    private fun showToast(error: EvcErrorMessage) {
        showToast("${error.code}\n${error.desc}")
    }

    private fun showToast(toastMessage: String) {
        LogUtil.d(TAG, toastMessage)
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

    private val mMettingCallback = object: EZMeetingCall.CallBack {
        override fun onError(code: Int, message: String) {
            showToast("onError: $code\n$message")
            if (isFinishing) {
                return
            }
            if (code == 50017) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_call_is_accepted).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50103 || code == 50106) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_watch_is_busy).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50104) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_watch_temperature_high_reject).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50203) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_watch_temperature_high_hang).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50105) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_is_playing).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 10152) {
                isLackOfRecordAudioPermission = true
            } else if (code == 20153) {
                isLackOfCameraPermission = true
            } else if (code == 50007 || code == 50005) {
                showToast("网络断开，退出房间")
                finish()
            } else if(code == EvcErrorMessage.MULTI_CALL_FAILED_DEVICE_ENCRYPT.code){
                showToast("不支持加密设备，退出房间")
                finish()
            }
        }

        override fun onRoomCreated(roomId: Int) {
            showToast("onRoomCreated: $roomId")
            runOnUiThread {
                room_id_text.text = roomId.toString()
            }
        }

        override fun onJoinRoom(roomId: Int, clientId: Int, username: String) {
            runOnUiThread {
                joinRoom(clientId, username)
            }
        }

        override fun onFirstFrameDisplayed(width: Int, height: Int, clientId: Int) {
//            showToast("onFirstFrameDisplayed: $roomId")
        }

        override fun onQuitRoom(roomId: Int, clientId: Int) {
            runOnUiThread {
                quitRoom(clientId)
            }
        }

        override fun onBadNet(delayTimeMs: Int) {
            runOnUiThread {
                showToast(getString(R.string.video_talk_signal_weak))
            }
        }
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
        if (mEZMeetingCall?.isTalking() == true) {
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

    override fun onDestroy() {
        if (mEZMeetingCall != null) {
            mEZMeetingCall?.quitMeeting()
            mEZMeetingCall = null
        }
        super.onDestroy()
    }

    private fun joinRoom(clientId: Int, username: String) {
        if (clientInRoomMap.keys.contains(clientId)) {
            showToast("该用户已加入房间")
            return
        }
        val view = getFreeVideoTalkView()
        vg_child_watch_video_talk_video_container.addView(view.rootView)
        view.joinRoom(clientId, username)
        clientInRoomMap[clientId] = view
    }

    private fun quitRoom(clientId: Int) {
        if (!clientInRoomMap.keys.contains(clientId)) {
            showToast("该用户已不在房间")
            return
        }
        runOnUiThread {
            val view = clientInRoomMap[clientId]
            if (view != null) {
                vg_child_watch_video_talk_video_container.removeView(view.rootView)
                view.leaveRoom()
                clientInRoomMap.remove(clientId)
                freeVideoTalkView.push(view)
            }
        }
    }

    private fun getFreeVideoTalkView(): VideoTalkView {
        val view = if (freeVideoTalkView.isEmpty()) {
            VideoTalkView(this, mEZMeetingCall!!)
        } else {
            freeVideoTalkView.pop()
        }
//        val view = VideoTalkView(this, mEzvizVideoCall!!)
        view.reset()
        return view
    }


    private val clientInRoomMap = hashMapOf<Int, VideoTalkView>()
    private val freeVideoTalkView = Stack<VideoTalkView>()

    private var mRole = ERROR
    private var mCurrentToast: Toast? = null
    private var mEZMeetingCall: EZMeetingCall? = null

    // 应用权限信息
    private var isLackOfCameraPermission = false
    private var isLackOfRecordAudioPermission = false

    // 发起或者接听视频通话的参数
    private var mServerDomain: String? = null
    private var mServerPort = 0

    /**
     * 显示的用户名
     */
    private var mSelfId: String? = null
    /**
     * 是否是呼叫，否则是加入
     */
    private var mIsCall = true

    /**
     * 是否呼叫设备，否则只创建房间
     */
    private var mIsCallDevice = false

    /**
     * mIsCallDevice = true 时
     * 需要呼叫的设备
     */
    private var mDeviceSerial: String? = null
    /**
     * mIsCallDevice = true 时
     * 需要呼叫的设备通道
     */
    private var mChannelId: Int = 1
    /**
     * 是否使用自定义密码校验
     */
    private var mPassword: String? = null
    /**
     * 加入的房间号
     */
    private var mInputtedRoomID = ERROR

}