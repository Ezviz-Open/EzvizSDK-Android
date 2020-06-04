package com.videogo.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ezviz.ezopensdk.R;


public class GroupLayout extends LinearLayout {

    private int mDividerColor;
    private int mDividerHeight;
    private int mDividerPadding;

    private LayoutParamsHolder mLayoutParamsHolder;

    private Paint mDividerPaint;
    private int mItemSelectorResId;

    public GroupLayout(Context context) {
        this(context, null);
    }

    public GroupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GroupLayout);

        mDividerHeight = a.getDimensionPixelSize(R.styleable.GroupLayout_dividerHeight, 0);
        mDividerPadding = a.getDimensionPixelSize(R.styleable.GroupLayout_dividerPadding, 0);
        mDividerColor = a.getColor(R.styleable.GroupLayout_dividerColor, Color.TRANSPARENT);
        mItemSelectorResId = a.getResourceId(R.styleable.GroupLayout_itemSelector, 0);

        a.recycle();

        mDividerPaint = new Paint();
        mDividerPaint.setColor(mDividerColor);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int visibleChildCount = 0;
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getVisibility() != View.GONE)
                visibleChildCount++;
        }

        if (visibleChildCount == 0) {
            if (mLayoutParamsHolder == null) {
                if (getLayoutParams() instanceof MarginLayoutParams) {
                    MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();

                    mLayoutParamsHolder = new LayoutParamsHolder();
                    mLayoutParamsHolder.topMargin = marginLayoutParams.topMargin;
                    mLayoutParamsHolder.bottomMargin = marginLayoutParams.bottomMargin;

                    marginLayoutParams.topMargin = 0;
                    marginLayoutParams.bottomMargin = 0;
                }
            }
            setMeasuredDimension(getMeasuredWidth(), 0);
        } else {
            if (mLayoutParamsHolder != null) {
                if (getLayoutParams() instanceof MarginLayoutParams) {
                    MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();

                    marginLayoutParams.topMargin = mLayoutParamsHolder.topMargin;
                    marginLayoutParams.bottomMargin = mLayoutParamsHolder.bottomMargin;

                    mLayoutParamsHolder = null;
                }
            }
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + (visibleChildCount + 1) * mDividerHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int visibleChildCount = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                visibleChildCount++;
                int offset = (visibleChildCount) * mDividerHeight;
                child.layout(child.getLeft(), child.getTop() + offset, child.getRight(), child.getBottom() + offset);
            }
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        child.setBackgroundResource(mItemSelectorResId);
        super.addView(child, index, params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int visibleChildCount = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                canvas.drawRect(visibleChildCount == 0 ? 0 : mDividerPadding, child.getTop() - mDividerHeight,
                        getMeasuredWidth(), child.getTop(), mDividerPaint);
                visibleChildCount++;
            }
        }

        if (visibleChildCount > 0)
            canvas.drawRect(0, getMeasuredHeight() - mDividerHeight, getMeasuredWidth(), getMeasuredHeight(),
                    mDividerPaint);
    }

    protected static class LayoutParamsHolder {
        int topMargin;
        int bottomMargin;
    }
}