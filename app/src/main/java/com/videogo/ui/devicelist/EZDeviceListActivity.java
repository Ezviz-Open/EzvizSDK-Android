/**
 * 设备列表页面
 */
package com.videogo.ui.devicelist;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.videogo.ui.common.EZBusinessTool;
import com.videogo.ui.login.MainActivity;
import com.videogo.ui.others.EZTestActivity;
import com.google.gson.Gson;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.ui.setting.EZDeviceSettingActivity;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.ui.playback.EZPlayBackListActivity;
import com.videogo.ui.playback.RemoteListContant;
import com.videogo.ui.adddevice.CaptureActivity;
import com.videogo.ui.message.EZMessageActivity2;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.ui.videotalk.EZJoinMeetingActivity;
import com.videogo.util.ActivityUtils;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.EZUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshFooter.Style;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.pulltorefresh.IPullToRefresh.Mode;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.LoadingLayoutCreator;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.Orientation;
import com.videogo.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;

import com.videogo.ui.login.SdkInitParams;
import com.videogo.util.SpTool;
import com.videogo.global.ValueKeys;
import com.videogo.ui.realplay.multiScreenPreview.MultiScreenPreviewActivity;

import ezviz.ezopensdkcommon.common.RootActivity;

import static com.videogo.EzvizApplication.getOpenSDK;

