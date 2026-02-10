package com.videogo.ui.adddevice;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.eziot.ble.EZIoTBleConfig;
import com.eziot.ble.EZIoTBleManager;
import com.eziot.ble.EZIoTBlePubParams;
import com.eziot.ble.callback.EZIoTBleConnectCallback;
import com.eziot.ble.callback.EZIoTBleScanCallback;
import com.eziot.ble.callback.EZIoTBleSendDataCallback;
import com.eziot.ble.device.IEZIoTBleDevice;
import com.eziot.ble.http.core.callback.IEZIoTResultCallback;
import com.eziot.ble.http.core.callback.IResultCallback;
import com.eziot.ble.model.EZIoTBleDeviceInfo;
import com.eziot.ble.model.EZIoTBleTimeZone;
import com.eziot.ble.model.EZIoTBleWifiInfo;
import com.eziot.ble.model.EZIoTPeripheral;
import com.eziot.ble.model.EZIoTWifiEncryptType;
import com.ezviz.http.exception.EzConfigWifiException;
import com.ezviz.http.model.DeviceTokenInfo;
import com.ezviz.sdk.configwifi.touchAp.GetTokenCallback;
import com.ezviz.sdk.configwifi.touchAp.QueryPlatformBindStatusCallback;
import com.ezviz.sdk.configwifi.touchAp.TouchApApi;
import com.videogo.EzvizApplication;
import com.videogo.global.GlobalKit;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;
import com.videogo.widget.bottommenu.BottomMenuFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;
import ezviz.ezopensdkcommon.common.RouteNavigator;

@Route(path = RouteNavigator.BLUETOOTH_WIFICONFIG_PAGE)
public class BluetoothConfigActivity extends RootActivity implements View.OnClickListener {

    public final static String TAG = BluetoothConfigActivity.class.getSimpleName();
    // WiFi名称
    private final static String SSID_Name = "";
    // WiFi密码
    private final static String SSID_Pwd = "";
    // 蓝牙搜索到的设备列表
    private List<EZIoTPeripheral> deviceList = new ArrayList<>();
    // 蓝牙连接的设备
    IEZIoTBleDevice iConnectDevice;
    // token信息
    private DeviceTokenInfo tokenInfo;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private int searchCount;
    private final static int SEARCH_MAX_COUNT = 20;

    private EditText wifiSsidEt, wifiPwdEt, devSerialEt, configTokenEt;
    private TextView logPrintTv;
    private Dialog waitDialog = null;

