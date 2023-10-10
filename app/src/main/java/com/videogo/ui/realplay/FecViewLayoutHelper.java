package com.videogo.ui.realplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.videogo.constant.Constant;
import com.videogo.openapi.EZConstants.EZFecPlaceType;
import com.videogo.openapi.EZConstants.EZFecCorrectType;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.LocalInfo;
import com.videogo.util.Utils;

import ezviz.ezopensdk.R;

/**
 * Copyright (C) 2023 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2023/9/12 14:22
 */
public class FecViewLayoutHelper {

    private Context context;

    /// 对象初始化后设置以下属性
    public ViewGroup playWindowVg;// 播放视图
    public View playerView;
    public EZPlayer player;// 预览播放器
    public PopupWindow fecPopupWindow;// popupWindow对象创建后延后设置
    public RelativeLayout playPtzRL;// 分屏父视图的显示和隐藏，否则会挡住TextureView画面
    /// 对象初始化后设置以上属性

    private SurfaceView[] ptzPlayViews;
    private SurfaceView ptz1PlayView;
    private SurfaceView ptz2PlayView;
    private SurfaceView ptz3PlayView;
    private SurfaceView ptz4PlayView;
    private SurfaceView ptz5PlayView;
    private SurfaceView ptz6PlayView;

    // 记录全景五分屏下点击的分屏
    private SurfaceView previousPlayView;

    private EZFecPlaceType fecPlaceType;
    private EZFecCorrectType fecCorrectType;

    public FecViewLayoutHelper(Context context) {
        this.context = context;
    }

    /**
     * 设置6个播放窗口进来
     */
    @SuppressLint("ClickableViewAccessibility")
    public void setSurfaceViews(SurfaceView[] surfaceViews) {
        ptzPlayViews = surfaceViews;
        ptz1PlayView = surfaceViews[0];
        ptz2PlayView = surfaceViews[1];
        ptz3PlayView = surfaceViews[2];
        ptz4PlayView = surfaceViews[3];
        ptz5PlayView = surfaceViews[4];
        ptz6PlayView = surfaceViews[5];

        for (int i = 0; i < surfaceViews.length; i++) {
            int index = i;
            surfaceViews[i].setOnTouchListener(new FecPlayTouchListener(context) {
                @Override
                public void onStartDrag(float x, float y) {
                    player.onFecTouchDown(index, x, y);
                }

                @Override
                public boolean onDrag(float deltaX, float deltaY, float x, float y) {
                    player.onFecTouchMove(index, x, y);
                    return false;
                }

                @Override
                public void onDown(float x, float y) {
                    // 全景5分屏 & 有选中某个ptz 切换到其他矫正模式时，需要将选中状态清除
                    if (previousPlayView != null && index > 0) {
                        previousPlayView.setBackgroundResource(0);
                    }
                    if (fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_FULL5PTZ && index != 0) {
                        boolean isSupportTap = player.onFecTouchDown(index, x, y);
                        if (isSupportTap) {// 全景5分屏 & 点击分屏时，设置选中状态
                            ptzPlayViews[index].setBackgroundResource(R.drawable.border);
                            previousPlayView = ptzPlayViews[index];
                        } else if (!isSupportTap && ptzPlayViews[index] == previousPlayView) {
                            previousPlayView = null;
                        } else {

                        }
                    }
                }

                @Override
                public void onStartScale(float distance) {
                    player.onFecTouchStartScale(index, distance);
                }

                @Override
                public boolean onScale(float scale, float distance, float centerX, float centerY) {
                    player.onFecTouchScale(index, scale, 4.0f);
                    return true;
                }

                @Override
                public void onUp(float x, float y) {
                    player.onFecTouchUp(index);
                }
            });
        }
    }

