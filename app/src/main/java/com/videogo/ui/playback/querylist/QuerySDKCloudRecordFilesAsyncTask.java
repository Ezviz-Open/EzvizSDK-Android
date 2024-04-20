package com.videogo.ui.playback.querylist;

import android.app.Activity;

import com.videogo.EzvizApplication;
import com.videogo.device.DeviceReportInfo;
import com.videogo.exception.BaseException;
import com.videogo.global.ValueKeys;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.ui.common.HikAsyncTask;
import com.videogo.ui.playback.RemoteListContant;
import com.videogo.ui.playback.bean.CloudPartInfoFileEx;
import com.videogo.util.CollectionUtil;
import com.videogo.util.SpTool;
import com.videogo.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;

public class QuerySDKCloudRecordFilesAsyncTask extends HikAsyncTask<String, Void, Integer> {
    private final String MINUTE;

    // 设备序列号
    private String deviceSerial;
    // 通道号
    private int channelNo;
    // 搜索日期（格式为：yyyy-MM-dd）
    private Date searchDate;
    private volatile boolean abort = false;
    private int cloudErrorCode = 0;
    private QueryPlayBackListTaskCallback queryPlayBackListTaskCallback;
    private List<CloudPartInfoFile> cloudPartFiles;
    List<CloudPartInfoFileEx> cloudPartInfoFileExList = new ArrayList<CloudPartInfoFileEx>();

    public QuerySDKCloudRecordFilesAsyncTask(String deviceSerial, int channelNo,
                                             QueryPlayBackListTaskCallback queryPlayBackListTaskCallback) {
        MINUTE = ((Activity) queryPlayBackListTaskCallback).getString(R.string.play_hour);
        this.deviceSerial = deviceSerial;
        this.channelNo = channelNo;
        this.queryPlayBackListTaskCallback = queryPlayBackListTaskCallback;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
    }

    public void setSearchDate(Date searchDate) {
        this.searchDate = searchDate;
    }

    @Override
    protected Integer doInBackground(String... params) {
        int queryCloudFiles = queryCloudFile();

        if (queryCloudFiles == RemoteListContant.QUERY_NO_DATA) {
            return RemoteListContant.QUERY_NO_DATA;
        } else {
            return RemoteListContant.QUERY_CLOUD_SUCCESSFUL;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (!abort) {
            if (result == RemoteListContant.QUERY_NO_DATA) {// 云没有数据
                queryPlayBackListTaskCallback.queryHasNoData(RemoteListContant.TYPE_SDKCLOUD);
            } else if (result == RemoteListContant.QUERY_CLOUD_SUCCESSFUL) {// 云有数据
                queryPlayBackListTaskCallback.querySuccessFromSDKCloud(cloudPartInfoFileExList, cloudPartFiles);
            }
        }
    }

    private void convertEZCloudRecordFile2CloudPartInfoFile(CloudPartInfoFile dst, EZCloudRecordFile src, int pos) {
    	String startT = new SimpleDateFormat("yyyyMMddHHmmss").format(src.getStartTime().getTime());
		String endT = new SimpleDateFormat("yyyyMMddHHmmss").format(src.getStopTime().getTime());
    	dst.setCloud(true);
    	dst.setDownloadPath(src.getDownloadPath());
    	dst.setEndTime(endT);
    	dst.setFileId(src.getFileId());
    	dst.setKeyCheckSum(src.getEncryption());
    	dst.setPicUrl(src.getCoverPic());
    	dst.setPosition(pos);
    	dst.setStartTime(startT);
    	dst.setDeviceSerial(src.getDeviceSerial());
    	dst.setCameraNo(src.getCameraNo());
    	dst.setVideoType(src.getVideoType());
        dst.setiStorageVersion(src.getiStorageVersion());
        dst.setFileSize(src.getFileSize());
        dst.setSpaceId(src.getSpaceId());
    }

    private int queryCloudFile(){
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		startTime.setTime(searchDate);
		endTime.setTime(searchDate);
       
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);
		endTime.set(Calendar.HOUR_OF_DAY, 23);
		endTime.set(Calendar.MINUTE, 59);
		endTime.set(Calendar.SECOND, 59);

		List<EZCloudRecordFile> tmpList = null;
		try {
			tmpList = EzvizApplication.getOpenSDK().searchRecordFileFromSDKCloud(deviceSerial, channelNo,
					startTime, endTime, SpTool.obtainValue(ValueKeys.SDK_CLOUD_SPACEID));
		} catch (BaseException e) {
			e.printStackTrace();
        }

        cloudPartFiles = new ArrayList<>();
		if (tmpList != null && tmpList.size() > 0) {
			for (int i = 0; i < tmpList.size(); i++) {
				EZCloudRecordFile file = tmpList.get(i);
				CloudPartInfoFile cpif = new CloudPartInfoFile();

				convertEZCloudRecordFile2CloudPartInfoFile(cpif, file, i);
				cloudPartFiles.add(cpif);
			}
		}

		if (CollectionUtil.isNotEmpty(cloudPartFiles)) {
			Collections.sort(cloudPartFiles);
		}

        int length = cloudPartFiles.size();
        int i = 0;
        while (i < length) {
            CloudPartInfoFileEx cloudPartInfoFileEx = new CloudPartInfoFileEx();
            CloudPartInfoFile dataOne = cloudPartFiles.get(i);
            dataOne.setPosition(i);
            Calendar beginCalender = Utils.convert14Calender(dataOne.getStartTime());
            String hour = getHour(beginCalender.get(Calendar.HOUR_OF_DAY));
            cloudPartInfoFileEx.setHeadHour(hour);
            cloudPartInfoFileEx.setDataOne(dataOne);
            i++;
            if (i > length - 1) {
                cloudPartInfoFileExList.add(cloudPartInfoFileEx);
                continue;
            }
            CloudPartInfoFile dataTwo = cloudPartFiles.get(i);
            if (hour.equals(getHour(Utils.convert14Calender(dataTwo.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                dataTwo.setPosition(i);
                cloudPartInfoFileEx.setDataTwo(dataTwo);
                i++;
                if (i > length - 1) {
                    cloudPartInfoFileExList.add(cloudPartInfoFileEx);
                    continue;
                }
                CloudPartInfoFile dataThree = cloudPartFiles.get(i);
                if (hour.equals(getHour(Utils.convert14Calender(dataThree.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                    dataThree.setPosition(i);
                    cloudPartInfoFileEx.setDataThree(dataThree);
                    i++;
                }
            }
            cloudPartInfoFileExList.add(cloudPartInfoFileEx);
        }
        if (CollectionUtil.isNotEmpty(cloudPartInfoFileExList)) {
            return RemoteListContant.QUERY_CLOUD_SUCCESSFUL;
        }
        return RemoteListContant.QUERY_NO_DATA;
    }

    private String getHour(int hourOfDay) {
        return hourOfDay + MINUTE;
    }

}
