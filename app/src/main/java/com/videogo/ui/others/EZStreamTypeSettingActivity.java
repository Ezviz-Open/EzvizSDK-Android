package com.videogo.ui.others;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.videogo.global.GlobalKit;
import com.videogo.openapi.EzvizAPI;
import com.videogo.widget.TitleBar;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

import static com.videogo.EzvizApplication.getOpenSDK;

public class EZStreamTypeSettingActivity extends RootActivity implements CompoundButton.OnCheckedChangeListener {

    protected static final String TAG = EZStreamTypeSettingActivity.class.getSimpleName();

    private Switch directInnerSwitch;
    private Switch p2pSwitch;
    private Switch vtduSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ezstream_type_setting);

        directInnerSwitch = findViewById(R.id.switch_directInner);
        p2pSwitch = findViewById(R.id.switch_p2p);
        vtduSwitch = findViewById(R.id.switch_vtdu);
        directInnerSwitch.setChecked(GlobalKit.getInstance().enableDirectInner);
        p2pSwitch.setChecked(GlobalKit.getInstance().enableP2P);
        vtduSwitch.setChecked(GlobalKit.getInstance().enableVTDU);

        directInnerSwitch.setOnCheckedChangeListener(this);
        p2pSwitch.setOnCheckedChangeListener(this);
        vtduSwitch.setOnCheckedChangeListener(this);

        if (EzvizAPI.getInstance().isUsingGlobalSDK()) {
            directInnerSwitch.setEnabled(false);
            vtduSwitch.setEnabled(false);
        }

        initTitleBar();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.switch_directInner) {
            getOpenSDK().enableDirectInner(isChecked);
            GlobalKit.getInstance().enableDirectInner = isChecked;
        } else if (id == R.id.switch_p2p) {
            getOpenSDK().enableP2P(isChecked);
            GlobalKit.getInstance().enableP2P = isChecked;
        } else if (id == R.id.switch_vtdu) {
            getOpenSDK().enableVTDU(isChecked);
            GlobalKit.getInstance().enableVTDU = isChecked;
        }
    }


    /***************** UI *******************/

    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.addBackButton(view -> finish());
        mTitleBar.setTitle("取流方式设置");
    }
}