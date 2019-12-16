package com.videogo.api;

import com.videogo.openapi.annotation.Serializable;

//布撤防接口
public class UpdateDefenceReq {
	
	@Serializable(name = "method")
    public String method = "updateDefence";//方法名
	
	@Serializable(name = "params")
    public Params params = new Params();
	
	@Serializable
	public class Params {
		@Serializable(name = "accessToken")
	    public String accessToken;
		
		@Serializable(name = "deviceSerial")
	    public String deviceSerial;
		
		@Serializable(name = "isDefence")
	    public int isDefence;
	}
}
