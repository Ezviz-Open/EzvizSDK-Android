/**
 * 回放页面
 */
package com.videogo.ui.playback;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.global.GlobalKit;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.ui.playback.download.DownLoadTaskRecordAbstract;
import com.videogo.ui.playback.download.DownloadTaskRecordOfCloud;
import com.videogo.ui.playback.download.DownloadTaskRecordOfDevice;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZConstants.EZFecPlaceType;
import com.videogo.openapi.EZConstants.EZFecCorrectType;
import com.videogo.openapi.EZConstants.EZPlaybackConstants;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.EzvizAPI;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.remoteplayback.RecordCoverFetcherManager;
import com.videogo.ui.playback.bean.ClickedListItem;
import com.videogo.ui.playback.bean.CloudPartInfoFileEx;
import com.videogo.ui.playback.querylist.QueryCloudRecordFilesAsyncTask;
import com.videogo.ui.playback.querylist.QueryDeviceRecordFilesAsyncTask;
import com.videogo.ui.playback.querylist.QueryPlayBackListTaskCallback;
import com.videogo.ui.playback.querylist.QuerySDKCloudRecordFilesAsyncTask;
import com.videogo.ui.playback.querylist.SectionListAdapter;
import com.videogo.ui.playback.querylist.SectionListAdapter.OnHikItemClickListener;
import com.videogo.ui.playback.querylist.StandardArrayAdapter;
import com.videogo.ui.playback.querylist.StandardArrayAdapter.ArrayAdapterChangeListener;
import com.videogo.stream.EZCloudStreamDownload;
import com.videogo.stream.EZDeviceStreamDownload;
import com.videogo.ui.common.EZBusinessTool;
import com.videogo.ui.common.ScreenOrientationHelper;
import com.videogo.ui.realplay.fec.FecViewLayoutHelper;
import com.videogo.util.AppUtil;
import com.videogo.util.DataManager;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.EZUtils;
import com.videogo.util.VerifyCodeInput;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.PinnedHeaderListView;
import com.videogo.widget.TitleBar;
import com.videogo.widget.loading.LoadingTextView;
import com.videogo.widget.toprightmenu.EZMenuItem;
import com.videogo.widget.toprightmenu.EZTopRightMenu;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;

import com.videogo.global.DemoConfig;

import ezviz.ezopensdkcommon.common.RootActivity;

import static com.videogo.EzvizApplication.getOpenSDK;

