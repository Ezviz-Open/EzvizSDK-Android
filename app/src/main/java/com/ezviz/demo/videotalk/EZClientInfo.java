package com.ezviz.demo.videotalk;

import com.ezviz.sdk.videotalk.meeting.EZRtcParam;

import java.util.List;

public class EZClientInfo {

    public String userId;
    public int volume;
    public int mVideoAvailable;  //视频启用状态 0-关闭 1-主流 5-主子流
    public boolean mAudioAvailable;
    public EZRtcParam.NetQuality netQuality = EZRtcParam.NetQuality.BAV_NETWORK_QUALITY_UNKNOWN;  //网络质量，参考
    public EZRtcParam.StreamType subscribeType = EZRtcParam.StreamType.NONE; //0-不订阅 1-订阅主流 2-订阅子流

    public static EZClientInfo findClient(String userName, List<EZClientInfo> clientInfoList){
        if (clientInfoList == null){
            return null;
        }
        for (EZClientInfo clientInfo : clientInfoList){
            if (clientInfo.userId.equals(userName)){
                return clientInfo;
            }
        }
        return null;
    }

    public static void insertOrReplace(EZClientInfo clientInfo, List<EZClientInfo> clientInfoList){
        if (clientInfoList == null){
            return;
        }

        EZClientInfo in = findClient(clientInfo.userId, clientInfoList);
        if (in == null){
            clientInfoList.add(clientInfo);
        }else {
            int index = clientInfoList.indexOf(in);
            clientInfoList.remove(index);
            clientInfoList.add(index, clientInfo);
        }
    }

    public static int delete(String userId, List<EZClientInfo> clientInfoList){
        int ret = -1;
        if (clientInfoList != null){
            for (EZClientInfo clientInfo : clientInfoList){
                if (clientInfo.userId.equals(userId)){
                    ret = clientInfoList.indexOf(clientInfo);
                    clientInfoList.remove(clientInfo);
                    break;
                }
            }
        }
        return ret;
    }
}
