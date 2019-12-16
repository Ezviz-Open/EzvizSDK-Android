package ezviz.ezopensdk.demo;

import java.util.ArrayList;
import java.util.List;

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
    ASIA_CHINA(0,"Asia-China", "https://open.ys7.com", "https://openauth.ys7.com", "26810f3acd794862b608b6cfbc32a6b8"),
    /**
     * 海外：俄罗斯
     */
    ASIA_Russia(5, "Asia-Russia", "https://irusopen.ezvizru.com", "https://irusopenauth.ezvizru.com"),
    /**
     * 海外：亚洲
     * （服务亚洲的所有国家，但不包括中国和俄罗斯）
     */
    ASIA(10, "Asia", "https://isgpopen.ezvizlife.com", "https://isgpopenauth.ezvizlife.com"),
    /**
     * 海外：北美洲
     */
    NORTH_AMERICA(15,"North America", "https://iusopen.ezvizlife.com", "https://iusopenauth.ezvizlife.com"),
    /**
     * 海外：南美洲
     */
    SOUTH_AMERICA(20, "South America", "https://isaopen.ezvizlife.com", "https://isaopenauth.ezvizlife.com"),
    /**
     * 海外：欧洲
     */
    EUROPE(25, "Europe", "https://ieuopen.ezvizlife.com", "https://ieuopenauth.ezvizlife.com", "5cadedf5478d11e7ae26fa163e8bac01"),

    /*线上平台的id范围为0到99，测试平台的id范围为100+*/

    /**
     * 测试平台:test2
     */
    TEST2(100, "test2", "https://test2.ys7.com:9000", "https://test2auth.ys7.com:8643", "f24940e782454a0ca7cbf7c2a292a6c7"),
    /**
     * 测试平台:test11
     */
    TEST11(105, "test11", "https://test11open.ys7.com", "https://test11openauth.ys7.com"),
    /**
     * 测试平台:test12
     */
    TEST12(110, "test12", "https://test12open.ys7.com", "https://test12openauth.ys7.com", "680948cc41c44fbaac23d8b47be4028b"),
    /**
     * 测试平台:testcn
     */
    TEST_CN(115, "testcn", "https://testcnopen.ezvizlife.com", "https://testcnopenauth.ezvizlife.com"),
    /**
     * 测试平台:testus
     */
    TEST_US(120, "testus", "https://testusopen.ezvizlife.com", "https://testusopenauth.ezvizlife.com"),
    /**
     * 测试平台:testeu
     */
    TEST_EU(125, "testeu", "https://testeuopen.ezvizlife.com", "https://testeuopenauth.ezvizlife.com");

    public int id;
    public String areaName;
    public String openApiServer;
    public String openAuthApiServer;
    // 预置的用于测试h5登录的appKey（该appKey的bundleId已绑定到ezviz.opensdk）
    public String defaultOpenAuthAppKey;

    ServerAreasEnum(int id, String areaName, String openApiServer, String openAuthApiServer){
        this.id = id;
        this.areaName = areaName;
        this.openApiServer = openApiServer;
        this.openAuthApiServer = openAuthApiServer;
    }

    ServerAreasEnum(int id, String areaName, String openApiServer, String openAuthApiServer, String defaultOpenAuthAppKey){
        this.id = id;
        this.areaName = areaName;
        this.openApiServer = openApiServer;
        this.openAuthApiServer = openAuthApiServer;
        this.defaultOpenAuthAppKey = defaultOpenAuthAppKey;
    }

    public static List<ServerAreasEnum> getAllServers(){
        List<ServerAreasEnum> serversList = new ArrayList<>();
        for (ServerAreasEnum server : values()) {
            boolean isTestServer = server.id >= 100;
            // 线上demo不展示测试平台
            if (!com.videogo.openapi.BuildConfig.DEBUG && isTestServer) {
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
