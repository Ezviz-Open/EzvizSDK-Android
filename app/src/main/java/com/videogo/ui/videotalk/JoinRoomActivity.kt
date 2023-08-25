package com.videogo.ui.videotalk

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.videogo.ui.videotalk.DataConfig.APPID_DEV
import ezviz.ezopensdk.R
import kotlinx.android.synthetic.main.activity_join_room.*


class JoinRoomActivity : AppCompatActivity() {

    private var roomId = 0
    private var appId = ""
    private var customerId = ""
    private var password = ""

    private var width = 360
    private var height = 640
    private var fps = 15
    private var bitrate = 500

    private val TAG = "JoinRoomActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_room)

        edt_appid.setText(APPID_DEV)
        edt_nick.setText("8")
        edt_width.setText("$width")
        edt_height.setText("$height")
        edt_bitrate.setText("$bitrate")
        edt_fps.setText("$fps")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){
            setResult(RESULT_OK)
            finish()
        }
    }

    fun onClickJoin(view : View) {
        appId = edt_appid.text.toString()
        if (TextUtils.isEmpty(appId)){
            Toast.makeText(this, "请输入appid", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            roomId = Integer.valueOf(edt_room.text.toString())
        }catch (e : Exception){
            Toast.makeText(this, "请输入正确房间号", Toast.LENGTH_SHORT).show()
            return
        }

        customerId = edt_nick.text.toString()
        if (TextUtils.isEmpty(customerId)){
            Toast.makeText(this, "请输入customerId", Toast.LENGTH_SHORT).show()
            return
        }

        password = edt_passwd.text.toString()

        try {
            width = Integer.valueOf(edt_width.text.toString())
            height = Integer.valueOf(edt_height.text.toString())
            bitrate = Integer.valueOf(edt_bitrate.text.toString())
            fps = Integer.valueOf(edt_fps.text.toString())
        }catch (e : Exception){

        }

        gotoRoom()
    }

    private fun gotoRoom(){
        val intent = Intent(this, EZRtcTestActivity::class.java)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_APP_ID, appId)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_ROOM_ID, roomId)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_USER_ID, customerId)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PASSWORD, password)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_WIDTH, width)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_HEIGHT, height)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_FPS, fps)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_BITRATE, bitrate * 1024)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_OPUS, cb_opus.isChecked)
        startActivityForResult(intent, 100)
    }
}