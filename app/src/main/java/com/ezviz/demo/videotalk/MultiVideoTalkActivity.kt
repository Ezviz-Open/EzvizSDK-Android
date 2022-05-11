package com.ezviz.demo.videotalk

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.ezviz.demo.model.VideoTalkBean
import com.ezviz.demo.videotalk.widget.VideoTalkAdapter
import com.ezviz.demo.videotalk.widget.VideoTalkView
import com.ezviz.sdk.videotalk.EvcErrorMessage
import com.ezviz.sdk.videotalk.EvcParam
import com.ezviz.sdk.videotalk.sdk.EZMeetingCall
import com.ezviz.videotalk.JNAApi
import com.ezviz.videotalk.jna.BavClientJoinInfo
import com.videogo.exception.BaseException
import com.videogo.openapi.EZOpenSDK
import ezviz.ezopensdk.R
import ezviz.ezopensdkcommon.common.LogUtil
import kotlinx.android.synthetic.main.activity_multi_video_talk.*
import java.util.*
import kotlin.concurrent.thread

class MultiVideoTalkActivity : Activity() {

    companion object {
        private const val TAG = "@@MultiVideoTalkActivity"
        /*视频通话服务器域名*/
//        private const val SERVER_DOMAIN = "vtm.ys7.com"
        private const val SERVER_DOMAIN = "test12.ys7.com"

        /*视频通话服务器地址*/
        private const val SERVER_PORT = 8554

        /*对应手表联系人ID*/
        private const val ERROR = -1
        var param: EvcParam = EvcParam();
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 不支持AP19以下版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            finish()
            runOnUiThread { toast3s("手机版本太低，不支持视频通话功能") }
        }
        setContentView(R.layout.activity_multi_video_talk)
        initData()

        initListeners()
        // 实例化视频通话对象
        /**
         * EvcLocalWindowView 本机视图
         * nickname 当前设备在会议中显示的名称
         * enableCamera 进入会议时是否开启相机
         * enableMic 进入会议时是否开启麦克风
         * EZMeetingCall.CallBack 会议通话回调
         */


//        EZMeetingCall(camera_view_my, mSelfId?:"", hostView?.findViewById<CheckBox>(R.id.check_video_my)!!.isChecked, hostView?.findViewById<CheckBox>(R.id.check_mic_my)!!.isChecked, mMettingCallback, param).run {
        EZMeetingCall(camera_view_my, mSelfId?:"", check_video_my.isChecked, check_mic_my.isChecked, mMettingCallback, param).run {
            mEZMeetingCall = this
            initView()
            this.setLogPrintEnable(true)
        }


