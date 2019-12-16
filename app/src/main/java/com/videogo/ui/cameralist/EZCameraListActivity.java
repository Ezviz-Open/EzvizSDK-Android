/* 
 * @ProjectName VideoGoJar
 * @Copyright null
 * 
 * @FileName CameraListActivity.java
 * @Description 这里对文件进行描述
 * 
 * @author xia xingsuo
 * @data 2015-11-5
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.cameralist;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ezviz.stream.EZStreamClientManager;
import com.videogo.RootActivity;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.devicemgt.EZDeviceSettingActivity;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.remoteplayback.list.EZPlayBackListActivity;
import com.videogo.remoteplayback.list.RemoteListContant;
import com.videogo.scan.main.CaptureActivity;
import com.videogo.ui.message.EZMessageActivity2;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.ui.util.EZUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshFooter.Style;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.pulltorefresh.IPullToRefresh.Mode;
import com.videogo.widget.pulltorefresh.IPullToRefresh.OnRefreshListener;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.LoadingLayoutCreator;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.Orientation;
import com.videogo.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;
import ezviz.ezopensdk.debug.TestActivityForFullSdk;
import ezviz.ezopensdk.demo.DemoConfig;
import ezviz.ezopensdk.demo.ValueKeys;

import static com.ezviz.stream.EZError.EZ_OK;
import static com.videogo.EzvizApplication.getOpenSDK;


public class EZCameraListActivity extends RootActivity implements OnClickListener, SelectCameraDialog.CameraItemClick {
    protected static final String TAG = "CameraListActivity";
    public final static int REQUEST_CODE = 100;
    public final static int RESULT_CODE = 101;
    private final static int SHOW_DIALOG_DEL_DEVICE = 1;

    private BroadcastReceiver mReceiver = null;

    private PullToRefreshListView mListView = null;
    private View mNoMoreView;
    private EZCameraListAdapter mAdapter = null;

    private LinearLayout mNoCameraTipLy = null;
    private LinearLayout mGetCameraFailTipLy = null;
    private TextView mCameraFailTipTv = null;
    private Button mAddBtn;
    private Button mUserBtn;
    private TextView mMyDevice;
    private TextView mShareDevice;

    private boolean bIsFromSetting = false;

    public final static int TAG_CLICK_PLAY = 1;
    public final static int TAG_CLICK_REMOTE_PLAY_BACK = 2;
    public final static int TAG_CLICK_SET_DEVICE = 3;
    public final static int TAG_CLICK_ALARM_LIST = 4;

    private int mClickType;

    private final static int LOAD_MY_DEVICE = 0;
    private final static int LOAD_SHARE_DEVICE = 1;
    private int mLoadType = LOAD_MY_DEVICE;

    private String mSingleDeviceSerial="";

    @Override
    public void onCameraItemClick(EZDeviceInfo deviceInfo, int camera_index) {
        EZCameraInfo cameraInfo = null;
        Intent intent = null;
        switch (mClickType) {
            case TAG_CLICK_PLAY:
                cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, camera_index);
                if (cameraInfo == null) {
                    return;
                }
                int ret = EZStreamClientManager.create(getApplication().getApplicationContext()).clearTokens();
                if (EZ_OK == ret){
                    Log.i(TAG, "clearTokens: ok");
                }else{
                    Log.e(TAG, "clearTokens: faile");
                }
                intent = new Intent(EZCameraListActivity.this, EZRealPlayActivity.class);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case TAG_CLICK_REMOTE_PLAY_BACK:
                cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, camera_index);
                if (cameraInfo == null) {
                    return;
                }
                intent = new Intent(EZCameraListActivity.this, EZPlayBackListActivity.class);
                intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
                intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DemoConfig.isNeedJumpToTestPage){
            startActivity(new Intent(mContext, TestActivityForFullSdk.class));
        }

        setContentView(R.layout.cameralist_page);

        // 只展示单个设备
        mSingleDeviceSerial = getIntent().getStringExtra(ValueKeys.DEVICE_SERIAL.name());
        if (!TextUtils.isEmpty(mSingleDeviceSerial)){
            Log.e(TAG, "only show the device which serial is " + mSingleDeviceSerial );
        }

        initData();
        initView();
    }

    private void initView() {
        mMyDevice = (TextView) findViewById(R.id.text_my);
        mShareDevice = (TextView) findViewById(R.id.text_share);
        mAddBtn = (Button) findViewById(R.id.btn_add);
        mUserBtn = (Button) findViewById(R.id.btn_user);
        mUserBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popLogoutDialog();
            }
        });

        mAddBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EZCameraListActivity.this, CaptureActivity.class);
                startActivity(intent);

            }
        });

        mShareDevice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareDevice.setTextColor(getResources().getColor(R.color.orange_text));
                mMyDevice.setTextColor(getResources().getColor(R.color.black_text));
                mAdapter.clearAll();
                mLoadType = LOAD_SHARE_DEVICE;
                getCameraInfoList(true);
            }
        });

        mMyDevice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareDevice.setTextColor(getResources().getColor(R.color.black_text));
                mMyDevice.setTextColor(getResources().getColor(R.color.orange_text));
                mAdapter.clearAll();
                mLoadType = LOAD_MY_DEVICE;
                getCameraInfoList(true);
            }
        });
        mNoMoreView = getLayoutInflater().inflate(R.layout.no_device_more_footer, null);
        mAdapter = new EZCameraListAdapter(this);
        mAdapter.setOnClickListener(new EZCameraListAdapter.OnClickListener() {
            @Override
            public void onPlayClick(BaseAdapter adapter, View view, int position) {

                mClickType = TAG_CLICK_PLAY;
                final EZDeviceInfo deviceInfo = mAdapter.getItem(position);

                if (deviceInfo.getCameraNum() <= 0 || deviceInfo.getCameraInfoList() == null || deviceInfo.getCameraInfoList().size() <= 0) {
                    LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                    return;
                }
                if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
                    LogUtil.d(TAG, "cameralist have one camera");
                    final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }
                    int ret = EZStreamClientManager.create(getApplication().getApplicationContext()).clearTokens();
                    if (EZ_OK == ret){
                        Log.i(TAG, "clearTokens: ok");
                    }else{
                        Log.e(TAG, "clearTokens: fail");
                    }
                    Intent intent = new Intent(EZCameraListActivity.this, EZRealPlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                    startActivityForResult(intent, REQUEST_CODE);
                    return;
                }
                SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
                selectCameraDialog.setEZDeviceInfo(deviceInfo);
                selectCameraDialog.setCameraItemClick(EZCameraListActivity.this);
                selectCameraDialog.show(getFragmentManager(), "onPlayClick");
            }

            @Override
            public void onRemotePlayBackClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_REMOTE_PLAY_BACK;
                EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                if (deviceInfo.getCameraNum() <= 0 || deviceInfo.getCameraInfoList() == null || deviceInfo.getCameraInfoList().size() <= 0) {
                    LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                    return;
                }
                if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
                    LogUtil.d(TAG, "cameralist have one camera");
                    EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }
                    Intent intent = new Intent(EZCameraListActivity.this, EZPlayBackListActivity.class);
                    intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    startActivity(intent);
                    return;
                }
                SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
                selectCameraDialog.setEZDeviceInfo(deviceInfo);
                selectCameraDialog.setCameraItemClick(EZCameraListActivity.this);
                selectCameraDialog.show(getFragmentManager(), "RemotePlayBackClick");
            }

            @Override
            public void onSetDeviceClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_SET_DEVICE;
                EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                Intent intent = new Intent(EZCameraListActivity.this, EZDeviceSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(IntentConsts.EXTRA_DEVICE_INFO,deviceInfo);
                intent.putExtra("Bundle",bundle);
                startActivity(intent);
                bIsFromSetting = true;
            }

            @Override
            public void onDeleteClick(BaseAdapter adapter, View view, int position) {
                showDialog(SHOW_DIALOG_DEL_DEVICE);
            }

            @Override
            public void onAlarmListClick(BaseAdapter adapter, View view, int position) {
                mClickType = TAG_CLICK_ALARM_LIST;
                final EZDeviceInfo deviceInfo = mAdapter.getItem(position);
                LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
                Intent intent = new Intent(EZCameraListActivity.this, EZMessageActivity2.class);
                intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, deviceInfo.getDeviceSerial());
                startActivity(intent);
            }

            @Override
            public void onDevicePictureClick(BaseAdapter adapter, View view, int position) {
            }

            @Override
            public void onDeviceVideoClick(BaseAdapter adapter, View view, int position) {
            }

            @Override
            public void onDeviceDefenceClick(BaseAdapter adapter, View view,
                                             int position) {
            }

        });
        mListView = (PullToRefreshListView) findViewById(R.id.camera_listview);
        mListView.setLoadingLayoutCreator(new LoadingLayoutCreator() {

            @Override
            public LoadingLayout create(Context context, boolean headerOrFooter, Orientation orientation) {
                if (headerOrFooter)
                    return new PullToRefreshHeader(context);
                else
                    return new PullToRefreshFooter(context, Style.EMPTY_NO_MORE);
            }
        });
        mListView.setMode(Mode.BOTH);
        mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView, boolean headerOrFooter) {
                getCameraInfoList(headerOrFooter);
            }
        });
        mListView.getRefreshableView().addFooterView(mNoMoreView);
        mListView.setAdapter(mAdapter);
        mListView.getRefreshableView().removeFooterView(mNoMoreView);

        mNoCameraTipLy = (LinearLayout) findViewById(R.id.no_camera_tip_ly);
        mGetCameraFailTipLy = (LinearLayout) findViewById(R.id.get_camera_fail_tip_ly);
        mCameraFailTipTv = (TextView) findViewById(R.id.get_camera_list_fail_tv);
    }

    private void initData() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                LogUtil.debugLog(TAG, "onReceive:" + action);
                if (action.equals(Constant.ADD_DEVICE_SUCCESS_ACTION)) {
                    refreshButtonClicked();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ADD_DEVICE_SUCCESS_ACTION);
        registerReceiver(mReceiver, filter);
    }

    int count = 100;
    @Override
    protected void onResume() {
        super.onResume();
        if (bIsFromSetting || (mAdapter != null && mAdapter.getCount() == 0)) {
            refreshButtonClicked();
            bIsFromSetting = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.shutDownExecutorService();
        }
    }

    private void getCameraInfoList(boolean headerOrFooter) {
        if (this.isFinishing()) {
            return;
        }
        new GetCamersInfoListTask(headerOrFooter).execute();


    }

    /**
     * 多次点击打开调试页面
     */
    public void onClickTryOpenTestPage(View view) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastClickTime < 1000){
            mValidCount++;
        }else{
            mValidCount = 0;
        }
        // 短时间内连续点击至少5次，则打开测试页面
        if (mValidCount >= 5){
            startActivity(new Intent(mContext, TestActivityForFullSdk.class));
            showToast("test!!!");
            mValidCount = 0;
        }
        mLastClickTime = currentTime;
    }

    private int mValidCount = 0;
    private long mLastClickTime = 0;

    private class GetCamersInfoListTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {
        private boolean mHeaderOrFooter;
        private int mErrorCode = 0;

        public GetCamersInfoListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mListView.setFooterRefreshEnabled(true);
            if (mHeaderOrFooter) {
                mListView.setVisibility(View.VISIBLE);
                mNoCameraTipLy.setVisibility(View.GONE);
                mGetCameraFailTipLy.setVisibility(View.GONE);
            }
            mListView.getRefreshableView().removeFooterView(mNoMoreView);
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... params) {
            if (EZCameraListActivity.this.isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(EZCameraListActivity.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }
            try {
                List<EZDeviceInfo> result = null;
                if (!TextUtils.isEmpty(mSingleDeviceSerial)){
                    EZDeviceInfo deviceInfo = getOpenSDK().getDeviceInfo(mSingleDeviceSerial);
                    result = new ArrayList<EZDeviceInfo>();
                    result.add(deviceInfo);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "已过滤多余设备", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return result;
                }
                if (mLoadType == LOAD_MY_DEVICE) {

                    if (mHeaderOrFooter) {
                        result = getOpenSDK().getDeviceList(0, 20);
                    } else {
                        result = getOpenSDK().getDeviceList((mAdapter.getCount() / 20)+(mAdapter.getCount() % 20>0?1:0), 20);
                    }
                } else if (mLoadType == LOAD_SHARE_DEVICE) {
                    if (mHeaderOrFooter) {
                        result = getOpenSDK().getSharedDeviceList(0, 20);
                    } else {
                        result = getOpenSDK().getSharedDeviceList((mAdapter.getCount() / 20)+(mAdapter.getCount() % 20>0?1:0), 20);
                    }
                }
                return result;

            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.debugLog(TAG, errorInfo.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> result) {
            super.onPostExecute(result);
            mListView.onRefreshComplete();
            if (EZCameraListActivity.this.isFinishing()) {
                return;
            }

            if (result != null) {
                if (mHeaderOrFooter) {
                    CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
                    for (LoadingLayout layout : mListView.getLoadingLayoutProxy(true, false).getLayouts()) {
                        ((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
                    }
                    mAdapter.clearItem();
                }
                if (mAdapter.getCount() == 0 && result.size() == 0) {
                    mListView.setVisibility(View.GONE);
                    mNoCameraTipLy.setVisibility(View.VISIBLE);
                    mGetCameraFailTipLy.setVisibility(View.GONE);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                } else if (result.size() < 10) {
                    mListView.setFooterRefreshEnabled(false);
                    mListView.getRefreshableView().addFooterView(mNoMoreView);
                } else if (mHeaderOrFooter) {
                    mListView.setFooterRefreshEnabled(true);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }
                addCameraList(result);
                mAdapter.notifyDataSetChanged();
            }

            if (mErrorCode != 0) {
                onError(mErrorCode);
            }
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                    ActivityUtils.handleSessionException(EZCameraListActivity.this);
                    break;
                default:
                    if (mAdapter.getCount() == 0) {
                        mListView.setVisibility(View.GONE);
                        mNoCameraTipLy.setVisibility(View.GONE);
                        mCameraFailTipTv.setText(Utils.getErrorTip(EZCameraListActivity.this, R.string.get_camera_list_fail, errorCode));
                        mGetCameraFailTipLy.setVisibility(View.VISIBLE);
                    } else {
                        Utils.showToast(EZCameraListActivity.this, R.string.get_camera_list_fail, errorCode);
                    }
                    break;
            }
        }
    }

    private void addCameraList(List<EZDeviceInfo> result) {
        int count = result.size();
        EZDeviceInfo item = null;
        for (int i = 0; i < count; i++) {
            item = result.get(i);
            mAdapter.addItem(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_list_refresh_btn:
            case R.id.no_camera_tip_ly:
                refreshButtonClicked();
                break;
            case R.id.camera_list_gc_ly:
//                Intent intent = new Intent(EZCameraListActivity.this, SquareColumnActivity.class);
//                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void refreshButtonClicked() {
        mListView.setVisibility(View.VISIBLE);
        mNoCameraTipLy.setVisibility(View.GONE);
        mGetCameraFailTipLy.setVisibility(View.GONE);
        mListView.setMode(Mode.BOTH);
        mListView.setRefreshing();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case SHOW_DIALOG_DEL_DEVICE:
                break;
        }
        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.update_exit).setIcon(R.drawable.exit_selector);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (dialog != null) {
            removeDialog(id);
            TextView tv = (TextView) dialog.findViewById(android.R.id.message);
            tv.setGravity(Gravity.CENTER);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {// 得到被点击的item的itemId
            case 1:// 对应的ID就是在add方法中所设定的Id
                popLogoutDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void popLogoutDialog() {
        Builder exitDialog = new Builder(EZCameraListActivity.this);
        exitDialog.setTitle(R.string.exit);
        exitDialog.setMessage(R.string.exit_tip);
        exitDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getString(R.string.tip_login_out), Toast.LENGTH_LONG).show();
                EZOpenSDK.getInstance().logout();
                finish();
            }
        });
        exitDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // doNothing
            }
        });
        exitDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CODE){
            if (requestCode == REQUEST_CODE){
                String deviceSerial = intent.getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
                int cameraNo = intent.getIntExtra(IntentConsts.EXTRA_CAMERA_NO,-1);
                int videoLevel = intent.getIntExtra("video_level",-1);
                if (TextUtils.isEmpty(deviceSerial)){
                    return;
                }
                if (videoLevel == -1 || cameraNo == -1){
                    return;
                }
                if (mAdapter.getDeviceInfoList() != null){
                    for (EZDeviceInfo deviceInfo:mAdapter.getDeviceInfoList()){
                        if (deviceInfo.getDeviceSerial().equals(deviceSerial)){
                            if (deviceInfo.getCameraInfoList() != null){
                                for (EZCameraInfo cameraInfo:deviceInfo.getCameraInfoList()){
                                    if (cameraInfo.getCameraNo() == cameraNo){
                                        cameraInfo.setVideoLevel(videoLevel);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        checkExit();
    }

    private long mLastPressTimeMs = 0;
    private void checkExit(){
        boolean isExist = false;
        if (mLastPressTimeMs > 0){
            if (System.currentTimeMillis() - mLastPressTimeMs < 2 * 1000){
                isExist = true;
            }
        }
        if (isExist){
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.app_closed), Toast.LENGTH_LONG).show();
            exitApp();
        }else{
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            mLastPressTimeMs = System.currentTimeMillis();
        }
    }
}
