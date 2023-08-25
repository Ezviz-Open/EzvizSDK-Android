package com.videogo.ui.videotalk

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import ezviz.ezopensdk.R

class ConfluenceTestEntranceActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "@@MeetingTestActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_test)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){
            finish()
        }
    }

    fun orderRoom(view: View) {
        Intent(this, OrderRoomActivity::class.java).run {
            startActivityForResult(this, 100)
        }
    }

    fun joinRoom(view: View) {
        Intent(this, JoinRoomActivity::class.java).run {
            startActivityForResult(this, 100)
        }
    }
}