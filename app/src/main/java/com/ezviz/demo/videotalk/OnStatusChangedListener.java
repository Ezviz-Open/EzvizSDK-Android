package com.ezviz.demo.videotalk;

import android.view.Surface;

import com.ezviz.sdk.videotalk.meeting.EZRtcParam;

public interface OnStatusChangedListener {

    void onSurfaceSet(String userId, EZRtcParam.StreamType type, Surface surface);

    void onUnSubscribe(String userId, EZRtcParam.StreamType type);

    void onSubscribe(String clientId);
}
