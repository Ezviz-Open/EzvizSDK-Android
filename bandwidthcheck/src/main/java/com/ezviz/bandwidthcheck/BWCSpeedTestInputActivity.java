/**
 * 带宽检测设置页面
 */
package com.ezviz.bandwidthcheck;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ezviz.common.RootActivity;
import com.ezviz.common.TitleBar;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.EZConstants;
import com.videogo.util.LogUtil;

public class BWCSpeedTestInputActivity extends RootActivity implements View.OnClickListener {

    // region --- 全局变量|global variable ---

    private static final String TAG = BWCSpeedTestInputActivity.class.getSimpleName();
    public static final String EXTRA_BWCHECK_TYPE = "EXTRA_BWCHECK_TYPE";

    // token输入提示图标
    private ImageView tokenHintIv;
    // token输入框
    private EditText tokenEt;
    // token输入框清空按钮
    private ImageButton tokenClearIb;

    // 设备信息输入提示图标
    private ImageView deviceHintIv;
    // 设备信息输入框
    private EditText deviceEt;
    // 设备信息输入框清空按钮
    private ImageButton deviceClearIb;
    // 设备信息输入*号
    private TextView deviceAsteriskTv;

    // 开始测速按钮
    private Button startBtn;

    // 以下为上一个页面传递的数据
    private int bwCheckType;
    private String deviceSerial;
    private int cameraNo;
    // 以上为上一个页面传递的数据

    // endregion

    // region --- 页面启动方法|Page startup method ---

