/**
 * 带宽检测工具选择页面
 */
package com.ezviz.bandwidthcheck;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.ezviz.common.RootActivity;
import com.ezviz.common.TitleBar;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.EZConstants;
import com.videogo.util.LocalInfo;

import java.util.ArrayList;
import java.util.List;

public class BWCToolSelectActivity extends RootActivity implements View.OnClickListener {

    // region --- 全局变量|global variable ---

    private static final String TAG = BWCToolSelectActivity.class.getSimpleName();

    // 设备带宽检测选项
    private RelativeLayout bwcDeviceRL;
    // 网络带宽检测选项
    private RelativeLayout bwcNetRL;

    /**
     * true为应用权限管理返回
     */
    private boolean isFromPermissionSetting;

    // 以下为上一个页面传递的数据
    private String deviceSerial;
    private int cameraNo;
    // 以上为上一个页面传递的数据

    // endregion

    // region --- 页面启动方法|Page startup method ---

    public static void launch(Context context) {
        Intent intent = new Intent(context, BWCToolSelectActivity.class);
        context.startActivity(intent);
    }

    public static void launch(Context context, String deviceSerial, int cameraNo) {
        Intent intent = new Intent(context, BWCToolSelectActivity.class);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, deviceSerial);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_NO, cameraNo);
        context.startActivity(intent);
    }

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bwc_tool_select);
        initData();
        initTitleBar();
        initUI();
        checkPermissions();
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
    public void onBackPressed() {
        if (!TextUtils.isEmpty(deviceSerial)) {// 设置页面跳转过来，deviceSerial不为空
            finish();
        } else {// 带宽检测App跳转过来，deviceSerial为空
            checkExit();
        }
    }

    // endregion

    // region --- Click点击事件|Click event ---

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rl_bwc_device) {
            BWCSpeedTestInputActivity.launch(BWCToolSelectActivity.this,
                    EZConstants.EZBWCheckType.EZBWCheckClientUpward, deviceSerial, cameraNo);
        } else if (id == R.id.rl_bwc_net) {
            BWCSpeedTestInputActivity.launch(BWCToolSelectActivity.this,
                    EZConstants.EZBWCheckType.EZBWCheckClientUpward | EZConstants.EZBWCheckType.EZBWCheckClientDownward,
                    deviceSerial, cameraNo);
        }
    }

    // endregion

    // region --- 自定义方法|custom method ---

    private long mLastPressTimeMs = 0;
    private void checkExit() {
        boolean isExist = false;
        if (mLastPressTimeMs > 0) {
            if (System.currentTimeMillis() - mLastPressTimeMs < 2 * 1000) {
                isExist = true;
            }
        }
        if (isExist) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.app_closed),
                    Toast.LENGTH_LONG).show();
            RootActivity.exitApp();
        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.press_again_to_exit),
                    Toast.LENGTH_SHORT).show();
            mLastPressTimeMs = System.currentTimeMillis();
        }
    }

    // endregion

    // region --- init ---

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle("带宽检测工具");
        if (!TextUtils.isEmpty(deviceSerial)) {
            mTitleBar.addBackButton(v -> finish());
        }
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    private void initData() {
        Intent intent = getIntent();
        deviceSerial = intent.getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
        cameraNo = intent.getIntExtra(IntentConsts.EXTRA_CAMERA_NO, 1);
    }

    private void initUI() {
        bwcDeviceRL = findViewById(R.id.rl_bwc_device);
        bwcNetRL = findViewById(R.id.rl_bwc_net);
        bwcDeviceRL.setOnClickListener(this);
        bwcNetRL.setOnClickListener(this);
    }

    // endregion

    // region --- 动态权限申请|Dynamic permission application ---

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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。")
                .setPositiveButton("去设置", (dialog1, which) -> {
                    isFromPermissionSetting = true;
                    dialog1.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog12, which) -> {
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

    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    // endregion
}