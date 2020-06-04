/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright null
 * 
 * @FileName MenuAdapter.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2015-5-12
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.widget;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.videogo.util.Utils;

import java.util.List;

import ezviz.ezopensdk.R;

public class MenuAdapter extends ArrayAdapter<TitleMenuItem> {
    private Context mContext = null;
    public MenuAdapter(Context context, List<TitleMenuItem> objects) {
        super(context, 0, objects);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setPadding(Utils.dip2px(getContext(), 10), 0, Utils.dip2px(getContext(), 10), 0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            textView.setCompoundDrawablePadding(Utils.dip2px(getContext(), 4));
            textView.setTextColor(mContext.getResources().getColorStateList(R.color.title_down_text_selector));
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
                    Utils.dip2px(mContext, 40));
            textView.setLayoutParams(layoutParams);
            convertView = textView;
        } else {
            textView = (TextView) convertView;
        }

        TitleMenuItem menuItem = getItem(position);
        textView.setText(menuItem.text);
        textView.setCompoundDrawablesWithIntrinsicBounds(menuItem.iconResId, 0, 0, 0);

        return convertView;
    }   
}