        Handler().postDelayed({
            if (mIsCall) {
                createMeeting(mCustomId, limit)
            } else {
                joinMeeting(mCustomId)
            }
        }, 1000)

//        val s : String? = null
//        s!!.toString()
    }

    /**
     * 初始化通话参数
     */
    private fun initData() {
        mServerDomain = SERVER_DOMAIN
        mServerPort = SERVER_PORT
        mIsCall = intent.getBooleanExtra("is_call", true)
        mDeviceSerial = intent.getStringExtra("device_id")
        mChannelId = intent.getIntExtra("channel_id", 1)
        mSelfId = intent.getStringExtra("username")
        mPassword = intent.getStringExtra("password")
        mInputtedRoomID = intent.getIntExtra("room_id", 0)
        mIsCallDevice = intent.getBooleanExtra("is_call_device", false)
        mCustomId = intent.getStringExtra("custom_id")
        limit = intent.getStringExtra("limit")

//        hostView = LayoutInflater.from(this).inflate( R.layout.multi_video_talk_host_item, null)

//        hostView?.findViewById<CheckBox>(R.id.check_video_my)!!.isChecked = intent.getBooleanExtra("enable_video", false)
        check_video_my.isChecked = intent.getBooleanExtra("enable_video", false)
//        hostView?.findViewById<CheckBox>(R.id.check_mic_my)!!.isChecked = intent.getBooleanExtra("enable_audio", true)
        check_mic_my.isChecked = intent.getBooleanExtra("enable_audio", true)

//        mCustomId = System.currentTimeMillis().toString()

        if (!mIsCall) {
            room_id_text.text = mInputtedRoomID.toString()
        }
        username_text.text = mSelfId
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickLeaveRoom(view: View) {
        finish()
    }

    fun onClickCreateProject(view : View){
        val ezAccessToken = EZOpenSDK.getInstance().ezAccessToken;
        projectName = findViewById(R.id.project_name)
        projectId = findViewById(R.id.project_id)
        val projectNameText: String = projectName?.text.toString()
        val projectIdText: String = projectId?.text.toString()
        if(TextUtils.isEmpty(projectNameText)){
            Toast.makeText(this, "请输入projectName", Toast.LENGTH_LONG).show()
            return;
        }
        if(TextUtils.isEmpty(projectIdText)){
            Toast.makeText(this, "请输入projectId", Toast.LENGTH_LONG).show()
            return;
        }

        val regex: Regex = "^[a-z0-9A-Z]+$".toRegex()
        //str.matches(regex);
        if(!projectIdText.matches(regex)){
            Toast.makeText(this, "projectId只能为英文字符或数字", Toast.LENGTH_LONG).show()
            return
        }
        //val response :String? = null
        thread {
            try {
                val response = mEZMeetingCall?.createProject(ezAccessToken.accessToken,projectIdText,projectNameText)

                runOnUiThread {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                }
            } catch (e : BaseException){
                e.printStackTrace()
            }

        }

    }

    fun onClickStartRecord(view : View){
        val ezAccessToken = EZOpenSDK.getInstance().ezAccessToken;
        fileId = findViewById(R.id.file_id)
        projectId = findViewById(R.id.project_id)
        val fileIdText: String = fileId?.text.toString()
        val projectIdText: String = projectId?.text.toString()

        if(TextUtils.isEmpty(fileIdText)){
            Toast.makeText(this, "请输入fileId", Toast.LENGTH_LONG).show();
            return
        }
        if(TextUtils.isEmpty(projectIdText)){
            Toast.makeText(this, "请输入projectId", Toast.LENGTH_LONG).show();
            return
        }

        val regex: Regex = "^[a-z0-9A-Z]+$".toRegex()
        //str.matches(regex);
        if(!fileIdText.matches(regex) || !projectIdText.matches(regex)){
            Toast.makeText(this, "fileId或projectId只能为英文字符或数字", Toast.LENGTH_LONG).show()
            return
        }

        if(!isCreateUser!!){ //如果不是房间创建者
            Toast.makeText(this, "非管理员无权限", Toast.LENGTH_LONG).show()
            return
        }

        //var fileId:
        thread {
            try {
                //val time = System.currentTimeMillis()
                val response: String? = mEZMeetingCall?.startConRecord(ezAccessToken.accessToken, mRoomId!!, mClient!!, projectIdText, fileIdText, mCustomId)
                //mEZMeetingCall?.startConRecord(ezAccessToken.accessToken, mRoomId!!, mClient!!.toString(),"123","test5")
                runOnUiThread {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                }
            } catch (e : BaseException){
                e.printStackTrace()
            }
        }
    }

    fun onClickStopRecord(view : View){
        val ezAccessToken = EZOpenSDK.getInstance().ezAccessToken;

        thread {
            try {
                val response: String? = mEZMeetingCall?.stopConRecord(ezAccessToken.accessToken,mRoomId!!, mCustomId)
                runOnUiThread {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                }
            } catch (e : BaseException){
                e.printStackTrace()
            }
        }
    }

    fun onClickSearchRecord(view : View){
        val ezAccessToken = EZOpenSDK.getInstance().ezAccessToken;

        fileId = findViewById(R.id.file_id)
        projectId = findViewById(R.id.project_id)
        val fileIdText: String = fileId?.text.toString()
        val projectIdText: String = projectId?.text.toString()
        if(TextUtils.isEmpty(fileIdText)){
            Toast.makeText(this, "请输入fileId", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(projectIdText)){
            Toast.makeText(this, "请输入projectId", Toast.LENGTH_LONG).show();
            return;
        }

        val regex: Regex = "^[a-z0-9A-Z]+$".toRegex()
        //str.matches(regex);
        if(!fileIdText.matches(regex) || !projectIdText.matches(regex)){
            Toast.makeText(this, "fileId或projectId只能为英文字符或数字", Toast.LENGTH_LONG).show()
            return
        }

        thread {
            try {
                val response: String? = mEZMeetingCall?.searchConRecord(ezAccessToken.accessToken, projectIdText, fileIdText)
                runOnUiThread {
                    //val alertDialog1: AlertDialog?  = AlertDialog.Builder(this).setMessage(response).create()
                    //alertDialog1?.show()
                    //val json:JSONObject = JSONObject(response)
                    val urls: EditText? = findViewById(R.id.url)
                    urls?.setText(response)
                }
            } catch (e : BaseException){
                e.printStackTrace()
            }
        }
    }


    private fun initView(){
        mRecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        mRecyclerView!!.layoutManager = layoutManager
        adapter = VideoTalkAdapter(mEZMeetingCall!!,this, videoTalkList)
        mRecyclerView?.adapter = adapter
    }


    /**
     * 初始化监听器
     */
    private fun initListeners() {

        check_video_my.setOnCheckedChangeListener{ _: CompoundButton, isChecked: Boolean ->
            mEZMeetingCall?.enableCamera(isChecked)
        }
        check_mic_my.setOnCheckedChangeListener { _, isChecked ->
            mEZMeetingCall?.enableMic(isChecked)
        }
        camera_switch_btn.setOnClickListener {
            mEZMeetingCall?.switchCamera()
        }
    }

    private fun createMeeting(customId: String?, limit: String?) {
        if (mIsCallDevice) {
            mEZMeetingCall?.createMeetingWithDevice(mDeviceSerial?:"", mChannelId, mPassword, customId, limit)
        } else {
            mEZMeetingCall?.createMeeting(mPassword, customId, limit)
        }
        isCreateUser = true
    }

    private fun joinMeeting(customId:String?) {
        if (isValidRoomId()) {
            mEZMeetingCall?.joinMeeting(mInputtedRoomID, mPassword, customId)
        }
        isCreateUser = false
    }



    private fun isValidRoomId(): Boolean {
        return mInputtedRoomID >= 0
    }

    override fun onPause() {
        super.onPause()
        mEZMeetingCall?.enableCamera(false)
    }

    override fun onRestart() {
        super.onRestart()
        mEZMeetingCall?.enableCamera(mEZMeetingCall?.enableCamera == true)
        mEZMeetingCall?.enableMic(mEZMeetingCall?.enableMic == true)
    }

    /**
     * 发起或者加入通话前检查权限
     */
    private fun hasNeededPermissions(): Boolean {
        if (isLackOfCameraPermission) {
            showToast(EvcErrorMessage.LACK_OF_CAMERA_PERMISSION)
            return false
        }
        if (isLackOfRecordAudioPermission) {
            showToast(EvcErrorMessage.LACK_OF_RECORD_AUDIO_PERMISSION)
            return false
        }
        return true
    }

    private fun showToast(error: EvcErrorMessage) {
        showToast("${error.code}\n${error.desc}")
    }

    private fun showToast(toastMessage: String) {
        LogUtil.d(TAG, toastMessage)
        runOnUiThread {
            toast3s(toastMessage)
        }
    }

    private fun toast3s(toastMessage: String) {
        runOnUiThread {
            if (mCurrentToast != null) {
                mCurrentToast!!.cancel()
                mCurrentToast = null
            }
            val toast = Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG)
            toast.show()
            mCurrentToast = toast
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    if (mCurrentToast === toast) {
                        mCurrentToast!!.cancel()
                    }
                }
            }, 3000)
        }
    }

    private val mMettingCallback = object: EZMeetingCall.CallBack {
        override fun onError(code: Int, message: String) {
            //窗口未建立链接
            if(code==60022){
                return
            }
            showToast("onError: $code\n$message")
            if (isFinishing) {
                return
            }
            if (code == 50017) {
//                runOnUiThread {
//                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_call_is_accepted).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
//                        dialog.dismiss()
//                        finish()
//                    }.show()
//                }
            } else if (code == 50103 || code == 50106) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_watch_is_busy).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50104) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_watch_temperature_high_reject).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50203) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_watch_temperature_high_hang).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 50105) {
                runOnUiThread {
                    AlertDialog.Builder(this@MultiVideoTalkActivity).setMessage(R.string.video_talk_is_playing).setCancelable(false).setPositiveButton(R.string.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }.show()
                }
            } else if (code == 10152) {
                isLackOfRecordAudioPermission = true
                showToast("没有麦克风权限")
            } else if (code == 20153) {
                isLackOfCameraPermission = true
                showToast("没有相机权限")
            } else if (code == 50007 || code == 50005) {
                showToast("网络断开，退出房间")
                finish()
            } else if(code == EvcErrorMessage.MULTI_CALL_FAILED_DEVICE_ENCRYPT.code){
                showToast("不支持加密设备，退出房间")
                finish()
            }else if(code == 50013 || code == 50008){
                showToast("消息超时")
                finish()
            }
        }

        override fun onRoomCreated(roomId: Int,clientId: Int, customId: String) {
            showToast("onRoomCreated: $roomId")
            runOnUiThread {
                mRoomId = roomId
                room_id_text.text = roomId.toString()
                mCustomId = customId
//                joinRoom(clientId, "", true)
            }
        }


        override fun onJoinRoom(joinInfo: BavClientJoinInfo, customId: String?) {
            runOnUiThread {

                if(TextUtils.isEmpty(mClient)){
                    mClient = mClient.plus(joinInfo.m_uClientId.toString())
                }else{
                    mClient = mClient.plus(",${joinInfo.m_uClientId}")
                }
                mRoomId = joinInfo.m_uRoomId
                mCustomId = customId
                joinRoom(joinInfo.m_uClientId, joinInfo.m_szUserName.toString())
            }
        }

        override fun onClientStat(clientId: Int, volume: Int){
            runOnUiThread{
                //非静音状态下，同步音量
//                if(videoTalkMap[clientId]==null){
//                    return@runOnUiThread
//                }
//                if(!videoMap[clientId]!!){
//                    val videoTalkBean = videoTalkMap[clientId]
//                    Log.d("aaaaaaaaa: ", "$volume")
                    changeUserVolume(clientId, volume)
//                }
            }
        }
        //静音m_szMute为'\u0001',非静音为'\u0000'
        override fun onClientUpdated(bavJoinInfo: BavClientJoinInfo?){

            /*runOnUiThread{
//                if(videoTalkMap[bavJoinInfo!!.m_uClientId]==null){
//                    return@runOnUiThread
//                }
                var volume : Int = 0
//                Log.d("bbbbbbbbb: ", "$volume")
//                if(bavJoinInfo!!.m_szMute != '\u0000'){
////                    mMute = true
//                    //保存每个房间用户的静音状态
//                    videoMap[bavJoinInfo!!.m_uClientId] = true
//                    volume = 1
//                }else{
//                    videoMap[bavJoinInfo!!.m_uClientId] = false
//                }
                changeUserVolume(bavJoinInfo!!.m_uClientId, volume)
//                Log.d("cccccccccc: ", "$volume")
            }*/
        }

        override fun onFirstFrameDisplayed(width: Int, height: Int, clientId: Int) {
//            showToast("onFirstFrameDisplayed: $roomId")
        }

        override fun onQuitRoom(roomId: Int, clientId: Int) {
            runOnUiThread {
                quitRoom(clientId)
            }
        }

        override fun onBadNet(delayTimeMs: Int) {
            runOnUiThread {
                showToast(getString(R.string.video_talk_signal_weak))
            }
        }
    }

    @Suppress("unused")
    object InIntentKeysAndValues {
        const val KEY_ROLE = "role"
        const val VALUE_CALLER = 0
        const val VALUE_ANSWER = 1
        const val VALUE_REFUSE = 2
        const val KEY_ROOM_ID = "room_id"
        const val KEY_NICK_NAME = "nick_name"
        const val KEY_HEAD_PORTRAIT_REMOTE = "head_portrait_remote"
        const val KEY_HEAD_PORTRAIT_LOCAL = "head_portrait_remote"
        const val KEY_DEVICE_SERIAL = "device_serial"
        const val KEY_TOKEN = "token"
        const val KEY_SERVER = "server"
        const val KEY_SERVER_PORT = "server_port"
        const val KEY_SELF_ID = "caller_id"
        const val KEY_USE_AUDIO = "key_use_audio"
        const val KEY_ENABLE_VIDEO = "key_enable_video"
        const val KEY_ENABLE_AUDIO = "key_enable_audio"
    }

    override fun onBackPressed() {
        if (mEZMeetingCall?.isTalking() == true) {
            val ensureDialog = AlertDialog.Builder(this)
                    .setTitle("退出将结束视频聊天")
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("确定") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .create()
            ensureDialog.show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (mEZMeetingCall != null) {
            mEZMeetingCall?.quitMeeting()
            mEZMeetingCall = null
        }
        super.onDestroy()
    }

    /**
     * 改变其他用户的音量
     */
    private fun changeUserVolume(clientId: Int, volume: Int){
//        videoTalkMap[clientId] = videoTalkBean
//
//        videoTalkList.add(videoTalkBean)
//        adapter!!.setData(videoTalkList)
        //防止有些用户还没加入房间，就开始回调音量
        var videoTalkBean: VideoTalkBean? = videoTalkMap[clientId] ?: return
        videoTalkBean!!.volume = volume
        (mRecyclerView!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        adapter!!.changeData(videoTalkList, videoTalkList.indexOf(videoTalkBean))
    }

    private fun joinRoom(clientId: Int, username: String) {
        if (videoTalkMap.keys.contains(clientId)) {
            showToast("该用户已加入房间")
            return
        }

        val videoTalkBean : VideoTalkBean = VideoTalkBean(clientId, username, 1,1)
        videoTalkMap[clientId] = videoTalkBean
        videoMap[clientId] = false
        videoTalkList.add(videoTalkBean)
        adapter!!.setData(videoTalkList)

    }

    private fun quitRoom(clientId: Int) {
        if (!videoTalkMap.keys.contains(clientId)) {
            showToast("该用户已不在房间")
            return
        }
        runOnUiThread {
//            val view = clientInRoomMap[clientId]
//            if (view != null) {
//                vg_child_watch_video_talk_video_container.removeView(view.rootView)
//                view.leaveRoom()
//                clientInRoomMap.remove(clientId)
//                freeVideoTalkView.push(view)
//            }
//            val videoTalkBean: Boolean = videoTalkMap.keys.contains(clientId)
            var position = videoTalkList.indexOf(videoTalkMap[clientId])

            videoMap.remove(clientId)
            videoTalkList[position].isJoinRoom = -1
//            adapter!!.setData(videoTalkList)
            videoTalkList[position].volume = 0
            (mRecyclerView!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            adapter!!.changeData(videoTalkList, position)
            videoTalkList.remove(videoTalkMap[clientId])
            videoTalkMap.remove(clientId)
            adapter!!.deleteData(videoTalkList, position)

        }
    }

    private fun getFreeVideoTalkView(): VideoTalkView {
        val view = if (freeVideoTalkView.isEmpty()) {
            VideoTalkView(this, mEZMeetingCall!!)
        } else {
            freeVideoTalkView.pop()
        }
//        val view = VideoTalkView(this, mEzvizVideoCall!!)
        view.reset()
        return view
    }


    private val clientInRoomMap = hashMapOf<Int, VideoTalkView>()
    private val freeVideoTalkView = Stack<VideoTalkView>()

    private var mRole = ERROR
    private var mCurrentToast: Toast? = null
    private var mEZMeetingCall: EZMeetingCall? = null

    // 应用权限信息
    private var isLackOfCameraPermission = false
    private var isLackOfRecordAudioPermission = false

    // 发起或者接听视频通话的参数
    private var mServerDomain: String? = null
    private var mServerPort = 0

    /**
     * 显示的用户名
     */
    private var mSelfId: String? = null
    /**
     * 是否是呼叫，否则是加入
     */
    private var mIsCall = true

    /**
     * 是否呼叫设备，否则只创建房间
     */
    private var mIsCallDevice = false

    /**
     * mIsCallDevice = true 时
     * 需要呼叫的设备
     */
    private var mDeviceSerial: String? = null
    /**
     * mIsCallDevice = true 时
     * 需要呼叫的设备通道
     */
    private var mChannelId: Int = 1
    /**
     * 是否使用自定义密码校验
     */
    private var mPassword: String? = null
    /**
     * 加入的房间号
     */
    private var mInputtedRoomID = ERROR

    private var mRoomId : Int? = null

    //private var mClient : Int? = null
    private var mClient: String = ""

    private var fileId: EditText? = null
    private var projectId: EditText? = null
    private var projectName: EditText? = null

    private var isCreateUser: Boolean? = null

    /**
     * 开启会议和关闭会议传入的字段
     */
    private var mCustomId: String? = null

    private var limit: String? = null

//    private var hostView : View? = null

    private var videoTalkList = ArrayList<VideoTalkBean>()

    private var videoTalkMap = hashMapOf<Int, VideoTalkBean>()
    private var videoMap = hashMapOf<Int, Boolean>()


    var mRecyclerView : RecyclerView? = null
    var layoutManager: StaggeredGridLayoutManager? = null
    var adapter: VideoTalkAdapter? = null

    /**
     * 保存静音状态
     */
    private var mMute: Boolean = false
}