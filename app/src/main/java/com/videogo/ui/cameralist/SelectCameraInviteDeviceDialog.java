package com.videogo.ui.cameralist;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.pulltorefresh.IPullToRefresh;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase;
import com.videogo.widget.pulltorefresh.PullToRefreshListView;

import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;

import static com.videogo.EzvizApplication.getOpenSDK;

public class SelectCameraInviteDeviceDialog extends DialogFragment{
    protected static final String TAG = "SelectCameraInviteDeviceDialog";
    private PullToRefreshListView mListView = null;
    private View mNoMoreView;
    private EZCameraListInviteDeviceAdapter mAdapter = null;
    private LinearLayout mNoCameraTipLy = null;
    private LinearLayout mGetCameraFailTipLy = null;
    private TextView mCameraFailTipTv = null;

    private CameraItemClick mCameraItemClick;

    private Activity mActivity;

    public void setCameraItemClick(CameraItemClick cameraItemClick) {
        mCameraItemClick = cameraItemClick;
    }

    public SelectCameraInviteDeviceDialog(){

    }
    public interface CameraItemClick {
        public void onCameraItemClick(EZDeviceInfo deviceInfo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.select_camera_invite_device_dialog, container,false);
        mActivity = getActivity();
        mListView = (PullToRefreshListView) view.findViewById(R.id.camera_listview);
        mNoMoreView = inflater.inflate(R.layout.no_device_more_footer, null);
        mAdapter = new EZCameraListInviteDeviceAdapter(mActivity);
        mAdapter.setOnClickListener(mEZDeviceInfo -> {
            if (mCameraItemClick != null){
                mCameraItemClick.onCameraItemClick(mEZDeviceInfo);
            }
            dismiss();
        });

        mListView.setLoadingLayoutCreator(new PullToRefreshBase.LoadingLayoutCreator() {

            @Override
            public LoadingLayout create(Context context, boolean headerOrFooter, PullToRefreshBase.Orientation orientation) {
                if (headerOrFooter)
                    return new PullToRefreshHeader(context);
                else
                    return new PullToRefreshFooter(context, PullToRefreshFooter.Style.EMPTY_NO_MORE);
            }
        });
        mListView.setMode(IPullToRefresh.Mode.BOTH);
        mListView.setOnRefreshListener((refreshView, headerOrFooter) -> getCameraInfoList(headerOrFooter));
        mListView.getRefreshableView().addFooterView(mNoMoreView);
        mListView.setAdapter(mAdapter);
        mListView.getRefreshableView().removeFooterView(mNoMoreView);

        mNoCameraTipLy = (LinearLayout) view.findViewById(R.id.no_camera_tip_ly);
//        mNoCameraTipLy.setOnClickListener(v -> refreshButtonClicked());
        mGetCameraFailTipLy = (LinearLayout) view.findViewById(R.id.get_camera_fail_tip_ly);
        mCameraFailTipTv = (TextView) view.findViewById(R.id.get_camera_list_fail_tv);
        view.findViewById(R.id.camera_list_refresh_btn).setOnClickListener(v -> refreshButtonClicked());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCameraInfoList(true);
    }

    private void refreshButtonClicked() {
        mListView.setVisibility(View.VISIBLE);
        mNoCameraTipLy.setVisibility(View.GONE);
        mGetCameraFailTipLy.setVisibility(View.GONE);
        mListView.setMode(IPullToRefresh.Mode.BOTH);
        mListView.setRefreshing();
    }

    private void getCameraInfoList(boolean headerOrFooter) {
        if (mActivity.isFinishing()) {
            return;
        }
        new GetCamersInfoListTask(headerOrFooter).execute();
    }

    private class GetCamersInfoListTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {
        private boolean mHeaderOrFooter;
        private int mErrorCode = 0;

        public GetCamersInfoListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mHeaderOrFooter) {
                mListView.setVisibility(View.VISIBLE);
                mNoCameraTipLy.setVisibility(View.GONE);
                mGetCameraFailTipLy.setVisibility(View.GONE);
            }
            mListView.getRefreshableView().removeFooterView(mNoMoreView);
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... params) {
            if (mActivity.isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(mActivity)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }
            try {
                List<EZDeviceInfo> result = null;
                if (mHeaderOrFooter) {
                    result = getOpenSDK().getDeviceList(0, 20);
                } else {
                    result = getOpenSDK().getDeviceList((mAdapter.getCount() / 20) + (mAdapter.getCount() % 20 > 0 ? 1 : 0), 20);
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
            if (mActivity.isFinishing()) {
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
                } else if (result.size() < 20) {
                    mListView.setFooterRefreshEnabled(false);
                    mListView.getRefreshableView().addFooterView(mNoMoreView);
                } else if (mHeaderOrFooter) {
                    mListView.setFooterRefreshEnabled(true);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }
                mAdapter.addItems(result);
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
                    ActivityUtils.handleSessionException(mActivity);
                    break;
                default:
                    if (mAdapter.getCount() == 0) {
                        mListView.setVisibility(View.GONE);
                        mNoCameraTipLy.setVisibility(View.GONE);
                        mCameraFailTipTv.setText(Utils.getErrorTip(mActivity, R.string.get_camera_list_fail, errorCode));
                        mGetCameraFailTipLy.setVisibility(View.VISIBLE);
                    } else {
                        Utils.showToast(mActivity, R.string.get_camera_list_fail, errorCode);
                    }
                    break;
            }
        }
    }


}