@SuppressLint({"DefaultLocale", "HandlerLeak", "NewApi", "NonConstantResourceId"})
public class EZPlayBackListActivity extends RootActivity implements QueryPlayBackListTaskCallback,
        OnHikItemClickListener, TextureView.SurfaceTextureListener, ArrayAdapterChangeListener,
        VerifyCodeInput.VerifyCodeInputListener {

    // region --- 全局变量|global variable ---

    private static final String TAG = EZPlayBackListActivity.class.getSimpleName();// TAG

    private LocalInfo localInfo = null;// 本地信息
    private EZPlayer mPlaybackPlayer = null;
    private Date queryDate = null;// 查询时间

    // 云存储ListView & 适配器
    private PinnedHeaderListView mPinnedHeaderListViewForCloud;
    private StandardArrayAdapter mCloudRecordsAdapter;
    private SectionListAdapter mSectionAdapterForCloud;
    // 云录制ListView & 适配器
    private PinnedHeaderListView mPinnedHeaderListViewForSDKCloud;
    private StandardArrayAdapter mSDKCloudRecordsAdapter;
    private SectionListAdapter mSectionAdapterForSDKCloud;
    // SD卡录像ListView & 适配器
    private PinnedHeaderListView mPinnedHeaderListViewForLocal;
    private StandardArrayAdapter mDeviceRecordsAdapter;
    private SectionListAdapter mSectionAdapterForLocal;

    // 列表查询task(云存储)
    private QueryCloudRecordFilesAsyncTask queryCloudRecordFilesAsyncTask;
    // 列表查询task(云录制)
    private QuerySDKCloudRecordFilesAsyncTask querySDKCloudRecordFilesAsyncTask;
    // 列表查询task(本地)
    private QueryDeviceRecordFilesAsyncTask queryDeviceRecordFilesAsyncTask;

    private TitleBar mTitleBar;// 标题
    private TitleBar mLandscapeTitleBar = null;// 横屏标题
    private Button backBtn;
    private Button rightButton;
    private ViewGroup remoteListPage = null;// 整个回放页面Layout
    // 播放区域
    private RelativeLayout remotePlayBackArea;
    private TextureView mTextureView = null;
    private TextureView mSecondTextureView = null;
    private TextView streamTypeTv;// 取流方式
    // 播放加载进程UI
    private LinearLayout loadingPbLayout;
    // 播放区域错误信息
    private ViewGroup errorTipsVg;// 错误信息显示父视图
    private ImageButton errorReplay;// 错误重播按钮
    private TextView errorInfoTV;// 错误信息
    // 矫正模式分屏
    private RelativeLayout playBackPtzRL;
    private SurfaceView mPlayBackSv1;
    private SurfaceView mPlayBackSv2;
    private SurfaceView mPlayBackSv3;
    private SurfaceView mPlayBackSv4;
    private SurfaceView mPlayBackSv5;
    private SurfaceView mPlayBackSv6;

    // 播放进度条区域 + 播放工具栏区域
    private LinearLayout controlArea = null;
    // 播放进度条区域
    private LinearLayout progressArea = null;// 播放进度条区域
    private TextView playTimeTV = null;// 播放进度条左侧，录像已播放时长
    private TextView duringTimeTV = null;// 播放进度条右侧，录像总时长
    private SeekBar progressSeekbar = null;// 播放进度条
    // 播放工具栏区域
    private ViewGroup mControlBarRL;// 工具栏父视图
    private ImageButton pauseBtn = null;// 暂停/播放按钮
    private ImageButton soundBtn = null;// 声音开关按钮
    private LinearLayout downloadBtn = null;// 下载按钮
    private Button playbackRateBtn = null;// 倍数设置按钮
    private CheckTextButton mFullscreenButton;// 全屏按钮

    // 云存储广告图片
    private ImageView mCloudVideoActiveImg;
    // 录像列表区域，没有录像时需要展示的组件
    private LoadingTextView recordLoadingBar;// 录像列表加载进度圈
    private LinearLayout mNovideoImgCloud;// 没有云存储数据
    private LinearLayout mNovideoImgSDKCloud;// 没有云录制数据
    private LinearLayout mNoVideoImgLocal;// 没有SD卡数据

    private FrameLayout mTabContentMainFrame;// 所有录像列表父区域
    private RelativeLayout mContentTabCloudRl;// 云存储录像列表显示区域
    private RelativeLayout mContentTabSDKCloudRl;// 云录制录像列表显示区域
    private RelativeLayout mContentTabDeviceRl;// SD卡录像列表显示区域
    // 云存储、SDK云录制、SD卡录像 Tab按钮
    private CheckTextButton mCheckBtnCloud;
    private CheckTextButton mCheckBtnSDKCloud;
    private CheckTextButton mCheckBtnDevice;
    private int mRecordType;// 录像类型
    // 右上角录像类型选择按钮
    private EZTopRightMenu mTopRightMenu;
    private List<EZMenuItem> menuItems;
    private EZConstants.EZVideoRecordType recordType = EZConstants.EZVideoRecordType.EZ_VIDEO_RECORD_TYPE_ALL;
    // 录像item点击、下载相关全局变量
    private ClickedListItem currentClickItemFile;// 当前被点击的item
    private EZDeviceRecordFile mDeviceRecordInfo = null;// item点击时记录录像的时间用，下载的时候需要时间
    private EZCloudRecordFile mCloudRecordInfo = null;// item点击时记录录像的时间用，下载的时候需要时间
    public static ArrayList<DownLoadTaskRecordAbstract> mDownloadTaskRecordListAbstract = new ArrayList<>();// 下载任务列表
    private String downloadFilePath;// 录像下载路径
    private long recordInfoDuration;// 下载的录像时长
    // 鱼眼矫正模式相关
    private PopupWindow mFecPopupWindow;// 鱼眼矫正模式pop
    private Button[] fecCorrectTypeButtons;// 鱼眼设备矫正模式按钮数组
    private EZFecPlaceType fecPlaceType;// 鱼眼安装模式
    private EZFecCorrectType fecCorrectType;// 鱼眼矫正模式
    private FecViewLayoutHelper fecViewLayoutHelper;// 鱼眼辅助类
    // 状态
    private Rect mRemotePlayBackRect = null;
    private float mRealRatio = Constant.LIVE_VIEW_RATIO;// 播放分辨率
    private int status = RemoteListContant.STATUS_INIT;// 播放状态
    private boolean notPause = true;// 是否暂停播放，默认为没有暂停
    private boolean isDateSelected = false;// 是否为选择日期事件
    private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;// 屏幕方向
    // 其他
    private ScreenOrientationHelper mScreenOrientationHelper;
    private boolean isFromPermissionSetting;// true为应用权限管理返回
    private Timer mUpdateTimer = null;// 播放定时器，每秒刷新
    private TimerTask mUpdateTimerTask = null;// 定时器执行的任务
    // 以下为设备列表页面通过Bundle传递的数据
    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;
    // 以上为设备列表页面通过Bundle传递的数据
    private boolean isMultiChannelDevice;// 是否是双目设备，如果接入的设备没有双目设备，可以忽略相关逻辑

    // endregion

    // region --- 页面启动方法|Page startup method ---

    public static void launch(Context context, EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        Intent intent = new Intent(context, EZPlayBackListActivity.class);
        intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
        context.startActivity(intent);
    }

    public static void launch(Context context, String deviceSerial, int cameraNo) {
        EZCameraInfo cameraInfo = new EZCameraInfo();
        cameraInfo.setDeviceSerial(deviceSerial);
        cameraInfo.setCameraNo(cameraNo);
        launch(context, new EZDeviceInfo(), cameraInfo);
    }

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_playback_list_page);
        // 保持屏幕常亮 Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initData();// 数据初始化
        initView();// UI初始化
        initEZPlayer();// 播放器初始化
        initListener();// 设置监听
        startQueryCloudRecordFiles();
        // 国内支持SD卡录像封面获取，海外不支持
        if (!EzvizAPI.getInstance().isUsingGlobalSDK()) {
            // 与设备建立链接，获取SD卡录像封面（页面退出的时候必须断开链接，释放资源，见onDestroy方法）
            RecordCoverFetcherManager.getInstance().initFetcher(this, mCameraInfo.getDeviceSerial(),
                    mCameraInfo.getCameraNo(), recordCoverFetcherInitCallBack);
        }
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如下代码
        initFecView();
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如上代码
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume()");
        if (isFromPermissionSetting) {
            checkPermissions();
            isFromPermissionSetting = false;
        }
        new Handler().postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mTextureView.getWindowToken(), 0);
        }, 200);
        // 判断是否处理暂停状态
        if (notPause || status == RemoteListContant.STATUS_DECRYPT) {
            onActivityResume();
            isDateSelected = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mScreenOrientationHelper.postOnStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScreenOrientationHelper.postOnStop();
        LogUtil.d(TAG, "onStop():" + notPause + " status:" + status);
        if (notPause) {
            closePlayBack();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closePlayBack();
        if (mPlaybackPlayer != null) {
            getOpenSDK().releasePlayer(mPlaybackPlayer);
        }
        if (!EzvizAPI.getInstance().isUsingGlobalSDK()) {
            RecordCoverFetcherManager.getInstance().stopFetcher();// 断开与设备的链接
        }
        stopQueryTask();
        removeHandler(playBackHandler);
        stopAllDownloadTasks();
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        onExitCurrentPageClick();
        finish();
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
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;
        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    // endregion

    // region --- 播放器消息回调|Player message callback ---

    private Handler playBackHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtil.i(TAG, "handleMessage:" + msg.what);
            switch (msg.what) {
                // 以下消息为SDK发送的消息，用于取流相关消息回调
                case EZPlaybackConstants.MSG_REMOTE_PLAYBACK_PLAY_PREPARED:// 播放准备
                    // 如果开始回放时需立即开启倍数，须在此消息回调中调用setPlaybackRate，不要在MSG_REMOTEPLAYBACK_PLAY_START消息回调中设置。
                    // 因为setPlaybackRate设置后也会有一个MSG_REMOTEPLAYBACK_PLAY_START消息通知，在MSG_REMOTEPLAYBACK_PLAY_START消息回调中设置会陷入循环
//                    mPlaybackPlayer.setPlaybackRate(EZConstants.EZPlaybackRate.EZ_PLAYBACK_RATE_4);
                    break;
                case EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_START:// 录像回放开始|seek成功
                    handlePlaybackStart(msg);
                    break;
                case EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_FINISH:// 录像回放完成，回放完成后回调
                    handlePlaybackFinish();
                    break;
                case EZPlaybackConstants.MSG_REMOTE_PLAYBACK_RATE_LOWER:// 回放倍速降低消息
                    showToast("回放倍速降速通知");
                    // TODO 4倍速以上则直接降速到4倍速，4倍速及其以下则直接降速到1倍速
                    break;
                case EZPlaybackConstants.MSG_REMOTEPLAYBACK_PLAY_FAIL:// 录像回放失败
                    ErrorInfo errorInfo = (ErrorInfo) msg.obj;
                    handlePlaybackFail(errorInfo);
                    break;
                // 以上消息为SDK发送的消息，用于取流相关消息回调
                // 以下消息为该页面发送的消息，用于UI更新
                case RemoteListContant.MSG_REMOTELIST_UI_UPDATE:// 回放进度 UI刷新
                    playBoxToolRefresh();
                    break;
                // 以上消息为该页面发送的消息，用于UI更新
                default:
                    break;
            }
        }

    };

    /**
     * 播放失败处理
     *
     * @param errorInfo
     */
    private void handlePlaybackFail(ErrorInfo errorInfo) {
        LogUtil.d(TAG, "handlePlaybackFail. Playback failed. error info is " + errorInfo.toString());
        status = RemoteListContant.STATUS_STOP;
        mPlaybackPlayer.stopPlayback();
        switch (errorInfo.errorCode) {
            // 收到这两个错误码，弹出对话框，让用户输入密码后，重新取流
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:// 需要验证码
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {// 验证码错误
                showPlayEventTip("");
                VerifyCodeInput.VerifyCodeInputDialog(this, this).show();
            }
            break;
            default: {// 显示错误码信息
                String errorInfoText = getErrorTip(R.string.remoteplayback_fail, errorInfo.errorCode);
                showPlayEventTip(errorInfoText);
                showToast(errorInfo.toString());
            }
            break;
        }
    }

    /**
     * 录像回放开始，更新UI
     */
    private void handlePlaybackStart(Message msg) {
        if (msg.arg1 != 0) {
            mRealRatio = (float) msg.arg2 / msg.arg1;// 计算画面比例
        }
        status = RemoteListContant.STATUS_PLAYING;// 记录播放状态：播放中
        loadingPbLayout.setVisibility(View.GONE);// 加载进度隐藏
        progressArea.setVisibility(View.VISIBLE);
        mControlBarRL.setVisibility(View.VISIBLE);
        errorTipsVg.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
        setRemoteListSvLayout();
        mScreenOrientationHelper.enableSensorOrientation();// 横竖屏感应
        // 播放器声音开启或关闭
        if (mPlaybackPlayer != null) {
            if (localInfo.isSoundOpen()) {// 打开声音
                mPlaybackPlayer.openSound();
            } else {// 关闭声音
                mPlaybackPlayer.closeSound();
            }
        }
        startUpdateTimer();
        showStreamFetchType();
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如下代码
        if (fecViewLayoutHelper != null) {
            fecViewLayoutHelper.openFecCorrect(fecCorrectType, fecPlaceType);
        }
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如上代码
    }

    /**
     * 执行播放完毕的操作：如停止player、更新界面
     * execute some operations when play has finished. such as stop player, update UI
     */
    private void handlePlaybackFinish() {
        LogUtil.d(TAG, "handlePlaybackFinish");
        stopUpdateTimer();
        mPlaybackPlayer.stopPlayback();
        status = RemoteListContant.STATUS_STOP;
        progressArea.setVisibility(View.INVISIBLE);// 播放完毕隐藏进度条（当前片段播放时长、进度条、总时长）
        mControlBarRL.setVisibility(View.INVISIBLE);// 播放完毕隐藏工具栏视图（播放/暂停、声音开关、下载、倍数、全屏）
        showPlayEventTip(getString(R.string.tip_playback_again));// 展示再次播放功能按钮
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如下代码
        if (fecViewLayoutHelper != null) {
            fecViewLayoutHelper.resetFecType();
            closeFecViewModePopupWindow();
        }
        /// 鱼眼设备专用设置，如果没有鱼眼设备，不需要如上代码
    }

    /**
     * 回放进度 UI每秒刷新
     * Playback progress UI refreshes every second
     */
    private void playBoxToolRefresh() {
        // 已播放时长 & 播放进度更新
        if (mPlaybackPlayer != null && status == RemoteListContant.STATUS_PLAYING) {
            Calendar osdTime = mPlaybackPlayer.getOSDTime();
            if (osdTime != null) {
                long osd = osdTime.getTimeInMillis();
                long begin = currentClickItemFile.getBeginTime();
                long end = currentClickItemFile.getEndTime();
                double x = ((osd - begin) * RemoteListContant.PROGRESS_MAX_VALUE) / (double) (end - begin);
                int progress = (int) x;
                progressSeekbar.setProgress(progress);

                LogUtil.i(TAG,
                        "handlePlayProgress, begin time:" + DateTimeUtil.formatTimeToYMDHMSString(begin) +
                                " endtime:" + DateTimeUtil.formatTimeToYMDHMSString(end) +
                                " osdTime:" + DateTimeUtil.formatTimeToYMDHMSString(osd) +
                                " progress:" + progress + " maxProgress:" + RemoteListContant.PROGRESS_MAX_VALUE);
                if (osd >= begin && osd <= end) {
                    int beginTimeClock = (int) ((osd - begin) / 1000);
                    playTimeTV.setText(RemoteListUtil.convToUIDuration(beginTimeClock));
                }
            }
        }
    }

    // endregion

    // region --- Click点击事件|Click event ---

    /**
     * 播放|暂停Click
     */
    private OnClickListener mPlayButtonClickListener = v -> {
        if (mPlaybackPlayer == null) {
            showToast(getString(R.string.please_operate_after_select_any_record));
            return;
        }
        if (notPause) {
            // 暂停播放
            notPause = false;
            pauseBtn.setBackgroundResource(R.drawable.remote_list_play_btn_selector);
            if (status == RemoteListContant.STATUS_PLAYING) {
                status = RemoteListContant.STATUS_PAUSE;
                if (mPlaybackPlayer != null) {
                    // 加保护，规避CAS库小概率出现的10S死锁导致的ANR问题
                    getTaskManager().submit(() -> mPlaybackPlayer.pausePlayback());
                }
            }
        } else {
            notPause = true;
            pauseBtn.setBackgroundResource(R.drawable.ez_remote_list_pause_btn_selector);
            if (status == RemoteListContant.STATUS_PAUSE) {
                if (mPlaybackPlayer != null) {
                    // 加保护，规避CAS库小概率出现的10S死锁导致的ANR问题
                    getTaskManager().submit(() -> mPlaybackPlayer.resumePlayback());
                }
                mScreenOrientationHelper.enableSensorOrientation();
                status = RemoteListContant.STATUS_PLAYING;
            }
        }
    };

    /**
     * 开启/关闭声音Click
     */
    private OnClickListener mSoundButtonClickListener = v-> {
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
    };

    /**
     * 录像下载Click
     */
    private OnClickListener mDownloadButtonClickListener = v -> {
        if (mCloudRecordInfo != null) {
            recordInfoDuration = RemoteListUtil.converToSecondDuration(mCloudRecordInfo.getStartTime(),
                    mCloudRecordInfo.getStopTime());
            startDownloadCloudVideo(mCloudRecordInfo);
        } else {
            recordInfoDuration = RemoteListUtil.converToSecondDuration(mDeviceRecordInfo.getStartTime(),
                    mDeviceRecordInfo.getStopTime());
            startDownloadDeviceVideo(mDeviceRecordInfo);
        }
    };

    // 用于防止重复点击
    private boolean isShowChangePlaybackRateWindow = false;

    /**
     * 倍数设置按钮Click，弹出倍数设置选项框
     */
    public void onPlaybackRateClick(View view) {
        if (isShowChangePlaybackRateWindow) {
            return;
        }
        PopupWindow popupWindow = new PopupWindow(mContext);
        ViewGroup popupVg = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.layout_change_playback_rate,
                (ViewGroup) getWindow().getDecorView(), false);
        popupWindow.setContentView(popupVg);
        popupWindow.getContentView().setTag(view);
        for (int i = 0; i < popupVg.getChildCount(); i++) {
            View childView = popupVg.getChildAt(i);
            if (childView instanceof Button) {
                Button changeRateBtn = (Button) childView;
                changeRateBtn.setOnClickListener(mChangePlaybackRateListener);
                changeRateBtn.setTag(popupWindow);

                String selectRateText = ((Button) view).getText().toString();
                String childRateText = ((Button) childView).getText().toString();
                if (selectRateText.contains(childRateText)) {
                    childView.setVisibility(View.GONE);
                }
                // SD卡回放不支持32倍速
                if (mCheckBtnDevice.isChecked() && childRateText.contains("32x")) {
                    childView.setVisibility(View.GONE);
                }
            }
        }
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOnDismissListener(() -> isShowChangePlaybackRateWindow = false);
        int widthMode = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMode = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        popupWindow.getContentView().measure(widthMode, heightMode);
        int yOffset = -(view.getHeight() + popupWindow.getContentView().getMeasuredHeight());
        popupWindow.showAsDropDown(view, 0, yOffset);
        isShowChangePlaybackRateWindow = true;
    }

    /**
     * 回放倍数选项Click
     */
    private final OnClickListener mChangePlaybackRateListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupWindow popupWindow = null;
            if (v.getTag() != null && v.getTag() instanceof PopupWindow) {
                popupWindow = ((PopupWindow) v.getTag());
                popupWindow.dismiss();
            }
            if (v instanceof Button) {
                String targetRateWithX = (String) ((Button) v).getText();
                String rate = targetRateWithX.replaceAll("(?i)x", "");
                double rateDouble = Double.parseDouble(rate);
                EZConstants.EZPlaybackRate targetRateEnum = null;
                // 寻找对应的枚举值
                for (EZConstants.EZPlaybackRate rateEnum : EZConstants.EZPlaybackRate.values()) {
                    if (rateDouble == rateEnum.speed) {
                        targetRateEnum = rateEnum;
                        break;
                    }
                }
                if (mCheckBtnDevice.isChecked()) {
                    if (mPlaybackPlayer.getStreamFetchType() == 2 && !EZBusinessTool.isSupportDirectInnerRelaySpeed(mDeviceInfo, mCameraInfo)) {
                        showToast(R.string.device_directinner_playbackrate_not_support);
                    } else if (!EZBusinessTool.isSupportPlaybackRate(mDeviceInfo, mCameraInfo)) {
                        showToast(R.string.device_playbackrate_not_support);
                    } else {
                        // 切换到指定倍速
                        setPlaybackRate(popupWindow, targetRateEnum, targetRateWithX);
//                        lastDevicePlaybackRate = targetRateEnum;
                    }
                } else {
                    setPlaybackRate(popupWindow, targetRateEnum, targetRateWithX);
                }
            }
        }
    };

    private void setPlaybackRate(PopupWindow popupWindow, EZConstants.EZPlaybackRate targetRateEnum,
                                 String targetRateWithX) {
        // 切换到指定倍速，倍数设置为耗时任务，需要在子线程中执行
        new Thread(() -> {
            if (mPlaybackPlayer.setPlaybackRate(targetRateEnum)) {
                runOnUiThread(() -> {
                    if (popupWindow != null && popupWindow.getContentView() != null && popupWindow.getContentView().getTag() instanceof Button) {
                        ((Button) popupWindow.getContentView().getTag()).setText(targetRateWithX);
                    }
                });
            } else {
                showToast("failed to change to " + targetRateWithX);
            }
        }).start();
    }

    /**
     * 云存储Tab 点击事件监听
     */
    private final OnClickListener mCloudCheckListener = v -> {
        mRecordType = RemoteListContant.TYPE_CLOUD;
        if (!mCheckBtnCloud.isChecked()) {
            resetRecordCheckBtns();
            mCheckBtnCloud.setChecked(true);
            mContentTabCloudRl.setVisibility(View.VISIBLE);
            downloadBtn.setVisibility(View.VISIBLE);
            rightButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_title_extension_selector));
            rightButton.setVisibility(EzvizAPI.getInstance().isUsingGlobalSDK() ? View.VISIBLE : View.GONE);
            startQueryCloudRecordFiles();
        }
    };

    /**
     * SDK云录制Tab 点击事件监听
     */
    private final OnClickListener mSDKCloudCheckListener = v -> {
        mRecordType = RemoteListContant.TYPE_SDKCLOUD;
        if (!mCheckBtnSDKCloud.isChecked()) {
            resetRecordCheckBtns();
            mCheckBtnSDKCloud.setChecked(true);
            mContentTabSDKCloudRl.setVisibility(View.VISIBLE);
            downloadBtn.setVisibility(View.GONE);
            rightButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_title_extension_selector));
            rightButton.setVisibility(View.GONE);
            startQuerySDKCloudRecordFiles();
        }
    };

    /**
     * SD卡录像Tab 点击事件监听
     */
    private final OnClickListener mDeviceCheckListener = v -> {
        mRecordType = RemoteListContant.TYPE_LOCAL;
        if (!mCheckBtnDevice.isChecked()) {
            resetRecordCheckBtns();
            mCheckBtnDevice.setChecked(true);
            mContentTabDeviceRl.setVisibility(View.VISIBLE);
            downloadBtn.setVisibility(EZBusinessTool.isSupportSDRecordDownload(mDeviceInfo, mCameraInfo) ? View.VISIBLE : View.GONE);
            rightButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_title_recordtype_selector));
            rightButton.setVisibility(View.VISIBLE);
            startQueryDeviceRecordFiles();
        }
    };

    /**
     * 重播当前录像片段Click
     */
    private final OnClickListener mReplayButtonClickListener = v -> {
        newPlayInit();
        setPlaybackProgressUI(currentClickItemFile.getBeginTime(), currentClickItemFile.getEndTime());
        startPlaybackByRecordCategory();
    };

    /**
     * 云存储图片Click
     */
    public OnClickListener mCloudAdImageViewClickListener = v -> {
        String errorInfo = "fail to call openCloudPage!";
        if (mCameraInfo == null) {
            showToast(errorInfo);
            return;
        }
        try {
            getOpenSDK().openCloudPage(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
        } catch (BaseException e) {
            showToast(errorInfo);
            e.printStackTrace();
        }
    };

    /**
     * 左上角返回Click
     */
    private void onExitCurrentPageClick() {
        notPause = true;
        stopQueryTask();
        closePlayBack();
    }

    /**
     * 右上角录像类型按钮Click
     */
    private final OnClickListener mTopRightMenuClickListener = v -> {
        if (mCheckBtnCloud.isChecked()) {
            if (EzvizAPI.getInstance().isUsingGlobalSDK()) {
                EZAbroadCloudServiceExAct.launch(EZPlayBackListActivity.this, mCameraInfo.getDeviceSerial(),
                        mCameraInfo.getCameraNo());
            }
        } else {
            if (menuItems == null) {
                menuItems = new ArrayList<>();
                menuItems.add(new EZMenuItem(R.mipmap.videogo_icon, "全部"));
                menuItems.add(new EZMenuItem(R.mipmap.videogo_icon, "定时"));
                menuItems.add(new EZMenuItem(R.mipmap.videogo_icon, "事件"));
            }
            if (mTopRightMenu == null) {
                mTopRightMenu = new EZTopRightMenu(EZPlayBackListActivity.this);
                mTopRightMenu.setWidth(450)         // 默认宽度wrap_content
                        .showIcon(false)            // 显示菜单图标，默认为true
                        .dimBackground(true)        // 背景变暗，默认为true
                        .needAnimationStyle(true)   // 显示动画，默认为true
                        .setAnimationStyle(R.style.TRM_ANIM_STYLE) // 默认为R.style.TRM_ANIM_STYLE
                        .addMenuList(menuItems).setOnMenuItemClickListener(new EZTopRightMenu.OnMenuItemClickListener() {
                            @Override
                            public void onMenuItemClick(int position) {
                                if (position == 1) {
                                    recordType = EZConstants.EZVideoRecordType.EZ_VIDEO_RECORD_TYPE_CMR;
                                } else if (position == 2) {
                                    recordType = EZConstants.EZVideoRecordType.EZ_VIDEO_RECORD_TYPE_Event;
                                } else {
                                    recordType = EZConstants.EZVideoRecordType.EZ_VIDEO_RECORD_TYPE_ALL;
                                }
                                startQueryDeviceRecordFiles();
                            }
                        });
            }
            mTopRightMenu.showAsDropDown(rightButton, -300, 10);
        }
    };

    /**
     * 鱼眼矫正模式的点击事件
     */
    private final OnClickListener mOnFecWndClickListener = v -> {
        // 没有在播放，拦截
        if (status != RemoteListContant.STATUS_PLAYING) {
            return;
        }
        int id = v.getId();
        if (id == R.id.fec_place_wall || id == R.id.fec_place_floor || id == R.id.fec_place_ceiling) {
            // 壁装/底装/顶装
            fecPlaceType = EZFecPlaceType.values()[Integer.parseInt(String.valueOf(v.getTag()))];
            setFecCorrectTypeBtnsEnable(fecPlaceType);
        } else if (id == R.id.fec_correct_fish || id == R.id.fec_correct_4ptz || id == R.id.fec_correct_5ptz ||
                id == R.id.fec_correct_full5ptz || id == R.id.fec_correct_lat || id == R.id.fec_correct_arc_hor ||
                id == R.id.fec_correct_arc_ver || id == R.id.fec_correct_wide_angle || id == R.id.fec_correct_180 ||
                id == R.id.fec_correct_360 || id == R.id.fec_correct_cyc) {
            // 默认鱼眼/4分屏/5分屏/全景5分屏/维度拉伸/ARC/ARCV/广角/180°全景/360°全景/柱状
            fecCorrectType = FecViewLayoutHelper.getFecCorrectTypeFromTag(Integer.parseInt(String.valueOf(v.getTag())));
            fecViewLayoutHelper.openFecCorrect(fecCorrectType, fecPlaceType);
        } else if (id == R.id.fec_close_btn) {
            closeFecViewModePopupWindow();
        }
    };

    // endregion

    // region --- 顶部日历点击事件|Top Calendar Click Event ---

    // 切换到日历界面
    private OnClickListener goToCalendarClickListener = v -> {
        if (EZBusinessTool.getMinDate() != null && new Date().before(EZBusinessTool.getMinDate())) {
            showToast(R.string.calendar_setting_error);
            return;
        }
        showDatePicker();
    };

    private void showDatePicker() {
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(queryDate);
        DatePickerDialog dpd = new DatePickerDialog(this, null, nowCalendar.get(Calendar.YEAR),
                nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        dpd.setCancelable(true);
        dpd.setTitle(R.string.select_date);
        dpd.setCanceledOnTouchOutside(true);
        dpd.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.certain), (dg, which) -> {
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
                isDateSelected = true;
                queryDate = selectCalendar.getTime();
                onDateChanged();
            }
        });
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> {
            LogUtil.d("Picker", "Cancel!");
            if (!isFinishing()) {
                dialog.dismiss();
            }

        });
        dpd.show();
    }

    private void onDateChanged() {
        if (queryDate != null) {
            mTitleBar.setTitle(RemoteListUtil.converToMonthAndDay(queryDate));
        }
        switch (mRecordType) {
            case RemoteListContant.TYPE_CLOUD:
                mSDKCloudRecordsAdapter = null;
                mDeviceRecordsAdapter = null;
                startQueryCloudRecordFiles();
                break;
            case RemoteListContant.TYPE_SDKCLOUD:
                mCloudRecordsAdapter = null;
                mDeviceRecordsAdapter = null;
                startQuerySDKCloudRecordFiles();
                break;
            case RemoteListContant.TYPE_LOCAL:
                mCloudRecordsAdapter = null;
                mCloudRecordsAdapter = null;
                startQueryDeviceRecordFiles();
                break;
            default:
                // do nothing
                break;
        }
    }

    // endregion

    // region --- 回放进度条拖动事件|Replay progress bar drag event ---

    private final OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
        /**
         * 拖动条停止拖动的时候调用
         */
        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            int progress = arg0.getProgress();
            if (progress == RemoteListContant.PROGRESS_MAX_VALUE) {// 拖动到最后
                handlePlaybackFinish();
                return;
            }
            if (currentClickItemFile != null) {
                // 计算拖动后的时间
                long beginTime = currentClickItemFile.getBeginTime();
                long endTime = currentClickItemFile.getEndTime();
                long avg = (endTime - beginTime) / RemoteListContant.PROGRESS_MAX_VALUE;
                long trackTime = beginTime + (progress * avg);
                LogUtil.i(TAG,
                        "onSeekBarStopTracking, begin time:" + beginTime +
                                " endtime:" + endTime +
                                " avg:" + avg +
                                " MAX:" + RemoteListContant.PROGRESS_MAX_VALUE +
                                " tracktime:" + trackTime);
                stopUpdateTimer();
                if (mPlaybackPlayer != null) {
                    Calendar seekTime = Calendar.getInstance();
                    seekTime.setTime(new Date(trackTime));
                    mPlaybackPlayer.seekPlayback(seekTime);
                }
                seekInit();
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
                playTimeTV.setText(convToUIDuration);
            }
        }
    };

    // endregion

    // region --- 录像下载|video download ---

    /**
     * 云存储录像下载 | download video from ezviz cloud
     *
     * @param cloudFile video file of ezviz cloud
     */
    private void startDownloadCloudVideo(final EZCloudRecordFile cloudFile) {
        if (cloudFile == null) {
            return;
        }
        getTaskManager().submit(() -> {
            // 设置下载路径并创建文件
            String strFileNameWithPath = DemoConfig.getRecordsFolder() + "/cloud_" + System.currentTimeMillis() + ".mp4";
            final File file = new File(strFileNameWithPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // 创建下载器
            final EZCloudStreamDownload ezCloudStreamDownloader = new EZCloudStreamDownload(strFileNameWithPath, cloudFile);
            // 云存储录像支持下载进度回调，SD卡录像下载不支持
            ezCloudStreamDownloader.setStreamDownloadCallback(new EZStreamDownloadCallback(cloudFile));
            // 设置下载验证码（录像未加密的话可以不设置）
            ezCloudStreamDownloader.setSecretKey(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
            // 如果是双目设备，需要设置
            if (isMultiChannelDevice) {
                ezCloudStreamDownloader.setMultiChannelDevice(true);
            }
            // 开始下载
            ezCloudStreamDownloader.start();
            // 记录本次下载任务，onDestory时需要暂停下载任务
            mDownloadTaskRecordListAbstract.add(new DownloadTaskRecordOfCloud(ezCloudStreamDownloader));

            showToast(R.string.download_start);
        });
    }

    /**
     * 设备SD卡录像下载 | download video from ezviz device
     *
     * @param deviceFile video file of device SdCard
     */
    private void startDownloadDeviceVideo(final EZDeviceRecordFile deviceFile) {
        if (deviceFile == null) {
            return;
        }
        getTaskManager().submit(() -> {
            // 设置下载路径并创建文件
            String strRecordFilePath = DemoConfig.getRecordsFolder() + "/device_" + System.currentTimeMillis() + ".mp4";
            File file = new File(strRecordFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // 创建下载器
            final EZDeviceStreamDownload ezDeviceStreamDownloader = new EZDeviceStreamDownload(strRecordFilePath,
                    mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), deviceFile);
            // 国内平台 && 使用小权限token模式的时候，需要设置小权限token
            if (!EzvizAPI.getInstance().isUsingGlobalSDK() && EZOpenSDK.isEnableSDKWithTKToken()) {
                ezDeviceStreamDownloader.setStreamToken(GlobalKit.getInstance().playbackStreamToken);
            }
            // 设置下载回调监听
            ezDeviceStreamDownloader.setStreamDownloadCallback(new EZStreamDownloadCallback());
            // 设置下载验证码
            ezDeviceStreamDownloader.setSecretKey(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
            // 如果是双目设备，需要设置
            if (isMultiChannelDevice) {
                ezDeviceStreamDownloader.setMultiChannelDevice(true);
            }
            // 开始下载
            ezDeviceStreamDownloader.start();
            // 记录本次下载任务，onDestory时需要暂停下载任务
            mDownloadTaskRecordListAbstract.add(new DownloadTaskRecordOfDevice(ezDeviceStreamDownloader));

            showToast(R.string.download_start);
        });
    }

    /**
     * 下载回调监听
     */
    private class EZStreamDownloadCallback extends EZOpenSDKListener.EZStreamDownloadCallbackEx {

        private EZCloudRecordFile cloudFile;

        public EZStreamDownloadCallback() {
        }

        public EZStreamDownloadCallback(EZCloudRecordFile cloudFile) {
            this.cloudFile = cloudFile;
        }

        @Override
        public void onDownloadingSize(long downloadSize) {// 只有云存储录像支持下载进度回调，SD卡录像下载不支持
            if (cloudFile != null) {
                LogUtil.d(TAG, "percent--->" + downloadSize * 100 / cloudFile.getFileSize());
            }
        }

        @Override
        public void onSuccess(final String filepath) {
            downloadFilePath = filepath;
            String successMsg = "saved video to " + filepath;
            LogUtil.d(TAG, successMsg);
            showToast(successMsg);
            // 申请动态权限将下载文件存储到相册
            checkAndRequestPermission();
        }

        @Override
        public void onError(final EZOpenSDKListener.EZStreamDownloadError code) {
            String failMsg = "failed: ";
            switch (code) {
                case ERROR_EZSTREAM_DOWNLOAD_MAX_CONNECTIONS:
                    failMsg += "device reached max connections!";
                    break;
                case ERROR_EZSTREAM_DOWNLOAD_VERIFYCODE:
                    failMsg += "The video has been encrypted, please enter the password!";
                    break;
                case ERROR_EZSTREAM_DOWNLOAD_START:
                    failMsg += "download parameter error or download startup failure, please check the log";
                    /**
                     * 下载启动失败可能有两个原因：
                     * 1、下载参数设置错误，请检查下载器参数设置是否错误
                     * 2、媒体类错误码，日志中搜索 handleDownloadError: 找到具体的错误码，再查阅错误码文档
                     *
                     * There may be two reasons for download startup failure:
                     * 1.Download parameter settings are incorrect. Please check if the downloader parameter settings
                     * are incorrect
                     * 2.Media related error codes, search for handleDownloadError:  in the log: find the specific
                     * error code,
                     *   and then refer to the error code document
                     */
                    break;
                default:
                    failMsg += code;
                    break;
            }
            showToast(failMsg);
        }

        @Override
        public void onErrorCode(int code) {
            showToast(R.string.download_fail, code);
        }

    }

    // endregion

    // region --- 验证码校验|CAPTCHA Verification ---

    @Override
    public void onInputVerifyCode(final String verifyCode) {
        LogUtil.d(TAG, "verify code is " + verifyCode);
        DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), verifyCode);
        if (mPlaybackPlayer != null) {
            resetPlayUIVisible();
            startPlaybackByRecordCategory();
        }
    }

    // endregion

    // region --- 录像查询|Video inquiry ---

    /**
     * 录像列表最左侧的时间戳是否显示
     *
     * @param isShow
     */
    private void hasShowListViewLine(boolean isShow) {
        findViewById(R.id.listview_line).setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    private void stopQueryTask() {
        if (queryCloudRecordFilesAsyncTask != null) {
            queryCloudRecordFilesAsyncTask.cancel(true);
            queryCloudRecordFilesAsyncTask.setAbort(true);
            queryCloudRecordFilesAsyncTask = null;
        }
        if (querySDKCloudRecordFilesAsyncTask != null) {
            querySDKCloudRecordFilesAsyncTask.cancel(true);
            querySDKCloudRecordFilesAsyncTask.setAbort(true);
            querySDKCloudRecordFilesAsyncTask = null;
        }
        if (queryDeviceRecordFilesAsyncTask != null) {
            queryDeviceRecordFilesAsyncTask.cancel(true);
            queryDeviceRecordFilesAsyncTask.setAbort(true);
            queryDeviceRecordFilesAsyncTask = null;
        }
    }

    private void startQueryCloudRecordFiles() {
        if (queryDate != null) {
            mTitleBar.setTitle(RemoteListUtil.converToMonthAndDay(queryDate));
        }
        mPinnedHeaderListViewForCloud.setVisibility(View.GONE);
        stopQueryTask();
        mCloudRecordsAdapter = null;
        mSectionAdapterForCloud = null;
        hasShowListViewLine(false);
        queryCloudRecordFilesAsyncTask = new QueryCloudRecordFilesAsyncTask(mCameraInfo.getDeviceSerial(),
                mCameraInfo.getCameraNo(), EZPlayBackListActivity.this);
        recordLoadingBar.setVisibility(View.VISIBLE);
        showTab(R.id.record_loadingTextView);
        queryCloudRecordFilesAsyncTask.setSearchDate(queryDate);
        queryCloudRecordFilesAsyncTask.execute();
    }

    private void startQuerySDKCloudRecordFiles() {
        mPinnedHeaderListViewForSDKCloud.setVisibility(View.GONE);
        stopQueryTask();
        mSDKCloudRecordsAdapter = null;
        mSectionAdapterForSDKCloud = null;
        hasShowListViewLine(false);
        querySDKCloudRecordFilesAsyncTask = new QuerySDKCloudRecordFilesAsyncTask(mCameraInfo.getDeviceSerial(),
                mCameraInfo.getCameraNo(), EZPlayBackListActivity.this);
        recordLoadingBar.setVisibility(View.VISIBLE);
        showTab(R.id.record_loadingTextView);
        querySDKCloudRecordFilesAsyncTask.setSearchDate(queryDate);
        querySDKCloudRecordFilesAsyncTask.execute();
    }

    private void startQueryDeviceRecordFiles() {
        int cloudTotal = 100000;
        mPinnedHeaderListViewForLocal.setVisibility(View.GONE);
        stopQueryTask();
        hasShowListViewLine(false);
        queryDeviceRecordFilesAsyncTask = new QueryDeviceRecordFilesAsyncTask(mCameraInfo.getDeviceSerial(),
                mCameraInfo.getCameraNo(), recordType, EZPlayBackListActivity.this);
        recordLoadingBar.setVisibility(View.VISIBLE);
        showTab(R.id.record_loadingTextView);
        queryDeviceRecordFilesAsyncTask.setQueryDate(queryDate);
        queryDeviceRecordFilesAsyncTask.execute(String.valueOf(cloudTotal));
    }

    @Override
    public void queryHasNoData(int type) {
        if (type == RemoteListContant.TYPE_CLOUD) {
            showTab(R.id.novideo_img_cloud);
        } else if (type == RemoteListContant.TYPE_SDKCLOUD) {
            showTab(R.id.novideo_img_sdkcloud);
        } else if (type == RemoteListContant.TYPE_LOCAL) {
            showTab(R.id.novideo_img_device);
        }
    }

    @Override
    public void querySuccessFromCloud(List<CloudPartInfoFileEx> cloudPartInfoFileExs,
                                      List<CloudPartInfoFile> cloudPartInfoFile) {
        findViewById(R.id.display_layout).setVisibility(View.VISIBLE);
        hasShowListViewLine(true);
        recordLoadingBar.setVisibility(View.GONE);
        mPinnedHeaderListViewForCloud.setVisibility(View.VISIBLE);
        showTab(R.id.content_tab_cloud_root);
        mCloudRecordsAdapter = new StandardArrayAdapter(this, R.id.layout, cloudPartInfoFileExs);
        mCloudRecordsAdapter.setAdapterChangeListener(this);
        mSectionAdapterForCloud = new SectionListAdapter(EZPlayBackListActivity.this, getLayoutInflater(),
                mCloudRecordsAdapter, mCameraInfo.getDeviceSerial());
        mPinnedHeaderListViewForCloud.setAdapter(mSectionAdapterForCloud);

        mPinnedHeaderListViewForCloud.setOnScrollListener(mSectionAdapterForCloud);
        mPinnedHeaderListViewForCloud.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.list_section,
                mPinnedHeaderListViewForCloud, false));
        mPinnedHeaderListViewForCloud.startAnimation();
        mSectionAdapterForCloud.setOnHikItemClickListener(EZPlayBackListActivity.this);

        doPlayback(cloudPartInfoFile, RemoteListContant.TYPE_CLOUD);
    }

    @Override
    public void querySuccessFromSDKCloud(List<CloudPartInfoFileEx> cloudPartInfoFileExs,
                                         List<CloudPartInfoFile> cloudPartInfoFile) {
        hasShowListViewLine(true);
        recordLoadingBar.setVisibility(View.GONE);
        mPinnedHeaderListViewForSDKCloud.setVisibility(View.VISIBLE);
        showTab(R.id.content_tab_sdkcloud_root);
        mSDKCloudRecordsAdapter = new StandardArrayAdapter(this, R.id.layout, cloudPartInfoFileExs);
        mSDKCloudRecordsAdapter.setAdapterChangeListener(this);
        mSectionAdapterForSDKCloud = new SectionListAdapter(EZPlayBackListActivity.this, getLayoutInflater(),
                mSDKCloudRecordsAdapter, mCameraInfo.getDeviceSerial());
        mPinnedHeaderListViewForSDKCloud.setAdapter(mSectionAdapterForSDKCloud);

        mPinnedHeaderListViewForSDKCloud.setOnScrollListener(mSectionAdapterForSDKCloud);
        mPinnedHeaderListViewForSDKCloud.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.list_section,
                mPinnedHeaderListViewForSDKCloud, false));
        mPinnedHeaderListViewForSDKCloud.startAnimation();
        mSectionAdapterForSDKCloud.setOnHikItemClickListener(EZPlayBackListActivity.this);

        doPlayback(cloudPartInfoFile, RemoteListContant.TYPE_SDKCLOUD);
    }

    @Override
    public void querySuccessFromDevice(List<CloudPartInfoFileEx> cloudPartInfoFileExs, int position,
                                       List<CloudPartInfoFile> cloudPartInfoFile) {
        hasShowListViewLine(true);
        recordLoadingBar.setVisibility(View.GONE);
        showTab(R.id.content_tab_device_root);
        mPinnedHeaderListViewForLocal.setVisibility(View.VISIBLE);

        if (mDeviceRecordsAdapter != null) {
            mDeviceRecordsAdapter.clear();
            mDeviceRecordsAdapter.addLocalFileExAll(cloudPartInfoFileExs);
            mDeviceRecordsAdapter.notifyDataSetChanged();
            int selPosition = mDeviceRecordsAdapter.getCloudFileEx().size() - 2;
            if (AppUtil.getAndroidOSVersion() < 14) {
                mPinnedHeaderListViewForLocal.setSelection(selPosition > 0 ? selPosition : 0);
            } else {
                mPinnedHeaderListViewForLocal.smoothScrollToPositionFromTop(selPosition > 0 ? selPosition : 0, 100, 500);
            }
        } else {
            mDeviceRecordsAdapter = new StandardArrayAdapter(this, R.id.layout, cloudPartInfoFileExs);
            mDeviceRecordsAdapter.setAdapterChangeListener(this);
            mSectionAdapterForLocal = new SectionListAdapter(EZPlayBackListActivity.this, getLayoutInflater(),
                    mDeviceRecordsAdapter, mCameraInfo.getDeviceSerial());
            mPinnedHeaderListViewForLocal.setAdapter(mSectionAdapterForLocal);
            mPinnedHeaderListViewForLocal.setOnScrollListener(mSectionAdapterForLocal);
            mPinnedHeaderListViewForLocal.setPinnedHeaderView(getLayoutInflater().inflate(R.layout.list_section,
                    mPinnedHeaderListViewForLocal, false));
            mPinnedHeaderListViewForLocal.startAnimation();
            mSectionAdapterForLocal.setOnHikItemClickListener(EZPlayBackListActivity.this);
        }
        doPlayback(cloudPartInfoFile, RemoteListContant.TYPE_LOCAL);
        if (!EzvizAPI.getInstance().isUsingGlobalSDK() && EZBusinessTool.isSupportSdCover(mDeviceInfo, mCameraInfo)) {// 国内 & 支持SD卡录像封面
            // 去获取SD卡视频封面
            List<EZDeviceRecordFile> recordFiles = new ArrayList<>();
            for (int i = 0; i < cloudPartInfoFile.size(); i++) {
                CloudPartInfoFile file = cloudPartInfoFile.get(i);
                EZDeviceRecordFile recordFile = new EZDeviceRecordFile();
                recordFile.setBegin(file.getBegin());
                recordFile.setEnd(file.getEnd());
                recordFile.setSeq(i);// 设置索引，封面回调的时候知道对应哪一个录像
                recordFiles.add(recordFile);
            }
            RecordCoverFetcherManager.getInstance().requestRecordCover(recordFiles,
                    new RecordCoverFetcherManager.RecordCoverFetcherCallBack() {

                        @Override
                        public void onGetCoverSuccess(int seq, byte[] bytes) {
                            /**
                             * 注意：图片是设备一张一张传回来的，接收到一张就需要局部刷新UI。
                             */
                            // 以下情况做拦截，否则会将SD卡录像封面显示在云存储录像上或者数组越界崩溃
                            if (mCheckBtnCloud.isChecked() || seq > cloudPartInfoFile.size() - 1) {
                                return;
                            }
                            // 此处将bytes转为bitmap。开发者也可自行将bytes转为文件，进行缓存管理。
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            // TODO 局部刷新UI

                            // 将获取到的图片赋值给列表中的对象
                            CloudPartInfoFile cloudFile = cloudPartInfoFile.get(seq);
                            cloudFile.setBitmap(bitmap);
                            for (int i = 0; i < cloudPartInfoFileExs.size(); i++) {
                                CloudPartInfoFileEx cloudFileEx = cloudPartInfoFileExs.get(i);
                                if (cloudFileEx.getDataOne() != null && cloudFile.getBegin().equals(cloudFileEx.getDataOne().getBegin())
                                        && cloudFile.getEnd().equals(cloudFileEx.getDataOne().getEnd())) {
                                    cloudFileEx.getDataOne().setBitmap(bitmap);
                                    break;
                                } else if (cloudFileEx.getDataTwo() != null && cloudFile.getBegin().equals(cloudFileEx.getDataTwo().getBegin())
                                        && cloudFile.getEnd().equals(cloudFileEx.getDataTwo().getEnd())) {
                                    cloudFileEx.getDataTwo().setBitmap(bitmap);
                                    break;
                                } else if (cloudFileEx.getDataThree() != null && cloudFile.getBegin().equals(cloudFileEx.getDataThree().getBegin())
                                        && cloudFile.getEnd().equals(cloudFileEx.getDataThree().getEnd())) {
                                    cloudFileEx.getDataThree().setBitmap(bitmap);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onGetCoverFailed(int errorCode) {
                            LogUtil.e(TAG, "onGetCoverFailed");
                        }
                    });
            /**
             * 本demo中使用的是ListView，无法局部刷新，只能延时5秒去刷新UI做个演示。建议使用RecycleView来实现
             */
            new Handler().postDelayed(() -> mDeviceRecordsAdapter.notifyDataSetChanged(), 5 * 1000);  //延迟5秒执行
        }
    }

    private void doPlayback(List<CloudPartInfoFile> cloudPartInfoFile, int playbackType) {
        if (cloudPartInfoFile != null && !cloudPartInfoFile.isEmpty()) {
            CloudPartInfoFile file = cloudPartInfoFile.get(0);
            ClickedListItem clickedListItem = new ClickedListItem(0, playbackType, file.getStartMillis(),
                    file.getEndMillis(), 0);
            onHikItemClickListener(cloudPartInfoFile.get(0), clickedListItem);
        }
    }

    // endregion

    // region --- SD卡录像封面监听|SD card recording cover monitoring ---

    private RecordCoverFetcherManager.RecordCoverFetcherInitCallBack recordCoverFetcherInitCallBack =
            new RecordCoverFetcherManager.RecordCoverFetcherInitCallBack() {
                @Override
                public void onFetcherInitSuccess() {

                }

                @Override
                public void onFetcherInitFailed() {

                }
            };

    // endregion

    // region --- OnHikItemClickListener 录像片段点击|OnHikItemClickListener video clip click ---

    @Override
    public void onHikItemClickListener(CloudPartInfoFile cloudFile, ClickedListItem playClickItem) {
        stopUpdateTimer();
        mPlaybackPlayer.stopPlayback();
        newPlayInit();
        setPlaybackProgressUI(playClickItem.getBeginTime(), playClickItem.getEndTime());

        currentClickItemFile = playClickItem;
        mDeviceRecordInfo = null;
        mCloudRecordInfo = null;
        if (mRecordType == RemoteListContant.TYPE_LOCAL) {
            mDeviceRecordInfo = new EZDeviceRecordFile();
            EZBusinessTool.convertCloudPartInfoFile2EZDeviceRecordFile(mDeviceRecordInfo, cloudFile);
            mSectionAdapterForLocal.setSelection(cloudFile.getPosition());
            if (AppUtil.getAndroidOSVersion() < 14) {
                mPinnedHeaderListViewForLocal.setSelection(playClickItem.getPosition());
            } else {
                mPinnedHeaderListViewForLocal.smoothScrollToPositionFromTop(playClickItem.getPosition(), 100, 500);
            }
        } else if (mRecordType == RemoteListContant.TYPE_CLOUD) {
            mCloudRecordInfo = new EZCloudRecordFile();
            EZBusinessTool.convertCloudPartInfoFile2EZCloudRecordFile(mCloudRecordInfo, cloudFile);
            mSectionAdapterForCloud.setSelection(cloudFile.getPosition());
            if (AppUtil.getAndroidOSVersion() < 14) {
                mPinnedHeaderListViewForCloud.setSelection(playClickItem.getPosition());
            } else {
                mPinnedHeaderListViewForCloud.smoothScrollToPositionFromTop(playClickItem.getPosition(), 100, 500);
            }
        } else if (mRecordType == RemoteListContant.TYPE_SDKCLOUD) {
            mCloudRecordInfo = new EZCloudRecordFile();
            EZBusinessTool.convertCloudPartInfoFile2EZCloudRecordFile(mCloudRecordInfo, cloudFile);
            mSectionAdapterForSDKCloud.setSelection(cloudFile.getPosition());
            if (AppUtil.getAndroidOSVersion() < 14) {
                mPinnedHeaderListViewForSDKCloud.setSelection(playClickItem.getPosition());
            } else {
                mPinnedHeaderListViewForSDKCloud.smoothScrollToPositionFromTop(playClickItem.getPosition(), 100, 500);
            }
        }
        startPlaybackByRecordCategory();
    }

    @Override
    public void onHikMoreClickListener(boolean isExpand) {
        if (isExpand) {
            if (mCloudRecordsAdapter != null && mCloudRecordsAdapter.getLocalFileEx() != null) {
                mCloudRecordsAdapter.addLocalFileExAll();
                mCloudRecordsAdapter.notifyDataSetChanged();
                int position = mCloudRecordsAdapter.getCloudFileEx().size() - 1;
                if (AppUtil.getAndroidOSVersion() < 14) {
                    mPinnedHeaderListViewForCloud.setSelection(position > 0 ? position : 0);
                } else {
                    mPinnedHeaderListViewForCloud.smoothScrollToPositionFromTop(position > 0 ? position : 0, 100, 500);
                }
            } else {
                // 当云视频文件不超过100000个不会出现异常，超过即异常
                int cloudTotal = 100000;
                hasShowListViewLine(false);
                stopQueryTask();
                queryDeviceRecordFilesAsyncTask = new QueryDeviceRecordFilesAsyncTask(mCameraInfo.getDeviceSerial(),
                        mCameraInfo.getCameraNo(), recordType, EZPlayBackListActivity.this);
                queryDeviceRecordFilesAsyncTask.setQueryDate(queryDate);
                queryDeviceRecordFilesAsyncTask.execute(String.valueOf(cloudTotal));
            }
        } else {
            if (mCloudRecordsAdapter != null) {
                mCloudRecordsAdapter.minusLocalFileExAll();
            }
        }
    }

    @Override
    public void onSelectedChangeListener(int total) {
    }

    @Override
    public void onDeleteCloudFileCompleteListener(boolean isLocal) {

    }

    // endregion

    // region --- SurfaceTexture生命周期|SurfaceTexture Lifecycle ---

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mPlaybackPlayer != null) {
            if (mTextureView.getSurfaceTexture() == surface) {
                mPlaybackPlayer.setSurfaceEx(surface);
            } else if (mSecondTextureView.getSurfaceTexture() == surface) {
                mPlaybackPlayer.setSurfaceEx(surface, 1);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mPlaybackPlayer != null) {
            if (mTextureView.getSurfaceTexture() == surface) {
                mPlaybackPlayer.setSurfaceEx(null);
            } else if (mSecondTextureView.getSurfaceTexture() == surface) {
                mPlaybackPlayer.setSurfaceEx(null, 1);
            }
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    // endregion

    // region --- 自定义方法|custom method ---

    private void initEZPlayer() {
        // 1.创建播放器
        mPlaybackPlayer = getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
        // 2.配置播放器：设置回调，设置验证码，设置播放视图等配置
        mPlaybackPlayer.setHandler(playBackHandler);
        mPlaybackPlayer.setSurfaceEx(mTextureView.getSurfaceTexture());
        if (isMultiChannelDevice) {// 双目设备
            mPlaybackPlayer.setSurfaceEx(mSecondTextureView.getSurfaceTexture(), 1);
        }
        if (mDeviceInfo.getIsEncrypt() == 1) {
            mPlaybackPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
        }
    }

    /**
     * 根据录像类别开始回放
     * 调用EZPlayer.startPlayback接口开始回放（重播或验证码输入后调用）
     * call EZPlayer.startPlayback to start playback
     */
    private void startPlaybackByRecordCategory() {
        if (mPlaybackPlayer == null) {
            return;
        }
        // 国内 & 小权限token模式下，设置自己服务器生成的tkToken
        if (!EzvizAPI.getInstance().isUsingGlobalSDK() && EZOpenSDK.isEnableSDKWithTKToken()) {
            mPlaybackPlayer.setStreamToken(GlobalKit.getInstance().playbackStreamToken);
        }
        if (mDeviceRecordInfo != null) {
            mPlaybackPlayer.startPlayback(mDeviceRecordInfo);
        } else if (mCloudRecordInfo != null) {
            mPlaybackPlayer.startPlayback(mCloudRecordInfo);
        }
    }

    private void closePlayBack() {
        if (status == RemoteListContant.STATUS_EXIT_PAGE) {
            return;
        }
        LogUtil.d(TAG, "停止运行.........");
        mPlaybackPlayer.stopPlayback();
        stopUpdateTimer();
        status = RemoteListContant.STATUS_EXIT_PAGE;
    }

    /**
     * 根据安装模式和能力集设置哪些矫正模式可用
     */
    private void setFecCorrectTypeBtnsEnable(EZFecPlaceType fecPlaceType) {
        int supportValue = FecViewLayoutHelper.getSupportInt(fecPlaceType, mDeviceInfo);
        FecViewLayoutHelper.setFecCorrectButtonsState(fecCorrectTypeButtons, supportValue);
    }

    // 取消所有下载任务
    private void stopAllDownloadTasks() {
        for (DownLoadTaskRecordAbstract downloadRecord : mDownloadTaskRecordListAbstract) {
            downloadRecord.stopDownloader();
        }
    }

    // 开启定时器，回放进度 UI 每秒刷新
    private void startUpdateTimer() {
        stopUpdateTimer();
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                sendMessage(RemoteListContant.MSG_REMOTELIST_UI_UPDATE, 0, 0);
            }
        };
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    // 停止定时器
    private void stopUpdateTimer() {
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
            if (status == RemoteListContant.STATUS_EXIT_PAGE || status == RemoteListContant.STATUS_DECRYPT) {
                newPlayInit();
                setPlaybackProgressUI(currentClickItemFile.getBeginTime(), currentClickItemFile.getEndTime());
                startPlaybackByRecordCategory();
            }
        }
    }

    /**
     * 数据初始化
     */
    private void initData() {
        localInfo = LocalInfo.getInstance();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            queryDate = (Date) bundle.getSerializable(RemoteListContant.QUERY_DATE_INTENT_KEY);
            mCameraInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
            mDeviceInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_DEVICE_INFO);
            isMultiChannelDevice = EZBusinessTool.isSupportMultiChannel(mDeviceInfo, mCameraInfo);
            if (isMultiChannelDevice) {// 双目设备调整画面比例
                mRealRatio = Constant.LIVE_MULTI_VIEW_RATIO;
            }
        }
        Application application = getApplication();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        localInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        localInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
        if (mCameraInfo == null) {
            LogUtil.d(TAG, "cameraInfo is null");
            finish();
        }
    }

    // endregion

    // region --- UI ---

    public void initView() {
        mContentTabCloudRl = (RelativeLayout) findViewById(R.id.content_tab_cloud_root);
        mContentTabSDKCloudRl = (RelativeLayout) findViewById(R.id.content_tab_sdkcloud_root);
        mContentTabDeviceRl = (RelativeLayout) findViewById(R.id.content_tab_device_root);
        mCloudVideoActiveImg = (ImageView) findViewById(R.id.img_active_cloud_video);
        mCheckBtnCloud = (CheckTextButton) findViewById(R.id.pb_search_tab_btn_cloud);
        mCheckBtnSDKCloud = (CheckTextButton) findViewById(R.id.pb_search_tab_btn_sdkcloud);
        mCheckBtnDevice = (CheckTextButton) findViewById(R.id.pb_search_tab_btn_device);
        mTabContentMainFrame = (FrameLayout) findViewById(R.id.ez_tab_content_frame);

        mCheckBtnCloud.setToggleEnable(false);
        mCheckBtnSDKCloud.setToggleEnable(false);
        mCheckBtnDevice.setToggleEnable(false);
        mCheckBtnCloud.setChecked(true);

        mPinnedHeaderListViewForCloud = (PinnedHeaderListView) findViewById(R.id.listView);
        mPinnedHeaderListViewForSDKCloud = (PinnedHeaderListView) findViewById(R.id.listView_sdkcloud);
        mPinnedHeaderListViewForLocal = (PinnedHeaderListView) findViewById(R.id.listView_device);
        remoteListPage = (ViewGroup) findViewById(R.id.remote_list_page);
        mTitleBar = (TitleBar) findViewById(R.id.title);
        /* 测量状态栏高度 **/
        ViewTreeObserver viewTreeObserver = remoteListPage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(() -> {
            if (mRemotePlayBackRect == null) {
                // 获取状况栏高度
                mRemotePlayBackRect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(mRemotePlayBackRect);
            }
        });
        mNovideoImgCloud = (LinearLayout) findViewById(R.id.novideo_img_cloud);
        mNovideoImgSDKCloud = (LinearLayout) findViewById(R.id.novideo_img_sdkcloud);
        mNoVideoImgLocal = (LinearLayout) findViewById(R.id.novideo_img_device);
        recordLoadingBar = (LoadingTextView) findViewById(R.id.record_loadingTextView);
        recordLoadingBar.setText(R.string.loading_text_default);
        remotePlayBackArea = (RelativeLayout) findViewById(R.id.remote_playback_area);
        duringTimeTV = (TextView) findViewById(R.id.during_time_tv);
        playBackPtzRL = findViewById(R.id.play_ptz_rl);
        mFullscreenButton = (CheckTextButton) findViewById(R.id.fullscreen_button);
        mScreenOrientationHelper = new ScreenOrientationHelper(this, mFullscreenButton);
        mTextureView = findViewById(R.id.remote_playback_wnd_sv);
        mTextureView.setSurfaceTextureListener(this);
        // 双目设备
        if (isMultiChannelDevice) {
            mSecondTextureView = findViewById(R.id.remote_playback_wnd_second_sv);
            mSecondTextureView.setSurfaceTextureListener(this);
            mSecondTextureView.setVisibility(View.VISIBLE);
            mFullscreenButton.setVisibility(View.INVISIBLE);
        }
        setRemoteListSvLayout();

        progressSeekbar = (SeekBar) findViewById(R.id.progress_seekbar);
        playTimeTV = (TextView) findViewById(R.id.play_time_tv);
        if (GlobalKit.isJCTest) {
            playTimeTV.setVisibility(View.VISIBLE);
        }
        controlArea = (LinearLayout) findViewById(R.id.control_area);
        progressArea = (LinearLayout) findViewById(R.id.progress_area);
        downloadBtn = (LinearLayout) findViewById(R.id.remote_playback_download_btn);
        playbackRateBtn = (Button) findViewById(R.id.btn_change_playback_rate);
        measure(downloadBtn);
        measure(controlArea);
        loadingPbLayout = (LinearLayout) findViewById(R.id.loading_pb_ly);

        errorInfoTV = (TextView) findViewById(R.id.error_info_tv);
        errorTipsVg = (ViewGroup) findViewById(R.id.vg_error_tips);
        errorReplay = (ImageButton) findViewById(R.id.error_replay_btn);
        pauseBtn = (ImageButton) findViewById(R.id.remote_playback_pause_btn);
        soundBtn = (ImageButton) findViewById(R.id.remote_playback_sound_btn);
        progressSeekbar.setMax(RemoteListContant.PROGRESS_MAX_VALUE);
        streamTypeTv = (TextView) findViewById(R.id.tv_stream_type);

        notPause = true;
        mControlBarRL = (ViewGroup) findViewById(R.id.control_bar);

        mLandscapeTitleBar = (TitleBar) findViewById(R.id.pb_title_bar_landscape);
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                null/*getResources().getDrawable(R.drawable.message_back_selector)*/);
        if (mCameraInfo != null) {
            mLandscapeTitleBar.setTitle(mCameraInfo.getCameraName());
        }
        mLandscapeTitleBar.addBackButton(v -> onBackPressed());
    }

    /**
     * 鱼眼设备专用设置
     */
    private void initFecView() {
        if (FecViewLayoutHelper.isFecDevice(mDeviceInfo)) {
            // 鱼眼设备显示"查看模式" & 调整画面比例为1:1
            mFullscreenButton.setVisibility(View.GONE);// 鱼眼设备不支持全屏，隐藏全屏按钮
            mCloudVideoActiveImg.setVisibility(View.GONE);// 页面太挤，云存储开通图片不显示
            ImageButton viewTypeBtn = (ImageButton) findViewById(R.id.remote_playback_viewtype_btn);
            viewTypeBtn.setVisibility(View.VISIBLE);
            viewTypeBtn.setOnClickListener(v -> openFecViewModePopupWindow(controlArea));

            fecPlaceType = EZFecPlaceType.EZ_FEC_PLACE_CEILING;// demo中默认顶装
            fecCorrectType = EZFecCorrectType.EZ_FEC_CORRECT_FISH;// demo中默认鱼眼（原始码流）
            mRealRatio = 1;

            mPlayBackSv1 = (SurfaceView) findViewById(R.id.playback_sv1);
            mPlayBackSv2 = (SurfaceView) findViewById(R.id.playback_sv2);
            mPlayBackSv3 = (SurfaceView) findViewById(R.id.playback_sv3);
            mPlayBackSv4 = (SurfaceView) findViewById(R.id.playback_sv4);
            mPlayBackSv5 = (SurfaceView) findViewById(R.id.playback_sv5);
            mPlayBackSv6 = (SurfaceView) findViewById(R.id.playback_sv6);
            ViewGroup playWindowVg = (ViewGroup) findViewById(R.id.vg_play_window);

            // fecViewLayoutHelper.fecPopupWindow 需要延后设置，全局搜索查看
            fecViewLayoutHelper = new FecViewLayoutHelper(this);
            fecViewLayoutHelper.playerView = mTextureView;
            fecViewLayoutHelper.playWindowVg = playWindowVg;
            fecViewLayoutHelper.player = mPlaybackPlayer;
            fecViewLayoutHelper.playPtzRL = playBackPtzRL;
            fecViewLayoutHelper.setSurfaceViews(new SurfaceView[]{mPlayBackSv1, mPlayBackSv2, mPlayBackSv3,
                    mPlayBackSv4, mPlayBackSv5, mPlayBackSv6});
            playWindowVg.post(() -> fecViewLayoutHelper.setPlayViewAspectRadioWith1V1());
        }
    }

    private void initListener() {
        backBtn = mTitleBar.addBackButton(v -> {
            onExitCurrentPageClick();
            finish();
        });
        // 右上角录像类型按钮事件
        rightButton = mTitleBar.addRightButton(R.drawable.common_title_extension_selector, mTopRightMenuClickListener);
        rightButton.setVisibility(EzvizAPI.getInstance().isUsingGlobalSDK() ? View.VISIBLE : View.GONE);
        // 标题栏日历时间选择事件
        mTitleBar.addTitleButton(R.drawable.remote_cal_selector, goToCalendarClickListener);
        mTitleBar.setOnTitleClickListener(goToCalendarClickListener);
        mCloudVideoActiveImg.setOnClickListener(mCloudAdImageViewClickListener);// 云存储图片事件
        downloadBtn.setOnClickListener(mDownloadButtonClickListener);// 下载按钮事件
        playbackRateBtn.setOnClickListener(v -> onPlaybackRateClick(v));// 倍数回放按钮事件
        errorReplay.setOnClickListener(mReplayButtonClickListener);// 播放成后显示的重播按钮事件
        pauseBtn.setOnClickListener(mPlayButtonClickListener);// 暂停播放按钮事件
        soundBtn.setOnClickListener(mSoundButtonClickListener);// 声音按钮事件
        progressSeekbar.setOnSeekBarChangeListener(mSeekBarChangeListener);// 回放进度条拖动事件
        // 录像类型切换事件
        mCheckBtnCloud.setOnClickListener(mCloudCheckListener);
        mCheckBtnSDKCloud.setOnClickListener(mSDKCloudCheckListener);
        mCheckBtnDevice.setOnClickListener(mDeviceCheckListener);
    }

    /**
     * 开始播放前，UI重置
     */
    private void newPlayInit() {
        resetPlayUIVisible();// 初始化播放界面组件的可见性
        resetPauseBtnUI();// 暂停按钮初始化
        progressSeekbar.setProgress(0);
    }

    /**
     * 暂停按钮 UI和状态值重置
     */
    private void resetPauseBtnUI() {
        notPause = true;
        pauseBtn.setBackgroundResource(R.drawable.ez_remote_list_pause_btn_selector);
    }

    /**
     * 初始化播放界面组件的可见性
     */
    private void resetPlayUIVisible() {
        loadingPbLayout.setVisibility(View.VISIBLE);
        errorTipsVg.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
        playbackRateBtn.setText("1x");
    }

    /**
     * 回放进度条拖动后，UI重置
     */
    private void seekInit() {
        resetSeekPlayUI();
        resetPauseBtnUI();
    }

    /**
     * 回放进度条拖动后，设置相关组件的可见性
     */
    private void resetSeekPlayUI() {
        loadingPbLayout.setVisibility(View.VISIBLE);
        errorTipsVg.setVisibility(View.GONE);
        errorReplay.setVisibility(View.GONE);
    }

    /**
     * 录像播放进度UI初始化（已播放时长、总时长、SeekBar进度）
     */
    private void setPlaybackProgressUI(long beginTime, long endTime) {
        int diffSeconds = (int) (endTime - beginTime) / 1000;
        String convToUIDuration = RemoteListUtil.convToUIDuration(diffSeconds);
        duringTimeTV.setText(convToUIDuration);
        playTimeTV.setText(RemoteListContant.VIDEO_DUAR_BEGIN_INIT);
        progressSeekbar.setProgress(0);
    }

    /**
     * 设置取流方式
     */
    private void showStreamFetchType() {
        int type = mPlaybackPlayer.getStreamFetchType();
        LogUtil.i(TAG, "getStreamFetchType: " + type);
        String streamTypeMsg = getApplicationContext().getString(R.string.stream_type) + EZBusinessTool.getStreamType(type);
        streamTypeTv.setText(streamTypeMsg);
        streamTypeTv.setVisibility(View.VISIBLE);
    }

    /**
     * 录像类型选中状态重置
     */
    private void resetRecordCheckBtns() {
        mCheckBtnCloud.setChecked(false);
        mCheckBtnSDKCloud.setChecked(false);
        mCheckBtnDevice.setChecked(false);
        mContentTabCloudRl.setVisibility(View.GONE);
        mContentTabSDKCloudRl.setVisibility(View.GONE);
        mContentTabDeviceRl.setVisibility(View.GONE);
    }

    /**
     * 根据id对视图进行显示/隐藏
     *
     * @param id
     */
    private void showTab(int id) {
        if (id == R.id.novideo_img_cloud) {
            mNovideoImgCloud.setVisibility(View.VISIBLE);
            recordLoadingBar.setVisibility(View.GONE);
            mTabContentMainFrame.setVisibility(View.VISIBLE);
        } else if (id == R.id.novideo_img_sdkcloud) {
            mNovideoImgSDKCloud.setVisibility(View.VISIBLE);
            recordLoadingBar.setVisibility(View.GONE);
            mTabContentMainFrame.setVisibility(View.VISIBLE);
        } else if (id == R.id.novideo_img_device) {
            mNoVideoImgLocal.setVisibility(View.VISIBLE);
            mPinnedHeaderListViewForLocal.setVisibility(View.GONE);
            recordLoadingBar.setVisibility(View.GONE);
            mTabContentMainFrame.setVisibility(View.VISIBLE);
        } else if (id == R.id.record_loadingTextView) {
            mNovideoImgCloud.setVisibility(View.GONE);
            recordLoadingBar.setVisibility(View.VISIBLE);
            mTabContentMainFrame.setVisibility(View.GONE);
        } else if (id == R.id.content_tab_cloud_root) {
            mNovideoImgCloud.setVisibility(View.GONE);
            recordLoadingBar.setVisibility(View.GONE);
            mTabContentMainFrame.setVisibility(View.VISIBLE);
        } else if (id == R.id.content_tab_sdkcloud_root) {
            mNovideoImgSDKCloud.setVisibility(View.GONE);
            recordLoadingBar.setVisibility(View.GONE);
            mTabContentMainFrame.setVisibility(View.VISIBLE);
        } else if (id == R.id.content_tab_device_root) {
            mNoVideoImgLocal.setVisibility(View.GONE);
            recordLoadingBar.setVisibility(View.GONE);
            mTabContentMainFrame.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 展示播放过程中的重要事件提示，如播放出错、播放完成
     * show important tip during playing. just like error event, finish event
     */
    private void showPlayEventTip(final String tip) {
        runOnUiThread(() -> {
            loadingPbLayout.setVisibility(View.GONE);
            errorTipsVg.setVisibility(View.VISIBLE);// 错误提示布局显示
            errorReplay.setVisibility(View.VISIBLE);// 重播按钮显示
            errorInfoTV.setText(tip);// 错误提示信息
        });
    }

    /**
     * 根据分辨率调整SurfaceView的大小
     */
    private void setRemoteListSvLayout() {
        final int screenWidth = localInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ?
                (localInfo.getScreenHeight() - localInfo.getNavigationBarHeight()) : localInfo.getScreenHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                localInfo.getScreenWidth(), (int) (localInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);

        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ViewGroup playWindowVg = (ViewGroup) findViewById(R.id.vg_play_window);
        playWindowVg.setLayoutParams(svLp);
    }

    private void onOrientationChanged() {
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
            mPinnedHeaderListViewForCloud.setVisibility(View.VISIBLE);
            mControlBarRL.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            // 横屏处理
            // 隐藏状态栏
            fullScreen(true);
            remoteListPage.setBackgroundColor(getResources().getColor(R.color.black_bg));
            mTitleBar.setVisibility(View.GONE);
            mPinnedHeaderListViewForCloud.setVisibility(View.GONE);
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

    // endregion

    // region --- 鱼眼矫正模式弹出框打开 & 关闭|Fish eye correction mode pop-up box on&off ---

    /**
     * 打开鱼眼矫正模式操作弹出框
     *
     * @param parent
     */
    private void openFecViewModePopupWindow(View parent) {
        closeFecViewModePopupWindow();

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_fec_wnd, null, true);
        layoutView.findViewById(R.id.fec_close_btn).setOnClickListener(mOnFecWndClickListener);

        Button placeWallBtn = layoutView.findViewById(R.id.fec_place_wall);
        Button placeFloorBtn = layoutView.findViewById(R.id.fec_place_floor);
        Button placeCeilingBtn = layoutView.findViewById(R.id.fec_place_ceiling);
        Button correctFishBtn = layoutView.findViewById(R.id.fec_correct_fish);
        Button correct4PtzBtn = layoutView.findViewById(R.id.fec_correct_4ptz);
        Button correct5PtzBtn = layoutView.findViewById(R.id.fec_correct_5ptz);
        Button correctFull5PtzBtn = layoutView.findViewById(R.id.fec_correct_full5ptz);
        Button correctLatBtn = layoutView.findViewById(R.id.fec_correct_lat);
        Button correctARCHorBtn = layoutView.findViewById(R.id.fec_correct_arc_hor);
        Button correctARCVerBtn = layoutView.findViewById(R.id.fec_correct_arc_ver);
        Button correctWideAngleBtn = layoutView.findViewById(R.id.fec_correct_wide_angle);
        Button correct180Btn = layoutView.findViewById(R.id.fec_correct_180);
        Button correct360Btn = layoutView.findViewById(R.id.fec_correct_360);
        Button correctCycBtn = layoutView.findViewById(R.id.fec_correct_cyc);
        fecCorrectTypeButtons = new Button[]{correct4PtzBtn, correct5PtzBtn, correctFull5PtzBtn, correctLatBtn,
                correctARCHorBtn, correctARCVerBtn, correctWideAngleBtn, correct180Btn, correct360Btn, correctCycBtn};

        placeWallBtn.setOnClickListener(mOnFecWndClickListener);
        placeFloorBtn.setOnClickListener(mOnFecWndClickListener);
        placeCeilingBtn.setOnClickListener(mOnFecWndClickListener);
        correctFishBtn.setOnClickListener(mOnFecWndClickListener);
        correct4PtzBtn.setOnClickListener(mOnFecWndClickListener);
        correct5PtzBtn.setOnClickListener(mOnFecWndClickListener);
        correctFull5PtzBtn.setOnClickListener(mOnFecWndClickListener);
        correctLatBtn.setOnClickListener(mOnFecWndClickListener);
        correctARCHorBtn.setOnClickListener(mOnFecWndClickListener);
        correctARCVerBtn.setOnClickListener(mOnFecWndClickListener);
        correctWideAngleBtn.setOnClickListener(mOnFecWndClickListener);
        correct180Btn.setOnClickListener(mOnFecWndClickListener);
        correct360Btn.setOnClickListener(mOnFecWndClickListener);
        correctCycBtn.setOnClickListener(mOnFecWndClickListener);
        // 设置按钮的可见和可用状态
        int wallTypeValue = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_WALL, mDeviceInfo);
        int floorTypeValue = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_FLOOR, mDeviceInfo);
        int ceilingTypeValue = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_CEILING, mDeviceInfo);
        placeWallBtn.setVisibility(wallTypeValue > 0 ? View.VISIBLE : View.GONE);
        placeFloorBtn.setVisibility(floorTypeValue > 0 ? View.VISIBLE : View.GONE);
        placeCeilingBtn.setVisibility(ceilingTypeValue > 0 ? View.VISIBLE : View.GONE);
        setFecCorrectTypeBtnsEnable(fecPlaceType);

        int height = localInfo.getScreenHeight() - mTitleBar.getHeight() - remotePlayBackArea.getHeight() - controlArea.getHeight()
                - (mRemotePlayBackRect != null ? mRemotePlayBackRect.top : localInfo.getNavigationBarHeight());
        mFecPopupWindow = new PopupWindow(layoutView, LayoutParams.MATCH_PARENT, height, true);
        mFecPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mFecPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        mFecPopupWindow.setFocusable(false);
        mFecPopupWindow.setOutsideTouchable(false);
        mFecPopupWindow.showAsDropDown(parent);
        mFecPopupWindow.update();

        fecViewLayoutHelper.fecPopupWindow = mFecPopupWindow;
    }

    /**
     * 关闭鱼眼查看模式操作弹出框
     */
    private void closeFecViewModePopupWindow() {
        if (mFecPopupWindow != null) {
            dismissPopWindow(mFecPopupWindow);
            mFecPopupWindow = null;
            fecViewLayoutHelper.fecPopupWindow = null;// fecViewLayoutHelper中的窗口对象置空，必须
        }
    }

    // endregion

    // region --- 动态权限申请|Dynamic permission application ---

    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermission();
        } else {
            afterHasPermission();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {// Android 14+ (API 34+) 需要 READ_MEDIA_VIDEO 权限
            if (!(checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {// Android 10-13 需要读写权限
            if (!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {// Android 6.0-9.0 只需要写权限
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        // 权限都已经有了
        if (lackedPermission.size() == 0) {
            afterHasPermission();
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && hasAllPermissionsGranted(grantResults)) {
            afterHasPermission();
        } else {
            try {
                showPermissionDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 权限设置
     */
    private void showPermissionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。")
                .setPositiveButton("去设置", (dialog1, which) -> {
                    isFromPermissionSetting = true;
                    dialog1.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }).setNegativeButton("取消", (dialog12, which) -> {
                    dialog12.dismiss();
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.black));
        // 设置居中，解决Android9.0 AlertDialog不居中问题
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.width = (int) (LocalInfo.getInstance().getScreenWidth() * 0.9);
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);
    }

    private void afterHasPermission() {
        // 将录像存储到相册
        File file = new File(downloadFilePath);
        EZUtils.saveVideo2Album(EZPlayBackListActivity.this, file, recordInfoDuration);
        if (isMultiChannelDevice) {
            int index = downloadFilePath.lastIndexOf(".");
            String downloadFilePath2 = downloadFilePath.substring(0, index) + "_1" + downloadFilePath.substring(index);
            File file2 = new File(downloadFilePath2);
            EZUtils.saveVideo2Album(EZPlayBackListActivity.this, file2, recordInfoDuration);
        }
        showToast(getResources().getString(R.string.already_saved_to_volume));
        // TODO downloadFilePath的录像可以自行删除，避免占用手机内存，可以在onDestroy的时候。不能立即调用file.delete();因为文件存储到相册是异步耗时操作。
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    // endregion

}
