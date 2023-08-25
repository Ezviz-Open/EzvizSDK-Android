package com.videogo.ui.playback;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceCloudServiceInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.DateTimeUtil;
import com.videogo.widget.TitleBar;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;

import static com.videogo.EzvizApplication.getOpenSDK;
import static com.videogo.util.DateTimeUtil.DAY_FORMAT;
import static com.videogo.util.DateTimeUtil.TIME_FORMAT;

public class EZAbroadCloudServiceExAct extends AppCompatActivity {

    private TitleBar mTitleBar;
    private ListView lv_api;
    private ArrayAdapter<String> apiAdapter;

    private String[] apiArray = {"01 - 检查国家是否支持云存储", "02 - 查询云存储设备通道套餐信息", "03 - 云存储功能暂停恢复", "04 - 查询某月中有视频文件的天(日期)",
            "05 - 按设备通道删除所有云存储录像片段", "06 - 按天增量查询云存储录像列表接口(概要信息)", "07 - 根据文件id批量查询详情信息接口", "08 - 删除云存储录像片段"};

    private EZCameraInfo mCameraInfo;


    /**
     * 录像列表概要信息
     */
    private List<String> videoList;

    public static void launch(Context context, EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        Intent intent = new Intent(context, EZAbroadCloudServiceExAct.class);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
        context.startActivity(intent);
    }

