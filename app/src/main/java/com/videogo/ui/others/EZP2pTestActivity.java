package com.videogo.ui.others;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LogUtil;
import com.videogo.widget.TitleBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

import static com.videogo.EzvizApplication.getOpenSDK;

public class EZP2pTestActivity extends RootActivity {

    protected static final String TAG = EZP2pTestActivity.class.getSimpleName();;

    private TextView tv_p2pInfo;
    private ListView listView;

    private EZDeviceAdapter deviceAdapter;
    private List<EZDeviceInfo> deviceArray;

    private ArrayList<String> processedPreconnectSerialArray;
    private ArrayList<String> toDoPreconnectSerialArray;
    private EZDeviceInfo selectDevice;
    /**
     * 计时器
     */
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ezp2p_test);
        initTitleBar();

        tv_p2pInfo = findViewById(R.id.tv_p2pInfo);
        listView = findViewById(R.id.listView);
        deviceAdapter = new EZDeviceAdapter(this);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectDevice = deviceArray.get(position);
                deviceAdapter.notifyDataSetChanged();
            }
        });
        new GetCamersInfoListTask().execute();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> refreshAction());
            }
        }, 1000, 1000);
    }

    @Override
    protected void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroy();
    }

    /**
     * 连接Action
     */
    public void connectAction(View view) {
        if (selectDevice == null) {
            Toast.makeText(this, "请先选择一个设备", Toast.LENGTH_LONG).show();
            return;
        }
        getOpenSDK().startP2PPreconnect(selectDevice.getDeviceSerial());
    }

    /**
     * 断开连接Action
     */
    public void disconnectAction(View view) {
        if (selectDevice == null) {
            Toast.makeText(this, "请先选择一个设备", Toast.LENGTH_LONG).show();
            return;
        }
        getOpenSDK().clearP2PPreconnect(selectDevice.getDeviceSerial());
    }

    /**
     * 设备p2p预连接是否成功
     */
    public void isPreConnectionSucceedAction(View view) {
        if (selectDevice == null) {
            Toast.makeText(this, "请先选择一个设备", Toast.LENGTH_LONG).show();
            return;
        }
        boolean isSuccess = getOpenSDK().isPreConnectionSucceed(selectDevice.getDeviceSerial());
        Toast.makeText(this, isSuccess ? "Success" : "Failed", Toast.LENGTH_LONG).show();
    }

    /**
     * 刷新Action
     */
    public void refreshAction() {
        processedPreconnectSerialArray = getOpenSDK().getAllProcessedPreconnectSerials();
        toDoPreconnectSerialArray = getOpenSDK().getAllToDoPreconnectSerials();
        deviceAdapter.notifyDataSetChanged();
        tv_p2pInfo.setText(String.format("p2p预连接设备数量：%s\n正在排队的p2p预连接设备数量：%s", processedPreconnectSerialArray.size(),
                toDoPreconnectSerialArray.size()));
    }

    private class GetCamersInfoListTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {

        public GetCamersInfoListTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... params) {
            if (!ConnectionDetector.isNetworkAvailable(EZP2pTestActivity.this)) {
                return null;
            }
            try {
                return getOpenSDK().getDeviceList(0, 20);

            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                LogUtil.d(TAG, errorInfo.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> result) {
            super.onPostExecute(result);
            if (result != null) {
                deviceArray = result;
                deviceAdapter.setData(result);
                deviceAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Adapter
     */
    private class EZDeviceAdapter extends BaseAdapter {
        protected List<EZDeviceInfo> mData = new ArrayList<EZDeviceInfo>();
        private LayoutInflater inflater;

        public EZDeviceAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<EZDeviceInfo> list) {
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
            EZDeviceInfo info = mData.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                viewHolder.tv_name = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tv_name.setText(TextUtils.isEmpty(info.getDeviceName()) ?
                    String.format("%s(%s)", info.getCategory(), info.getDeviceSerial()) : info.getDeviceName());
            // 设置title颜色
            if (processedPreconnectSerialArray != null && processedPreconnectSerialArray.contains(info.getDeviceSerial())) {
                viewHolder.tv_name.setTextColor(getResources().getColor(R.color.yellow_text));
            } else {
                viewHolder.tv_name.setTextColor(getResources().getColor(R.color.black));
            }
            // 设置选中状态
            if (selectDevice != null && selectDevice.getDeviceSerial().equals(info.getDeviceSerial())) {
                viewHolder.tv_name.setBackgroundColor(getResources().getColor(R.color.gray));
            } else {
                viewHolder.tv_name.setBackgroundColor(getResources().getColor(R.color.white));
            }

            return convertView;
        }

        class ViewHolder {
            TextView tv_name;
        }
    }

    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.addBackButton(view -> finish());
        mTitleBar.setTitle("p2p测试");
    }
}