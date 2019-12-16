package com.videogo.ui.util;

import android.content.Context;

import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;

import java.util.HashMap;
import java.util.Map;

import ezviz.ezopensdk.demo.SpTool;
import ezviz.ezopensdk.demo.SpTool;

public class DataManager {
    private static DataManager mDataManager;

    private static LruBitmapPool mBitmapPool;

    private DataManager(){
        Map<String, String> mDeviceSerialVerifyCodeMap = new HashMap<String, String>();
    }

   public static DataManager getInstance(){
       if (mDataManager == null){
            mDataManager = new DataManager();
       }
        return mDataManager;
    }

    public LruBitmapPool getBitmapPool(Context context){
        if (mBitmapPool == null){
            MemorySizeCalculator calculator = new MemorySizeCalculator(context);
            int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
            mBitmapPool = new LruBitmapPool(defaultBitmapPoolSize);
        }
        return mBitmapPool;
    }

    public synchronized void setDeviceSerialVerifyCode(String deviceSerial,String verifyCode){
        SpTool.storeValue(deviceSerial, verifyCode);
    }

    public synchronized String getDeviceSerialVerifyCode(String deviceSerial){
        return SpTool.obtainValue(deviceSerial);
    }
}


