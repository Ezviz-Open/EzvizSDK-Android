package com.videogo.ui.others;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.videogo.exception.BaseException;
import com.videogo.global.ValueKeys;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZStorageStatus;
import com.videogo.openapi.bean.EZUserInfo;
import com.videogo.ui.login.SdkInitParams;
import com.videogo.util.SpTool;
import com.videogo.widget.TitleBar;

import java.util.List;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

import static com.videogo.EzvizApplication.getOpenSDK;

public class EZInterfaceTestActivity extends RootActivity {

    private String mSingleDeviceSerial = "";

    /***************** 生命周期|life cycle *******************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ezinterface_test);
        initTitleBar();
    }

    /***************** 接口方法 *******************/

    /**
     * 获取设备列表（支持网关子设备）
     */
    public void onClickGetDeviceListEx(View view) {
        new Thread(() -> {
            try {
                List<EZDeviceInfo> list = getOpenSDK().getDeviceListEx(0, 10);
            } catch (BaseException e) {
                e.printStackTrace();
            }
            requestSuccess();
        }).start();
    }

    /**
     * 获取设备信息（支持网关子设备）
     */
    public void onClickGetDeviceInfoEx(View view) {
        if (!isExistDeviceSerial()) return;
        new Thread(() -> {
            try {
                EZDeviceInfo deviceInfo = getOpenSDK().getDeviceInfoEx(mSingleDeviceSerial);
            } catch (BaseException e) {
                e.printStackTrace();
            }
            requestSuccess();
        }).start();
    }

    /**
     * 抓图
     */
    public void onClickCaptureCamera(View view) {
        if (!isExistDeviceSerial()) return;
        new Thread(() -> {
            try {
                String string = getOpenSDK().captureCamera(mSingleDeviceSerial, 1);
            } catch (BaseException e) {
                e.printStackTrace();
            }
            requestSuccess();
        }).start();

    }

    /**
     * 获取用户信息
     */
    public void onClickGetUserInfo(View view) {
        new Thread(() -> {
            try {
                EZUserInfo userInfo = getOpenSDK().getUserInfo();
            } catch (BaseException e) {
                e.printStackTrace();
            }
            requestSuccess();
        }).start();
    }

    /**
     * SD卡格式化
     */
    public void onClickFormatStorage(View view) {
        if (!isExistDeviceSerial()) return;
        new Thread(() -> {
            try {
                List<EZStorageStatus> storageStatusList = getOpenSDK().getStorageStatus(mSingleDeviceSerial);
                if (storageStatusList != null && storageStatusList.size() > 0) {
                    EZStorageStatus info = storageStatusList.get(0);
                    getOpenSDK().formatStorage(mSingleDeviceSerial, info.getIndex());
                    requestSuccess();
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 云台结束转动Mix
     */
    public void onClickPtzStopMix(View view) {
        if (!isExistDeviceSerial()) return;
        new Thread(() -> {
            try {
                boolean result = getOpenSDK().controlPTZMix(mSingleDeviceSerial, 1,
                        EZConstants.EZPTZCommand.EZPTZCommandUp, EZConstants.EZPTZAction.EZPTZActionSTOP, 2);
                requestSuccess();
            } catch (BaseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 刷新设备详情数据
     */
    public void onClickRefreshDeviceInfo(View view) {
        if (!isExistDeviceSerial()) return;
        new Thread(() -> {
            try {
                getOpenSDK().refreshDeviceDetailInfo(mSingleDeviceSerial, 1);
                requestSuccess();
            } catch (BaseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onClickIPCRecordDirectQuery(View view) {
        if (!isExistDeviceSerial()) return;
        EZDeviceRecordDirectQueryActivity.launch(this, mSingleDeviceSerial);
    }

    /**
     * 透传接口测试
     */
    public void onClickTransferAPI(View view) {

    }

    /***************** 其他自定义方法 *******************/

    private boolean isExistDeviceSerial() {
        // 只展示单个设备
        String sdkInitParamStr = SpTool.obtainValue(ValueKeys.SDK_INIT_PARAMS);
        if (sdkInitParamStr != null) {
            SdkInitParams mInitParams = new Gson().fromJson(sdkInitParamStr, SdkInitParams.class);
            mSingleDeviceSerial = mInitParams.specifiedDevice;
        }
        if (TextUtils.isEmpty(mSingleDeviceSerial)) {
            showToast(R.string.test_specify_device_tip);
            return false;
        }
        return true;
    }

    private void requestSuccess() {
        runOnUiThread(() -> showToast(R.string.test_request_success_tip));
    }

    /***************** UI *******************/

    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.addBackButton(view -> finish());
        mTitleBar.setTitle("接口测试");
    }

}