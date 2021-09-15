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
import kotlinx.android.synthetic.main.activity_join_room.*
import kotlin.concurrent.thread

class JoinRoomActivity : AppCompatActivity() {

    private var roomId = 0
    private var customerId = ""
    private var confluenceInfo : EZConfluenceInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_room)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){
            setResult(RESULT_OK)
            finish()
        }
    }

    fun onClickJoin(view: View) {
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

        thread{
            try{
                confluenceInfo = EzvizAPI.getInstance().queryOrderedConfluence(roomId, customerId)
                runOnUiThread {
                    gotoRoom()
                }
            }catch (e : BaseException){
                runOnUiThread { Toast.makeText(this, e.message, Toast.LENGTH_LONG).show() }
                e.printStackTrace()
            }
        }
    }

    private fun gotoRoom(){
        val intent = Intent(this, ConfluenceActivity::class.java)
                .putExtra(ConfluenceActivity.KEY_ROOM_ID, confluenceInfo?.roomId)
                .putExtra(ConfluenceActivity.KEY_CLIENT_ID, confluenceInfo?.clientId)
                .putExtra(ConfluenceActivity.KEY_NICK_ID, customerId)
                .putExtra(ConfluenceActivity.KEY_PASSWD_ID, edt_passwd.text.toString())
                .putExtra(ConfluenceActivity.KEY_STS_IP_ID, confluenceInfo?.vtmIp)
                .putExtra(ConfluenceActivity.KEY_STS_PORT_ID, confluenceInfo?.vtmPort)
                .putExtra(ConfluenceActivity.KEY_VC_IP_ID, confluenceInfo?.controlServerIp)
                .putExtra(ConfluenceActivity.KEY_VC_PORT_ID, confluenceInfo?.controlServerPort)
        startActivityForResult(intent, 100)
    }
}