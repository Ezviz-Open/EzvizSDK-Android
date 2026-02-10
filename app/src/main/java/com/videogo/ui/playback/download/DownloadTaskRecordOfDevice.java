package com.videogo.ui.playback.download;

import com.videogo.stream.EZDeviceStreamDownload;

public class DownloadTaskRecordOfDevice extends DownLoadTaskRecordAbstract {

    public DownloadTaskRecordOfDevice(EZDeviceStreamDownload downloader) {
        super(downloader);
    }

    @Override
    public void stopDownloader() {
        if (mDownloader instanceof EZDeviceStreamDownload) {
            ((EZDeviceStreamDownload) mDownloader).stop();
        }
    }
}
