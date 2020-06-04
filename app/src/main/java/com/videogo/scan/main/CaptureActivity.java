/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.videogo.scan.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import ezviz.ezopensdkcommon.common.RootActivity;
import com.videogo.exception.BaseException;
import com.videogo.exception.ExtraException;
import com.videogo.scan.camera.CameraManager;
import com.videogo.ui.devicelist.SeriesNumSearchActivity;
import com.videogo.util.Base64;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalValidate;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.AddCameraGuideDialog.QuitNow;
import com.videogo.widget.TitleBar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.regex.Pattern;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.configwifi.AutoWifiNetConfigActivity;

//import com.videogo.device.DetectorType;
//import com.videogo.devicemgt.AddProbeActivity;
//import com.videogo.discovery.CommonWebActivity;
//import com.videogo.discovery.WebUtils;
//import com.videogo.main.RootActivity;
//import com.videogo.restful.VideoGoNetSDK;

public final class CaptureActivity extends RootActivity implements SurfaceHolder.Callback, QuitNow {

    public static final String PROBE_SEARCH = "probe_search";

    // 常量定义区
    private static final String TAG = CaptureActivity.class.getSimpleName();// 打印标识

    private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";

    private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";

    private static final String[] ZXING_URLS = {
        "http://zxing.appspot.com/scan", "zxing://scan/"};

    public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

    private static final float BEEP_VOLUME = 0.10f;// 读取成功后音效大小

    private static final long VIBRATE_DURATION = 200L;// 震动持续时间 单位：微秒

    public static final int REQUEST_CODE_CLOUD = 1;
    public static final int REQUEST_ADD_PROBE = 2;

    private ViewfinderView mViewfinderView = null;

    private TextView mTxtResult = null;

    private InactivityTimer mInactivityTimer = null;

    private MediaPlayer mMediaPlayer = null;

    private LocalValidate mLocalValidate = null;

    private final boolean mPlayBeep = false;

    private boolean mVibrate = false;

    private String mSerialNoStr = null;

    private String mSerialVeryCodeStr = null;

    private boolean mHasMeasured = false;

    private CameraManager cameraManager;

    private CaptureActivityHandler handler;

    private Result savedResultToShow;

    private boolean hasSurface;

    // private IntentSource source;

    private String sourceUrl;

    private Collection<BarcodeFormat> decodeFormats;

    private String characterSet;

    private boolean mHasShow = true;// 进来直接启动 ture 否则false

    private boolean mScanNow = false;

    private String deviceType = "";

    private TitleBar mTitleBar;

    private CheckBox ckbLight;

    private Button mBtnRight;
    // mA1DeviceSeries不为空 表示 从a1界面进来扫描
    private String mA1DeviceSeries;

    private PopupWindow mPromptWindow;;

    public Handler getHandler() {
        return handler;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        // 页面统计
//        super.setPageKey(HikStatPageConstant.HIK_STAT_PAGE_NEW_SCAN);
        super.onCreate(icicle);

        // Window window = getWindow();
        // window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.capture_activity);

        hasSurface = false;

        init();
        initTitleBar();
        findViews();
        setListener();
    }

