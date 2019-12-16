package com.videogo.api;

import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EzvizAPI;
import com.videogo.util.LocalInfo;
import com.videogo.util.ReflectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * 通过透传接口实现平台对应业务接口
 */
public class TransferAPI {
    public static final String RESULT = "result";
    
    public static final int SUSCCEED = 200;
    
    public static final String CODE = "code";
    
    public static final String MSG = "description";
    
    public static boolean paserCode(String reponse) throws BaseException, JSONException {
        JSONObject jsonObject = new JSONObject(reponse);
        JSONObject result = jsonObject.getJSONObject(RESULT);
        int resultCode = result.optInt(CODE, ErrorCode.ERROR_INNER_WEBRESPONSE_JSONERROR);
        String resultDesc = result.optString(MSG, "Resp Error:" + resultCode);
        
        if (resultCode == SUSCCEED) {
            return true;
        } else {
            throw new BaseException(resultDesc, resultCode);
        }
    }
    
	//布撤防接口
	public static boolean updateDefence(String deviceSerial, int isDefence) throws BaseException, JSONException {
		UpdateDefenceReq updateDefenceReq = new UpdateDefenceReq();
		updateDefenceReq.params.accessToken = LocalInfo.getInstance().getEZAccesstoken().getAccessToken();
		updateDefenceReq.params.deviceSerial = deviceSerial;
		updateDefenceReq.params.isDefence = isDefence;
		JSONObject reqObj = ReflectionUtils.convObjectToJSON(updateDefenceReq);
		String response = EzvizAPI.getInstance().transferAPI(reqObj.toString());
		return paserCode(response);
	}
	
	//获取开通萤石云服务短信接口
    public static boolean openYSServiceSmsCode(String phone) throws BaseException, JSONException {
        OpenYSServiceSmsCodeReq openYSServiceSmsCodeReq = new OpenYSServiceSmsCodeReq();
        openYSServiceSmsCodeReq.params.phone = phone;
        JSONObject reqObj = ReflectionUtils.convObjectToJSON(openYSServiceSmsCodeReq);
        String response = EzvizAPI.getInstance().transferAPI(reqObj.toString());
        return paserCode(response);
    }
    
    //开通萤石云服务接口
    public static boolean openYSService(String phone, String smsCode) throws BaseException, JSONException {
        OpenYSServiceReq openYSServiceReq = new OpenYSServiceReq();
        openYSServiceReq.params.phone = phone;
        openYSServiceReq.params.smsCode = smsCode;
        JSONObject reqObj = ReflectionUtils.convObjectToJSON(openYSServiceReq);
        String response = EzvizAPI.getInstance().transferAPI(reqObj.toString());
        return paserCode(response);
    }
    
    //获取安全验证短信接口
    public static boolean getSecureSmcCode() throws BaseException, JSONException {
        GetSecureSmcCodeReq getSecureSmcCodeReq = new GetSecureSmcCodeReq();
        getSecureSmcCodeReq.params.accessToken = LocalInfo.getInstance().getEZAccesstoken().getAccessToken();
        JSONObject reqObj = ReflectionUtils.convObjectToJSON(getSecureSmcCodeReq);
        String response = EzvizAPI.getInstance().transferAPI(reqObj.toString());
        return paserCode(response);
    }
    
    //安全验证接口
    public static boolean secureValidate(String smsCode) throws BaseException, JSONException {
        SecureValidateReq secureValidateReq = new SecureValidateReq();
        secureValidateReq.params.smsCode = smsCode;
        secureValidateReq.params.accessToken = LocalInfo.getInstance().getEZAccesstoken().getAccessToken();
        JSONObject reqObj = ReflectionUtils.convObjectToJSON(secureValidateReq);
        String response = EzvizAPI.getInstance().transferAPI(reqObj.toString());
        return paserCode(response);
    }
    
    //添加设备接口
    public static boolean addDevice(String deviceSerial, String validateCode) throws BaseException, JSONException {
        AddDeviceReq addDeviceReq = new AddDeviceReq();
        addDeviceReq.params.deviceSerial = deviceSerial;
        addDeviceReq.params.validateCode = validateCode;
        addDeviceReq.params.accessToken = LocalInfo.getInstance().getEZAccesstoken().getAccessToken();
        JSONObject reqObj = ReflectionUtils.convObjectToJSON(addDeviceReq);
        String response = EzvizAPI.getInstance().transferAPI(reqObj.toString());
        return paserCode(response);
    }
}
