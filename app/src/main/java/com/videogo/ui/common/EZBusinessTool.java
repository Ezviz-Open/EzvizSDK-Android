package com.videogo.ui.common;

import android.text.TextUtils;

import com.videogo.openapi.EZConstants;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.EZSubDeviceInfo;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Copyright (C) 2022 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2022/9/29 4:49 下午
 */
public class EZBusinessTool {

    /**
     * 获取变声类型
     * @param tag
     * @return
     */
    public static EZConstants.EZVoiceChangeType getVoiceChangeType(String tag) {
        switch (Integer.parseInt(tag)) {
            case -7:
                return EZConstants.EZVoiceChangeType.EZ_VOICE_CHANGE_TYPE_MAN;
            case 7:
                return EZConstants.EZVoiceChangeType.EZ_VOICE_CHANGE_TYPE_CLOWN;
            default:
                return EZConstants.EZVoiceChangeType.EZ_VOICE_CHANGE_TYPE_NORMAL;
        }
    }

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

    /**
     * 根据设备型号判断是否是HUB设备
     */
    public static boolean isHubDevice(String deviceType) {
        if (TextUtils.isEmpty(deviceType)) {
            return false;
        }
        switch (deviceType) {
            case "CASTT":
            case "CAS_HUB_NEW":
                return true;
            default:
                return deviceType.startsWith("CAS_WG_TEST");
        }
    }

    public static void convertCloudPartInfoFile2EZCloudRecordFile(EZCloudRecordFile dst, CloudPartInfoFile src) {
        dst.setCoverPic(src.getPicUrl());
        dst.setDownloadPath(src.getDownloadPath());
        dst.setFileId(src.getFileId());
        dst.setEncryption(src.getKeyCheckSum());
        dst.setStartTime(Utils.convert14Calender(src.getStartTime()));
        dst.setStopTime(Utils.convert14Calender(src.getEndTime()));
        dst.setDeviceSerial(src.getDeviceSerial());
        dst.setCameraNo(src.getCameraNo());
        dst.setVideoType(src.getVideoType());
        dst.setiStorageVersion(src.getiStorageVersion());
        dst.setFileSize(src.getFileSize());
        dst.setSpaceId(src.getSpaceId());
    }

    public static void convertCloudPartInfoFile2EZDeviceRecordFile(EZDeviceRecordFile dst, CloudPartInfoFile src) {
        dst.setStartTime(Utils.convert14Calender(src.getStartTime()));
        dst.setStopTime(Utils.convert14Calender(src.getEndTime()));
    }

    public static Date getMinDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse("2012-01-01");
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否支持对讲
     */
    public static EZConstants.EZTalkbackCapability isSupportTalk(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        // 网关下子设备的能力集挂载在自己的对象上
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo)cameraInfo;
            return subDeviceInfo.isSupportTalk();
        }
        return deviceInfo.isSupportTalk();
    }

    /**
     * 是否支持云台
     */
    public static boolean isSupportPTZ(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo)cameraInfo;
            return subDeviceInfo.isSupportPTZ();
        }
        return deviceInfo.isSupportPTZ();
    }

    /**
     * 是否支持内网直连时倍数设置
     */
    public static boolean isSupportDirectInnerRelaySpeed(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo)cameraInfo;
            return subDeviceInfo.isSupportDirectInnerRelaySpeed();
        }
        return deviceInfo.isSupportDirectInnerRelaySpeed();
    }

    /**
     * 是否支持回放倍率设置
     */
    public static boolean isSupportPlaybackRate(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo)cameraInfo;
            return subDeviceInfo.isSupportPlaybackRate();
        }
        return deviceInfo.isSupportPlaybackRate();
    }

    /**
     * 是否支持SD卡录像封面
     */
    public static boolean isSupportSdCover(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo)cameraInfo;
            return subDeviceInfo.isSupportSdCover();
        }
        return deviceInfo.isSupportSdCover();
    }

    /**
     * 是否支持SD卡录像下载
     */
    public static boolean isSupportSDRecordDownload(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo)cameraInfo;
            return subDeviceInfo.isSupportSDRecordDownload();
        }
        return deviceInfo.isSupportSDRecordDownload();
    }

    /**
     * 是否支持缩放
     */
    public static boolean isSupportZoom(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo)cameraInfo;
            return subDeviceInfo.isSupportZoom();
        }
        return deviceInfo.isSupportZoom();
    }

}
