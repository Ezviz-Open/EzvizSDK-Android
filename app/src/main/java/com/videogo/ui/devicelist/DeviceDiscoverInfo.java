/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright null
 * 
 * @FileName DeviceDiscoverInfo.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2015-5-13
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.devicelist;

import android.os.Parcel;
import android.os.Parcelable;

import com.videogo.openapi.bean.EZProbeDeviceInfo;


public class DeviceDiscoverInfo implements Parcelable {
    public String deviceName;
    public String deviceSerial;
    public boolean isWifiConnected = false;
    public boolean isPlatConnected = false;
    public String localIP;
    public int localPort = 8000;
    public EZProbeDeviceInfo mEZProbeDeviceInfo;
    public boolean isAdded = false;
    
    public DeviceDiscoverInfo() {
    }

;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeString(this.deviceSerial);
        dest.writeByte(isWifiConnected ? (byte) 1 : (byte) 0);
        dest.writeByte(isPlatConnected ? (byte) 1 : (byte) 0);
        dest.writeString(this.localIP);
        dest.writeInt(this.localPort);
        dest.writeParcelable(this.mEZProbeDeviceInfo, 0);
        dest.writeByte(isAdded ? (byte) 1 : (byte) 0);
    }

    protected DeviceDiscoverInfo(Parcel in) {
        this.deviceName = in.readString();
        this.deviceSerial = in.readString();
        this.isWifiConnected = in.readByte() != 0;
        this.isPlatConnected = in.readByte() != 0;
        this.localIP = in.readString();
        this.localPort = in.readInt();
        this.mEZProbeDeviceInfo = in.readParcelable(EZProbeDeviceInfo.class.getClassLoader());
        this.isAdded = in.readByte() != 0;
    }

    public static final Parcelable.Creator<DeviceDiscoverInfo> CREATOR = new Parcelable.Creator<DeviceDiscoverInfo>() {
        public DeviceDiscoverInfo createFromParcel(Parcel source) {
            return new DeviceDiscoverInfo(source);
        }

        public DeviceDiscoverInfo[] newArray(int size) {
            return new DeviceDiscoverInfo[size];
        }
    };
}
