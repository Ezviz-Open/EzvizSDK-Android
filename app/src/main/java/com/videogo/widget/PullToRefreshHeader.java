package com.videogo.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.Orientation;

import ezviz.ezopensdk.R;

public class PullToRefreshHeader extends LoadingLayout {

    public enum Style {
        NORMAL, NO_TIME, MORE
    }

    private ImageView mArrowImageView;
    private ProgressBar mProgressBar;
    private TextView mHintTextView, mHeaderTimeView, mHintMoreView;
    private ViewGroup mHeaderTimelayout;

    private Animation mRotateUpAnim, mRotateDownAnim;

    private Style mStyle = Style.NORMAL;

    private final static int ROTATE_ANIM_DURATION = 180;

    public PullToRefreshHeader(Context context) {
        this(context, Style.NORMAL);
    }

    public PullToRefreshHeader(Context context, Style style) {
        super(context, true, Orientation.VERTICAL);
        setContentView(R.layout.pull_to_refresh_header);

        mArrowImageView = (ImageView) findViewById(R.id.header_arrow);
        mHintTextView = (TextView) findViewById(R.id.header_hint);
        mHeaderTimeView = (TextView) findViewById(R.id.header_time);
        mProgressBar = (ProgressBar) findViewById(R.id.header_progress);
        mHeaderTimelayout = (ViewGroup) findViewById(R.id.header_time_layout);
        mHintMoreView = (TextView) findViewById(R.id.header_hint_more);
        
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

        if (style == Style.NO_TIME) {
            mHeaderTimelayout.setVisibility(View.GONE);
        } else if (style == Style.MORE) {
            mHeaderTimelayout.setVisibility(View.GONE);
        }
        mStyle = style;
    }

    @Override
    public void pullToRefresh() {
        if (mStyle == Style.MORE) {
            mHintTextView.setText(R.string.xlistview_header_hint_more);
        } else {
            mHintTextView.setText(R.string.xlistview_header_hint_normal);
        }
        if (mRotateUpAnim == mArrowImageView.getAnimation()) {
            mArrowImageView.startAnimation(mRotateDownAnim);
        }
    }

    @Override
    public void refreshing() {
        mHintTextView.setText(R.string.xlistview_header_hint_loading);
        mArrowImageView.clearAnimation();
        mArrowImageView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void releaseToRefresh() {
        if (mStyle == Style.MORE) {
            mHintTextView.setText(R.string.xlistview_footer_hint_ready);
        } else {
            mHintTextView.setText(R.string.xlistview_header_hint_ready);
        }
        mArrowImageView.startAnimation(mRotateUpAnim);
    }

    @Override
    public void onPull(float scaleOfLayout) {
    }

    @Override
    public void reset() {
        if (mStyle == Style.MORE) {
            mHintTextView.setText(R.string.xlistview_header_hint_more);
        } else {
            mHintTextView.setText(R.string.xlistview_header_hint_normal);
        }
        mArrowImageView.clearAnimation();
        mArrowImageView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mHintMoreView.setVisibility(View.GONE);
    }

    @Override
    public void disableRefresh() {
        if (mStyle == Style.MORE) {
            mHintMoreView.setVisibility(View.VISIBLE);
            mHintTextView.setText(R.string.xlistview_footer_no_more);
        }
    }

    public void setLastRefreshTime(CharSequence text) {
        mHeaderTimeView.setText(text);
    }
}