/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.videogo.widget.pulltorefresh;

import android.view.View;
import android.view.animation.Interpolator;

public interface IPullToRefresh<T extends View> {

    public static enum Mode {

        DISABLED(0x0),

        PULL_FROM_START(0x1),

        PULL_FROM_END(0x2),

        BOTH(0x3),

        MANUAL_REFRESH_ONLY(0x4);

        static Mode mapIntToValue(final int modeInt) {
            for (Mode value : Mode.values()) {
                if (modeInt == value.getIntValue()) {
                    return value;
                }
            }

            // If not, return default
            return getDefault();
        }

        static Mode getDefault() {
            return DISABLED;
        }

        private int mIntValue;

        // The modeInt values need to match those from attrs.xml
        Mode(int modeInt) {
            mIntValue = modeInt;
        }

        boolean permitsPullToRefresh() {
            return !(this == DISABLED || this == MANUAL_REFRESH_ONLY);
        }

        public boolean showHeaderLoadingLayout() {
            return this == PULL_FROM_START || this == BOTH;
        }

        public boolean showFooterLoadingLayout() {
            return this == PULL_FROM_END || this == BOTH || this == MANUAL_REFRESH_ONLY;
        }

        int getIntValue() {
            return mIntValue;
        }
    }

    public static enum State {

        RESET(0x0),

        PULL_TO_REFRESH(0x1),

        RELEASE_TO_REFRESH(0x2),

        REFRESHING(0x8),

        MANUAL_REFRESHING(0x9),

        OVERSCROLLING(0x10);

        static State mapIntToValue(final int stateInt) {
            for (State value : State.values()) {
                if (stateInt == value.getIntValue()) {
                    return value;
                }
            }

            // If not, return default
            return RESET;
        }

        private int mIntValue;

        State(int intValue) {
            mIntValue = intValue;
        }

        int getIntValue() {
            return mIntValue;
        }
    }

    public static interface OnRefreshListener<V extends View> {

        public void onRefresh(final PullToRefreshBase<V> refreshView, boolean headerOrFooter);

    }

    public static interface OnPullEventListener<V extends View> {

        public void onPullEvent(final PullToRefreshBase<V> refreshView, State state, Mode direction);

    }

    public boolean demo();

    public Mode getCurrentMode();

    public boolean getFilterTouchEvents();

    public LoadingLayoutProxy getLoadingLayoutProxy();

    public LoadingLayoutProxy getLoadingLayoutProxy(boolean includeStart, boolean includeEnd);

    public Mode getMode();

    public T getRefreshableView();

    public boolean getShowViewWhileRefreshing();

    public State getState();

    public boolean isPullToRefreshEnabled();

    public boolean isPullToRefreshOverScrollEnabled();

    public boolean isRefreshing();

    public boolean isScrollingWhileRefreshingEnabled();

    public void onRefreshComplete();

    public void setFilterTouchEvents(boolean filterEvents);

    public void setMode(Mode mode);

    public void setOnPullEventListener(OnPullEventListener<T> listener);

    public void setOnRefreshListener(OnRefreshListener<T> listener);

    public void setPullToRefreshOverScrollEnabled(boolean enabled);

    public void setRefreshing();

    public void setRefreshing(boolean doScroll);

    public void setScrollAnimationInterpolator(Interpolator interpolator);

    public void setScrollingWhileRefreshingEnabled(boolean scrollingWhileRefreshingEnabled);

    public void setShowViewWhileRefreshing(boolean showView);

}