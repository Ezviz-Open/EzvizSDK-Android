package com.videogo.ui.realplay.popWindow;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.videogo.openapi.EZConstants;
import com.videogo.util.LogUtil;
import com.videogo.widget.PtzControlAngleView;

import ezviz.ezopensdk.R;

/**
 * Copyright (C) 2025 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2025/9/19 14:53
 */
public class PTZPopWindow {

    private static final String TAG = PTZPopWindow.class.getSimpleName();

    // 上下文
    private Context context;
    // pop窗口父控件
    private View parent;
    // pop窗口高度
    private int windowHeight;

    // 云台水平比例尺
    private PtzControlAngleView ptzControlAngleViewHor;
    // 云台垂直比例尺
    private PtzControlAngleView ptzControlAngleViewVer;

    // 云台pop
    private PopupWindow mPtzPopupWindow;
    // 云台控制方向盘，设置背景图
    private LinearLayout mPtzControlLy;

    private OnPTZPopupWindowListener ptzPopupWindowListener;

    /**
     * @param context         上下文
     * @param parent          pop窗口父控件
     * @param windowHeight    pop窗口高度
     * @param ptzControlAngleViewHor 云台水平比例尺
     * @param ptzControlAngleViewVer 云台垂直比例尺
     */
    public PTZPopWindow(Context context, View parent, int windowHeight,
                        PtzControlAngleView ptzControlAngleViewHor, PtzControlAngleView ptzControlAngleViewVer) {
        this.context = context;
        this.parent = parent;
        this.windowHeight = windowHeight;
        this.ptzControlAngleViewHor = ptzControlAngleViewHor;
        this.ptzControlAngleViewVer = ptzControlAngleViewVer;
    }

    public void setPTZPopupWindowListener(OnPTZPopupWindowListener ptzPopupWindowListener) {
        this.ptzPopupWindowListener = ptzPopupWindowListener;
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionevent) {
            int action = motionevent.getAction();
            final int speed = EZConstants.PTZ_SPEED_DEFAULT;
            switch (action) {
                case MotionEvent.ACTION_DOWN:// 手指按下
                    int id = view.getId();
                    if (id == R.id.ptz_top_btn) {// 云台-上
                        ptzControlAngleViewVer.setVisibility(View.VISIBLE);
                        mPtzControlLy.setBackgroundResource(R.drawable.ptz_up_sel);
                        ptzOption(EZConstants.EZPTZCommand.EZPTZCommandUp, EZConstants.EZPTZAction.EZPTZActionSTART);
                    } else if (id == R.id.ptz_bottom_btn) {// 云台-下
                        ptzControlAngleViewVer.setVisibility(View.VISIBLE);
                        mPtzControlLy.setBackgroundResource(R.drawable.ptz_bottom_sel);
                        ptzOption(EZConstants.EZPTZCommand.EZPTZCommandDown, EZConstants.EZPTZAction.EZPTZActionSTART);
                    } else if (id == R.id.ptz_left_btn) {// 云台-左
                        ptzControlAngleViewHor.setVisibility(View.VISIBLE);
                        mPtzControlLy.setBackgroundResource(R.drawable.ptz_left_sel);
                        ptzOption(EZConstants.EZPTZCommand.EZPTZCommandLeft, EZConstants.EZPTZAction.EZPTZActionSTART);
                    } else if (id == R.id.ptz_right_btn) {// 云台-右
                        ptzControlAngleViewHor.setVisibility(View.VISIBLE);
                        mPtzControlLy.setBackgroundResource(R.drawable.ptz_right_sel);
                        ptzOption(EZConstants.EZPTZCommand.EZPTZCommandRight, EZConstants.EZPTZAction.EZPTZActionSTART);
                    }
                    break;
                case MotionEvent.ACTION_UP:// 手指抬起
                    id = view.getId();
                    if (id == R.id.ptz_top_btn) {
                        mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                        ptzOption(EZConstants.EZPTZCommand.EZPTZCommandUp, EZConstants.EZPTZAction.EZPTZActionSTOP);
                    } else if (id == R.id.ptz_bottom_btn) {
                        mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                        ptzOption(EZConstants.EZPTZCommand.EZPTZCommandDown, EZConstants.EZPTZAction.EZPTZActionSTOP);
                    } else if (id == R.id.ptz_left_btn) {
                        mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                        ptzOption(EZConstants.EZPTZCommand.EZPTZCommandLeft, EZConstants.EZPTZAction.EZPTZActionSTOP);
                    } else if (id == R.id.ptz_right_btn) {
                        mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                        ptzOption(EZConstants.EZPTZCommand.EZPTZCommandRight, EZConstants.EZPTZAction.EZPTZActionSTOP);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    /**
     * 打开云台操作弹出框
     */
    public void openPtzPopupWindow() {
        closePtzPopupWindow();
        if (ptzPopupWindowListener != null) {
            ptzPopupWindowListener.onPtzPopWindowOpen(true);
        }

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_ptz_wnd, null, true);

        mPtzControlLy = layoutView.findViewById(R.id.ptz_control_ly);
        layoutView.findViewById(R.id.ptz_close_btn).setOnClickListener(v -> closePtzPopupWindow());
        layoutView.findViewById(R.id.ptz_top_btn).setOnTouchListener(mOnTouchListener);
        layoutView.findViewById(R.id.ptz_bottom_btn).setOnTouchListener(mOnTouchListener);
        layoutView.findViewById(R.id.ptz_left_btn).setOnTouchListener(mOnTouchListener);
        layoutView.findViewById(R.id.ptz_right_btn).setOnTouchListener(mOnTouchListener);

        mPtzPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, windowHeight, true);
        mPtzPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPtzPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        mPtzPopupWindow.setFocusable(false);
        mPtzPopupWindow.setOutsideTouchable(false);
        mPtzPopupWindow.setTouchable(true);
        mPtzPopupWindow.showAsDropDown(parent);
        mPtzPopupWindow.setOnDismissListener(() -> {
            LogUtil.i(TAG, "KEYCODE_BACK DOWN");
            closePtzPopupWindow();
        });
        mPtzPopupWindow.update();
    }

    /**
     * 关闭云台操作弹出框
     */
    public void closePtzPopupWindow() {
        if (ptzPopupWindowListener != null) {
            ptzPopupWindowListener.onPtzPopWindowOpen(false);
        }
        if (mPtzPopupWindow != null) {
            dismissPopWindow(mPtzPopupWindow);
            mPtzPopupWindow = null;
            mPtzControlLy = null;
        }
    }

    private void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private void ptzOption(EZConstants.EZPTZCommand command, EZConstants.EZPTZAction action) {
        if (ptzPopupWindowListener != null) {
            ptzPopupWindowListener.onPtzOption(command, action);
        }
    }

    /**
     * 云台操作弹出框监听器
     */
    public interface OnPTZPopupWindowListener {

        /**
         * 云台操作弹出框是否打开
         *
         * @param open
         */
        void onPtzPopWindowOpen(boolean open);

        /**
         * 云台操作
         *
         * @param command 云台控制命令
         * @param action  云台控制动作
         */
        void onPtzOption(EZConstants.EZPTZCommand command, EZConstants.EZPTZAction action);
    }
}
