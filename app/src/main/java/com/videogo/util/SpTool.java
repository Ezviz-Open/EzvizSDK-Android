package com.videogo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.videogo.global.ValueKeys;

/**
 * 用于demo的SharedPreference工具类
 */
public class SpTool {

    private final static String TAG = SpTool.class.getSimpleName();
    private final static String SP_FILE_NAME = "demo";
    private static SharedPreferences mSP = null;

    /**
     * 初始化
     */
    public static void init(@NonNull Context context){
        mSP = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     *安全Key存
     */
    public static String obtainValue(ValueKeys key){
        return obtainValue(key.name());
    }

    /**
     * 存
     */
    public static String obtainValue(String key){
        if (mSP == null){
            Log.e(TAG, "SpTool is not init!!!");
            return null;
        }
        return mSP.getString(key, null);
    }

    /**
     * 取
     */
    public static void storeValue(ValueKeys key, String value){
        storeValue(key.name(), value);
    }

    /**
     * 安全Kye取
     */
    public static void storeValue(String key, String value){
        if (mSP == null){
            Log.e(TAG, "SpTool is not init!!!");
            return;
        }
        mSP.edit().putString(key, value).apply();
    }




    /**
     *安全Key存
     */
    public static boolean obtainBooleanValue(ValueKeys key){
        return obtainBooleanValue(key.name());
    }

    /**
     * 存
     */
    public static boolean obtainBooleanValue(String key){
        if (mSP == null){
            Log.e(TAG, "SpTool is not init!!!");
            return false;
        }
        return mSP.getBoolean(key, false);
    }

    /**
     * 取
     */
    public static void storeBooleanValue(ValueKeys key, boolean value){
        storeBooleanValue(key.name(), value);
    }

    /**
     * 安全Kye取
     */
    public static void storeBooleanValue(String key, boolean value){
        if (mSP == null){
            Log.e(TAG, "SpTool is not init!!!");
            return;
        }
        mSP.edit().putBoolean(key, value).apply();
    }

}
