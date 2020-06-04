@file:Suppress("UNUSED_PARAMETER")

package com.ezviz.demo.streamctrl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ezviz.demo.common.DataTimeUtil
import com.ezviz.demo.common.FolderPathManager
import com.ezviz.sdk.streamctrl.EscDataCallback
import com.ezviz.sdk.streamctrl.EscMessageCallback
import com.ezviz.sdk.streamctrl.EzvizStreamController
import com.ezviz.sdk.streamctrl.impl.EscStreamType
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.LogUtil
import ezviz.ezopensdkcommon.common.RootActivity
import kotlinx.android.synthetic.main.activity_origin_stream_control.*
import org.MediaPlayer.PlayM4.Player
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class OriginStreamControlActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_origin_stream_control)
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        mController?.release()
    }

    fun init(){
        tv_file_path.text = "码流文件路径：这里将在取流期间或取流完成后展示相应码流文件路径"
    }

    fun onclickStartPreview(view: View) {
        if(!prepareInputValidDeviceInfo()){
            return
        }
        EzvizStreamController.createForPreview(mDeviceSerial, mCameraNo)?.apply {
            mController = this
            setDataCallback(mDataCallback)
            setMessageCallback(mMessageCallback)
            startPreview()
            mStatus = StreamCtrlStatusEnum.WAITING
            mFilePath = "${FolderPathManager.getOriginStreamFolder()}/preview_${DataTimeUtil.getSimpleTimeInfoForTmpFile()}.dat"
            LogUtil.i(TAG, "onclickStartPreview: $mFilePath")
            createFile()
            try {
                mFos = FileOutputStream(mFilePath!!)
                tv_file_path.text = "码流文件路径：$mFilePath"
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun onclickStopPreview(view: View) {
        mController?.apply {
            setDataCallback(null)
            setMessageCallback(null)
            stopPreview()
            release()
            mStatus = StreamCtrlStatusEnum.UNKNOWN
            stopWriteDataToFile()
            unInitPlayer()
        }
    }

    fun onclickStartPlayback(view: View) {
        if(!prepareInputValidDeviceInfo()){
            return
        }
        if (!prepareInputValidRecordFileTimeInfo()){
            return
        }
        EzvizStreamController.createForPlayback(mDeviceSerial, mCameraNo)?.apply {
            mController = this
            setDataCallback(mDataCallback)
            setMessageCallback(mMessageCallback)
            startPlayback(mStartTime, mStopTime)
            mStatus = StreamCtrlStatusEnum.WAITING
            mFilePath = "${FolderPathManager.getOriginStreamFolder()}/record_${System.currentTimeMillis()}.dat"
            LogUtil.i(TAG, "onclickStartPlayback: $mFilePath")
            createFile()
            try {
                mFos = FileOutputStream(mFilePath!!)
                tv_file_path.text = "码流文件路径：$mFilePath"
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun onclickStopPlayback(view: View) {
        stopPlayback()
    }

    private fun stopPlayback(){
        showToast("回放结束")
        mController?.apply {
            setDataCallback(null)
            setMessageCallback(null)
            stopPlayback()
            release()
            mStatus = StreamCtrlStatusEnum.UNKNOWN
            stopWriteDataToFile()
            unInitPlayer()
        }
    }

    private var mDeviceSerial: String? = null
    private var mCameraNo: Int = -1
    private fun prepareInputValidDeviceInfo(): Boolean{
        try {
            // 设备序列号
            mDeviceSerial = et_device_serial.text!!.toString()
            // 设备通道号
            mCameraNo = et_camera_no.text!!.toString().toInt()
        }catch (e: Exception){
            e.printStackTrace()
        }
        val isValid = mDeviceSerial != null && mCameraNo >=0
        if(!isValid){
            showToast("请输入有效的设备信息")
        }
        return isValid
    }

    private var mStartTime: Calendar? = null
    private var mStopTime: Calendar? = null
    private fun prepareInputValidRecordFileTimeInfo(): Boolean{
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        try {
            // 开始时间
            Calendar.getInstance().apply {
                time = sdf.parse(et_record_file_start_time.text.toString())!!
                mStartTime = this
            }
            // 结束时间
            Calendar.getInstance().apply {
                time = sdf.parse(et_record_file_stop_time.text.toString())!!
                mStopTime = this
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        val isValid = mStartTime != null && mStopTime != null
        if(!isValid){
            showToast("请输入有效的录像起止时间！")
        }
        return isValid
    }
    
    private fun createFile(){
        val file = File(mFilePath!!)
        file.parentFile?.apply {
            if(!exists()){
                mkdirs()
            }
        }
        file.createNewFile()
    }

    private fun stopWriteDataToFile(){
        mFos?.run {
            flush()
            close()
        }
    }

    private fun addLog(newLog: String){
        runOnUiThread {
            tv_file_path?.apply {
                val oldText = text
                val newText = "${oldText}\n${newLog}"
                text = newText
            }
        }
    }

    private fun clearLog(newLog: String){
        setLog("")
    }

    private fun setLog(initLog: String){
        runOnUiThread{
            tv_file_path?.apply {
                text = initLog
            }
        }
    }

    private fun initPlayer(header: ByteArray, length: Int) {
        mPlayerPort = Player.getInstance().port
        if (mPlayerPort < 0){
            LogUtil.e(TAG, "failed to init player, invalid stream header")
            return
        }
        Player.getInstance().setStreamOpenMode(mPlayerPort, Player.STREAM_REALTIME)
        Player.getInstance().openStream(mPlayerPort, header, length, 1024 * 1024)
        // 设置加密秘钥
//        Player.getInstance().setSecretKey()
    }

    private fun unInitPlayer() {
        if(mPlayerPort < 0){
            return
        }
        Player.getInstance()?.apply {
            closeStream(mPlayerPort)
            freePort(mPlayerPort)
        }
    }

    private var mController: EzvizStreamController? = null
    private var mStatus = StreamCtrlStatusEnum.UNKNOWN
    private var mFos: FileOutputStream? = null
    private var mFilePath: String? = null
    private var mPlayerPort = -1

    private val mDataCallback = EscDataCallback { dataType, bytes, length ->
        when(dataType){
            EscStreamType.EZ_STREAM_TYPE_HEADER -> {
                LogUtil.i(TAG, "流头到达，取流成功")
                // 初始化播放库
                initPlayer(bytes, length)
            }
            EscStreamType.EZ_STREAM_TYPE_DATA -> {
                LogUtil.i(TAG, "EscDataCallback：data length is $length")
                // 塞入播放库
                mDataCallback2?.onData(dataType, bytes, length)
            }
            EscStreamType.EZ_STREAM_TYPE_END -> {
                LogUtil.i(TAG, "回放结束，取流结束")
                stopPlayback()
                return@EscDataCallback
            }
        }
        // 数据写入文件
        try {
            mFos!!.write(bytes, 0, length)
        }catch (e: Exception){
            LogUtil.e(TAG, "failed to write data to file")
        }
    }

    private val mMessageCallback = EscMessageCallback { code, desc ->
        LogUtil.i(TAG, "EzvizStreamMessageCallback: $code")
        LogUtil.i(TAG, "EzvizStreamMessageCallback: $desc")
        addLog("onMessage: $code")
        showToast("onMessage: $code")
    }

    companion object{
        fun launch(context: Context){
            context.startActivity(Intent(context, OriginStreamControlActivity::class.java))
        }

        private val TAG = OriginStreamControlActivity::class.java.simpleName
        public var mDataCallback2: EscDataCallback? = null
    }

    fun onClickViewPicture(view: View) {
        RtpStreamPlayActivity.launch(this, mPlayerPort)
    }

}
