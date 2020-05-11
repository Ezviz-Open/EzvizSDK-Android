package com.videogo.remoteplayback.list;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.videogo.EzvizApplication;
import com.videogo.RootActivity;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.download.DownloadTaskRecordOfCloud;
import com.videogo.download.DownloadTaskRecordOfDevice;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZConstants.EZPlaybackConstants;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.remoteplayback.RemoteFileInfo;
import com.videogo.remoteplayback.list.bean.ClickedListItem;
import com.videogo.remoteplayback.list.bean.CloudPartInfoFileEx;
import com.videogo.remoteplayback.list.querylist.QueryCloudRecordFilesAsyncTask;
import com.videogo.remoteplayback.list.querylist.QueryPlayBackListTaskCallback;
import com.videogo.remoteplayback.list.querylist.QueryDeviceRecordFilesAsyncTask;
import com.videogo.remoteplayback.list.querylist.SectionListAdapter;
import com.videogo.remoteplayback.list.querylist.SectionListAdapter.OnHikItemClickListener;
import com.videogo.remoteplayback.list.querylist.StandardArrayAdapter;
import com.videogo.remoteplayback.list.querylist.StandardArrayAdapter.ArrayAdapterChangeListener;
import com.videogo.stream.EZCloudStreamDownload;
import com.videogo.stream.EZDeviceStreamDownload;
import com.videogo.ui.common.ScreenOrientationHelper;
import com.videogo.ui.util.AudioPlayUtil;
import com.videogo.ui.util.DataManager;
import com.videogo.ui.util.EZUtils;
import com.videogo.ui.util.VerifyCodeInput;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.MediaScanner;
import com.videogo.util.SDCardUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.PinnedHeaderListView;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;
import com.videogo.widget.loading.LoadingTextView;
import com.videogo.widget.loading.LoadingView;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;
import ezviz.ezopensdk.debug.VideoFileUtil;
import ezviz.ezopensdk.demo.DemoConfig;

