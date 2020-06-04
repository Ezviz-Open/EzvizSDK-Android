package com.videogo.ui.message;


import ezviz.ezopensdk.R;

public enum AlarmType {
    /*
           报警类型：
    -1:全部
    10000:人体感应事件
    10001:紧急遥控按钮事件
    10002:移动侦测报警
    10003:婴儿啼哭报警
    10004:门磁报警
    10005:烟感报警
    10006:可燃气体报警
    10008:水浸报警
    10009:紧急按钮报警
    10010:人体感应报警
    10100:IO报警
    10101:IO-1报警
    10102:IO-2报警
    10103:IO-3报警
    10104:IO-4报警
    10105:IO-5报警
    10106:IO-6报警
    10107:IO-7报警
    10108:IO-8报警
    10109:IO-9报警
    10110:IO-10报警
    10111:IO-11报警
    10112:IO-12报警
    10113:IO-13报警
    10114:IO-14报警
    10115:IO-15报警
    10116:IO-16报警

        /*
           Alarm type：
    -1:全部
    10000:Human induction events
    10001:Emergency remote control button event
    10002:Motion detection alarm
    10003:Baby crying alarm
    10004:Menci alarm
    10005:Smoke alarm
    10006:Combustible gas alarm
    10008:Flooding alarm
    10009:Emergency button alarm
    10010:Human induction alarm
    10100:IO alarm
    10101:IO-1 alarm
    10102:IO-2 alarm
    10103:IO-3 alarm
    10104:IO-4 alarm
    10105:IO-5 alarm
    10106:IO-6 alarm
    10107:IO-7 alarm
    10108:IO-8 alarm
    10109:IO-9 alarm
    10110:IO-10 alarm
    10111:IO-11 alarm
    10112:IO-12 alarm
    10113:IO-13 alarm
    10114:IO-14 alarm
    10115:IO-15 alarm
    10116:IO-16 alarm
    */
    UNKNOWN(0, R.string.alarm_type_unknown),
    BODY_FEEL(10000, true, R.drawable.message_infrared, R.string.alarm_type_infrared),
    REMOTE_CONTROL(10001, true, R.string.alarm_type_remotecontrol),
    MOTION_DETECTION_ALARM(10002, true, R.string.alarm_type_motion_detection),
    BABY_CRY_ALARM(10003, true, R.drawable.message_infrared, R.string.alarm_type_baby_cry),
    DOOR_ALARM(10004, R.drawable.message_door, R.string.alarm_type_door),
    SMOKE_ALARM(10005, R.drawable.message_smoke, R.string.alarm_type_smoke),
    GAS_ALARM(10006, R.drawable.message_smoke, R.string.alarm_type_gas),
    WATER_ALARM(10008, R.drawable.water_alarm, R.string.alarm_type_water),
    URGENT_BUTTON_ALARM(10009, R.drawable.message_callhelp, R.string.alarm_type_urgent_button),
    BODY_ALARM(10010, R.drawable.message_infrared, R.string.ez_alarm_type_person_alarm);

    private int id;
    private boolean hasCamera;
    private int drawableResId;
    private int textResId;

    private AlarmType(int id, boolean hasCamera, int drawableResId, int textResId) {
        this.id = id;
        this.hasCamera = hasCamera;
        this.drawableResId = drawableResId;
        this.textResId = textResId;
    }

    private AlarmType(int id, boolean hasCamera, int textResId) {
        this.id = id;
        this.hasCamera = hasCamera;
        this.drawableResId = R.drawable.defalut_alarm;
        this.textResId = textResId;
    }

    private AlarmType(int id, int drawableResId, int textResId) {
        this.id = id;
        this.drawableResId = drawableResId;
        this.textResId = textResId;
    }

    private AlarmType(int id, int textResId) {
        this.id = id;
        this.drawableResId = R.drawable.defalut_alarm;
        this.textResId = textResId;
    }

    public int getId() {
        return id;
    }

    public boolean hasCamera() {
        return hasCamera;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public int getTextResId() {
        return textResId;
    }

    public static AlarmType getAlarmTypeById(int id) {
        for (AlarmType e : AlarmType.values()) {
            if (id == e.id)
                return e;
        }
        return UNKNOWN;
    }
}