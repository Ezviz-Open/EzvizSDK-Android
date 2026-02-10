/**
 * 带宽检测进程页面
 */
package com.ezviz.bandwidthcheck;

import static com.ezviz.bandwidthcheck.BWCSpeedTestInputActivity.EXTRA_BWCHECK_TYPE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ezviz.common.RootActivity;
import com.ezviz.common.TitleBar;
import com.videogo.bandwidthcheck.EZBWCheckManager;
import com.videogo.bandwidthcheck.EZBWCheckResult;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.util.LogUtil;

public class BWCSpeedTestActivity extends RootActivity implements View.OnClickListener {

    // region --- 全局变量|global variable ---

    private static final String TAG = BWCSpeedTestActivity.class.getSimpleName();
    private static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    // 检测15秒
    public static final int checkTime = 15;

    // 设备信息视图
    private LinearLayout deviceLL;
    // 设备信息，设备序列号@通道号
    private TextView deviceTv;
    // 停止检测按钮
    private Button stopBtn;

    // 测速中视图
    private RelativeLayout speedTestingRl;
    // 测速失败视图
    private LinearLayout speedTestFailLl;
    // 测速失败错误码
    private TextView speedTestFailErrorTv;
    // 测速失败原因
    private TextView speedTestFailReasonTv;
    // 重新测速按钮
    private Button retryBtn;

    // 停止检测对话框
    private AlertDialog stopAlertDialog = null;

    // 以下为上一个页面传递的数据
    private int bwCheckType;
    private String token;
    private String deviceSerial;
    private int cameraNo;
    // 以上为上一个页面传递的数据

    // endregion

    // region --- 页面启动方法|Page startup method ---

    public static void launch(Context context, int bwCheckType, String token, String deviceSerial, int cameraNo) {
        Intent intent = new Intent(context, BWCSpeedTestActivity.class);
        intent.putExtra(EXTRA_BWCHECK_TYPE, bwCheckType);
        intent.putExtra(EXTRA_TOKEN, token);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, deviceSerial);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_NO, cameraNo);
        context.startActivity(intent);
    }

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bwc_speed_test);
        initData();
        initTitleBar();
        initUI();
        initListener();

        new Thread(() -> startClientSpeedTest()).start();
    }

    // endregion

    // region --- Click点击事件|Click event ---

    @Override
    public void onBackPressed() {
        if (speedTestingRl.getVisibility() == View.VISIBLE) {// 带宽检测中，需要弹框提示
            showStopAlertDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_stop) {
            showStopAlertDialog();
        } else if (v.getId() == R.id.btn_retry) {
            speedTestingRl.setVisibility(View.VISIBLE);
            speedTestFailLl.setVisibility(View.GONE);
            new Thread(() -> startClientSpeedTest()).start();
        }
    }

    // endregion

    // region --- 自定义方法|custom method ---

    private void startClientSpeedTest() {
        try {
            EZOpenSDK.getInstance().startBWCheck(bwCheckType, deviceSerial, cameraNo, checkTime, token,
                    new EZBWCheckManager.EZBWCheckResultCallback() {
                @Override
                public void onBWCheckResult(EZBWCheckResult result) {
                    super.onBWCheckResult(result);
                    if (result.result == 0) {
                        LogUtil.i(TAG, "startClientSpeedTest success");
                        runOnUiThread(() -> {
                            if (stopAlertDialog != null && stopAlertDialog.isShowing()) {
                                stopAlertDialog.dismiss();
                            }
                            BWCSpeedTestResultActivity.launch(BWCSpeedTestActivity.this, result, true);
                            finish();
                        });
                    } else {
                        setSpeedTestFailedUI(result.result, "网络异常，请稍后重试");
                    }
                }
            });
        } catch (BaseException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                if (e.getErrorInfo().errorCode == 400902) {
                    showToast("账号凭证失效，请联系服务商重新提供");
                    finish();
                } else {
                    ErrorInfo errorInfo = e.getErrorInfo();
                    setSpeedTestFailedUI(errorInfo.errorCode, errorInfo.description);
                }
            });
        }
    }

    private void setSpeedTestFailedUI(int errorCode, String errorReason) {
        LogUtil.e(TAG, "startClientSpeedTest failed");
        runOnUiThread(() -> {
            speedTestFailLl.setVisibility(View.VISIBLE);
            speedTestingRl.setVisibility(View.GONE);
            speedTestFailErrorTv.setText("检测失败，错误码：" + errorCode);
            speedTestFailReasonTv.setText(errorReason);
        });
    }

    // endregion

    // region --- 停止检测PopupWindow|Stop detecting PopupWindow ---

    public void showStopAlertDialog() {
        if (stopAlertDialog != null && stopAlertDialog.isShowing()) {
            stopAlertDialog.dismiss();
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BWCSpeedTestActivity.this);
        ViewGroup msgLayoutVg = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_dialog,
                null);
        TextView cancelTv = msgLayoutVg.findViewById(R.id.tv_cancel);
        cancelTv.setOnClickListener(v -> stopAlertDialog.dismiss());
        TextView stopTv = msgLayoutVg.findViewById(R.id.tv_stop);
        stopTv.setOnClickListener(v -> {
            EZOpenSDK.getInstance().stopBWCheck();
            stopAlertDialog.dismiss();
            finish();
        });

        dialogBuilder.setView(msgLayoutVg);
        dialogBuilder.setCancelable(false);
        stopAlertDialog = dialogBuilder.show();
    }

    // endregion

    // region --- init ---

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle(bwCheckType == EZConstants.EZBWCheckType.EZBWCheckClientUpward ? "模拟设备带宽检测" : "网络测速");
        mTitleBar.addBackButton(v -> {
            onBackPressed();
        });
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    private void initData() {
        Intent intent = getIntent();
        bwCheckType = intent.getIntExtra(EXTRA_BWCHECK_TYPE, 1);
        token = intent.getStringExtra(EXTRA_TOKEN);
        deviceSerial = intent.getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
        cameraNo = intent.getIntExtra(IntentConsts.EXTRA_CAMERA_NO, 1);
    }

    private void initUI() {
        deviceLL = findViewById(R.id.ll_device);
        deviceTv = findViewById(R.id.tv_device);
        stopBtn = findViewById(R.id.btn_stop);
        speedTestingRl = findViewById(R.id.rl_speed_testing);
        speedTestFailLl = findViewById(R.id.ll_speed_test_fail);
        speedTestFailErrorTv = findViewById(R.id.tv_speed_test_fail_error);
        speedTestFailReasonTv = findViewById(R.id.tv_speed_test_fail_reason);
        retryBtn = findViewById(R.id.btn_retry);
        if (!TextUtils.isEmpty(deviceSerial)) {
            deviceLL.setVisibility(View.VISIBLE);
            deviceTv.setText(deviceSerial + "@" + cameraNo);
        } else {
            deviceLL.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        stopBtn.setOnClickListener(this);
        retryBtn.setOnClickListener(this);
    }

    // endregion
}