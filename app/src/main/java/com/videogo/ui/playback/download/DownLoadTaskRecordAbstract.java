package com.videogo.ui.playback.download;

public abstract class DownLoadTaskRecordAbstract {

    protected Object mDownloader;

    protected DownLoadTaskRecordAbstract(Object downloader) {
        mDownloader = downloader;
    }

    public abstract void stopDownloader();

}