public class EZDeviceListActivity extends RootActivity implements OnClickListener,
        EZDeviceListAdapter.OnItemClickListener, SelectCameraDialog.CameraItemClick {

    // region --- 全局变量|global variable ---

    protected static final String TAG = EZDeviceListActivity.class.getSimpleName();

    public final static int REQUEST_CODE = 100;
    public final static int RESULT_CODE = 101;

    // 设备配网添加后广播监听，刷新设备列表
    private BroadcastReceiver mReceiver = null;

    // 设备列表
    private PullToRefreshListView mListView = null;
    private View mNoMoreView;
    private EZDeviceListAdapter mAdapter = null;

    // 以下为设备加载失败相关视图
    private LinearLayout mNoDeviceTipLy = null;
    private LinearLayout mGetDeviceFailTipLy = null;
    private TextView mDeviceFailTipTv = null;

    private TextView mMyDevice;// 我的设备Tab
    private TextView mShareDevice;// 分享设备Tab

    // 设置页面返回的话需要刷新页面
    private boolean bIsFromSetting = false;

    // 以下为事件点击类型
    public final static int TAG_CLICK_REAL_PLAY = 1;// 预览
    public final static int TAG_CLICK_PLAY_BACK = 2;// 回放
    public final static int TAG_CLICK_SET_DEVICE = 3;// 设置
    public final static int TAG_CLICK_ALARM_LIST = 4;// 消息
    public final static int TAG_CLICK_VIDEO_TALK = 5;// 视频通话
    private int mClickType;

    // 是否是分享设备的Tab选中
    private boolean isSharedDevice = false;
    // 登录页面指定的设备序列号
    private String mSingleDeviceSerial = "";

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initData();
        initView();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void initData() {
        // 只展示单个设备
        String sdkInitParamStr = SpTool.obtainValue(ValueKeys.SDK_INIT_PARAMS);
        if (sdkInitParamStr != null) {
            SdkInitParams mInitParams = new Gson().fromJson(sdkInitParamStr, SdkInitParams.class);
            mSingleDeviceSerial = mInitParams.specifiedDevice;
        }
        if (!TextUtils.isEmpty(mSingleDeviceSerial)) {
            Log.e(TAG, "only show the device which serial is " + mSingleDeviceSerial);
        }
        // 设备添加成功后广播监听，刷新设备列表|After the device is successfully added, broadcast listening and refresh the device list
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                LogUtil.d(TAG, "onReceive:" + action);
                if (action.equals(Constant.ADD_DEVICE_SUCCESS_ACTION)) {
                    onClickRefreshDeviceList();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ADD_DEVICE_SUCCESS_ACTION);
        // 添加版本兼容性判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// Android 8.0及以上
            registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mReceiver, filter);
        }
    }

    private void initView() {
        mMyDevice = findViewById(R.id.text_my);
        mShareDevice = findViewById(R.id.text_share);
        mNoMoreView = getLayoutInflater().inflate(R.layout.no_device_more_footer, null);
        mAdapter = new EZDeviceListAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mListView = findViewById(R.id.device_listview);
        mListView.setLoadingLayoutCreator(new LoadingLayoutCreator() {

            @Override
            public LoadingLayout create(Context context, boolean headerOrFooter, Orientation orientation) {
                if (headerOrFooter) return new PullToRefreshHeader(context);
                else return new PullToRefreshFooter(context, Style.EMPTY_NO_MORE);
            }
        });
        mListView.setMode(Mode.BOTH);
        mListView.setOnRefreshListener((refreshView, headerOrFooter) -> getDeviceInfoList(headerOrFooter));
        mListView.getRefreshableView().addFooterView(mNoMoreView);
        mListView.setAdapter(mAdapter);
        mListView.getRefreshableView().removeFooterView(mNoMoreView);

        mNoDeviceTipLy = findViewById(R.id.no_device_tip_ly);
        mGetDeviceFailTipLy = findViewById(R.id.get_device_fail_tip_ly);
        mDeviceFailTipTv = findViewById(R.id.get_device_list_fail_tv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bIsFromSetting || (mAdapter != null && mAdapter.getCount() == 0)) {
            onClickRefreshDeviceList();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    // endregion

    // region --- NVR设备多通道选择回调|NVR device multi-channel selection callback ---

    @Override
    public void onCameraItemClick(EZDeviceInfo deviceInfo, int camera_index) {
        switch (mClickType) {
            case TAG_CLICK_REAL_PLAY:
                jumpToRealPlayActivity(deviceInfo, camera_index);
                break;
            case TAG_CLICK_PLAY_BACK:
                jumpToPlaybackActivity(deviceInfo, camera_index);
                break;
            default:
                break;
        }
    }

    // endregion

    // region --- 预览、回放、消息、设置、视频通话回调|preview, replay, message, settings, video call callback ---

    @Override
    public void onPlayClick(BaseAdapter adapter, View view, int position) {
        mClickType = TAG_CLICK_REAL_PLAY;
        final EZDeviceInfo deviceInfo = mAdapter.getItem(position);
        // HUB设备，手动输入信息进行预览，普通开发者无视此分支
        if (EZBusinessTool.isHubDevice(deviceInfo.getDeviceType())) {
            jumpToHubDebugActivity();
        }
        // 单通道设备，直接跳转预览页面
        else if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
            LogUtil.d(TAG, "cameralist have one camera");
            jumpToRealPlayActivity(deviceInfo, 0);
        }
        // 多通道设备，跳转通道列表进行选择
        else if (deviceInfo.getCameraNum() > 1 && deviceInfo.getCameraInfoList() != null) {
            if (deviceInfo.isSupportMultiChannel()) {// 双目设备(双镜头或多镜头设备)
                jumpToRealPlayActivity(deviceInfo, 0);
            } else {// NVR设备，要选通道
                SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
                selectCameraDialog.setEZDeviceInfo(deviceInfo);
                selectCameraDialog.setCameraItemClick(EZDeviceListActivity.this);
                selectCameraDialog.show(getFragmentManager(), "onPlayClick");
            }
        }
        // 子设备(挂载下网关下)
        else if (deviceInfo.getSubDeviceInfoList() != null && deviceInfo.getSubDeviceInfoList().size() > 0) {
            SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
            selectCameraDialog.setEZDeviceInfo(deviceInfo);
            selectCameraDialog.setCameraItemClick(EZDeviceListActivity.this);
            selectCameraDialog.show(getFragmentManager(), "onPlayClick");
        }
    }

    @Override
    public void onPlayBackClick(BaseAdapter adapter, View view, int position) {
        mClickType = TAG_CLICK_PLAY_BACK;
        EZDeviceInfo deviceInfo = mAdapter.getItem(position);
        if (EZBusinessTool.isHubDevice(deviceInfo.getDeviceType())) {
            jumpToHubDebugActivity();
        }
        // 单通道设备
        else if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
            LogUtil.d(TAG, "cameralist have one camera");
            jumpToPlaybackActivity(deviceInfo, 0);
        }
        // 多通道设备
        else if (deviceInfo.getCameraNum() > 1 && deviceInfo.getCameraInfoList() != null) {
            if (deviceInfo.isSupportMultiChannel()) {// 双目设备
                jumpToPlaybackActivity(deviceInfo, 0);
            } else {
                SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
                selectCameraDialog.setEZDeviceInfo(deviceInfo);
                selectCameraDialog.setCameraItemClick(EZDeviceListActivity.this);
                selectCameraDialog.show(getFragmentManager(), "RemotePlayBackClick");
            }
        }
        // 子设备(挂载下网关下)
        else if (deviceInfo.getSubDeviceInfoList() != null && deviceInfo.getSubDeviceInfoList().size() > 0) {
            SelectCameraDialog selectCameraDialog = new SelectCameraDialog();
            selectCameraDialog.setEZDeviceInfo(deviceInfo);
            selectCameraDialog.setCameraItemClick(EZDeviceListActivity.this);
            selectCameraDialog.show(getFragmentManager(), "RemotePlayBackClick");
        }
    }

    @Override
    public void onAlarmListClick(BaseAdapter adapter, View view, int position) {
        mClickType = TAG_CLICK_ALARM_LIST;
        final EZDeviceInfo deviceInfo = mAdapter.getItem(position);
        LogUtil.d(TAG, "cameralist is null or cameralist size is 0");
        Intent intent = new Intent(EZDeviceListActivity.this, EZMessageActivity2.class);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, deviceInfo.getDeviceSerial());
        startActivity(intent);
    }

    @Override
    public void onSetDeviceClick(BaseAdapter adapter, View view, int position) {
        mClickType = TAG_CLICK_SET_DEVICE;
        EZDeviceInfo deviceInfo = mAdapter.getItem(position);
        Intent intent = new Intent(EZDeviceListActivity.this, EZDeviceSettingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
        intent.putExtra("Bundle", bundle);
        startActivity(intent);
        bIsFromSetting = true;
    }

    @Override
    public void onVideoTalkClick(BaseAdapter adapter, View view, int position) {
        mClickType = TAG_CLICK_VIDEO_TALK;
        EZDeviceInfo deviceInfo = mAdapter.getItem(position);
        final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
        if (cameraInfo == null) {
            return;
        }
        Intent intent = new Intent(EZDeviceListActivity.this, EZJoinMeetingActivity.class);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
        startActivity(intent);
    }

    /**
     * 跳转预览页面|jump to realplay page
     */
    private void jumpToRealPlayActivity(EZDeviceInfo deviceInfo, int cameraIndex) {
        final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, cameraIndex);
        if (cameraInfo == null) {
            return;
        }
        Intent intent = new Intent(EZDeviceListActivity.this, EZRealPlayActivity.class);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 跳转回放页面|jump to playback page
     */
    private void jumpToPlaybackActivity(EZDeviceInfo deviceInfo, int cameraIndex) {
        EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, cameraIndex);
        if (cameraInfo == null) {
            return;
        }
        Intent intent = new Intent(EZDeviceListActivity.this, EZPlayBackListActivity.class);
        intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
        startActivity(intent);
    }

    /**
     * 如果是HUB设备，则需要手动输入相应HUB设备和子设备序列号组合后的序列号才能进行取流操作
     */
    private void jumpToHubDebugActivity() {
        startActivity(new Intent(mContext, EZHubDebugActivity.class));
    }

    // endregion

    // region - 预览页面清晰度设置后回调，更新对象的清晰度数据|After setting the clarity of the preview page, call back and update the clarity data of the object -

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CODE) {
            if (requestCode == REQUEST_CODE) {
                // 更新设备列表 该设备的清晰度
                String deviceSerial = intent.getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
                int cameraNo = intent.getIntExtra(IntentConsts.EXTRA_CAMERA_NO, -1);
                int videoLevel = intent.getIntExtra("video_level", -1);
                if (TextUtils.isEmpty(deviceSerial)) {
                    return;
                }
                if (videoLevel == -1 || cameraNo == -1) {
                    return;
                }
                if (mAdapter.getDeviceInfoList() != null) {
                    for (EZDeviceInfo deviceInfo : mAdapter.getDeviceInfoList()) {
                        if (deviceInfo.getDeviceSerial().equals(deviceSerial)) {
                            if (deviceInfo.getCameraInfoList() != null) {
                                for (EZCameraInfo cameraInfo : deviceInfo.getCameraInfoList()) {
                                    if (cameraInfo.getCameraNo() == cameraNo) {
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

    // endregion

    // region --- Click点击事件|Click event ---

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.device_list_refresh_btn || id == R.id.no_device_tip_ly) {
            onClickRefreshDeviceList();
        } else if (id == R.id.text_share) { // "分享"按钮
            mShareDevice.setTextColor(getResources().getColor(R.color.orange_text));
            mMyDevice.setTextColor(getResources().getColor(R.color.black_text));
            mAdapter.clearAll();
            isSharedDevice = true;
            getDeviceInfoList(true);
        } else if (id == R.id.text_my) { // "我的"按钮
            mShareDevice.setTextColor(getResources().getColor(R.color.black_text));
            mMyDevice.setTextColor(getResources().getColor(R.color.orange_text));
            mAdapter.clearAll();
            isSharedDevice = false;
            getDeviceInfoList(true);
        }
    }

    /**
     * 添加设备|add device
     */
    public void onClickAddDevice(View view) {
        Intent intent = new Intent(EZDeviceListActivity.this, CaptureActivity.class);
        startActivity(intent);
    }

    /**
     * 多画面预览|multi screen preview
     */
    public void onClickMultiScreenPreview(View view) {
        MultiScreenPreviewActivity.Companion.launch(view.getContext());
    }

    /**
     * 测试页面|test page
     */
    public void onClickTest(View view) {
        startActivity(new Intent(this, EZTestActivity.class));
    }

    /**
     * 退出账号|exit account
     */
    public void onClickLogout(View view) {
        // 自动化测试的时候不响应。
        if (SpTool.obtainBooleanValue(ValueKeys.AUTO_TEST)) {
            return;
        }
        popLogoutDialog();
    }

    // endregion

    // region --- 获取设备列表任务|Obtain device list task ---

    private void getDeviceInfoList(boolean headerOrFooter) {
        if (this.isFinishing()) {
            return;
        }
        new GetDeviceInfoListTask(headerOrFooter).execute();
    }

    private class GetDeviceInfoListTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {
        private boolean mHeaderOrFooter;// true为header，下拉刷新
        private int mErrorCode = 0;

        public GetDeviceInfoListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mListView.setFooterRefreshEnabled(true);
            if (mHeaderOrFooter) {
                mListView.setVisibility(View.VISIBLE);
                mNoDeviceTipLy.setVisibility(View.GONE);
                mGetDeviceFailTipLy.setVisibility(View.GONE);
            }
            mListView.getRefreshableView().removeFooterView(mNoMoreView);
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... params) {
            if (EZDeviceListActivity.this.isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(EZDeviceListActivity.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }
            if (getOpenSDK() == null) {
                finish();
            }
            try {
                List<EZDeviceInfo> result = null;
                if (!TextUtils.isEmpty(mSingleDeviceSerial)) {
                    // 如果设备是网关-门铃类子设备，请将getDeviceInfo替换为getDeviceInfoEx方法
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
                if (isSharedDevice) {
                    if (mHeaderOrFooter) {
                        result = getOpenSDK().getSharedDeviceList(0, 20);
                    } else {
                        result = getOpenSDK().getSharedDeviceList((mAdapter.getCount() / 20) + (mAdapter.getCount() % 20 > 0 ? 1 : 0), 20);
                    }
                } else {
                    // 如果设备是网关-门铃类子设备，请将getDeviceList替换为getDeviceListEx方法
                    if (mHeaderOrFooter) {
                        result = getOpenSDK().getDeviceList(0, 20);
                    } else {
                        result = getOpenSDK().getDeviceList((mAdapter.getCount() / 20) + (mAdapter.getCount() % 20 > 0 ? 1 : 0), 20);
                    }
                }
                return result;
            } catch (BaseException e) {
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d(TAG, errorInfo.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> result) {
            super.onPostExecute(result);
            mListView.onRefreshComplete();
            if (EZDeviceListActivity.this.isFinishing()) {
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
                    mNoDeviceTipLy.setVisibility(View.VISIBLE);
                    mGetDeviceFailTipLy.setVisibility(View.GONE);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                } else if (result.size() < 20) {
                    mListView.setFooterRefreshEnabled(false);
                    mListView.getRefreshableView().addFooterView(mNoMoreView);
                } else if (mHeaderOrFooter) {
                    mListView.setFooterRefreshEnabled(true);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }
                addDeviceList(result);
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
                    ActivityUtils.handleSessionException(EZDeviceListActivity.this);
                    break;
                default:
                    if (mAdapter.getCount() == 0) {
                        mListView.setVisibility(View.GONE);
                        mNoDeviceTipLy.setVisibility(View.GONE);
                        mDeviceFailTipTv.setText(Utils.getErrorTip(EZDeviceListActivity.this,
                                R.string.get_device_list_fail, errorCode));
                        mGetDeviceFailTipLy.setVisibility(View.VISIBLE);
                    } else {
                        Utils.showToast(EZDeviceListActivity.this, R.string.get_device_list_fail, errorCode);
                    }
                    break;
            }
        }
    }

    // endregion

    // region --- 其他方法|Other methods ---
    private void addDeviceList(List<EZDeviceInfo> result) {
        int count = result.size();
        EZDeviceInfo item = null;
        for (int i = 0; i < count; i++) {
            item = result.get(i);
            mAdapter.addItem(item);
        }
    }

    private void onClickRefreshDeviceList() {
        mListView.setVisibility(View.VISIBLE);
        mNoDeviceTipLy.setVisibility(View.GONE);
        mGetDeviceFailTipLy.setVisibility(View.GONE);
        mListView.setMode(Mode.BOTH);
        mListView.setRefreshing();
    }

    // endregion

    // region --- 退出登录Pop|Logout Pop ---

    private void popLogoutDialog() {
        Builder exitDialog = new Builder(EZDeviceListActivity.this);
        exitDialog.setTitle(R.string.exit);
        exitDialog.setMessage(R.string.exit_tip);
        exitDialog.setPositiveButton(R.string.confirm, (dialog, which) -> {
            new Thread(() -> {
                if (getOpenSDK() == null) {
                    finish();
                    return;
                }
                getOpenSDK().logout();
                String sdkInitParamStr = SpTool.obtainValue(ValueKeys.SDK_INIT_PARAMS);
                if (sdkInitParamStr != null) {
                    SdkInitParams mInitParams = new Gson().fromJson(sdkInitParamStr, SdkInitParams.class);
                    mInitParams.token = "";
                    // 保存相关信息
                    SpTool.storeValue(ValueKeys.SDK_INIT_PARAMS, mInitParams.toString());
                }
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }).start();
        });
        exitDialog.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // doNothing
        });
        exitDialog.show();
    }

    // endregion

    // region --- 连续点击返回，退出Demo App|Click back continuously to exit the Demo App ---

    @Override
    public void onBackPressed() {
        checkExit();
    }

    private long mLastPressTimeMs = 0;

    private void checkExit() {
        boolean isExist = false;
        if (mLastPressTimeMs > 0) {
            if (System.currentTimeMillis() - mLastPressTimeMs < 2 * 1000) {
                isExist = true;
            }
        }
        if (isExist) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.app_closed),
                    Toast.LENGTH_LONG).show();
            exitApp();
        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.press_again_to_exit),
                    Toast.LENGTH_SHORT).show();
            mLastPressTimeMs = System.currentTimeMillis();
        }
    }

    // endregion

}
