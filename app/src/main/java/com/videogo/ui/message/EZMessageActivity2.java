package com.videogo.ui.message;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.videogo.EzvizApplication;
import ezviz.ezopensdkcommon.common.RootActivity;
import com.videogo.alarm.AlarmLogInfoManager;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZConstants.EZAlarmStatus;
import com.videogo.openapi.bean.EZAlarmInfo;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.ui.util.VerifyCodeInput;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LogUtil;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.PinnedSectionListView;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshFooter.Style;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;
import com.videogo.widget.pulltorefresh.IPullToRefresh.Mode;
import com.videogo.widget.pulltorefresh.IPullToRefresh.OnRefreshListener;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.LoadingLayoutCreator;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.Orientation;
import com.videogo.widget.pulltorefresh.PullToRefreshPinnedSectionListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;

public class EZMessageActivity2 extends RootActivity implements VerifyCodeInput.VerifyCodeErrorListener{
    private static final String TAG = "EMessageActivity2";

    private static final int REQUEST_CODE_SINGLE_DETAIL = 1;
    private static final int REQUEST_CODE_MULTI_DETAIL = 2;

    public static final int ERROR_WEB_NO_ERROR = 100000; // /< No error
    public static final int ERROR_WEB_NO_DATA = 100000 - 2; // /< The data is empty or does not exist

    private TitleBar mTitleBar;
    private PullToRefreshPinnedSectionListView mMessageListView;
    private ViewGroup mMainLayout;
    private ViewGroup mNoMessageLayout;
    private TextView mNoMessageTextView;
    private Button mNoMessageButton;
    private ViewGroup mRefreshLayout;
    private Button mRefreshButton;
    private TextView mRefreshTipView;
    private View mNoMoreView;
    private CheckTextButton mCheckModeButton;

    private ViewGroup mCheckModeTopLayout;
    private View mCheckModeTopDivider;
    private CheckBox mCheckAllView;
    private ViewGroup mCheckModeBottomLayout;
    private View mCheckModeBottomDivider;
    private Button mDeleteButton;
    private Button mReadButton;

    private EZMessageListAdapter2 mAdapter;
    private AlarmLogInfoManager mAlarmLogInfoManager;
    /** 消息处理 Message processing*/
//    private MessageCtrl mMessageCtrl;
    private List<EZAlarmInfo> mMessageList;

    private long mLastLoadTime;
    private UIHandler mUIHandler;

