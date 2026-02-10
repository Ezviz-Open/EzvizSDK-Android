@file:Suppress("UNUSED_PARAMETER")

package com.videogo.ui.realplay.multiScreenPreview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.videogo.openapi.bean.EZCameraInfo
import com.videogo.openapi.bean.EZDeviceInfo
import ezviz.ezopensdk.databinding.ActivityMultiScreenPreviewBinding
import ezviz.ezopensdkcommon.common.LogUtil

class MultiScreenPreviewActivity : ezviz.ezopensdkcommon.common.RootActivity(), MultiScreenPreviewActivityPresenter.Callback {

    private lateinit var binding: ActivityMultiScreenPreviewBinding
    private val mPresenter = MultiScreenPreviewActivityPresenter()
    private val mDeviceList = ArrayList<EZDeviceInfo>()
    private val mCameraList = ArrayList<EZCameraInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiScreenPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPresenter()
        initData()
        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.setCallback(null)
    }

    override fun onStop() {
        super.onStop()
        stopAll()
    }

    override fun onResume() {
        super.onResume()
        startAll()
    }

    private fun initData() {
        mPresenter.getDeviceList()
    }

    override fun initPresenter() {
        mPresenter.setCallback(this)
    }

    private fun initViews() {
        changeScreenColumnNumberTo(1)
    }

    private fun changeScreenColumnNumberTo(column: Int){
        binding.rvMultiScreen.apply {
            layoutManager = GridLayoutManager(mContext,column).apply {
                adapter = MultiScreenPreviewAdapter(mContext, mDeviceList, mCameraList, spanCount)
            }
        }
    }

    override fun onReceiveDeviceAndCameraList(deviceList:List<EZDeviceInfo>, cameraList: List<EZCameraInfo>) {
        LogUtil.d(TAG, "onReceiveCameraList: " + cameraList.size)
        mDeviceList.apply {
            clear()
            addAll(deviceList)
        }
        mCameraList.apply {
            clear()
            addAll(cameraList)
        }.sortBy { it.deviceSerial }
        binding.rvMultiScreen.adapter?.notifyDataSetChanged()
    }

    fun onClickExit(view: View) {
        finish()
    }

    fun onClickSingleColumn(view: View) {
        changeScreenColumnNumberTo(1)
    }

    fun onClickDoubleColumn(view: View) {
        changeScreenColumnNumberTo(2)
    }

    fun onClickStartAll(view: View) {
        startAll()
    }

    fun onClickStopAll(view: View) {
        stopAll()
    }

    private fun startAll(){
        binding.rvMultiScreen.adapter?.apply {
            (this as MultiScreenPreviewAdapter).startAll()
        }
    }

    private fun stopAll(){
        binding.rvMultiScreen.adapter?.apply {
            (this as MultiScreenPreviewAdapter).stopAll()
        }
    }

    companion object{
        fun launch(context: Context){
            context.startActivity(Intent(context, MultiScreenPreviewActivity::class.java))
        }
    }

}
