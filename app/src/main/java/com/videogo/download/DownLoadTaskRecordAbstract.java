package com.videogo.download;

public abstract class DownLoadTaskRecordAbstract {

    protected int mNotificationId;
    protected Object mDownloader;

    protected DownLoadTaskRecordAbstract(Object downloader, int notificationId){
        mDownloader = downloader;
        mNotificationId = notificationId;
    }

    public int getNotificationId() {
        return mNotificationId;
    }

    public abstract void stopDownloader();

}