    /**
     * 打开鱼眼矫正
     */
    public void openFecCorrect(EZFecCorrectType fecCorrectType, EZFecPlaceType fecPlaceType) {
        /*
         * 此处跟iOS有点区别，Surface设置Gone后，画面会被销毁会导致黑屏，所以以下几点要注意
         * 1.layoutViewsForFec->setAllFecPlayViewsHidden方法中Surface设置了Gone，点击同一个矫正模式会导致黑屏，需要判断拦截
         * 2.取流停止后，需要调用resetFecType方法重置下状态，否则会被此判断拦截，导致无法重新播放
         */
        if (fecCorrectType == this.fecCorrectType && fecPlaceType == this.fecPlaceType) {
            return;
        }
        // 1.缓存fecPlaceType & fecCorrectType
        this.fecCorrectType = fecCorrectType;
        this.fecPlaceType = fecPlaceType;
        // 2.UI布局调整
        layoutViewsForFec(() -> {
            // 3.设置对应的子视图
            player.setSurfaceHolds(fecSurfaceHolds());
            // 4.重新设置矫正模式
            player.openFecCorrect(fecCorrectType, fecPlaceType);
        });
    }

    /**
     * 播放结束后状态重置
     */
    public void resetFecType() {
        this.fecCorrectType = EZFecCorrectType.EZ_FEC_CORRECT_NONE;
        this.fecPlaceType = EZFecPlaceType.EZ_FEC_PLACE_NONE;
    }

    /**
     * 根据矫正模式返回对应需要的分屏
     */
    public SurfaceHolder[] fecSurfaceHolds() {
        switch (fecCorrectType) {
            case EZ_FEC_CORRECT_180:
            case EZ_FEC_CORRECT_360:
            case EZ_FEC_CORRECT_CYC:
            case EZ_FEC_CORRECT_ARC_HOR:
            case EZ_FEC_CORRECT_LAT:
            case EZ_FEC_CORRECT_WIDEANGLE:
                return new SurfaceHolder[]{ptz1PlayView.getHolder()};
            case EZ_FEC_CORRECT_4PTZ:
                return new SurfaceHolder[]{ptz1PlayView.getHolder(), ptz2PlayView.getHolder(),
                        ptz3PlayView.getHolder(), ptz4PlayView.getHolder()};
            case EZ_FEC_CORRECT_5PTZ:
                return new SurfaceHolder[]{ptz1PlayView.getHolder(), ptz2PlayView.getHolder(),
                        ptz3PlayView.getHolder(), ptz4PlayView.getHolder(), ptz5PlayView.getHolder()};
            case EZ_FEC_CORRECT_FULL5PTZ:
                return new SurfaceHolder[]{ptz1PlayView.getHolder(), ptz2PlayView.getHolder(),
                        ptz3PlayView.getHolder(), ptz4PlayView.getHolder(), ptz5PlayView.getHolder(),
                        ptz6PlayView.getHolder()};
            default:
                return null;
        }
    }

    /**
     * 根据矫正模式调整分屏布局
     */
    private void layoutViewsForFec(LayoutViewsCallback callback) {
        playerView.setVisibility(fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_FISH ? View.VISIBLE : View.GONE);
        playPtzRL.setVisibility(fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_FISH ? View.GONE : View.VISIBLE);
        if (previousPlayView != null) {
            previousPlayView.setBackgroundResource(0);
        }
        // 子视图布局调整
        if (fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_FISH) {
            setPlayViewAspectRadioWith1V1();
            setAllFecPlayViewsHidden();
            if (callback != null) {
                callback.onLayoutViewComplete();
            }
        } else if (fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_180
                || fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_ARC_HOR
                || fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_LAT) {
            layoutViewsFor1PTZ1V1(callback);
        } else if (fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_360
                || fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_WIDEANGLE
                || fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_CYC) {
            layoutViewsFor1PTZ16V9(callback);
        } else if (fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_4PTZ) {
            layoutViewsFor4PTZ(callback);
        } else if (fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_5PTZ) {
            layoutViewsFor5PTZ(callback);
        } else if (fecCorrectType == EZFecCorrectType.EZ_FEC_CORRECT_FULL5PTZ) {
            layoutViewsForFull5PTZ(callback);
        }
    }

