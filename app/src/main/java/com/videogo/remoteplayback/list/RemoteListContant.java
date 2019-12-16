/**
 * @ProjectName: null
 * @Copyright: null
 * @address: https://www.ys7.com
 * @date: 2014-6-6 上午11:22:05
 * @Description:
 */
package com.videogo.remoteplayback.list;

public class RemoteListContant {

    // -----------------------bundle name 定义-------------------
    public static final String DEVICESERIAL_INTENT_KEY = "deviceSerial";
    public static final String CHANNELNO_INTENT_KEY = "channelNo";
    public static final String QUERY_DATE_INTENT_KEY = "queryDate";
    public static final String ALARM_TIME_INTENT_KEY = "alarmTime";
    // Progress MAX值
    public static final int PROGRESS_MAX_VALUE = 1000;
    public static final String VIDEO_DUAR_BEGIN_INIT = "00:00:00";
    public static final int REMOTE_LIST_MAX_TIMEOUT = 45;

    // 类型定义
    public static final int CAPTURE_PIC = 0;
    public static final int VIDEO_RECORD = 1;
    public static final int VIDEO_RECORD_STOP = 2;

    // ---------------回放列表查询状态-------------------------
    // 查询回放列表->查询成功
    public static final int QUERY_CLOUD_SUCCESSFUL_NOLOACL = 1;
    public static final int QUERY_CLOUD_SUCCESSFUL_HASLOCAL = 11;
    public static final int QUERY_CLOUD_SUCCESSFUL_LOCAL_EX = 12;
    // 查询回放列表->查询后无数据
    public static final int QUERY_NO_DATA = 2;
    // 查询回放列表 ->只有本地录像
    public static final int QUERY_ONLY_LOCAL = 3;
    // 查询回放列表 ->查询本地录像成功
    public static final int QUERY_LOCAL_SUCCESSFUL = 4;
    // 查询回放列表->异常
    public static final int QUERY_EXCEPTION = 10000;

    public static final int HAS_LOCAL = 0;
    public static final int NO_LOCAL = 1;
    public static final int EXCEPTION_LOCAL = 2;

    // --------------------回放列表类型-----------------
    public static final int TYPE_CLOUD = 0;
    public static final int TYPE_LOCAL = 1;
    public static final int TYPE_MORE = 2;
    public static final int UI_TYPE_COUNT = TYPE_MORE + 1;

    // ---------------------日历数据查询结果-------------
    public static final int CALENDAR_SEARCH_SUCCESS = 0;

    // -------------------回放类型-----------------------
    public static final int FETCH_TYPE_CAS = 1;
    public static final int FETCH_TYPE_PISA = 2;
    public static final int FETCH_TYPE_RTSP = 3;
    public static final int FETCH_TYPE_UPNP = 4;
    public static final int FETCH_TYPE_CLOUD = 5;
    public static final int FETCH_TYPE_NETSDK = 6;

    // ------------------播放库：播放状态--------------------
    public final static int STAT_STOP = 0;
    public final static int STAT_PLAY = 1;
    public final static int STAT_PAUSE = 2;
    public final static int STAT_SLOW = 3;
    public final static int STAT_FAST = 4;

    // ------------------播放时返回值-------------------------

    public final static int PLAY_CLOUD_PASSWORD_ERROR = 2000;
    public final static int PLAY_CLOUD_EXCEPTION = 2001;
    public final static int PLAY_SUCCESSFUL = 0;
    public final static int PLAY_LOCAL_PASSWORD_ERROR = 3010;
    public final static int PLAY_LOCAL_EXCEPTION = 3011;
    public final static int PLAY_CAPTURE_PICTURE_SUCCESS = 3012;// 播放抓图成功
    public final static int PLAY_CAPTURE_PICTURE_FAIL = 3013;// 播放抓图失败
    public final static int PLAY_START_RECORD_SUCCESS = 3014;// 开始录像成功
    public final static int PLAY_START_RECORD_FAIL = 3015;// 开始录像失败
    public final static int PLAY_ABORT = 3016;// 播放中断

    // ------------------Message Code 定义---------------------
    // 播放结束
    public final static int MSG_REMOTELIST_PLAY_SEGMENT_OVER = 4000;
    // 显示第一帧画面
    public final static int MSG_REMOTELIST_FIRST_FRAME = 4001;
    // 播放百分比
    public final static int MSG_REMOTELIST_PERCENTAGE = 4002;
    // 播放进度
    public final static int MSG_REMOTELIST_PLAY_PROGRESS = 4010;
    // 回放分辨率变化
    public static final int MSG_REMOTELIST_RATIO_CHANGED = 4011;
    // 回放链接中断
    public static final int MSG_REMOTELIST_CONNECTION_EXCEPTION = 4012;
    // 条数提示框消失
    public static final int MSG_REMOTELIST_UI_UPDATE = 5000;
    // 回放流超时
    public static final int MSG_REMOTELIST_STREAM_TIMEOUT = 6000;

    // ---------------------外部调用：播放状态-----------------------------
    // 初始状态
    public static final int STATUS_INIT = 0;
    // 停止状态
    public static final int STATUS_STOP = 1;
    // 播放状态
    public static final int STATUS_PLAY = 2;
    // 暂停状态
    public static final int STATUS_PAUSE = 3;
    // 退出页面
    public static final int STATUS_EXIT_PAGE = 4;
    // 播放第一帧为playing
    public static final int STATUS_PLAYING = 5;
    // 解密状态
    public static final int STATUS_DECRYPT = 6;    

    // ----------------------列表模式查询request 上传-------------------------------
}
