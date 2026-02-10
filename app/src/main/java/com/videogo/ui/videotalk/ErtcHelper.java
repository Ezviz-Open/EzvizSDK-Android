package com.videogo.ui.videotalk;

import android.content.Context;
import android.os.Environment;

import com.ez.baselib.utils.AppSpUtils;
import com.ez.baselib.utils.LogUtil;
import com.ez.basertc.RTCConstant;
import com.ez.ertcengine.ERTCEngine;
import com.videogo.util.LocalInfo;

/**
 * Copyright (C) 2025 HIKVISION Inc.
 * Comments:
 *
 * @author ChengJun9
 * @date 2025/4/3 16:19
 */
public class ErtcHelper {

    protected static final String TAG = ErtcHelper.class.getSimpleName();

    public static ERTCEngine ertcEngine = null;

    public static void initERTCEngine(Context context, String appId, ERTCEngine.OnInitListener onInitListener) {
        // 环境设置
        AppSpUtils.saveHost(context, LocalInfo.getInstance().getOriginalServAddr());
        // 初始化
        RTCConstant.RTCEngineConfig config = new RTCConstant.RTCEngineConfig();
        config.appId = appId;
        config.context = context.getApplicationContext();
        // 音频编码类型[必须，S10音频使用AAC]
        config.audioCodeType = RTCConstant.ErtcAudioCodeType.AAC;
        // 日志路径[非必须]
        config.logLevel = LogUtil.ERTC_LOG_LEVEL_DEBUG;
        config.logPath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
        // 日志回调[非必须]
        LogUtil.setErtcLogCallback((logLevel, tag, content) -> {
            // TODO 日志回调，可以根据自己的需要打印或写入日志
            com.videogo.util.LogUtil.d(tag, content);
        });
        ERTCEngine.init(config, onInitListener);
    }

}
