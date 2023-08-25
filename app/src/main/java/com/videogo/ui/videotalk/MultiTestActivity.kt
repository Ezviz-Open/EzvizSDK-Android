package com.videogo.ui.videotalk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.Utils
import kotlinx.android.synthetic.main.activity_multi_video_talk_test.*

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
        if(TextUtils.isEmpty(customId_et.text.toString())){
            Utils.showToast(this,"请输入customId")
            return
        }
        if(TextUtils.isEmpty(limit_id.text.toString())){
            limit = "100"

        }else{
            var text = limit_id.text.toString().toInt()

            if(text>100){
                Utils.showToast(this,"人数不能超过100")
                return
            }
            limit = text.toString()
        }
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
            putExtra("custom_id", customId_et.text.toString())
            putExtra("limit", limit)
            startActivity(this)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickJoinRoom(view: View) {
        if(TextUtils.isEmpty(customId_et.text.toString())){
            Utils.showToast(this,"请输入customId")
            return
        }
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
            putExtra("enable_audio", enable_audio_cb.isChecked)
            putExtra("custom_id", customId_et.text.toString())
            startActivity(this)
        }
    }

    var limit: String? = null
}