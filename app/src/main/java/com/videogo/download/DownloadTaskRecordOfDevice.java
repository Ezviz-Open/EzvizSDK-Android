package com.videogo.download;

import com.videogo.stream.EZDeviceStreamDownload;

public class DownloadTaskRecordOfDevice extends DownLoadTaskRecordAbstract {

    public DownloadTaskRecordOfDevice(EZDeviceStreamDownload downloader, int notificationId) {
        super(downloader, notificationId);
    }

    @Override
    public void stopDownloader() {
        if (mDownloader instanceof EZDeviceStreamDownload){
            ((EZDeviceStreamDownload) mDownloader).stop();
        }
    }
}
