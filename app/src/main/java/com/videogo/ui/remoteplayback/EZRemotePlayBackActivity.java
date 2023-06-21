/* 
 * @ProjectName VideoGo
 * @Copyright null
 * 
 * @FileName RemotePlayBackActivity.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-7-1
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.remoteplayback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.videogo.EzvizApplication;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZAlarmInfo;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.remoteplayback.RemoteFileInfo;
import com.videogo.remoteplayback.RemotePlayBackMsg;
import com.videogo.remoteplayback.list.RemoteListContant;
import com.videogo.remoteplayback.list.RemoteListUtil;
import com.videogo.ui.common.ScreenOrientationHelper;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.ui.util.AudioPlayUtil;
import com.videogo.ui.util.DataManager;
import com.videogo.ui.util.EZUtils;
import com.videogo.ui.util.VerifyCodeInput;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.RotateViewUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.TimeBarHorizontalScrollView;
import com.videogo.widget.TimeBarHorizontalScrollView.TimeScrollBarScrollListener;
import com.videogo.widget.TitleBar;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;

public class EZRemotePlayBackActivity extends Activity implements OnClickListener, SurfaceHolder.Callback,
        Handler.Callback, TimeScrollBarScrollListener, VerifyCodeInput.VerifyCodeInputListener {
    private static final String TAG = "EZRemotePlayBackActivity";
    public static final int MSG_PLAY_UI_UPDATE = 100;
    public static final int MSG_SEARCH_CLOUD_FILE_SUCCUSS = 101;
    public static final int MSG_SEARCH_CLOUD_FILE_FAIL = 102;

    public static final int ALARM_MAX_DURATION = 30;

    public static final int STATUS_INIT = 0;

    public static final int STATUS_START = 1;

    public static final int STATUS_STOP = 2;

    public static final int STATUS_PLAY = 3;

    public static final int STATUS_PAUSE = 4;

    // 云存储录像 Cloud storage video
    private List<EZCloudRecordFile> mCloudFileList = null;
    // 设备本地录像 Device local video
    private List<RemoteFileInfo> mDeviceFileList = null;
    private Calendar mStartTime = null;
    private Calendar mEndTime = null;
    private Calendar mAlarmStartTime = null;
    private Calendar mAlarmStopTime = null;
    private Calendar mPlayStartTime = null;

    private AudioPlayUtil mAudioPlayUtil = null;
    private LocalInfo mLocalInfo = null;
    private Handler mHandler = null;
    private EZPlayer mEZMediaPlayer = null;

    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    private int mStatus = STATUS_INIT;
    private boolean mIsOnStop = false;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private long mStreamFlow = 0;
    private long mTotalStreamFlow = 0;
    private Rect mRemotePlayBackRect = null;

    private RelativeLayout mRemotePlayBackPageLy = null;
    private TitleBar mTitleBar = null;

    private SurfaceView mRemotePlayBackSv = null;
    private SurfaceHolder mRemotePlayBackSh = null;
    private CustomTouchListener mRemotePlayBackTouchListener = null;

    private float mPlayScale = 1;

    private LinearLayout mRemotePlayBackLoadingLy = null;
    private LinearLayout mRemotePlayBackLoadingPbLy = null;
    private TextView mRemotePlayBackLoadingTv = null;
    private TextView mRemotePlayBackTipTv = null;
    private ImageButton mRemotePlayBackReplayBtn = null;
    private ImageButton mRemotePlayBackLoadingPlayBtn = null;

    private RelativeLayout mRemotePlayBackControlRl = null;
    private ImageButton mRemotePlayBackBtn = null;
    private ImageButton mRemotePlayBackSoundBtn = null;
    private TextView mRemotePlayBackFlowTv = null;
    private int mControlDisplaySec = 0;
    private long mPlayTime = 0;

    private LinearLayout mRemotePlayBackProgressLy = null;
    private TextView mRemotePlayBackBeginTimeTv = null;
    private TextView mRemotePlayBackEndTimeTv = null;
    private SeekBar mRemotePlayBackSeekBar = null;
    private ProgressBar mRemotePlayBackProgressBar = null;

    private RelativeLayout mRemotePlayBackCaptureRl = null;
    private RelativeLayout.LayoutParams mRemotePlayBackCaptureRlLp = null;
    private ImageView mRemotePlayBackCaptureIv = null;
    private ImageView mRemotePlayBackCaptureWatermarkIv = null;
    private int mCaptureDisplaySec = 0;
    private LinearLayout mRemotePlayBackRecordLy = null;
    private ImageView mRemotePlayBackRecordIv = null;
    private TextView mRemotePlayBackRecordTv = null;

    private String mRecordFilePath = null;
    private String mRecordTime = null;
    private int mRecordSecond = 0;

    private LinearLayout mRemotePlayBackOperateBar = null;
    private ImageButton mRemotePlayBackCaptureBtn = null;
    private ImageButton mRemotePlayBackRecordBtn = null;
    private ImageButton mRemotePlayBackRecordStartBtn = null;
    private View mRemotePlayBackRecordContainer = null;
    private RotateViewUtil mRecordRotateViewUtil = null;

    private ImageButton mRemotePlayBackSmallRecordBtn = null;
    private ImageButton mRemotePlayBackSmallRecordStartBtn = null;
    private View mRemotePlayBackSmallRecordContainer = null;
    private ImageButton mRemotePlayBackSmallCaptureBtn = null;

    private RelativeLayout mRemotePlayBackFullOperateBar = null;
    private ImageButton mRemotePlayBackFullPlayBtn = null;
    private ImageButton mRemotePlayBackFullSoundBtn = null;
    private ImageButton mRemotePlayBackFullCaptureBtn = null;
    private ImageButton mRemotePlayBackFullRecordBtn = null;
    private ImageButton mRemotePlayBackFullRecordStartBtn = null;
    private View mRemotePlayBackFullRecordContainer = null;

    private ImageButton mRemotePlayBackFullDownBtn = null;
    private LinearLayout mRemotePlayBackFullFlowLy = null;
    private TextView mRemotePlayBackFullRateTv = null;
    private TextView mRemotePlayBackFullFlowTv = null;
    private TextView mRemotePlayBackRatioTv = null;

    private RelativeLayout mRemotePlayBackTimeBarRl = null;
    private TimeBarHorizontalScrollView mRemotePlayBackTimeBar = null;
    private RemoteFileTimeBar mRemoteFileTimeBar = null;
    private TextView mRemotePlayBackTimeTv = null;


    private ScreenBroadcastReceiver mScreenBroadcastReceiver = null;

    private Timer mUpdateTimer = null;

    private TimerTask mUpdateTimerTask = null;

    private boolean mNeedAutoPlaySearchResult = false;
    private final static int USER_MESSAGE_PLAYBACK_BASE = 1;
    private final static int USER_MESSAGE_PLAYBACK_ONETIME = USER_MESSAGE_PLAYBACK_BASE + 1;


    private CheckTextButton mFullscreenButton;
    private CheckTextButton mFullscreenFullButton;
    private ScreenOrientationHelper mScreenOrientationHelper;
    private boolean bIsRecording = false;
    private List<EZDeviceRecordFile> mEZDeviceFileList = null;
    private EZCloudRecordFile mAlarmRecordFile = null;
    private EZDeviceRecordFile mAlarmRecordDeviceFile = null;
    private TitleBar mLandscapeTitleBar = null;

    private EZAlarmInfo mEZAlarmInfo;
    private String mRecordFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mEZAlarmInfo = intent.getParcelableExtra(IntentConsts.EXTRA_ALARM_INFO);

            if (mEZAlarmInfo == null){
                finish();
                return;
            }
            mAlarmStartTime = Utils.parseTimeToCalendar(mEZAlarmInfo.getAlarmStartTime());
        }
        if (mAlarmStartTime != null) {
            mAlarmStartTime.add(Calendar.SECOND, -5);
            mAlarmStopTime = (Calendar) mAlarmStartTime.clone();
            mAlarmStopTime.add(Calendar.SECOND, ALARM_MAX_DURATION);
        } else {
            mStartTime = Calendar.getInstance();
            mStartTime.set(Calendar.AM_PM, 0);
            mStartTime.set(mStartTime.get(Calendar.YEAR), mStartTime.get(Calendar.MONTH),
                    mStartTime.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            mEndTime = Calendar.getInstance();
            mEndTime.set(Calendar.AM_PM, 0);
            mEndTime.set(mEndTime.get(Calendar.YEAR), mEndTime.get(Calendar.MONTH),
                    mEndTime.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        }
        Application application = (Application) getApplication();
        //mRemotePlayBackHelper = RemotePlayBackHelper.getInstance(application);
        mAudioPlayUtil = AudioPlayUtil.getInstance(application);
        mLocalInfo = LocalInfo.getInstance();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));

        mHandler = new Handler(this);
        mRecordRotateViewUtil = new RotateViewUtil();

        mScreenBroadcastReceiver = new ScreenBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenBroadcastReceiver, filter);
    }

    private void initView() {
        setContentView(R.layout.ez_remote_playback_page);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mStatus != STATUS_STOP) {
                    stopRemotePlayBack();
                }
                finish();
            }
        });
        mLandscapeTitleBar = (TitleBar) findViewById(R.id.pb_notlist_title_bar_landscape);
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                null/*getResources().getDrawable(R.drawable.message_back_selector)*/);
//        mLandscapeTitleBar.setOnTouchListener(this);
        //mFullScreenTitleBarBackBtn = new CheckTextButton(this);
        //mFullScreenTitleBarBackBtn.setBackground(getResources().getDrawable(R.drawable.common_title_back_selector));
        //mLandscapeTitleBar.addLeftView(mFullScreenTitleBarBackBtn);
        if (!TextUtils.isEmpty(mEZAlarmInfo.getAlarmName())) {
            mLandscapeTitleBar.setTitle(mEZAlarmInfo.getAlarmName());
        }
        mLandscapeTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (mAlarmStartTime == null) {
            mTitleBar.setTitle(Utils.date2String(mStartTime.getTime()));
            mTitleBar.addTitleButton(R.drawable.remote_cal_selector, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePicker();
                }
            });
            mTitleBar.setOnTitleClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    showDatePicker();
                }
            });
        }
        mRemotePlayBackPageLy = (RelativeLayout) findViewById(R.id.remoteplayback_page_ly);
        ViewTreeObserver viewTreeObserver = mRemotePlayBackPageLy.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mRemotePlayBackRect == null) {
                    mRemotePlayBackRect = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(mRemotePlayBackRect);
                }
            }
        });
        mRemotePlayBackSv = (SurfaceView) findViewById(R.id.remoteplayback_sv);
        mRemotePlayBackSv.getHolder().addCallback(this);
        mRemotePlayBackTouchListener = new CustomTouchListener() {
            @Override
            public boolean canZoom(float scale) {
                if (mStatus == STATUS_PLAY) {
                    return true;
                } else {
                    return false;
                }
            }
            @Override
            public boolean canDrag(int direction) {
                if (mPlayScale != 1) {
                    return true;
                }
                return false;
            }
            @Override
            public void onSingleClick() {
                onRemotePlayBackSvClick();
            }
            @Override
            public void onDoubleClick(View v, MotionEvent e) {
                LogUtil.d(TAG, "onDoubleClick:");
            }

            @Override
            public void onZoom(float scale) {
            }
            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.d(TAG, "onDrag:" + direction);
            }
            @Override
            public void onEnd(int mode) {
                LogUtil.d(TAG, "onEnd:" + mode);
            }
            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.d(TAG, "onZoomChange:" + scale);
            }


        };
        mRemotePlayBackSv.setOnTouchListener(mRemotePlayBackTouchListener);
        mRemotePlayBackLoadingLy = (LinearLayout) findViewById(R.id.remoteplayback_loading_ly);
        mRemotePlayBackLoadingPbLy = (LinearLayout) findViewById(R.id.remoteplayback_loading_pb_ly);
        mRemotePlayBackLoadingTv = (TextView) findViewById(R.id.remoteplayback_loading_tv);
        mRemotePlayBackTipTv = (TextView) findViewById(R.id.remoteplayback_tip_tv);
        mRemotePlayBackReplayBtn = (ImageButton) findViewById(R.id.remoteplayback_replay_btn);
        mRemotePlayBackLoadingPlayBtn = (ImageButton) findViewById(R.id.remoteplayback_loading_play_btn);
        mRemotePlayBackControlRl = (RelativeLayout) findViewById(R.id.remoteplayback_control_rl);
        mRemotePlayBackBtn = (ImageButton) findViewById(R.id.remoteplayback_play_btn);
        mRemotePlayBackSoundBtn = (ImageButton) findViewById(R.id.remoteplayback_sound_btn);
        mRemotePlayBackFlowTv = (TextView) findViewById(R.id.remoteplayback_flow_tv);
        mRemotePlayBackFlowTv.setText("0k/s 0MB");
        mRemotePlayBackProgressLy = (LinearLayout) findViewById(R.id.remoteplayback_progress_ly);
        mRemotePlayBackBeginTimeTv = (TextView) findViewById(R.id.remoteplayback_begin_time_tv);
        mRemotePlayBackEndTimeTv = (TextView) findViewById(R.id.remoteplayback_end_time_tv);
        mRemotePlayBackSeekBar = (SeekBar) findViewById(R.id.remoteplayback_progress_seekbar);
        mRemotePlayBackProgressBar = (ProgressBar) findViewById(R.id.remoteplayback_progressbar);
        mRemotePlayBackCaptureRl = (RelativeLayout) findViewById(R.id.remoteplayback_capture_rl);
        mRemotePlayBackCaptureRlLp = (RelativeLayout.LayoutParams) mRemotePlayBackCaptureRl.getLayoutParams();
        mRemotePlayBackCaptureIv = (ImageView) findViewById(R.id.remoteplayback_capture_iv);
        mRemotePlayBackCaptureWatermarkIv = (ImageView) findViewById(R.id.remoteplayback_capture_watermark_iv);
        mRemotePlayBackRecordLy = (LinearLayout) findViewById(R.id.remoteplayback_record_ly);
        mRemotePlayBackRecordIv = (ImageView) findViewById(R.id.remoteplayback_record_iv);
        mRemotePlayBackRecordTv = (TextView) findViewById(R.id.remoteplayback_record_tv);
        mRemotePlayBackOperateBar = (LinearLayout) findViewById(R.id.remoteplayback_operate_bar);
        mRemotePlayBackCaptureBtn = (ImageButton) findViewById(R.id.remoteplayback_previously_btn);
        mRemotePlayBackRecordBtn = (ImageButton) findViewById(R.id.remoteplayback_video_btn);
        mRemotePlayBackRecordContainer = findViewById(R.id.remoteplayback_video_container);
        mRemotePlayBackRecordStartBtn = (ImageButton) findViewById(R.id.remoteplayback_video_start_btn);
        mRemotePlayBackSmallCaptureBtn = (ImageButton) findViewById(R.id.remoteplayback_small_previously_btn);
        mRemotePlayBackSmallRecordBtn = (ImageButton) findViewById(R.id.remoteplayback_small_video_btn);
        mRemotePlayBackSmallRecordContainer = findViewById(R.id.remoteplayback_small_video_container);
        mRemotePlayBackSmallRecordStartBtn = (ImageButton) findViewById(R.id.remoteplayback_small_video_start_btn);
        mRemotePlayBackFullOperateBar = (RelativeLayout) findViewById(R.id.remoteplayback_full_operate_bar);
        mRemotePlayBackFullPlayBtn = (ImageButton) findViewById(R.id.remoteplayback_full_play_btn);
        mRemotePlayBackFullSoundBtn = (ImageButton) findViewById(R.id.remoteplayback_full_sound_btn);
        mRemotePlayBackFullCaptureBtn = (ImageButton) findViewById(R.id.remoteplayback_full_previously_btn);
        mRemotePlayBackFullRecordBtn = (ImageButton) findViewById(R.id.remoteplayback_full_video_btn);
        mRemotePlayBackFullRecordContainer = findViewById(R.id.remoteplayback_full_video_container);
        mRemotePlayBackFullRecordStartBtn = (ImageButton) findViewById(R.id.remoteplayback_full_video_start_btn);
        mRemotePlayBackFullDownBtn = (ImageButton) findViewById(R.id.remoteplayback_full_down_btn);
        mRemotePlayBackFullFlowLy = (LinearLayout) findViewById(R.id.remoteplayback_full_flow_ly);
        mRemotePlayBackFullRateTv = (TextView) findViewById(R.id.remoteplayback_full_rate_tv);
        mRemotePlayBackFullFlowTv = (TextView) findViewById(R.id.remoteplayback_full_flow_tv);
        mRemotePlayBackRatioTv = (TextView) findViewById(R.id.remoteplayback_ratio_tv);
        mRemotePlayBackFullRateTv.setText("0k/s");
        mRemotePlayBackFullFlowTv.setText("0MB");
        mFullscreenButton = (CheckTextButton) findViewById(R.id.fullscreen_button);
        mFullscreenFullButton = (CheckTextButton) findViewById(R.id.fullscreen_full_button);
        mRemotePlayBackTimeBarRl = (RelativeLayout) findViewById(R.id.remoteplayback_timebar_rl);
        mRemotePlayBackTimeBar = (TimeBarHorizontalScrollView) findViewById(R.id.remoteplayback_timebar);
        mRemotePlayBackTimeBar.setTimeScrollBarScrollListener(this);
        mRemotePlayBackTimeBar.smoothScrollTo(0, 0);
        mRemoteFileTimeBar = (RemoteFileTimeBar) findViewById(R.id.remoteplayback_file_time_bar);
        mRemoteFileTimeBar.setX(0, mLocalInfo.getScreenWidth() * 6);
        mRemotePlayBackTimeTv = (TextView) findViewById(R.id.remoteplayback_time_tv);
        mRemotePlayBackTimeTv.setText("00:00:00");
        setRemotePlayBackSvLayout();
        if (mAlarmStartTime != null) {
            mRemotePlayBackTimeBarRl.setVisibility(View.GONE);
            mRemotePlayBackProgressLy.setVisibility(View.VISIBLE);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//            mRemotePlayBackBeginTimeTv.setText(sdf.format(mAlarmStartTime.getTimeInMillis()));
//            mRemotePlayBackEndTimeTv.setText(sdf.format(mAlarmStopTime.getTimeInMillis()));
            mRemotePlayBackProgressBar.setMax(ALARM_MAX_DURATION);
            mRemotePlayBackProgressBar.setProgress(0);
            mRemotePlayBackSeekBar.setMax(ALARM_MAX_DURATION);
            mRemotePlayBackSeekBar.setProgress(0);
            mRemotePlayBackSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    if (mStatus != STATUS_STOP) {
//                        stopRemotePlayBack();
                        stopUpdateTimer();
                        if (mEZMediaPlayer != null) {
                            stopRemotePlayBackRecord();
                        }
                    }
                    Calendar seletedTime = (Calendar) mPlayStartTime.clone();
                    seletedTime.add(Calendar.SECOND, progress);
                    mPlayTime = seletedTime.getTimeInMillis();
                    mRemotePlayBackProgressBar.setProgress(progress);
                    //startRemotePlayBack(seletedTime);
                    if (mEZMediaPlayer != null) {
                        mEZMediaPlayer.seekPlayback(seletedTime);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                }
            });
        } else {
            mRemotePlayBackTimeBarRl.setVisibility(View.VISIBLE);
        }

        mScreenOrientationHelper = new ScreenOrientationHelper(this, mFullscreenButton, mFullscreenFullButton);
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        {
            //onExitCurrentPage();
            if (mStatus != STATUS_STOP) {
                stopRemotePlayBack();
            }
            finish();
        }
    }

    private void initUI() {
        if (mEZAlarmInfo == null) {
            return;
        }
        if (mAlarmStartTime != null) {
            mTitleBar.setTitle(TextUtils.isEmpty(mEZAlarmInfo.getAlarmName())?"":mEZAlarmInfo.getAlarmName());
        }
    }

    private void showDatePicker() {
        DatePickerDialog dpd = new DatePickerDialog(this, null, mStartTime.get(Calendar.YEAR),
                mStartTime.get(Calendar.MONTH), mStartTime.get(Calendar.DAY_OF_MONTH));

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
                        dp.clearFocus();

                        if (mStatus != STATUS_STOP) {
                            stopRemotePlayBack();
                        }

                        mEZCloudFileList = null;
                        mCloudFileList = null;
                        mPlayTime = 0;
                        mStartTime = Calendar.getInstance();
                        mStartTime.set(Calendar.AM_PM, 0);
                        mStartTime.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), 0, 0, 0);
                        mEndTime = Calendar.getInstance();
                        mEndTime.set(Calendar.AM_PM, 0);
                        mEndTime.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), 23, 59, 59);
                        mTitleBar.setTitle(Utils.date2String(mStartTime.getTime()));

                        startRemotePlayBack(null);
                    }
                });
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtil.d("Picker", "Cancel!");
                        if (!isFinishing()) {
                            dialog.dismiss();
                        }

                    }
                });

        dpd.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRemotePlayBackSv.setVisibility(View.VISIBLE);
        initUI();
        if (mStatus == STATUS_INIT || mStatus == STATUS_PAUSE) {
            startRemotePlayBack(getTimeBarSeekTime());
        } else if (mIsOnStop) {
            if (mStatus != STATUS_STOP) {
                stopRemotePlayBack();
            }
            startRemotePlayBack(getTimeBarSeekTime());
        }

        mIsOnStop = false;

    }

    @Override
    protected void onStart() {
        super.onStart();
        mScreenOrientationHelper.postOnStart();

        // On first entering, search the current day file list
        Message msg = Message.obtain();
        msg.what = USER_MESSAGE_PLAYBACK_ONETIME;
        mHandler.sendMessage(msg);
    }

    private Calendar getTimeBarSeekTime() {
        if (mAlarmStartTime != null) {
            int progress = mRemotePlayBackSeekBar.getProgress();
            Calendar seletedTime = (Calendar) mAlarmStartTime.clone();
            if (progress < ALARM_MAX_DURATION) {
                seletedTime.add(Calendar.SECOND, progress);
            }
            return seletedTime;
        } else {
            return mRemoteFileTimeBar.pos2Calendar(mPlayTime == 0 ? 0 : mRemotePlayBackTimeBar.getScrollX(), mOrientation);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScreenOrientationHelper.postOnStop();
        if (mEZAlarmInfo == null) {
            return;
        }
        if (mStatus != STATUS_STOP) {
            mIsOnStop = true;
            stopRemotePlayBack();
            setRemotePlayBackStopUI();
        }
        mRemotePlayBackSv.setVisibility(View.INVISIBLE);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScreenBroadcastReceiver != null) {
            // 取消锁屏广播的注册
            unregisterReceiver(mScreenBroadcastReceiver);
        }

        if (mEZMediaPlayer != null) {
            mEZMediaPlayer.release();
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                if (mStatus != STATUS_STOP) {
                    if (mStatus == STATUS_PLAY) {
                        pauseRemotePlayBack();
                    } else {
                        stopRemotePlayBack();
                    }
                    setRemotePlayBackStopUI();
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;
        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void updateSoundUI() {
        if (mLocalInfo.isSoundOpen()) {
            mRemotePlayBackSoundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
            mRemotePlayBackFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundon_btn_selector);
        } else {
            mRemotePlayBackSoundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
            mRemotePlayBackFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundoff_btn_selector);
        }
    }

    private void updateTimeBarUI() {
        if (mAlarmStartTime != null) {
            if (mRemotePlayBackControlRl.getVisibility() == View.VISIBLE) {
                //RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mRemotePlayBackProgressLy.getLayoutParams();
                //lp.setMargins(0, 0, 0, Utils.dip2px(this, 40));
                //mRemotePlayBackProgressLy.setLayoutParams(lp);
            } else {
                //RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mRemotePlayBackProgressLy.getLayoutParams();
                //lp.setMargins(0, 0, 0, 0);
                //mRemotePlayBackProgressLy.setLayoutParams(lp);               
            }
        } else {
            float pos = mRemoteFileTimeBar.getScrollPosByPlayTime(mPlayTime, mOrientation);
            mRemotePlayBackTimeBar.smoothScrollTo((int) pos, 0);
        }
    }

    private void updateOperatorUI() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            fullScreen(false);
            mRemotePlayBackPageLy.setBackgroundColor(getResources().getColor(R.color.common_bg));
            mTitleBar.setVisibility(View.VISIBLE);
            mRemotePlayBackOperateBar.setVisibility(View.VISIBLE);
            mRemotePlayBackFullOperateBar.setVisibility(View.GONE);
            mRemotePlayBackControlRl.setVisibility(View.VISIBLE);
            if (mAlarmStartTime != null) {
                mRemotePlayBackProgressLy.setVisibility(View.VISIBLE);
                mRemotePlayBackProgressBar.setVisibility(View.GONE);
            }
            mRemotePlayBackSmallCaptureBtn.setVisibility(View.GONE);
            mRemotePlayBackSmallRecordContainer.setVisibility(View.GONE);
            if (mAlarmStartTime == null) {
                mRemoteFileTimeBar.setX(0, mLocalInfo.getScreenWidth() * 6);
                mRemotePlayBackTimeBarRl.setBackgroundColor(getResources().getColor(R.color.transparent));
                mRemotePlayBackTimeBarRl.setVisibility(View.VISIBLE);
            }
            if (mRecordFilePath != null) {
                mRemotePlayBackRecordBtn.setVisibility(View.GONE);
                mRemotePlayBackRecordStartBtn.setVisibility(View.VISIBLE);
            } else {
                mRemotePlayBackRecordBtn.setVisibility(View.VISIBLE);
                mRemotePlayBackRecordStartBtn.setVisibility(View.GONE);
            }
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            mLandscapeTitleBar.setVisibility(View.VISIBLE);
            mRemotePlayBackControlRl.setVisibility(View.GONE);
            // 隐藏状态栏
            fullScreen(true);
            mRemotePlayBackPageLy.setBackgroundColor(getResources().getColor(R.color.black_bg));
            mTitleBar.setVisibility(View.GONE);
            mRemotePlayBackOperateBar.setVisibility(View.GONE);
            if (mAlarmStartTime == null) {
                mRemotePlayBackFullOperateBar.setVisibility(View.GONE);
                mRemoteFileTimeBar.setX(0, mLocalInfo.getScreenHeight() * 6);
                mRemotePlayBackTimeBarRl.setBackgroundColor(getResources().getColor(R.color.play_translucent_bg));
                mRemotePlayBackTimeBarRl.setVisibility(View.GONE);
                mRemotePlayBackFullDownBtn.setBackgroundResource(R.drawable.palyback_full_up);
                mRemotePlayBackFullOperateBar.setPadding(0, 0, 0, Utils.dip2px(this, 5));
            } else {
                mRemotePlayBackProgressLy.setVisibility(View.VISIBLE);
                mRemotePlayBackProgressBar.setVisibility(View.GONE);
                mRemotePlayBackSmallCaptureBtn.setVisibility(View.VISIBLE);
                mRemotePlayBackSmallRecordContainer.setVisibility(View.VISIBLE);
            }
            if (mRecordFilePath != null) {
                if (mAlarmStartTime != null) {
                    mRemotePlayBackSmallRecordBtn.setVisibility(View.GONE);
                    mRemotePlayBackSmallRecordStartBtn.setVisibility(View.VISIBLE);
                } else {
                    mRemotePlayBackFullRecordBtn.setVisibility(View.GONE);
                    mRemotePlayBackFullRecordStartBtn.setVisibility(View.VISIBLE);
                }
            } else {
                if (mAlarmStartTime != null) {
                    mRemotePlayBackSmallRecordBtn.setVisibility(View.VISIBLE);
                    mRemotePlayBackSmallRecordStartBtn.setVisibility(View.GONE);
                } else {
                    mRemotePlayBackFullRecordBtn.setVisibility(View.VISIBLE);
                    mRemotePlayBackFullRecordStartBtn.setVisibility(View.GONE);
                }
            }
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

    private void onOrientationChanged() {
//        mRemotePlayBackSv.setVisibility(View.INVISIBLE);
        setRemotePlayBackSvLayout();
//        mRemotePlayBackSv.setVisibility(View.VISIBLE);

        updateOperatorUI();
        updateCaptureUI();
        updateTimeBarUI();

    }

    private void updateLoadingProgress(final int progress) {
        mRemotePlayBackLoadingTv.setText(progress + "%");
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Random r = new Random();
                mRemotePlayBackLoadingTv.setText((progress + r.nextInt(20)) + "%");
            }

        }, 500);
    }

    /* (non-Javadoc)
     * @see android.os.Handler.Callback#handleMessage(android.os.Message)
     */
    @Override
    public boolean handleMessage(Message msg) {
        LogUtil.i(TAG, "handleMessage:" + msg.what);
        switch (msg.what) {
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_PLAY_START:
                updateLoadingProgress(60);
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_CONNECTION_START:
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_CONNECTION_SUCCESS:
                updateLoadingProgress(80);
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_PLAY_FINISH:
                handlePlayFinish();
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_RATIO_CHANGED:
                if (msg.arg1 != 0) {
                    mRealRatio = (float) msg.arg2 / msg.arg1;
                }
                setRemotePlayBackSvLayout();
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_CONNECTION_EXCEPTION:
                handleConnectionException(msg.arg1);
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_PLAY_SUCCUSS:
                handlePlaySuccess(msg);
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_PLAY_FAIL:
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_SEARCH_FILE_FAIL:
                handlePlayFail(msg.arg1, msg.obj);
                break;
            case RemotePlayBackMsg.MSG_START_RECORD_SUCCESS:
                handleStartRecordSuccess((String) msg.obj);
                break;
            case RemotePlayBackMsg.MSG_START_RECORD_FAIL:
                Utils.showToast(this, R.string.remoteplayback_record_fail);
                break;
            case RemotePlayBackMsg.MSG_CAPTURE_PICTURE_SUCCESS:
                handleCapturePictureSuccess((String) msg.obj);
                break;
            case RemotePlayBackMsg.MSG_CAPTURE_PICTURE_FAIL:
                // 提示抓图失败
                Utils.showToast(this, R.string.remoteplayback_capture_fail);
                break;
            case MSG_SEARCH_CLOUD_FILE_SUCCUSS:
                updateLoadingProgress(20);
                //handleSearchCloudFileSuccess((Calendar)description.obj);
                handleSearchEZCloudFileSuccess((Calendar) msg.obj);
                break;
            case MSG_SEARCH_CLOUD_FILE_FAIL:
                handleSearchCloudFileFail(msg.arg1);
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_SEARCH_FILE_SUCCUSS:
                updateLoadingProgress(40);
                //handleSearchDeviceFileSuccess();
//                handleSearchEZDeviceFileSuccess();
                break;
            case RemotePlayBackMsg.MSG_REMOTEPLAYBACK_SEARCH_NO_FILE:
                handleSearchNoFile();
                break;
            case MSG_PLAY_UI_UPDATE:
                updateRemotePlayBackUI();
                break;
            case USER_MESSAGE_PLAYBACK_ONETIME:
                // first entering activity, initiate a search and auto play back.
                break;
            default:
                break;
        }
        return false;
    }

    private void handleSearchDeviceFileSuccess() {
        /*mDeviceFileList = mEZMediaPlayer.getRemoteFileInfoList();
        if((mCloudFileList != null && mCloudFileList.size() > 0) || (mDeviceFileList != null && mDeviceFileList.size() > 0)) {
            if(mAlarmStartTime != null) {
                if(mRemotePlayBackBeginTimeTv.getTag() == null) {
                    Calendar fileStartTime = getFileStartTime();
                    Calendar beginTime = (fileStartTime != null && fileStartTime.getTimeInMillis() > mAlarmStartTime.getTimeInMillis()) ? fileStartTime:mAlarmStartTime;
                    Calendar lastStopTime = getFileStopTime();
                    Calendar endTime = (lastStopTime != null && lastStopTime.getTimeInMillis()  < mAlarmStopTime.getTimeInMillis()) ?lastStopTime:mAlarmStopTime;
                    if(beginTime.getTimeInMillis() > endTime.getTimeInMillis()) {
                        endTime = (Calendar)beginTime.clone();
                        endTime.add(Calendar.SECOND, ALARM_MAX_DURATION); 
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    mRemotePlayBackBeginTimeTv.setText(sdf.format(beginTime.getTimeInMillis()));
                    mRemotePlayBackEndTimeTv.setText(sdf.format(endTime.getTimeInMillis()));
                    int duration = (int)(endTime.getTimeInMillis() - beginTime.getTimeInMillis())/1000;
                    mRemotePlayBackProgressBar.setMax(duration);
                    mRemotePlayBackSeekBar.setMax(duration);
                    mRemotePlayBackBeginTimeTv.setTag(mAlarmStartTime);
                }
            } else {
                mRemoteFileTimeBar.drawFileLayout(mDeviceFileList, mCloudFileList, mStartTime, mEndTime);
            }  
        } else {
            handleSearchNoFile();
        }*/
    }

    private Calendar getFileStartTime() {
        Calendar cloudStartTime = null;
        if (mCloudFileList != null && mCloudFileList.size() > 0) {
            cloudStartTime = mCloudFileList.get(0).getStartTime();
        }
        Calendar deviceStartTime = null;
        if (mDeviceFileList != null && mDeviceFileList.size() > 0) {
            deviceStartTime = mDeviceFileList.get(0).getStartTime();
        }

        if (cloudStartTime != null && deviceStartTime != null) {
            return (cloudStartTime.getTimeInMillis() > deviceStartTime.getTimeInMillis()) ? deviceStartTime : cloudStartTime;
        } else if (cloudStartTime != null) {
            return cloudStartTime;
        } else {
            return deviceStartTime;
        }
    }

    private Calendar getFileStopTime() {
        Calendar cloudStopTime = null;
        if (mCloudFileList != null && mCloudFileList.size() > 0) {
            cloudStopTime = mCloudFileList.get(mCloudFileList.size() - 1).getStopTime();
        }
        Calendar deviceStopTime = null;
        if (mDeviceFileList != null && mDeviceFileList.size() > 0) {
            deviceStopTime = mDeviceFileList.get(mDeviceFileList.size() - 1).getStopTime();
        }

        if (cloudStopTime != null && deviceStopTime != null) {
            return (cloudStopTime.getTimeInMillis() > deviceStopTime.getTimeInMillis()) ? cloudStopTime : deviceStopTime;
        } else if (cloudStopTime != null) {
            return cloudStopTime;
        } else {
            return deviceStopTime;
        }
    }

    private void handleSearchNoFile() {
        stopRemotePlayBack();

        if (mAlarmStartTime != null) {
            setRemotePlayBackFailUI(getString(R.string.remoteplayback_norecordfile_alarm));
        } else {
            setRemotePlayBackFailUI(getString(R.string.remoteplayback_norecordfile));
        }
    }

    private void handleSearchEZCloudFileSuccess(Calendar seletedTime) {
        if (mEZMediaPlayer == null) {
            mEZMediaPlayer = EzvizApplication.getOpenSDK().createPlayer(mEZAlarmInfo.getDeviceSerial(),mEZAlarmInfo.getCameraNo());
            if (mEZMediaPlayer == null)
                return;

            if (mEZAlarmInfo.getIsEncrypt() == 1) {
                mEZMediaPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mEZAlarmInfo.getDeviceSerial()));
            }
            mEZMediaPlayer.setHandler(mHandler);
            mEZMediaPlayer.setSurfaceHold(mRemotePlayBackSh);
        }

        if (mAlarmStartTime != null) {
            if (mAlarmRecordDeviceFile != null) {
                mEZMediaPlayer.startPlayback(mAlarmRecordDeviceFile);
                mPlayStartTime = mAlarmRecordDeviceFile.getStartTime();
            } else if (mAlarmRecordFile != null) {
                mEZMediaPlayer.startPlayback(mAlarmRecordFile);
                mPlayStartTime = mAlarmRecordFile.getStartTime();
            }
        } else {
            EZCloudRecordFile ezCloudFile = null;
            if (mEZCloudFileList.size() > 0) {
                ezCloudFile = mEZCloudFileList.get(0);
            } else {
                Toast.makeText(this, "No record files found!", Toast.LENGTH_LONG).show();
                return;
            }

            if (ezCloudFile != null) {
                mEZMediaPlayer.startPlayback(ezCloudFile);
                mPlayStartTime = ezCloudFile.getStartTime();
            }
        }
    }

    private void handleSearchEZDeviceFileSuccess() {
        if ((mEZDeviceFileList != null && mEZDeviceFileList.size() > 0)
                || (mDeviceFileList != null && mDeviceFileList.size() > 0)) {
            if (mAlarmStartTime != null) {
                if (mRemotePlayBackBeginTimeTv.getTag() == null) {
                    Calendar fileStartTime = getFileStartTime();
                    Calendar beginTime = (fileStartTime != null && fileStartTime
                            .getTimeInMillis() > mAlarmStartTime
                            .getTimeInMillis()) ? fileStartTime
                            : mAlarmStartTime;
                    Calendar lastStopTime = getFileStopTime();
                    Calendar endTime = (lastStopTime != null && lastStopTime
                            .getTimeInMillis() < mAlarmStopTime
                            .getTimeInMillis()) ? lastStopTime : mAlarmStopTime;
                    if (beginTime.getTimeInMillis() > endTime.getTimeInMillis()) {
                        endTime = (Calendar) beginTime.clone();
                        endTime.add(Calendar.SECOND, ALARM_MAX_DURATION);
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    mRemotePlayBackBeginTimeTv.setText(sdf.format(beginTime
                            .getTimeInMillis()));
                    mRemotePlayBackEndTimeTv.setText(sdf.format(endTime
                            .getTimeInMillis()));
                    int duration = (int) (endTime.getTimeInMillis() - beginTime
                            .getTimeInMillis()) / 1000;
                    mRemotePlayBackProgressBar.setMax(duration);
                    mRemotePlayBackSeekBar.setMax(duration);
                    mRemotePlayBackBeginTimeTv.setTag(mAlarmStartTime);
                }
            } else {

                if (mEZMediaPlayer == null) {
                    mEZMediaPlayer = EzvizApplication.getOpenSDK().createPlayer(mEZAlarmInfo.getDeviceSerial(),mEZAlarmInfo.getCameraNo());
                    if (mEZMediaPlayer == null)
                        return;
                    if (mEZAlarmInfo.getIsEncrypt() == 1) {
                        mEZMediaPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mEZAlarmInfo.getDeviceSerial()));
                    }
                    mEZMediaPlayer.setHandler(mHandler);
                    mEZMediaPlayer.setSurfaceHold(mRemotePlayBackSh);
                }

                EZDeviceRecordFile ezDeviceFile = null;
                if (mEZDeviceFileList.size() > 0) {
                    ezDeviceFile = mEZDeviceFileList.get(0);
                } else {
                    Toast.makeText(this, "No record files found!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (ezDeviceFile != null) {
                    mEZMediaPlayer.startPlayback(ezDeviceFile.getStartTime(), ezDeviceFile.getStopTime());
                    mPlayStartTime = ezDeviceFile.getStartTime();
                }
            }
        } else {
            handleSearchNoFile();
        }
    }

    private void handleSearchCloudFileSuccess(Calendar seletedTime) {

        if (mEZMediaPlayer == null) {
            mEZMediaPlayer = EzvizApplication.getOpenSDK().createPlayer(mEZAlarmInfo.getDeviceSerial(),mEZAlarmInfo.getCameraNo());
            if (mEZMediaPlayer == null)
                return;
            if (mEZAlarmInfo.getIsEncrypt() == 1) {
                mEZMediaPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mEZAlarmInfo.getDeviceSerial()));
            }
            mEZMediaPlayer.setHandler(mHandler);
            mEZMediaPlayer.setSurfaceHold(mRemotePlayBackSh);
        }
    }

    // 搜索文件异常处理
    private void handleSearchCloudFileFail(int errorCode) {
        LogUtil.d(TAG, "handleSearchFileFail:" + errorCode);

        stopRemotePlayBack();

        String txt = null;
        // 判断返回的错误码
        switch (errorCode) {
            case ErrorCode.ERROR_WEB_SESSION_ERROR:
            case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
            case ErrorCode.ERROR_CAS_VERIFY_SESSION_ERROR:
                ActivityUtils.goToLoginAgain(EZRemotePlayBackActivity.this);
                return;
            default:
                txt = Utils.getErrorTip(this, R.string.remoteplayback_searchfile_fail_for_device, errorCode);
                break;
        }

        setRemotePlayBackFailUI(txt);
    }

    private void handleConnectionException(int errorCode) {
        LogUtil.d(TAG, "handleConnectionException:" + errorCode);
        Calendar startTime = Calendar.getInstance();
        Toast.makeText(this, "network connection exception, will restart playback", Toast.LENGTH_SHORT).show();

        Calendar seekTime = getTimeBarSeekTime();
        if (seekTime == null) {
            handlePlayFail(errorCode, null);
            return;
        }

        if (mPlayTime == 0) {
            Calendar osdTime = mEZMediaPlayer.getOSDTime();
            if (osdTime != null) {
                mPlayTime = osdTime.getTimeInMillis() + 5000;
            } else {
                mPlayTime = seekTime.getTimeInMillis() + 5000;
            }
        } else {
            mPlayTime = mPlayTime + 5000;
        }

        startTime.setTimeInMillis(mPlayTime);
        LogUtil.d(TAG, "handleConnectionException replay:" + startTime.toString());
        stopRemotePlayBack();
        startRemotePlayBack(startTime);
    }

    /* (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZMediaPlayer != null) {
            mEZMediaPlayer.setSurfaceHold(holder);
        }
        mRemotePlayBackSh = holder;
    }

    /* (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZMediaPlayer != null) {
            mEZMediaPlayer.setSurfaceHold(null);
        }
        mRemotePlayBackSh = null;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.remoteplayback_loading_play_btn:
                startRemotePlayBack(getTimeBarSeekTime());
                break;
            case R.id.remoteplayback_play_btn:
            case R.id.remoteplayback_full_play_btn:
                if (mStatus == STATUS_START || mStatus == STATUS_PLAY) {
                    if (mStatus == STATUS_PLAY) {
                        pauseRemotePlayBack();
                    } else {
                        stopRemotePlayBack();
                    }
                    setRemotePlayBackStopUI();
                } else {
                    startRemotePlayBack(getTimeBarSeekTime());
                }
                break;
            case R.id.remoteplayback_replay_btn:
                if (mStatus != STATUS_STOP) {
                    stopRemotePlayBack();
                }
                startRemotePlayBack(null);
                break;
            case R.id.remoteplayback_sound_btn:
            case R.id.remoteplayback_full_sound_btn:
                onSoundBtnClick();
                break;
            case R.id.remoteplayback_previously_btn:
            case R.id.remoteplayback_full_previously_btn:
            case R.id.remoteplayback_small_previously_btn:
                onCapturePicBtnClick();
                break;
            case R.id.remoteplayback_capture_rl:
                onCaptureRlClick();
                break;
            case R.id.remoteplayback_video_btn:
            case R.id.remoteplayback_full_video_btn:
            case R.id.remoteplayback_small_video_btn:
            case R.id.remoteplayback_video_start_btn:
            case R.id.remoteplayback_full_video_start_btn:
            case R.id.remoteplayback_small_video_start_btn:
                onRecordBtnClick();
                break;
            case R.id.remoteplayback_full_down_btn:
                onTimeBarDownBtnClick();
                break;
            default:
                break;
        }
    }

    private void onTimeBarDownBtnClick() {
        if (mRemotePlayBackTimeBarRl.getVisibility() == View.VISIBLE) {
            mRemotePlayBackTimeBarRl.setVisibility(View.GONE);
            mRemotePlayBackFullDownBtn.setBackgroundResource(R.drawable.palyback_full_up);
            mRemotePlayBackFullOperateBar.setPadding(0, 0, 0, Utils.dip2px(this, 5));
        } else {
            mRemotePlayBackTimeBarRl.setVisibility(View.VISIBLE);
            mRemotePlayBackFullDownBtn.setBackgroundResource(R.drawable.palyback_full_down);
            mRemotePlayBackFullOperateBar.setPadding(0, 0, 0, Utils.dip2px(this, 92));
        }
        updateCaptureUI();
    }

    private void onRecordBtnClick() {
        mControlDisplaySec = 0;

        if (bIsRecording) {
            stopRemotePlayBackRecord();
            bIsRecording = !bIsRecording;
            return;
        }

//        if (!SDCardUtil.isSDCardUseable()) {
//            // 提示SD卡不可用
//            Utils.showToast(this, R.string.remoteplayback_SDCard_disable_use);
//            return;
//        }
//
//        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
//            // 提示内存不足
//            Utils.showToast(this, R.string.remoteplayback_record_fail_for_memory);
//            return;
//        }

        if (mEZMediaPlayer != null) {
            mCaptureDisplaySec = 4;
            updateCaptureUI();
            mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
            // 可以采用deviceSerial+时间作为文件命名，demo中简化，只用时间命名
            java.util.Date date = new java.util.Date();
            final String strRecordFile = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + String.format("%tY", date)
                    + String.format("%tm", date) + String.format("%td", date) + "/"
                    + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) + ".mp4";

            mEZMediaPlayer.setStreamDownloadCallback(new EZOpenSDKListener.EZStreamDownloadCallback() {
                @Override
                public void onSuccess(String filepath) {
                    LogUtil.i(TAG, "EZStreamDownloadCallback onSuccess  "+filepath);

                }

                @Override
                public void onError(EZOpenSDKListener.EZStreamDownloadError code) {

                }

            });
            if(mEZMediaPlayer.startLocalRecordWithFile(strRecordFile)){
                bIsRecording = !bIsRecording;
                handleStartRecordSuccess(strRecordFile);
            }else{
                bIsRecording = !bIsRecording;
                handleRecordFail();
            }
        }
    }

    private void stopRemotePlayBackRecord() {
        if (mRecordFilePath == null) {
            return;
        }
        if (!bIsRecording) {
            return;
        }
        // 设置录像按钮为check状态
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRemotePlayBackRecordContainer, mRemotePlayBackRecordStartBtn,
                        mRemotePlayBackRecordBtn, 0, 90);
            } else {
                mRemotePlayBackRecordBtn.setVisibility(View.VISIBLE);
                mRemotePlayBackRecordStartBtn.setVisibility(View.GONE);
            }
            if (mAlarmStartTime != null) {
                mRemotePlayBackSmallRecordBtn.setVisibility(View.VISIBLE);
                mRemotePlayBackSmallRecordStartBtn.setVisibility(View.GONE);
            } else {
                mRemotePlayBackFullRecordBtn.setVisibility(View.VISIBLE);
                mRemotePlayBackFullRecordStartBtn.setVisibility(View.GONE);
            }
        } else {
            if (mAlarmStartTime != null) {
                if (!mIsOnStop) {
                    mRecordRotateViewUtil.applyRotation(mRemotePlayBackSmallRecordContainer,
                            mRemotePlayBackSmallRecordStartBtn, mRemotePlayBackSmallRecordBtn, 0, 90);
                } else {
                    mRemotePlayBackSmallRecordBtn.setVisibility(View.VISIBLE);
                    mRemotePlayBackSmallRecordStartBtn.setVisibility(View.GONE);
                }
                mRemotePlayBackSmallRecordBtn.setVisibility(View.VISIBLE);
                mRemotePlayBackSmallRecordStartBtn.setVisibility(View.GONE);
            } else {
                if (!mIsOnStop) {
                    mRecordRotateViewUtil.applyRotation(mRemotePlayBackFullRecordContainer,
                            mRemotePlayBackFullRecordStartBtn, mRemotePlayBackFullRecordBtn, 0, 90);
                } else {
                    mRemotePlayBackFullRecordBtn.setVisibility(View.VISIBLE);
                    mRemotePlayBackFullRecordStartBtn.setVisibility(View.GONE);
                }
                mRemotePlayBackRecordBtn.setVisibility(View.VISIBLE);
                mRemotePlayBackRecordStartBtn.setVisibility(View.GONE);
            }
        }

        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        mEZMediaPlayer.stopLocalRecord();

        // 计时按钮不可见
        mRemotePlayBackRecordLy.setVisibility(View.GONE);
//        mRemotePlayBackCaptureRl.setVisibility(View.VISIBLE);
        mCaptureDisplaySec = 0;
        try {
            mRemotePlayBackCaptureIv.setImageURI(Uri.parse(mRecordFilePath));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        mRemotePlayBackCaptureWatermarkIv.setTag(mRecordFilePath);
        mRecordFilePath = null;
        updateCaptureUI();
    }

    private void onCaptureRlClick() {
        mRemotePlayBackCaptureRl.setVisibility(View.GONE);
        mRemotePlayBackCaptureIv.setImageURI(null);
        mRemotePlayBackCaptureWatermarkIv.setTag(null);
        mRemotePlayBackCaptureWatermarkIv.setVisibility(View.GONE);
    }

    private void onSoundBtnClick() {
        if (mLocalInfo.isSoundOpen()) {
            mLocalInfo.setSoundOpen(false);
            mRemotePlayBackSoundBtn.setBackgroundResource(R.drawable.remote_list_soundoff_btn_selector);
            mRemotePlayBackFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundoff_btn_selector);
        } else {
            mLocalInfo.setSoundOpen(true);
            mRemotePlayBackSoundBtn.setBackgroundResource(R.drawable.remote_list_soundon_btn_selector);
            mRemotePlayBackFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundon_btn_selector);
        }

        setRemotePlaySound();
    }

    private void setRemotePlaySound() {
        LogUtil.e("aaaaa", mLocalInfo.isSoundOpen()+"");
        if (mEZMediaPlayer != null) {
            if (mLocalInfo.isSoundOpen()) {
                mEZMediaPlayer.openSound();
            } else {
                mEZMediaPlayer.closeSound();
            }
        }
    }

    private void onCapturePicBtnClick() {
        mControlDisplaySec = 0;
//        if (!SDCardUtil.isSDCardUseable()) {
//            // 提示SD卡不可用
//            Utils.showToast(this, R.string.remoteplayback_SDCard_disable_use);
//            return;
//        }
//
//        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
//            // 提示内存不足
//            Utils.showToast(this, R.string.remoteplayback_capture_fail_for_memory);
//            return;
//        }

        if (mEZMediaPlayer != null) {
            mCaptureDisplaySec = 4;
            updateCaptureUI();
            Thread thr = new Thread() {
                @Override
                public void run() {
                    if (mEZMediaPlayer == null) {
                        return;
                    }
                    String serial = !TextUtils.isEmpty(mEZAlarmInfo.getDeviceSerial()) ? mEZAlarmInfo.getDeviceSerial() : "123456789";
                    Bitmap bmp = mEZMediaPlayer.capturePicture();
                    if (bmp != null) {
                        try {
                            mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);

                            // 可以采用deviceSerial+时间作为文件命名，demo中简化，只用时间命名
                            java.util.Date date = new java.util.Date();
                            final String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + String.format("%tY", date)
                                    + String.format("%tm", date) + String.format("%td", date) + "/"
                                    + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";

                            if (TextUtils.isEmpty(path)) {
                                bmp.recycle();
                                bmp = null;
                                return;
                            }
                            EZUtils.saveCapturePictrue(path, bmp);
                            // TODO 将文件保存至相册，需要申请动态权限WRITE_EXTERNAL_STORAGE，由开发者自行实现
                            // EZUtils.savePicture2Album(EZRemotePlayBackActivity.this, bmp);// 将文件保存至相册
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EZRemotePlayBackActivity.this, getResources().getString(R.string.already_saved_to)+path, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
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
            //mEZMediaPlayer.capturePicture();
        }
    }

    private void onRemotePlayBackSvClick() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mLandscapeTitleBar.setVisibility(View.GONE);
            if (mRemotePlayBackControlRl.getVisibility() == View.VISIBLE) {
                mRemotePlayBackControlRl.setVisibility(View.GONE);
                if (mAlarmStartTime != null) {
                    mRemotePlayBackProgressLy.setVisibility(View.GONE);
                    mRemotePlayBackProgressBar.setVisibility(View.VISIBLE);
                }
            } else {
                mRemotePlayBackControlRl.setVisibility(View.VISIBLE);
                if (mAlarmStartTime != null) {
                    mRemotePlayBackProgressLy.setVisibility(View.VISIBLE);
                    mRemotePlayBackProgressBar.setVisibility(View.GONE);
                }
                mControlDisplaySec = 0;
            }
            updateTimeBarUI();
        } else {
            mRemotePlayBackControlRl.setVisibility(View.GONE);
            if (mAlarmStartTime == null) {
                if (mRemotePlayBackFullOperateBar.getVisibility() == View.VISIBLE) {
                    mRemotePlayBackFullOperateBar.setVisibility(View.GONE);
                } else {
                    mRemotePlayBackFullOperateBar.setVisibility(View.VISIBLE);
                    mControlDisplaySec = 0;
                }
            } else {
                mRemotePlayBackFullOperateBar.setVisibility(View.GONE);
                if (mRemotePlayBackProgressLy.getVisibility() == View.GONE) {
                    mRemotePlayBackProgressLy.setVisibility(View.VISIBLE);
                    mRemotePlayBackProgressBar.setVisibility(View.GONE);
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                } else {
                    mRemotePlayBackProgressLy.setVisibility(View.GONE);
                    mRemotePlayBackProgressBar.setVisibility(View.VISIBLE);
                    mLandscapeTitleBar.setVisibility(View.GONE);
                }
            }
        }

    }

    private void searchCloudFileList(final Calendar seletedTime) {
        new Thread() {
            @Override
            public void run() {
                Calendar starttime = null;
                Calendar endtime = null;
                try {
                    if (mAlarmStartTime != null) {
                       starttime = mAlarmStartTime;
                        endtime = mAlarmStopTime;
                    } else {
                        starttime = mStartTime;
                        endtime = mEndTime;
                    }
                    mCloudFileList = EzvizApplication.getOpenSDK().searchRecordFileFromCloud(mEZAlarmInfo.getDeviceSerial(),mEZAlarmInfo.getCameraNo(),starttime,endtime);
                    sendMessage(MSG_SEARCH_CLOUD_FILE_SUCCUSS, 0, seletedTime);
                } catch (BaseException e) {
                    e.printStackTrace();
                    sendMessage(MSG_SEARCH_CLOUD_FILE_FAIL, e.getErrorCode());
                }
            }
        }.start();
    }

    private void searchEZCloudFileList(final Calendar seletedTime) {
        new Thread() {
            @Override
            public void run() {
                try {
                    mEZCloudFileList = EzvizApplication.getOpenSDK().searchRecordFileFromCloud(mEZAlarmInfo.getDeviceSerial(),mEZAlarmInfo.getCameraNo(), mStartTime, mEndTime);
                    LogUtil.d(TAG, "searchEZCloudFileList ends: " + mEZCloudFileList);
                    sendMessage(MSG_SEARCH_CLOUD_FILE_SUCCUSS, 0, seletedTime);
                } catch (BaseException e) {
                    e.printStackTrace();

                    ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                    LogUtil.d(TAG, errorInfo.toString());
                    sendMessage(MSG_SEARCH_CLOUD_FILE_FAIL, errorInfo.errorCode);
                }
            }
        }.start();
    }

    private void searchEZAlarmFile(final Calendar seletedTime) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Calendar startTime = (Calendar) seletedTime.clone();
                    Calendar endTime = (Calendar) seletedTime.clone();
                    startTime.set(Calendar.HOUR_OF_DAY, 0);
                    startTime.set(Calendar.MINUTE, 0);
                    startTime.set(Calendar.SECOND, 0);
                    endTime.set(Calendar.HOUR_OF_DAY, 23);
                    endTime.set(Calendar.MINUTE, 59);
                    endTime.set(Calendar.SECOND, 59);

                    int size = 0;

                    LogUtil.i(TAG, "searchEZAlarmFile seletedTime:" + seletedTime.getTime());
                    mEZDeviceFileList = EzvizApplication.getOpenSDK().searchRecordFileFromDevice(mEZAlarmInfo.getDeviceSerial(),mEZAlarmInfo.getCameraNo(), startTime, endTime);
                    if (mEZDeviceFileList != null && mEZDeviceFileList.size() > 0) {
                        size = mEZDeviceFileList.size();
                        LogUtil.i(TAG, "searchEZDeviceFileList size:" + size);
                        for (int i = 0; i < size; i++) {
                            EZDeviceRecordFile ezDeviceFile = mEZDeviceFileList.get(i);
                            Calendar tmpStartTime = (ezDeviceFile.getStartTime());
                            Calendar tmpEndTime = (ezDeviceFile.getStopTime());
                            LogUtil.v(TAG, "startTime:" + tmpStartTime.getTime() + " endTime:" + tmpEndTime.getTime());

                            if (seletedTime.compareTo(tmpStartTime) >= 0 && seletedTime.compareTo(tmpEndTime) <= 0) {
                                mAlarmRecordDeviceFile = ezDeviceFile;
                                mAlarmRecordDeviceFile.setStartTime(mAlarmStartTime);
                                mAlarmRecordDeviceFile.setStopTime(mAlarmStopTime);

                                LogUtil.d(TAG, "searchEZDeviceFileList success: start, " + mAlarmRecordDeviceFile.getStartTime());
                                sendMessage(MSG_SEARCH_CLOUD_FILE_SUCCUSS, 0, seletedTime);
                                return;
                            }
                        }
                        LogUtil.d(TAG, "no matching device record file for alarm");
                    }

                    mEZCloudFileList = EzvizApplication.getOpenSDK().searchRecordFileFromCloud(mEZAlarmInfo.getDeviceSerial(),mEZAlarmInfo.getCameraNo(), startTime, endTime);
                    if (mEZCloudFileList != null && mEZCloudFileList.size() > 0) {
                        size = mEZCloudFileList.size();
                        LogUtil.d(TAG, "searchEZCloudFileList size:" + size);
                        for (int i = 0; i < size; i++) {
                            EZCloudRecordFile ezCloudFile = mEZCloudFileList.get(i);
                            Calendar tmpStartTime = ezCloudFile.getStartTime();
                            Calendar tmpEndTime = ezCloudFile.getStopTime();
                            LogUtil.v(TAG, "startTime:" + tmpStartTime.getTime() + " endTime:" + tmpEndTime.getTime());

                            if (seletedTime.compareTo(tmpStartTime) >= 0 && seletedTime.compareTo(tmpEndTime) <= 0) {
                                mAlarmRecordFile = ezCloudFile;
//                    			mAlarmRecordFile.setStartTime(mAlarmStartTime);
//                    			mAlarmRecordFile.setStopTime(mAlarmStopTime);
//                    			String startT = new SimpleDateFormat("yyyyMMddHHmmss").format(mAlarmStartTime.getTime());
//                    			String endT = new SimpleDateFormat("yyyyMMddHHmmss").format(mAlarmStopTime.getTime());
                                mAlarmRecordFile.setStartTime(tmpStartTime);
                                mAlarmRecordFile.setStopTime(tmpEndTime);

                                LogUtil.d(TAG, "searchEZCloudFileList success: start, " + mAlarmRecordFile.getStartTime());
                                sendMessage(MSG_SEARCH_CLOUD_FILE_SUCCUSS, 0, seletedTime);
                                return;
                            }
                        }
                        LogUtil.d(TAG, "no matching cloud record file for alarm");
                    }

                    sendMessage(MSG_SEARCH_CLOUD_FILE_FAIL, -1);
                } catch (BaseException e) {
                    e.printStackTrace();

                    ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                    LogUtil.d(TAG, "search file list failed. error " + errorInfo.toString());
                    sendMessage(MSG_SEARCH_CLOUD_FILE_FAIL, e.getErrorCode());
                }
            }
        }.start();
    }

    private List<EZCloudRecordFile> mEZCloudFileList = null;

    private void startRemotePlayBack(final Calendar selectedTime) {
        LogUtil.d(TAG, "startRemotePlayBack:" + selectedTime);

        if (mStatus == STATUS_START || mStatus == STATUS_PLAY) {
            return;
        }

        // 检查网络是否可用
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络
            setRemotePlayBackFailUI(getString(R.string.remoteplayback_searchfile_fail_for_network));
            return;
        }

        if (mEZMediaPlayer != null && mStatus == STATUS_PAUSE) {
            resumeRemotePlayBack();
            setRemotePlayBackSuccessUI();
            setRemotePlaySound();
            return;
        }

        mStatus = STATUS_START;
        setRemotePlayBackLoadingUI();
        updateLoadingProgress(0);


        if (mEZCloudFileList == null) {
            if (mAlarmStartTime != null) {
                searchEZAlarmFile(mAlarmStartTime);
            } else
                searchEZCloudFileList(selectedTime);
            return;
        }

        EZCloudRecordFile ezCloudFile = null;
        if (mAlarmStartTime != null && mAlarmRecordFile != null) {
            ezCloudFile = mAlarmRecordFile;
        } else if (mEZCloudFileList.size() > 0) {
            ezCloudFile = mEZCloudFileList.get(0);
        } else {
            return;
        }

        if (mEZMediaPlayer == null) {
            mEZMediaPlayer = EzvizApplication.getOpenSDK().createPlayer(mEZAlarmInfo.getDeviceSerial(),mEZAlarmInfo.getCameraNo());
            if (mEZMediaPlayer == null)
                return;
            if (mEZAlarmInfo.getIsEncrypt() == 1){
                mEZMediaPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mEZAlarmInfo.getDeviceSerial()));
            }
            mEZMediaPlayer.setHandler(mHandler);
            mEZMediaPlayer.setSurfaceHold(mRemotePlayBackSh);
        }


        if (mAlarmStartTime != null) {
            if (ezCloudFile != null) {
                mEZMediaPlayer.startPlayback(ezCloudFile);
                mPlayStartTime = ezCloudFile.getStartTime();
            }
        } else {
            if (ezCloudFile != null) {
                mEZMediaPlayer.startPlayback(ezCloudFile);
                mPlayStartTime = ezCloudFile.getStartTime();
            }
        }
    }

    private void stopRemotePlayBack() {
        LogUtil.d(TAG, "stopRemotePlayBack");
        mStatus = STATUS_STOP;

        stopUpdateTimer();
        if (mEZMediaPlayer != null) {
            stopRemotePlayBackRecord();

            mEZMediaPlayer.stopPlayback();
            //mj mTotalStreamFlow += mEZMediaPlayer.getStreamFlow();
        }
        mStreamFlow = 0;
    }

    private void pauseRemotePlayBack() {
        LogUtil.d(TAG, "pauseRemotePlayBack");
        mStatus = STATUS_PAUSE;

        if (mEZMediaPlayer != null) {
            stopRemotePlayBackRecord();

            mEZMediaPlayer.pausePlayback();
        }
    }

    private void resumeRemotePlayBack() {
        LogUtil.d(TAG, "resumeRemotePlayBack");
        mStatus = STATUS_PLAY;

        if (mEZMediaPlayer != null) {
            mEZMediaPlayer.openSound();
            mEZMediaPlayer.resumePlayback();
        }
    }


    private void setRemotePlayBackLoadingUI() {
        mRemotePlayBackSv.setVisibility(View.INVISIBLE);
        mRemotePlayBackSv.setVisibility(View.VISIBLE);
        mRemotePlayBackTipTv.setVisibility(View.GONE);
        mRemotePlayBackReplayBtn.setVisibility(View.GONE);
        mRemotePlayBackLoadingPlayBtn.setVisibility(View.GONE);

        mRemotePlayBackLoadingLy.setVisibility(View.VISIBLE);
        mRemotePlayBackLoadingPbLy.setVisibility(View.VISIBLE);

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT || mAlarmStartTime != null) {
            mRemotePlayBackControlRl.setVisibility(View.VISIBLE);
            if (mAlarmStartTime != null) {
                mRemotePlayBackProgressLy.setVisibility(View.VISIBLE);
                mRemotePlayBackProgressBar.setVisibility(View.GONE);
            }
            mRemotePlayBackFullOperateBar.setVisibility(View.GONE);
        } else {
            mRemotePlayBackFullOperateBar.setVisibility(View.VISIBLE);
            mRemotePlayBackControlRl.setVisibility(View.GONE);
        }
        mRemotePlayBackCaptureBtn.setEnabled(false);
        mRemotePlayBackRecordBtn.setEnabled(false);
        mRemotePlayBackSmallCaptureBtn.setEnabled(false);
        mRemotePlayBackSmallRecordBtn.setEnabled(false);

        mRemotePlayBackFullPlayBtn.setEnabled(false);
        mRemotePlayBackFullCaptureBtn.setEnabled(false);
        mRemotePlayBackFullRecordBtn.setEnabled(false);
        mRemotePlayBackFullFlowLy.setVisibility(View.GONE);

        updateSoundUI();

        updateTimeBarUI();
    }

    private void setRemotePlayBackStopUI() {
        stopUpdateTimer();
        setRemotePlayBackSvLayout();
        mRemotePlayBackTipTv.setVisibility(View.GONE);
        mRemotePlayBackReplayBtn.setVisibility(View.GONE);
        mRemotePlayBackLoadingLy.setVisibility(View.GONE);
        mRemotePlayBackLoadingPlayBtn.setVisibility(View.VISIBLE);
//        if (mTotalStreamFlow > 0) {
//            mRemotePlayBackFlowTv.setVisibility(View.VISIBLE);
//            mRemotePlayBackFullFlowLy.setVisibility(View.VISIBLE);    
//            updateRemotePlayBackFlowTv(mStreamFlow);
//        } else {
//            mRemotePlayBackFlowTv.setVisibility(View.GONE);
//            mRemotePlayBackFullFlowLy.setVisibility(View.GONE);
//        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT || mAlarmStartTime != null) {
            mRemotePlayBackControlRl.setVisibility(View.VISIBLE);
            if (mAlarmStartTime != null) {
                mRemotePlayBackProgressLy.setVisibility(View.VISIBLE);
                mRemotePlayBackProgressBar.setVisibility(View.GONE);
            }
        } else {
            mRemotePlayBackFullOperateBar.setVisibility(View.VISIBLE);
        }
        mRemotePlayBackBtn.setBackgroundResource(R.drawable.remote_list_play_btn_selector);

        mRemotePlayBackCaptureBtn.setEnabled(false);
        mRemotePlayBackRecordBtn.setEnabled(false);
        mRemotePlayBackSmallCaptureBtn.setEnabled(false);
        mRemotePlayBackSmallRecordBtn.setEnabled(false);

        mRemotePlayBackFullPlayBtn.setEnabled(true);
        mRemotePlayBackFullPlayBtn.setBackgroundResource(R.drawable.play_full_play_selector);
        mRemotePlayBackFullCaptureBtn.setEnabled(false);
        mRemotePlayBackFullRecordBtn.setEnabled(false);
        updateSoundUI();

        updateTimeBarUI();
    }

    private void setRemotePlayBackFailUI(String errorStr) {
        stopUpdateTimer();
        mScreenOrientationHelper.disableSensorOrientation();
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (TextUtils.isEmpty(errorStr)) {
            mRemotePlayBackTipTv.setVisibility(View.GONE);
            mRemotePlayBackReplayBtn.setVisibility(View.VISIBLE);
        } else {
            mRemotePlayBackTipTv.setVisibility(View.VISIBLE);
            mRemotePlayBackTipTv.setText(errorStr);
            mRemotePlayBackReplayBtn.setVisibility(View.GONE);
        }

        mRemotePlayBackLoadingPlayBtn.setVisibility(View.GONE);
        mRemotePlayBackLoadingLy.setVisibility(View.GONE);
        mRemotePlayBackFlowTv.setVisibility(View.GONE);
        mRemotePlayBackFullFlowLy.setVisibility(View.GONE);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT || mAlarmStartTime != null) {
            mRemotePlayBackControlRl.setVisibility(View.VISIBLE);
            if (mAlarmStartTime != null) {
                mRemotePlayBackProgressLy.setVisibility(View.VISIBLE);
                mRemotePlayBackProgressBar.setVisibility(View.GONE);
            }
        } else {
            mRemotePlayBackFullOperateBar.setVisibility(View.VISIBLE);
        }
        mRemotePlayBackBtn.setBackgroundResource(R.drawable.remote_list_play_btn_selector);

        mRemotePlayBackCaptureBtn.setEnabled(false);
        mRemotePlayBackRecordBtn.setEnabled(false);
        mRemotePlayBackSmallCaptureBtn.setEnabled(false);
        mRemotePlayBackSmallRecordBtn.setEnabled(false);

        mRemotePlayBackFullPlayBtn.setEnabled(true);
        mRemotePlayBackFullPlayBtn.setBackgroundResource(R.drawable.play_full_play_selector);
        mRemotePlayBackFullCaptureBtn.setEnabled(false);
        mRemotePlayBackFullRecordBtn.setEnabled(false);

        updateSoundUI();

        updateTimeBarUI();
    }

    /**
     * 这里对方法做描述
     * @see
     * @since V1.8.2
     */
    private void setRemotePlayBackSuccessUI() {
        // 允许屏幕旋转
        mScreenOrientationHelper.enableSensorOrientation();
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mRemotePlayBackTipTv.setVisibility(View.GONE);
        mRemotePlayBackReplayBtn.setVisibility(View.GONE);
        mRemotePlayBackLoadingPlayBtn.setVisibility(View.GONE);

        mRemotePlayBackLoadingLy.setVisibility(View.GONE);
//        mRemotePlayBackFlowTv.setVisibility(View.VISIBLE);
        mRemotePlayBackFullFlowLy.setVisibility(View.VISIBLE);
        mRemotePlayBackBtn.setBackgroundResource(R.drawable.remote_list_pause_btn_selector);

        mRemotePlayBackCaptureBtn.setEnabled(true);
        mRemotePlayBackRecordBtn.setEnabled(true);
        mRemotePlayBackSmallCaptureBtn.setEnabled(true);
        mRemotePlayBackSmallRecordBtn.setEnabled(true);

        mRemotePlayBackFullPlayBtn.setEnabled(true);
        mRemotePlayBackFullPlayBtn.setBackgroundResource(R.drawable.play_full_pause_selector);
        mRemotePlayBackFullCaptureBtn.setEnabled(true);
        mRemotePlayBackFullRecordBtn.setEnabled(true);
        updateTimeBarUI();
        if (mAlarmStartTime != null) {
            timeBucketUIInit(mAlarmStartTime.getTimeInMillis(), mAlarmStopTime.getTimeInMillis());
        }

        updateSoundUI();

        startUpdateTimer();
    }

    private void checkRemotePlayBackFlow() {
//        if (mEZMediaPlayer != null && mRemotePlayBackFlowTv.getVisibility() == View.VISIBLE) {
//            // 更新流量数据
//            long streamFlow = 0l;//mj mEZMediaPlayer.getStreamFlow();
//
//            updateRemotePlayBackFlowTv(streamFlow);
//        }
    }

    private void updateRemotePlayBackFlowTv(long streamFlow) {
        long streamFlowUnit = streamFlow - mStreamFlow;
        if (streamFlowUnit < 0)
            streamFlowUnit = 0;
        float fKBUnit = (float) streamFlowUnit / (float) Constant.KB;
        String descUnit = String.format("%.2f k/s ", fKBUnit);
        String desc = null;
        float fMB = 0;
        if (streamFlow >= Constant.GB) {
            float fGB = (float) streamFlow / (float) Constant.GB;
            desc = String.format("%.2f GB ", fGB);
        } else {
            fMB = (float) streamFlow / (float) Constant.MB;
            desc = String.format("%.2f MB ", fMB);
        }

        // 显示流量
//        mRemotePlayBackFlowTv.setText(descUnit + " " + desc);
        mRemotePlayBackFullRateTv.setText(descUnit);
        mRemotePlayBackFullFlowTv.setText(desc);
        mStreamFlow = streamFlow;
    }


    private void handleStartRecordSuccess(String recordFilePath) {
        // 设置录像按钮为check状态
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRemotePlayBackRecordContainer, mRemotePlayBackRecordBtn,
                        mRemotePlayBackRecordStartBtn, 0, 90);
            } else {
                mRemotePlayBackRecordBtn.setVisibility(View.GONE);
                mRemotePlayBackRecordStartBtn.setVisibility(View.VISIBLE);
            }
            if (mAlarmStartTime != null) {
                mRemotePlayBackSmallRecordBtn.setVisibility(View.GONE);
                mRemotePlayBackSmallRecordStartBtn.setVisibility(View.VISIBLE);
            } else {
                mRemotePlayBackFullRecordBtn.setVisibility(View.GONE);
                mRemotePlayBackFullRecordStartBtn.setVisibility(View.VISIBLE);
            }
        } else {
            if (mAlarmStartTime != null) {
                if (!mIsOnStop) {
                    mRecordRotateViewUtil.applyRotation(mRemotePlayBackSmallRecordContainer,
                            mRemotePlayBackSmallRecordBtn, mRemotePlayBackSmallRecordStartBtn, 0, 90);
                } else {
                    mRemotePlayBackSmallRecordBtn.setVisibility(View.GONE);
                    mRemotePlayBackSmallRecordStartBtn.setVisibility(View.VISIBLE);
                }
            } else {
                if (!mIsOnStop) {
                    mRecordRotateViewUtil.applyRotation(mRemotePlayBackFullRecordContainer,
                            mRemotePlayBackFullRecordBtn, mRemotePlayBackFullRecordStartBtn, 0, 90);
                } else {
                    mRemotePlayBackFullRecordBtn.setVisibility(View.GONE);
                    mRemotePlayBackFullRecordStartBtn.setVisibility(View.VISIBLE);
                }
            }
            mRemotePlayBackRecordBtn.setVisibility(View.GONE);
            mRemotePlayBackRecordStartBtn.setVisibility(View.VISIBLE);
        }

        mRecordFilePath = recordFilePath;
        // 计时按钮可见
        mRemotePlayBackRecordLy.setVisibility(View.VISIBLE);
        mRemotePlayBackRecordTv.setText("00:00");
        mRecordSecond = 0;
    }

    private void handleRecordFail() {
        Utils.showToast(EZRemotePlayBackActivity.this, R.string.remoteplayback_record_fail);
        if (bIsRecording) {
            stopRemotePlayBackRecord();
            bIsRecording = !bIsRecording;
            return;
        }
    }

    private void handleCapturePictureSuccess(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        // 播放抓拍音频
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);

