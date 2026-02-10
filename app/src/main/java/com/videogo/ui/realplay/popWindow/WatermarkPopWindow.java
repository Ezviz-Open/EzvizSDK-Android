package com.videogo.ui.realplay.popWindow;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.videogo.stream.EZPlayWaterMarkConfig;
import com.videogo.util.LogUtil;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import ezviz.ezopensdk.R;

/**
 * Copyright (C) 2025 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2025/9/19 10:36
 */
public class WatermarkPopWindow {

    private static final String TAG = WatermarkPopWindow.class.getSimpleName();

    // 上下文
    private Context context;
    // pop窗口父控件
    private View parent;
    // pop窗口高度
    private int windowHeight;
    // 播放窗口
    private SurfaceView surfaceView;
    // 水印配置
    private EZPlayWaterMarkConfig waterMarkConfig;// 水印设置

    // 水印设置pop
    private PopupWindow mWatermarkPopupWindow;
    // 水印设置pop窗口监听器
    private OnWatermarkPopupWindowListener watermarkPopupWindowListener;

    /**
     * @param context         上下文
     * @param parent          pop窗口父控件
     * @param windowHeight    pop窗口高度
     * @param surfaceView     播放窗口
     * @param waterMarkConfig 水印配置
     */
    public WatermarkPopWindow(Context context, View parent, int windowHeight, SurfaceView surfaceView,
                              EZPlayWaterMarkConfig waterMarkConfig) {
        this.context = context;
        this.parent = parent;
        this.windowHeight = windowHeight;
        this.surfaceView = surfaceView;
        this.waterMarkConfig = waterMarkConfig;
    }

    public void setWatermarkPopupWindowListener(OnWatermarkPopupWindowListener watermarkPopupWindowListener) {
        this.watermarkPopupWindowListener = watermarkPopupWindowListener;
    }

    /**
     * 水印SeekBar拖动事件
     */
    private final OnSeekChangeListener mWatermarkSeekBarChangeListener = new OnSeekChangeListener() {
        @Override
        public void onSeeking(SeekParams seekParams) {
            if (seekParams.seekBar.getId() == R.id.seekbar_font_size) {// 水印字体大小
                waterMarkConfig.fontWidth = seekParams.progress;
                waterMarkConfig.fontHeight = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_space) {// 水印字体行间距
                waterMarkConfig.fontSpace = seekParams.progressFloat;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_rotate_angle) {// 水印字体倾斜角度
                waterMarkConfig.fontRotateAngle = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_alignment) {// 水印字体对齐方式
                if (seekParams.progress == 0) {
                    waterMarkConfig.fontAlignment =
                            EZPlayWaterMarkConfig.EZWaterMarkTextAlignment.EZWaterMarkTextAlignmentCenter;
                } else if (seekParams.progress == 1) {
                    waterMarkConfig.fontAlignment =
                            EZPlayWaterMarkConfig.EZWaterMarkTextAlignment.EZWaterMarkTextAlignmentLeft;
                } else if (seekParams.progress == 2) {
//                    waterMarkConfig.fontAlignment = EZPlayWaterMarkConfig.EZWaterMarkTextAlignment
//                    .EZWaterMarkTextAlignmentRight;
                    Toast.makeText(context, "暂不支持右对齐", Toast.LENGTH_SHORT).show();
                }
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_position_x) {// x轴比例开始位置
                waterMarkConfig.startPosX = seekParams.progressFloat;
//                float marginScale = (float) Utils.dip2px(context, 10) / (float)surfaceView.getWidth();
//                waterMarkConfig.startPosX = marginScale;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_position_y) {// y轴比例开始位置
                waterMarkConfig.startPosY = seekParams.progressFloat;
//                float marginScale = (float) Utils.dip2px(context, 10) / (float)surfaceView.getWidth();
//                waterMarkConfig.startPosY = marginScale;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_color_red) {// 水印字体颜色-红
                waterMarkConfig.red = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_color_green) {// 水印字体颜色-绿
                waterMarkConfig.green = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_color_blue) {// 水印字体颜色-蓝
                waterMarkConfig.blue = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_alpha) {// 水印字体-透明度
                waterMarkConfig.alpha = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_row_number) {// 水印行数
                waterMarkConfig.rowNumber = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_font_column_number) {// 水印列数
                waterMarkConfig.columnNumber = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_window_adapt_row_space) {// 设置自适应行列数行间距
                waterMarkConfig.windowAdaptRowSpace = seekParams.progress;
            } else if (seekParams.seekBar.getId() == R.id.seekbar_window_adapt_column_space) {// 设置自适应行列数列间距
                waterMarkConfig.windowAdaptColumnSpace = seekParams.progress;
            }

            onWaterMarkInfoChanged();
        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
        }
    };

