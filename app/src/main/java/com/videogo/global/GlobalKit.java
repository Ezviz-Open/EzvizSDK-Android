package com.videogo.global;

import com.videogo.openapi.EZConstants;

/**
 * Copyright (C) 2024 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2024/7/9 19:09
 */
public class GlobalKit {

    private static GlobalKit globalKit;

    public static boolean isJCTest = true;

    // 预览取流token
    public String previewStreamToken = "";
    // 对讲取流token
    public String talkStreamToken = "";
    // 回放取流token
    public String playbackStreamToken = "";

    // 视频质量
    public EZConstants.EZVideoQuality videoQuality = EZConstants.EZVideoQuality.VIDEO_PRIORITY_NONE;

    public boolean enableDirectInner = true;// 是否允许内网直连取流
    public boolean enableP2P = false;// 是否允许P2P取流
    public boolean enableVTDU = true;// 是否允许流媒体取流

    public static GlobalKit getInstance() {
        if (globalKit == null) {
            globalKit = new GlobalKit();
        }
        return globalKit;
    }
}
