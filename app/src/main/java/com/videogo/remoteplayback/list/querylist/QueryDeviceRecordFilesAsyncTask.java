package com.videogo.remoteplayback.list.querylist;

import android.app.Activity;

import com.videogo.EzvizApplication;
import com.videogo.device.DeviceReportInfo;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.remoteplayback.list.RemoteListContant;
import com.videogo.remoteplayback.list.bean.CloudPartInfoFileEx;
import com.videogo.ui.common.HikAsyncTask;
import com.videogo.util.CollectionUtil;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdkcommon.R;

public class QueryDeviceRecordFilesAsyncTask extends HikAsyncTask<String, Void, Integer> {

    private int channelNo;
    private String deviceSerial;
    private Date queryDate;
    private List<CloudPartInfoFileEx> playBackListLocalItems = new ArrayList<CloudPartInfoFileEx>();
    private List<CloudPartInfoFile> convertCalendarFiles;
    private QueryPlayBackListTaskCallback playBackListTaskCallback = null;
    private volatile boolean abort = false;
    private boolean onlyHasLocal = false;
    private int localErrorCode = 0;
    private int queryMode = DeviceReportInfo.REPOERT_QUERY_RELATE_TF;
    private String queryDetail = "";
    private int cloudTotal;
    private final String MINUTE;

    public QueryDeviceRecordFilesAsyncTask(String deviceSerial, int channelNo,
                                           QueryPlayBackListTaskCallback playBackListTaskCallback) {
        MINUTE = ((Activity) playBackListTaskCallback).getString(R.string.local_play_hour);
        this.channelNo = channelNo;
        this.deviceSerial = deviceSerial;
        this.playBackListTaskCallback = playBackListTaskCallback;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
    }

    public void setQueryDate(Date queryDate) {
        this.queryDate = queryDate;
    }

    public void setOnlyHasLocal(boolean onlyHasLocal) {
        this.onlyHasLocal = onlyHasLocal;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Integer doInBackground(String... params) {
        cloudTotal = Integer.parseInt(params[0]);
        return searchLocalFile();
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (!abort) {
            playBackListTaskCallback
                    .queryTaskOver(RemoteListContant.TYPE_LOCAL, queryMode, localErrorCode, queryDetail);
            if (result == RemoteListContant.QUERY_LOCAL_SUCCESSFUL) {
                playBackListTaskCallback.querySuccessFromDevice(playBackListLocalItems, cloudTotal, convertCalendarFiles);
            } else if (result == RemoteListContant.QUERY_NO_DATA) {
                if (onlyHasLocal) {
                    playBackListTaskCallback.queryOnlyLocalNoData();
                } else {
                    playBackListTaskCallback.queryLocalNoData();
                }
            } else if (result == RemoteListContant.QUERY_EXCEPTION) {
                if (onlyHasLocal) {
                    playBackListTaskCallback.queryException();
                } else {
                    playBackListTaskCallback.queryLocalException();
                }
            }
        }
    }

    private String calendar2String(Calendar cal) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(cal.getTime());
        
        return dateStr;
    }
    private void convertEZDeviceRecordFile2CloudPartInfoFile(CloudPartInfoFile dst, EZDeviceRecordFile src, int pos) {
    	dst.setStartTime(calendar2String(src.getStartTime()));
    	dst.setEndTime(calendar2String(src.getStopTime()));
    	dst.setPosition(pos);
    }

    public int searchLocalFile() {
        playBackListLocalItems.clear();
        Date beginDate = DateTimeUtil.beginDate(queryDate);
        Date endDate = DateTimeUtil.endDate(queryDate);
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		startTime.setTime(queryDate);
		endTime.setTime(queryDate);
        
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);
		endTime.set(Calendar.HOUR_OF_DAY, 23);
		endTime.set(Calendar.MINUTE, 59);
		endTime.set(Calendar.SECOND, 59);

