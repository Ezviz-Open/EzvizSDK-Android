package com.videogo.widget.bottommenu;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ezviz.ezopensdk.R;

/**
 * Copyright (C) 2024 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2024/9/2 15:12
 */
public class MenuAdapter extends BaseAdapter {

    private Context context;//上下文
    private List<String> list;//数据源
    private OnItemClickListener listener;//点击事件监听器


    private int contentSize;
    private double lineHeight;
    private String contentColor;
    private String selectContentColor;
    private String selectContent;
    private String lineColor;
    //资源shape
    private int sizeShape;
    private int topShape;
    private int middleShape;
    private int bottomShape;
    //是否有标题
    private boolean isShowTitle;

    public MenuAdapter(Context context, List<String> list, boolean isShowTitle) {
        this.context = context;
        this.list = list;
        this.isShowTitle = isShowTitle;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        String item = "";
        if (position >= 0 && getCount() > position) {
            item = list.get(position);
        }
        return item;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 自定义视图
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            // 获取item_menu布局文件的视图
            convertView = LayoutInflater.from(context).inflate(R.layout.item_menu, null);
            viewHolder.menuItemTv = convertView.findViewById(R.id.menu_item);
            viewHolder.lineView = convertView.findViewById(R.id.view_line);
            // 设置控件集到convertView
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String value = getItem(position);
        viewHolder.menuItemTv.setText(value);
        if (value.equals(selectContent)) {
            viewHolder.menuItemTv.setTextColor(Color.parseColor(selectContentColor));
        } else {
            viewHolder.menuItemTv.setTextColor(Color.parseColor(contentColor));
        }

        if (position == 0) {// 第一个position，没有标题时，上面左右设置圆角
            viewHolder.menuItemTv.setBackgroundResource(isShowTitle ? middleShape : topShape);
            viewHolder.menuItemTv.setTextSize(contentSize);
        } else if (position < getCount() - 1) {//中间选项不需要圆角
            viewHolder.menuItemTv.setBackgroundResource(middleShape);
            viewHolder.menuItemTv.setTextSize(contentSize);
        } else {// 将底部也就是第一个position下面面左右圆角设置
            viewHolder.menuItemTv.setBackgroundResource(bottomShape);
            viewHolder.menuItemTv.setTextSize(contentSize);
        }
        // 分割线
        if (position == getCount() - 1) {
            viewHolder.lineView.setVisibility(View.GONE);
        } else {
            viewHolder.lineView.setVisibility(View.VISIBLE);
        }
        // 设置分割线高度
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) lineHeight);
        viewHolder.lineView.setLayoutParams(params);
        // 设置颜色
        viewHolder.lineView.setBackgroundColor(Color.parseColor(lineColor));

        viewHolder.menuItemTv.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        public TextView menuItemTv;
        public View lineView;
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 定义一个点击事件接口回调
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * 内容大小
     */
    public MenuAdapter setContentSize(int size) {
        contentSize = size;
        return this;
    }

    /**
     * 内容颜色
     */
    public MenuAdapter setContentColor(String color) {
        contentColor = color;
        return this;
    }

    /**
     * 选中项内容颜色
     */
    public MenuAdapter setSelectContentColor(String color) {
        selectContentColor = color;
        return this;
    }

    /**
     * 选中项内容
     */
    public MenuAdapter setSelectContent(String content) {
        selectContent = content;
        return this;
    }

    /**
     * 分割线高度
     */
    public MenuAdapter setLineHeight(double size) {
        lineHeight = size;
        return this;
    }

    /**
     * 分割线颜色
     */
    public MenuAdapter setLineColor(String color) {
        lineColor = color;
        return this;
    }

    /**
     * 背景颜色(list大小为1时)
     */
    public MenuAdapter setSizeOneShape(int resId) {
        sizeShape = resId;
        return this;
    }

    public MenuAdapter setTopShape(int resId) {
        topShape = resId;
        return this;
    }

    public MenuAdapter setMiddleShape(int resId) {
        middleShape = resId;
        return this;
    }

    public MenuAdapter setBottomShape(int resId) {
        bottomShape = resId;
        return this;
    }

}
