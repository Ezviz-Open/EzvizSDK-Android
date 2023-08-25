package com.videogo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.videogo.constant.Constant;

import ezviz.ezopensdk.R;

public class VerifyCodeInput {

    public interface VerifyCodeInputListener {

        void onInputVerifyCode(final String verifyCode);
    }

    public interface VerifyCodeErrorListener{

        void verifyCodeError();
    }

    private AlertDialog mPasswordDialog = null;

    public static AlertDialog VerifyCodeInputDialog(final Context context,final VerifyCodeInputListener l) {

        LinearLayout passwordErrorLayout = new LinearLayout(context);
        FrameLayout.LayoutParams layoutLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        passwordErrorLayout.setOrientation(LinearLayout.VERTICAL);
        passwordErrorLayout.setLayoutParams(layoutLp);

        TextView message1 = new TextView(context);
        LinearLayout.LayoutParams message1Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        message1Lp.gravity = Gravity.CENTER_HORIZONTAL;
        message1Lp.leftMargin = Utils.dip2px(context, 10);
        message1Lp.rightMargin = Utils.dip2px(context, 10);
        message1.setGravity(Gravity.CENTER);
        message1.setTextColor(Color.rgb(0, 0, 0));
        message1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        passwordErrorLayout.addView(message1, message1Lp);

        final EditText newPassword = new EditText(context);
        newPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constant.PSW_MAX_LENGTH)});
        LinearLayout.LayoutParams newPasswordLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        newPasswordLp.leftMargin = Utils.dip2px(context, 10);
        newPasswordLp.rightMargin = Utils.dip2px(context, 10);
        newPassword.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        newPassword.setSingleLine(true);
        newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordErrorLayout.addView(newPassword, newPasswordLp);

        TextView message2 = new TextView(context);
        LinearLayout.LayoutParams message2Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        message2Lp.gravity = Gravity.CENTER_HORIZONTAL;
        message2Lp.leftMargin = Utils.dip2px(context, 10);
        message2Lp.rightMargin = Utils.dip2px(context, 10);
        message2.setGravity(Gravity.CENTER);
        message2.setTextColor(Color.rgb(0, 0, 0));
        message2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        passwordErrorLayout.addView(message2, message2Lp);
        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        message1.setText("");
        message2.setText(context.getResources().getString(R.string.realplay_encrypt_password_error_message));
        builder.setTitle(context.getResources().getString(R.string.input_device_verify_code));
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "The verification code is empty", Toast.LENGTH_LONG).show();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(context, "The verification code is empty", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String verifyCode = newPassword.getText().toString();
                if (TextUtils.isEmpty(verifyCode)) {
                    Toast.makeText(context, "The verification code is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                l.onInputVerifyCode(verifyCode);
            }
        });
        AlertDialog mPasswordDialog = null;
        mPasswordDialog = builder.create();
        mPasswordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return mPasswordDialog;
    }
}
