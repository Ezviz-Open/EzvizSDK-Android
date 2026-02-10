package com.videogo.ui.common;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;

import com.videogo.openapi.EZConstants;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.EZDeviceDetailPublicInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.EZSubDeviceInfo;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.Utils;

import java.util.Date;

import ezviz.ezopensdk.R;

/**
 * Copyright (C) 2022 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2022/9/29 4:49 下午
 */
public class EZBusinessTool {

    /**
     * 根据videoLevel获取设备视频质量名，0-流畅，1-均衡，2-高清，3-超清，4-极清，5-3K，6-4K
     *
     * @return
     */
    public static String getDeviceQualityTitle(ContextThemeWrapper context, int videoLevel) {
        Resources res = context.getResources();
        String qualityTitle;
        switch (videoLevel) {
            case 0:// 流畅
                qualityTitle = res.getString(R.string.quality_flunet);
                break;
            case 1:// 均衡
                qualityTitle = res.getString(R.string.quality_balanced);
                break;
            case 2:// 高清
                qualityTitle = res.getString(R.string.quality_hd);
                break;
            case 3:// 超清
                qualityTitle = res.getString(R.string.quality_super_hd);
                break;
            case 4:// 极清
                qualityTitle = res.getString(R.string.quality_extreme_hd);
                break;
            case 5:// 3K
                qualityTitle = res.getString(R.string.quality_3K);
                break;
            case 6:// 4K
                qualityTitle = res.getString(R.string.quality_4K);
                break;
            default:
                qualityTitle = "unknown";
                break;
        }

        return qualityTitle;
    }

    /**
     * 获取比当前清晰度高一级的清晰度
     * @param currentQulityMode 当前清晰度
     * @param deviceDetailPublicInfo 设备详情
     * @return
     */
    public static EZConstants.EZVideoLevel getHigherVideoLevel(EZConstants.EZVideoLevel currentQulityMode, EZDeviceDetailPublicInfo deviceDetailPublicInfo) {
        if (deviceDetailPublicInfo != null && deviceDetailPublicInfo.videoQualityInfos != null) {
            int targetVideoLevel = 0;
            int dif = 100;
            for (int i = 0; i < deviceDetailPublicInfo.videoQualityInfos.size(); i++) {
                int videoLevel = deviceDetailPublicInfo.videoQualityInfos.get(i).getVideoLevel();
                // 遍历找出比当前videoLevel大 且 最接近 的清晰度值
                if (videoLevel > currentQulityMode.getVideoLevel() && videoLevel - currentQulityMode.getVideoLevel() < dif) {
                    targetVideoLevel = videoLevel;
                    dif = videoLevel - currentQulityMode.getVideoLevel();
                }
            }
            if (targetVideoLevel > 0) {
                EZCameraInfo cameraInfo = new EZCameraInfo();
                cameraInfo.setVideoLevel(targetVideoLevel);
                return cameraInfo.getVideoLevel();
            }
        }

        return currentQulityMode;
    }

    /**
     * 获取比当前清晰度低一级的清晰度
     * @param currentQulityMode 当前清晰度
     * @param deviceDetailPublicInfo 设备详情
     * @return
     */
    public static EZConstants.EZVideoLevel getLowerVideoLevel(EZConstants.EZVideoLevel currentQulityMode, EZDeviceDetailPublicInfo deviceDetailPublicInfo) {
        if (deviceDetailPublicInfo != null && deviceDetailPublicInfo.videoQualityInfos != null) {
            int targetVideoLevel = 0;
            int dif = 100;
            for (int i = 0; i < deviceDetailPublicInfo.videoQualityInfos.size(); i++) {
                int videoLevel = deviceDetailPublicInfo.videoQualityInfos.get(i).getVideoLevel();
                // 遍历找出比当前videoLevel大 且 最接近 的清晰度值
                if (videoLevel < currentQulityMode.getVideoLevel() && currentQulityMode.getVideoLevel() - videoLevel < dif) {
                    targetVideoLevel = videoLevel;
                    dif = currentQulityMode.getVideoLevel() - videoLevel;
                }
            }
            if (targetVideoLevel > 0) {
                EZCameraInfo cameraInfo = new EZCameraInfo();
                cameraInfo.setVideoLevel(targetVideoLevel);
                return cameraInfo.getVideoLevel();
            }
        }

        return currentQulityMode;
    }

