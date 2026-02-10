package com.videogo.ui.others;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.videogo.global.GlobalKit;
import com.videogo.util.Utils;
import com.videogo.widget.TitleBar;

import ezviz.ezopensdk.R;
import ezviz.ezopensdkcommon.common.RootActivity;

import static com.videogo.EzvizApplication.getOpenSDK;

public class EZTKTokenActivity extends RootActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eztktoken);
        initTitleBar();
    }

    /**
     * 设置设备级token
     */
    public void onDeviceTokenSetClick(View view) {
        TKTokenInputDialog2(this, "设备级tkToken设置", (deviceSerial, tkToken) -> {
            getOpenSDK().setDeviceToken(deviceSerial, tkToken);
        }).show();
    }

    /**
     * 设置设备通道级token
     */
    public void onDeviceCameraNoTokenSetClick(View view) {
        TKTokenInputDialog4(this, "设备级通道级tkToken设置", (deviceSerial, cameraNo, globalTkToken, videoTkToken) -> {
            getOpenSDK().setDeviceToken(deviceSerial, cameraNo, globalTkToken, videoTkToken);
        }).show();
    }

    /**
     * 设置预览token
     */
    public void onPreviewTokenSetClick(View view) {
        TKTokenInputDialog1(this, "预览tkToken设置", tkToken -> {
            GlobalKit.getInstance().previewStreamToken = tkToken;
        }).show();
    }

    /**
     * 设置对讲token
     */
    public void onTalkTokenSetClick(View view) {
        TKTokenInputDialog1(this, "对讲tkToken设置", tkToken -> {
            GlobalKit.getInstance().talkStreamToken = tkToken;
        }).show();
    }

    /**
     * 设置回放token
     */
    public void onPlaybackTokenSetClick(View view) {
        TKTokenInputDialog1(this, "回放tkToken设置", tkToken -> {
            GlobalKit.getInstance().playbackStreamToken = tkToken;
        }).show();
    }

    /**
     * 一个输入框的Dialog
     */
    public static AlertDialog TKTokenInputDialog1(final Context context, final String title,
                                                  final tkTokenInputListener1 l) {

        LinearLayout Layout = new LinearLayout(context);
        FrameLayout.LayoutParams layoutLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Layout.setOrientation(LinearLayout.VERTICAL);
        Layout.setLayoutParams(layoutLp);

        final EditText tkTokenEdit = new EditText(context);
        LinearLayout.LayoutParams tkTokenLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tkTokenLp.leftMargin = Utils.dip2px(context, 10);
        tkTokenLp.rightMargin = Utils.dip2px(context, 10);
        tkTokenEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tkTokenEdit.setSingleLine(true);
        tkTokenEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        tkTokenEdit.setHint("请输入tkToken");
        Layout.addView(tkTokenEdit, tkTokenLp);

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(Layout);
        builder.setPositiveButton(android.R.string.cancel, (dialog, which) -> Toast.makeText(context,
                "The tkToken " + "is" + " empty", Toast.LENGTH_LONG).show());
        builder.setNegativeButton(android.R.string.ok, (dialog, which) -> {
            final String tkToken = tkTokenEdit.getText().toString();
            if (TextUtils.isEmpty(tkToken)) {
                Toast.makeText(context, "The tkToken is empty", Toast.LENGTH_LONG).show();
                return;
            }
            l.onInputTKToken(tkToken);
        });
        AlertDialog alertDialog = null;
        alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return alertDialog;
    }

    public interface tkTokenInputListener1 {

        void onInputTKToken(final String tkToken);
    }

    /**
     * 两个输入框的Dialog
     */
    public static AlertDialog TKTokenInputDialog2(final Context context, final String title,
                                                  final tkTokenInputListener2 l) {

        LinearLayout Layout = new LinearLayout(context);
        FrameLayout.LayoutParams layoutLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Layout.setOrientation(LinearLayout.VERTICAL);
        Layout.setLayoutParams(layoutLp);

        final EditText deviceSerialEdit = new EditText(context);
        LinearLayout.LayoutParams deviceSerialLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deviceSerialLp.leftMargin = Utils.dip2px(context, 10);
        deviceSerialLp.rightMargin = Utils.dip2px(context, 10);
        deviceSerialEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        deviceSerialEdit.setSingleLine(true);
        deviceSerialEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        deviceSerialEdit.setHint("请输入设备序列号");
        Layout.addView(deviceSerialEdit, deviceSerialLp);

        final EditText tkTokenEdit = new EditText(context);
        LinearLayout.LayoutParams tkTokenLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tkTokenLp.leftMargin = Utils.dip2px(context, 10);
        tkTokenLp.rightMargin = Utils.dip2px(context, 10);
        tkTokenEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tkTokenEdit.setSingleLine(true);
        tkTokenEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        tkTokenEdit.setHint("请输入tkToken");
        Layout.addView(tkTokenEdit, tkTokenLp);

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(Layout);
        builder.setPositiveButton(android.R.string.cancel, (dialog, which) -> Toast.makeText(context,
                "The tkToken " + "is" + " empty", Toast.LENGTH_LONG).show());
        builder.setNegativeButton(android.R.string.ok, (dialog, which) -> {
            final String deviceSerial = deviceSerialEdit.getText().toString();
            final String tkToken = tkTokenEdit.getText().toString();
            if (TextUtils.isEmpty(deviceSerial)) {
                Toast.makeText(context, "The deviceSerial is empty", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(tkToken)) {
                Toast.makeText(context, "The tkToken is empty", Toast.LENGTH_LONG).show();
                return;
            }
            l.onInputTKToken(deviceSerial, tkToken);
        });
        AlertDialog alertDialog = null;
        alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return alertDialog;
    }

    public interface tkTokenInputListener2 {

        void onInputTKToken(final String deviceSerial, final String tkToken);
    }

    /**
     * 四个输入框的Dialog
     */
    public static AlertDialog TKTokenInputDialog4(final Context context, final String title,
                                                  final tkTokenInputListener4 l) {

        LinearLayout Layout = new LinearLayout(context);
        FrameLayout.LayoutParams layoutLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Layout.setOrientation(LinearLayout.VERTICAL);
        Layout.setLayoutParams(layoutLp);

        final EditText deviceSerialEdit = new EditText(context);
        LinearLayout.LayoutParams deviceSerialLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deviceSerialLp.leftMargin = Utils.dip2px(context, 10);
        deviceSerialLp.rightMargin = Utils.dip2px(context, 10);
        deviceSerialEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        deviceSerialEdit.setSingleLine(true);
        deviceSerialEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        deviceSerialEdit.setHint("请输入设备序列号");
        Layout.addView(deviceSerialEdit, deviceSerialLp);

        final EditText cameraNoEdit = new EditText(context);
        LinearLayout.LayoutParams cameraNoLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        cameraNoLp.leftMargin = Utils.dip2px(context, 10);
        cameraNoLp.rightMargin = Utils.dip2px(context, 10);
        cameraNoEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        cameraNoEdit.setSingleLine(true);
        cameraNoEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        cameraNoEdit.setHint("请输入设备通道号，不支持*");
        Layout.addView(cameraNoEdit, cameraNoLp);

        final EditText globalTkTokenEdit = new EditText(context);
        LinearLayout.LayoutParams globalTkTokenLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);
        globalTkTokenLp.leftMargin = Utils.dip2px(context, 10);
        globalTkTokenLp.rightMargin = Utils.dip2px(context, 10);
        globalTkTokenEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        globalTkTokenEdit.setSingleLine(true);
        globalTkTokenEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        globalTkTokenEdit.setHint("请输入global tkToken");
        Layout.addView(globalTkTokenEdit, globalTkTokenLp);

        final EditText videoTkTokenEdit = new EditText(context);
        LinearLayout.LayoutParams tkTokenLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tkTokenLp.leftMargin = Utils.dip2px(context, 10);
        tkTokenLp.rightMargin = Utils.dip2px(context, 10);
        videoTkTokenEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        videoTkTokenEdit.setSingleLine(true);
        videoTkTokenEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        videoTkTokenEdit.setHint("请输入video tkToken");
        Layout.addView(videoTkTokenEdit, tkTokenLp);

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(Layout);
        builder.setPositiveButton(android.R.string.cancel, (dialog, which) -> Toast.makeText(context,
                "The tkToken " + "is" + " empty", Toast.LENGTH_LONG).show());
        builder.setNegativeButton(android.R.string.ok, (dialog, which) -> {
            final String deviceSerial = deviceSerialEdit.getText().toString();
            final String cameraNoString = cameraNoEdit.getText().toString();
            final String globalTKToken = globalTkTokenEdit.getText().toString();
            final String videoTKToken = videoTkTokenEdit.getText().toString();
            if (TextUtils.isEmpty(deviceSerial)) {
                Toast.makeText(context, "The deviceSerial is empty", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(cameraNoString)) {
                Toast.makeText(context, "The cameraNo is empty", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(globalTKToken)) {
                Toast.makeText(context, "The global tkToken is empty", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(videoTKToken)) {
                Toast.makeText(context, "The video tkToken is empty", Toast.LENGTH_LONG).show();
                return;
            }
            l.onInputTKToken(deviceSerial, Integer.parseInt(cameraNoString), globalTKToken, videoTKToken);
        });
        AlertDialog alertDialog = null;
        alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return alertDialog;
    }

    public interface tkTokenInputListener4 {

        void onInputTKToken(final String deviceSerial, final int cameraNo, final String deviceGlobalTKToken,
                            final String deviceVideoTKToken);
    }

    /***************** UI *******************/

    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.addBackButton(view -> finish());
        mTitleBar.setTitle("小权限tkToken设置");
    }
}