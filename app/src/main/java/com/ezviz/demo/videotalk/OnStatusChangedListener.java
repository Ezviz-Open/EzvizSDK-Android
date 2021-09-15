package com.ezviz.demo.videotalk;

import android.view.Surface;

import com.ezviz.videotalk.videomeeting.ConstVideoMeeting;

public interface OnStatusChangedListener {

    void onSurfaceSet(int clientId, ConstVideoMeeting.StreamState type, Surface surface);

    void onUnSubscribe(int clientId, ConstVideoMeeting.StreamState type);

    void onSubscribe(int clientId);
}
