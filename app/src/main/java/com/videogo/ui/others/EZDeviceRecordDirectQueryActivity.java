package com.videogo.ui.others;

import static com.videogo.EzvizApplication.getOpenSDK;
import static com.videogo.openapi.EZConstants.EZVideoRecordLocation.EZ_VIDEO_RECORD_LOCATION_LOCAL;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordInfo;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.LogUtil;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.TitleBar;
import com.videogo.widget.pulltorefresh.IPullToRefresh;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase;
import com.videogo.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

public class EZDeviceRecordDirectQueryActivity extends RootActivity {

    protected static final String TAG = EZDeviceRecordDirectQueryActivity.class.getSimpleName();

    private static final int PAGE_SIZE = 20;

    // 录像列表
    private PullToRefreshListView mListView;
    private View mNoMoreView;
    private EZRecordAdapter recordAdapter;

    // 上一次录像查询对象
    private EZDeviceRecordInfo lastestDeviceRecordInfo;

    // 以下为设备列表页面通过Bundle传递的数据
    private String deviceSerial;
    // 以上为设备列表页面通过Bundle传递的数据

    public static void launch(Context context, String deviceSerial) {
        Intent intent = new Intent(context, EZDeviceRecordDirectQueryActivity.class);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, deviceSerial);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_record_direct_query);
        initTitleBar();
        deviceSerial = getIntent().getStringExtra(IntentConsts.EXTRA_DEVICE_ID);

        mNoMoreView = getLayoutInflater().inflate(R.layout.no_device_more_footer, null);
        ((TextView) mNoMoreView.findViewById(R.id.no_more_text)).setText("无更多录像");
        recordAdapter = new EZRecordAdapter(this);
        mListView = findViewById(R.id.listview);
        mListView.setLoadingLayoutCreator(new PullToRefreshBase.LoadingLayoutCreator() {

            @Override
            public LoadingLayout create(Context context, boolean headerOrFooter,
                                        PullToRefreshBase.Orientation orientation) {
                if (headerOrFooter) return new PullToRefreshHeader(context);
                else return new PullToRefreshFooter(context, PullToRefreshFooter.Style.EMPTY_NO_MORE);
            }
        });
        mListView.setMode(IPullToRefresh.Mode.BOTH);
        mListView.setOnRefreshListener((refreshView, headerOrFooter) -> getDeviceRecordList(headerOrFooter));
        mListView.getRefreshableView().addFooterView(mNoMoreView);
        mListView.setAdapter(recordAdapter);
        mListView.getRefreshableView().removeFooterView(mNoMoreView);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            EZDeviceRecordFile recordFile = (EZDeviceRecordFile) parent.getItemAtPosition(position);

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recordAdapter != null && recordAdapter.getCount() == 0) {
            refreshButtonClicked();
        }
    }

    private void getDeviceRecordList(boolean headerOrFooter) {
        if (this.isFinishing()) {
            return;
        }
        new GetDeviceRecordListTask(headerOrFooter).execute();
    }

    private void refreshButtonClicked() {
        mListView.setVisibility(View.VISIBLE);
        mListView.setMode(IPullToRefresh.Mode.BOTH);
        mListView.setRefreshing();
    }

    private class GetDeviceRecordListTask extends AsyncTask<Void, Void, List<EZDeviceRecordFile>> {
        private boolean mHeaderOrFooter;

        public GetDeviceRecordListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mHeaderOrFooter) {
                mListView.setVisibility(View.VISIBLE);
            }
            mListView.getRefreshableView().removeFooterView(mNoMoreView);
        }

        @Override
        protected List<EZDeviceRecordFile> doInBackground(Void... params) {
            if (EZDeviceRecordDirectQueryActivity.this.isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(EZDeviceRecordDirectQueryActivity.this)) {
                return null;
            }
            try {
                Date beginDate = DateTimeUtil.beginDate(new Date());
                Date endDate = DateTimeUtil.endDate(new Date());
                Calendar startTime = Calendar.getInstance();
                Calendar endTime = Calendar.getInstance();
                startTime.setTime(beginDate);
                endTime.setTime(endDate);
                // 开始时间
                Calendar beginTime = mHeaderOrFooter ? startTime : lastestDeviceRecordInfo.getNextFileTime();
                EZDeviceRecordInfo deviceRecordInfo = getOpenSDK().searchRecordFileFromDeviceEx(deviceSerial, 1, beginTime,
                        endTime, EZConstants.EZVideoRecordTypeEx.EZ_VIDEO_RECORD_TYPE_NONE, false,
                        EZ_VIDEO_RECORD_LOCATION_LOCAL, PAGE_SIZE);
                lastestDeviceRecordInfo = deviceRecordInfo;
                return deviceRecordInfo.getDeviceRecordFileList();

            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                LogUtil.d(TAG, errorInfo.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EZDeviceRecordFile> result) {
            super.onPostExecute(result);
            mListView.onRefreshComplete();
            if (EZDeviceRecordDirectQueryActivity.this.isFinishing()) {
                return;
            }
            if (result != null) {
                // 下拉刷新，清空适配器数据
                if (mHeaderOrFooter) {
                    CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
                    for (LoadingLayout layout : mListView.getLoadingLayoutProxy(true, false).getLayouts()) {
                        ((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
                    }
                    recordAdapter.clearItem();
                }
                if (recordAdapter.getCount() == 0 && result.isEmpty()) {
                    mListView.setVisibility(View.GONE);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                } else if (result.size() < PAGE_SIZE) {
                    mListView.setFooterRefreshEnabled(false);
                    mListView.getRefreshableView().addFooterView(mNoMoreView);
                } else if (mHeaderOrFooter) {
                    mListView.setFooterRefreshEnabled(true);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }
                recordAdapter.addItems(result);
                recordAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Adapter
     */
    private class EZRecordAdapter extends BaseAdapter {
        protected List<EZDeviceRecordFile> mData = new ArrayList<>();
        private LayoutInflater inflater;

        public EZRecordAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<EZDeviceRecordFile> list) {
            mData.clear();
            mData.addAll(list);
        }

        public void addItems(List<EZDeviceRecordFile> list) {
            mData.addAll(list);
        }

        public void clearItem() {
            mData.clear();
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
            EZDeviceRecordFile info = mData.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                viewHolder.tv_name = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            int diffSeconds =
                    (int) (info.getStopTime().getTimeInMillis() - info.getStartTime().getTimeInMillis()) / 1000;
            int minute = diffSeconds / 60;
            int seconds = diffSeconds % 60;
            String during = minute + "'" + seconds + "\"";

            viewHolder.tv_name.setText(String.format("%s - %s (%s)",
                    DateTimeUtil.calendarToHMSString(info.getStartTime()),
                    DateTimeUtil.calendarToHMSString(info.getStopTime()), during));

            return convertView;
        }

        class ViewHolder {
            TextView tv_name;
        }
    }

    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.addBackButton(view -> finish());
        mTitleBar.setTitle("NVR下的IPC设备录像直查");
    }
}