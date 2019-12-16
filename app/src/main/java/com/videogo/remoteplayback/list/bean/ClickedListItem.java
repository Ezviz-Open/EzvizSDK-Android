/**
 * @ProjectName: null
 * @Copyright: null
 * @address: https://www.ys7.com
 * @date: 2014-6-16 下午4:58:48
 * @Description: null
 */
package com.videogo.remoteplayback.list.bean;

import java.util.Calendar;

public class ClickedListItem {

    private int index;

    private int type;

    private long beginTime;

    private long endTime;

    private Calendar uiPlayTimeOnStop = null;

    private int position;
    
    private int fileSize;

    public int getIndex() {
        return index;
    }

    public ClickedListItem(int index, int type, long beginTime, long endTime, int position) {
        super();
        this.index = index;
        this.type = type;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.position = position;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Calendar getUiPlayTimeOnStop() {
        return uiPlayTimeOnStop;
    }

    public void setUiPlayTimeOnStop(Calendar uiPlayTimeOnStop) {
        this.uiPlayTimeOnStop = uiPlayTimeOnStop;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "[index=" + index + ", type=" + type + "]";
    }

}
