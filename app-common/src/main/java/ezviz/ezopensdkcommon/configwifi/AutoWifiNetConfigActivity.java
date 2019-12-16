package ezviz.ezopensdkcommon.configwifi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ezviz.sdk.configwifi.WiFiUtils;
import com.ezviz.sdk.configwifi.ap.ConnectionDetector;
import com.hikvision.wifi.configuration.BaseUtil;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_wifi_net_config);
        initTitleBar();
        findViews();
        initUI();
        setListener();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (ConnectionDetector.getConnectionType(this) != ConnectionDetector.WIFI) {
            tvSSID.setText(R.string.unknow_ssid);
            showWifiRequiredDialog();
        } else {
            tvSSID.setText(WiFiUtils.getCurrentWifiSsid(this));
        }
    }


    private void findViews() {
        btnNext = findViewById(R.id.btnNext);
        tvSSID = findViewById(R.id.tvSSID);
        edtPassword = findViewById(R.id.edtPassword);
    }


    private void initUI() {
        tvTitle.setText(R.string.auto_wifi_cer_config_title2);
        tvSSID.setText(BaseUtil.getWifiSSID(this));
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

    private void goToChooseConfigWifiWay(){
        Intent toConfigIntent = new Intent(mContext, ManualChooseConfigWifiWayActivity.class);
        toConfigIntent.putExtras(getIntent());
        toConfigIntent.putExtra(IntentConstants.ROUTER_WIFI_SSID, tvSSID.getText().toString());
        toConfigIntent.putExtra(IntentConstants.ROUTER_WIFI_PASSWORD, TextUtils.isEmpty(edtPassword.getText().toString())
                ? "smile" : edtPassword.getText().toString());
        startActivity(toConfigIntent);
    }
}
