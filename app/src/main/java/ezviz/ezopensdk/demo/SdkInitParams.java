package ezviz.ezopensdk.demo;

import com.google.gson.Gson;

public class SdkInitParams {

    public String appKey;
    public String accessToken;
    public int serverAreaId;
    public String openApiServer;
    public String openAuthApiServer;
    public boolean usingGlobalSDK;

    private SdkInitParams(){}

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static SdkInitParams createBy(ServerAreasEnum serverArea){
        SdkInitParams sdkInitParams = new SdkInitParams();
        if (serverArea != null){
            sdkInitParams.appKey = serverArea.defaultOpenAuthAppKey;
            sdkInitParams.serverAreaId = serverArea.id;
            sdkInitParams.openApiServer = serverArea.openApiServer;
            sdkInitParams.openAuthApiServer = serverArea.openAuthApiServer;
            sdkInitParams.usingGlobalSDK = serverArea.usingGlobalSDK;
        }
        return sdkInitParams;
    }

}
