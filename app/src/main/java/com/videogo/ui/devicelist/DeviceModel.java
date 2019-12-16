package com.videogo.ui.devicelist;

import android.text.TextUtils;

import ezviz.ezopensdkcommon.R;

public enum DeviceModel {

    // C1
    C1("C1", R.drawable.device_c1, R.drawable.results_pic_c1, 0, true, DeviceCategory.IP_CAMERA, "CS-C1-", "DS-2CD8464"),
    // C2
    C2("C2", R.drawable.device_c2, R.drawable.results_pic_c2, 0, true, DeviceCategory.IP_CAMERA, "CS-C2-1",
            "DS-2CD8133"),
    //C2_USA
    C2_USA("C2", R.drawable.device_c2_usa, R.drawable.results_pic_c2_usa, 0, true, DeviceCategory.IP_CAMERA, "CS-CV100-B0-"),
    //C2 Cube
    C2_CUBE("C2 Cube", R.drawable.device_c2_usa, R.drawable.results_pic_c2_usa, 0, true, DeviceCategory.IP_CAMERA, "CS-CV100-B1-"),
    // C2S
    C2S("C2S", R.drawable.device_c2s, R.drawable.results_pic_c2s, 0, true, DeviceCategory.IP_CAMERA, "CS-C2S"),
    // C2_2
    C2_2("C2", R.drawable.device_c2_2, R.drawable.results_pic_c2_2, 0, true, DeviceCategory.IP_CAMERA, "CS-C2-2"),
    // C2S_BD
    C2S_BD("C2S", R.drawable.device_baidu_c2s, R.drawable.results_pic_baidu_c2s, 0, true, DeviceCategory.IP_CAMERA,
            "CS-C2S"),
    // C2W
    C2W("C2W", R.drawable.device_c2w, R.drawable.results_pic_c2w, 0, true, DeviceCategory.IP_CAMERA, "CS-C2W"),
    // C2_ALL
    C2_ALL("C2/C2S/C2W", R.drawable.device_c2_2, R.drawable.results_pic_c2_2, 0, true, DeviceCategory.IP_CAMERA,
            "C2_ALL"),
    // C2_MINI
    C2_MINI("C2mini", R.drawable.device_c2mini2, R.drawable.results_pic_c2mini2, 0, true, DeviceCategory.IP_CAMERA,
            "CS-C2mini-"),
    // C1S原C2plus
    C1S("C1s", R.drawable.device_c2plus, R.drawable.results_pic_c2plus, R.drawable.device_c2plus_bg, true,
            DeviceCategory.IP_CAMERA, "CS-C1s-", "CS-C2plus-"),
    //海外Mini IQ
    Mini_IQ("Mini IQ", R.drawable.device_c2plus, R.drawable.results_pic_c2plus, R.drawable.device_c2plus_bg, true,
            DeviceCategory.IP_CAMERA, "CS-CV208-A0-"),
    // CO2
    CO2("CO2", R.drawable.device_co2, R.drawable.results_pic_co2, 0, true, DeviceCategory.IP_CAMERA, "CS-CO2-"),
    // H2S
    H2S("H2S", R.drawable.device_h2s, R.drawable.results_pic_h2s, 0, true, DeviceCategory.IP_CAMERA, "CS-H2S-"),
    // C3
    C3("C3", R.drawable.device_c3, R.drawable.results_pic_c3, 0, true, DeviceCategory.IP_CAMERA, "CS-C3-"),
    // C3S
    C3S("C3S", R.drawable.device_c3s, R.drawable.results_pic_c3s, 0, true, DeviceCategory.IP_CAMERA, "CS-C3S-","CS-CV210-A0-"),
    C3S_USA("Hawk Bullet", R.drawable.device_c3s, R.drawable.results_pic_c3s, 0, true, DeviceCategory.IP_CAMERA, "CS-CV210-A1-","CS-CV210"),
    // C3_ALL
    C3_ALL("C3/C3S", R.drawable.device_c3, R.drawable.results_pic_c3, 0, true, DeviceCategory.IP_CAMERA, "C3_ALL"),
    // C3S
    C3S_WIFI("C3S", R.drawable.device_c3s, R.drawable.results_pic_c3s, 0, true, DeviceCategory.IP_CAMERA, "CS-C3S-"),
    // C3S
    C3C("C3C", R.drawable.device_c3c, R.drawable.results_pic_c3c, 0, true, DeviceCategory.IP_CAMERA, "CS-C3C-","CS-CV216-A0-","CS-CV216-A1","CS-CV216"),
    // C3_WIFI
    C3_WIFI("C3S-WIFI/C3C", R.drawable.device_c3c, R.drawable.results_pic_c3c, 0, true, DeviceCategory.IP_CAMERA,
            "C3_WIFI"),
    // C3E
    C3E("C3E", R.drawable.device_c3e, R.drawable.results_pic_c3e, R.drawable.device_c3e_bg, true, DeviceCategory.IP_CAMERA, "CS-C3E-"),
    // C4
    C4("C4", R.drawable.device_c4, R.drawable.results_pic_c4, 0, true, DeviceCategory.IP_CAMERA, "CS-C4-"),
    // C4S
    C4S("C4S", R.drawable.device_c4s, R.drawable.results_pic_c4s, 0, true, DeviceCategory.IP_CAMERA, "CS-C4S-","CS-CV220-A0-"),
    C4S_USA("Hawk Dome", R.drawable.device_c4s, R.drawable.results_pic_c4s, 0, true, DeviceCategory.IP_CAMERA, "CS-CV220-A1-","CS-CV220"),
    // C3S
    C4_ALL("C4/C4S", R.drawable.device_c4, R.drawable.results_pic_c4, 0, true, DeviceCategory.IP_CAMERA, "C4ALL"),
    // C3S
    C4S_WIFI("C4S", R.drawable.device_c4s, R.drawable.results_pic_c4s, 0, true, DeviceCategory.IP_CAMERA, "CS-C4S-"),
    // C4_WIFI
    C4_WIFI("C4S-WIFI", R.drawable.device_c4s, R.drawable.results_pic_c4s, 0, true, DeviceCategory.IP_CAMERA, "C4_WIFI"),
    // C4C
    C4C("C4C", R.drawable.device_c4c, R.drawable.results_pic_c4c, R.drawable.device_c4c_bg, true, DeviceCategory.IP_CAMERA, "CS-C4C-"),
    // C4E
    C4E("C4E", R.drawable.device_c4e, R.drawable.results_pic_c4e, R.drawable.device_c4e_bg, true, DeviceCategory.IP_CAMERA, "CS-C4E-"),
    // C6
    C6("C6", R.drawable.device_c6, R.drawable.results_pic_c6, 0, true, DeviceCategory.IP_CAMERA, "CS-C6-", "CS-C2PT"),
    // D1
    D1("D1", R.drawable.device_d1, R.drawable.results_pic_d1, R.drawable.device_d1_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-D1"),
    // X1
    X1("X1", R.drawable.device_x1, R.drawable.results_pic_x1, R.drawable.device_x1_bg, R.drawable.my_x1, DeviceCategory.ROUTER, "CS-X1-"),
    // X2
    X2("X2", R.drawable.device_x2, R.drawable.results_pic_x2, R.drawable.device_x2_bg, R.drawable.my_x2, DeviceCategory.ROUTER, "CS-X2-"),
    // X3
    X3("X3", R.drawable.device_x3, R.drawable.results_pic_x3, R.drawable.device_x3_bg, R.drawable.my_x3,
            DeviceCategory.NETWORK_VIDEO_RECORDER, "CS-X3-", "CS-N1C-"),
    // X3C
    X3C("X3C", R.drawable.device_x3c, R.drawable.results_pic_x3c, R.drawable.device_x3c_bg, R.drawable.my_x3c,
            DeviceCategory.NETWORK_VIDEO_RECORDER, "CS-X3C-"),
    // VR104N X4
    VR104N("VR104N", R.drawable.device_x4, R.drawable.results_pic_x4, R.drawable.device_x4_bg, R.drawable.my_x4,
            DeviceCategory.NETWORK_VIDEO_RECORDER, "CS-VR104N-A1-"),
    //VR108N X4
    VR108N("VR108N", R.drawable.device_x4, R.drawable.results_pic_x4, R.drawable.device_x4_bg, R.drawable.my_x4,
            DeviceCategory.NETWORK_VIDEO_RECORDER, "CS-VR108N-A1-"),
    //VR116N X4
    VR116N("VR116N", R.drawable.device_x4, R.drawable.results_pic_x4, R.drawable.device_x4_bg, R.drawable.my_x4,
            DeviceCategory.NETWORK_VIDEO_RECORDER, "CS-VR116N-A1-"),
    // Vault
    VAULT("Vault", R.drawable.device_x3, R.drawable.results_pic_x3, R.drawable.device_x3_bg, R.drawable.my_x3,
            DeviceCategory.NETWORK_VIDEO_RECORDER, "CS-Vault-"),
    // N1
    N1("N1", R.drawable.device_n1, R.drawable.results_pic_n1, R.drawable.device_d1_bg, R.drawable.my_n1,
            DeviceCategory.NETWORK_VIDEO_RECORDER, "CS-N1-"),
    // N1W
    N1W("N1W", R.drawable.device_n1w, R.drawable.results_pic_n1w, R.drawable.device_n1w_bg,
            DeviceCategory.ROUTER, "CS-N1W-"),
    // R1
    R1("R1", R.drawable.device_r1, R.drawable.results_pic_r1, R.drawable.device_r1_bg, R.drawable.my_r1, DeviceCategory.VIDEO_BOX,
            "CS-R1"),
    // R2
    R2("R2", R.drawable.device_r2, R.drawable.results_pic_r2, R.drawable.device_r2_bg, R.drawable.my_r2, DeviceCategory.VIDEO_BOX,
            "CS-R2"),
    // A1
    A1("A1", R.drawable.device_a1, R.drawable.results_pic_a1, R.drawable.device_a1_bg, R.drawable.my_a1, DeviceCategory.ALARM_BOX,
            "CS-A1-"),
    // A1C
    A1C("A1C", R.drawable.device_a1c, R.drawable.results_pic_a1c, R.drawable.device_a1c_bg, R.drawable.my_a1c, DeviceCategory.ALARM_BOX,
            "CS-A1C"),

    // A1S
    A1S("A1S", R.drawable.device_a1s, R.drawable.results_pic_a1s, R.drawable.device_a1s_bg, R.drawable.my_a1s, DeviceCategory.ALARM_BOX,
            "CS-A1S"),

    // F1
    F1("F1", R.drawable.device_f1, R.drawable.results_pic_f1, 0, true, DeviceCategory.IP_CAMERA, "CS-F1"),
    // C2C
    C2C("C2C", R.drawable.device_c2c, R.drawable.results_pic_c2c, 0, true, DeviceCategory.IP_CAMERA, "CS-C2C-"),
    //    CS-CV206-C0-1A1WFR
    C2C_USA("C2C", R.drawable.device_c2c, R.drawable.results_pic_c2c, 0, true, DeviceCategory.IP_CAMERA, "CS-CV206-B1-","CS-CV206"),
    // H2C
    H2C("H2C", R.drawable.device_h2c, R.drawable.results_pic_h2c, 0, true, DeviceCategory.IP_CAMERA, "CS-H2C-"),
    // C2C_ALL
    C2C_ALL("C2C/H2C", R.drawable.device_c2c, R.drawable.results_pic_c2c, 0, true, DeviceCategory.IP_CAMERA, "C2C_ALL"),
    // W1
    W1("W1", R.drawable.device_w1, R.drawable.results_pic_w1, R.drawable.device_default_details, R.drawable.my_w1, DeviceCategory.ROUTER,
            "CS-W1-"),
    // W3
    W3("W3", R.drawable.device_w3, R.drawable.results_pic_w3, R.drawable.device_w3_details, R.drawable.my_w3,
            DeviceCategory.ROUTER, "CS-W3-"),
    // CS-X4-104P
    X4_104P("X4", R.drawable.device_pic_4_nvr, R.drawable.results_8_nvr, R.drawable.device_4_nvr_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-X4-104P"),
    // CS-X4_108P
    X4_108P("X4", R.drawable.device_pic_8_16_nvr, R.drawable.results_8_16_nvr, R.drawable.device_16_nvr_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-X4-108P"),
    // CS-X4_116P
    X4_116P("X4", R.drawable.device_pic_8_16_nvr, R.drawable.results_8_16_nvr, R.drawable.device_16_nvr_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-X4-116P"),
    // CS-X5_104T
    X5_104T("X5", R.drawable.device_pic_4_dvr, R.drawable.results_4_dvr, R.drawable.device_4_dvr_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-X5-104T"),

    //CS-X5-108TP
    X5_108TP("X5", R.drawable.device_x5_108tp, R.drawable.results_x5_108tp, R.drawable.device_x5_108tp_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-X5-108TP"),
    // CS-X5_108T
    X5_108T("X5", R.drawable.device_pic_8_16_dvr, R.drawable.result_8_16dvr, R.drawable.device_8_16dvr_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-X5-108T"),
    X5_116T("X5", R.drawable.device_pic_8_16_dvr, R.drawable.result_8_16dvr, R.drawable.device_8_16dvr_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-X5-116T"),
    // CS-X5C-4EU,CS-X5C-8EU
    X5C("Vault Live", R.drawable.device_x5c, R.drawable.result_x5c, R.drawable.device_x5c_bg,R.drawable.my_x5c,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-X5C-"),
    // MINI
    MINI("Mini", R.drawable.device_c2mini2, R.drawable.results_pic_c2mini2, 0, true, DeviceCategory.IP_CAMERA,
            "CS-Mini-"),
    // MINI
    MINI_PLUS("Mini+", R.drawable.device_c2mini2, R.drawable.results_pic_c2mini2, 0, true, DeviceCategory.IP_CAMERA,
            "CS-CV200-A0-"),
    MINI_PLUS_USA("Mini Plus", R.drawable.device_c2mini2, R.drawable.results_pic_c2mini2, 0, true, DeviceCategory.IP_CAMERA,
            "CS-CV200-A1-"),
    Mini_360("Mini 360", R.drawable.device_c6, R.drawable.results_pic_c6, 0, true, DeviceCategory.IP_CAMERA, "CS-CV240"),

    //CS-CV246-A0-3B1WFR
    C6C("C6C", R.drawable.device_c6c_1, R.drawable.results_pic_c6c_1, 0, true, DeviceCategory.IP_CAMERA, "CS-CV246-A0-3B1WFR","CS-CV246-"),
    //C6C CS-CV246-B0-3B1WFR,CS-CV246-B0-3B2WFR
    C6C_2("C6C", R.drawable.device_c6c_2, R.drawable.results_pic_c6c_2, 0, true, DeviceCategory.IP_CAMERA, "CS-CV246-B0-3B1WFR","CS-CV246-B0-3B2WFR"),

    //C6H CS-CV246-A0-3B1WFR
//    C6H("C6H", R.drawable.device_c6h, R.drawable.results_pic_c6h, 0, true, DeviceCategory.IP_CAMERA, "CS-CV246-"),

    //C6T美分
    C6T("C6T", R.drawable.device_mini_360_plus, R.drawable.results_mini_360_plus,R.drawable.device_c6t_bg, 0, true, DeviceCategory.IP_CAMERA,"CS-CV248"),
    //C6T美分  CS-CV248-A0-32WMFR
    MINI_360_PLUS("C6TC", R.drawable.device_mini_360_plus, R.drawable.results_mini_360_plus, R.drawable.device_c6t_bg,R.drawable.my_c6t, true, DeviceCategory.IP_CAMERA, "CS-CV248-A1-","CS-CV248"),
    //C13美分
    MINI_TROOPER("Mini Trooper", R.drawable.device_mini_trooper, R.drawable.results_mini_trooper, 0, true, DeviceCategory.IP_CAMERA, "CS-CV316-A1-"),
    //w2s
    W2S("Base Station", R.drawable.device_w2s, R.drawable.results_w2s, R.drawable.device_w2s_bg, R.drawable.my_w2s, DeviceCategory.ROUTER, "CS-W2S", "CS-W2S-A1"),
    //    CS-W2D-NAM
    W2D("W2D", R.drawable.device_w2d, R.drawable.results_w2d, R.drawable.device_w2d_bg, R.drawable.my_w2d, DeviceCategory.ROUTER, "CS-W2D-"),

    //    CS-WLB
    WLB("WLB", R.drawable.device_wlb, R.drawable.results_wlb, R.drawable.device_wlb_bg, R.drawable.my_wlb, DeviceCategory.ROUTER, "CS-WLB-"),

    //海外5K设备 CS-VR104D-A1-15Hv
    VR104D_5k("VR104D", R.drawable.device_5k_vr104d, R.drawable.result_5k_vr104d, R.drawable.device_5k_vr104d_bg,R.drawable.my_5k_vr,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-VR104D-A1-15"),
    //海外5K设备 CS-VR108D-A1-15Hv
    VR108D_5k("VR108D", R.drawable.device_5k_vr104d, R.drawable.result_5k_vr104d, R.drawable.device_5k_vr104d_bg,R.drawable.my_5k_vr,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-VR108D-A1-15"),
    //海外5K设备 CS-VR116D-A1-15Hv
    VR116D_5k("VR116D", R.drawable.device_5k_vr116d, R.drawable.result_5k_vr116d, R.drawable.device_5k_vr116d_bg,R.drawable.my_5k_vr116,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-VR116D-A1-15"),

    //海外2.2.1 CS-VR104D-A1-13Hv
    VR104D("VR104D", R.drawable.device_cs_vr104d, R.drawable.result_cs_vr104d, R.drawable.device_vr104d_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-VR104D-"),
    //海外2.2.1 CS-VR108D-A1-13Hv
    VR108D("VR108D", R.drawable.result_cs_vr108d, R.drawable.device_cs_vr108d, R.drawable.device_vr108d_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-VR108D-"),
    //海外2.2.1   CS-VR116D-A1-13Hv
    VR116D("VR116D", R.drawable.result_cs_vr116d, R.drawable.device_cs_vr116d, R.drawable.device_vr116d_bg,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "CS-VR116D-"),
    //c3w   CS-CV310-A0-1B2WFR,CS-CV310-A0-3B1WFR
    C3W("C3W", R.drawable.result_cs_c3w, R.drawable.device_cs_c3w, 0, true, DeviceCategory.IP_CAMERA, "CS-CV310-"),

    //CS-CV260-B0-1C2WPMFBR  C31电池设备
    C31("C31", R.drawable.device_other, R.drawable.results_pic_default, 0, true, DeviceCategory.IP_CAMERA, "CS-CV260-","HSBC1"),

    //CS-C3A-A0-1C2WPMFBR  C3A电池设备
    C3A("C3A", R.drawable.device_other, R.drawable.results_pic_default, 0, true, DeviceCategory.IP_CAMERA, "CS-C3A-A0","HSBC1"),

    HSDVR1("HSDVR1", R.drawable.device_dvr, R.drawable.results_pic_default, R.drawable.device_default_details,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "HSDVR1"),
    // Z1
    Z1("Z1", R.drawable.device_z1, R.drawable.results_pic_z1, R.drawable.z1_bg, 0, true, DeviceCategory.IP_CAMERA, "CS-Z1"),
    //海外c6p  CS-CV346-A0-7A3WFR   CS-CV346   (A0-7A3WFR)
    C6P("C6P", R.drawable.device_c6p, R.drawable.results_c6p, R.drawable.c6p_bg, R.drawable.my_c6p, true, DeviceCategory.IP_CAMERA, "CS-CV346"),
    // WIFI_CAMERA
    IPC("", R.drawable.device_ipc_dome, R.drawable.results_pic_default, 0, R.drawable.my_cover620, true, DeviceCategory.IP_CAMERA, "WIFI_CAMERA"),
    // DVR
    DVR("", R.drawable.device_dvr, R.drawable.results_pic_default, R.drawable.device_default_details,
            DeviceCategory.DIGITAL_VIDEO_RECORDER, "DVR"),
    // DH1 猫眼
    DH1("", R.drawable.device_dh1, R.drawable.result_pic_dh1, R.drawable.device_default_details, R.drawable.my_db1,true, DeviceCategory.IP_CAMERA, "CS-DH1-", "CS-DP1-","CS-CV336-"),
    //门铃   CS-DB1-A0-1B3WPFR
    DOORBELL("", R.drawable.device_doorbell, R.drawable.result_pic_doorbell, R.drawable.device_default_details, R.drawable.my_doorbell, true, DeviceCategory.IP_CAMERA, "HSDB2", "HSDB3", "CS-DB1-"),


    //电池门铃
    DOORBELL_BATTERY("",0, 0, 0,0, true, DeviceCategory.IP_CAMERA, "CS-DB2-"),

    DOORBELL_HIK("", R.drawable.device_doorbell_hik, R.drawable.result_pic_doorbell_hik, R.drawable.device_default_details,R.drawable.my_doorbell_hik, true, DeviceCategory.IP_CAMERA,"HSDB1"),

    //安防灯
    LIGHT("", R.drawable.device_light, R.drawable.result_pic_light, R.drawable.device_default_details, true, DeviceCategory.IP_CAMERA, "CS-CV250","HSFLC1WH","HSFLC1"),
    // OTHER
    OTHER("", R.drawable.device_other, R.drawable.results_pic_default, R.drawable.device_default_details, R.drawable.my_cover620, null, "OTHER"),

    //插座
    T30("T30", R.drawable.device_other, R.drawable.results_pic_default, R.drawable.device_default_details, DeviceCategory.PLUG, "CS-T30", "CS-T31","CS-HAE-S", "CS-HAE-T");

    private String[] key;
    private String display;
    private int drawable1ResId;
    private int drawable2ResId;
    private int detailDrawableResId;
    private int myDrawableResId;
    private boolean camera;
    private DeviceCategory category;

    private DeviceModel(String display, int drawable1ResId, int drawable2ResId, int detailDrawableResId,
                        DeviceCategory category, String... key) {
        this(display, drawable1ResId, drawable2ResId, detailDrawableResId, false, category, key);
    }

    private DeviceModel(String display, int drawable1ResId, int drawable2ResId, int detailDrawableResId,
                        int myDrawableResId, DeviceCategory category, String... key) {
        this(display, drawable1ResId, drawable2ResId, detailDrawableResId, myDrawableResId, false, category, key);
    }

    private DeviceModel(String display, int drawable1ResId, int drawable2ResId, int detailDrawableResId,
                        boolean camera, DeviceCategory category, String... key) {
        this(display, drawable1ResId, drawable2ResId, detailDrawableResId, 0, camera, category, key);
    }

    private DeviceModel(String display, int drawable1ResId, int drawable2ResId, int detailDrawableResId,
                        int myDrawableResId, boolean camera, DeviceCategory category, String... key) {
        this.display = display;
        this.drawable1ResId = drawable1ResId;
        this.drawable2ResId = drawable2ResId;
        this.myDrawableResId = myDrawableResId;
        this.detailDrawableResId = detailDrawableResId;
        this.camera = camera;
        this.key = key;
        this.category = category;
    }

    public int getDrawable1ResId() {
        return drawable1ResId;
    }

    public int getDrawable2ResId() {
        return drawable2ResId;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isCamera() {
        return camera;
    }

    public DeviceCategory getCategory() {
        return category;
    }

//    public static DeviceModel getDeviceModel(DeviceInfoEx device) {
//        return C1;
//    }

    public static DeviceModel getDeviceModel(String model/*, int supportRelatedDevice*/) {
        if (TextUtils.isEmpty(model))
            return null;

        // 确认百度设备
        if (model.endsWith("-BD")) {
            for (DeviceModel e : DeviceModel.values()) {
                if (e.name().endsWith("_BD")) {
                    for (String key : e.key)
                        if (model.startsWith(key))
                            return e;
                }
            }
        }

        // 确认支持SmartConfig设备
        if (model.endsWith("WFR")) {
            for (DeviceModel e : DeviceModel.values()) {
                if (e.name().endsWith("_WIFI")) {
                    for (String key : e.key)
                        if (model.startsWith(key))
                            return e;
                }
            }
        }

        // 确认萤石设备
        for (DeviceModel e : DeviceModel.values()) {
            for (String key : e.key)
                if (model.startsWith(key))
                    return e;
        }

//        // 确认行业设备
//        if (supportRelatedDevice == DeviceConsts.NOT_SUPPORT)
//            return IPC;
//        else if (supportRelatedDevice == DeviceConsts.SUPPORT_RELATED_D1)
//            return DVR;

        return OTHER;
    }

    public String[] getKey() {
        return key;
    }

    public void setKey(String[] key) {
        this.key = key;
    }
}