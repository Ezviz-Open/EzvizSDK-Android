package com.ezviz.demo.videotalk.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import com.ezviz.sdk.videotalk.sdk.EZMeetingCall
import ezviz.ezopensdk.R


class VideoTalkView(context: Context, val mEZMeetingCall: EZMeetingCall) {
    private val TAG = "VideoTalkView"
    val rootView: View
    private val cameraView: TextureView
    private val checkMic: CheckBox
    private val checkVideo: CheckBox
    private val clientIdTv: TextView
    private val usernameTv: TextView
    private val timeTv: TextView
    private val cameraLayout: FrameLayout

    private var clientId: Int = -1
    private var username: String? = null

    private var joinTime: Long = 0
    private var handler: Handler? = null

    private var mContext = context

    init {
        val inflater = LayoutInflater.from(context)
        rootView = inflater.inflate(R.layout.multi_video_talk_item, null)
        cameraView = rootView.findViewById(R.id.camera_view)
        checkMic = rootView.findViewById(R.id.check_mic)
        checkVideo = rootView.findViewById(R.id.check_video)
        clientIdTv = rootView.findViewById(R.id.client_id_tv)
        usernameTv = rootView.findViewById(R.id.username_tv)
        timeTv = rootView.findViewById(R.id.time_tv)
        cameraLayout = rootView.findViewById(R.id.camera_layout)
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val width = wm.defaultDisplay.width
//        val height = wm.defaultDisplay.height
        val lp = cameraLayout.layoutParams
//        lp.width = width / 2
//        lp.height = width / 2
//        cameraLayout.layoutParams = lp
        checkVideo.setOnCheckedChangeListener { _, isChecked ->
            cameraView.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        checkMic.setOnCheckedChangeListener { _, isChecked ->
            if (clientId > 0) {
                mEZMeetingCall.mute(!isChecked, clientId)
            }
        }
        handler = Handler(Looper.getMainLooper())
    }

    fun reset() {
        this.clientId = -1
        checkVideo.isChecked = true
        checkMic.isChecked = true
        clientIdTv.text = "clientId:"
        usernameTv.text = "name:"
        // 停止计时器
        handler?.removeCallbacks(runnable)
    }

    fun joinRoom(clientId: Int, username: String) {
        this.username = username
        this.clientId = clientId
        mEZMeetingCall.showJoinUser(cameraView, clientId)
        clientIdTv.text = "client:$clientId"
        usernameTv.text = "$username"
        timeTv.text = "00:00"
        joinTime = System.currentTimeMillis()
        // 启动计时器
        handler?.postDelayed(runnable, 1000)
    }


    private val runnable: Runnable = object : Runnable {
        override fun run() {
            val dtTime = (System.currentTimeMillis() - joinTime) / 1000
            val min = dtTime / 60
            val seconds = dtTime % 60
            timeTv.text = java.lang.String.format("%02d:%02d", min, seconds)
            handler!!.postDelayed(this, 1000)
        }
    }

    fun leaveRoom() {
        handler?.removeCallbacks(runnable)
        mEZMeetingCall.leaveRoom(this.clientId)
        this.clientId = -1
        this.username = null
        cameraView.surfaceTextureListener = null
    }
}