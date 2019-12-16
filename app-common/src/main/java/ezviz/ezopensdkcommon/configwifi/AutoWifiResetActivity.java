package ezviz.ezopensdkcommon.configwifi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import ezviz.ezopensdkcommon.R;
import ezviz.ezopensdkcommon.common.RootActivity;
import ezviz.ezopensdkcommon.common.TitleBar;

//import com.videogo.main.CustomApplication;

public class AutoWifiResetActivity extends RootActivity implements OnClickListener {

    private View btnNext;
    private TextView topTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        ((CustomApplication) getApplication()).addSingleActivity(AutoWifiResetActivity.class.getName(), this);
        // 页面统计
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_wifi_reset);
        initTitleBar();
        initUI();
    }

    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.device_reset_title);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void initUI() {
        topTip = (TextView) findViewById(R.id.topTip);
        btnNext = findViewById(R.id.btnNext);
        topTip.setText(R.string.device_reset_tip);
        btnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        int id = v.getId();
        if (id == R.id.btnNext){
            intent = new Intent(this, AutoWifiNetConfigActivity.class);
            intent.putExtras(getIntent());
            startActivity(intent);
        }
//        switch (v.getId()) {
//            case R.id.btnNext:
//                intent = new Intent(this, AutoWifiNetConfigActivity.class);
//                intent.putExtras(getIntent());
//                startActivity(intent);
//                break;
//            default:
//                break;
//        }
    }
}
