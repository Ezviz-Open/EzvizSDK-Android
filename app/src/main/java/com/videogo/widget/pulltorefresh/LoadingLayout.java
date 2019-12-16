package com.videogo.widget.pulltorefresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.videogo.widget.pulltorefresh.PullToRefreshBase.Orientation;

@SuppressLint("ViewConstructor")
public abstract class LoadingLayout extends FrameLayout {

    protected final boolean mHeaderOrFooter;

    protected final Orientation mScrollDirection;

    private View mContentView;
    private Runnable mPostRenderRunnable;

    public LoadingLayout(Context context, final boolean headerOrFooter, final Orientation scrollDirection) {
        super(context);
        mHeaderOrFooter = headerOrFooter;
        mScrollDirection = scrollDirection;
    }

    protected void setContentView(int layoutResID) {
        mContentView = LayoutInflater.from(getContext()).inflate(layoutResID, this, false);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContentView.getLayoutParams();
        if (mHeaderOrFooter)
            lp.gravity = mScrollDirection == Orientation.VERTICAL ? Gravity.BOTTOM : Gravity.RIGHT;
        else
            lp.gravity = mScrollDirection == Orientation.VERTICAL ? Gravity.TOP : Gravity.LEFT;
        addView(mContentView, lp);
    }

    public final void setHeight(int height) {
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getLayoutParams();
        lp.height = height;
        requestLayout();
    }

    public final void setWidth(int width) {
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getLayoutParams();
        lp.width = width;
        requestLayout();
    }

    public int getContentSize(Orientation orientation) {
        switch (orientation) {
            case HORIZONTAL:
                return mContentView.getWidth();
            case VERTICAL:
            default:
                return mContentView.getHeight();
        }
    }

    public abstract void pullToRefresh();

    public abstract void refreshing();

    public abstract void releaseToRefresh();

    public abstract void onPull(float scaleOfLayout);

    public abstract void reset();

    public abstract void disableRefresh();

    public final void showInvisibleViews() {
        if (View.INVISIBLE == mContentView.getVisibility()) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    public final void hideAllViews() {
        if (View.VISIBLE == mContentView.getVisibility()) {
            mContentView.setVisibility(View.INVISIBLE);
        }
    }

    public void postRenderRunnable(Runnable r) {
        mPostRenderRunnable = r;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        boolean result = mScrollDirection == Orientation.VERTICAL ? (bottom - top) > 0 : (right - left) > 0;
        if (result && mPostRenderRunnable != null) {
            mPostRenderRunnable.run();
            mPostRenderRunnable = null;
        }
    }
}