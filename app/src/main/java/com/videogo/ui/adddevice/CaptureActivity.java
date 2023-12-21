package com.videogo.ui.adddevice;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

import com.videogo.exception.BaseException;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LocalValidate;
import com.videogo.util.LogUtil;
import com.videogo.widget.TitleBar;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;
import ezviz.ezopensdkcommon.configwifi.AutoWifiNetConfigActivity;

public class CaptureActivity extends RootActivity implements QRCodeView.Delegate {

    private static final String TAG = CaptureActivity.class.getSimpleName();// 打印标识

    private TitleBar mTitleBar;
    private Button mBtnRight;
    private CheckBox ckbLight;

    private ZXingView mZXingView;

    /**
     * true为应用权限管理返回
     */
    private boolean isFromPermissionSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_activity);
        initTitleBar();

        ckbLight = findViewById(R.id.ckbLight);
        ckbLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mZXingView.openFlashlight();
            } else {
                mZXingView.closeFlashlight();
            }
        });
        mZXingView = findViewById(R.id.zxingview);
        mZXingView.setDelegate(this);
        checkPermissions();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        afterHasPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromPermissionSetting) {
            checkPermissions();
            isFromPermissionSetting = false;
        }
    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    /**
     * 处理扫描结果
     *
     * @param resultString 摄像头扫码时只要回调了该方法 result 就一定有值，不会为 null。解析本地图片或 Bitmap 时 result 可能为 null
     */
    @Override
    public void onScanQRCodeSuccess(String resultString) {
        if (resultString == null) {
            LogUtil.e(TAG, "scan resultString is null");
            return;
        }
        LogUtil.e(TAG, "scan resultString = " + resultString);
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        mZXingView.startSpotDelay(500); // 延迟0.5秒后开始识别

        // 初始化数据
        String mSerialNoStr = "";
        String mSerialVeryCodeStr = "";
        String deviceType = "";
        // resultString = "www.xxx.com\n456654855\nABCDEF\nCS-C3-21PPFR\n";
        // 字符集合
        String[] newlineCharacterSet = {"\n\r", "\r\n", "\r", "\n"};
        String stringOrigin = resultString;
        // 寻找第一次出现的位置
        int a = -1;
        int firstLength = 1;
        for (String string : newlineCharacterSet) {
            if (a == -1) {
                a = resultString.indexOf(string);
                if (a > stringOrigin.length() - 3) {
                    a = -1;
                }
                if (a != -1) {
                    firstLength = string.length();
                }
            }
        }

        // 扣去第一次出现回车的字符串后，剩余的是第二行以及以后的
        if (a != -1) {
            resultString = resultString.substring(a + firstLength);
        }
        // 寻找最后一次出现的位置
        int b = -1;
        for (String string : newlineCharacterSet) {
            if (b == -1) {
                b = resultString.indexOf(string);
                if (b != -1) {
                    mSerialNoStr = resultString.substring(0, b);
                    firstLength = string.length();
                }
            }
        }

        // 寻找遗失的验证码阶段
        if (mSerialNoStr != null && b != -1 && (b + firstLength) <= resultString.length()) {
            resultString = resultString.substring(b + firstLength);
        }

        // 再次寻找回车键最后一次出现的位置
        int c = -1;
        for (String string : newlineCharacterSet) {
            if (c == -1) {
                c = resultString.indexOf(string);
                if (c != -1) {
                    mSerialVeryCodeStr = resultString.substring(0, c);
                }
            }
        }

        // 寻找CS-C2-21WPFR 判断是否支持wifi
        if (mSerialNoStr != null && c != -1 && (c + firstLength) <= resultString.length()) {
            resultString = resultString.substring(c + firstLength);
        }
        if (resultString != null && resultString.length() > 0) {
            deviceType = resultString;
        }

        if (b == -1) {
            mSerialNoStr = resultString;
        }

        if (mSerialNoStr == null) {
            mSerialNoStr = stringOrigin;
        }
        LogUtil.d(TAG, "mSerialNoStr = " + mSerialNoStr + ",mSerialVeryCodeStr = " + mSerialVeryCodeStr
                + ",deviceType = " + deviceType);
        // 判断是不是9位
        isValidate(mSerialNoStr, mSerialVeryCodeStr, deviceType);
    }

    private void isValidate(String mSerialNoStr, String mSerialVeryCodeStr, String deviceType) {
        LocalValidate mLocalValidate = new LocalValidate();
        try {
            mLocalValidate.localValidatSerialNo(mSerialNoStr);
            LogUtil.i(TAG, mSerialNoStr);
        } catch (BaseException e) {
            LogUtil.e(TAG, "searchCameraBySN-> local validate serial no fail, errCode:" + e.getErrorCode());
            return;
        }

        if (!ConnectionDetector.isNetworkAvailable(this)) {
            showToast(R.string.query_camera_fail_network_exception);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("type", 1);
        bundle.putString("SerialNo", mSerialNoStr);
        bundle.putString("very_code", mSerialVeryCodeStr);
        bundle.putString(AutoWifiNetConfigActivity.DEVICE_TYPE, deviceType);
        LogUtil.d(TAG, "very_code:" + mSerialVeryCodeStr);
        Intent intent = new Intent(CaptureActivity.this, SeriesNumSearchActivity.class);
        intent.putExtras(bundle);
        CaptureActivity.this.startActivity(intent);
    }

    /**
     * 摄像头环境亮度发生变化
     *
     * @param isDark 是否变暗
     */
    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    /**
     * 处理打开相机出错
     */
    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    private void initTitleBar() {
        mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.ez_scan_title_txt);
        mTitleBar.addBackButton(v -> finish());
        mBtnRight = mTitleBar.addRightButton(R.drawable.common_title_input_selector, v -> addCameraBySN());

        // 做400ms保护 ，解决上个页面过来 点击过快 直接跑下一个界面问题。
        mBtnRight.setClickable(false);
        mBtnRight.postDelayed(() -> mBtnRight.setClickable(true), 400);
    }

    /**
     * 跳转手动输入设备序列号页面
     */
    private void addCameraBySN() {
        // type -0 手动输入序列号， type - 1二维码扫描
        Bundle bundle = new Bundle();
        bundle.putInt("type", 0);
        Intent intent = new Intent(CaptureActivity.this, SeriesNumSearchActivity.class);
        intent.putExtras(bundle);
        CaptureActivity.this.startActivity(intent);
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
        if (!(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.CAMERA);
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
        AlertDialog dialog = new AlertDialog.Builder(CaptureActivity.this)
                .setMessage("应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isFromPermissionSetting = true;
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                })
                .setNegativeButton("退出应用", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                        System.exit(0);
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(CaptureActivity.this, R.color.black));
        //设置居中，解决Android9.0 AlertDialog不居中问题
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.width = (int) (LocalInfo.getInstance().getScreenWidth() * 0.9);
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);
    }

    private void afterHasPermission() {
        mZXingView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        mZXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.1秒后开始识别
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