package com.videogo.ui.playback.querylist;

import static com.videogo.EzvizApplication.getOpenSDK;
import static com.videogo.openapi.EZConstants.EZVideoRecordLocation.EZ_VIDEO_RECORD_LOCATION_LOCAL;

import android.app.Activity;

import com.videogo.EzvizApplication;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.openapi.bean.EZDeviceRecordInfo;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.ui.playback.RemoteListContant;
import com.videogo.ui.playback.bean.CloudPartInfoFileEx;
import com.videogo.ui.common.HikAsyncTask;
import com.videogo.util.CollectionUtil;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;

public class QueryDeviceRecordFilesAsyncTask extends HikAsyncTask<String, Void, Integer> {

    private int channelNo;
    private String deviceSerial;
    private EZConstants.EZVideoRecordType recordType;
    private Date queryDate;
    private List<CloudPartInfoFileEx> playBackListLocalItems = new ArrayList<CloudPartInfoFileEx>();
    private List<CloudPartInfoFile> convertCalendarFiles;
    private QueryPlayBackListTaskCallback playBackListTaskCallback = null;
    private volatile boolean abort = false;
    private int cloudTotal;
    private final String MINUTE;

    public QueryDeviceRecordFilesAsyncTask(String deviceSerial, int channelNo, EZConstants.EZVideoRecordType recordType,
                                           QueryPlayBackListTaskCallback playBackListTaskCallback) {
        MINUTE = ((Activity) playBackListTaskCallback).getString(R.string.local_play_hour);
        this.channelNo = channelNo;
        this.deviceSerial = deviceSerial;
        this.recordType = recordType;
        this.playBackListTaskCallback = playBackListTaskCallback;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
    }

    public void setQueryDate(Date queryDate) {
        this.queryDate = queryDate;
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
            if (result == RemoteListContant.QUERY_LOCAL_SUCCESSFUL) {
                playBackListTaskCallback.querySuccessFromDevice(playBackListLocalItems, cloudTotal, convertCalendarFiles);
            } else if (result == RemoteListContant.QUERY_NO_DATA) {
                playBackListTaskCallback.queryHasNoData(RemoteListContant.TYPE_LOCAL);
            }
        }
    }

    private void convertEZDeviceRecordFile2CloudPartInfoFile(CloudPartInfoFile dst, EZDeviceRecordFile src, int pos) {
        dst.setStartTime(DateTimeUtil.calendarToYMDHMSCompactString(src.getStartTime()));
        dst.setEndTime(DateTimeUtil.calendarToYMDHMSCompactString(src.getStopTime()));
        dst.setPosition(pos);
        dst.setBegin(src.getBegin());
        dst.setEnd(src.getEnd());
    }

    public int searchLocalFile() {
        playBackListLocalItems.clear();
        Date beginDate = DateTimeUtil.beginDate(queryDate);
        Date endDate = DateTimeUtil.endDate(queryDate);
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        startTime.setTime(beginDate);
        endTime.setTime(endDate);

        List<EZDeviceRecordFile> tmpList = null;
        try {
            tmpList = EzvizApplication.getOpenSDK().searchRecordFileFromDevice(deviceSerial, channelNo, startTime, endTime, recordType);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = e.getObject();
            LogUtil.d("search file list failed. error ", errorInfo.toString());
        }

        convertCalendarFiles = new ArrayList<>();
        if (tmpList != null && tmpList.size() > 0) {
            for (int i = 0; i < tmpList.size(); i++) {
                EZDeviceRecordFile file = tmpList.get(i);
                CloudPartInfoFile cpif = new CloudPartInfoFile();
                // 云存储和SD卡录像共用组件，所以需要做对象转换
                convertEZDeviceRecordFile2CloudPartInfoFile(cpif, file, i);
                convertCalendarFiles.add(cpif);
            }
        }

        if (CollectionUtil.isNotEmpty(convertCalendarFiles)) {
            Collections.sort(convertCalendarFiles);
            Collections.reverse(convertCalendarFiles);
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
//        if (beginCalender.getTimeInMillis() < beginDate.getTime()) {
//            beginCalender = Calendar.getInstance();
//            beginCalender.setTime(beginDate);
//        }
        Calendar endCalender = Utils.convert14Calender(cloudPartInfoFile.getEndTime());
        if (endCalender.getTimeInMillis() > endDate.getTime()) {
            endCalender = Calendar.getInstance();
            endCalender.setTime(endDate);
        }
        cloudPartInfoFile.setStartTime(DateTimeUtil.calendarToYMDHMSCompactString(beginCalender));
        cloudPartInfoFile.setEndTime(DateTimeUtil.calendarToYMDHMSCompactString(endCalender));
        return cloudPartInfoFile;
    }

    private String getHour(int hourOfDay) {
        return hourOfDay + MINUTE;
    }

}
