package com.ezviz.demo.videotalk

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import ezviz.ezopensdk.R
import kotlinx.android.synthetic.main.activity_order_room.*

class OrderRoomActivity : AppCompatActivity() {

    private var password = ""
    private var customerId = ""

    private var width = 360
    private var height = 640
    private var fps = 15
    private var bitrate = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_room)

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

    fun onClickOrder(view : View) {
        password = edt_passwd.text.toString()
        customerId = edt_nick.text.toString()
        if (TextUtils.isEmpty(customerId)){
            Toast.makeText(this, "请输入customerId", Toast.LENGTH_SHORT).show()
            return
        }
        var limit = 100
        try {
            limit = Integer.valueOf(edt_limit.text.toString())
            width = Integer.valueOf(edt_width.text.toString())
            height = Integer.valueOf(edt_height.text.toString())
            bitrate = Integer.valueOf(edt_bitrate.text.toString())
            fps = Integer.valueOf(edt_fps.text.toString())
        }catch (e : Exception){

        }

        val intent = Intent(this, EZRtcTestActivity::class.java)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_USER_ID, customerId)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PASSWORD, password)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_LIMIT, limit)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_WIDTH, width)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_HEIGHT, height)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_FPS, fps)
            .putExtra(EZRtcTestActivity.InIntentKeysAndValues.KEY_PARAM_BITRATE, bitrate * 1024)
        startActivityForResult(intent, 100)
    }

}