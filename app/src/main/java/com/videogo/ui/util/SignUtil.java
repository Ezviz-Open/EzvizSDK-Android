package com.videogo.ui.util;

import android.text.TextUtils;

import com.videogo.util.MD5Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SignUtil {

    public static String getGetSmsCodeSign(String phone) {
        Map<String, Object> map = new HashMap<String, Object>();
        {
            map.put("type", 1);
            map.put("userId", "654321");
            map.put("phone", phone);
        }
        Map<String, Object> resultMap = paramsInit("c279ded87d3f4fdca7658f95fb5f1d9e",
                "b097e53bb9627e7e32b7a8001c701151", "description/get", map);
        JSONObject signJson;
        try {
            signJson = setJosn(resultMap);
            return signJson.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        };
        return null;
    }
    
    public static String getGetAccessTokenSign(String phone) {
        Map<String, Object> map = new HashMap<String, Object>();
        {
            map.put("userId", "654321");
            map.put("phone", phone);
        }
        Map<String, Object> resultMap = paramsInit("c279ded87d3f4fdca7658f95fb5f1d9e",
                "b097e53bb9627e7e32b7a8001c701151", "token/accessToken/get", map);
        JSONObject signJson;
        try {
            signJson = setJosn(resultMap);
            return signJson.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        };
        return null;
    }
    
    public static Map<String, Object> paramsInit(String appKey, String appSecret, String method, Map<String, Object> paramsMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        long time = System.currentTimeMillis() / 1000;
        StringBuilder paramString = new StringBuilder();
        if (paramsMap != null && !paramsMap.isEmpty()) {
            List<String> paramList = new ArrayList<String>();
            for (Iterator<String> it = paramsMap.keySet().iterator(); it.hasNext();) {
                String key1 = it.next();
                String param = key1 + ":" + paramsMap.get(key1);
                paramList.add(param);
            }
            String[] params = paramList.toArray(new String[paramList.size()]);
            Arrays.sort(params);
            for (String param : params) {
                paramString.append(param).append(",");
            }
        }
        paramString.append("method").append(":").append(method).append(",");
        paramString.append("time").append(":").append(time).append(",");
        paramString.append("secret").append(":").append(appSecret);
        String sign = MD5Util.getMD5String(paramString.toString().trim());

        Map<String, Object> systemMap = new HashMap<String, Object>();
        systemMap.put("ver", "1.0");
        systemMap.put("sign", sign);
        systemMap.put("name", appKey);
        systemMap.put("time", time);

        map.put("system", systemMap);
        map.put("method", method);
        map.put("params", paramsMap);
        map.put("id", "123456");
        return map;
    }
    
    public static JSONObject setJosn(Map map) throws Exception {
            JSONObject json = null;
            StringBuffer temp = new StringBuffer();
            if (!map.isEmpty()) {
                    temp.append("{");
                    // 遍历map
                    Set set = map.entrySet();
                    Iterator i = set.iterator();
                    while (i.hasNext()) {
                            Map.Entry entry = (Map.Entry) i.next();
                            String key = (String) entry.getKey();
                            Object value = entry.getValue();
                            temp.append("\"" + key + "\":");
                            if (value instanceof Map<?, ?>) {
                                    temp.append(setJosn((Map<String, Object>) value) + ",");
                            } else if (value instanceof List<?>) {
                                    temp.append(setList((List<Map<String, Object>>) value)
                                                    + ",");
                            } else {
                                    temp.append("\"" + value + "\"" + ",");
                            }
                    }
                    if (temp.length() > 1) {
                            temp = new StringBuffer(temp.substring(0, temp.length() - 1));
                    }
                    temp.append("}");
                    json = new JSONObject(temp.toString());
            }
            return json;
    }

    public static String setList(List<Map<String, Object>> list)
                    throws Exception {
            String jsonL = "";
            StringBuffer temp = new StringBuffer();
            temp.append("[");
            for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> m = list.get(i);
                    if (i == list.size() - 1) {
                            temp.append(setJosn(m));
                    } else {
                            temp.append(setJosn(m) + ",");
                    }
            }
            if (temp.length() > 1) {
                    temp = new StringBuffer(temp.substring(0, temp.length()));
            }
            temp.append("]");
            jsonL = temp.toString();
            return jsonL;
    }

    public static Map<String, Object> getJosn(String jsonStr) throws Exception {
            Map<String, Object> map = new HashMap<String, Object>();
            if (!TextUtils.isEmpty(jsonStr)) {
                    JSONObject json = new JSONObject(jsonStr);
                    Iterator i = json.keys();
                    while (i.hasNext()) {
                            String key = (String) i.next();
                            String value = json.getString(key);
                            if (value.indexOf("{") == 0) {
                                    map.put(key.trim(), getJosn(value));
                            } else if (value.indexOf("[") == 0) {
                                    map.put(key.trim(), getList(value));
                            } else {
                                    map.put(key.trim(), value.trim());
                            }
                    }
            }
            return map;
    }

    public static List<Map<String, Object>> getList(String jsonStr)
                    throws Exception {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            JSONArray ja = new JSONArray(jsonStr);
            for (int j = 0; j < ja.length(); j++) {
                    String jm = ja.get(j) + "";
                    if (jm.indexOf("{") == 0) {
                            Map<String, Object> m = getJosn(jm);
                            list.add(m);
                    }
            }
            return list;
    }
}
