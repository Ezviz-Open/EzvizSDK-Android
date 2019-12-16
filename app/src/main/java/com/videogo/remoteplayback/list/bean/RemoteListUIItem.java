/**
 * @ProjectName: null
 * @Copyright: null
 * @address: https://www.ys7.com
 * @date: 2014-5-30 下午3:55:04
 * @Description: null
 */
package com.videogo.remoteplayback.list.bean;

import android.text.TextUtils;

public class RemoteListUIItem implements Comparable<RemoteListUIItem> {
    // 排序时间
    private long sortTime;

    // 开始时间
    private long beginTime;

    // 用于UI显示的开始时间
    private String uiBegin;

    // 结束时间
    private long stopTime;

    // 用于UI显示的录像长度
    private String uiDuration;

    // 图片 url
    private String picUrl;
    // 加密两次MD5值
    private String keyChecksum;
    // 设备序列号
    private String deviceSerial;

    // 是否云存储
    private boolean isCloud;

    // UI显示类型
    private int uiType;

    // 是否为报警录像
    private boolean isAlarm;

    // 云存储类型
    private int cloudType;

    public int getCloudType() {
        return cloudType;
    }

    public void setCloudType(int cloudType) {
        this.cloudType = cloudType;
    }

    public long getSortTime() {
        return sortTime;
    }

    public void setSortTime(long sortTime) {
        this.sortTime = sortTime;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public boolean isCloud() {
        return isCloud;
    }

    public void setCloud(boolean isCloud) {
        this.isCloud = isCloud;
    }

    public String getUiDuration() {
        return uiDuration;
    }

    public void setUiDuration(String uiDuration) {
        this.uiDuration = uiDuration;
    }

    public int getUiType() {
        return uiType;
    }

    public void setUiType(int uiType) {
        this.uiType = uiType;
    }

    public String getUiBegin() {
        return uiBegin;
    }

    public void setUiBegin(String uiBegin) {
        this.uiBegin = uiBegin;
    }

    @Override
    public int compareTo(RemoteListUIItem another) {
        if (this == another) {
            return 0;
        } else if (another != null) {
            if (this.sortTime <= another.getSortTime()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (sortTime ^ (sortTime >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RemoteListUIItem other = (RemoteListUIItem) obj;
        if (sortTime != other.sortTime)
            return false;
        return true;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public boolean isAlarm() {
        return isAlarm;
    }

    public void setAlarm(boolean isAlarm) {
        this.isAlarm = isAlarm;
    }

    public String getKeyChecksum() {
        return keyChecksum;
    }

    public void setKeyChecksum(String keyChecksum) {
        this.keyChecksum = keyChecksum;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public boolean needDecrypt() {
        if (!TextUtils.isEmpty(keyChecksum)) {
            return true;
        } else {
            return false;
        }
    }

}
