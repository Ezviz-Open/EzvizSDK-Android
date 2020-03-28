package com.videogo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.widget.TopBar;

import ezviz.ezopensdk.R;
import ezviz.ezopensdk.demo.ValueKeys;

public class SupportActivity extends RootActivity {

    private EditText mAppkeyTv;
    private EditText mAccesstokenTv;
    private EditText mApiUrlTv;
    private EditText mWebUrlTv;
    private EditText mDeviceSerialTv;
    private String mAppkey;
    private String mAccesstoken;
    private String mApiUrl;
    private String mWebUrl;
    private String mDeviceSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        TopBar topBar = (TopBar) findViewById(R.id.topbar);
        topBar.setOnTopbarClickListener(new TopBar.OnTopbarClickListener() {
            @Override
            public void onLeftButtonClicked() {
                finish();
            }

            @Override
            public void onRightButtonClicked() {

            }
        });
        mAppkeyTv = (EditText) findViewById(R.id.edit_appkey);
        mAccesstokenTv = (EditText) findViewById(R.id.edit_accesstoken);
        mApiUrlTv = (EditText) findViewById(R.id.edit_apiurl);
        mWebUrlTv = (EditText) findViewById(R.id.edit_weburl);
        TextView mEnsureTv = (TextView) findViewById(R.id.ok_tv);
        mDeviceSerialTv = (EditText) findViewById(R.id.edit_deviceserial);
        mEnsureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAppkey = mAppkeyTv.getText().toString().trim();
                mAccesstoken = mAccesstokenTv.getText().toString().trim();
                mApiUrl = mApiUrlTv.getText().toString().trim();
                mWebUrl = mWebUrlTv.getText().toString().trim();
                mDeviceSerial = mDeviceSerialTv.getText().toString().trim();

                if (TextUtils.isEmpty(mAppkey)){
                    showToast("appkey is null");
                    return;
                }
                EZOpenSDK.initLib(SupportActivity.this.getApplication(),mAppkey);
                if(!TextUtils.isEmpty(mApiUrl) && !TextUtils.isEmpty(mWebUrl)){
                    EzvizAPI.getInstance().setServerUrl(mApiUrl, mWebUrl);
                }
                if (!TextUtils.isEmpty(mAccesstoken)){
                    getOpenSDK().setAccessToken(mAccesstoken);
                    Intent intent = new Intent(getApplicationContext(),EZCameraListActivity.class);

                    if (!TextUtils.isEmpty(mDeviceSerial)){
                        intent.putExtra(ValueKeys.DEVICE_SERIAL.name(),mDeviceSerial);
                    }
                    startActivity(intent);
//                    finish();
                }
//                finish();
            }

        });

    }
}
