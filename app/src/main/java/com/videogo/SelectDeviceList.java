package com.videogo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.videogo.ui.ddns.EZDDNSListActivity;
import ezviz.ezopensdkcommon.R;

public class SelectDeviceList extends Activity implements View.OnClickListener {

    private Button mOnlineDeviceBtn;
    private View mDDNSDeviceBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mOnlineDeviceBtn = (Button) findViewById(R.id.btn_online_device);
        mDDNSDeviceBtn = findViewById(R.id.btn_ddns);

        mOnlineDeviceBtn.setOnClickListener(this);
        mDDNSDeviceBtn.setOnClickListener(this);

        //mOnlineDeviceBtn.post(new Runnable() {
        //    @Override
        //    public void run() {
        //        Intent toIntent = new Intent(SelectDeviceList.this, com.videogo.ui.cameralist.EZCameraListActivity.class);
        //        startActivity(toIntent);
        //    }
        //});
    }

    @Override
    public void onClick(View v) {
        if (v == mOnlineDeviceBtn){
            Intent toIntent = new Intent(this, com.videogo.ui.cameralist.EZCameraListActivity.class);
            startActivity(toIntent);
            //EZAuthAPI.sendOpenPage(this, EZAuthAPI.EZAuthSDKOpenPage.OpenPage_DeviceList);
        }else if(v == mDDNSDeviceBtn){
            Intent toIntent = new Intent(this, EZDDNSListActivity.class);
            startActivity(toIntent);

            //EZAuthAPI.sendOpenPage(this, EZAuthAPI.EZAuthSDKOpenPage.OpenPage_AlarmList);

        }
    }
}