    public static void launch(Context context, int bwCheckType, String deviceSerial, int cameraNo) {
        Intent intent = new Intent(context, BWCSpeedTestInputActivity.class);
        intent.putExtra(EXTRA_BWCHECK_TYPE, bwCheckType);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, deviceSerial);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_NO, cameraNo);
        context.startActivity(intent);
    }

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bwc_speed_test_input);
        initData();
        initTitleBar();
        initUI();
        initListener();
    }

    // endregion

    // region --- Click点击事件|Click event ---

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            startSpeedTest();
        } else if (v.getId() == R.id.iv_token_hint) {
            openHintPopupWindow(tokenHintIv, "请至open.ys7.com登录控制台，进入[云直播>数据监控>带宽检测工具]，进行获取");
        } else if (v.getId() == R.id.iv_device_hint) {
            openHintPopupWindow(deviceHintIv, "IPC多目或NVR请输入通道号，IPC单目可以不用输入，默认为1通道");
        } else if (v.getId() == R.id.ib_token_clear) {
            tokenEt.setText("");
        } else if (v.getId() == R.id.ib_device_clear) {
            deviceEt.setText("");
            deviceSerial = "";
            cameraNo = 1;
        }

    }

    // endregion

    // region --- 自定义方法|custom method ---

    private void startSpeedTest() {
        String deviceInfo = deviceEt.getText().toString();
        if (!TextUtils.isEmpty(deviceInfo)) {
            if (deviceInfo.contains("@")) {
                String[] deviceInfoArray = deviceInfo.split("@");
                if (deviceInfoArray.length == 2) {
                    deviceSerial = deviceInfoArray[0];
                    try {
                        cameraNo = Integer.parseInt(deviceInfoArray[1]);
                    } catch (NumberFormatException e) {
                        showToast("请输入正确的IPC设备信息");
                        return;
                    }
                } else {
                    showToast("请输入正确的IPC设备信息");
                    return;
                }
            } else {
                deviceSerial = deviceInfo;
                cameraNo = 1;
            }
        }
        LogUtil.i(TAG, String.format(java.util.Locale.getDefault(),"bwCheckType=%s, token=%s, deviceSerial=%s, cameraNo=%d",
                bwCheckType, tokenEt.getText().toString(), deviceSerial, cameraNo));
        BWCSpeedTestActivity.launch(this, bwCheckType, tokenEt.getText().toString(), deviceSerial, cameraNo);
    }

    private void updateStartButtonState(boolean enabled) {
        startBtn.setEnabled(enabled);
        int backgroundRes = enabled ? R.drawable.selector_blue_8dp : R.drawable.bg_blue_8dp_disable;
        startBtn.setBackground(getResources().getDrawable(backgroundRes));
    }

    // endregion

    // region --- 打开提示框PopupWindow|Open the PopupWindow prompt box ---

    public void openHintPopupWindow(View parent, String hint) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.pop_hint, null, true);

        TextView hintTv = layoutView.findViewById(R.id.tv_hint);
        hintTv.setText(hint);

        int popupWindowWidth = tokenEt.getWidth() - 100;
        PopupWindow popupWindow = new PopupWindow(layoutView, popupWindowWidth,
                RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(parent, -popupWindowWidth / 2 + tokenHintIv.getWidth() / 2, -10);
        popupWindow.setOnDismissListener(() -> {
            LogUtil.i(TAG, "KEYCODE_BACK DOWN");
        });
        popupWindow.update();
    }

    // endregion

    // region --- init ---

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setTitle(bwCheckType == EZConstants.EZBWCheckType.EZBWCheckClientUpward ? "模拟设备带宽检测" : "网络测速");
        mTitleBar.addBackButton(v -> onBackPressed());
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.transparent));
        mTitleBar.addRightButton(R.mipmap.ic_bwc_history, v -> {
            BWCSpeedTestNetRecordActivity.launch(this, bwCheckType);
        });
    }

    private void initData() {
        Intent intent = getIntent();
        bwCheckType = intent.getIntExtra(EXTRA_BWCHECK_TYPE, 1);
        deviceSerial = intent.getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
        cameraNo = intent.getIntExtra(IntentConsts.EXTRA_CAMERA_NO, 1);
    }

    private void initUI() {
        tokenHintIv = findViewById(R.id.iv_token_hint);
        tokenEt = findViewById(R.id.et_token);
        tokenClearIb = findViewById(R.id.ib_token_clear);

        deviceHintIv = findViewById(R.id.iv_device_hint);
        deviceEt = findViewById(R.id.et_device);
        deviceClearIb = findViewById(R.id.ib_device_clear);
        deviceAsteriskTv = findViewById(R.id.tv_device_asterisk);

        startBtn = findViewById(R.id.btn_start);

        // 测试用，正式上线时删掉
//        tokenEt.setText("");
        if (!TextUtils.isEmpty(deviceSerial)) {
            deviceEt.setText(deviceSerial + "@" + cameraNo);
            deviceEt.setEnabled(false);
        }
        boolean isTokenValid = !tokenEt.getText().toString().isEmpty();
        boolean isDeviceValid = !TextUtils.isEmpty(deviceEt.getText().toString());
        tokenClearIb.setVisibility(isTokenValid ? View.VISIBLE : View.GONE);
        deviceClearIb.setVisibility(View.GONE);
        if (bwCheckType == EZConstants.EZBWCheckType.EZBWCheckClientUpward) {// 模拟设备带宽检测，设备信息必填
            deviceAsteriskTv.setVisibility(View.VISIBLE);
            updateStartButtonState(isTokenValid && isDeviceValid);
        } else {
            deviceAsteriskTv.setVisibility(View.GONE);
            updateStartButtonState(isTokenValid);
        }
    }

    private void initListener() {
        tokenHintIv.setOnClickListener(this);
        deviceHintIv.setOnClickListener(this);
        tokenClearIb.setOnClickListener(this);
        deviceClearIb.setOnClickListener(this);
        startBtn.setOnClickListener(this);
        tokenEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tokenClearIb.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (bwCheckType == EZConstants.EZBWCheckType.EZBWCheckClientUpward) {// 模拟设备带宽检测，设备信息必填
                    boolean isTokenValid = s.length() > 0;
                    boolean isDeviceValid = !TextUtils.isEmpty(deviceEt.getText().toString());
                    updateStartButtonState(isTokenValid && isDeviceValid);
                } else {// 客户端带宽检测，设备信息非必填
                    updateStartButtonState(s.length() > 0);
                }
            }
        });
        deviceEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                deviceClearIb.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                boolean isTokenValid = !TextUtils.isEmpty(tokenEt.getText().toString());
                if (bwCheckType == EZConstants.EZBWCheckType.EZBWCheckClientUpward) {// 模拟设备带宽检测，设备信息必填
                    boolean isDeviceValid = s.length() > 0;
                    updateStartButtonState(isTokenValid && isDeviceValid);
                } else {// 客户端带宽检测，设备信息非必填
                    updateStartButtonState(isTokenValid);
                }
            }

        });
    }

    // endregion
}