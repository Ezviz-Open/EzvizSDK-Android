/* 
 * @ProjectName VideoGo
 * @Copyright null
 * 
 * @FileName GatherWifiInfoActivity.java
 * @Description 获取wifi设备信息
 * 
 * @author YaoCongMing
 * @data 2013-11-5
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */

package com.videogo.ui.devicelist;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.videogo.widget.TitleBar;

import ezviz.ezopensdkcommon.R;

public class LineConnectgIntroduceActivity extends Activity {
    public static String FROM_PAGE = "from_page";

    public static int FROM_PAGE_DEVICE_DETAIL = 1;

    public static int FROM_PAGE_WIFI_CONFIG = 2;

    private TitleBar mTitleBar;

    TextView tvIntroduce1;

    private TextView tvIntroduce2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.line_connecting_introduce_activity);
        tvIntroduce1 = (TextView) findViewById(R.id.tvIntroduce1);
        tvIntroduce2 = (TextView) findViewById(R.id.tvIntroduce2);
        initTitleBar();

        int fromPage = getIntent().getIntExtra(FROM_PAGE, 0);
        if (fromPage == FROM_PAGE_WIFI_CONFIG) {
            tvIntroduce1.setText(R.string.connect_device_to_router);
            tvIntroduce2.setVisibility(View.INVISIBLE);
        } else {
            tvIntroduce1.setText(R.string.device_wificonfig_hasline_introduce);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initTitleBar() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.wired_connection);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