    public static void launch(Context context, String deviceSerial, int cameraNo) {
        EZCameraInfo cameraInfo = new EZCameraInfo();
        cameraInfo.setDeviceSerial(deviceSerial);
        cameraInfo.setCameraNo(cameraNo);
        launch(context, new EZDeviceInfo(), cameraInfo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abroad_cloud_service_ex);

        initData();
        initUI();
    }


    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mCameraInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
        }
    }

    private void initUI() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.cloud_storage_services_interface);
        mTitleBar.addBackButton(v -> onBackPressed());

        lv_api = findViewById(R.id.lv_api);
        apiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, apiArray);
        lv_api.setAdapter(apiAdapter);
        lv_api.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:// 检查国家是否支持云存储
                    Thread thr0 = new Thread(() -> {
                        try {
                            boolean isSupport = ((EZGlobalSDK) getOpenSDK()).isSupportCloundService();
                            runOnUiThread(() -> {
                                int resId = isSupport ? R.string.cloud_storage_services_is_supported :
                                        R.string.cloud_storage_services_is_not_supported;
                                Toast.makeText(getApplicationContext(), getApplicationContext().getString(resId),
                                        Toast.LENGTH_LONG).show();
                            });

                        } catch (BaseException e) {
                            e.printStackTrace();
                        }
                    });
                    thr0.start();
                    break;
                case 1:// 查询云存储设备通道套餐信息
                    Thread thr1 = new Thread(() -> {
                        try {
                            EZDeviceCloudServiceInfo serviceInfo =
                                    ((EZGlobalSDK) getOpenSDK()).getCloundDevicePackageInfo(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
                            String jsonString = new Gson().toJson(serviceInfo);
                            runOnUiThread(() -> {
                                popCopyDialog("查询云存储设备通道套餐信息", jsonString);
                            });

                        } catch (BaseException e) {
                            e.printStackTrace();
                        }
                    });
                    thr1.start();
                    break;
                case 2:// 云存储功能暂停恢复
                    AlertDialog.Builder copyDialog = new AlertDialog.Builder(this);
                    copyDialog.setTitle(R.string.pause_or_recovery_cloud_storage_function);
                    copyDialog.setPositiveButton(R.string.recovery, (dialog, which) -> {
                        Thread thr2 = new Thread(() -> {
                            try {
                                boolean isSuccess =
                                        ((EZGlobalSDK) getOpenSDK()).setCloundServiceActive(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), true);
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), isSuccess ? "恢复成功" : "恢复失败",
                                            Toast.LENGTH_LONG).show();
                                });
                            } catch (BaseException e) {
                                e.printStackTrace();
                            }
                        });
                        thr2.start();
                    });
                    copyDialog.setNegativeButton(R.string.pause, (dialog, which) -> {
                        Thread thr2 = new Thread(() -> {
                            try {
                                boolean isSuccess =
                                        ((EZGlobalSDK) getOpenSDK()).setCloundServiceActive(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), false);
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), isSuccess ? "暂停成功" : "暂停失败",
                                            Toast.LENGTH_LONG).show();
                                });
                            } catch (BaseException e) {
                                e.printStackTrace();
                            }
                        });
                        thr2.start();
                    });
                    copyDialog.show();
                    break;
                case 3:// 查询某月中有视频文件的天(日期)
                    Thread thr3 = new Thread(() -> {
                        try {
                            List<String> dayList =
                                    ((EZGlobalSDK) getOpenSDK()).getCloudVideoDays(mCameraInfo.getDeviceSerial(),
                                            mCameraInfo.getCameraNo(),
                                            DateTimeUtil.formatDateToString(DateTimeUtil.getNow(), "yyyyMM"));
                            String jsonString = new Gson().toJson(dayList);
                            runOnUiThread(() -> {
                                popCopyDialog("查询某月中有视频文件的天(日期)", jsonString);
                            });
                        } catch (BaseException e) {
                            e.printStackTrace();
                        }
                    });
                    thr3.start();
                    break;
                case 4:// 按设备通道删除所有云存储录像片段
                    Thread thr4 = new Thread(() -> {
                        try {
                            boolean isSuccess =
                                    ((EZGlobalSDK) getOpenSDK()).deleteAllCloudVideo(mCameraInfo.getDeviceSerial(),
                                            mCameraInfo.getCameraNo());
                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(), isSuccess ? "删除成功" : "删除失败",
                                        Toast.LENGTH_LONG).show();
                            });
                        } catch (BaseException e) {
                            if (e.getErrorInfo().errorCode == 150002) {
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "云储存视频删除成功，除了最后一个在录制的视频",
                                            Toast.LENGTH_LONG).show();
                                });
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                    thr4.start();
                    break;
                case 5:// 按天增量查询云存储录像列表接口(概要信息)
                    // 获取当前date
                    final Date searchDate = DateTimeUtil.getNow();
                    // 获取往前N小时date
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(searchDate);
                    cal.add(Calendar.HOUR_OF_DAY, -3);// 往前N小时
                    final Date maxCreateTime = cal.getTime();
                    Thread thr5 = new Thread(() -> {
                        try {
                            List<String> videoList =
                                    ((EZGlobalSDK) getOpenSDK()).getIncrCloudVideos(mCameraInfo.getDeviceSerial(),
                                            mCameraInfo.getCameraNo(),
                                            EZConstants.EZCloudVideoType.EZ_CLOUD_VIDEO_TYPE_ALL,
                                            DateTimeUtil.formatDateToString(searchDate, DAY_FORMAT),
                                            DateTimeUtil.formatDateToString(maxCreateTime, TIME_FORMAT));
                            String jsonString = new Gson().toJson(videoList);
                            runOnUiThread(() -> {
                                this.videoList = videoList;
                                popCopyDialog("按天增量查询云存储录像列表接口(概要信息)", jsonString);
                            });
                        } catch (BaseException e) {
                            e.printStackTrace();
                        }
                    });
                    thr5.start();
                    break;
                case 6:// 根据文件id批量查询详情信息接口
                    if (videoList == null || videoList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "请先调用录像概要接口，获取云存储录像概要信息", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Thread thr6 = new Thread(() -> {
                        try {
                            List<EZCloudRecordFile> videoList =
                                    ((EZGlobalSDK) getOpenSDK()).getCloudVideoDetails(mCameraInfo.getDeviceSerial(),
                                            mCameraInfo.getCameraNo(), this.videoList);
                            String jsonString = new Gson().toJson(videoList);
                            runOnUiThread(() -> {
                                popCopyDialog("根据文件id批量查询详情信息接口", jsonString);
                            });
                        } catch (BaseException e) {
                            e.printStackTrace();
                        }
                    });
                    thr6.start();
                    break;
                case 7:// 删除云存储录像片段
                    if (videoList == null || videoList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "请先调用录像概要接口，获取云存储录像概要信息", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Thread thr7 = new Thread(() -> {
                        try {
                            boolean isSuccess =
                                    ((EZGlobalSDK) getOpenSDK()).deleteCloudVideoFragment(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), this.videoList);
                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(), isSuccess ? "删除成功" : "删除失败",
                                        Toast.LENGTH_LONG).show();
                            });
                        } catch (BaseException e) {
                            e.printStackTrace();
                        }
                    });
                    thr7.start();
                    break;
                default:
                    break;
            }
        });
    }


    /**
     * 复制弹出框
     */
    private void popCopyDialog(String title, String resString) {
        AlertDialog.Builder copyDialog = new AlertDialog.Builder(this);
        copyDialog.setTitle(title);
        copyDialog.setMessage(resString);
        copyDialog.setPositiveButton(R.string.copy, (dialog, which) -> {
            // 获取剪贴版
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", resString);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show();
        });
        copyDialog.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // doNothing
        });
        copyDialog.show();
    }
}