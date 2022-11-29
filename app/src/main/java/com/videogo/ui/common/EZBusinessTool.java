package com.videogo.ui.common;

/**
 * Copyright (C) 2022 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2022/9/29 4:49 下午
 */
public class EZBusinessTool {

    /**
     * 根据type获取取流方式名
     *
     * @param streamType
     */
    public static String getStreamType(int streamType) {
        String strStreamType;
        switch (streamType) {
            /*
              取流方式切换到私有流媒体转发模式
             */
            case 0:
                strStreamType = "private_stream";
                break;
            /*
              取流方式切换到P2P模式
             */
            case 1:
                strStreamType = "p2p";
                break;
            /*
              取流方式切换到内网直连模式
             */
            case 2:
                strStreamType = "direct_inner";
                break;
            /*
              取流方式切换到外网直连模式
             */
            case 3:
                strStreamType = "direct_outer";
                break;
            /*
              取流方式切换到云存储回放
             */
            case 4:
                strStreamType = "cloud_playback";
                break;
            /*
              取流方式切换到云存储留言
             */
            case 5:
                strStreamType = "cloud_leave_msg";
                break;
            /*
              取流方式切换到反向直连模式
             */
            case 6:
                strStreamType = "direct_reverse";
                break;
            /*
              取流方式切换到HCNETSDK
             */
            case 7:
                strStreamType = "hcnetsdk";
                break;
            default:
                strStreamType = "unknown(" + streamType + ")";
                break;
        }
        return strStreamType;
    }
}
