/**
 * @ProjectName: null
 * @Copyright: null
 * @address: https://www.ys7.com
 * @date: 2014-6-5 下午4:41:40
 * @Description: null
 */
package com.videogo.ui.playback.querylist;

import com.videogo.ui.playback.bean.ClickedListItem;

public interface RemoteListClickListener {

    void onMoreBtnClick(int position, boolean notExpand);

    void onListItemClick(ClickedListItem playClickItem);

}