    private void layoutViewsFor4PTZ(LayoutViewsCallback callback) {
        setPlayViewAspectRadioWith1V1();
        setAllFecPlayViewsHidden();
        ptz1PlayView.setVisibility(View.VISIBLE);
        ptz2PlayView.setVisibility(View.VISIBLE);
        ptz3PlayView.setVisibility(View.VISIBLE);
        ptz4PlayView.setVisibility(View.VISIBLE);

        LocalInfo mLocalInfo = LocalInfo.getInstance();
        final int screenWidth = mLocalInfo.getScreenWidth();

        RelativeLayout.LayoutParams svLp1 = new RelativeLayout.LayoutParams(screenWidth/2, screenWidth/2);
        ptz1PlayView.setLayoutParams(svLp1);

        RelativeLayout.LayoutParams svLp2 = new RelativeLayout.LayoutParams(screenWidth/2, screenWidth/2);
        svLp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ptz2PlayView.setLayoutParams(svLp2);

        RelativeLayout.LayoutParams svLp3 = new RelativeLayout.LayoutParams(screenWidth/2, screenWidth/2);
        svLp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ptz3PlayView.setLayoutParams(svLp3);

        RelativeLayout.LayoutParams svLp4 = new RelativeLayout.LayoutParams(screenWidth/2, screenWidth/2);
        svLp4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        svLp4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ptz4PlayView.setLayoutParams(svLp4);

        ptz4PlayView.post(() -> {
            if (callback != null) {
                callback.onLayoutViewComplete();
            }
        });
    }

    private void layoutViewsFor5PTZ(LayoutViewsCallback callback) {
        setPlayViewAspectRadioWith1V1();
        setAllFecPlayViewsHidden();
        ptz1PlayView.setVisibility(View.VISIBLE);
        ptz2PlayView.setVisibility(View.VISIBLE);
        ptz3PlayView.setVisibility(View.VISIBLE);
        ptz4PlayView.setVisibility(View.VISIBLE);
        ptz5PlayView.setVisibility(View.VISIBLE);

        LocalInfo mLocalInfo = LocalInfo.getInstance();
        final int screenWidth = mLocalInfo.getScreenWidth();
        int smallWidth = screenWidth/3;// 较小的视图宽度
        int margin = (screenWidth - smallWidth - smallWidth * 3 / 2) / 2;

        RelativeLayout.LayoutParams svLp1 = new RelativeLayout.LayoutParams(screenWidth/2, screenWidth/2);
        svLp1.topMargin = margin;
        ptz1PlayView.setLayoutParams(svLp1);

        RelativeLayout.LayoutParams svLp2 = new RelativeLayout.LayoutParams(screenWidth/2, screenWidth/2);
        svLp2.topMargin = margin;
        svLp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ptz2PlayView.setLayoutParams(svLp2);

        RelativeLayout.LayoutParams svLp3 = new RelativeLayout.LayoutParams(smallWidth, smallWidth);
        svLp3.topMargin = margin+screenWidth/2;
        ptz3PlayView.setLayoutParams(svLp3);

        RelativeLayout.LayoutParams svLp4 = new RelativeLayout.LayoutParams(smallWidth, smallWidth);
        svLp4.topMargin = margin+screenWidth/2;
        svLp4.leftMargin = smallWidth;
        ptz4PlayView.setLayoutParams(svLp4);

        RelativeLayout.LayoutParams svLp5 = new RelativeLayout.LayoutParams(smallWidth, smallWidth);
        svLp5.topMargin = margin+screenWidth/2;
        svLp5.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ptz5PlayView.setLayoutParams(svLp5);

        ptz5PlayView.post(() -> {
            if (callback != null) {
                callback.onLayoutViewComplete();
            }
        });
    }

