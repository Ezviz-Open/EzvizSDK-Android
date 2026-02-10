/**
 * 带宽检测检测结果页面
 */
package com.ezviz.bandwidthcheck;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.ezviz.common.RootActivity;
import com.ezviz.common.TitleBar;
import com.ezviz.common.Utils;
import com.ezviz.demo.DemoConfig;
import com.ezviz.sdk.configwifi.WiFiUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hikvision.wifi.configuration.BaseUtil;
import com.videogo.bandwidthcheck.EZBWCheckResult;
import com.videogo.openapi.EZConstants;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BWCSpeedTestResultActivity extends RootActivity implements View.OnClickListener {

    // region --- 全局变量|global variable ---

    private static final String TAG = BWCSpeedTestResultActivity.class.getSimpleName();
    public static final String EXTRA_BWC_RESULT = "EXTRA_BWC_RESULT";
    public static final String EXTRA_BWC_FROM = "EXTRA_BWC_FROM";

    private TextView resultTitleTv;
    private TextView deviceTv;
    private Button picSaveBtn;

    // 测试结果视图
    private LinearLayout resultLayout;
    // 客户端检测结果
    private LinearLayout clientResultLayout;
    private TextView clientUpwardTv;
    private TextView clientDownwardTv;
    private TextView clientLossTv;
    private TextView clientLatencyTv;
    private TextView clientUpwardRefTv;
    private TextView clientDownwardRefTv;
    // 设备端检测结果
    private LinearLayout deviceResultLayout;
    private TextView deviceUpwardTv;
    private TextView deviceLossTv;
    private TextView deviceLatencyTv;
    private TextView deviceUpwardRefTv;
    // 其他测试信息
    private TextView netTv;
    private TextView ispTv;
    private TextView ipTv;
    private TextView timeTv;

    private boolean isFromPermissionSetting;// true为应用权限管理返回

    // 以下为上一个页面传递的数据
    // 检测结果
    private EZBWCheckResult bwCheckResult;
    // 是否从检测页面传递过来
    private boolean isFromSpeedTest;
    // 以上为上一个页面传递的数据

    // endregion

    // region --- 页面启动方法|Page startup method ---

    public static void launch(Context context, EZBWCheckResult result, boolean isFromSpeedTest) {
        Intent intent = new Intent(context, BWCSpeedTestResultActivity.class);
        intent.putExtra(EXTRA_BWC_RESULT, result);
        intent.putExtra(EXTRA_BWC_FROM, isFromSpeedTest);
        context.startActivity(intent);
    }

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bwc_speed_result);
        initTitleBar();
        initData();
        initUI();

        if (isFromSpeedTest) {
            updateBWCheckResultCache();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromPermissionSetting) {
            checkPermissions();
            isFromPermissionSetting = false;
        }
    }

    // endregion

    // region --- Click点击事件|Click event ---

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_pic_save) {
            checkPermissions();
        }
    }

    // endregion

    // region --- 自定义方法|custom method ---

    /**
     * 获取测试参考富文本String
     *
     * @param wardString 上行/下行
     * @param speed      速率
     * @return
     */
    private String getTestRefRichText(String wardString, float speed) {
        String mbpsString = Math.round(speed / 8) + "M";// 四舍五入
        String qualityString = getQualityText(speed);

        StringBuffer sb = new StringBuffer();
        sb.append("<font color='#2C2C2C' style='font-weight:bold'>").append(wardString).append("带宽：").append("</font>");
        sb.append("本次测试结果相当于 ");
        sb.append("<font color='#407AFF' style='font-weight:bold'>").append(mbpsString).append("</font>");
        sb.append(" 带宽，可以支撑");
        sb.append("<font color='#407AFF' style='font-weight:bold'>").append(qualityString).append("</font>");
        sb.append(" 视频通道").append(wardString).append("宽带");

        return sb.toString();
    }

    /**
     * 获取该速度可支持的设备分辨率
     *
     * @param speed 速度
     * @return 设备分辨率
     */
    private String getQualityText(float speed) {
        int bandWidth = Math.round(speed / 8);
        if (bandWidth < 1) {
            bandWidth = 1;
        }
        if (bandWidth <= 2) {
            return "标清(480P)";
        } else if (bandWidth <= 4) {
            return "高清(720P)";
        } else if (bandWidth <= 8) {
            return "超清(1080P)";
        } else if (bandWidth <= 12) {
            return "2K";
        } else {
            return "4K";
        }
    }

    /**
     * 捕获视图并保存
     */
    private void captureAndSaveView() {
        try {
            // 设置视图为可绘制状态
            resultLayout.setDrawingCacheEnabled(true);
            resultLayout.buildDrawingCache();

            Bitmap bitmap = Bitmap.createBitmap(resultLayout.getDrawingCache());
            resultLayout.setDrawingCacheEnabled(false);

            if (bitmap != null) {
                saveBitmapToGallery(bitmap);
            } else {
                Toast.makeText(this, "截图失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "capture view error: " + e.getMessage());
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 保存Bitmap到相册
     */
    private void saveBitmapToGallery(Bitmap bitmap) {
        String fileName = "带宽测试报告_" + System.currentTimeMillis() + ".jpg";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用MediaStore
            saveBitmapToMediaStore(bitmap, fileName);
        } else {
            // Android 10以下使用传统方式
            saveBitmapToExternalStorage(bitmap, fileName);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveBitmapToMediaStore(Bitmap bitmap, String fileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + Environment.DIRECTORY_SCREENSHOTS);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                    Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "save to media store error: " + e.getMessage());
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveBitmapToExternalStorage(Bitmap bitmap, String fileName) {
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File saveDir = new File(picturesDir, "带宽测试");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        File file = new File(saveDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
                // 通知系统更新相册
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(file));
                sendBroadcast(mediaScanIntent);
                Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "save to external storage error: " + e.getMessage());
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNetworkInfo() {
        // 优先使用getCurrentWifiSsid方法获取wifi名
        String wifiName = WiFiUtils.getCurrentWifiSsid(this);
        // 如上述方式无效，则使用getWifiSSID方法进行获取
        if (!isValidWifiSSID(wifiName)) {
            wifiName = BaseUtil.getWifiSSID(this);
        }

        // 获取网络类型和运营商
        String networkType = Utils.getNetworkType(this);
//        String carrierName = Utils.getCarrierName(this);

        // 组合显示信息
        String displayText;
        if ("Wi-Fi".equals(networkType) && isValidWifiSSID(wifiName)) {
            displayText = wifiName + "(Wi-Fi)";
        } else if ("Wi-Fi".equals(networkType)) {
            displayText = "Wi-Fi网络";
        } else {
            displayText = networkType;
        }

        netTv.setText(displayText);
    }

    private boolean isValidWifiSSID(String wifiName) {
        return !TextUtils.isEmpty(wifiName) && !"<unknown ssid>".equalsIgnoreCase(wifiName);
    }

    /**
     * 更新测试结果缓存
     */
    private void updateBWCheckResultCache() {
        String bcCheckResultKey = bwCheckResult.checkType == EZConstants.EZBWCheckType.EZBWCheckClientUpward ?
                DemoConfig.SP_BWCHECK_RESULT_DEVICE : DemoConfig.SP_BWCHECK_RESULT_CLIENT;
        SharedPreferences sp = getSharedPreferences(DemoConfig.SP_BWCHECK_RESULT, Context.MODE_PRIVATE);
        String resultsString = sp.getString(bcCheckResultKey, "");
        Gson gson = new Gson();
        List<EZBWCheckResult> resultList = gson.fromJson(resultsString, new TypeToken<List<EZBWCheckResult>>() {
        }.getType());
        if (resultList == null) {
            resultList = new ArrayList<>();
        }
        if (resultList.size() >= 20) {
            resultList.remove(resultList.size() - 1);// 移除最后一个
        }
        bwCheckResult.networkName = netTv.getText().toString();// 设置网络名称
        resultList.add(0, bwCheckResult);
        String jsonString = gson.toJson(resultList);
        sp.edit().putString(bcCheckResultKey, jsonString).apply();
    }

    // endregion

    // region --- init ---

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle("测试报告");
        mTitleBar.addBackButton(v -> onBackPressed());
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent.getParcelableExtra(EXTRA_BWC_RESULT) != null) {
            bwCheckResult = intent.getParcelableExtra(EXTRA_BWC_RESULT);
            LogUtil.i(TAG, "initData bwCheckResult = " + bwCheckResult);
        }
        isFromSpeedTest = intent.getBooleanExtra(EXTRA_BWC_FROM, false);
    }

    private void initUI() {
        resultTitleTv = findViewById(R.id.tv_result_title);
        deviceTv = findViewById(R.id.tv_device);
        picSaveBtn = findViewById(R.id.btn_pic_save);
        picSaveBtn.setOnClickListener(this);
        // 测试结果视图
        resultLayout = findViewById(R.id.ll_result);
        // 客户端检测结果
        clientResultLayout = findViewById(R.id.ll_client_result);
        clientUpwardTv = findViewById(R.id.tv_client_upward);
        clientDownwardTv = findViewById(R.id.tv_client_downward);
        clientLossTv = findViewById(R.id.tv_client_loss);
        clientLatencyTv = findViewById(R.id.tv_client_latency);
        clientUpwardRefTv = findViewById(R.id.tv_client_upward_ref);
        clientDownwardRefTv = findViewById(R.id.tv_client_downward_ref);
        // 设备端检测结果
        deviceResultLayout = findViewById(R.id.ll_device_result);
        deviceUpwardTv = findViewById(R.id.tv_device_upward);
        deviceLossTv = findViewById(R.id.tv_device_loss);
        deviceLatencyTv = findViewById(R.id.tv_device_latency);
        deviceUpwardRefTv = findViewById(R.id.tv_device_upward_ref);
        // 其他测试信息
        netTv = findViewById(R.id.tv_net);
        ispTv = findViewById(R.id.tv_isp);
        ipTv = findViewById(R.id.tv_ip);
        timeTv = findViewById(R.id.tv_time);
        // 赋值
        if (isFromSpeedTest) {
            updateNetworkInfo();
        } else {
            netTv.setText(bwCheckResult.networkName);
        }
        if ((bwCheckResult.checkType & EZConstants.EZBWCheckType.EZBWCheckClientUpward) > 0 && (bwCheckResult.checkType & EZConstants.EZBWCheckType.EZBWCheckClientDownward) > 0) {
            resultTitleTv.setText("网络");
            deviceTv.setText(netTv.getText().toString());
            clientResultLayout.setVisibility(View.VISIBLE);
            deviceResultLayout.setVisibility(View.GONE);
            clientUpwardTv.setText(String.format("%.1f", bwCheckResult.uploadSpeed));
            clientDownwardTv.setText(String.format("%.1f", bwCheckResult.downloadSpeed));
            clientLossTv.setText(String.format("%d", bwCheckResult.loss));
            clientLatencyTv.setText(String.format("%d", bwCheckResult.latency));
            clientUpwardRefTv.setText(Html.fromHtml(getTestRefRichText("上行", bwCheckResult.uploadSpeed)));
            clientDownwardRefTv.setText(Html.fromHtml(getTestRefRichText("下行", bwCheckResult.downloadSpeed)));
        } else {
            resultTitleTv.setText("设备");
            deviceTv.setText(bwCheckResult.deviceSerial + "@" + bwCheckResult.cameraNo);
            clientResultLayout.setVisibility(View.GONE);
            deviceResultLayout.setVisibility(View.VISIBLE);
            deviceUpwardTv.setText(String.format("%.1f", bwCheckResult.uploadSpeed));
            deviceLossTv.setText(String.format("%d", bwCheckResult.loss));
            deviceLatencyTv.setText(String.format("%d", bwCheckResult.latency));
            deviceUpwardRefTv.setText(Html.fromHtml(getTestRefRichText("上行", bwCheckResult.uploadSpeed)));
        }
        ispTv.setText(bwCheckResult.isp);
        ipTv.setText(bwCheckResult.exIp);
        timeTv.setText(bwCheckResult.checkTime);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {// Android 14+ (API 34+) 需要 READ_MEDIA_VIDEO 权限
            if (!(checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {// Android 10-13 需要读写权限
            if (!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {// Android 6.0-9.0 只需要写权限
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
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
        captureAndSaveView();
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