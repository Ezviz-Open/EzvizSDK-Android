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

    void queryHasNoData(int type);

    void querySuccessFromCloud(List<CloudPartInfoFileEx> cloudPartInfoFileExs, List<CloudPartInfoFile> cloudPartInfoFile);

    void querySuccessFromSDKCloud(List<CloudPartInfoFileEx> cloudPartInfoFileExs, List<CloudPartInfoFile> cloudPartInfoFile);

    void querySuccessFromDevice(List<CloudPartInfoFileEx> cloudPartInfoFileExs, int position, List<CloudPartInfoFile> cloudPartInfoFile);

}