    private boolean isFromPermissionSetting;// true为应用权限管理返回

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_config);
        initTitleBar();

        wifiSsidEt = findViewById(R.id.wifiSsidEt);
        wifiPwdEt = findViewById(R.id.wifiPwdEt);
        devSerialEt = findViewById(R.id.devSerialEt);
        configTokenEt = findViewById(R.id.tokenEt);
        logPrintTv = findViewById(R.id.log_print);
        waitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        findViewById(R.id.scanDeviceBtn).setOnClickListener(this);
        findViewById(R.id.showDeviceBtn).setOnClickListener(this);
        findViewById(R.id.getDeviceInfoBtn).setOnClickListener(this);
        findViewById(R.id.getDeviceWifiListBtn).setOnClickListener(this);
        findViewById(R.id.getTokenBtn).setOnClickListener(this);
        findViewById(R.id.bluetoothConfigWifiBtn).setOnClickListener(this);
        findViewById(R.id.continueQueryBtn).setOnClickListener(this);
        findViewById(R.id.stopWifiConfigBtn).setOnClickListener(this);
        findViewById(R.id.instructionsBtn).setOnClickListener(this);

        wifiSsidEt.setText(SSID_Name);
        wifiPwdEt.setText(SSID_Pwd);

        // 步骤0：蓝牙配网SDK初始化
        initIoTBleSDK();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromPermissionSetting) {
            checkPermissions();
            isFromPermissionSetting = false;
        }
    }

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.wifi_bluetooth_device_config);
        mTitleBar.addBackButton(v -> {
            onBackPressed();
            stopTimer();
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.scanDeviceBtn) {// 蓝牙设备扫描
            deviceList.clear();// 清空设备列表
            checkAndRequestPermission();
        } else if (id == R.id.showDeviceBtn) {// 显示已扫描设备
            if (deviceList.isEmpty()) {
                int tip = EZIoTBleManager.INSTANCE.isScaning() ? R.string.wifi_bluetooth_scanning_device_tip :
                        R.string.wifi_bluetooth_scan_device_tip;
                showToast(getString(tip));
                return;
            }
            List<String> devices = new ArrayList<>();
            for (int i = 0; i < deviceList.size(); i++) {
                devices.add(deviceList.get(i).getDeviceSerial());
            }
            new BottomMenuFragment(this)
                    .setHeadTitle(getResources().getString(R.string.wifi_bluetooth_device_select_tip))
                    .addMenuItems(devices).setOnItemClickListener(position -> {
                        EZIoTBleManager.INSTANCE.stopScan();
                        EZIoTPeripheral device = deviceList.get(position);
                        iConnectDevice = EZIoTBleManager.INSTANCE.getBleDevice(this, device.getDeviceSerial());
                        waitDialog.show();
                        // 步骤2：蓝牙设备连接
                        iConnectDevice.connect(new EZIoTBleConnectCallback() {
                            @Override
                            public void connectSuccess() {
                                waitDialog.dismiss();
                                devSerialEt.setText(device.getDeviceSerial());
                                showToast(device.getDeviceSerial() + getString(R.string.wifi_bluetooth_device_connect_success));
                            }

                            @Override
                            public void connectFailed(int i, @org.jetbrains.annotations.Nullable String s) {
                                waitDialog.dismiss();
                                showToast(getString(R.string.wifi_bluetooth_device_connect_failed, i));
                            }

                            @Override
                            public void onConnectionStateChange(int i) {

                            }
                        });
                    }).show();
        } else if (id == R.id.getTokenBtn) {// 获取配网token
            waitDialog.show();
            // 步骤3：获取蓝牙设备配网必要的token信息
            EzvizApplication.getOpenSDK().getNewApConfigToken(new GetTokenCallback() {
                @Override
                public void onSuccess(final DeviceTokenInfo deviceTokenInfo) {
                    waitDialog.dismiss();
                    runOnUiThread(() -> {
                        tokenInfo = deviceTokenInfo;
                        configTokenEt.setText(deviceTokenInfo.token);
                        logPrintTv.setText(TouchApApi.responseData);
                    });

                }

                @Override
                public void onError(final EzConfigWifiException ezConfigWifiException) {
                    runOnUiThread(() -> Utils.showToast(BluetoothConfigActivity.this,
                            "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: " + ezConfigWifiException.message));
                    waitDialog.dismiss();
                }
            });
        } else if (id == R.id.bluetoothConfigWifiBtn) {// 发起配网
            stopTimer();
            final String wifiSsid = wifiSsidEt.getText().toString();
            final String wifiPwd = wifiPwdEt.getText().toString();
            final String devSerial = devSerialEt.getText().toString();
            final String configToken = configTokenEt.getText().toString();

            if (TextUtils.isEmpty(wifiSsid)) {
                Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_ssid_cannot_empty);
                return;
            }
            if (TextUtils.isEmpty(wifiPwd)) {
                Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_password_cannot_empty);
                return;
            }
            if (TextUtils.isEmpty(devSerial)) {
                Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_bluetooth_device_connect_first);
                return;
            }
            if (TextUtils.isEmpty(configToken)) {
                Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_get_token_first);
                return;
            }

            waitDialog.show();
            // 步骤4：发起配网请求，设备配网成功后会自动绑定到账号下
            iConnectDevice.startFastApConfigWithToken(wifiSsid, wifiPwd, EZIoTWifiEncryptType.WPA2, "CN",
                    tokenInfo.registerUrl, tokenInfo.token, new IResultCallback() {
                        @Override
                        public void onSuccess() {
                            LogUtil.i(TAG, "startFastApConfigWithToken success");
                            runOnUiThread(() -> {
                                waitDialog.dismiss();
                                Utils.showToast(BluetoothConfigActivity.this, getString(R.string.wifi_info_send_succeed));
                                // 步骤5：开始轮询设备的绑定情况
                                startSearchDeviceTimer();
                            });
                        }

                        @Override
                        public void onError(int i, @org.jetbrains.annotations.Nullable String s) {
                            runOnUiThread(() -> {
                                waitDialog.dismiss();
                                Utils.showToast(BluetoothConfigActivity.this, getString(R.string.config_wifi_failed));
                                Log.e(TAG, "startFastApConfigWithToken error code: " + i + " error message: " + s);
                            });
                        }
                    });
        } else if (id == R.id.getDeviceInfoBtn) {// 获取设备信息
            if (TextUtils.isEmpty(devSerialEt.getText().toString())) {
                Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_bluetooth_device_connect_first);
                return;
            }
            waitDialog.show();
            iConnectDevice.getDeviceInfo(new EZIoTBleSendDataCallback<EZIoTBleDeviceInfo>() {
                @Override
                public void onError(int i) {
                    runOnUiThread(() -> {
                        waitDialog.dismiss();
                        Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_device_info_get_failed);
                    });
                }

                @Override
                public void onSuccess(EZIoTBleDeviceInfo ezIoTBleDeviceInfo) {
                    runOnUiThread(() -> {
                        waitDialog.dismiss();
                        String info = String.format("pid: %s\ndeviceName: %s", ezIoTBleDeviceInfo.pid,
                                ezIoTBleDeviceInfo.deviceName);
                        logPrintTv.setText(info);
                    });
                }
            });
        } else if (id == R.id.getDeviceWifiListBtn) {// 获取设备WiFi列表
            waitDialog.show();
            if (iConnectDevice == null) {
                Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_bluetooth_device_connect_first);
                return;
            }
            iConnectDevice.getWifiList(new IEZIoTResultCallback<List<EZIoTBleWifiInfo>>() {
                @Override
                public void onSuccess(List<EZIoTBleWifiInfo> ezIoTBleWifiInfos) {
                    runOnUiThread(() -> {
                        waitDialog.dismiss();
                        Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_list_get_success);
                        LogUtil.i(TAG, "getWifiList onSuccess: ezIoTBleWifiInfos.size: " + ezIoTBleWifiInfos.size());
                        String info = "";
                        for (int i = 0; i < ezIoTBleWifiInfos.size(); i++) {
                            LogUtil.i(TAG, "SSID: " + ezIoTBleWifiInfos.get(i).ssid);
                            info += "SSID: " + ezIoTBleWifiInfos.get(i).ssid + "\n";
                        }
                        logPrintTv.setText(info);
                    });

                }

                @Override
                public void onError(int i, @org.jetbrains.annotations.Nullable String s) {
                    runOnUiThread(() -> {
                        waitDialog.dismiss();
                        Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_list_get_failed);
                        LogUtil.e(TAG, "getWifiList onError: error code: " + i + " error message: " + s);
                    });
                }
            });
        } else if (id == R.id.continueQueryBtn) {// 继续查询设备绑定状态
            String deviceSerial = devSerialEt.getText().toString();
            EzvizApplication.getOpenSDK().queryPlatformBindStatus(deviceSerial, new QueryPlatformBindStatusCallback() {
                @Override
                public void onSuccess(boolean isBindSuccess) {
                    if (isBindSuccess) {
                        Log.e(TAG, "queryPlatformBindStatus success");
                        dialog(getString(R.string.wifi_config_succeed), getString(R.string.wifi_device_bind_success));
                    } else {
                        Log.e(TAG, "");
                    }
                }

                @Override
                public void onError(final EzConfigWifiException ezConfigWifiException) {
                    Log.e(TAG,
                            "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: " + ezConfigWifiException.message);
                }
            });
        } else if (id == R.id.stopWifiConfigBtn) {// 停止配网
            stopTimer();
            EZIoTBleManager.INSTANCE.stopScan();
            if (iConnectDevice != null) {
                iConnectDevice.disconnect();
            }
        } else if (id == R.id.instructionsBtn) {// 使用说明
            dialog(getString(R.string.wifi_bluetooth_config_instructions),
                    getString(R.string.wifi_bluetooth_config_instructions_content));
        }
    }


    /**
     * 设备联网成功或者未知错误(某些型号设备无返回值)的时候发起轮询设备的绑定情况
     */
    private void startSearchDeviceTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                searchDeviceFromService();
            }
        };
        mTimer.schedule(mTimerTask, 0, 5000);
    }

    private void searchDeviceFromService() {
        searchCount++;
        if (searchCount >= SEARCH_MAX_COUNT) {
            mTimer.cancel();
            mTimer = null;
            return;
        }
        String deviceSerial = devSerialEt.getText().toString();
        EzvizApplication.getOpenSDK().queryPlatformBindStatus(deviceSerial, new QueryPlatformBindStatusCallback() {
            @Override
            public void onSuccess(boolean isBindSuccess) {
                if (isBindSuccess) {
                    Log.e(TAG, "queryPlatformBindStatus success");
                    dialog(getString(R.string.wifi_config_succeed), getString(R.string.wifi_device_bind_success));
                    stopTimer();
                } else {
                    Log.e(TAG, "");
                }
            }

            @Override
            public void onError(final EzConfigWifiException ezConfigWifiException) {
                Log.e(TAG, "请求失败，错误码 ： " + ezConfigWifiException.errorCode + " 错误信息: " + ezConfigWifiException.message);
            }
        });
    }

    private void stopTimer() {
        if (mTimer == null) {
            return;
        }
        mTimer.cancel();
        mTimer = null;
        searchCount = 0;
    }

    /**
     * 蓝牙配网SDK初始化
     */
    private void initIoTBleSDK() {
        EZIoTBlePubParams ezIoTBlePubParams = new EZIoTBlePubParams() {
            @Override
            public String getApiDomain() {
                return "";
            }

            @Override
            public String getSession() {
                return "";
            }

            @Override
            public String getHardwareCode() {
                return "";
            }

            @Override
            public String getClientType() {
                return "";
            }

            @Override
            public String getAppId() {
                return "";
            }

            @Override
            public String getLanguage() {
                return "";
            }

            @Override
            public String getClientVersion() {
                return "";
            }

            @Override
            public String getUserId() {
                return "";
            }

            @Override
            public HashMap<String, String> getDeviceSerialAndMacMap() {
                return null;
            }

            @Override
            public void saveDeviceSerialAndMac(HashMap<String, String> hashMap) {

            }

            @Override
            public void log(String s, String s1) {
                LogUtil.i(TAG, s + ": " + s1);
            }

            @Override
            public void dclog(String s) {

            }

            @Override
            public EZIoTBleTimeZone getDeviceTimeZone(String s, String s1) {
                return null;
            }
        };
        EZIoTBleConfig.getInstance().setDebug(true) // 是否开启debug模式
                .setEzIoTBlePubParams(ezIoTBlePubParams).init(EzvizApplication.mInstance);
    }


    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermission();
        } else {
            afterHasPermission();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<>();
        // 蓝牙权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (!(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            if (!(checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.BLUETOOTH);
            }
        }
        if (!(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        // 权限都已经有了
        if (lackedPermission.size() == 0) {
            afterHasPermission();
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && hasAllPermissionsGranted(grantResults)) {
            afterHasPermission();
        } else {
            try {
                showPermissionDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 权限设置
     */
    private void showPermissionDialog() {
        AlertDialog dialog =
                new AlertDialog.Builder(this).setMessage("应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。").setPositiveButton("去设置",
                        (dialog1, which) -> {
                            isFromPermissionSetting = true;
                            dialog1.dismiss();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }).setNegativeButton("取消", (dialog12, which) -> {
                    dialog12.dismiss();
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.black));
        // 设置居中，解决Android9.0 AlertDialog不居中问题
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.width = (int) (LocalInfo.getInstance().getScreenWidth() * 0.9);
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);
    }

    private void afterHasPermission() {
        LogUtil.i(TAG, "start scan bluetooth device");
        // 步骤1：蓝牙设备扫描
        // 有蓝牙权限 & 本地位置信息权限后，开始蓝牙设备搜索；isAllDevices: true为全部设备， false为萤石设备
        EZIoTBleManager.INSTANCE.stopScan();
        EZIoTBleManager.INSTANCE.scanPeripherals(true, this, new EZIoTBleScanCallback() {
            @Override
            public void onScanResult(@NotNull EZIoTPeripheral ezIoTPeripheral, int i, @NotNull ScanResult scanResult) {
                if (ezIoTPeripheral != null && !TextUtils.isEmpty(ezIoTPeripheral.getDeviceSerial())) {
//                    LogUtil.i(TAG, "onScanResult: " + ezIoTPeripheral.getDeviceSerial());
                    // 遍历列表，已经存在的拦截；只将新扫描到的设备加入列表
                    for (int index = 0; index < deviceList.size(); index++) {
                        String scanDevSerial = ezIoTPeripheral.getDeviceSerial();
                        String existDevSerial = deviceList.get(index).getDeviceSerial();
                        if (scanDevSerial.equals(existDevSerial)) {
                            return;
                        }
                    }
                    deviceList.add(ezIoTPeripheral);
                }
            }

            @Override
            public void onScanFail(int i) {
                LogUtil.e(TAG, "onScanFail: " + i);
            }
        });
        Utils.showToast(BluetoothConfigActivity.this, R.string.wifi_bluetooth_scan_start);
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

}
