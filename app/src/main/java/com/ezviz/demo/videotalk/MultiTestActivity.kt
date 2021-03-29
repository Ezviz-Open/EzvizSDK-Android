package com.ezviz.demo.videotalk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import com.ezviz.demo.videotalk.widget.VideoTalkView
import com.ezviz.sdk.videotalk.*
import com.ezviz.sdk.videotalk.EvcParamValueEnum.EvcOperationEnum
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.LogUtil
import ezviz.ezopensdkcommon.common.Utils
import kotlinx.android.synthetic.main.activity_multi_video_talk.*
import kotlinx.android.synthetic.main.activity_multi_video_talk_test.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.thread

class MultiTestActivity : Activity() {

    companion object {
        private const val TAG = "@@MultiTestActivity"

    }

    private fun toast3s(toastMessage: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 不支持AP19以下版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            finish()
            runOnUiThread { toast3s("手机版本太低，不支持视频通话功能") }
        }
        setContentView(R.layout.activity_multi_video_talk_test)
        initListener()
    }

    private fun initListener() {

        is_call_device_cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                device_id_layout.visibility = View.VISIBLE
            } else {
                device_id_layout.visibility = View.GONE
            }
        }

        use_custom_pwd_cb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                custom_pwd_layout.visibility = View.VISIBLE
            } else {
                custom_pwd_layout.visibility = View.GONE
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickCreateRoom(view: View) {
        val roomId = try {
            room_id.text.toString().toInt()
        } catch (e:Throwable){
            0
        }
        val channelId = try {
            channel_id_et.text.toString().toInt()
        } catch (e:Throwable){
            0
        }
        Intent(this, MultiVideoTalkActivity::class.java).run {
            putExtra("device_id", device_id_et.text.toString())
            putExtra("channel_id", channelId)
            putExtra("room_id", roomId)
            putExtra("username", username_et.text.toString())
            putExtra("password", custom_pwd_et.text.toString())
            putExtra("is_call", true)
            putExtra("is_call_device", is_call_device_cb.isChecked)
            putExtra("enable_video", enable_video_cb.isChecked)
            putExtra("enable_audio", enable_audio_cb.isChecked)
            startActivity(this)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickJoinRoom(view: View) {
        val roomId = try {
            room_id.text.toString().toInt()
        } catch (e:Throwable){
            0
        }
        Intent(this, MultiVideoTalkActivity::class.java).run {
            putExtra("room_id", roomId)
            putExtra("username", username_et.text.toString())
            putExtra("password", custom_pwd_et.text.toString())
            putExtra("is_call", false)
            putExtra("is_call_device", is_call_device_cb.isChecked)
            putExtra("enable_video", enable_video_cb.isChecked)
            putExtra("enable_audio", enable_audio_cb.isChecked)
            startActivity(this)
        }
    }
}