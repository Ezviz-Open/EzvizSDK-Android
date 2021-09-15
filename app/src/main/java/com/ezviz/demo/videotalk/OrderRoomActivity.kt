package com.ezviz.demo.videotalk

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.videogo.exception.BaseException
import com.videogo.openapi.EzvizAPI
import com.videogo.openapi.bean.EZConfluenceInfo
import ezviz.ezopensdk.R
import kotlinx.android.synthetic.main.activity_order_room.*
import kotlin.concurrent.thread

class OrderRoomActivity : AppCompatActivity() {

    private var password = ""
    private var customerId = ""
    private var limit = 100
    private var confluenceInfo : EZConfluenceInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_room)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){
            setResult(RESULT_OK)
            finish()
        }
    }

    fun onClickOrder(view: View) {
        password = edt_passwd.text.toString()
        customerId = edt_nick.text.toString()
        if (TextUtils.isEmpty(customerId)){
            Toast.makeText(this, "请输入customerId", Toast.LENGTH_SHORT).show()
            return
        }
        thread{
            try {
                try {
                    limit = Integer.valueOf(edt_limit.text.toString())
                }catch (e : Exception){
                    limit = 100
                }

                var roomInfo = EzvizAPI.getInstance().orderConfluence(customerId, password, limit, 100L, 100L)

                roomInfo?.let {
                    confluenceInfo = EzvizAPI.getInstance().queryOrderedConfluence(roomInfo.roomId, customerId)
                    runOnUiThread {
                        gotoRoom()
                    }
                }
            }catch (e : BaseException){
                runOnUiThread {
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun gotoRoom(){
        val intent = Intent(this, ConfluenceActivity::class.java)
                .putExtra(ConfluenceActivity.KEY_ROOM_ID, confluenceInfo?.roomId)
                .putExtra(ConfluenceActivity.KEY_CLIENT_ID, confluenceInfo?.clientId)
                .putExtra(ConfluenceActivity.KEY_NICK_ID, customerId)
                .putExtra(ConfluenceActivity.KEY_PASSWD_ID, password)
                .putExtra(ConfluenceActivity.KEY_STS_IP_ID, confluenceInfo?.vtmIp)
                .putExtra(ConfluenceActivity.KEY_STS_PORT_ID, confluenceInfo?.vtmPort)
                .putExtra(ConfluenceActivity.KEY_VC_IP_ID, confluenceInfo?.controlServerIp)
                .putExtra(ConfluenceActivity.KEY_VC_PORT_ID, confluenceInfo?.controlServerPort)
        startActivityForResult(intent, 100)
    }
}