package com.videogo.widget.bottommenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

@SuppressLint("ValidFragment")
public class BottomMenuFragment extends DialogFragment {

    private final String TAG = "BottomMenuFragment";
    private Activity context;
    //标题
    private boolean showTitle = false;
    //取消文字大小、颜色、文本内容
    private int cancelTextSize = 16;
    private String cancelTextColor = "#333333";
    private String cancelTextTitle = "取消";
    //标题文字大小、内容、分割线等
    private String headTextTitle = "请选择类型";
    private String headTextColor = "#adadad";
    private int headTextSize = 14;

    private int contentSize = 15;
    private double lineHeight = 1;
    private String contentColor = "#333333";
    private String selectContentColor = "#467CD4";
    private String selectContent = "";
    private String lineColor = "#ebebeb";
    //内容list
    private List<String> menuItemList = new ArrayList<>();
    //取消高度
    private int cancelTextHeight = 45;
    //顶部边距
    private int cancelTextTopMargin = 20;
    //取消样式
    private int cancelTextShape = R.drawable.bottom_menu_selector;
    //距离四周边距
    private int leftPadding = 20;
    private int topPadding = 0;
    private int rightPadding = 20;
    private int bottomPadding = 20;
    //背景颜色
    private String backgroundColor = "#00000000";
    //资源shape
    private int sizeShape = R.drawable.bottom_menu_selector;
    private int topShape = R.drawable.bottom_menu_top_selector;
    private int middleShape = R.drawable.bottom_menu_mid_selector;
    private int bottomShape = R.drawable.bottom_menu_bottom_selector;


    private MenuAdapter adapter;

