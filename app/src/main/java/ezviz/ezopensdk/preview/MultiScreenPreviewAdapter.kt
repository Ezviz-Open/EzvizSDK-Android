package ezviz.ezopensdk.preview

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.videogo.EzvizApplication
import com.videogo.exception.ErrorCode.ERROR_INNER_VERIFYCODE_ERROR
import com.videogo.openapi.EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL
import com.videogo.openapi.EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS
import com.videogo.openapi.bean.EZCameraInfo
import com.videogo.openapi.bean.EZDeviceInfo
import com.videogo.ui.realplay.EZRealPlayActivity
import com.videogo.ui.util.DataManager
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.LogUtil
import kotlin.concurrent.thread

/**
 * 此处简要说明此文件用途
 * Created by zhuwen6 on 2020/4/23
 */
class MultiScreenPreviewAdapter(private val mContext: Context, private val mDeviceList: List<EZDeviceInfo>,
                                private val mCameraList: List<EZCameraInfo>?, private var mColumn: Int = 1) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun stopAll() {
        for (playInfo in PlayInfoManager.getAll().values) {
            playInfo.player?.stopRealPlay()
        }
    }

    fun startAll() {
        for (playInfo in PlayInfoManager.getAll().values) {
            playInfo.player?.startRealPlay()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        LogUtil.d(TAG, "onCreateViewHolder")
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_multi_screen_preview, viewGroup, false).apply {
            layoutParams.width = viewGroup.width / mColumn
            layoutParams.height = layoutParams.width / 16 * 9
        }
        return MultiScreenViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        LogUtil.d(TAG, "onBindViewHolder")
        val holder = viewHolder as MultiScreenViewHolder
        mCameraList?.get(i)?.apply {
            LogUtil.d(TAG, "onBindViewHolder: $cameraName")
            val playInfoKey = getPlayInfoKeyBy(this)
            var playInfo = PlayInfoManager.get(playInfoKey)
            if (playInfo == null) {
                var deviceInfo: EZDeviceInfo? = null
                for (device in mDeviceList) {
                    if (device.deviceSerial == deviceSerial) {
                        deviceInfo = device
                        break
                    }
                }
                playInfo = PlayInfo(playInfoKey, deviceInfo!!, this, this@MultiScreenPreviewAdapter, holder)
                PlayInfoManager.put(playInfo)
            }
            playInfo.holder = holder
            if (playInfo.deviceInfo.status == 1) {
                changePlayViewsBy(PlayStatusEnum.LOADING, playInfoKey)
            } else {
                changePlayViewsBy(PlayStatusEnum.OFFLINE, playInfoKey)
            }
        }
    }

    fun changePlayViewsBy(playStatus: PlayStatusEnum, playInfoKey: String) {
        val playInfo = PlayInfoManager.get(playInfoKey)
        if (playInfo == null) {
            LogUtil.e(TAG, "changePlayViewsBy: return")
            return
        }
        playInfo.playStatus = playStatus
        val viewHolder = playInfo.holder
        val cameraInfo = playInfo.cameraInfo
        viewHolder.apply {
            cameraNameTv?.text = cameraInfo.cameraName
            if (playStatus != PlayStatusEnum.OFFLINE) {
                playWindowTextureView?.surfaceTextureListener = SurfaceTextureListerWithControlPreview(playInfoKey)
            }
        }
        when (playStatus) {
            PlayStatusEnum.OFFLINE -> {
                viewHolder.playLoadingPb?.visibility = View.GONE
                viewHolder.playErrorTv?.apply {
                    visibility = View.VISIBLE
                    text = "设备不在线"
                }
            }
            PlayStatusEnum.LOADING -> {
                viewHolder.playLoadingPb?.visibility = View.VISIBLE
                viewHolder.playErrorTv?.apply {
                    visibility = View.GONE
                    setOnClickListener(ErrorMessageOnClickListener(playInfoKey))
                }
                viewHolder.itemView.setOnClickListener(PlayWindowOnClickListener(playInfoKey))
            }
            PlayStatusEnum.PLAYING -> {
                viewHolder.playLoadingPb?.visibility = View.GONE
                viewHolder.playErrorTv?.apply {
                    visibility = View.GONE
                }?.setOnClickListener(null)
            }
            PlayStatusEnum.ERROR_VERIFY_CODE -> {
                viewHolder.playLoadingPb?.visibility = View.GONE
                viewHolder.playErrorTv?.apply {
                    visibility = View.VISIBLE
                    text = "视频已加密"
                }
            }
            else -> {
                LogUtil.e(TAG, "未知状态")
            }
        }
    }

    override fun getItemCount(): Int {
        val cnt = mCameraList?.size ?: 0
        LogUtil.d(TAG, "getItemCount: $cnt")
        return cnt
    }

    class PlayWindowOnClickListener(private val mPlayInfoKey: String) : View.OnClickListener {

        override fun onClick(view: View?) {
            val context = view?.context!!
            val playInfo = PlayInfoManager.get(mPlayInfoKey)
            if (playInfo?.playStatus == PlayStatusEnum.PLAYING) {
                EZRealPlayActivity.launch(context, playInfo.deviceInfo, playInfo.cameraInfo)
            } else {
                LogUtil.d(TAG, "非播放状态，点击无效")
            }
        }

    }

    class ErrorMessageOnClickListener(private val mPlayInfoKey: String) : View.OnClickListener {

        override fun onClick(view: View?) {
            val playInfo = PlayInfoManager.get(mPlayInfoKey)
            when (playInfo?.lastError) {
                ERROR_INNER_VERIFYCODE_ERROR -> {
                    val context = view?.context!!
                    val verifyCodeEt = EditText(context)
                    AlertDialog.Builder(context)
                            .setTitle("请输入验证码")
                            .setView(verifyCodeEt)
                            .setPositiveButton("确定") { dialog, _ ->
                                run {
                                    val verifyCode = verifyCodeEt.text.toString()
                                    if (TextUtils.isEmpty(verifyCode)) {
                                        Toast.makeText(context, "请输入有效的验证码", Toast.LENGTH_SHORT).show()
                                    } else {
                                        dialog.dismiss()
                                        playInfo.verifyCode = verifyCode
                                        playInfo.adapter.changePlayViewsBy(PlayStatusEnum.LOADING, mPlayInfoKey)
                                        playInfo.player?.apply {
                                            setPlayVerifyCode(verifyCode)
                                            startRealPlay()
                                        }
                                    }
                                }
                            }
                            .show()
                }
                else -> {
                    LogUtil.d(TAG, "无需处理此点击事件")
                }
            }
        }

    }

    class SurfaceTextureListerWithControlPreview(private val mPlayInfoKey: String) : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            releasePlayer()
            return true
        }

        private fun releasePlayer() {
            LogUtil.d(TAG, "stopPlay")
            thread {
                PlayInfoManager.get(mPlayInfoKey)?.player?.apply {
                    setHandler(null)
                    release()
                }
            }
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            initPlayer(surface)
        }

        private fun initPlayer(surface: SurfaceTexture?) {
            LogUtil.d(TAG, "startPlay")
            thread {
                val playInfo = PlayInfoManager.get(mPlayInfoKey)
                EzvizApplication.getOpenSDK().createPlayer(playInfo?.cameraInfo?.deviceSerial, playInfo?.cameraInfo?.cameraNo!!, true)?.apply {
                    setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(playInfo.cameraInfo.deviceSerial))
                    playInfo.player = this
                    Handler(Looper.getMainLooper()).post {
                        PlayMessageHandler(mPlayInfoKey).apply {
                            playInfo.handler = this
                            setHandler(this)
                        }
                    }
                    setSurfaceEx(surface)
                    startRealPlay()
                }
            }
        }
    }

    class PlayMessageHandler(private val mPlayInfoKey: String) : Handler() {
        override fun handleMessage(msg: Message) {
            LogUtil.d(TAG, "handleMessage" + msg.toString())
            when (msg?.what) {
                // 播放成功
                MSG_REALPLAY_PLAY_SUCCESS -> {
                    PlayInfoManager.get(mPlayInfoKey)?.apply {
                        adapter.changePlayViewsBy(PlayStatusEnum.PLAYING, mPlayInfoKey)
                    }
                }
                // 播放失败
                MSG_REALPLAY_PLAY_FAIL -> {
                    val errCd = msg.arg1
                    PlayInfoManager.get(mPlayInfoKey)?.apply {
                        lastError = errCd
                        when (errCd) {
                            // 验证码错误
                            ERROR_INNER_VERIFYCODE_ERROR -> adapter.changePlayViewsBy(PlayStatusEnum.ERROR_VERIFY_CODE, mPlayInfoKey)
                            else -> LogUtil.e(TAG, "未知错误，摄像头：$mPlayInfoKey, 错误码：$errCd")
                        }
                    }
                }
            }
        }
    }

    enum class PlayStatusEnum() {
        LOADING, PLAYING, PAUSED, OFFLINE, ERROR_VERIFY_CODE
    }

    class MultiScreenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var playWindowTextureView: TextureView? = null
        var cameraNameTv: TextView? = null
        var playLoadingPb: ProgressBar? = null
        var playErrorTv: TextView? = null

        init {
            findViews()
        }

        private fun findViews() {
            itemView.apply {
                playWindowTextureView = findViewById(R.id.texture_view_play_window)
                cameraNameTv = findViewById(R.id.tv_camera_name)
                playLoadingPb = findViewById(R.id.pb_play_loading)
                playErrorTv = findViewById(R.id.tv_play_error)
            }
        }

    }

    companion object {
        private val TAG = MultiScreenPreviewAdapter::class.java.simpleName

        fun getPlayInfoKeyBy(cameraInfo: EZCameraInfo): String {
            return cameraInfo.deviceSerial + "-" + cameraInfo.cameraNo
        }
    }

}