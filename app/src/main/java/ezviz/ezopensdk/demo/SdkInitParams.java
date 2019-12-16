package ezviz.ezopensdk.demo;

import com.google.gson.Gson;

public class SdkInitParams {

    public String appKey;
    public String accessToken;
    public int serverAreaId;
    public String openApiServer;
    public String openAuthApiServer;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
