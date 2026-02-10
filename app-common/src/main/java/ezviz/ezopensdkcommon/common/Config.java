package ezviz.ezopensdkcommon.common;

public class Config {

    // 日志开关
    public static boolean LOGGING = true;

    // 设备热点前缀，萤石是EZVIZ，定制设备根据能力集获取，方法为EZBusinessTool.getWiFiConfigPrefix(mEZProbeDeviceInfo.getEZProbeDeviceInfo().getDeviceHotSpot())
    public static String DeviceHotspotPrefix = "EZVIZ";
}
