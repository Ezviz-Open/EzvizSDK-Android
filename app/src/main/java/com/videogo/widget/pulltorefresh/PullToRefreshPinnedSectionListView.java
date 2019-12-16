package com.videogo.widget.pulltorefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;

import com.videogo.widget.PinnedSectionListView;

public class PullToRefreshPinnedSectionListView extends PullToRefreshAdapterViewBase<PinnedSectionListView> {

    public PullToRefreshPinnedSectionListView(Context context) {
        super(context);
    }

    public PullToRefreshPinnedSectionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshPinnedSectionListView(Context context, Mode mode) {
        super(context, mode);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected final PinnedSectionListView createRefreshableView(Context context, AttributeSet attrs) {
        final PinnedSectionListView lv;
        if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
            lv = new InternalPinnedSectionListViewSDK9(context, attrs);
        } else {
            lv = new InternalPinnedSectionListView(context, attrs);
        }

        return lv;
    }

    class InternalPinnedSectionListView extends PinnedSectionListView implements EmptyViewMethodAccessor {

        public InternalPinnedSectionListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void setEmptyView(View emptyView) {
            PullToRefreshPinnedSectionListView.this.setEmptyView(emptyView);
        }

        @Override
        public void setEmptyViewInternal(View emptyView) {
            super.setEmptyView(emptyView);
        }
    }

    @TargetApi(9)
    final class InternalPinnedSectionListViewSDK9 extends InternalPinnedSectionListView {

        public InternalPinnedSectionListViewSDK9(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
                int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

            final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                    scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

            // Does all of the hard work...
            OverscrollHelper.overScrollBy(PullToRefreshPinnedSectionListView.this, deltaX, scrollX, deltaY, scrollY,
                    isTouchEvent);

            return returnValue;
        }
    }
}
