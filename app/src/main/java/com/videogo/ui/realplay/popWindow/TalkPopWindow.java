package com.videogo.ui.realplay.popWindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EzvizAPI;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.RingView;

import ezviz.ezopensdk.R;

/**
 * Copyright (C) 2025 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2025/9/19 16:14
 */
public class TalkPopWindow {

    private static final String TAG = TalkPopWindow.class.getSimpleName();

    // 上下文
    private Context context;
    // pop窗口父控件
    private View parent;
    // pop窗口高度
    private int windowHeight;
    // 是否是全双工对讲
    private boolean isFullDuplex;

    // 对讲pop
    private PopupWindow mTalkPopupWindow;
    // 对讲效果视图
    private RingView mTalkRingView;
    // 半双工对讲按钮
    private Button mTalkBackControlBtn;
    // 对讲本地音频音量大小
    private TextView mTalkLocalLoudnessTv;

    private OnTalkPopupWindowListener talkPopupWindowListener;

    /**
     * @param context         上下文
     * @param parent          pop窗口父控件
     * @param windowHeight    pop窗口高度
     * @param isFullDuplex    是否支持半双工对讲
     */
    public TalkPopWindow(Context context, View parent, int windowHeight, boolean isFullDuplex) {
        this.context = context;
        this.parent = parent;
        this.windowHeight = windowHeight;
        this.isFullDuplex = isFullDuplex;
    }

    public void setTalkPopupWindowListener(OnTalkPopupWindowListener talkPopupWindowListener) {
        this.talkPopupWindowListener = talkPopupWindowListener;
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionevent) {
            int action = motionevent.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:// 手指按下
                    if (view.getId() == R.id.talkback_control_btn) {// 半双工对讲按钮
                        mTalkRingView.setVisibility(View.VISIBLE);
                        if (talkPopupWindowListener != null) {
                            talkPopupWindowListener.onVoiceTalkStatusChanged(true);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:// 手指抬起
                    if (view.getId() == R.id.talkback_control_btn) {
                        mTalkRingView.setVisibility(View.GONE);
                        if (talkPopupWindowListener != null) {
                            talkPopupWindowListener.onVoiceTalkStatusChanged(false);
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.talkback_close_btn) {// 关闭按钮
                closeTalkPopupWindow(true);
            } else if (id == R.id.talk_voice_normal) {// 对讲变声按钮-原声
                if (talkPopupWindowListener != null) {
                    talkPopupWindowListener.onVoiceTalkVoiceChangeClick(EZConstants.EZVoiceChangeType.EZ_VOICE_CHANGE_TYPE_NORMAL);
                }
            } else if (id == R.id.talk_voice_clown) {// 对讲变声按钮-小丑音
                if (talkPopupWindowListener != null) {
                    talkPopupWindowListener.onVoiceTalkVoiceChangeClick(EZConstants.EZVoiceChangeType.EZ_VOICE_CHANGE_TYPE_CLOWN);
                }
            } else if (id == R.id.talk_voice_man) {// 对讲变声按钮-大叔音
                if (talkPopupWindowListener != null) {
                    talkPopupWindowListener.onVoiceTalkVoiceChangeClick(EZConstants.EZVoiceChangeType.EZ_VOICE_CHANGE_TYPE_MAN);
                }
            }
        }

    };

    /**
     * 打开对讲弹出框
     * @param showAnimation 是否显示弹出动画
     */
    public void openTalkPopupWindow(boolean showAnimation) {
        closeTalkPopupWindow(false);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_talkback_wnd, null, true);
        // 关闭按钮
        layoutView.findViewById(R.id.talkback_close_btn).setOnClickListener(mOnClickListener);
        // 全双工对讲水波纹效果
        mTalkRingView = layoutView.findViewById(R.id.talkback_rv);
        // 半双工对讲状态切换
        mTalkBackControlBtn = layoutView.findViewById(R.id.talkback_control_btn);
        mTalkBackControlBtn.setOnTouchListener(mOnTouchListener);
        // 全双工对讲时，显示水波纹效果，隐藏半双工对讲状态按钮
        if (isFullDuplex) {
            mTalkRingView.setVisibility(View.VISIBLE);
            mTalkBackControlBtn.setEnabled(false);
            mTalkBackControlBtn.setText(R.string.talking);
        }
        // 设备端静音开关
        Switch remoteMuteSwitch = layoutView.findViewById(R.id.switch_remote_mute);
        remoteMuteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (talkPopupWindowListener != null) {
                talkPopupWindowListener.onVoiceTalkRemoteMuted(isChecked);
            }
        });
        // 本地音频音量
        mTalkLocalLoudnessTv = layoutView.findViewById(R.id.talkback_local_loudness_tv);
        // 对讲变声按钮监听
        layoutView.findViewById(R.id.talk_voice_normal).setOnClickListener(mOnClickListener);
        layoutView.findViewById(R.id.talk_voice_clown).setOnClickListener(mOnClickListener);
        layoutView.findViewById(R.id.talk_voice_man).setOnClickListener(mOnClickListener);
        // 海外不支持对讲变声
        if (EzvizAPI.getInstance().isUsingGlobalSDK()) {
            layoutView.findViewById(R.id.talk_voice_change_ll).setVisibility(View.GONE);
        }

        mTalkPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, windowHeight, true);
        // mTalkPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        if (showAnimation) {
            mTalkPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        }
        mTalkPopupWindow.setFocusable(false);
        mTalkPopupWindow.setOutsideTouchable(false);
        mTalkPopupWindow.setTouchable(true);
        mTalkPopupWindow.showAsDropDown(parent);
        mTalkPopupWindow.setOnDismissListener(() -> {
            LogUtil.i(TAG, "KEYCODE_BACK DOWN");
            closeTalkPopupWindow(true);
        });
        mTalkPopupWindow.update();
        mTalkRingView.post(() -> {
            if (mTalkRingView != null) {
                mTalkRingView.setMinRadiusAndDistance(mTalkBackControlBtn.getHeight() / 2f, Utils.dip2px(context, 22));
            }
        });
    }

    /**
     * 关闭对讲弹出框
     * @param stopVoiceTalk 是否停止对讲
     */
    public void closeTalkPopupWindow(boolean stopVoiceTalk) {
        if (mTalkPopupWindow != null) {
            LogUtil.i(TAG, "closeTalkPopupWindow");
            dismissPopWindow(mTalkPopupWindow);
            mTalkPopupWindow = null;
        }
        mTalkRingView = null;
        if (stopVoiceTalk) {
            if (talkPopupWindowListener != null) {
                talkPopupWindowListener.onStopVoiceTalk();
            }
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

    /**
     * 设置本地音频音量
     * @param loudness 音频音量
     */
    public void setTalkLocalLoudness(float loudness) {
        if (mTalkLocalLoudnessTv != null) {
            mTalkLocalLoudnessTv.setText(String.format("本地音频音量：%.2f分贝 ", loudness));
        }
    }

    /**
     * 对讲弹出框监听
     */
    public interface OnTalkPopupWindowListener {

        /**
         * 半双工对讲 状态
         * @param pressed true只说不听 false只听不说
         */
        void onVoiceTalkStatusChanged(boolean pressed);

        /**
         * 全双工对讲时设置手机端是否能听到对端的声音
         * @param muted true 设备端静音 false 设备端不静音
         */
        void onVoiceTalkRemoteMuted(boolean muted);

        /**
         * 对讲变声
         * @param voiceChangeType 对讲变声类型
         */
        void onVoiceTalkVoiceChangeClick(EZConstants.EZVoiceChangeType voiceChangeType);

        /**
         * 停止对讲
         */
        void onStopVoiceTalk();
    }

}
