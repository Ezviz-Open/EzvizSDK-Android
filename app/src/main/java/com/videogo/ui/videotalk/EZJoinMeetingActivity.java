/**
 * 视频通话信息设置页面
 */
package com.videogo.ui.videotalk;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.ez.baselib.utils.LogUtil;
import com.ez.basertc.RTCConstant;
import com.ez.ertcengine.ERTCEngine;
import com.videogo.EzvizApplication;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.global.GlobalKit;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.util.LocalInfo;
import com.videogo.widget.TitleBar;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

public class EZJoinMeetingActivity extends RootActivity {

    // region --- 全局变量|global variable ---

    protected static final String TAG = EZJoinMeetingActivity.class.getSimpleName();

    private EditText appIdEt;
    private EditText ertcClientTokenEt;
    private EditText ertcDeviceTokenEt;
    private EditText roomIdEt;
    private EditText userIdEt;

    private EZCameraInfo mCameraInfo = null;

    private boolean isClientCallDevice;// true:客户端呼叫设备端 false:设备端呼叫，客户端接听

    private boolean isFromPermissionSetting;// true为应用权限管理返回

    // endregion

    // region --- 生命周期|Life Cycle ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_meeting);
        initData();
        initView();
        if (GlobalKit.isJCTest) {
            appIdEt.setText("807c2745e644485fa192324b4c9e620d");
            ertcClientTokenEt.setText("tk.oCA4MDdjMjc0NWU2NDQ0ODVmYTE5MjMyNGI0YzllNjIwZABQeyJFUlRDX0lORk8iOnsic3RyUm9vbUlkIjoidGVzdHJvb20iLCJjdXN0b21JZCI6IjE1ODY5MDM1MDAwIiwicGVybWlzc2lvbiI6IjcifX0AAAAAaXxs3wABUYAscTVCUGJ2UTJWZE80bERqTUNPKzNXeFI2UFRyNHJrN09JOXlUaGtzOGFTTT0eEHhXViBna02kndkoSOS0-Mg_");
            ertcDeviceTokenEt.setText("tk.oCA4MDdjMjc0NWU2NDQ0ODVmYTE5MjMyNGI0YzllNjIwZABOeyJFUlRDX0lORk8iOnsic3RyUm9vbUlkIjoidGVzdHJvb20iLCJjdXN0b21JZCI6IkJFNjM1MjMyNCIsInBlcm1pc3Npb24iOiI3In19AAAAAGl8bN8AAVGALG94d1NiL3lGRjJEVXEzWHl1Y3JnbG1RaEV4dm9xRlJqNkdrMzZkejZXS1U9HhB4V1YgZ2tNpJ3ZKEjktPzI");
            roomIdEt.setText("testroom");
            userIdEt.setText("15869035000");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromPermissionSetting) {
            checkPermissions();
            isFromPermissionSetting = false;
        }
    }

    // endregion

    // region --- Click点击事件|Click event ---

    /**
     * 客户端发起视频通话
     */
    public void onClickVideoTalk(View view) {
        if (!checkVideoTalkParams()) {
            return;
        }
        isClientCallDevice = true;
        checkPermissions();
    }

    /**
     * 设备端端发起视频通话后，客户端接听
     */
    public void onClickAnswerDeviceCall(View view) {
        if (!checkVideoTalkParams()) {
            return;
        }
        isClientCallDevice = false;
        checkPermissions();
    }

    /**
     * 设备端端发起视频通话后，客户端拒接
     */
    public void onClickRejectDeviceCall(View view) {
        if (!checkVideoTalkParams()) {
            return;
        }
        new Thread(() -> {
            try {
                boolean ret = EzvizApplication.getOpenSDK().rejectVideoCallReqFromDevice(appIdEt.getText().toString(), ertcDeviceTokenEt.getText().toString(),
                        roomIdEt.getText().toString(), mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), userIdEt.getText().toString());
                if (ret) {
                    showToast("拒接成功");
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onClickClearEditText(View view) {
        int id = view.getId();
        if (id == R.id.btn_appId_clear) {
            appIdEt.setText("");
        } else if (id == R.id.btn_ertc_client_token_clear) {
            ertcClientTokenEt.setText("");
        } else if (id == R.id.btn_ertc_device_token_clear) {
            ertcDeviceTokenEt.setText("");
        } else if (id == R.id.btn_roomId_clear) {
            roomIdEt.setText("");
        } else if (id == R.id.btn_userId_clear) {
            userIdEt.setText("");
        }
    }

    private boolean checkVideoTalkParams() {
        String appId = appIdEt.getText().toString();
        String ertcClientToken = ertcClientTokenEt.getText().toString();
        String ertcDeviceToken = ertcDeviceTokenEt.getText().toString();
        String roomId = roomIdEt.getText().toString();
        String userId = userIdEt.getText().toString();
        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(ertcClientToken) || TextUtils.isEmpty(ertcDeviceToken) || TextUtils.isEmpty(roomId) || TextUtils.isEmpty(userId)) {
            showToast("信息不能为空");
            return false;
        }
        return true;
    }

    // endregion

    // region --- ERTC初始化|ERTC initialization ---

    private void initErtcEngine() {
        String appId = appIdEt.getText().toString();
        String ertcClientToken = ertcClientTokenEt.getText().toString();
        String ertcDeviceToken = ertcDeviceTokenEt.getText().toString();
        String roomId = roomIdEt.getText().toString();
        String userId = userIdEt.getText().toString();
        ErtcHelper.initERTCEngine(EZJoinMeetingActivity.this, appId, new ERTCEngine.OnInitListener() {
            @Override
            public void onInitialization(ERTCEngine engine) {
                LogUtil.d(TAG, "Init OK");
                ErtcHelper.ertcEngine = engine;
//                ErtcHelper.ertcEngine.setERTCDebugAudioLog(true);

                RTCConstant.EnterParam localUserInfo = new RTCConstant.EnterParam();
                localUserInfo.roomId = roomId;
                localUserInfo.userId = userId;
                localUserInfo.token = ertcClientToken;
                localUserInfo.appId = appId;

                ErtcRoomActivity.launch(EZJoinMeetingActivity.this, localUserInfo, mCameraInfo, ertcDeviceToken,
                        isClientCallDevice,true, true);
            }

            @Override
            public void onError(int code) {
                LogUtil.d(TAG, "Init failed error:" + code);
            }
        });
    }

    // endregion

    // region --- init ---

    private void initData() {
        // 初始化mLocalInfo，记录屏幕尺寸信息
        LocalInfo mLocalInfo = LocalInfo.getInstance();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        if (mLocalInfo != null) {
            mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
            mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
        }
        mCameraInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
    }

    private void initView() {
        initTitleBar();

        appIdEt = findViewById(R.id.et_appId);
        ertcClientTokenEt = findViewById(R.id.et_ertc_client_token);
        ertcDeviceTokenEt = findViewById(R.id.et_ertc_device_token);
        roomIdEt = findViewById(R.id.et_roomId);
        userIdEt = findViewById(R.id.et_userId);
    }

    private void initTitleBar() {
        TitleBar mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.addBackButton(view -> finish());
        mTitleBar.setTitle("视频通话");
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
        if (!(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.CAMERA);
        }
        if (!(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.RECORD_AUDIO);
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
        AlertDialog dialog =
                new AlertDialog.Builder(this).setMessage("应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。").setPositiveButton("去设置",
                        (dialog1, which) -> {
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
        // 有相机、麦克风权限后，初始化ERTCEngine
        initErtcEngine();
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