    /**
     * 获取变声类型
     *
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
     * 根据type获取视频编码格式
     *
     * @param videoEncodeType
     */
    public static String getVideoEncodeType(int videoEncodeType) {
        String strVideoEncodeType;
        switch (videoEncodeType) {
            case 0:
                strVideoEncodeType = "无编码";
                break;
            case 1:
                strVideoEncodeType = "HIK264";
                break;
            case 2:
                strVideoEncodeType = "MPEG2";
                break;
            case 3:
                strVideoEncodeType = "MPEG4";
                break;
            case 4:
                strVideoEncodeType = "MJPEG";
                break;
            case 5:
                strVideoEncodeType = "H265";
                break;
            case 6:
                strVideoEncodeType = "SVAC";
                break;
            case 256:
                strVideoEncodeType = "H264";
                break;
            default:
                strVideoEncodeType = "unknown(" + videoEncodeType + ")";
                break;
        }
        return strVideoEncodeType;
    }

    /**
     * 显示解码方式，软解或硬解
     */
    public static String getDecodeType(int type) {
        String decodeTypeName;
        if (type == 1) {
            decodeTypeName = "hard";
        } else {
            decodeTypeName = "soft";
        }
        return decodeTypeName;
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
        dst.setCloudType(src.getCloudType());
        dst.setiStorageVersion(src.getiStorageVersion());
        dst.setFileSize(src.getFileSize());
        dst.setSpaceId(src.getSpaceId());
    }

    public static void convertCloudPartInfoFile2EZDeviceRecordFile(EZDeviceRecordFile dst, CloudPartInfoFile src) {
        dst.setStartTime(Utils.convert14Calender(src.getStartTime()));
        dst.setStopTime(Utils.convert14Calender(src.getEndTime()));
    }

    public static Date getMinDate() {
        return DateTimeUtil.parseYMDStringToDate("2020-01-01");
    }

    /**
     * 是否支持对讲
     */
    public static EZConstants.EZTalkbackCapability isSupportTalk(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        // 网关下子设备的能力集挂载在自己的对象上
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportTalk();
        }
        return deviceInfo.isSupportTalk();
    }

    /**
     * 是否支持云台
     */
    public static boolean isSupportPTZ(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportPTZ();
        }
        return deviceInfo.isSupportPTZ();
    }

    /**
     * 是否支持内网直连时倍数设置
     */
    public static boolean isSupportDirectInnerRelaySpeed(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportDirectInnerRelaySpeed();
        }
        return deviceInfo.isSupportDirectInnerRelaySpeed();
    }

    /**
     * 是否支持回放倍率设置
     */
    public static boolean isSupportPlaybackRate(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportPlaybackRate();
        }
        return deviceInfo.isSupportPlaybackRate();
    }

    /**
     * 是否支持SD卡录像封面
     */
    public static boolean isSupportSdCover(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportSdCover();
        }
        return deviceInfo.isSupportSdCover();
    }

    /**
     * 是否支持SD卡录像下载
     */
    public static boolean isSupportSDRecordDownload(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportSDRecordDownload();
        }
        return deviceInfo.isSupportSDRecordDownload();
    }

    /**
     * 是否支持缩放
     */
    public static boolean isSupportZoom(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportZoom();
        }
        return deviceInfo.isSupportZoom();
    }

    /**
     * 是否支持双目
     */
    public static boolean isSupportMultiChannel(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportMultiChannel();
        }
        return deviceInfo.isSupportMultiChannel();
    }

    /**
     * 是否支持自动清晰度
     */
    public static boolean isSupportDeviceAutoVideolevel(EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        if (cameraInfo instanceof EZSubDeviceInfo) {
            EZSubDeviceInfo subDeviceInfo = (EZSubDeviceInfo) cameraInfo;
            return subDeviceInfo.isSupportDeviceAutoVideolevel();
        }
        return deviceInfo.isSupportDeviceAutoVideolevel();
    }

    /**
     * 获取设备热点前缀
     * @param deviceHotSpot 0-EZVIZ，1-SoftAP，2-CAMGO
     */
    public static String getWiFiConfigPrefix(int deviceHotSpot) {
        String WiFiConfigPrefix = "EZVIZ";
        switch (deviceHotSpot) {
            case 1:
                WiFiConfigPrefix = "SoftAP";
                break;
            case 2:
                WiFiConfigPrefix = "CAMGO";
                break;
            default:
                break;
        }
        return WiFiConfigPrefix;
    }

}