//        mRemotePlayBackCaptureRl.setVisibility(View.VISIBLE);   
        mCaptureDisplaySec = 0;
        try {
            mRemotePlayBackCaptureIv.setImageURI(Uri.parse(filePath));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        updateCaptureUI();
    }

    private void updateRemotePlayBackUI() {
        if (isFinishing()) {
            return;
        }
        if (mControlDisplaySec == 5) {
            mControlDisplaySec = 0;
            mRemotePlayBackControlRl.setVisibility(View.GONE);
            if (mAlarmStartTime != null) {
                mRemotePlayBackProgressLy.setVisibility(View.GONE);
                mRemotePlayBackProgressBar.setVisibility(View.VISIBLE);
                mLandscapeTitleBar.setVisibility(View.GONE);
            }
            mRemotePlayBackFullOperateBar.setVisibility(View.GONE);
            updateTimeBarUI();
        }

        updateCaptureUI();

        if (bIsRecording || mRecordFilePath != null) {
            updateRecordTime();
        }

        checkRemotePlayBackFlow();

        Calendar OSDTime = mEZMediaPlayer.getOSDTime();
        if (OSDTime != null) {
            mPlayTime = OSDTime.getTimeInMillis();
            if (mAlarmStartTime != null) {
                mRemotePlayBackProgressBar.setProgress((int) (mPlayTime - mPlayStartTime.getTimeInMillis()) / 1000);
                mRemotePlayBackSeekBar.setProgress((int) (mPlayTime - mPlayStartTime.getTimeInMillis()) / 1000);
                int progress = (int) (mPlayTime - mAlarmStartTime.getTimeInMillis());
                LogUtil.i(TAG, "updateRemotePlayBackUI mPlayTime:" + mPlayTime + "mAlarmStartTime:" + mAlarmStartTime.getTime()
                        + " mAlarmStartTime:" + mAlarmStartTime.getTimeInMillis() + " startPlayTime:" + mPlayStartTime.getTimeInMillis());
                LogUtil.i(TAG, "updateRemotePlayBackUI progress:" + progress);
                handlePlayProgress(OSDTime);
//                if(mPlayTime + 1000 > mAlarmStopTime.getTimeInMillis()) {
//                    handlePlayFinish();
//                }
            } else {
                float pos = mRemoteFileTimeBar.getScrollPosByPlayTime(mPlayTime, mOrientation);
                mRemotePlayBackTimeBar.smoothScrollTo((int) pos, 0);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                mRemotePlayBackTimeTv.setText(sdf.format(mPlayTime));
            }
        }
    }

    private void handlePlayProgress(Calendar osdTime) {
        long osd = osdTime.getTimeInMillis();
        long begin = mPlayStartTime.getTimeInMillis();

        int beginTimeClock = (int) ((osd - begin) / 1000);
        updateTimeBucketBeginTime(beginTimeClock);
    }

    private void updateTimeBucketBeginTime(int beginTimeClock) {
        String convToUIDuration = RemoteListUtil.convToUIDuration(beginTimeClock);
        mRemotePlayBackBeginTimeTv.setText(convToUIDuration);
    }

    private void timeBucketUIInit(long beginTime, long endTime) {
        int diffSeconds = (int) (endTime - beginTime) / 1000;
        String convToUIDuration = RemoteListUtil.convToUIDuration(diffSeconds);
        mRemotePlayBackBeginTimeTv.setText(RemoteListContant.VIDEO_DUAR_BEGIN_INIT);
        mRemotePlayBackEndTimeTv.setText(convToUIDuration);
    }

    //更新抓图/录像显示UI
    private void updateCaptureUI() {
        if (isFinishing()) {
            return;
        }
        if (mRemotePlayBackCaptureRl.getVisibility() == View.VISIBLE) {
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT || mRemotePlayBackTimeBarRl.getVisibility() == View.GONE) {
                if (mRemotePlayBackControlRl.getVisibility() == View.VISIBLE) {
                    mRemotePlayBackCaptureRlLp.setMargins(0, 0, 0, Utils.dip2px(this, mAlarmStartTime != null ? 60 : 40));
                } else {
                    mRemotePlayBackCaptureRlLp.setMargins(0, 0, 0, mAlarmStartTime != null ? Utils.dip2px(this, 2) : 0);
                }
                mRemotePlayBackCaptureRl.setLayoutParams(mRemotePlayBackCaptureRlLp);
            } else {
                if (mAlarmStartTime != null) {
                    if (mRemotePlayBackControlRl.getVisibility() == View.VISIBLE) {
                        mRemotePlayBackCaptureRlLp.setMargins(0, 0, 0, Utils.dip2px(this, 60));
                    } else {
                        mRemotePlayBackCaptureRlLp.setMargins(0, 0, 0, Utils.dip2px(this, 2));
                    }
                } else {
                    mRemotePlayBackCaptureRlLp.setMargins(0, 0, 0, Utils.dip2px(this, 87));
                }
                mRemotePlayBackCaptureRl.setLayoutParams(mRemotePlayBackCaptureRlLp);
            }
            if (mRemotePlayBackCaptureWatermarkIv.getTag() != null) {
                mRemotePlayBackCaptureWatermarkIv.setVisibility(View.VISIBLE);
                mRemotePlayBackCaptureWatermarkIv.setTag(null);
            }
        }
        if (mCaptureDisplaySec >= 4) {
            mCaptureDisplaySec = 0;
            mRemotePlayBackCaptureRl.setVisibility(View.GONE);
            mRemotePlayBackCaptureIv.setImageURI(null);
            mRemotePlayBackCaptureWatermarkIv.setTag(null);
            mRemotePlayBackCaptureWatermarkIv.setVisibility(View.GONE);
        }
    }

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

    // 处理密码错误
    private void handlePasswordError(int title_resid, int msg1_resid, int msg2_resid) {
        stopRemotePlayBack();
        setRemotePlayBackStopUI();

        if (mStatus == STATUS_START || mStatus == STATUS_PLAY) {
            return;
        }

        // 检查网络是否可用
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络
            setRemotePlayBackFailUI(getString(R.string.remoteplayback_searchfile_fail_for_network));
            return;
        }

        if (mEZMediaPlayer != null && mStatus == STATUS_PAUSE) {
            resumeRemotePlayBack();
            setRemotePlayBackSuccessUI();
            return;
        }

        mStatus = STATUS_START;
        setRemotePlayBackLoadingUI();
        updateLoadingProgress(0);

        Calendar seletedTime = getTimeBarSeekTime();
        LogUtil.d(TAG, "startRemotePlayBack:" + seletedTime);

        if (mCloudFileList == null) {
            searchCloudFileList(seletedTime);
            return;
        }

        if (mAlarmStartTime != null) {
        } else {
        }
    }

    private void handlePlaySuccess(Message msg) {
        LogUtil.d(TAG, "handlePlaySuccess:" + msg.arg1);
        mStatus = STATUS_PLAY;

        if (msg.arg1 != 0) {
            mRealRatio = (float) msg.arg2 / msg.arg1;
        } else {
            mRealRatio = Constant.LIVE_VIEW_RATIO;
        }
        setRemotePlayBackSvLayout();

        setRemotePlayBackSuccessUI();
        setRemotePlaySound();
    }

    private void setRemotePlayBackSvLayout() {
        // 设置播放窗口位置
        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo
                .getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);

        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRemotePlayBackSv.setLayoutParams(svLp);

        mRemotePlayBackTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
    }

    private void handlePlayFail(int errorCode, Object obj) {
        if (obj != null) {
            ErrorInfo errorInfo = (ErrorInfo) obj;
            LogUtil.d(TAG, "handlePlayFail:" + errorCode);
        }

        stopRemotePlayBack();

        String txt = null;
        // 判断返回的错误码
        switch (errorCode) {
            case ErrorCode.ERROR_WEB_SESSION_ERROR:
            case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
            case ErrorCode.ERROR_CAS_PLATFORM_CLIENT_NO_SIGN_RELEATED:
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
            case ErrorCode.ERROR_CAS_VERIFY_SESSION_ERROR:
                ActivityUtils.goToLoginAgain(EZRemotePlayBackActivity.this);
                return;
            case ErrorCode.ERROR_CAS_MSG_PU_NO_RESOURCE:
                txt = getString(R.string.remoteplayback_over_link);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE:
            case ErrorCode.ERROR_CAS_PLATFORM_CLIENT_REQUEST_NO_PU_FOUNDED:
                txt = getString(R.string.realplay_fail_device_not_exist);
                if (mStatus != STATUS_STOP) {
                    stopRemotePlayBack();
                }
                setRemotePlayBackFailUI(getString(R.string.camera_not_online));
                mIsOnStop = false;
                break;
            case ErrorCode.ERROR_WEB_DEVICE_SO_TIMEOUT:
                txt = getString(R.string.realplay_fail_connect_device);
                break;
            case ErrorCode.ERROR_INNER_DEVICE_NOT_EXIST:
                txt = getString(R.string.camera_not_online);
                break;
            case ErrorCode.ERROR_CAS_CONNECT_FAILED:
                txt = getString(R.string.remoteplayback_connect_server_error);
                break;
            case ErrorCode.ERROR_WEB_CODE_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_LOGIN, this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_OP_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_HARDWARE, this, this);
//                SecureValidate.secureValidateDialog(this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
            // 收到这两个错误码，可以弹出对话框，让用户输入密码后，重新取流预览
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {
                DataManager.getInstance().setDeviceSerialVerifyCode(mEZAlarmInfo.getDeviceSerial(),null);
                VerifyCodeInput.VerifyCodeInputDialog(this,this).show();
            }
            break;
            default:
                txt = Utils.getErrorTip(this, R.string.remoteplayback_fail, errorCode);
                break;
        }

        if (!TextUtils.isEmpty(txt)) {
            setRemotePlayBackFailUI(txt);
        } else {
            setRemotePlayBackStopUI();
        }

    }

    private void handlePlayFinish() {
        LogUtil.d(TAG, "handlePlayFinish");

        stopRemotePlayBack();

        if (mAlarmStartTime != null) {
            mRemotePlayBackProgressBar.setProgress(mRemotePlayBackProgressBar.getMax());
            mRemotePlayBackSeekBar.setProgress(mRemotePlayBackSeekBar.getMax());
            setRemotePlayBackFailUI(null);
        } else {
            setRemotePlayBackFailUI(null);
        }
    }

    private void startUpdateTimer() {
        stopUpdateTimer();
        // 开始录像计时
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                if ((mRemotePlayBackControlRl.getVisibility() == View.VISIBLE || mRemotePlayBackFullOperateBar.getVisibility() == View.VISIBLE)
                        && mControlDisplaySec < 5) {
                    mControlDisplaySec++;
                }
                if (mRemotePlayBackCaptureRl.getVisibility() == View.VISIBLE && mCaptureDisplaySec < 4) {
                    mCaptureDisplaySec++;
                }

                // 更新录像时间
                if (mRecordFilePath != null) {
                    // 更新录像时间
                    Calendar OSDTime = mEZMediaPlayer.getOSDTime();
                    if (OSDTime != null) {
                        String playtime = Utils.OSD2Time(OSDTime);
                        if (!TextUtils.equals(playtime, mRecordTime)) {
                            mRecordSecond++;
                            mRecordTime = playtime;
                        }
                    }
                }

                sendMessage(MSG_PLAY_UI_UPDATE, 0);
            }
        };
        // 延时1000ms后执行，1000ms执行一次
        mUpdateTimer.schedule(mUpdateTimerTask, 1000, 1000);
    }

    private void stopUpdateTimer() {
        mCaptureDisplaySec = 4;
        updateCaptureUI();
        mHandler.removeMessages(MSG_PLAY_UI_UPDATE);
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

    public void sendMessage(int msg, int arg1) {
        if (mHandler != null) {
            Message message = Message.obtain();
            message.what = msg;
            message.arg1 = arg1;
            mHandler.sendMessage(message);
        }
    }

    public void sendMessage(int msg, int arg1, Object obj) {
        if (mHandler != null) {
            Message message = Message.obtain();
            message.what = msg;
            message.arg1 = arg1;
            message.obj = obj;
            mHandler.sendMessage(message);
        }
    }

    private void dismissPopDialog(AlertDialog popDialog) {
        if (popDialog != null && popDialog.isShowing() && !isFinishing()) {
            try {
                popDialog.dismiss();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /* (non-Javadoc)
     * @see com.videogo.widget.TimeBarHorizontalScrollView.TimeScrollBarScrollListener#onScrollChanged(int, int, int, int, android.widget.HorizontalScrollView)
     */
    @Override
    public void onScrollChanged(int left, int top, int oldLeft, int oldTop, HorizontalScrollView scrollView) {
        Calendar startCalendar = mRemoteFileTimeBar.pos2Calendar(left, mOrientation);
        if (startCalendar != null) {
            mPlayTime = startCalendar.getTimeInMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            mRemotePlayBackTimeTv.setText(sdf.format(mPlayTime));
        }
    }

    /* (non-Javadoc)
     * @see com.videogo.widget.TimeBarHorizontalScrollView.TimeScrollBarScrollListener#onScrollStart(android.widget.HorizontalScrollView)
     */
    @Override
    public void onScrollStart(HorizontalScrollView scrollView) {
        if (mStatus != STATUS_STOP) {
            //stopRemotePlayBack();
        }
    }

    /* (non-Javadoc)
     * @see com.videogo.widget.TimeBarHorizontalScrollView.TimeScrollBarScrollListener#onScrollStop(android.widget.HorizontalScrollView)
     */
    @Override
    public void onScrollStop(HorizontalScrollView scrollView) {
        if (mStatus != STATUS_STOP) {
            //stopRemotePlayBack();
        }
        // startRemotePlayBack(getTimeBarSeekTime());
    }

    @Override
    public void onInputVerifyCode(final String verifyCode) {
        LogUtil.d(TAG, "verify code is " + verifyCode);

        LogUtil.d(TAG, "verify code is " + verifyCode);
        DataManager.getInstance().setDeviceSerialVerifyCode(mEZAlarmInfo.getDeviceSerial(),verifyCode);
        if (mEZMediaPlayer != null) {
            mEZMediaPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mEZAlarmInfo.getDeviceSerial()));
            startRemotePlayBack(getTimeBarSeekTime());
        }
    }
}
