package com.videogo.ui.LanDevice;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.videogo.openapi.EZHCNetDeviceSDK;
import com.videogo.constant.IntentConsts;
import com.videogo.util.Utils;

import ezviz.ezopensdk.R;

public class LanDeviceActivateActivity extends Activity implements View.OnClickListener {
    private static final String TAG = LanDeviceActivateActivity.class.getName();
    EditText mPasswordETV;
    Button mActivateBtn;
    private String mSeriNo;
    private TextView mTitleTextView;
    private String mPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hcactive);
        mTitleTextView = (TextView) findViewById(R.id.title_text);
        mPasswordETV = (EditText) findViewById(R.id.passwordETV);
        mActivateBtn = (Button) findViewById(R.id.activateBtn);
        mActivateBtn.setOnClickListener(this);
        mSeriNo = getIntent().getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
    }

    @Override
    public void onClick(View v) {
        if (v == mActivateBtn){
            mPassword = mPasswordETV.getText().toString().trim();
            if (TextUtils.isEmpty(mPassword)) {
                Utils.showToast(this, R.string.sadp_password_toast);
                return;
            }
            activateDevice();
        }
    }

    private void activateDevice(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int result = EZHCNetDeviceSDK.getInstance().activeDeviceWithSerial(mSeriNo, mPassword);
                if (result == 1) {
                    // TODO: 2017/8/15 Activation successful
                    setResult(LanDeviceActivity.RESULT_OK,null);
                    finish();
                    return;
                }else {
                    //TODO: 2017/8/15 Activation failed
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(result == 2020){
                                // TODO: 2017/8/16 密码太弱
                                Utils.showToast(LanDeviceActivateActivity.this, R.string.sadp_password_too_weak);
                            }else{
                                Utils.showToast(LanDeviceActivateActivity.this, R.string.title_activate_device_fail);
                            }
                        }
                    });
                }
            }
        }).start();
    }
}
