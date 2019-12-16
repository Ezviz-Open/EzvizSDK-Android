package com.videogo.ui.devicelist;


import ezviz.ezopensdkcommon.R;

public enum DeviceCategory {

    IP_CAMERA(R.string.ip_camera), DIGITAL_VIDEO_RECORDER(R.string.digital_video_recorder), VIDEO_BOX(
            R.string.video_box), ALARM_BOX(R.string.alarm_box), NETWORK_VIDEO_RECORDER(R.string.network_video_recorder), ROUTER(
            R.string.router),
    DOORLOCK(R.string.doorlock),PLUG(R.string.plug),KEYBOARD(R.string.keyboard);

    private int textResId;

    private DeviceCategory(int textResId) {
        this.textResId = textResId;
    }

    public int getTextResId() {
        return textResId;
    }
}