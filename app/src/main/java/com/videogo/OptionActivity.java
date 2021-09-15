package com.videogo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.ezviz.opensdk.auth.EZAuthAPI;
import com.videogo.exception.BaseException;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.ui.LanDevice.LanDeviceActivity;

import java.util.ArrayList;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

import static com.videogo.EzvizApplication.getOpenSDK;

public class OptionActivity extends RootActivity {

    ArrayList<EZDeviceInfo> mList = new ArrayList<EZDeviceInfo>();
    String url = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        findViewById(R.id.btn_get_device_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取设备列表
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            mList = (ArrayList<EZDeviceInfo>) getOpenSDK().getDeviceList(0, 10);
                            if (mList != null) {
                                showMsg("getDeviceList success size = " + mList.size());
                                Intent toIntent = new Intent(OptionActivity.this, com.videogo.ui.cameralist.EZCameraListActivity.class);
                                startActivity(toIntent);
                            }else {
                                showMsg("getDeviceList fail ");
                            }
                        } catch (BaseException e) {
                            e.printStackTrace();
                            showMsg("getDeviceList fail errorCode = " + e.getErrorCode());
                        }

//                        // 用于测试设备托管功能
//                        try {
//                            mList = (ArrayList<EZDeviceInfo>) EzvizApplication.mEzvizApplication.getOpenSDK().getInstance().getTrustDeviceList(0, 10);
//
//                            if (mList != null) {
//                                showMsg("getDeviceList success size = " + mList.size());
//                                Intent toIntent = new Intent(OptionActivity.this, com.videogo.ui.cameralist.EZCameraListActivity.class);
//                                startActivity(toIntent);
//                            }else {
//                                showMsg("getDeviceList fail ");
//                            }
//                        } catch (BaseSdkRuntimeException e) {
//                            e.printStackTrace();
//                            showMsg("getDeviceList fail errorCode = " + e.errCode);
//                        }

                    }
                }).start();
            }
        });

        findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2018/1/24 抓图 抓取设备列表第一个设备第一个通道的图片
                url = null;
                if (mList == null || mList.size() <= 0) {
                    showMsg("Please get the equipment list first");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            url = getOpenSDK().captureCamera(mList.get(0).getDeviceSerial(), mList.get(0).getCameraNum());
                            if (TextUtils.isEmpty(url)) {
                                showMsg("captureCamera fail");
                            } else {
                                showMsg("captureCamera url = " + url);
                            }
                        } catch (BaseException e) {
                            e.printStackTrace();
                            showMsg("captureCamera fail errorCode = " + e.getErrorCode());
                        }
                    }
                }).start();
            }
        });


        findViewById(R.id.btn_to_device_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EZAuthAPI.isEzvizAppInstalledWithType(OptionActivity.this, EZAuthAPI.EZAuthPlatform.EZVIZ)){
                    // TODO: 2018/1/24 跳转ezviz设备列表页
                    EZAuthAPI.sendOpenPage(OptionActivity.this, EZAuthAPI.EZAuthSDKOpenPage.OpenPage_DeviceList,
                        EZAuthAPI.EZAuthPlatform.EZVIZ);
                }else{
                    Toast.makeText(OptionActivity.this,"uninstalled or version is not newest",Toast.LENGTH_LONG).show();
                }

            }
        });

        findViewById(R.id.btn_to_alarm_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EZAuthAPI.isEzvizAppInstalledWithType(OptionActivity.this, EZAuthAPI.EZAuthPlatform.EZVIZ)){
                    // TODO: 2018/1/24 跳转ezviz报警消息列表页
                    EZAuthAPI.sendOpenPage(OptionActivity.this, EZAuthAPI.EZAuthSDKOpenPage.OpenPage_AlarmList, EZAuthAPI.EZAuthPlatform.EZVIZ);
                }else{
                    Toast.makeText(OptionActivity.this,"uninstalled or version is not newest",Toast.LENGTH_LONG).show();
                }

            }
        });

        findViewById(R.id.btn_landevice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  intent = new Intent(OptionActivity.this, LanDeviceActivity.class);
                startActivity(intent);
            }
        });

    }

    private void showMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(OptionActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
