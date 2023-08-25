package com.videogo.ui.others.streamctrl

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.TextureView
import android.widget.Toast
import com.ezviz.sdk.streamctrl.EscDataCallback
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.RootActivity
import kotlinx.android.synthetic.main.activity_rtp_stream_play.*
import org.MediaPlayer.PlayM4.Player

class RtpStreamPlayActivity : RootActivity(), EscDataCallback {

    companion object{
        private val TAG = RtpStreamPlayActivity::class.java.simpleName
        private const val INTENT_KEY_PLAYER_PORT = "player_port"

        fun launch(context: Context, playerPort: Int){
            if (playerPort < 0){
                Toast.makeText(context, "Player未初始化", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(context, RtpStreamPlayActivity::class.java).apply {
                putExtra(INTENT_KEY_PLAYER_PORT, playerPort)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtp_stream_play)

        mPlayerPort = intent.getIntExtra(INTENT_KEY_PLAYER_PORT, -1)

        textureView_play.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Player.getInstance()?.apply {
                    stop(mPlayerPort)
                }
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                Player.getInstance().playEx(mPlayerPort, surface)
                OriginStreamControlActivity.mDataCallback2 = this@RtpStreamPlayActivity
            }

        }

    }

    override fun onStop() {
        super.onStop()
        OriginStreamControlActivity.mDataCallback2 = null
    }

    override fun onData(dataType: Int, bytes: ByteArray?, length: Int) {
        Player.getInstance().inputData(mPlayerPort, bytes, length)
    }

    private var mPlayerPort = -1

}
