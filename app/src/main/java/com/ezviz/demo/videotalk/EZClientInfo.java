package com.ezviz.demo.videotalk;

import com.ezviz.videotalk.videomeeting.ConstVideoMeeting;

import java.util.List;

public class EZClientInfo {

    public int id;
    public String name;
    public int roomId;
    public int volume;
    public int mVideoAvailable;  //视频启用状态 0-关闭 1-主流 5-主子流
    public int mAudioAvailable;
    public ConstVideoMeeting.NetQuality netQuality = ConstVideoMeeting.NetQuality.BAV_NETWORK_QUALITY_UNKNOWN;  //网络质量，参考
    public ConstVideoMeeting.StreamState subscribeType = ConstVideoMeeting.StreamState.BAV_STREAM_INVALID; //0-不订阅 1-订阅主流 2-订阅子流

    public static EZClientInfo findClient(int clientId, List<EZClientInfo> clientInfoList){
        if (clientInfoList == null){
            return null;
        }
        for (EZClientInfo clientInfo : clientInfoList){
            if (clientInfo.id == clientId){
                return clientInfo;
            }
        }
        return null;
    }

    public static void insertOrReplace(EZClientInfo clientInfo, List<EZClientInfo> clientInfoList){
        if (clientInfoList == null){
            return;
        }

        EZClientInfo in = findClient(clientInfo.id, clientInfoList);
        if (in == null){
            clientInfoList.add(clientInfo);
        }else {
            int index = clientInfoList.indexOf(in);
            clientInfoList.remove(index);
            clientInfoList.add(index, clientInfo);
        }
    }

    public static int delete(int clientId, List<EZClientInfo> clientInfoList){
        int ret = -1;
        if (clientInfoList != null){
            for (EZClientInfo clientInfo : clientInfoList){
                if (clientInfo.id == clientId){
                    ret = clientInfoList.indexOf(clientInfo);
                    clientInfoList.remove(clientInfo);
                    break;
                }
            }
        }
        return ret;
    }
}