@SuppressLint({"DefaultLocale", "HandlerLeak", "NewApi"})
public class EZPlayBackListActivity extends RootActivity implements QueryPlayBackListTaskCallback,
        OnHikItemClickListener, Callback, OnClickListener, OnTouchListener,
        ArrayAdapterChangeListener, VerifyCodeInput.VerifyCodeInputListener {


    // TAG
    private static final String TAG = EZPlayBackListActivity.class.getSimpleName();

    // 动画更新
    private static final int ANIMATION_UPDATE = 0xde;

    // 显示数据网络提示
    private boolean mShowNetworkTip = true;
    private BroadcastReceiver mReceiver = null;

    // 查询时间
    private Date queryDate = null;
    // 自定义ListView
    private PinnedHeaderListView pinnedHeaderListView;
    private PinnedHeaderListView mPinnedHeaderListViewForLocal;
    // 列表适配器
    private StandardArrayAdapter mCloudRecordsAdapter;
    // ListView适配器
    private SectionListAdapter sectionAdapter;

    private StandardArrayAdapter mDeviceRecordsAdapter;
    // ListView适配器
    private SectionListAdapter mSectionAdapterForLocal;

    // 列表查询task(云存储)
    private QueryCloudRecordFilesAsyncTask queryCloudRecordFilesAsyncTask;
    // 列表查询task(本地)
    private QueryDeviceRecordFilesAsyncTask queryDeviceRecordFilesAsyncTask;

    // 标题
    private TitleBar mTitleBar;
    // 查询异常布局
    private LinearLayout queryExceptionLayout;
    // 没有数据
    private LinearLayout novideoImg;
    // 没有数据本地
    private LinearLayout mNoVideoImgLocal;

    // 加载进度圈
    private LoadingTextView loadingBar;
    // 播放区域
    private RelativeLayout remotePlayBackArea;
    // 关闭播放区域按钮
    private ImageButton exitBtn;
    private SurfaceView surfaceView = null;
    private CustomTouchListener mRemotePlayBackTouchListener = null;
    // 播放比例
    private float mPlayScale = 1;

    // 本地信息
    private LocalInfo localInfo = null;
    // 音频播放
    private AudioPlayUtil mAudioPlayUtil = null;
    // 播放缓冲百分比
    private TextView remoteLoadingBufferTv, touchLoadingBufferTv;
    // 播放进度条
    private SeekBar progressSeekbar = null;
    private ProgressBar progressBar = null;
    // 开始时间文本
    private TextView beginTimeTV = null;
    // 结束时间文本
    private TextView endTimeTV = null;
    // 当前被点击的item
    private ClickedListItem currentClickItemFile;

    // 本地播放文件
    private RemoteFileInfo fileInfo;

    // 播放分辨率
    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    // 播放状态
    private int status = RemoteListContant.STATUS_INIT;
    // 播放控制区域
    private LinearLayout controlArea = null;
    private LinearLayout progressArea = null;
    // 拍照
    private ImageButton captureBtn = null;
    // 录像
    private ImageButton videoRecordingBtn = null;
    // 下载按钮
    private LinearLayout downloadBtn = null;
    // Loading图片
    private LoadingView loadingImgView;
    private LinearLayout loadingPbLayout;
    // 屏幕方向
    private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    private TextView mRemotePlayBackRatioTv = null;
    // 页面Layout
    private ViewGroup remoteListPage = null;

    // 错误信息显示
    private TextView errorInfoTV;
    private ViewGroup errorTipsVg;
    // 错误重播按钮
    private ImageButton errorReplay;
    // loading时停止出现的播放按钮
    private ImageButton loadingPlayBtn;
    // 暂停/播放按钮
    private ImageButton pauseBtn = null;
    // 声音按钮
    private ImageButton soundBtn = null;
    // 是否暂停播放，默认为没有暂停
    private boolean notPause = true;
    // 重播和下一个播放 控制区域
    private LinearLayout replayAndNextArea = null;

    private Rect mRemotePlayBackRect = null;

    private LinearLayout mRemotePlayBackRecordLy = null;

    // 回放速率
    private Button mPlaybackRateBtn = null;

    // 录像时间
    private int mRecordSecond = 0;
    // 控制栏时间值
    private int mControlDisplaySec = 0;
    // 流量限定提示框
    private AlertDialog mLimitFlowDialog = null;

    private int mCountDown = 10;
    // 录像标记点
    private ImageView mRemotePlayBackRecordIv = null;
    // 播放时间
    private TextView mRemotePlayBackRecordTv = null;
    // 重播按钮
    private ImageButton replayBtn;
    // 下一个播放按钮
    private ImageButton nextPlayBtn;
    // 定时器
    private Timer mUpdateTimer = null;
    // 定时器执行的任务
    private TimerTask mUpdateTimerTask = null;

    private String mRecordTime = null;
    // 是否为选择日期事件
    private boolean isDateSelected = false;
    // 下载动画
    private ImageView downloading;
    // 下载个数
    private TextView downloadingNumber;
    // 下载区域布局
    private RelativeLayout downLayout;
    // 云播放下载提示状态
    private boolean isCloudPrompt = false;
    // 云播放下载提示状态key
    private static final String HAS_BEAN_CLOUD_PROMPT = "has_bean_cloud_prompt";

    private SharedPreferences sharedPreferences;
    // 抖动动画
    private Animation downShake;

    private AnimationDrawable downDrawable;

    private ImageView matteImage;

    private LinearLayout autoLayout;

    // 取消按钮
    private Button cancelBtn;
    // 文件大小文本
    private TextView fileSizeText;
    // 标题栏中间日期边上的向下箭头
    private ImageView selDateImage;

    // 进度条拖动时的进度圈
    private LinearLayout touchProgressLayout;

    // 全屏按钮
    private CheckTextButton mFullscreenButton;
    private ScreenOrientationHelper mScreenOrientationHelper;

    private WaitDialog mWaitDlg = null;
    // 右上角编辑按钮
    private TextView rightEditView;
    // 左上角返回按钮
    private Button backBtn;
    // 删除视频
    private TextView deleteVideoText;

    private EZPlayer mPlaybackPlayer = null;
    private RelativeLayout mContentTabCloudRl;
    private RelativeLayout mContentTabDeviceRl;
    private CheckTextButton mCheckBtnCloud;
    private CheckTextButton mCheckBtnDevice;
    private FrameLayout mTabContentMainFrame;
    private boolean mIsLocalDataQueryPerformed = false;
    // whether it is in recording
    private boolean isRecording = false;
    private ViewGroup mControlBarRL;
    private TitleBar mLandscapeTitleBar = null;
    private EZCameraInfo mCameraInfo = null;
    private EZDeviceRecordFile mDeviceRecordInfo = null;
    private EZCloudRecordFile mCloudRecordInfo = null;

    private Handler playBackHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // showDownLoad();
            switch (msg.what) {
                // 片段播放完毕
                // 380061即开始时间>=结束时间，播放完成
                case ErrorCode.ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR:
                    Log.d(TAG, "ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR");
                    handlePlaySegmentOver();
                    break;
                case EZConstants.EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_FINISH:
                    Log.d(TAG, "MSG_REMOTEPLAYBACK_PLAY_FINISH");
                    handlePlaySegmentOver();
                    break;
                // 画面显示第一帧
                case EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_SUCCUSS:
                    handleFirstFrame(msg);
                    break;
                case EZPlaybackConstants.MSG_REMOTEPLAYBACK_STOP_SUCCESS:
                    handleStopPlayback();
                    break;
                case EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_FAIL:
                    ErrorInfo errorInfo = (ErrorInfo) msg.obj;
                    handlePlayFail(errorInfo);
                    break;
                // 处理播放链接异常
                case RemoteListContant.MSG_REMOTELIST_CONNECTION_EXCEPTION:
                    if (msg.arg1 == ErrorCode.ERROR_CAS_RECORD_SEARCH_START_TIME_ERROR) {
                        handlePlaySegmentOver();
                    }
                    break;
                case RemoteListContant.MSG_REMOTELIST_UI_UPDATE:
                    updateRemotePlayUI();
                    break;
                case RemoteListContant.MSG_REMOTELIST_STREAM_TIMEOUT:
                    handleStreamTimeOut();
                    break;
                default:
                    break;
            }
        }

    };

    // 处理播放取流超时
    private void handleStreamTimeOut() { }

    // 重播
    private void reConnectPlay(int type, Calendar uiPlayTimeOnStop) {
        newPlayInit(false, false);
        if (type == RemoteListContant.TYPE_CLOUD) {
            // do nothing
        } else {
            RemoteFileInfo fileInfo1 = this.fileInfo.copy();
            fileInfo1.setStartTime(uiPlayTimeOnStop);
        }
    }

    private void updateRemotePlayUI() {
        if (mControlDisplaySec == 5) {
            mControlDisplaySec = 0;
        }

        if (mLimitFlowDialog != null && mLimitFlowDialog.isShowing()) {
            if (mCountDown == 0) {
                dismissPopDialog(mLimitFlowDialog);
                mLimitFlowDialog = null;
                // 流量大于限定时，停止播放
                if (status != RemoteListContant.STATUS_STOP) {
                    onPlayExitBtnOnClick();
                }
            }
        }
        updateCaptureUI();

        if (isRecording) {
            updateRecordTime();
        }

        if (mPlaybackPlayer != null && status == RemoteListContant.STATUS_PLAYING) {
            Calendar osd = mPlaybackPlayer.getOSDTime();
            if (osd != null)
                handlePlayProgress(osd);
        }
    }

    // 退出播放按钮事件处理
    private void onPlayExitBtnOnClick() {
        stopRemoteListPlayer();
        remotePlayBackArea.setVisibility(View.GONE);
        // 不允许旋转屏幕
        mScreenOrientationHelper.disableSensorOrientation();
        progressBar.setVisibility(View.GONE);
        mControlDisplaySec = 0;
        loadingImgView.setVisibility(View.GONE);
        loadingPbLayout.setVisibility(View.GONE);
        touchProgressLayout.setVisibility(View.GONE);
        status = RemoteListContant.STATUS_STOP;
        notPause = false;
        pinnedHeaderListView.startAnimation();
    }

    // 更新录像时间
    private void updateRecordTime() {
        if (mRemotePlayBackRecordIv.getVisibility() == View.VISIBLE) {
            mRemotePlayBackRecordIv.setVisibility(View.INVISIBLE);
        } else {
            mRemotePlayBackRecordIv.setVisibility(View.VISIBLE);
        }
        // 计算分秒
        int leftSecond = mRecordSecond % 3600;
        int minitue = leftSecond / 60;
        int second = leftSecond % 60;

        // 显示录像时间
        String recordTime = String.format("%02d:%02d", minitue, second);
        mRemotePlayBackRecordTv.setText(recordTime);
    }

    private void dismissPopDialog(AlertDialog popDialog) {
        if (popDialog != null && popDialog.isShowing() && !isFinishing()) {
            try {
                popDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handlePlaySegmentOver() {
        LogUtil.errorLog(TAG, "handlePlaySegmentOver");
        stopRemoteListPlayer();
        stopRemotePlayBackRecord();

        if (mOrientation != Configuration.ORIENTATION_PORTRAIT) {
            setRemoteListSvLayout();
        }

        mControlDisplaySec = 0;
        exitBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        beginTimeTV.setText(endTimeTV.getText());

        status = RemoteListContant.STATUS_STOP;


        loadingPbLayout.setVisibility(View.VISIBLE);

        autoLayout.setVisibility(View.GONE);
        onNextPlayBtnClick();
    }

    private void timeBucketUIInit(long beginTime, long endTime) {
        int diffSeconds = (int) (endTime - beginTime) / 1000;
        String convToUIDuration = RemoteListUtil.convToUIDuration(diffSeconds);
        beginTimeTV.setText(RemoteListContant.VIDEO_DUAR_BEGIN_INIT);
        endTimeTV.setText(convToUIDuration);
    }

    private String mCurrentRecordPath = null;
    // 停止播放
    private void stopRemotePlayBackRecord() {
        if (!isRecording) {
            return;
        }
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        dialog("Record result", "saved to " + mCurrentRecordPath);
        if (mPlaybackPlayer != null) {
            mPlaybackPlayer.stopLocalRecord();
        }
        // 计时按钮不可见
        mRemotePlayBackRecordLy.setVisibility(View.GONE);
        // 设置录像按钮为check状态
        videoRecordingBtn.setBackgroundResource(R.drawable.palyback_video_selector);
        updateCaptureUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 页面统计
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_playback_list_page);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(false);
        getData();
        if (mCameraInfo == null) {
            LogUtil.d(TAG, "cameraInfo is null");
            finish();
        }
        initUi();
        startQueryCloudRecordFiles();
        initListener();
        initRemoteListPlayer();
        showDownPopup();
        fakePerformClickUI();

    }

    private void fakePerformClickUI() {
        if (autoLayout.getVisibility() == View.VISIBLE) {
            autoLayout.setVisibility(View.GONE);
        }
        fileSizeText.setText("");
        downloadBtn.setPadding(0, 0, 0, 0);
        remotePlayBackArea.setVisibility(View.VISIBLE);
        errorReplay.setVisibility(View.GONE);
        loadingPlayBtn.setVisibility(View.GONE);
    }

    private void showTab(int id) {
        switch (id) {
            case R.id.novideo_img:
                novideoImg.setVisibility(View.VISIBLE);
                loadingBar.setVisibility(View.GONE);
                mTabContentMainFrame.setVisibility(View.VISIBLE);
                break;
            case R.id.novideo_img_device:
                mNoVideoImgLocal.setVisibility(View.VISIBLE);
                mPinnedHeaderListViewForLocal.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mTabContentMainFrame.setVisibility(View.VISIBLE);
                break;
            case R.id.loadingTextView:
                novideoImg.setVisibility(View.GONE);
                loadingBar.setVisibility(View.VISIBLE);
                mTabContentMainFrame.setVisibility(View.GONE);
                break;
            case R.id.content_tab_device_root:
                mNoVideoImgLocal.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mTabContentMainFrame.setVisibility(View.VISIBLE);
                break;
            case R.id.ez_tab_content_frame:
                novideoImg.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                mTabContentMainFrame.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void showDownPopup() {

    }

    // 更新抓图/录像显示UI
    private void updateCaptureUI() {
        if (isRecording){
            mLandscapeTitleBar.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.removeAllLeftView();
            mLandscapeTitleBar.setTitle("recording...");
        }else{
            mLandscapeTitleBar.setVisibility(View.GONE);
        }
    }

    private Calendar getTimeBarSeekTime() {
        if (currentClickItemFile != null) {
            long beginTime = currentClickItemFile.getBeginTime();
            long endTime = currentClickItemFile.getEndTime();
            int progress = progressSeekbar.getProgress();
            long seekTime = (((endTime - beginTime) * progress) / RemoteListContant.PROGRESS_MAX_VALUE) + beginTime;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(seekTime);
            return c;
        }
        return null;
    }

    // 播放失败处理
    private void handlePlayFail(ErrorInfo errorInfo) {

        LogUtil.debugLog(TAG, "handlePlayFail. Playback failed. error info is " + errorInfo.toString());
        status = RemoteListContant.STATUS_STOP;
        stopRemoteListPlayer();

        int errorCode = errorInfo.errorCode;

        switch (errorCode) {
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR: { }
            // 收到这两个错误码，可以弹出对话框，让用户输入密码后，重新取流预览
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {
                showTipDialog("");
                DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), null);
                VerifyCodeInput.VerifyCodeInputDialog(this, this).show();
            }
            break;
            default: {
                String errorInfoText;
                if (errorCode == ErrorCode.ERROR_CAS_CONNECT_FAILED) {
                    errorInfoText = getString(R.string.remoteplayback_connect_server_error);
                } else if (errorCode == 2004/*VideoGoNetSDKException.VIDEOGONETSDK_DEVICE_EXCEPTION*/) {
                    errorInfoText = getString(R.string.realplay_fail_connect_device);
                } else if (errorCode == InnerException.INNER_DEVICE_NOT_EXIST) {
                    // 提示播放失败
                    errorInfoText = getString(R.string.camera_not_online);
                } else {
                    errorInfoText = getErrorTip(R.string.remoteplayback_fail, errorCode);
                }
                showTipDialog(errorInfoText);
                if (errorCode == ErrorCode.ERROR_CAS_STREAM_RECV_ERROR
                        || errorCode == ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE
                        || errorCode == ErrorCode.ERROR_CAS_PLATFORM_CLIENT_REQUEST_NO_PU_FOUNDED
                        || errorCode == ErrorCode.ERROR_CAS_MSG_PU_NO_RESOURCE) {
                    updateCameraInfo();
                }
            }
        }
    }

    private void updateCameraInfo() { }

    private void showTipDialog(final String errorTips) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingImgView.setVisibility(View.GONE);
                loadingPbLayout.setVisibility(View.GONE);
                touchProgressLayout.setVisibility(View.GONE);
                mControlDisplaySec = 0;

                errorReplay.setVisibility(View.VISIBLE);
                errorInfoTV.setText(errorTips);
                errorTipsVg.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleFirstFrame(Message msg) {
        if (msg.arg1 != 0) {
            mRealRatio = (float) msg.arg2 / msg.arg1;
        }
        status = RemoteListContant.STATUS_PLAYING;
        controlArea.setVisibility(View.VISIBLE);
        progressArea.setVisibility(View.VISIBLE);
        mControlDisplaySec = 0;
        captureBtn.setEnabled(true);
        videoRecordingBtn.setEnabled(true);

        setRemoteListSvLayout();
        mScreenOrientationHelper.enableSensorOrientation();
        loadingImgView.setVisibility(View.GONE);
        loadingPbLayout.setVisibility(View.GONE);
        touchProgressLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        errorTipsVg.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);

        downloadBtn.setPadding(Utils.dip2px(this, 5), 0, Utils.dip2px(this, 5), 0);
        if (localInfo.isSoundOpen()) {
            // 打开声音
            if (mPlaybackPlayer != null)
                mPlaybackPlayer.openSound();
        } else {
            // 关闭声音
            if (mPlaybackPlayer != null)
                mPlaybackPlayer.closeSound();
        }
    }

    // 收到停止回放成功的消息后处理
    private void handleStopPlayback() {
        LogUtil.debugLog(TAG, "stop playback success");
    }

    private void setRemoteListSvLayout() {
        // 设置播放窗口位置
        final int screenWidth = localInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (localInfo.getScreenHeight() - localInfo
                .getNavigationBarHeight()) : localInfo.getScreenHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                localInfo.getScreenWidth(), (int) (localInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO), screenWidth,
                screenHeight);

        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        ViewGroup playWindowVg = (ViewGroup) findViewById(R.id.vg_play_window);
        playWindowVg.setLayoutParams(svLp);

        mRemotePlayBackTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
    }

    private void onPlayAreaTouched() {
        // do nothing
    }

    private void handlePlayProgress(Calendar osdTime) {
        long osd = osdTime.getTimeInMillis();
        long begin = currentClickItemFile.getBeginTime();
        long end = currentClickItemFile.getEndTime();
        double x = ((osd - begin) * RemoteListContant.PROGRESS_MAX_VALUE) / (double) (end - begin);
        int progress = (int) x;
        progressSeekbar.setProgress(progress);
        progressBar.setProgress(progress);

        LogUtil.i(TAG, "handlePlayProgress, begin time:" + begin + " endtime:" + end
                + " osdTime:" + osdTime.getTimeInMillis() + " progress:" + progress
        );

        int beginTimeClock = (int) ((osd - begin) / 1000);
        updateTimeBucketBeginTime(beginTimeClock);
    }

    private void updateTimeBucketBeginTime(int beginTimeClock) {
        String convToUIDuration = RemoteListUtil.convToUIDuration(beginTimeClock);
        beginTimeTV.setText(convToUIDuration);
    }

    private void initEZPlayer() {
        if (mPlaybackPlayer != null) {
            // 停止录像
            mPlaybackPlayer.stopLocalRecord();
            // 停止播放
            mPlaybackPlayer.stopPlayback();
        } else {
            mPlaybackPlayer = EzvizApplication.getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
            mPlaybackPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
        }
    }

    private void initRemoteListPlayer() {
        stopPlayTask();
        stopRemoteListPlayer();

        if (status != RemoteListContant.STATUS_DECRYPT) {
            status = RemoteListContant.STATUS_INIT;
        }
    }

    private void initListener() {
        backBtn = mTitleBar.addBackButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onExitCurrentPage();
                finish();
            }
        });
        selDateImage = mTitleBar.addTitleButton(R.drawable.remote_cal_selector, new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处于编辑状态不可点击
                if (sectionAdapter != null && sectionAdapter.isEdit()) {
                    return;
                }
                goToCalendar();
            }
        });
        mTitleBar.setOnTitleClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 处于编辑状态不可点击
                if (sectionAdapter != null && sectionAdapter.isEdit()) {
                    return;
                }
                goToCalendar();
            }
        });

        downLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                startActivity(new Intent(EZPlayBackListActivity.this, ImagesManagerActivity.class));
                /*if (downloadHelper.getDownloadCountInQueue() == 0) {
                    downLayout.setVisibility(View.INVISIBLE);
                }*/
            }
        });

        downloadBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mCloudRecordInfo != null){
                    startDownloadCloudVideo(mCloudRecordInfo);
                }else{
                    startDownloadDeviceVideo(mDeviceRecordInfo);
                }
            }
        });

        exitBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onPlayExitBtnOnClick();
            }
        });

        rightEditView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rightEditView.setLayoutParams(layoutParams);
        rightEditView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        rightEditView.setPadding(0, 0, Utils.dip2px(this, 15), 0);
        mTitleBar.addRightView(rightEditView);
        rightEditView.setVisibility(View.GONE);
        deleteVideoText.setOnClickListener(this);
        // loading继续播放按钮
        loadingPlayBtn.setOnClickListener(this);
        // 重播按钮事件
        replayBtn.setOnClickListener(this);
        errorReplay.setOnClickListener(this);
        // 播放下一片段按钮事件
        nextPlayBtn.setOnClickListener(this);
        // 查询异常区域touch事件
        queryExceptionLayout.setOnTouchListener(this);
        // 回放区域touch事件
        remotePlayBackArea.setOnTouchListener(this);
        // 控制区域touch事件
        controlArea.setOnTouchListener(this);
        controlArea.setOnClickListener(this);
        // 暂停播放按钮事件
        pauseBtn.setOnClickListener(this);
        // 声音按钮事件
        soundBtn.setOnClickListener(this);
        // 退出播放按钮事件
        exitBtn.setOnClickListener(this);
        // 抓图按钮事件
        captureBtn.setOnClickListener(this);
        // 录像按钮事件
        videoRecordingBtn.setOnClickListener(this);
        // 抓图/录像形成图片区域点击事件

        progressSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            /**
             * 拖动条停止拖动的时候调用
             */
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                int progress = arg0.getProgress();
                if (progress == RemoteListContant.PROGRESS_MAX_VALUE) {
                    stopRemoteListPlayer();
                    handlePlaySegmentOver();
                    return;
                }
                if (currentClickItemFile != null) {
                    long beginTime = currentClickItemFile.getBeginTime();
                    long endTime = currentClickItemFile.getEndTime();
                    long avg = (endTime - beginTime) / RemoteListContant.PROGRESS_MAX_VALUE;
                    long trackTime = beginTime + (progress * avg);

                    seekInit(true, false);
                    progressBar.setProgress(progress);

                    LogUtil.i(TAG, "onSeekBarStopTracking, begin time:"
                            + beginTime + " endtime:" + endTime
                            + " avg:" + avg + " MAX:"
                            + RemoteListContant.PROGRESS_MAX_VALUE
                            + " tracktime:" + trackTime);
                    if (mPlaybackPlayer != null) {
                        Calendar seekTime = Calendar.getInstance();
                        seekTime.setTime(new Date(trackTime));

                        mPlaybackPlayer.seekPlayback(seekTime);
                    }
                }
            }

            /**
             * 拖动条开始拖动的时候调用
             */
            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            /**
             * 拖动条进度改变的时候调用
             */
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                if (currentClickItemFile != null) {
                    long time = currentClickItemFile.getEndTime() - currentClickItemFile.getBeginTime();
                    int diffSeconds = (int) (time * arg1 / 1000) / 1000;
                    String convToUIDuration = RemoteListUtil.convToUIDuration(diffSeconds);
                    beginTimeTV.setText(convToUIDuration);
                }
            }
        });

        downShake.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                downLayout.clearAnimation();
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.debugLog(TAG, "onReceive:" + intent.getAction());
            }
        };
        IntentFilter filter = new IntentFilter();
        registerReceiver(mReceiver, filter);
    }

    // 退出编辑状态
    private void exitEditStatus() {
        selDateImage.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        deleteVideoText.setVisibility(View.GONE);
        sectionAdapter.clearAllSelectedCloudFiles();
        sectionAdapter.setEdit(false);
        mCloudRecordsAdapter.notifyDataSetChanged();
        pinnedHeaderListView.startAnimation();
    }

    private void onExitCurrentPage() {
        notPause = true;
        stopQueryTask();
        closePlayBack();
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        if (backBtn != null && backBtn.getVisibility() == View.GONE) {
            exitEditStatus();
        } else {
            onExitCurrentPage();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        closePlayBack();

        if (mPlaybackPlayer != null) {
            EzvizApplication.getOpenSDK().releasePlayer(mPlaybackPlayer);
        }
        stopQueryTask();
        removeHandler(handler);
        removeHandler(playBackHandler);
    }

    protected void removeHandler(Handler handler) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * download video from ezviz cloud
     * @param cloudFile video file of ezviz cloud
     */
    private void startDownloadCloudVideo(final EZCloudRecordFile cloudFile) {
        if (cloudFile == null){
            return;
        }
        final String notificationTitle = "download video from cloud";
        final int notificationId = getUniqueNotificationId();
        showSimpleNotification(notificationId, notificationTitle, "downloading...click to cancel!", true);
        getTaskManager().submit(new Runnable() {
            @Override
            public void run() {
                String strFileNameWithPath = DemoConfig.getRecordsFolder() + "/cloud_" + System.currentTimeMillis() + ".mp4";
                final File file = new File(strFileNameWithPath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                final EZCloudStreamDownload  ezCloudStreamDownloader = new EZCloudStreamDownload(strFileNameWithPath, cloudFile);
                ezCloudStreamDownloader.setStreamDownloadCallback(new EZStreamDownloadCallbackWithNotify(notificationId, notificationTitle));
                ezCloudStreamDownloader.setSecretKey(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
                ezCloudStreamDownloader.start();
                mDownloadTaskRecordListAbstract.add(new DownloadTaskRecordOfCloud(ezCloudStreamDownloader, notificationId));

                toast("started! And you can find download progress from notification bar.");
            }
        });
    }

    /**
     * download video from ezviz device
     * @param deviceFile video file of device SdCard
     */
    private void startDownloadDeviceVideo(final EZDeviceRecordFile deviceFile){
        if (deviceFile == null){
            return;
        }
        final String notificationTitle = "download video from sdcard";
        final int notificationId = getUniqueNotificationId();
        showSimpleNotification(notificationId, notificationTitle, "downloading...click to cancel!", true);
        getTaskManager().submit(new Runnable() {
            @Override
            public void run() {
                String strRecordFilePath = DemoConfig.getRecordsFolder() + "/device_" + System.currentTimeMillis() + ".mp4";
                File file = new File(strRecordFilePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                final EZDeviceStreamDownload ezDeviceStreamDownloader = new EZDeviceStreamDownload(strRecordFilePath, mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), deviceFile);
                ezDeviceStreamDownloader.setStreamDownloadCallback(new EZStreamDownloadCallbackWithNotify(notificationId, notificationTitle));
                ezDeviceStreamDownloader.setSecretKey(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
                ezDeviceStreamDownloader.start();
                mDownloadTaskRecordListAbstract.add(new DownloadTaskRecordOfDevice(ezDeviceStreamDownloader, notificationId));

                dialog(notificationTitle,"Note! It is very slow to download video from sdcard because of the device's limits! " +
                        "It will spend time about video length. So please wait patiently, and you can find download progress from notification bar." );
            }
        });
    }

    private class EZStreamDownloadCallbackWithNotify extends EZOpenSDKListener.EZStreamDownloadCallbackEx {

        private int notificationId;
        private String notificationTitle;

        public EZStreamDownloadCallbackWithNotify(int notificationId, String notificationTitle){
            this.notificationId = notificationId;
            this.notificationTitle = notificationTitle;
        }

        @Override
        public void onSuccess(final String filepath) {
            String successMsg = "saved video to " + filepath;
            LogUtil.d(TAG, successMsg);
            toast(successMsg);
            updateNotification(notificationId, successMsg);
        }

        @Override
        public void onError(final EZOpenSDKListener.EZStreamDownloadError code) {
            String failMsg = "failed: ";
            switch (code){
                case ERROR_EZSTREAM_DOWNLOAD_MAX_CONNECTIONS:
                    failMsg += " device reached max connections!";
                    break;
                default:
                    failMsg += code;
                    break;
            }
            LogUtil.d(TAG, failMsg);
            updateNotification(notificationId, failMsg);
        }

        @Override
        public void onErrorCode(int code) {
            showToast("onErrorCode: " + code);
        }

        private void updateNotification(int id, String content){
            showSimpleNotification(id, notificationTitle, content, false);
        }
    }

    private void stopRemoteListPlayer() {
        try {
            if (mPlaybackPlayer != null) {
                mPlaybackPlayer.stopPlayback();
                mPlaybackPlayer.stopLocalRecord();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ANIMATION_UPDATE:
                    ImageButton imageButton = (ImageButton) msg.obj;
                    if (downShake == null || downLayout == null || imageButton == null || downloadingNumber == null) {
                        return;
                    }
                    downLayout.startAnimation(downShake);
                    imageButton.setVisibility(View.GONE);
                    ViewGroup parent = (ViewGroup) imageButton.getParent();
                    parent.removeView(imageButton);
                    if (downloadingNumber.getVisibility() == View.INVISIBLE) {
                        downloadingNumber.setVisibility(View.VISIBLE);
                    }
                    startGifAnimation();
                    break;
                default:
                    break;
            }
        }

    };

    // 切换到日历界面
    private void goToCalendar() {
        if (getMinDate() != null && new Date().before(getMinDate())) {
            showToast(R.string.calendar_setting_error);
            return;
        }
        showDatePicker();
    }

    private void showDatePicker() {
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(queryDate);
        DatePickerDialog dpd = new DatePickerDialog(this, null, nowCalendar.get(Calendar.YEAR),
                nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        dpd.setCancelable(true);
        dpd.setTitle(R.string.select_date);
        dpd.setCanceledOnTouchOutside(true);
        dpd.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.certain),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dg, int which) {
                        DatePicker dp = null;
                        Field[] fields = dg.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            if (field.getName().equals("mDatePicker")) {
                                try {
                                    dp = (DatePicker) field.get(dg);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (dp != null) {
                            dp.clearFocus();
                            Calendar selectCalendar = Calendar.getInstance();
                            selectCalendar.set(Calendar.YEAR, dp.getYear());
                            selectCalendar.set(Calendar.MONTH, dp.getMonth());
                            selectCalendar.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
                            rightEditView.setVisibility(View.GONE);
                            isDateSelected = true;
                            queryDate = selectCalendar.getTime();
                            onDateChanged();
                        }
                    }
                });
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtil.debugLog("Picker", "Cancel!");
                        if (!isFinishing()) {
                            dialog.dismiss();
                        }

                    }
                });

        dpd.show();
    }

    private Date getMinDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse("2012-01-01");
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onDateChanged() {
        if (queryDate != null){
            mTitleBar.setTitle(RemoteListUtil.converToMonthAndDay(queryDate));
        }
        switch (mRecordType){
            case RemoteListContant.TYPE_CLOUD:
                mDeviceRecordsAdapter = null;
                startQueryCloudRecordFiles();
                break;
            case RemoteListContant.TYPE_LOCAL:
                mCloudRecordsAdapter = null;
                startQueryDeviceRecordFiles();
                break;
            default:
                // do nothing
                break;
        }
    }

    private void startQueryCloudRecordFiles() {
        if (queryDate != null){
            mTitleBar.setTitle(RemoteListUtil.converToMonthAndDay(queryDate));
        }
        pinnedHeaderListView.setVisibility(View.GONE);
        queryExceptionLayout.setVisibility(View.GONE);
        stopQueryTask();
        mCloudRecordsAdapter = null;
        sectionAdapter = null;
        hasShowListViewLine(false);
        queryCloudRecordFilesAsyncTask = new QueryCloudRecordFilesAsyncTask(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(),
                EZPlayBackListActivity.this);
        loadingBar.setVisibility(View.VISIBLE);
        showTab(R.id.loadingTextView);
        queryCloudRecordFilesAsyncTask.setSearchDate(queryDate);
        queryCloudRecordFilesAsyncTask.execute();
    }

    private void startQueryDeviceRecordFiles() {
        int cloudTotal = 100000;
        hasShowListViewLine(false);
        mWaitDlg.show();
        stopQueryTask();
        queryDeviceRecordFilesAsyncTask = new QueryDeviceRecordFilesAsyncTask(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(),
                EZPlayBackListActivity.this);
        queryDeviceRecordFilesAsyncTask.setQueryDate(queryDate);
        queryDeviceRecordFilesAsyncTask.setOnlyHasLocal(true);
        queryDeviceRecordFilesAsyncTask.execute(String.valueOf(cloudTotal));
    }

    private void hasShowListViewLine(boolean isShow) {
        if (isShow) {
            findViewById(R.id.listview_line).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.listview_line).setVisibility(View.INVISIBLE);
        }
    }

    private void stopQueryTask() {
        if (queryCloudRecordFilesAsyncTask != null) {
            queryCloudRecordFilesAsyncTask.cancel(true);
            queryCloudRecordFilesAsyncTask.setAbort(true);
            queryCloudRecordFilesAsyncTask = null;
        }

        if (queryDeviceRecordFilesAsyncTask != null) {
            queryDeviceRecordFilesAsyncTask.cancel(true);
            queryDeviceRecordFilesAsyncTask.setAbort(true);
            queryDeviceRecordFilesAsyncTask = null;
        }
    }

    private void initUi() {
        mContentTabCloudRl = (RelativeLayout) findViewById(R.id.content_tab_cloud_root);
        mContentTabDeviceRl = (RelativeLayout) findViewById(R.id.content_tab_device_root);
        mCheckBtnCloud = (CheckTextButton) findViewById(R.id.pb_search_tab_btn_cloud);
        mCheckBtnDevice = (CheckTextButton) findViewById(R.id.pb_search_tab_btn_device);
        mTabContentMainFrame = (FrameLayout) findViewById(R.id.ez_tab_content_frame);

        mCheckBtnDevice.setToggleEnable(false);
        mCheckBtnCloud.setToggleEnable(false);
        mCheckBtnCloud.setChecked(true);
        OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.getId() == R.id.pb_search_tab_btn_cloud) {
                    mContentTabCloudRl.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mCheckBtnDevice.setChecked(!isChecked);
                } else if ((buttonView.getId() == R.id.pb_search_tab_btn_device)) {
                    mContentTabDeviceRl.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mCheckBtnCloud.setChecked(!isChecked);
                }
            }
        };
        mCheckBtnDevice.setOnCheckedChangeListener(onCheckedChangeListener);
        mCheckBtnCloud.setOnCheckedChangeListener(onCheckedChangeListener);

        mCheckBtnCloud.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mCheckBtnCloud.isChecked()) {
                    mCheckBtnCloud.setChecked(true);
                    if (mCloudRecordsAdapter == null){
                        startQueryCloudRecordFiles();
                    }
                }
            }
        });
        mCheckBtnDevice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mCheckBtnDevice.isChecked()) {
                    mCheckBtnDevice.setChecked(true);
                    if (mDeviceRecordsAdapter == null){
                        startQueryDeviceRecordFiles();
                    }
                }
            }
        });

        pinnedHeaderListView = (PinnedHeaderListView) findViewById(R.id.listView);
        mPinnedHeaderListViewForLocal = (PinnedHeaderListView) findViewById(R.id.listView_device);
        remoteListPage = (ViewGroup) findViewById(R.id.remote_list_page);
        mTitleBar = (TitleBar) findViewById(R.id.title);
        /* 测量状态栏高度 **/
        ViewTreeObserver viewTreeObserver = remoteListPage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mRemotePlayBackRect == null) {
                    // 获取状况栏高度
                    mRemotePlayBackRect = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(mRemotePlayBackRect);
                }
            }
        });
        queryExceptionLayout = (LinearLayout) findViewById(R.id.query_exception_ly);
        novideoImg = (LinearLayout) findViewById(R.id.novideo_img);
        mNoVideoImgLocal = (LinearLayout) findViewById(R.id.novideo_img_device);
        loadingBar = (LoadingTextView) findViewById(R.id.loadingTextView);
        loadingBar.setText(R.string.loading_text_default);
        remoteLoadingBufferTv = (TextView) findViewById(R.id.remote_loading_buffer_tv);
        touchLoadingBufferTv = (TextView) findViewById(R.id.touch_loading_buffer_tv);
        remotePlayBackArea = (RelativeLayout) findViewById(R.id.remote_playback_area);
        endTimeTV = (TextView) findViewById(R.id.end_time_tv);
        exitBtn = (ImageButton) findViewById(R.id.exit_btn);
        surfaceView = (SurfaceView) findViewById(R.id.remote_playback_wnd_sv);
        surfaceView.getHolder().addCallback(this);
        mRemotePlayBackRatioTv = (TextView) findViewById(R.id.remoteplayback_ratio_tv);
        mRemotePlayBackTouchListener = new CustomTouchListener() {

            @Override
            public boolean canZoom(float scale) {
                // do nothing
                return false;
            }

            @Override
            public boolean canDrag(int direction) {
                return mPlayScale != 1;
            }

            @Override
            public void onSingleClick() {
                onPlayAreaTouched();
            }

            @Override
            public void onDoubleClick(MotionEvent e) {
            }

            @Override
            public void onZoom(float scale) {
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.debugLog(TAG, "onDrag:" + direction);
            }

            @Override
            public void onEnd(int mode) {
                LogUtil.debugLog(TAG, "onEnd:" + mode);
            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.debugLog(TAG, "onZoomChange:" + scale);
            }
        };
        surfaceView.setOnTouchListener(mRemotePlayBackTouchListener);

        setRemoteListSvLayout();

        mPlaybackRateBtn = (Button) findViewById(R.id.btn_change_playback_rate);
        mRemotePlayBackRecordLy = (LinearLayout) findViewById(R.id.remoteplayback_record_ly);
        progressSeekbar = (SeekBar) findViewById(R.id.progress_seekbar);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        beginTimeTV = (TextView) findViewById(R.id.begin_time_tv);
        controlArea = (LinearLayout) findViewById(R.id.control_area);
        progressArea = (LinearLayout) findViewById(R.id.progress_area);
        captureBtn = (ImageButton) findViewById(R.id.remote_playback_capture_btn);
        videoRecordingBtn = (ImageButton) findViewById(R.id.remote_playback_video_recording_btn);
        downloadBtn = (LinearLayout) findViewById(R.id.remote_playback_download_btn);
        downLayout = (RelativeLayout) findViewById(R.id.down_layout);
        fileSizeText = (TextView) findViewById(R.id.file_size_text);
        deleteVideoText = (TextView) findViewById(R.id.delete_playback);
        measure(downloadBtn);
        measure(downLayout);
        measure(controlArea);
        downloading = (ImageView) findViewById(R.id.downloading);
        downDrawable = ((AnimationDrawable) downloading.getBackground());
        downloadingNumber = (TextView) findViewById(R.id.downloading_number);
        loadingImgView = (LoadingView) findViewById(R.id.remote_loading_iv);
        loadingPbLayout = (LinearLayout) findViewById(R.id.loading_pb_ly);

        errorInfoTV = (TextView) findViewById(R.id.error_info_tv);
        errorTipsVg = (ViewGroup) findViewById(R.id.vg_error_tips);
        errorReplay = (ImageButton) findViewById(R.id.error_replay_btn);
        loadingPlayBtn = (ImageButton) findViewById(R.id.loading_play_btn);
        pauseBtn = (ImageButton) findViewById(R.id.remote_playback_pause_btn);
        soundBtn = (ImageButton) findViewById(R.id.remote_playback_sound_btn);
        replayAndNextArea = (LinearLayout) findViewById(R.id.re_next_area);

        mRemotePlayBackRecordIv = (ImageView) findViewById(R.id.remoteplayback_record_iv);
        mRemotePlayBackRecordTv = (TextView) findViewById(R.id.remoteplayback_record_tv);
        replayBtn = (ImageButton) findViewById(R.id.replay_btn);
        nextPlayBtn = (ImageButton) findViewById(R.id.next_play_btn);
        progressSeekbar.setMax(RemoteListContant.PROGRESS_MAX_VALUE);
        progressBar.setMax(RemoteListContant.PROGRESS_MAX_VALUE);
        matteImage = (ImageView) findViewById(R.id.matte_image);

        autoLayout = (LinearLayout) findViewById(R.id.auto_play_layout);
        autoLayout.setVisibility(View.GONE);
        cancelBtn = (Button) findViewById(R.id.cancel_auto_play_btn);
        cancelBtn.setOnClickListener(this);

        touchProgressLayout = (LinearLayout) findViewById(R.id.touch_progress_layout);
        showDownLoad();

        mFullscreenButton = (CheckTextButton) findViewById(R.id.fullscreen_button);
        mScreenOrientationHelper = new ScreenOrientationHelper(this, mFullscreenButton);
        notPause = true;
        mControlBarRL = (ViewGroup) findViewById(R.id.flow_area);

        mLandscapeTitleBar = (TitleBar) findViewById(R.id.pb_title_bar_landscape);
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                null/*getResources().getDrawable(R.drawable.message_back_selector)*/);
        mLandscapeTitleBar.setOnTouchListener(this);
        if (mCameraInfo != null) {
            mLandscapeTitleBar.setTitle(mCameraInfo.getCameraName());
        }
        mLandscapeTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void startGifAnimation() {
        if (!downDrawable.isRunning()) {
            downDrawable = (AnimationDrawable) downloading.getBackground();
            downDrawable.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScreenOrientationHelper.postOnStop();
        LogUtil.debugLog(TAG, "onStop():" + notPause + " status:" + status);

        if (notPause) {
            closePlayBack();
        }
    }

    private void closePlayBack() {
        if (status == RemoteListContant.STATUS_EXIT_PAGE) {
            return;
        }
        LogUtil.debugLog(TAG, "停止运行.........");
        stopPlayTask();
        stopRemoteListPlayer();

        onActivityStopUI();
        stopUpdateTimer();
        status = RemoteListContant.STATUS_EXIT_PAGE;
        if (surfaceView != null)
            surfaceView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.debugLog(TAG, "onResume()");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(surfaceView.getWindowToken(), 0);
            }
        }, 200);

        int downCount = 0;//downloadHelper.getDownloadCountInQueue();
        downloadingNumber.setText("" + downCount);
        if (downCount <= 0) {
            downLayout.setVisibility(View.INVISIBLE);
            downloadingNumber.setVisibility(View.INVISIBLE);
        } else {
            startGifAnimation();
        }

        // 判断是否处理暂停状态
        if (notPause || status == RemoteListContant.STATUS_DECRYPT) {
            surfaceView.setVisibility(View.VISIBLE);
            onActivityResume();
            startUpdateTimer();
            isDateSelected = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mScreenOrientationHelper.postOnStart();
    }

    private void startUpdateTimer() {
        stopUpdateTimer();
        // 开始录像计时
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 流量提醒
                if (mLimitFlowDialog != null && mLimitFlowDialog.isShowing() && mCountDown > 0) {
                    mCountDown--;
                }

                // 录像显示
                if (isRecording) {
                    // 更新录像时间
                    Calendar OSDTime = null;
                    if (mPlaybackPlayer != null)
                        OSDTime = mPlaybackPlayer.getOSDTime();
                    if (OSDTime != null) {
                        String playtime = Utils.OSD2Time(OSDTime);
                        if (!playtime.equals(mRecordTime)) {
                            mRecordSecond++;
                            mRecordTime = playtime;
                        }
                    }
                }
                sendMessage(RemoteListContant.MSG_REMOTELIST_UI_UPDATE, 0, 0);
            }
        };
        // 延时1000ms后执行，1000ms执行一次
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    private void sendMessage(int message, int arg1, int arg2) {
        if (playBackHandler != null) {
            Message msg = playBackHandler.obtainMessage();
            msg.what = message;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            playBackHandler.sendMessage(msg);
        }
    }

    // 页面可见
    private void onActivityResume() {
        if (!isDateSelected && currentClickItemFile != null) {
            if (currentClickItemFile.getUiPlayTimeOnStop() != null) {
                int type = currentClickItemFile.getType();
                Calendar uiPlayTimeOnStop = currentClickItemFile.getUiPlayTimeOnStop();
                reConnectPlay(type, uiPlayTimeOnStop);
            } else if (status == RemoteListContant.STATUS_EXIT_PAGE || status == RemoteListContant.STATUS_DECRYPT) {
                onReplayBtnClick();
            }
        }
    }

    // 停止定时器
    private void stopUpdateTimer() {
        mControlDisplaySec = 0;
        // 停止录像计时
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if (mUpdateTimerTask != null) {
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    // 页面不可见时UI
    private void onActivityStopUI() {
        if (exitBtn != null)
            exitBtn.setVisibility(View.GONE);
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        mControlDisplaySec = 0;
    }

    // 停止播放录像任务
    private void stopPlayTask() { }

    private void getData() {
        localInfo = LocalInfo.getInstance();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            queryDate = (Date) bundle.getSerializable(RemoteListContant.QUERY_DATE_INTENT_KEY);
            mCameraInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
        }
        Application application = getApplication();
        mAudioPlayUtil = AudioPlayUtil.getInstance(application);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        localInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        localInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
        sharedPreferences = getSharedPreferences(Constant.VIDEOGO_PREFERENCE_NAME, 0);
        isCloudPrompt = sharedPreferences.getBoolean(HAS_BEAN_CLOUD_PROMPT, true);

        downShake = AnimationUtils.loadAnimation(this, R.anim.button_shake);
        downShake.reset();
        downShake.setFillAfter(true);
    }

    @Override
    public void queryHasNoData() {
        showTab(R.id.novideo_img);
    }

    @Override
    public void queryOnlyHasLocalFile() {
        hasShowListViewLine(false);
        stopQueryTask();
        queryDeviceRecordFilesAsyncTask = new QueryDeviceRecordFilesAsyncTask(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), this);
        queryDeviceRecordFilesAsyncTask.setQueryDate(queryDate);
        queryDeviceRecordFilesAsyncTask.setOnlyHasLocal(true);
        queryDeviceRecordFilesAsyncTask.execute(String.valueOf(0));
    }

    // 录像查询为空UI显示
    private void queryNoDataUIDisplay() {
        loadingBar.setVisibility(View.GONE);
        novideoImg.setVisibility(View.VISIBLE);
        showTab(R.id.novideo_img);
    }

    @Override
    public void queryLocalException() {
        // do nothing
    }

    @Override
    public void querySuccessFromCloud(List<CloudPartInfoFileEx> cloudPartInfoFileExs, int queryMLocalStatus,
                                      List<CloudPartInfoFile> cloudPartInfoFile) {
        rightEditView.setVisibility(View.VISIBLE);
        findViewById(R.id.display_layout).setVisibility(View.VISIBLE);
        hasShowListViewLine(true);
        loadingBar.setVisibility(View.GONE);
        pinnedHeaderListView.setVisibility(View.VISIBLE);
        showTab(R.id.ez_tab_content_frame);
        if (queryMLocalStatus == RemoteListContant.HAS_LOCAL) {
            CloudPartInfoFileEx partInfoFileEx = new CloudPartInfoFileEx();
            partInfoFileEx.setMore(true);
            cloudPartInfoFileExs.add(partInfoFileEx);
        }
        mCloudRecordsAdapter = new StandardArrayAdapter(this, R.id.layout, cloudPartInfoFileExs);
        mCloudRecordsAdapter.setAdapterChangeListener(this);
        sectionAdapter = new SectionListAdapter(EZPlayBackListActivity.this, getLayoutInflater(), mCloudRecordsAdapter, mCameraInfo.getDeviceSerial());
        pinnedHeaderListView.setAdapter(sectionAdapter);

        pinnedHeaderListView.setOnScrollListener(sectionAdapter);
        pinnedHeaderListView.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.list_section,
                pinnedHeaderListView, false));
        pinnedHeaderListView.startAnimation();
        sectionAdapter.setOnHikItemClickListener(EZPlayBackListActivity.this);
    }

    @Override
    public void querySuccessFromDevice(List<CloudPartInfoFileEx> cloudPartInfoFileExs, int position,
                                       List<CloudPartInfoFile> cloudPartInfoFile) {
        hasShowListViewLine(true);
        showTab(R.id.content_tab_device_root);
        mPinnedHeaderListViewForLocal.setVisibility(View.VISIBLE);

        if (mDeviceRecordsAdapter != null) {
            mDeviceRecordsAdapter.clear();
            mDeviceRecordsAdapter.addLocalFileExAll(cloudPartInfoFileExs);
            mDeviceRecordsAdapter.notifyDataSetChanged();
            int selPosition = mDeviceRecordsAdapter.getCloudFileEx().size() - 2;
            if (getAndroidOSVersion() < 14) {
                mPinnedHeaderListViewForLocal.setSelection(selPosition > 0 ? selPosition : 0);
            } else {
                mPinnedHeaderListViewForLocal.smoothScrollToPositionFromTop(selPosition > 0 ? selPosition : 0, 100, 500);
            }
        } else {
            mDeviceRecordsAdapter = new StandardArrayAdapter(this, R.id.layout, cloudPartInfoFileExs);
            mDeviceRecordsAdapter.setAdapterChangeListener(this);
            mSectionAdapterForLocal = new SectionListAdapter(EZPlayBackListActivity.this, getLayoutInflater(), mDeviceRecordsAdapter, mCameraInfo.getDeviceSerial());
            mPinnedHeaderListViewForLocal.setAdapter(mSectionAdapterForLocal);
            mPinnedHeaderListViewForLocal.setOnScrollListener(mSectionAdapterForLocal);
            mPinnedHeaderListViewForLocal.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.list_section,
                    mPinnedHeaderListViewForLocal, false));
            mPinnedHeaderListViewForLocal.startAnimation();
            mSectionAdapterForLocal.setOnHikItemClickListener(EZPlayBackListActivity.this);
        }
    }


    @Override
    public void queryOnlyLocalNoData() {
        queryNoDataUIDisplay();
        showTab(R.id.novideo_img_device);
    }

    @Override
    public void queryLocalNoData() {
        showTab(R.id.novideo_img_device);
    }

    @Override
    public void queryException() {
        loadingBar.setVisibility(View.GONE);
        queryExceptionLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.display_layout).setVisibility(View.GONE);
    }

    private int mRecordType;

    @Override
    public void queryTaskOver(int type, int queryMode, int queryErrorCode, String detail) {
        mRecordType = type;
        if (type == RemoteListContant.TYPE_CLOUD) {
            LogUtil.errorLog(TAG, "queryTaskOver: TYPE_CLOUD");
        } else if (type == RemoteListContant.TYPE_LOCAL) {
            if (mWaitDlg != null && mWaitDlg.isShowing()) {
                mWaitDlg.dismiss();
            }
            LogUtil.errorLog(TAG, "queryTaskOver: TYPE_LOCAL");
            queryDeviceRecordFilesAsyncTask = null;
        }
    }

    private int getAndroidOSVersion() {
        int osVersion;
        try {
            osVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            osVersion = 0;
        }
        return osVersion;
    }

    private void convertCloudPartInfoFile2EZCloudRecordFile(EZCloudRecordFile dst, CloudPartInfoFile src) {
        dst.setCoverPic(src.getPicUrl());
        dst.setDownloadPath(src.getDownloadPath());
        dst.setFileId(src.getFileId());
        dst.setEncryption(src.getKeyCheckSum());
        dst.setStartTime(Utils.convert14Calender(src.getStartTime()));
        dst.setStopTime(Utils.convert14Calender(src.getEndTime()));
        dst.setDeviceSerial(src.getDeviceSerial());
        dst.setCameraNo(src.getCameraNo());
        dst.setVideoType(src.getVideoType());
        dst.setiStorageVersion(src.getiStorageVersion());
    }

    private void convertCloudPartInfoFile2EZDeviceRecordFile(EZDeviceRecordFile dst, CloudPartInfoFile src) {
        dst.setStartTime(Utils.convert14Calender(src.getStartTime()));
        dst.setStopTime(Utils.convert14Calender(src.getEndTime()));
    }

    @Override
    public void onHikItemClickListener(CloudPartInfoFile cloudFile, ClickedListItem playClickItem) {
        if (autoLayout.getVisibility() == View.VISIBLE) {
            autoLayout.setVisibility(View.GONE);
        }
        fileSizeText.setText("");
        newPlayInit(true, true);
        timeBucketUIInit(playClickItem.getBeginTime(), playClickItem.getEndTime());
        currentClickItemFile = playClickItem;
        // this.cloudFile = cloudFile;
        mDeviceRecordInfo = null;
        mCloudRecordInfo = null;

        if (!cloudFile.isCloud()) {
                downloadBtn.setVisibility(View.VISIBLE);
            RemoteFileInfo fileInfo = cloudFile.getRemoteFileInfo();
            this.fileInfo = fileInfo.copy();
            mDeviceRecordInfo = new EZDeviceRecordFile(); mCloudRecordInfo = null;
            convertCloudPartInfoFile2EZDeviceRecordFile(mDeviceRecordInfo, cloudFile);
            mSectionAdapterForLocal.setSelection(cloudFile.getPosition());
            if (getAndroidOSVersion() < 14) {
                mPinnedHeaderListViewForLocal.setSelection(playClickItem.getPosition());
            } else {
                mPinnedHeaderListViewForLocal.smoothScrollToPositionFromTop(playClickItem.getPosition(), 100, 500);
            }
            mPlaybackPlayer.setHandler(playBackHandler);
            mPlaybackPlayer.setSurfaceHold(surfaceView.getHolder());

            startRecordOriginVideo();
            mPlaybackPlayer.startPlayback(mDeviceRecordInfo);
        } else {
            downloadBtn.setVisibility(View.VISIBLE);
            sectionAdapter.setSelection(cloudFile.getPosition());
            if (getAndroidOSVersion() < 14) {
                pinnedHeaderListView.setSelection(playClickItem.getPosition());
            } else {
                pinnedHeaderListView.smoothScrollToPositionFromTop(playClickItem.getPosition(), 100, 500);
            }

            if (!isCloudPrompt) {
                isCloudPrompt = true;
                sharedPreferences.edit().putBoolean(HAS_BEAN_CLOUD_PROMPT, true).commit();
                // setWindowAlpha(0.2f);
                matteImage.setVisibility(View.VISIBLE);
                mScreenOrientationHelper.disableSensorOrientation();
            } else {
                mCloudRecordInfo = new EZCloudRecordFile(); mDeviceRecordInfo = null;
                convertCloudPartInfoFile2EZCloudRecordFile(mCloudRecordInfo, cloudFile);
                mPlaybackPlayer.setHandler(playBackHandler);
                mPlaybackPlayer.setSurfaceHold(surfaceView.getHolder());

                startRecordOriginVideo();
                mPlaybackPlayer.startPlayback(mCloudRecordInfo);
            }
        }
        showDownLoad();
    }

    private void measure(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
    }

    private void newSeekPlayUIInit() {
        touchProgressLayout.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        exitBtn.setVisibility(View.GONE);
        replayAndNextArea.setVisibility(View.GONE);
        errorTipsVg.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
        // 加载百分比重置
        remoteLoadingBufferTv.setText("0%");
        touchLoadingBufferTv.setText("0%");

        controlArea.setVisibility(View.VISIBLE);
        mControlDisplaySec = 0;

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            captureBtn.setVisibility(View.GONE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
        } else {
            captureBtn.setVisibility(View.VISIBLE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            captureBtn.setEnabled(false);
            videoRecordingBtn.setEnabled(false);
        }

        loadingPlayBtn.setVisibility(View.GONE);
    }

    // 新的播放UI初始化
    private void newPlayUIInit() {
        remotePlayBackArea.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);
        surfaceView.setVisibility(View.VISIBLE);
        loadingImgView.setVisibility(View.VISIBLE);
        loadingPbLayout.setVisibility(View.VISIBLE);
        touchProgressLayout.setVisibility(View.GONE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        exitBtn.setVisibility(View.GONE);
        replayAndNextArea.setVisibility(View.GONE);
        errorTipsVg.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
        // 加载百分比重置
        remoteLoadingBufferTv.setText("0%");
        touchLoadingBufferTv.setText("0%");

        controlArea.setVisibility(View.VISIBLE);
        mControlDisplaySec = 0;

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            captureBtn.setVisibility(View.GONE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            mControlBarRL.setVisibility(View.VISIBLE);
        } else {
            captureBtn.setVisibility(View.VISIBLE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            captureBtn.setEnabled(false);
            videoRecordingBtn.setEnabled(false);
            mControlBarRL.setVisibility(View.GONE);
        }

        loadingPlayBtn.setVisibility(View.GONE);
        mPlaybackRateBtn.setText("1x");
    }

    private void newPlayInit(boolean resetPause, boolean resetProgress) {
        if (mShowNetworkTip) {
            mShowNetworkTip = false;
        }

        initEZPlayer();
        newPlayUIInit();

        if (resetPause) {
            resetPauseBtnUI();
        }
        if (resetProgress) {
            progressBar.setProgress(0);
            progressSeekbar.setProgress(0);
        }
        if (localInfo.isSoundOpen()) {
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
        } else {
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
        }
    }

    private void seekInit(boolean resetPause, boolean resetProgress) {
        newSeekPlayUIInit();

        if (resetPause) {
            resetPauseBtnUI();
        }
        if (resetProgress) {
            progressBar.setProgress(0);
            progressSeekbar.setProgress(0);
        }
        if (localInfo.isSoundOpen()) {
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
        } else {
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
        }
    }


    // 重置暂停按钮 UI和状态值
    private void resetPauseBtnUI() {
        notPause = true;
        pauseBtn.setBackgroundResource(R.drawable.ez_remote_list_pause_btn_selector);
    }

    @Override
    public void onHikMoreClickListener(boolean isExpand) {
        if (isExpand) {
            if (mCloudRecordsAdapter != null && mCloudRecordsAdapter.getLocalFileEx() != null) {
                mCloudRecordsAdapter.addLocalFileExAll();
                mCloudRecordsAdapter.notifyDataSetChanged();
                int position = mCloudRecordsAdapter.getCloudFileEx().size() - 1;
                if (getAndroidOSVersion() < 14) {
                    pinnedHeaderListView.setSelection(position > 0 ? position : 0);
                } else {
                    pinnedHeaderListView.smoothScrollToPositionFromTop(position > 0 ? position : 0, 100, 500);
                }
            } else {
                // 当云视频文件不超过100000个不会出现异常，超过即异常
                int cloudTotal = 100000;
                hasShowListViewLine(false);
                mWaitDlg.show();
                stopQueryTask();
                queryDeviceRecordFilesAsyncTask = new QueryDeviceRecordFilesAsyncTask(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(),
                        EZPlayBackListActivity.this);
                queryDeviceRecordFilesAsyncTask.setQueryDate(queryDate);
                queryDeviceRecordFilesAsyncTask.setOnlyHasLocal(true);
                queryDeviceRecordFilesAsyncTask.execute(String.valueOf(cloudTotal));
            }
        } else {
            if (mCloudRecordsAdapter != null) {
                mCloudRecordsAdapter.minusLocalFileExAll();
            }
        }

    }


    // 暂停按钮实现停止
    private void pauseStop() {
        status = RemoteListContant.STATUS_STOP;
        stopRemoteListPlayer();
        loadingImgView.setVisibility(View.GONE);
        loadingPbLayout.setVisibility(View.GONE);

        loadingPlayBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mPlaybackPlayer != null) {
            mPlaybackPlayer.setSurfaceHold(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mPlaybackPlayer != null) {
            mPlaybackPlayer.setSurfaceHold(null);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;
        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void onOrientationChanged() {
        showDownLoad();
        setRemoteListSvLayout();
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // 显示状态栏
            fullScreen(false);
            if (status != RemoteListContant.STATUS_PLAYING) {
                // 不允许选择屏幕
                mScreenOrientationHelper.disableSensorOrientation();
            }
            // 竖屏处理
            remoteListPage.setBackgroundColor(getResources().getColor(R.color.white));
            mTitleBar.setVisibility(View.VISIBLE);
            pinnedHeaderListView.setVisibility(View.VISIBLE);
            if (controlArea.getVisibility() == View.VISIBLE) {
                exitBtn.setVisibility(View.VISIBLE);
                captureBtn.setVisibility(View.GONE);
                videoRecordingBtn.setVisibility(View.VISIBLE);
            }
            mControlBarRL.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            // 横屏处理
            // 隐藏状态栏
            fullScreen(true);
            remoteListPage.setBackgroundColor(getResources().getColor(R.color.black_bg));
            mTitleBar.setVisibility(View.GONE);
            pinnedHeaderListView.setVisibility(View.GONE);
            exitBtn.setVisibility(View.GONE);
            captureBtn.setVisibility(View.VISIBLE);
            videoRecordingBtn.setVisibility(View.VISIBLE);
            mControlBarRL.setVisibility(View.GONE);
            mLandscapeTitleBar.setVisibility(View.VISIBLE);
        }
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    // 是否显示下载图标
    private void showDownLoad() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT ) {
            downLayout.setVisibility(View.VISIBLE);
        } else {
            downLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.query_exception_ly:
                startQueryCloudRecordFiles();
                break;
            case R.id.cancel_auto_play_btn:
                autoLayout.setVisibility(View.GONE);
                break;
            case R.id.loading_play_btn:
                notPause = true;
                pauseBtn.setBackgroundResource(R.drawable.remote_list_pause_btn_selector);
                pausePlay();
                break;
            case R.id.error_replay_btn:
            case R.id.replay_btn:
                onReplayBtnClick();
                break;
            case R.id.next_play_btn:
                onNextPlayBtnClick();
                break;
            case R.id.remote_playback_pause_btn:
                onPlayPauseBtnClick();
                break;
            case R.id.remote_playback_sound_btn:
                onSoundBtnClick();
                break;
            case R.id.remote_playback_capture_btn:
                onCapturePicBtnClick();
                break;
            case R.id.remote_playback_video_recording_btn:
                onRecordBtnClick();
                break;
            case R.id.exit_btn:
                onPlayExitBtnOnClick();
                break;
            case R.id.control_area:
                break;
            case R.id.delete_playback:
                if (sectionAdapter != null && sectionAdapter.getSelectedCloudFiles().size() < 1) {
                } else {
                    showDelDialog();
                }
                break;
            default:
                break;
        }
    }

    private void showDelDialog() { }

    private void onNextPlayBtnClick() {}

    // 暂停按钮事件处理
    private void onPlayPauseBtnClick() {
        if (notPause) {
            // 暂停播放
            notPause = false;
            pauseBtn.setBackgroundResource(R.drawable.remote_list_play_btn_selector);
            if (status != RemoteListContant.STATUS_PLAYING) {
                pauseStop();
            } else {
                status = RemoteListContant.STATUS_PAUSE;
                if (mPlaybackPlayer != null) {
                    // 停止录像
                    stopRemotePlayBackRecord();
                    mPlaybackPlayer.pausePlayback();
                }
            }
        } else {
            notPause = true;
            pauseBtn.setBackgroundResource(R.drawable.ez_remote_list_pause_btn_selector);
            if (status != RemoteListContant.STATUS_PAUSE) {
                pausePlay();
            } else {
                if (mPlaybackPlayer != null) {
                    mPlaybackPlayer.resumePlayback();
                }
                mScreenOrientationHelper.enableSensorOrientation();
                status = RemoteListContant.STATUS_PLAYING;
            }
        }
    }

    // 重播当前录像片段
    private void onReplayBtnClick() {
        newPlayInit(true, true);
        timeBucketUIInit(currentClickItemFile.getBeginTime(), currentClickItemFile.getEndTime());
    }

    // 开始录像
    private void onRecordBtnClick() {
        mControlDisplaySec = 0;
        if (isRecording) {
            stopRemotePlayBackRecord();
            isRecording = !isRecording;
            return;
        }

        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            showToast(R.string.remoteplayback_SDCard_disable_use);
            return;
        }

        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // 提示内存不足
            showToast(R.string.remoteplayback_record_fail_for_memory);
            return;
        }

        if (mPlaybackPlayer != null) {
            String strRecordFile = DemoConfig.getRecordsFolder() +  "/" + System.currentTimeMillis()+ ".mp4";
            LogUtil.i(TAG, "current record path is " + strRecordFile);
            mPlaybackPlayer.setStreamDownloadCallback(new EZOpenSDKListener.EZStreamDownloadCallbackEx() {
                @Override
                public void onSuccess(String filepath) {
                    LogUtil.i(TAG, "EZStreamDownloadCallback onSuccess " + filepath);
                }

                @Override
                public void onError(EZOpenSDKListener.EZStreamDownloadError code) {
                    LogUtil.e(TAG, "EZStreamDownloadCallback onError = " + code);
                }

                @Override
                public void onErrorCode(int code) {
                    LogUtil.e(TAG, "EZStreamDownloadCallback onErrorCode = " + code);
                }
            });
            if (mPlaybackPlayer.startLocalRecordWithFile(strRecordFile)){
                isRecording = true;
                mCurrentRecordPath = strRecordFile;
                updateCaptureUI();
                mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
            }else{
                toast("failed to start record!");
            }
        }

    }

    // 抓拍按钮响应函数
    private void onCapturePicBtnClick() {
        mControlDisplaySec = 0;
        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            showToast(R.string.remoteplayback_SDCard_disable_use);
            return;
        }
        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // 提示内存不足
            showToast(R.string.remoteplayback_capture_fail_for_memory);
            return;
        }
        Thread thr = new Thread() {
            @Override
            public void run() {
                if (mPlaybackPlayer == null) {
                    return;
                }
                String serial = !TextUtils.isEmpty(mCameraInfo.getDeviceSerial()) ? mCameraInfo.getDeviceSerial() : "123456789";
                Bitmap bmp = mPlaybackPlayer.capturePicture();
                if (bmp != null) {
                    try {
                        mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);

                        // 可以采用deviceSerial+时间作为文件命名，demo中简化，只用时间命名
                        Date date = new Date();
                        String path = Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/CapturePicture/" + String.format("%tY", date)
                                + String.format("%tm", date) + String.format("%td", date) + "/"
                                + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) + ".jpg";

                        if (TextUtils.isEmpty(path)) {
                            bmp.recycle();
                            bmp = null;
                            return;
                        }
                        EZUtils.saveCapturePictrue(path, bmp);

                        MediaScanner mMediaScanner = new MediaScanner(EZPlayBackListActivity.this);
                        mMediaScanner.scanFile(path, "jpg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EZPlayBackListActivity.this, getResources().getString(R.string.already_saved_to_volume), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (InnerException e) {
                        e.printStackTrace();
                    } finally {
                        if (bmp != null) {
                            bmp.recycle();
                            bmp = null;
                            return;
                        }
                    }
                }
                super.run();
            }
        };
        thr.start();
    }

    // 声音按钮
    private void onSoundBtnClick() {
        if (mPlaybackPlayer == null) {
            return;
        }

        if (localInfo.isSoundOpen()) {
            // 关闭声音
            localInfo.setSoundOpen(false);
            mPlaybackPlayer.closeSound();
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
        } else {
            // 打开声音
            localInfo.setSoundOpen(true);
            mPlaybackPlayer.openSound();
            soundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
        }
    }

    private void pausePlay() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // 不允许选择屏幕
            mScreenOrientationHelper.disableSensorOrientation();
        }
        Calendar seekTime = getTimeBarSeekTime();
        Calendar osdTime = null;
        if (mPlaybackPlayer != null) {
            osdTime = mPlaybackPlayer.getOSDTime();
        }
        Calendar startTime = Calendar.getInstance();
        long playTime = 0L;
        if (osdTime != null) {
            playTime = osdTime.getTimeInMillis();
        } else {
            playTime = seekTime.getTimeInMillis();
        }
        startTime.setTimeInMillis(playTime);
        LogUtil.infoLog(TAG, "pausePlay:" + startTime);
        if (currentClickItemFile != null) {
            reConnectPlay(currentClickItemFile.getType(), startTime);
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.remote_playback_area:
                onPlayAreaTouched();
                break;
            case R.id.control_area:
                break;
            case R.id.query_exception_ly:
                startQueryCloudRecordFiles();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onSelectedChangeListener(int total) {
        if (total > 0) {
            deleteVideoText.setText(getString(R.string.delete) + "(" + total + ")");
        } else {
            deleteVideoText.setText(R.string.delete);
        }
    }

    @Override
    public void onDeleteCloudFileCompleteListener(boolean isLocal) {
        rightEditView.setVisibility(View.GONE);
        if (isLocal) {
            onHikMoreClickListener(true);
            sectionAdapter.setExpand(true);
        } else {
            pinnedHeaderListView.setVisibility(View.GONE);
            hasShowListViewLine(false);
            queryNoDataUIDisplay();
        }

    }

    @Override
    public void finish() {
        if (mCloudRecordsAdapter != null) {
            mCloudRecordsAdapter.clearData();
            mCloudRecordsAdapter.clear();
            mCloudRecordsAdapter.notifyDataSetChanged();
        }
        super.finish();
    }

    @Override
    public void onInputVerifyCode(final String verifyCode) {
        LogUtil.debugLog(TAG, "verify code is " + verifyCode);
        DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), verifyCode);
        if (mPlaybackPlayer != null) {

            newPlayUIInit();

            if (mDeviceRecordInfo != null) {
                if (mPlaybackPlayer != null) {
                    mPlaybackPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
                }

                startRecordOriginVideo();
                mPlaybackPlayer.startPlayback(mDeviceRecordInfo.getStartTime(), mDeviceRecordInfo.getStopTime());
            } else if (mCloudRecordInfo != null) {
                if (mPlaybackPlayer != null) {
                    mPlaybackPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
                }

                startRecordOriginVideo();
                mPlaybackPlayer.startPlayback(mCloudRecordInfo);
            }

        }
    }

    private void startRecordOriginVideo(){
        String fileName = LocalInfo.getInstance().getFilePath() + "/origin_video_play_back.ps";
        VideoFileUtil.startRecordOriginVideo(mPlaybackPlayer,fileName);
    }

    public void goToActiveCloudVideo(View view) {
        String errorInfo = "fail to call openCloudPage!";
        if (mCameraInfo == null){
            toast(errorInfo);
            return;
        }
        try {
            getOpenSDK().openCloudPage(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
        } catch (BaseException e) {
            toast(errorInfo);
            e.printStackTrace();
        }
    }

    // 用于防止重复点击
    private boolean isShowChangePlaybackRateWindow = false;

    public void onClickChangePlaybackSpeed(View view) {
        if (isShowChangePlaybackRateWindow){
            return;
        }
        PopupWindow popupWindow = new PopupWindow(mContext);
        ViewGroup popupVg = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.layout_change_playback_rate,
                (ViewGroup) getWindow().getDecorView(), false);
        popupWindow.setContentView(popupVg);
        popupWindow.getContentView().setTag(view);
        for (int i=0; i<popupVg.getChildCount(); i++){
            View childView = popupVg.getChildAt(i);
            if (childView instanceof  Button){
                Button changeRateBtn = (Button) childView;
                changeRateBtn.setOnClickListener(mChangePlaybackRateListener);
                changeRateBtn.setTag(popupWindow);
                if (view instanceof Button && ((Button) view).getText().toString().contains(((Button) childView).getText())){
                    childView.setVisibility(View.GONE);
                }
            }
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isShowChangePlaybackRateWindow = false;
            }
        });
        int widthMode = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMode = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        popupWindow.getContentView().measure(widthMode, heightMode);
        int yOffset = -(view.getHeight() + popupWindow.getContentView().getMeasuredHeight());
        popupWindow.showAsDropDown(view, 0, yOffset);
        isShowChangePlaybackRateWindow = true;
    }

    private OnClickListener mChangePlaybackRateListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
           PopupWindow popupWindow = null;
            if (v.getTag()!= null && v.getTag() instanceof PopupWindow){
                popupWindow = ((PopupWindow) v.getTag());
                popupWindow.dismiss();
            }
            if (v instanceof Button){
                String targetRateWithX = (String) ((Button) v).getText();
                String rate = targetRateWithX.replaceAll("x", "");
                rate = rate.replace("X", "");
                int rateInt = Integer.parseInt(rate);
                for (EZConstants.EZPlaybackRate rateEnum: EZConstants.EZPlaybackRate.values()){
                    if (rateInt == rateEnum.speed){
                        if (mPlaybackPlayer.setPlaybackRate(rateEnum)){
                            if (popupWindow != null && popupWindow.getContentView() != null && popupWindow.getContentView().getTag() instanceof  Button){
                                ((Button) popupWindow.getContentView().getTag()).setText(targetRateWithX);
                            }
                        }else{
                            toast("failed to change to " + targetRateWithX);
                        }
                    }
                }
            }
        }
    };
}