    private void layoutViewsForFull5PTZ(LayoutViewsCallback callback) {
        setPlayViewAspectRadioWith1V1();
        ptz1PlayView.setVisibility(View.VISIBLE);
        ptz2PlayView.setVisibility(View.VISIBLE);
        ptz3PlayView.setVisibility(View.VISIBLE);
        ptz4PlayView.setVisibility(View.VISIBLE);
        ptz5PlayView.setVisibility(View.VISIBLE);
        ptz6PlayView.setVisibility(View.VISIBLE);

        LocalInfo mLocalInfo = LocalInfo.getInstance();
        final int screenWidth = mLocalInfo.getScreenWidth();
        int smallWidth = screenWidth/3;// 较小的视图宽度

        RelativeLayout.LayoutParams svLp1 = new RelativeLayout.LayoutParams(smallWidth*2, smallWidth*2);
        ptz1PlayView.setLayoutParams(svLp1);

        RelativeLayout.LayoutParams svLp2 = new RelativeLayout.LayoutParams(smallWidth, smallWidth);
        svLp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ptz2PlayView.setLayoutParams(svLp2);

        RelativeLayout.LayoutParams svLp3 = new RelativeLayout.LayoutParams(smallWidth, smallWidth);
        svLp3.topMargin = smallWidth;
        svLp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ptz3PlayView.setLayoutParams(svLp3);

        RelativeLayout.LayoutParams svLp4 = new RelativeLayout.LayoutParams(smallWidth, smallWidth);
        svLp4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        svLp4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ptz4PlayView.setLayoutParams(svLp4);

        RelativeLayout.LayoutParams svLp5 = new RelativeLayout.LayoutParams(smallWidth, smallWidth);
        svLp5.topMargin = smallWidth*2;
        svLp5.leftMargin = smallWidth;
        ptz5PlayView.setLayoutParams(svLp5);

        RelativeLayout.LayoutParams svLp6 = new RelativeLayout.LayoutParams(smallWidth, smallWidth);
        svLp6.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ptz6PlayView.setLayoutParams(svLp6);

        ptz6PlayView.post(() -> {
            if (callback != null) {
                callback.onLayoutViewComplete();
            }
        });
    }

    /**
     * 显示1个分屏 & 画面比例调整为1:1
     */
    private void layoutViewsFor1PTZ1V1(LayoutViewsCallback callback) {
        setPlayViewAspectRadioWith1V1();
        setAllFecPlayViewsHidden();
        ptz1PlayView.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ptz1PlayView.setLayoutParams(svLp);
        ptz1PlayView.post(() -> {
            if (callback != null) {
                callback.onLayoutViewComplete();
            }
        });
    }

    /**
     * 显示1个分屏 & 画面比例调整为16:9
     */
    private void layoutViewsFor1PTZ16V9(LayoutViewsCallback callback) {
        setPlayViewAspectRadioWith16V9();
        setAllFecPlayViewsHidden();
        ptz1PlayView.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ptz1PlayView.setLayoutParams(svLp);
        ptz1PlayView.requestLayout();
        ptz1PlayView.post(() -> {
            if (callback != null) {
                callback.onLayoutViewComplete();
            }
        });
    }

    /**
     * 画面比例调整为1:1
     */
    public void setPlayViewAspectRadioWith1V1() {
        if (playWindowVg.getWidth() == playWindowVg.getHeight()) {
            return;
        }
        LocalInfo mLocalInfo = LocalInfo.getInstance();
        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = mLocalInfo.getScreenHeight() - mLocalInfo.getNavigationBarHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(1, Configuration.ORIENTATION_PORTRAIT,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);

        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        playWindowVg.setLayoutParams(svLp);

        if (fecPopupWindow != null) {
            float dis = screenWidth * (1 - Constant.LIVE_VIEW_RATIO);
            fecPopupWindow.update(screenWidth, fecPopupWindow.getHeight() - (int) dis);
        }
    }

    /**
     * 画面比例调整为16:9
     */
    public void setPlayViewAspectRadioWith16V9() {
        if (playWindowVg.getWidth() != playWindowVg.getHeight()) {
            return;
        }
        LocalInfo mLocalInfo = LocalInfo.getInstance();
        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = mLocalInfo.getScreenHeight() - mLocalInfo.getNavigationBarHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(Constant.LIVE_VIEW_RATIO,
                Configuration.ORIENTATION_PORTRAIT, mLocalInfo.getScreenWidth(),
                (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO), screenWidth, screenHeight);

        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        playWindowVg.setLayoutParams(svLp);

        if (fecPopupWindow != null) {
            float dis = screenWidth * (1 - Constant.LIVE_VIEW_RATIO);
            fecPopupWindow.update(screenWidth, fecPopupWindow.getHeight() + (int) dis);
        }
    }


