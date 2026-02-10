package com.videogo.ui.login;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.BuildConfig;

/**
 * 开放平台服务端在分为海外和国内，海外又分为5个大区
 * （北美、南美、新加坡（亚洲）、俄罗斯、欧洲）
 * 必须根据当前使用的AppKey对应大区切换到所在大区的服务器
 * 否则EZOpenSDK的接口调用将会出现异常
 */
public enum ServerAreasEnum {

    /**
     * 国内
     */
    ASIA_CHINA(0,"Asia-China(国内)", "https://open.ys7.com",
            "https://openauth.ys7.com",
            "26810f3acd794862b608b6cfbc32a6b8"),

    /**
     * 海外：俄罗斯
     */
    ASIA_Russia(5, "Asia-Russia(海外：俄罗斯)", "https://irusopen.ezvizru.com",
            "https://irusopenauth.ezvizru.com", true),
    /**
     * 海外：亚洲
     * （服务亚洲的所有国家，但不包括中国和俄罗斯）
     */
    ASIA(10, "Asia(海外：亚洲，不包括中国和俄罗斯)", "https://isgpopen.ezvizlife.com",
            "https://isgpopenauth.ezvizlife.com", true),
    /**
     * 海外：北美洲
     */
    NORTH_AMERICA(15,"North America(海外：北美洲)", "https://iusopen.ezvizlife.com",
            "https://iusopenauth.ezvizlife.com", true),
    /**
     * 海外：南美洲
     */
    SOUTH_AMERICA(20, "South America(海外：南美洲)", "https://isaopen.ezvizlife.com",
            "https://isaopenauth.ezvizlife.com", true),
    /**
     * 海外：欧洲
     */
    EUROPE(25, "Europe(海外：欧洲)", "https://ieuopen.ezvizlife.com",
            "https://ieuopenauth.ezvizlife.com",
            "5cadedf5478d11e7ae26fa163e8bac01",
            true),

    OTHER(26, "Other(自定义区域)", "",
            "",
            "1111"),

    OTHER_GLOBAL(27, "Other Global(海外自定义区域)", "",
            "",
            "1111",
            true),

    /*线上平台的id范围为0到99，测试平台的id范围为100+*/

    /**
     * 测试平台:test12
     */
    TEST12(110, "test12", "https://test12open.ezv-test.com",
            "https://test12openauth.ezv-test.com",
            "b22035492c7949bca95286382ed90b01"),

    /**
     * 测试平台:test13
     */
    TEST13(111, "test13", "https://test13open.ezv-test.com",
            "https://test13openauth.ezv-test.com",
            "b22035492c7949bca95286382ed90b01"),

    /**
     * 测试平台:testcn
     */
    TEST_CN(115, "testcn", "https://testcnopen.ezv-test.com",
            "https://testcnopenauth.ezv-test.com", true),
    /**
     * 测试平台:testus
     */
    TEST_US(120, "testus", "https://testusopen.ezv-test.com",
            "https://testusopenauth.ezv-test.com", true),
    /**
     * 测试平台:testeu
     */
//    TEST_EU(125, "testeu", "https://testeuopen.ezvizlife.com",
//            "https://testeuopenauth.ezvizlife.com", true),

    TEST_EU(125, "testeu", "https://ys-open.wens.com.cn",
            "https://test2auth.ys7.com:8643", true),

    /**
     * 温氏
     */
    WENSHI(126, "WenShi", "https://ezcpcloudopen.wens.com.cn",
            "https://ezcpcloudopenauth.wens.com.cn", false),

    /**
     * 华住
     */
    HUAZHU(127, "HuaZhu", "https://ezcpatctestopen.ys7.com",
                   "https://ezcpatctestopenauth.ys7.com", false);


//    TEST_NEW(130,"testnew", "https://ys-open.wens.com.cn",
//            "https://test2auth.ys7.com:8643", true);

    public int id;
    public String areaName;
    public String openApiServer;
    public String openAuthApiServer;
    // 预置的用于测试h5登录的appKey（该appKey的bundleId已绑定到ezviz.opensdk）
    public String defaultOpenAuthAppKey;
    // 是否正在海外域名，海外域名需要使用GlobalEZOpenSDK，反之使用EZOpenSDK
    public boolean usingGlobalSDK;

    ServerAreasEnum(int id, String areaName, String openApiServer, String openAuthApiServer){
        this(id, areaName, openApiServer, openAuthApiServer, null, false);
    }

    ServerAreasEnum(int id, String areaName, String openApiServer, String openAuthApiServer, String defaultOpenAuthAppKey){
        this(id, areaName, openApiServer, openAuthApiServer, defaultOpenAuthAppKey, false);
    }

    ServerAreasEnum(int id, String areaName, String openApiServer, String openAuthApiServer, boolean usingGlobalSDK){
        this(id, areaName, openApiServer, openAuthApiServer, null, usingGlobalSDK);
    }

    ServerAreasEnum(int id, String areaName, String openApiServer, String openAuthApiServer, String defaultOpenAuthAppKey, boolean usingGlobalSDK){
        this.id = id;
        this.areaName = areaName;
        this.openApiServer = openApiServer;
        this.openAuthApiServer = openAuthApiServer;
        this.defaultOpenAuthAppKey = defaultOpenAuthAppKey;
        this.usingGlobalSDK = usingGlobalSDK;
    }

    public static List<ServerAreasEnum> getAllServers(){
        List<ServerAreasEnum> serversList = new ArrayList<>();
        for (ServerAreasEnum server : values()) {
            boolean isTestServer = server.id >= 100;
            // 线上demo不展示测试平台
            if (!BuildConfig.DEBUG && isTestServer) {
                continue;
            }
            serversList.add(server);
        }
        return serversList;
    }

    @Override
    public String toString() {
        return "id: " + id + ", " + "areaName: " + areaName + ", " + "openApiServer: " + openApiServer + ", " + "openAuthApiServer: " + openAuthApiServer;
    }
}
