package com.videogo.download;

import com.videogo.stream.EZCloudStreamDownload;

public class DownloadTaskRecordOfCloud extends DownLoadTaskRecordAbstract {

    public DownloadTaskRecordOfCloud(EZCloudStreamDownload downloader, int notificationId) {
        super(downloader, notificationId);
    }

    @Override
    public void stopDownloader() {
        if (mDownloader instanceof EZCloudStreamDownload){
            ((EZCloudStreamDownload) mDownloader).stop();
        }
    }
}