    private void initTitleBar() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.ez_scan_title_txt);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBtnRight = mTitleBar.addRightButton(R.drawable.common_title_input_selector, new OnClickListener() {

            @Override
            public void onClick(View v) {
                addCameraBySN();
            }
        });

        // 做400ms保护 ，解决上个页面过来 点击过快 直接跑下一个界面问题。
        mBtnRight.setClickable(false);
        mBtnRight.postDelayed(new Runnable() {

            @Override
            public void run() {
                mBtnRight.setClickable(true);
            }
        }, 400);
    }

    private void init() {
        mInactivityTimer = new InactivityTimer(this);
        mLocalValidate = new LocalValidate();
        mA1DeviceSeries = getIntent().getStringExtra("a1_device_series");
        // default no light
        setPramaFrontLight(false);
    }

    private void findViews() {
        ckbLight = (CheckBox) findViewById(R.id.ckbLight);
        setmViewfinderView((ViewfinderView) findViewById(R.id.viewfinder_view));
        mTxtResult = (TextView) findViewById(R.id.txtResult);
        boolean isLightOn = getPramaFrontLight();
        ckbLight.setChecked(isLightOn);
        // mA1DeviceSeries不为空 表示 从a1界面进来扫描
        if (!TextUtils.isEmpty(mA1DeviceSeries)) {
            mTxtResult.setText(R.string.scan_search_probe_qrcode);
            mBtnRight.setVisibility(View.GONE);
        }
    }

    public static final String KEY_FRONT_LIGHT = "preferences_front_light";
    private boolean getPramaFrontLight() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CaptureActivity.this);
        boolean currentSetting = prefs.getBoolean(KEY_FRONT_LIGHT, false);
        return currentSetting;
    }

    private void setPramaFrontLight(boolean isChecked) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CaptureActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_FRONT_LIGHT, isChecked);
        editor.apply();
    }

    private void setListener() {
        // ckbLight.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        //
        // @Override
        // public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // setPramaFrontLight(isChecked);
        // // if (isChecked) {
        // // ckbLight.setText(R.string.scan_torch_on);
        // // } else {
        // // ckbLight.setText(R.string.scan_torch_off);
        // // }
        // reScan();
        // }
        // });
        ckbLight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setPramaFrontLight(!getPramaFrontLight());
                reScan();
            }
        });
        ViewTreeObserver vto = mTxtResult.getViewTreeObserver();

        vto.addOnPreDrawListener(/**
         * @ClassName: 匿名类
         * @Description: 用于监听在重绘前获得控件的大小按照屏幕的比例放置控件的位置
         * @author wangnanayf1
         * @date 2012-12-3 上午9:14:25
         */
        new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (mHasMeasured == false) {
                    DisplayMetrics dm = new DisplayMetrics();
                    // 取得窗口属性
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int windowsHeight = dm.heightPixels;
                    int windowsWidth = dm.heightPixels;
                    int moveLength = (int) ((windowsHeight - windowsWidth * 0.83f) / 2 - mTxtResult.getMeasuredHeight() / 2f);
                    if (moveLength > 0) {
                        // 移动控件的位置
                        mTxtResult.setPadding(0, 0, 0, moveLength);
                    }

                    mHasMeasured = true;
                    openPromptWindow(findViewById(R.id.flt_layout));
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager = new CameraManager(getApplication());
        // viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        getmViewfinderView().setCameraManager(cameraManager);

        handler = null;

        // resetStatusView();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            if (mHasShow && !mScanNow) {
                initCamera();
            }
        } else {
            // Install the mCallback and wait for surfaceCreated() to init the
            // camera.
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // beepManager.updatePrefs();

        mInactivityTimer.onResume();

        Intent intent = getIntent();

        // source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;

        if (intent != null) {

            String action = intent.getAction();
            String dataString = intent.getDataString();

            if (Intents.Scan.ACTION.equals(action)) {

                // Scan the formats the intent requested, and return the result
                // to the calling activity.
                // source = IntentSource.NATIVE_APP_INTENT;
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);

                if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
                    int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
                    int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
                    if (width > 0 && height > 0) {
                        cameraManager.setManualFramingRect(width, height);
                    }
                }

                // String customPromptMessage = intent
                // .getStringExtra(Intents.Scan.PROMPT_MESSAGE);

            } else if (dataString != null && dataString.contains(PRODUCT_SEARCH_URL_PREFIX)
                    && dataString.contains(PRODUCT_SEARCH_URL_SUFFIX)) {

                // Scan only products and send the result to mobile Product
                // Search.
                // source = IntentSource.PRODUCT_SEARCH_LINK;
                sourceUrl = dataString;
                decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;

            } else if (isZXingURL(dataString)) {

                // Scan formats requested in query string (all formats if none
                // specified).
                // If a return URL is specified, send the results there.
                // Otherwise, handle it ourselves.
                // source = IntentSource.ZXING_LINK;
                sourceUrl = dataString;
                Uri inputUri = Uri.parse(sourceUrl);
                // returnUrlTemplate =
                // inputUri.getQueryParameter(RETURN_URL_PARAM);
                decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);

            }

            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

        }
        ckbLight.setChecked(getPramaFrontLight());
    }

    private static boolean isZXingURL(String dataString) {
        if (dataString == null) {
            return false;
        }
        for (String url : ZXING_URLS) {
            if (dataString.startsWith(url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        mInactivityTimer.onPause();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();

        mHasShow = true;
        mScanNow = false;
    }

    @Override
    protected void onDestroy() {
        mInactivityTimer.shutdown();
        closePromptWindow();
        super.onDestroy();
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
        }
        if (!hasSurface) {
            hasSurface = true;
            if (mHasShow && !mScanNow) {
                initCamera();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void handleDecode(String resultString, Bitmap barcode) {
        mInactivityTimer.onActivity();
        playBeepSoundAndVibrate();

        if (resultString == null) {
            LogUtil.e(TAG, "handleDecode-> resultString is null");
            return;
        }
        LogUtil.e(TAG, "resultString = " + resultString);

        // mA1DeviceSeries不为空 表示 从a1界面进来扫描
        if (!TextUtils.isEmpty(mA1DeviceSeries)) {

            // www.ys7.com 096823495 ABCDEF CS-T1-A/12M TOO1
            // 字符集合
            if (!goAddProbe(resultString)) {

                if (isDeviceQRCode(resultString)) {
                    showToast(R.string.scan_probe_qrcode_error);
                } else {
                    // showToast(R.string.serial_number_is_illegal);
                    showDecodeFailedTip();
                }
                reScan();
            }
            return;
        }
        // 关注二维码名片地址
        // 例如：https://test.shipin7.com/h5/qrcode/intro?
        if (resultString.startsWith("https://") && resultString.contains("h5/qrcode/intro")) {
//            HikStat.onEvent(CaptureActivity.this, HikAction.ACTION_QRCODE_focus);
/*            Intent intent = new Intent(this, FollowActivity.class);
            intent.putExtra(IntentConstants.EXTRA_URL, resultString);
            startActivityForResult(intent, REQUEST_CODE_CLOUD);
*/
            // 设备二维码名片
        } else if (resultString.startsWith("http://") && resultString.contains("smart.jd.com")) {
            mSerialNoStr = "";
            mSerialVeryCodeStr = "";
            deviceType = "";
            try {
                String deviceInfoMarker = "$$$";
                String contentMarker = "f=";
                resultString = URLDecoder.decode(resultString, "UTF-8");
                // 验证url有效性 f=打头的为 需要的内容
                int contentIndex = resultString.indexOf(contentMarker);
                if (contentIndex < 0) {
                    mSerialNoStr = resultString;
                    isValidate();
                    return;
                }
                contentIndex += contentMarker.length();
                resultString = new String(Base64.decode(resultString.substring(contentIndex).trim()));
                int index = resultString.indexOf(deviceInfoMarker);
                // 二次判断有效性 $$$打头的为萤石信息
                if (index < 0) {
                    mSerialNoStr = resultString;
                    isValidate();
                    return;
                }
                index += deviceInfoMarker.length();
                resultString = resultString.substring(index);
                String[] infos = resultString.split("\r\n");
                if (infos.length >= 2) {
                    mSerialNoStr = infos[1];
                }
                if (infos.length >= 3) {
                    mSerialVeryCodeStr = infos[2];
                }
                if (infos.length >= 4) {
                    deviceType = infos[3];
                }
                isValidate();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // TODO 判断是否为探测器 1.5个字段 2.最后一个字段T或者K打头 且长度为4
            if (goAddProbe(resultString)) {
                return;
            }
            // 初始化数据
            mSerialNoStr = "";
            mSerialVeryCodeStr = "";
            deviceType = "";
            LogUtil.e(TAG, resultString);
            // CS-F1-1WPFR
            // CS-A1-1WPFR
            // CS-C1-1FPFR
            // resultString = "www.xxx.com\n456654855\nABCDEF\nCS-C3-21PPFR\n";
            // 字符集合
            String[] newlineCharacterSet = {
                "\n\r", "\r\n", "\r", "\n"};
            String stringOrigin = resultString;
            // 寻找第一次出现的位置
            int a = -1;
            int firstLength = 1;
            for (String string : newlineCharacterSet) {
                if (a == -1) {
                    a = resultString.indexOf(string);
                    if (a > stringOrigin.length() - 3) {
                        a = -1;
                    }
                    if (a != -1) {
                        firstLength = string.length();
                    }
                }
            }

            // 扣去第一次出现回车的字符串后，剩余的是第二行以及以后的
            if (a != -1) {
                resultString = resultString.substring(a + firstLength);
            }
            // 寻找最后一次出现的位置
            int b = -1;
            for (String string : newlineCharacterSet) {
                if (b == -1) {
                    b = resultString.indexOf(string);
                    if (b != -1) {
                        mSerialNoStr = resultString.substring(0, b);
                        firstLength = string.length();
                    }
                }
            }

            // 寻找遗失的验证码阶段
            if (mSerialNoStr != null && b != -1 && (b + firstLength) <= resultString.length()) {
                resultString = resultString.substring(b + firstLength);
            }

            // 再次寻找回车键最后一次出现的位置
            int c = -1;
            for (String string : newlineCharacterSet) {
                if (c == -1) {
                    c = resultString.indexOf(string);
                    if (c != -1) {
                        mSerialVeryCodeStr = resultString.substring(0, c);
                    }
                }
            }

            // 寻找CS-C2-21WPFR 判断是否支持wifi
            if (mSerialNoStr != null && c != -1 && (c + firstLength) <= resultString.length()) {
                resultString = resultString.substring(c + firstLength);
            }
            if (resultString != null && resultString.length() > 0) {
                deviceType = resultString;
            }

            if (b == -1) {
                mSerialNoStr = resultString;
            }

            if (mSerialNoStr == null) {
                mSerialNoStr = stringOrigin;
            }
            LogUtil.d(TAG, "mSerialNoStr = " + mSerialNoStr + ",mSerialVeryCodeStr = " + mSerialVeryCodeStr
                    + ",deviceType = " + deviceType);
            // 判断是不是9位
            isValidate();
        }
        // else {
        // 传感器添加 暂不实现
        // 网页/R1登录 暂不实现
        // 无法识别
        // handleLocalValidateSerialNoFail(ExtraException.SERIALNO_IS_ILLEGAL);
        // }
    }

    private void showDecodeFailedTip() {
        if (isFinishing()) {
            return;
        }

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView view = new TextView(this);
        view.setText(R.string.unable_identify_two_dimensional_code_tip);
        view.setTextSize(20);
        view.setTextColor(getResources().getColor(R.color.common_text));
        view.setBackgroundResource(R.drawable.decode_failed_tip_bg);
        view.setPadding(Utils.dip2px(this, 6), Utils.dip2px(this, 16), Utils.dip2px(this, 6), Utils.dip2px(this, 16));
        toast.setView(view);
        toast.show();
    }

    private boolean isDeviceQRCode(String qrCode) {
        // 字符集合
        String[] newlineCharacterSet = {
            "\n\r", "\r\n", "\r", "\n"};
        String[] tempStr;
        for (String sp : newlineCharacterSet) {
            tempStr = qrCode.split(sp);
            if (tempStr != null && tempStr.length >= 2) {
                try {
                    mLocalValidate.localValidatSerialNo(tempStr[1]);
                    return true;
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return false;
    }

    private boolean goAddProbe(String stringOrigin) {
        String[] newlineCharacterSet = {
            "\n\r", "\r\n", "\r", "\n"};

        String[] tempStr;
//        for (String sp : newlineCharacterSet) {
//
//            tempStr = stringOrigin.split(sp);
//            if (tempStr != null && tempStr.length == 5 && tempStr[4].length() == 4
//                    && DetectorType.isDetectorType(tempStr[4])) {
//                // 启动探测器关联界面
//                Intent intent = new Intent(CaptureActivity.this, AddProbeActivity.class);
//                intent.putExtra("probe_series", tempStr[1]);
//                intent.putExtra("probe_very_code", tempStr[2]);
//                intent.putExtra("probe_ex", tempStr[3]);
//                intent.putExtra("probe_type", tempStr[4]);
//                intent.putExtra("a1_device_series", mA1DeviceSeries);
//                startActivityForResult(intent, REQUEST_ADD_PROBE);
//                return true;
//            }
//        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CLOUD) {
            setResult(REQUEST_CODE_CLOUD, data);
            finish();
        } else if (requestCode == REQUEST_ADD_PROBE && resultCode == -1) {
            setResult(-1, data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void isValidate() {
        mLocalValidate = new LocalValidate();
        try {
            mLocalValidate.localValidatSerialNo(mSerialNoStr);
            LogUtil.i(TAG, mSerialNoStr);
        } catch (BaseException e) {
            handleLocalValidateSerialNoFail(e.getErrorCode());
            LogUtil.e(TAG, "searchCameraBySN-> local validate serial no fail, errCode:" + e.getErrorCode());
            return;
        }

        if (!ConnectionDetector.isNetworkAvailable(this)) {
            showToast(R.string.query_camera_fail_network_exception);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("type", 1);
        bundle.putString("SerialNo", mSerialNoStr);
        bundle.putString("very_code", mSerialVeryCodeStr);
        bundle.putString(AutoWifiNetConfigActivity.DEVICE_TYPE, deviceType);
        LogUtil.d(TAG, "very_code:" + mSerialVeryCodeStr);
        Intent intent = new Intent(CaptureActivity.this, SeriesNumSearchActivity.class);
        intent.putExtras(bundle);
        CaptureActivity.this.startActivity(intent);

        mScanNow = false;
    }

    private void handleLocalValidateSerialNoFail(int errCode) {
        switch (errCode) {
            case ExtraException.SERIALNO_IS_NULL:
                showToast(R.string.serial_number_is_null);
                break;
            case ExtraException.SERIALNO_IS_ILLEGAL:
                // showToast(R.string.serial_number_is_illegal);
                showDecodeFailedTip();
                break;
            default:
                showToast(R.string.serial_number_error, errCode);
                LogUtil.e(TAG, "handleLocalValidateSerialNoFail-> unkown error, errCode:" + errCode);
                break;
        }
        reScan();
    }

    private void reScan() {
        onPause();
        onResume();
    }

    private void initCamera() {
        try {

            // 声音效果
            initBeepSound();
            // 是否震动
            mVibrate = true;
            mSerialNoStr = null;

            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            LogUtil.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            LogUtil.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        showToast(R.string.open_camera_fail);

    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        // resetStatusView();
    }

    public void drawViewfinder() {
        getmViewfinderView().drawViewfinder();
    }

    private void initBeepSound() {
        if (mPlayBeep) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mMediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                mMediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (mPlayBeep && mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        if (mVibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private final OnCompletionListener beepListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    /*
     * (non-Javadoc) 实现对界面点击的响应
     */

    private void addCameraBySN() {
        // type -0 手动输入序列号， type - 1二维码扫描
        Bundle bundle = new Bundle();
        bundle.putInt("type", 0);
        Intent intent = new Intent(CaptureActivity.this, SeriesNumSearchActivity.class);
        intent.putExtras(bundle);
        CaptureActivity.this.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void quitNow() {
        finish();
    }

    @Override
    public void howToConnect() {
        mHasShow = true;
//        WebUtils.openDeviceConnectHelp(this);
    }

    @Override
    public void scanNow() {
        if (hasSurface && !mHasShow) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            mHasShow = true;
            mScanNow = true;
            initCamera();
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.fade_down);
    }

    public ViewfinderView getmViewfinderView() {
        return mViewfinderView;
    }

    public void setmViewfinderView(ViewfinderView mViewfinderView) {
        this.mViewfinderView = mViewfinderView;
    }

    public static boolean isNumeric(String str) {
        if (null == str) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    //mj
    private void openPromptWindow(View parent) {}

    private void closePromptWindow() {
        try {
            if (mPromptWindow != null && mPromptWindow.isShowing()) {
                mPromptWindow.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        // default no light
        setPramaFrontLight(false);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        // default no light
        setPramaFrontLight(false);
    }

}
