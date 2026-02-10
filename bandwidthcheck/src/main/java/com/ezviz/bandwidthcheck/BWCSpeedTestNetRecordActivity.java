/**
 * 带宽检测历史记录页面
 */
package com.ezviz.bandwidthcheck;

import static com.ezviz.bandwidthcheck.BWCSpeedTestInputActivity.EXTRA_BWCHECK_TYPE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ezviz.common.RootActivity;
import com.ezviz.common.TitleBar;
import com.ezviz.demo.DemoConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.videogo.bandwidthcheck.EZBWCheckResult;
import com.videogo.openapi.EZConstants;

import java.util.ArrayList;
import java.util.List;

public class BWCSpeedTestNetRecordActivity extends RootActivity {

    // region --- 全局变量|global variable ---

    private static final String TAG = BWCSpeedTestNetRecordActivity.class.getSimpleName();

    private ListView listView;
    private LinearLayout noDataLL;

    private List<EZBWCheckResult> bwCheckResultList;
    private EZBWCheckNetRecordAdapter recordAdapter;

    // 以下为上一个页面传递的数据
    private int bwCheckType;
    // 以上为上一个页面传递的数据

    // endregion

    // region --- 页面启动方法|Page startup method ---

    public static void launch(Context context, int bwCheckType) {
        Intent intent = new Intent(context, BWCSpeedTestNetRecordActivity.class);
        intent.putExtra(EXTRA_BWCHECK_TYPE, bwCheckType);
        context.startActivity(intent);
    }

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bwc_speed_test_net_record);
        initTitleBar();
        initUI();
        initData();
    }

    // endregion

    // region --- int ---

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle("测速记录");
        mTitleBar.addBackButton(v -> onBackPressed());
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    private void initData() {
        Intent intent = getIntent();
        bwCheckType = intent.getIntExtra(EXTRA_BWCHECK_TYPE, 1);

        String bcCheckResultKey = bwCheckType == EZConstants.EZBWCheckType.EZBWCheckClientUpward ?
                DemoConfig.SP_BWCHECK_RESULT_DEVICE : DemoConfig.SP_BWCHECK_RESULT_CLIENT;
        SharedPreferences sp = getSharedPreferences(DemoConfig.SP_BWCHECK_RESULT, Context.MODE_PRIVATE);
        String resultsString = sp.getString(bcCheckResultKey, "");
        Gson gson = new Gson();
        bwCheckResultList = gson.fromJson(resultsString, new TypeToken<List<EZBWCheckResult>>() {
        }.getType());
        if (bwCheckResultList == null) {
            noDataLL.setVisibility(View.VISIBLE);
        } else {
            noDataLL.setVisibility(View.GONE);
            recordAdapter.setData(bwCheckResultList);
            recordAdapter.notifyDataSetChanged();
        }
    }

    private void initUI() {
        listView = findViewById(R.id.listView);
        noDataLL = findViewById(R.id.ll_no_data);
        recordAdapter = new EZBWCheckNetRecordAdapter(this);
        listView.setAdapter(recordAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> BWCSpeedTestResultActivity.launch(BWCSpeedTestNetRecordActivity.this, bwCheckResultList.get(position), false));
    }

    // endregion

    // region --- Adapter ---

    private class EZBWCheckNetRecordAdapter extends BaseAdapter {
        protected List<EZBWCheckResult> mData = new ArrayList<>();
        private LayoutInflater inflater;

        public EZBWCheckNetRecordAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<EZBWCheckResult> list) {
            mData.clear();
            mData.addAll(list);
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            EZBWCheckResult resultInfo = mData.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_bwc_speed_test_net_record, parent, false);
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.text1);
                viewHolder.tv_checkTime = (TextView) convertView.findViewById(R.id.text2);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_title.setText(bwCheckType == EZConstants.EZBWCheckType.EZBWCheckClientUpward ?
                    resultInfo.deviceSerial + "@" + resultInfo.cameraNo : resultInfo.networkName);
            viewHolder.tv_checkTime.setText(resultInfo.checkTime);

            return convertView;
        }

        class ViewHolder {
            TextView tv_title;
            TextView tv_checkTime;
        }
    }

    // endregion

}