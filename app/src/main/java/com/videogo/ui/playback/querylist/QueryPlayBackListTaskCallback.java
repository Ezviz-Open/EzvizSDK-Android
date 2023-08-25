/**
 * @ProjectName: null
 * @Copyright: null
 * @address: https://www.ys7.com
 * @date: 2014-6-6 上午8:57:54
 * @Description: null
 */
package com.videogo.ui.playback.querylist;

import java.util.List;

import com.videogo.ui.playback.bean.CloudPartInfoFileEx;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;

public interface QueryPlayBackListTaskCallback {

    void queryHasNoData();

    void queryOnlyHasLocalFile();

    void queryOnlyLocalNoData();

    void queryLocalException();

    void querySuccessFromCloud(List<CloudPartInfoFileEx> cloudPartInfoFileExs, int queryMLocalStatus, List<CloudPartInfoFile> cloudPartInfoFile);

    void querySuccessFromDevice(List<CloudPartInfoFileEx> cloudPartInfoFileExs, int position, List<CloudPartInfoFile> cloudPartInfoFile);

    void queryLocalNoData();

    void queryException();

    void queryTaskOver(int type, int queryMode, int queryErrorCode, String detail);

}