    @SuppressLint("ValidFragment")
    public BottomMenuFragment(Activity context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //去除标题
//        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置背景透明
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //添加一组进出动画
        getDialog().getWindow().setWindowAnimations(R.style.menu_animation);
        //寻找布局资源
        View view = inflater.inflate(R.layout.fragment_bottom_menu, container, false);
        TextView head = view.findViewById(R.id.tv_head);
        TextView cancel = view.findViewById(R.id.tv_cancel);
        LinearLayout linear = view.findViewById(R.id.linear_view);
        //文本
        head.setText(headTextTitle);
        //字体大小
        head.setTextSize(headTextSize);
        //字体颜色
        head.setTextColor(Color.parseColor(headTextColor));
        //样式
        head.setBackgroundResource(R.drawable.bottom_menu_top_selector);

        //文本
        cancel.setText(cancelTextTitle);
        //字体大小
        cancel.setTextSize(cancelTextSize);
        //字体颜色
        cancel.setTextColor(Color.parseColor(cancelTextColor));
        //四周边距
        cancel.setHeight(dip2px(cancelTextHeight));
        //样式
        cancel.setBackgroundResource(cancelTextShape);
        //顶部边距
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cancel.getLayoutParams();
        lp.topMargin = cancelTextTopMargin;
        cancel.setLayoutParams(lp);
        //四周边距
        linear.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        //背景色
        linear.setBackgroundColor(Color.parseColor(backgroundColor));
        //关闭弹框事件
        cancel.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭弹框
                dismiss();
            }
        });
        //展示数据源
        ListView listView = view.findViewById(R.id.listView);
        adapter = new MenuAdapter(context, menuItemList, showTitle);
        listView.setAdapter(adapter);
        adapter.setContentColor(contentColor);
        adapter.setSelectContentColor(selectContentColor);
        adapter.setSelectContent(selectContent);
        adapter.setContentSize(contentSize);
        adapter.setLineHeight(lineHeight);
        adapter.setLineColor(lineColor);
        //资源shape
        adapter.setSizeOneShape(sizeShape);
        adapter.setTopShape(topShape);
        adapter.setMiddleShape(middleShape);
        adapter.setBottomShape(bottomShape);
        adapter.setOnItemClickListener((position) -> {
            if (null != listener) {
                listener.onItemClick(position);
                dismiss();
            }
        });
        return view;
    }

    /**
     * 展示
     */
    public void show() {
        this.show(context.getFragmentManager(), "BottomMenuFragment");
    }

    /**
     * 点击事件回调
     */
    private OnItemClickListener listener;

    public interface OnItemClickListener {

        void onItemClick(int position);
    }

    public BottomMenuFragment setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.listener = mOnItemClickListener;
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置弹出框宽屏显示，适应屏幕宽度
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getDialog().getWindow().setLayout(dm.widthPixels, getDialog().getWindow().getAttributes().height);
        //移动弹出菜单到底部
        WindowManager.LayoutParams manger = getDialog().getWindow().getAttributes();
        manger.gravity = Gravity.BOTTOM;
        //manger.width = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(manger);
    }

    @Override
    public void onStop() {
        //设置暂停动画
        this.getView().setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.menu_disappear));
        super.onStop();
    }

    /**
     * dp2px
     */
    private int dip2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 添加内容弹框
     */
    public BottomMenuFragment addMenuItems(List<String> menuItems) {
        if (null != menuItems && menuItems.size() > 0) {
            for (int i = 0; i < menuItems.size(); i++) {
                menuItemList.add(menuItems.get(i));
            }
        }
        return this;
    }

    /**
     * 底部取消文字
     */
    public BottomMenuFragment setCancelTextTitle(String textTitle) {
        cancelTextTitle = textTitle;
        return this;
    }

    /**
     * 底部取消文字大小
     */
    public BottomMenuFragment setCancelTextSize(int textSize) {
        cancelTextSize = textSize;
        return this;
    }

    /**
     * 底部取消文字颜色
     */
    public BottomMenuFragment setCancelTextColor(String textColor) {
        cancelTextColor = textColor;
        return this;
    }

    /**
     * 底部取消文字高度
     */
    public BottomMenuFragment setCancelTextHeight(int height) {
        cancelTextHeight = height;
        return this;
    }

    /**
     * 底部取消文字边距(距离顶部)
     */
    public BottomMenuFragment setCancelTextMarginTop(int marginTop) {
        cancelTextTopMargin = marginTop;
        return this;
    }

    /**
     * 底部整体边距
     */
    public BottomMenuFragment setCancelPadding(int left, int top, int right, int bottom) {
        leftPadding = left;
        topPadding = top;
        rightPadding = right;
        bottomPadding = bottom;
        return this;
    }

    /**
     * 取消样式
     */
    public BottomMenuFragment setCancelShape(int resId) {
        cancelTextShape = resId;
        return this;
    }

    /**
     * 底部整体背景
     */
    public BottomMenuFragment setBackgroundColor(String color) {
        backgroundColor = color;
        return this;
    }

    /**
     * 设置标题
     */
    public BottomMenuFragment setHeadTitle(String BottomTitle) {
        showTitle = true;
        //添加标题
        headTextTitle = BottomTitle;
        return this;
    }

    /**
     * 字体大小
     */
    public BottomMenuFragment setHeadTitleSize(int size) {
        headTextSize = size;
        return this;
    }

    /**
     * 字体颜色
     */
    public BottomMenuFragment setHeadTitleColor(String color) {
        headTextColor = color;
        return this;
    }

    /**
     * 内容大小
     */
    public BottomMenuFragment setContentSize(int size) {
        contentSize = size;
        return this;
    }

    /**
     * 内容颜色
     */
    public BottomMenuFragment setContentColor(String color) {
        contentColor = color;
        return this;
    }

    /**
     * 选中项内容颜色
     */
    public BottomMenuFragment setSelectContentColor(String color) {
        selectContentColor = color;
        return this;
    }

    /**
     * 选中项内容
     */
    public BottomMenuFragment setSelectContent(String content) {
        selectContent = content;
        return this;
    }

    /**
     * 分割线高度
     */
    public BottomMenuFragment setLineHeight(double height) {
        lineHeight = height;
        return this;
    }

    /**
     * 分割线颜色
     */
    public BottomMenuFragment setLineColor(String color) {
        lineColor = color;
        return this;
    }

    /**
     * 背景颜色(list大小为1时)
     */
    public BottomMenuFragment setSizeOneShape(int resId) {
        sizeShape = resId;
        return this;
    }

    public BottomMenuFragment setTopShape(int resId) {
        topShape = resId;
        return this;
    }

    public BottomMenuFragment setMiddleShape(int resId) {
        middleShape = resId;
        return this;
    }

    public BottomMenuFragment setBottomShape(int resId) {
        bottomShape = resId;
        return this;
    }
}