    /**
     * 所有的子视图都不显示
     */
    private void setAllFecPlayViewsHidden() {
        ptz1PlayView.setVisibility(View.GONE);
        ptz2PlayView.setVisibility(View.GONE);
        ptz3PlayView.setVisibility(View.GONE);
        ptz4PlayView.setVisibility(View.GONE);
        ptz5PlayView.setVisibility(View.GONE);
        ptz6PlayView.setVisibility(View.GONE);
    }

    /**
     * 是否支持矫正模式
     */
    public static boolean isFecDevice(EZDeviceInfo deviceInfo) {
        boolean supportWallType = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_WALL, deviceInfo) > 0;
        boolean supportFloorType = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_FLOOR, deviceInfo) > 0;
        boolean supportCeilingType = FecViewLayoutHelper.getSupportInt(EZFecPlaceType.EZ_FEC_PLACE_CEILING, deviceInfo) > 0;
        // 顶装、壁装、底装都不支持的话隐藏"查看模式"，只有鱼眼设备支持这几种安装方式
        boolean isFecDevice = supportWallType || supportFloorType || supportCeilingType;

        return isFecDevice;
    }

    /**
     * 获取安装模式对应的能力集值
     */
    public static int getSupportInt(EZFecPlaceType fecPlaceType, EZDeviceInfo deviceInfo) {
        /**
         能力集312位 : 顶装矫正模式
         能力集313位 : 壁装矫正模式
         能力集666位 : 底装矫正模式
         */
        int supportValue = 0;
        if (fecPlaceType == EZFecPlaceType.EZ_FEC_PLACE_CEILING) {
            supportValue = deviceInfo.getSupportInt(312);
        } else if (fecPlaceType == EZFecPlaceType.EZ_FEC_PLACE_WALL) {
            supportValue = deviceInfo.getSupportInt(313);
        } else if (fecPlaceType == EZFecPlaceType.EZ_FEC_PLACE_FLOOR) {
            supportValue = deviceInfo.getSupportInt(666);
        }

        return supportValue;
    }

    /**
     * 根据能力值设置各种矫正模式按钮的状态
     */
    public static void setFecCorrectButtonsState(Button[] buttons, int supportValue) {
        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            int correctType = Integer.parseInt(String.valueOf(button.getTag()));
            boolean enable = (supportValue & correctType) > 0;

            button.setEnabled(enable);
        }
    }

    /**
     * 将tag转为FecCorrectType
     */
    public static EZFecCorrectType getFecCorrectTypeFromTag(int tag) {
        switch (tag) {
            case 1:
                return EZFecCorrectType.EZ_FEC_CORRECT_4PTZ;
            case 2:
                return EZFecCorrectType.EZ_FEC_CORRECT_180;
            case 4:
                return EZFecCorrectType.EZ_FEC_CORRECT_360;
            case 8:
                return EZFecCorrectType.EZ_FEC_CORRECT_FISH;
            case 16:
                return EZFecCorrectType.EZ_FEC_CORRECT_LAT;
            case 32:
                return EZFecCorrectType.EZ_FEC_CORRECT_ARC_HOR;
            case 64:
                return EZFecCorrectType.EZ_FEC_CORRECT_ARC_VER;
//            case 128:
//                return EZFecCorrectType.EZ_FEC_CORRECT_PICINPIC;
            case 256:
                return EZFecCorrectType.EZ_FEC_CORRECT_FULL5PTZ;
            case 512:
                return EZFecCorrectType.EZ_FEC_CORRECT_5PTZ;
            case 1024:
                return EZFecCorrectType.EZ_FEC_CORRECT_CYC;
            case 2048:
                return EZFecCorrectType.EZ_FEC_CORRECT_WIDEANGLE;
//            case 4096:
//                return EZFecCorrectType.EZ_FEC_CORRECT_TILED;
            default:
                return EZFecCorrectType.EZ_FEC_CORRECT_NONE;
        }
    }

    public interface LayoutViewsCallback {

        void onLayoutViewComplete();
    }
}
