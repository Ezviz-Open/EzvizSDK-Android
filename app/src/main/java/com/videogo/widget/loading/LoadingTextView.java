package com.videogo.widget.loading;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import ezviz.ezopensdk.R;

public class LoadingTextView extends FrameLayout {

    public static final int NOTEXT = -1;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private LinearLayout mParentLayout;
    private LoadingView mLoadingView;
    private TextView mTextView;

    private int mTextPadding;

    public LoadingTextView(Context context) {
        this(context, null);
    }

    public LoadingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingTextView, defStyle,
                R.style.LoadingTextView);
        int style = a.getInt(R.styleable.LoadingTextView_textStyle, LinearLayout.VERTICAL);
        int gravity = a.getInt(R.styleable.LoadingTextView_gravity, Gravity.CENTER);
        ColorStateList textColor = a.getColorStateList(R.styleable.LoadingTextView_textColor);
        int textSize = a.getDimensionPixelOffset(R.styleable.LoadingTextView_textSize, 15);
        mTextPadding = a.getDimensionPixelOffset(R.styleable.LoadingTextView_textPadding, 0);
        CharSequence text = a.getText(R.styleable.LoadingTextView_android_text);
        a.recycle();

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.loading_text_view, this, false);
        addView(view);

        mParentLayout = (LinearLayout) view;
        mLoadingView = (LoadingView) view.findViewById(R.id.loading_view);
        mTextView = (TextView) view.findViewById(R.id.loading_text);

        setTextStyle(style);
        mParentLayout.setGravity(gravity);

        mTextView.setTextColor(textColor);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        setText(text);
    }

    public void setTextStyle(int style) {
        if (style == -1) {
            mTextView.setVisibility(View.GONE);
        } else {
            if (style != mParentLayout.getOrientation()) {
                mParentLayout.setOrientation(style);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mTextView.getLayoutParams();
                if (style == LinearLayout.VERTICAL) {
                    layoutParams.leftMargin = 0;
                    layoutParams.topMargin = mTextPadding;
                } else {
                    layoutParams.leftMargin = mTextPadding;
                    layoutParams.topMargin = 0;
                }
            }
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
    }

    public void setText(int resid) {
        mTextView.setText(resid);
    }
}