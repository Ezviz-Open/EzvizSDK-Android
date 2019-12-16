package ezviz.ezopensdkcommon.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import ezviz.ezopensdkcommon.R;


public class WaitDialog extends Dialog {

    private Context mContext;

    private TextView mWaitTv = null;

    private String mWaitingTxt = null;

    public WaitDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait_dialog);
        mWaitTv = (TextView) findViewById(R.id.wait_tv);

        if (mWaitingTxt != null && !mWaitingTxt.isEmpty()) {
            mWaitTv.setVisibility(View.VISIBLE);
            mWaitTv.setText(mWaitingTxt);
        } else {
            mWaitTv.setVisibility(View.GONE);
        }
    }

    public void setWaitText(String text) {
        mWaitingTxt = text;
        if (mWaitTv == null) {
            return;
        }
        if (mWaitingTxt != null && !mWaitingTxt.isEmpty()) {
            mWaitTv.setVisibility(View.VISIBLE);
            mWaitTv.setText(text);
        } else {
            mWaitTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void show() {
        if (mContext != null && !((Activity) mContext).isFinishing()) {
            try {
                super.show();
            } catch (Exception e) {
            }
        }

    }

    @Override
    public void dismiss() {
        if (mContext != null && !((Activity) mContext).isFinishing()) {
            try {
                super.dismiss();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void cancel() {
        if (mContext != null && !((Activity) mContext).isFinishing()) {
            try {
                super.cancel();
            } catch (Exception e) {
            }
        }
    }
}