		List<EZDeviceRecordFile> tmpList = null;
		try {
			tmpList = EzvizApplication.getOpenSDK().searchRecordFileFromDevice(deviceSerial,channelNo,
					startTime, endTime);
		} catch (BaseException e) {
			e.printStackTrace();

            ErrorInfo errorInfo = e.getObject();
            LogUtil.debugLog("search file list failed. error ", errorInfo.toString());
		}

		convertCalendarFiles = new ArrayList<>();
		if (tmpList != null && tmpList.size() > 0) {
			for (int i = 0; i < tmpList.size(); i++) {
				EZDeviceRecordFile file = tmpList.get(i);
				CloudPartInfoFile cpif = new CloudPartInfoFile();

				convertEZDeviceRecordFile2CloudPartInfoFile(cpif, file, i);
				convertCalendarFiles.add(cpif);
			}
		}

        if (CollectionUtil.isNotEmpty(convertCalendarFiles)) {
            Collections.sort(convertCalendarFiles);
        }
        int length = convertCalendarFiles.size();
        int i = 0;
        while (i < length) {
            CloudPartInfoFileEx cloudPartInfoFileEx = new CloudPartInfoFileEx();
            CloudPartInfoFile dataOne = getCloudPartInfoFile(convertCalendarFiles.get(i), beginDate, endDate);
            dataOne.setPosition(cloudTotal + i);
            Calendar beginCalender = Utils.convert14Calender(dataOne.getStartTime());

            String hour = getHour(beginCalender.get(Calendar.HOUR_OF_DAY));
            cloudPartInfoFileEx.setHeadHour(hour);
            cloudPartInfoFileEx.setDataOne(dataOne);
            i++;
            if (i > length - 1) {
                playBackListLocalItems.add(cloudPartInfoFileEx);
                continue;
            }
            CloudPartInfoFile dataTwo = getCloudPartInfoFile(convertCalendarFiles.get(i), beginDate, endDate);
            if (hour.equals(getHour(Utils.convert14Calender(dataTwo.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                dataTwo.setPosition(cloudTotal + i);
                cloudPartInfoFileEx.setDataTwo(dataTwo);
                i++;
                if (i > length - 1) {
                    playBackListLocalItems.add(cloudPartInfoFileEx);
                    continue;
                }
                CloudPartInfoFile dataThree = getCloudPartInfoFile(convertCalendarFiles.get(i), beginDate, endDate);
                if (hour.equals(getHour(Utils.convert14Calender(dataThree.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                    dataThree.setPosition(cloudTotal + i);
                    cloudPartInfoFileEx.setDataThree(dataThree);
                    i++;
                }
            }
            playBackListLocalItems.add(cloudPartInfoFileEx);
        }

        if (CollectionUtil.isNotEmpty(playBackListLocalItems)) {
            return RemoteListContant.QUERY_LOCAL_SUCCESSFUL;
        }
        return RemoteListContant.QUERY_NO_DATA;

    }

    private CloudPartInfoFile getCloudPartInfoFile(CloudPartInfoFile cloudPartInfoFile, Date beginDate, Date endDate) {
        Calendar beginCalender = Utils.convert14Calender(cloudPartInfoFile.getStartTime());
        if (beginCalender.getTimeInMillis() < beginDate.getTime()) {
            beginCalender = Calendar.getInstance();
            beginCalender.setTime(beginDate);
        }
        Calendar endCalender = Utils.convert14Calender(cloudPartInfoFile.getEndTime());
        if (endCalender.getTimeInMillis() > endDate.getTime()) {
            endCalender = Calendar.getInstance();
            endCalender.setTime(endDate);
        }
        cloudPartInfoFile.setStartTime(new SimpleDateFormat("yyyyMMddHHmmss").format(beginCalender.getTime()));
        cloudPartInfoFile.setEndTime(new SimpleDateFormat("yyyyMMddHHmmss").format(endCalender.getTime()));
        return cloudPartInfoFile;
    }

    private String getHour(int hourOfDay) {
        return hourOfDay + MINUTE;
    }

}
