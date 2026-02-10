package com.ezviz.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.videogo.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("Registered")
public class RootActivity extends Activity {

    public final static String TAG = RootActivity.class.getSimpleName();

    protected static Context mContext;

    protected void showToast(final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }
                String text = getString(id);
                if (text != null && !text.equals("")) {
                    Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void showToast(final int id, final int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                String text = getString(id);
                if (errCode != 0) {
                    int errorId = getErrorId(errCode);
                    if (errorId != 0) {
                        text = getString(errorId);
                    } else {
                        text = text + " (" + errCode + ")";
                    }
                }
                if (text != null && !text.equals("")) {
                    Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    protected void showToast(final int id, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                String text = getString(id);
                if (!TextUtils.isEmpty(msg)) {
                    text = text + " (" + msg + ")";
                }
                if (text != null && !text.equals("")) {
                    Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void showToast(final CharSequence text) {
        LogUtil.d(TAG, text.toString());
        runOnUiThread(() -> {
            if (isFinishing()) {
                return;
            }
            if (text != null && !text.equals("")) {
                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected int getErrorId(int errorCode) {
        int errorId = this.getResources().getIdentifier("error_code_" + errorCode, "string", this.getPackageName());
        /*
         * Field fieldError; int errorId = 0; try { fieldError =
         * R.string.class.getDeclaredField("error_code_" + errorCode);
         * fieldError.setAccessible(true); R.string string = new R.string(); try { errorId =
         * fieldError.getInt(string); } catch (IllegalAccessException e) { // TODO Auto-generated
         * catch block e.printStackTrace(); } catch (IllegalArgumentException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); } } catch (NoSuchFieldException e) { //
         * TODO Auto-generated catch block e.printStackTrace(); }
         */

        return errorId;
    }

    protected void showToast(final int res1, final int res2, final int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = res1 != 0 ? getString(res1) : "";
                if (res2 != 0) {
                    text = text + ", " + getString(res2);
                }
                if (errCode != 0) {
                    int errorId = getErrorId(errCode);
                    if (errorId != 0) {
                        text = getString(errorId);
                    } else {
                        text = text + " (" + errCode + ")";
                    }
                }
                if (text != null) {
                    Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /**
     * @return the isResumed
     */
    // public static boolean isResumed() {
    // return isResumed;
    // }

    // public static void setResumed(boolean isResumed) {
    // RootActivity.isResumed = isResumed;
    // }
    protected String getErrorTip(int id, int errCode) {
        StringBuffer errorTip = new StringBuffer();

        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                errorTip.append(getString(errorId));
            } else {
                errorTip.append(getString(id)).append(" (").append(errCode).append(")");
            }
        } else {
            errorTip.append(getString(id));
        }
        return errorTip.toString();
    }

    protected void hideInputMethod() {
        if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    protected void removeHandler(Handler handler) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    private static List<Activity> mActivityList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mActivityList.add(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applySystemWindowsInsets();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityList.remove(this);
    }

    private void applySystemWindowsInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            final View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
            if (rootView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                        Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars());
                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                        return insets;
                    });
                } else {
                    rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                        v.setPadding(
                                insets.getSystemWindowInsetLeft(),
                                insets.getSystemWindowInsetTop(),
                                insets.getSystemWindowInsetRight(),
                                insets.getSystemWindowInsetBottom()
                        );
                        return insets;
                    });
                }
            }
        }
    }

    private AlertDialog mLastDialog = null;


    public interface EditTextDialogCallback {
        public void confirm(String value);
    }


    private static int mNotificationId = 1;

    protected static int getUniqueNotificationId() {
        return mNotificationId++;
    }

    protected void initUi() {
    }

    public static void exitApp() {
        for (Activity activity : mActivityList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        System.exit(0);
    }

    protected void initPresenter() {
    }

    protected void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null && !isFinishing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    protected void measure(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
    }

}