    /**
     * 打开水印弹出框
     */
    public void openWatermarkPopupWindow() {
        closeWatermarkPopupWindow();

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_watermark_wnd, null, true);

        layoutView.findViewById(R.id.watermark_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWatermarkPopupWindow();
            }
        });
        LinearLayout watermarkLayout = layoutView.findViewById(R.id.ll_watermark);
        // 水印开关组件
        Switch watermarkSwitch = layoutView.findViewById(R.id.switch_watermark);
        watermarkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            watermarkLayout.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            if (isChecked) {
                watermarkLayout.setVisibility(View.VISIBLE);
                onWaterMarkInfoChanged();
            } else {
                watermarkLayout.setVisibility(View.INVISIBLE);
                onClearWaterMarkInfo();
            }
        });
        watermarkSwitch.setChecked(true);

        IndicatorSeekBar fontSizeSeekbar = layoutView.findViewById(R.id.seekbar_font_size);
        IndicatorSeekBar fontSpaceSeekbar = layoutView.findViewById(R.id.seekbar_font_space);
        IndicatorSeekBar rotateAngleSeekbar = layoutView.findViewById(R.id.seekbar_rotate_angle);
        IndicatorSeekBar fontAlignmentSeekbar = layoutView.findViewById(R.id.seekbar_font_alignment);
        IndicatorSeekBar fontPositionXSeekbar = layoutView.findViewById(R.id.seekbar_font_position_x);
        IndicatorSeekBar fontPositionYSeekbar = layoutView.findViewById(R.id.seekbar_font_position_y);
        IndicatorSeekBar fontColorRedSeekbar = layoutView.findViewById(R.id.seekbar_font_color_red);
        IndicatorSeekBar fontColorGreenSeekbar = layoutView.findViewById(R.id.seekbar_font_color_green);
        IndicatorSeekBar fontColorBlueSeekbar = layoutView.findViewById(R.id.seekbar_font_color_blue);
        IndicatorSeekBar fontAlphaSeekbar = layoutView.findViewById(R.id.seekbar_font_alpha);
        IndicatorSeekBar fontRowNumSeekbar = layoutView.findViewById(R.id.seekbar_font_row_number);
        IndicatorSeekBar fontColumnNumSeekbar = layoutView.findViewById(R.id.seekbar_font_column_number);
        IndicatorSeekBar windowAdaptSeekbar = layoutView.findViewById(R.id.seekbar_window_adapt);
        IndicatorSeekBar windowAdaptRowSpaceSeekbar = layoutView.findViewById(R.id.seekbar_window_adapt_row_space);
        IndicatorSeekBar windowAdaptColumnSpaceSeekbar =
                layoutView.findViewById(R.id.seekbar_window_adapt_column_space);
        windowAdaptRowSpaceSeekbar.setProgress(waterMarkConfig.windowAdaptRowSpace);
        windowAdaptColumnSpaceSeekbar.setProgress(waterMarkConfig.windowAdaptColumnSpace);
        // 设置监听
        fontSizeSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontSpaceSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        rotateAngleSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontAlignmentSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontPositionXSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontPositionYSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontColorRedSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontColorGreenSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontColorBlueSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontAlphaSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontRowNumSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        fontColumnNumSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        // 填充全屏组件
        Switch fontfillFullScreenSwitch = layoutView.findViewById(R.id.switch_fill_full_screen);
        fontfillFullScreenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            waterMarkConfig.fillFullScreen = isChecked;
            fontRowNumSeekbar.setEnabled(isChecked);
            fontColumnNumSeekbar.setEnabled(isChecked);
            if (waterMarkConfig.windowAdaptMode != EZPlayWaterMarkConfig.EZWaterMarkWindowAdaptMode.EZWaterMarkWindowAdaptModeFontRowColumn) {
                fontPositionXSeekbar.setEnabled(!isChecked);
                fontPositionYSeekbar.setEnabled(!isChecked);
            }
            onWaterMarkInfoChanged();
        });
        fontfillFullScreenSwitch.setChecked(waterMarkConfig.fillFullScreen);
        boolean b = waterMarkConfig.windowAdaptMode == EZPlayWaterMarkConfig.EZWaterMarkWindowAdaptMode.EZWaterMarkWindowAdaptModeNone
                && waterMarkConfig.fillFullScreen;
        fontRowNumSeekbar.setEnabled(b);
        fontColumnNumSeekbar.setEnabled(b);
        // 窗口自适应选择监听
        windowAdaptSeekbar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                windowAdaptRowSpaceSeekbar.setEnabled(false);
                windowAdaptColumnSpaceSeekbar.setEnabled(false);
                fontfillFullScreenSwitch.setEnabled(false);
                fontRowNumSeekbar.setEnabled(false);
                fontColumnNumSeekbar.setEnabled(false);
                fontPositionXSeekbar.setEnabled(false);
                fontPositionYSeekbar.setEnabled(false);
                if (seekParams.progress == 0) {// 关闭窗口自适应
                    waterMarkConfig.windowAdaptMode =
                            EZPlayWaterMarkConfig.EZWaterMarkWindowAdaptMode.EZWaterMarkWindowAdaptModeNone;
                    fontfillFullScreenSwitch.setEnabled(true);
                    if (waterMarkConfig.fillFullScreen) {
                        fontRowNumSeekbar.setEnabled(true);
                        fontColumnNumSeekbar.setEnabled(true);
                    } else {
                        fontPositionXSeekbar.setEnabled(true);
                        fontPositionYSeekbar.setEnabled(true);
                    }
                } else if (seekParams.progress == 1) {// 字体行数和列数自适应
                    waterMarkConfig.windowAdaptMode =
                            EZPlayWaterMarkConfig.EZWaterMarkWindowAdaptMode.EZWaterMarkWindowAdaptModeFontRowColumn;
                    windowAdaptRowSpaceSeekbar.setEnabled(true);
                    windowAdaptColumnSpaceSeekbar.setEnabled(true);
                } else if (seekParams.progress == 2) {// 字体大小自适应
                    waterMarkConfig.windowAdaptMode =
                            EZPlayWaterMarkConfig.EZWaterMarkWindowAdaptMode.EZWaterMarkWindowAdaptModeFontSize;
                    fontfillFullScreenSwitch.setEnabled(true);
                    if (waterMarkConfig.fillFullScreen) {
                        fontRowNumSeekbar.setEnabled(true);
                        fontColumnNumSeekbar.setEnabled(true);
                    } else {
                        fontPositionXSeekbar.setEnabled(true);
                        fontPositionYSeekbar.setEnabled(true);
                    }
                    // 设置为字体大小自适应时，必须设置windowAdaptBaseWindowWidth 和 windowAdaptBaseWindowHeight
                    waterMarkConfig.windowAdaptBaseWindowWidth = surfaceView.getWidth();
                    waterMarkConfig.windowAdaptBaseWindowHeight = surfaceView.getHeight();
                }
                onWaterMarkInfoChanged();
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            }
        });
        windowAdaptRowSpaceSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        windowAdaptColumnSpaceSeekbar.setOnSeekChangeListener(mWatermarkSeekBarChangeListener);
        // 颜色自适应组件
        Switch fontColorAdaptSwitch = layoutView.findViewById(R.id.switch_font_color_adapt);
        fontColorAdaptSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            waterMarkConfig.fontColorAdapt = isChecked;
            fontColorRedSeekbar.setEnabled(!isChecked);
            fontColorGreenSeekbar.setEnabled(!isChecked);
            fontColorBlueSeekbar.setEnabled(!isChecked);
            onWaterMarkInfoChanged();
        });
        fontColorAdaptSwitch.setChecked(waterMarkConfig.fontColorAdapt);
        // 设置初始值
        fontSizeSeekbar.setProgress(waterMarkConfig.fontWidth);
        fontSpaceSeekbar.setProgress(waterMarkConfig.fontSpace);
        rotateAngleSeekbar.setProgress(waterMarkConfig.fontRotateAngle);
        fontAlignmentSeekbar.setProgress(waterMarkConfig.fontAlignment.fontAlignment);
        fontPositionXSeekbar.setDecimalScale(2);
        fontPositionXSeekbar.setProgress(waterMarkConfig.startPosX);
        fontPositionYSeekbar.setDecimalScale(2);
        fontPositionYSeekbar.setProgress(waterMarkConfig.startPosY);
        fontColorRedSeekbar.setProgress(waterMarkConfig.red);
        fontColorGreenSeekbar.setProgress(waterMarkConfig.green);
        fontColorBlueSeekbar.setProgress(waterMarkConfig.blue);
        fontAlphaSeekbar.setProgress(waterMarkConfig.alpha);
        fontRowNumSeekbar.setProgress(waterMarkConfig.rowNumber);
        fontColumnNumSeekbar.setProgress(waterMarkConfig.columnNumber);
        windowAdaptRowSpaceSeekbar.setEnabled(false);
        windowAdaptColumnSpaceSeekbar.setEnabled(false);
        windowAdaptSeekbar.setProgress(waterMarkConfig.windowAdaptMode.windowAdaptMode);

        mWatermarkPopupWindow = new PopupWindow(layoutView, RelativeLayout.LayoutParams.MATCH_PARENT, windowHeight,
                true);
        mWatermarkPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mWatermarkPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        mWatermarkPopupWindow.setFocusable(false);
        mWatermarkPopupWindow.setOutsideTouchable(false);
        mWatermarkPopupWindow.setTouchable(true);
        mWatermarkPopupWindow.showAsDropDown(parent);
        mWatermarkPopupWindow.setOnDismissListener(() -> {
            LogUtil.i(TAG, "KEYCODE_BACK DOWN");
            closeWatermarkPopupWindow();
        });
        mWatermarkPopupWindow.update();
    }

    /**
     * 关闭云台操作弹出框
     */
    private void closeWatermarkPopupWindow() {
        if (mWatermarkPopupWindow != null) {
            dismissPopWindow(mWatermarkPopupWindow);
            mWatermarkPopupWindow = null;
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
     * 水印信息设置回调
     */
    private void onWaterMarkInfoChanged() {
        if (watermarkPopupWindowListener != null) {
            watermarkPopupWindowListener.onWaterMarkInfoChanged();
        }
    }

    /**
     * 水印信息清除回调
     */
    private void onClearWaterMarkInfo() {
        if (watermarkPopupWindowListener != null) {
            watermarkPopupWindowListener.onClearWaterMarkInfo();
        }
    }

    /**
     * 水印信息窗口回调
     */
    public interface OnWatermarkPopupWindowListener {

        /**
         * 设置水印信息
         */
        void onWaterMarkInfoChanged();

        /**
         * 清除水印信息
         */
        void onClearWaterMarkInfo();
    }
}
