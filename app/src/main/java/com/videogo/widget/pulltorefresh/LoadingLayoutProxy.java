package com.videogo.widget.pulltorefresh;

import java.util.HashSet;

public class LoadingLayoutProxy {

    private final HashSet<LoadingLayout> mLoadingLayouts;

    LoadingLayoutProxy() {
        mLoadingLayouts = new HashSet<LoadingLayout>();
    }

    public void addLayout(LoadingLayout layout) {
        if (null != layout) {
            mLoadingLayouts.add(layout);
        }
    }

    public HashSet<LoadingLayout> getLayouts() {
        return mLoadingLayouts;
    }
}