    private int mDataType = Constant.MESSAGE_LIST;
    private String mDeviceSerial;
    private int mCameraNo;
    private int mMenuPosition;
    private BroadcastReceiver mReceiver;
    private boolean mCheckMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_message_page);

        findViews();
        initData();
        initTitleBar();
        initViews();
        setListner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ALARM_MESSAGE_DISPLAY_ACTION);
        registerReceiver(mReceiver, intentFilter);

        if (mAdapter.getCount() > 0) {
            setRefreshLayoutVisibility(false);
            setNoMessageLayoutVisibility(false);
        }

        if (mDataType == Constant.MESSAGE_INNER_PUSH)
            getPushAlarmMessageList();

        if (mDataType == Constant.MESSAGE_LIST
                && System.currentTimeMillis() - mLastLoadTime >= Constant.RELOAD_INTERVAL) {
            refreshButtonClicked();
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void findViews() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mMessageListView = (PullToRefreshPinnedSectionListView) findViewById(R.id.message_list);
        mNoMessageLayout = (ViewGroup) findViewById(R.id.no_message_layout);
        mNoMessageTextView = (TextView) findViewById(R.id.no_message_text);
        mNoMessageButton = (Button) findViewById(R.id.no_message_button);
        mRefreshLayout = (ViewGroup) findViewById(R.id.refresh_layout);
        mRefreshButton = (Button) mRefreshLayout.findViewById(R.id.retry_button);
        mRefreshTipView = (TextView) mRefreshLayout.findViewById(R.id.error_prompt);
        mMainLayout = (ViewGroup) findViewById(R.id.main_layout);

        mCheckModeTopLayout = (ViewGroup) findViewById(R.id.check_mode_top);
        mCheckModeTopDivider = findViewById(R.id.check_mode_top_divider);
        mCheckAllView = (CheckBox) findViewById(R.id.check_all);
        mCheckModeBottomLayout = (ViewGroup) findViewById(R.id.check_mode_bottom);
        mCheckModeBottomDivider = findViewById(R.id.check_mode_bottom_divider);
        mDeleteButton = (Button) findViewById(R.id.del_button);
        mReadButton = (Button) findViewById(R.id.read_button);

    }

    private void initData() {
        mAlarmLogInfoManager = AlarmLogInfoManager.getInstance();
//        mMessageCtrl = MessageCtrl.getInstance();
        mMessageList = new ArrayList<EZAlarmInfo>();
        mDeviceSerial = getIntent().getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
        mCameraNo = getIntent().getIntExtra(IntentConsts.EXTRA_CAMERA_NO,1);

        mAdapter = new EZMessageListAdapter2(this, mMessageList, mDeviceSerial,this);
        mAdapter.setNoMenu(mDataType != Constant.MESSAGE_LIST);
        mUIHandler = new UIHandler();
    }

    private void initTitleBar() {
        if (mDataType == Constant.MESSAGE_LIST) {
            mTitleBar.setTitle(R.string.ez_event_message);
            mCheckModeButton = mTitleBar.addRightCheckedText(getText(R.string.edit_txt), getText(R.string.cancel),
                    new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                            }
                            setCheckMode(isChecked);
                        }
                    });
        }
        mTitleBar.addBackButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initViews() {
        if (mDataType == Constant.MESSAGE_LIST) {
            mNoMoreView = getLayoutInflater().inflate(R.layout.no_more_footer, null);
            ((TextView) mNoMoreView.findViewById(R.id.no_more_hint)).setText(R.string.no_more_alarm_tip);

            mMessageListView.setLoadingLayoutCreator(new LoadingLayoutCreator() {

                @Override
                public LoadingLayout create(Context context, boolean headerOrFooter, Orientation orientation) {
                    if (headerOrFooter)
                        return new PullToRefreshHeader(context);
                    else
                        return new PullToRefreshFooter(context, Style.EMPTY_NO_MORE);
                }
            });
            mMessageListView.setMode(Mode.BOTH);
            mMessageListView.setOnRefreshListener(new OnRefreshListener<PinnedSectionListView>() {

                @Override
                public void onRefresh(PullToRefreshBase<PinnedSectionListView> refreshView, boolean headerOrFooter) {
                    getAlarmMessageList(true, headerOrFooter);
                }
            });

            mMessageListView.getRefreshableView().addFooterView(mNoMoreView);
            mMessageListView.setAdapter(mAdapter);
            mMessageListView.getRefreshableView().removeFooterView(mNoMoreView);
            
            mCheckModeButton.setVisibility(View.GONE);
        }
//        boolean hasDevice = DeviceManager.getInstance().haveAlarmDevice();
//        mNoMessageTextView.setVisibility(hasDevice ? View.GONE : View.VISIBLE);
//        mNoMessageButton.setVisibility(hasDevice ? View.GONE : View.VISIBLE);
    }

    private void setListner() {
        OnClickListener clickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.retry_button:
                        refreshButtonClicked();
                        break;

                    case R.id.no_message_layout:
                        refreshButtonClicked();
                        break;

                    case R.id.check_mode_top:
                        mCheckAllView.toggle();
                    case R.id.check_all:
                        if (mCheckAllView.isChecked()) {
                        }
                        if (mCheckAllView.isChecked())
                            mAdapter.checkAll();
                        else
                            mAdapter.uncheckAll();
                        setupCheckModeLayout(false);
                        break;

                    case R.id.del_button:
                        deleteMessage(mAdapter.getCheckedIds());
                        break;

                    case R.id.read_button:
                        new CheckAlarmInfoTask2(true).execute(mAdapter.getCheckedIds());
                        break;

                    case R.id.no_message_button:
//                        WebUtils.openYsStore(EZMessageActivity2.this, null);
                        break;
                }
            }
        };

        mRefreshButton.setOnClickListener(clickListener);
        mNoMessageLayout.setOnClickListener(clickListener);
        mNoMessageButton.setOnClickListener(clickListener);
        mCheckModeTopLayout.setOnClickListener(clickListener);
        mCheckAllView.setOnClickListener(clickListener);
        mDeleteButton.setOnClickListener(clickListener);
        mReadButton.setOnClickListener(clickListener);

        mAdapter.setOnClickListener(new EZMessageListAdapter2.OnClickListener() {

            @Override
            public void onItemLongClick(BaseAdapter adapter, View view, int position) {
                mMenuPosition = position;
            }

            @Override
            public void onItemClick(BaseAdapter adapter, View view, int position) {
                EZAlarmInfo alarmInfo = (EZAlarmInfo) adapter.getItem(position);
                setAlarmInfoChecked(alarmInfo);

                Intent intent = new Intent(EZMessageActivity2.this, EZMessageImageActivity2.class);
                intent.putExtra(IntentConsts.EXTRA_ALARM_INFO, alarmInfo);
                startActivity(intent);
                //startActivityForResult(intent, REQUEST_CODE_MULTI_DETAIL);
            }

            @Override
            public void onCheckClick(BaseAdapter adapter, View view, int position, boolean checked) {
                setupCheckModeLayout(false);
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mDataType == Constant.MESSAGE_INNER_PUSH)
                    getPushAlarmMessageList();
                mAdapter.notifyDataSetChanged();
            };
        };
    }

    private void deleteMessage(final Object param) {
        Context context = (getParent() == null ? this : getParent());

        new AlertDialog.Builder(context).setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteAlarmMessageTask().execute(param);
                    }
                }).show();
    }

    private void refreshButtonClicked() {
        setRefreshLayoutVisibility(false);
        setNoMessageLayoutVisibility(false);
        getAlarmMessageList(false, true);
    }

    private void setAlarmInfoChecked(EZAlarmInfo alarmInfo) {
        // 判断是否已读
        if (alarmInfo.getIsRead() == 0)
            new CheckAlarmInfoTask2(false).execute(alarmInfo);
    }

    private void getAlarmMessageList(boolean pullOrClick, boolean headerOrFooter) {
        if (pullOrClick) {
            String lastTime = "";
            if (!headerOrFooter) {
                if (mMessageList != null && mMessageList.size() > 0)
                    lastTime = mMessageList.get(mMessageList.size() - 1).getAlarmStartTime();
            }
            new GetAlarmMessageTask(headerOrFooter).execute(lastTime);
        } else {
            mMessageListView.setRefreshing();
        }
    }

    private void getPushAlarmMessageList() {
        //mj
//        mMessageList.addAll(0, mAlarmLogInfoManager.getPushListFromNotifierByCamera(this, mDeviceSerial, mChannelNo,
//                AlarmLogInfoEx.ALARMTYPE));
        mAdapter.setList(mMessageList);
        mAdapter.notifyDataSetChanged();
    }

    public void setCheckMode(boolean checkMode) {
        if (mCheckMode != checkMode) {
            mCheckMode = checkMode;
//            mCheckModeTopLayout.setVisibility(mCheckMode ? View.VISIBLE : View.GONE);
            mCheckModeTopDivider.setVisibility(mCheckMode ? View.VISIBLE : View.GONE);
            mCheckModeBottomLayout.setVisibility(mCheckMode ? View.VISIBLE : View.GONE);
            mCheckModeBottomDivider.setVisibility(mCheckMode ? View.VISIBLE : View.GONE);

            if (mCheckMode)
                setupCheckModeLayout(true);
            mAdapter.setCheckMode(mCheckMode);
        }
    }

    private void setNoMessageLayoutVisibility(boolean visible) {
        if (mCheckModeButton != null) {
// Edit TextCheck           mCheckModeButton.setVisibility((visible || mMainLayout.getVisibility() != View.VISIBLE) ? View.GONE
//                    : View.VISIBLE);
        }
        mNoMessageLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setRefreshLayoutVisibility(boolean visible) {
        if (mCheckModeButton != null) {
//            mCheckModeButton.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
        mRefreshLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setupCheckModeLayout(boolean init) {
        if (mCheckMode) {
            List<String> ids = new ArrayList<String>();
            boolean checkAll = false;

            if (!init) {
                ids.addAll(mAdapter.getCheckedIds());
                checkAll = mAdapter.isCheckAll();
            }

            mCheckAllView.setChecked(checkAll);

            if (ids.size() == 0) {
                mDeleteButton.setText(R.string.delete);
                mDeleteButton.setEnabled(false);

                mReadButton.setEnabled(true);
            } else {
                mDeleteButton.setText(getString(R.string.delete) + '（' + ids.size() + '）');
                mDeleteButton.setEnabled(true);

                mReadButton.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mAdapter == null || mAdapter.getCount() == 0) {
            return true;
        }
        EZAlarmInfo alarmInfo = (EZAlarmInfo) mAdapter.getItem(mMenuPosition);
        if (alarmInfo == null) {
            return true;
        }
        switch (item.getItemId()) {
            case EZMessageListAdapter2.MENU_DEL_ID:
                deleteMessage(alarmInfo);
                break;

//            case MessageListAdapter.MENU_MORE_ID:
//                mCheckModeButton.setChecked(true);
//                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (getParent() == null)
            super.startActivityForResult(intent, requestCode);
        else
            getParent().startActivityForResult(intent, requestCode);
    }

    private void setAlarmRead(EZAlarmInfo alarmInfo) {
    	if(mMessageList != null && mMessageList.size() > 0) {
    		for(EZAlarmInfo info : mMessageList) {
    			if(info.getAlarmId().equals(alarmInfo.getAlarmId())) {
    				info.setIsRead(1);
    			}
    		}
    	}
    }

    private void setAlarmRead(String alarmId) {
    	if(mMessageList != null && mMessageList.size() > 0) {
    		for(EZAlarmInfo info : mMessageList) {
    			if(info.getAlarmId().equals(alarmId)) {
    				info.setIsRead(1);
    			}
    		}
    	}
    }
    private void deleteAlarmFromList(String alarmId) {
    	if(mMessageList != null && mMessageList.size() > 0) {
    		for(int i = 0 ; i < mMessageList.size(); i ++) {
    			EZAlarmInfo info = mMessageList.get(i);
    			if(info.getAlarmId().equals(alarmId)) {
    				mMessageList.remove(info);
    			}
    		}
    	}
    }

    @Override
    public void verifyCodeError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               if (mAdapter != null){
                   mAdapter.setVerifyCodeDialog();
               }
            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        if (requestCode == REQUEST_CODE_SINGLE_DETAIL) {
//            finish();
//
//        } else if (requestCode == REQUEST_CODE_MULTI_DETAIL) {
//            if (resultCode == RESULT_OK) {
//                if (intent.hasExtra(IntentConstants.EXTRA_ALARM_LIST))
//                    mMessageList = intent.getParcelableArrayListExtra(IntentConstants.EXTRA_ALARM_LIST);
//                mAdapter.setList(mMessageList);
//                mAdapter.notifyDataSetChanged();
//            }
//
//            if (intent != null) {
//                long lastLoadTime = intent.getLongExtra(IntentConstants.EXTRA_LAST_LOAD_TIME, 0);
//                if (lastLoadTime > 0)
//                    mLastLoadTime = lastLoadTime;
//            }
//        }
//    }

    private class GetAlarmMessageTask extends AsyncTask<String, Void, List<EZAlarmInfo>> {
        private boolean mHeaderOrFooter;
        private int mErrorCode = 100000;// ErrorCode.ERROR_WEB_NO_ERROR;

        public GetAlarmMessageTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected List<EZAlarmInfo> doInBackground(String... params) {
            if (mHeaderOrFooter) {
            } else {
            }

            if (!ConnectionDetector.isNetworkAvailable(EZMessageActivity2.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }

//            try {
            List<EZAlarmInfo> result = null;
            Calendar mStartTime;
            Calendar mEndTime;
            mStartTime = Calendar.getInstance();
            mStartTime.set(Calendar.AM_PM, 0);
            mStartTime.set(mStartTime.get(Calendar.YEAR), mStartTime.get(Calendar.MONTH), 
                    mStartTime.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            mEndTime = Calendar.getInstance();
            mEndTime.set(Calendar.AM_PM, 0);
            mEndTime.set(mEndTime.get(Calendar.YEAR), mEndTime.get(Calendar.MONTH), 
                    mEndTime.get(Calendar.DAY_OF_MONTH), 23, 59, 59);

            int pageSize = 10;
            int pageStart = 0;
            if(mHeaderOrFooter) {
            	pageStart = 0;
            } else {
            	pageStart = mAdapter.getCount() / pageSize;
            }

            try {
                result = EzvizApplication.getOpenSDK().getAlarmList(mDeviceSerial, pageStart, pageSize,mStartTime,
                    mEndTime);
            }
            catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d("EM", errorInfo.toString());
            }
            

            return result;

//            } catch (VideoGoNetSDKException e) {
//                mErrorCode = e.getErrorCode();
//                if (mErrorCode == ErrorCode.ERROR_WEB_NO_DATA) {
//                    try {
//                        mMessageCtrl.fetchUnreadMsgCount(EZMessageActivity2.this);
//                    } catch (VideoGoNetSDKException ex) {
//                    }
//                    return new ArrayList<AlarmLogInfoEx>();
//                } else {
//                    return null;
//                }
//            }
        }

        @Override
        protected void onPostExecute(List<EZAlarmInfo> result) {
            super.onPostExecute(result);
            mMessageListView.onRefreshComplete();
            int pageSize = 10;

            if (result == null){
                if (mErrorCode != ERROR_WEB_NO_ERROR)
                    onError(mErrorCode);
                return;
            }
            if (mHeaderOrFooter
                    && (mErrorCode == ERROR_WEB_NO_ERROR || mErrorCode == ERROR_WEB_NO_DATA)) {
                CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
                for (LoadingLayout layout : mMessageListView.getLoadingLayoutProxy(true, false).getLayouts()) {
                    ((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
                }
                mMessageListView.setFooterRefreshEnabled(true);
                mMessageListView.getRefreshableView().removeFooterView(mNoMoreView);
//                mAdapter.();
                mMessageList.clear();
            }

            if(mAdapter.getCount() == 0 && result.size() == 0) {
                // show no message ui
            } else if (result.size() < pageSize) {
                mMessageListView.setFooterRefreshEnabled(false);
                mMessageListView.getRefreshableView().addFooterView(mNoMoreView);
            } else if (mHeaderOrFooter) {
                mMessageListView.setFooterRefreshEnabled(true);
                mMessageListView.getRefreshableView().removeFooterView(mNoMoreView);
            }

            if (result != null && result.size() > 0) {
                mMessageList.addAll(result);
                mAdapter.setList(mMessageList);
                setupCheckModeLayout(false);
                mAdapter.notifyDataSetChanged();

                mLastLoadTime = System.currentTimeMillis();
            } else {
                mErrorCode = ERROR_WEB_NO_DATA;
            }
            sendUIMessage(mUIHandler, UI_MSG_LIST_CHANGE, 0, 0);

            if (mMessageList.size() > 0) {
                setNoMessageLayoutVisibility(false);
            }

            if (mErrorCode != ERROR_WEB_NO_ERROR)
                onError(mErrorCode);
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ERROR_WEB_NO_DATA:
                    if (mMessageList.size() == 0) {
                        setRefreshLayoutVisibility(false);
                        setNoMessageLayoutVisibility(true);
                        mMessageListView.getRefreshableView().removeFooterView(mNoMoreView);
                    } else {
                        setRefreshLayoutVisibility(false);
                        mMessageListView.setFooterRefreshEnabled(false);
                        mMessageListView.getRefreshableView().addFooterView(mNoMoreView);
                    }
                    break;

                /*case ErrorCode.ERROR_WEB_SESSION_ERROR:
                    ActivityUtils.handleSessionException(EZMessageActivity2.this);
                    break;

                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    ActivityUtils.handleHardwareError(EZMessageActivity2.this, null);
                    break;

                case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                    showError(getText(R.string.message_refresh_fail_server_exception));
                    break;

                case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                    showError(getText(R.string.message_refresh_fail_network_exception));
                    break;*/

                default:
                    showError(getErrorTip(R.string.get_message_fail_service_exception, errorCode));
                    break;
            }
        }

        private void showError(CharSequence text) {
            if (mHeaderOrFooter && mMessageList.size() == 0) {
                mRefreshTipView.setText(text);
                setRefreshLayoutVisibility(true);
            } else {
                showToast(text);
            }
        }
    }

    private class DeleteAlarmMessageTask extends AsyncTask<Object, Void, Object> {

        private Dialog mWaitDialog;
        private int mErrorCode = ERROR_WEB_NO_ERROR;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZMessageActivity2.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Object doInBackground(Object... params) {
            if (!ConnectionDetector.isNetworkAvailable(EZMessageActivity2.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }

            List<String> deleteIds = new ArrayList<String>();
                if (params[0] instanceof EZAlarmInfo) {
                    // 单个删除 Single delete
                    EZAlarmInfo info = (EZAlarmInfo) params[0];
                    if (TextUtils.isEmpty(info.getAlarmId())) {
                    }
                    else {
                        deleteIds.add(info.getAlarmId());
                    }

                } else if (params[0] instanceof List<?>) {
                    // 批量删除 batch deletion
                    List<String> ids = (List<String>) params[0];
                    if (ids != null && ids.size() > 0)
                        deleteIds.addAll(ids);
                }
                
                try {
                    EzvizApplication.getOpenSDK().deleteAlarm(deleteIds);
                }
                catch (BaseException e) {
                    e.printStackTrace();

                    ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                    mErrorCode = errorInfo.errorCode;
                    LogUtil.d(TAG, errorInfo.toString());
                }

                return params[0];
//            } catch (VideoGoNetSDKException e) {
//                mErrorCode = e.getErrorCode();
//                return null;
//            }
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

            if (result != null) {
                if (result instanceof EZAlarmInfo) {
                	EZAlarmInfo info = (EZAlarmInfo) result;
                    //mAlarmLogInfoManager.deleteAlarmLogList(info);
                    //mj if (mDataType == Constant.MESSAGE_INNER_PUSH)
                	if (mDataType == Constant.MESSAGE_LIST)
                        mMessageList.remove(info);
                } else if (result instanceof List<?>) {
                    List<String> ids = (List<String>) result;
                    for (String id : ids) {
                    	deleteAlarmFromList(id);
                    }
                }

                // 如果删除到最后会重新获取的 If deleted to the last will be re-acquired
                if (mDataType == Constant.MESSAGE_LIST && mMessageList.size() == 0) {
                    setNoMessageLayoutVisibility(true);
                    refreshButtonClicked();
                }

                mAdapter.setList(mMessageList);
                setupCheckModeLayout(false);
                mAdapter.notifyDataSetChanged();
                showToast(getText(R.string.alarm_message_del_success_txt));
                mCheckModeButton.setChecked(false);
            }

            if (mErrorCode != ERROR_WEB_NO_ERROR)
                onError(mErrorCode);
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                    ActivityUtils.handleSessionException(EZMessageActivity2.this);
                    break;

                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    ActivityUtils.handleSessionException(EZMessageActivity2.this);
                    break;

                case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                    showToast(getText(R.string.alarm_message_del_fail_txt));
                    break;

                case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                    showToast(getText(R.string.alarm_message_del_fail_network_exception));
                    break;

                default:
                    showToast(R.string.alarm_message_del_fail_txt, errorCode);
                    break;
            }
        }
    }

    private class CheckAlarmInfoTask extends AsyncTask<EZAlarmInfo, Void, Boolean> {

        private Dialog mWaitDialog;
        private boolean mCheckAll;
        private int mErrorCode = ERROR_WEB_NO_ERROR;
        private EZAlarmInfo info;

        public CheckAlarmInfoTask(boolean checkAll) {
            mCheckAll = checkAll;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCheckAll) {
                mWaitDialog = new WaitDialog(EZMessageActivity2.this, android.R.style.Theme_Translucent_NoTitleBar);
                mWaitDialog.setCancelable(false);
                mWaitDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(EZAlarmInfo... params) {
            if (!ConnectionDetector.isNetworkAvailable(EZMessageActivity2.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return false;
            }
            
            info = params[0];

            List<String> alarmIds = new ArrayList<String>();
            if(info != null)
                alarmIds.add(info.getAlarmId());

            try {
                EzvizApplication.getOpenSDK().setAlarmStatus(alarmIds, EZAlarmStatus.EZAlarmStatusRead);
                setAlarmRead(info);
                return true;
            }
            catch (BaseException e) {
                e.printStackTrace();

                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d("EM", errorInfo.toString());

                return false;
            }
//            try {
//                if (mCheckAll) {
//                    mMessageCtrl.setAllAlarmInfoChecked();
//                } else {
//                    // 单独已读
//                    info = params[0];
//                    if (TextUtils.isEmpty(info.getAlarmLogId()))
//                        mMessageCtrl.setAlarmInfoChecked(info.getDeviceSerial(), info.getChannelNo(),
//                                info.getAlarmType(), info.getAlarmStartTime());
//                    else
//                        mMessageCtrl.setAlarmInfoChecked(info.getAlarmLogId());
//                    info.setCheckState(1);
//                }
//                return true;

//            } catch (VideoGoNetSDKException e) {
//                mErrorCode = e.getErrorCode();
//                return false;
//            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mCheckAll)
                mWaitDialog.dismiss();

            if (result) {
                if (mCheckAll) {
//                    // 本地的全部标识已读
//                    mAlarmLogInfoManager.checkAllAlarmLogInfoList();
//
//                    // 清除所有应用外推送 ,提示栏已经显示了，所以清除掉
//                    Utils.clearAllNotification(EZMessageActivity2.this);
//                    mAlarmLogInfoManager.clearNotifierMessageList(AlarmLogInfoEx.ALARMTYPE);
//
//                    // 遍历本地摄像机标识本地的未读信息
//                    CameraManager.getInstance().clearAlarmCount();
//                    mMessageCtrl.checkAllUnReadCameraMessage(EZMessageActivity2.this);
//                    showToast(getText(R.string.alarm_message_check_success));
//                    mCheckModeButton.setChecked(false);
                } else {
//                    mAlarmLogInfoManager.getAlarmLogInfoFromNotifier().remove(info);
//                    mMessageCtrl.decreaseUnreadCameraMessageCount(EZMessageActivity2.this);
                }

                mAdapter.notifyDataSetChanged();
            }

            if (mErrorCode != ERROR_WEB_NO_ERROR)
                onError(mErrorCode);
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                    ActivityUtils.handleSessionException(EZMessageActivity2.this);
                    break;

                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    ActivityUtils.handleSessionException(EZMessageActivity2.this);
                    break;

                case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                    showToast(getText(R.string.alarm_message_check_fail));
                    break;

                case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                    showToast(getText(R.string.alarm_message_check_fail_network_exception));
                    break;

                default:
                    showToast(R.string.alarm_message_check_fail, errorCode);
                    break;
            }
        }
    }
    
    private class CheckAlarmInfoTask2 extends AsyncTask<Object, Void, Boolean> {

        private Dialog mWaitDialog;
        private boolean mCheckAll;
        private int mErrorCode = ERROR_WEB_NO_ERROR;
        private EZAlarmInfo info;

        public CheckAlarmInfoTask2(boolean checkAll) {
            mCheckAll = checkAll;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCheckAll) {
                mWaitDialog = new WaitDialog(EZMessageActivity2.this, android.R.style.Theme_Translucent_NoTitleBar);
                mWaitDialog.setCancelable(false);
                mWaitDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            if (!ConnectionDetector.isNetworkAvailable(EZMessageActivity2.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return false;
            }
            
            List<String> alarmIds = new ArrayList<String>();
            if (params[0] instanceof EZAlarmInfo) {
                EZAlarmInfo info = (EZAlarmInfo) params[0];
                if (TextUtils.isEmpty(info.getAlarmId())) {
                }
                else {
                	alarmIds.add(info.getAlarmId());
                }

            } else if (params[0] instanceof List<?>) {
                List<String> ids = (List<String>) params[0];
                if (ids != null && ids.size() > 0)
                	alarmIds.addAll(ids);
            }
            
            try {
                EzvizApplication.getOpenSDK().setAlarmStatus(alarmIds, EZAlarmStatus.EZAlarmStatusRead);
                for(String alarmId : alarmIds) {
                	setAlarmRead(alarmId);
                }
                return true;
            }
            catch (BaseException e) {
                e.printStackTrace();

                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                LogUtil.d("EM", errorInfo.toString());

                return false;
            }
//            try {
//                if (mCheckAll) {
//                    mMessageCtrl.setAllAlarmInfoChecked();
//                } else {
//                    // 单独已读
//                    info = params[0];
//                    if (TextUtils.isEmpty(info.getAlarmLogId()))
//                        mMessageCtrl.setAlarmInfoChecked(info.getDeviceSerial(), info.getChannelNo(),
//                                info.getAlarmType(), info.getAlarmStartTime());
//                    else
//                        mMessageCtrl.setAlarmInfoChecked(info.getAlarmLogId());
//                    info.setCheckState(1);
//                }
//                return true;

//            } catch (VideoGoNetSDKException e) {
//                mErrorCode = e.getErrorCode();
//                return false;
//            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mCheckAll)
                mWaitDialog.dismiss();

            if (result) {
                setupCheckModeLayout(false);
                mAdapter.notifyDataSetChanged();
//                showToast(getText(R.string.ez_alarm_message_check_success));
                mCheckModeButton.setChecked(false);
            }

            if (mErrorCode != ERROR_WEB_NO_ERROR)
                onError(mErrorCode);
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                    ActivityUtils.handleSessionException(EZMessageActivity2.this);
                    break;

                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    ActivityUtils.handleSessionException(EZMessageActivity2.this);
                    break;

                case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                    showToast(getText(R.string.alarm_message_check_fail));
                    break;

                case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                    showToast(getText(R.string.alarm_message_check_fail_network_exception));
                    break;

                default:
                    showToast(R.string.alarm_message_check_fail, errorCode);
                    break;
            }
        }
    }

    private void sendUIMessage(Handler handler, int what, int arg1, int arg2) {
    	Message msg = Message.obtain();
    	msg.what = what;
    	msg.arg1 = arg1;
    	msg.arg2 = arg2;
    	handler.sendMessage(msg);
    }
    private static final int UI_MSG_BASE = 500;
    private static final int UI_MSG_EMPTY_LIST = UI_MSG_BASE + 1;
    private static final int UI_MSG_LIST_CHANGE = UI_MSG_BASE + 2;

    private class UIHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UI_MSG_EMPTY_LIST:
				break;
			case UI_MSG_LIST_CHANGE:
				if(mMessageList.size() == 0) {
					mCheckModeButton.setVisibility(View.GONE);
				} else {
					mCheckModeButton.setVisibility(View.VISIBLE);
				}
				break;
			default:
				break;
			}
		}
    	
    }
}