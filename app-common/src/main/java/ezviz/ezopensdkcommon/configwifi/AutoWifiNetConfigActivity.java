package ezviz.ezopensdkcommon.configwifi;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ezviz.sdk.configwifi.WiFiUtils;
import com.ezviz.sdk.configwifi.ap.ConnectionDetector;
import com.hikvision.wifi.configuration.BaseUtil;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdkcommon.R;
import ezviz.ezopensdkcommon.common.IntentConstants;
import ezviz.ezopensdkcommon.common.RootActivity;
import ezviz.ezopensdkcommon.common.TitleBar;

public class AutoWifiNetConfigActivity extends RootActivity {

    public static final String WIFI_PASSWORD = "wifi_password";
    public static final String WIFI_SSID = "wifi_ssid";
    public static final String DEVICE_TYPE = "device_type";

    private Button btnNext;
    private TextView tvSSID;
    private EditText edtPassword;
    private TextView tvTitle;
    /**
     * true为应用权限管理返回
     */
    private boolean isFromPermissionSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_wifi_net_config);
        initTitleBar();
        findViews();
        initUI();
        setListener();
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromPermissionSetting) {
            checkPermissions();
            isFromPermissionSetting = false;
        }
        if (ConnectionDetector.getConnectionType(this) != ConnectionDetector.WIFI) {
            tvSSID.setText(R.string.unknow_ssid);
            showWifiRequiredDialog();
        } else {
            updateWifiInfo();
        }
    }

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        tvTitle = mTitleBar.setTitle(R.string.auto_wifi_cer_config_title1);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void showWifiRequiredDialog() {

        new AlertDialog.Builder(this).setTitle(R.string.auto_wifi_dialog_title_wifi_required)
                .setMessage(R.string.please_open_wifi_network)
                .setNegativeButton(R.string.auto_wifi_dialog_btn_wifi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {

                        dialog.dismiss();
                        // 跳转wifi设置界面
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    }
                }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onBackPressed();
            }
        }).setCancelable(false).create().show();
    }

    private void updateWifiInfo() {
        // 优先使用getCurrentWifiSsid方法获取wifi名
        String wifiName = WiFiUtils.getCurrentWifiSsid(this);
        // 如上述方式无效，则使用getWifiSSID方法进行获取
        if (!isValidWifiSSID(wifiName)) {
            wifiName = BaseUtil.getWifiSSID(this);
        }
        if (isValidWifiSSID(wifiName)) {
            tvSSID.setText(wifiName);
        }
        tvSSID.setText("ezviz_mobile_AV");
        edtPassword.setText("chengjun");
    }

    private boolean isValidWifiSSID(String wifiName) {
        return !TextUtils.isEmpty(wifiName) && !"<unknown ssid>".equalsIgnoreCase(wifiName);
    }

    private void findViews() {
        btnNext = findViewById(R.id.btnNext);
        tvSSID = findViewById(R.id.tvSSID);
        edtPassword = findViewById(R.id.edtPassword);
    }


    private void initUI() {
        tvTitle.setText(R.string.auto_wifi_cer_config_title2);
        String password = "";
        edtPassword.setText(password);
    }


    private void setListener() {
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChooseConfigWifiWay();
            }
        });
    }

    private void goToChooseConfigWifiWay() {
        Intent toConfigIntent = new Intent(mContext, ManualChooseConfigWifiWayActivity.class);
        toConfigIntent.putExtras(getIntent());
        toConfigIntent.putExtra(IntentConstants.ROUTER_WIFI_SSID, tvSSID.getText().toString());
        toConfigIntent.putExtra(IntentConstants.ROUTER_WIFI_PASSWORD, TextUtils.isEmpty(edtPassword.getText().toString())
                ? "smile" : edtPassword.getText().toString());
        // 此处可以把WIFI名+密码存到Map中，再保存到本地文件中。这样以后匹配到已经保存的WIFI名，可以把密码也设置上，省去手动输入密码的步骤。萤石云视频App就是这么做的。
        // TODO
        startActivity(toConfigIntent);
    }

    /**
     * ***********************以下为动态权限请求，没有权限会影响到配网*****************************
     */
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
        android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(this)
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
        dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.black));
        //设置居中，解决Android9.0 AlertDialog不居中问题
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        p.width = (int) (metric.widthPixels * 0.9);
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
}
