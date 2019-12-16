package com.videogo.remoteplayback.list.bean;

import com.videogo.openapi.bean.resp.CloudPartInfoFile;

public class CloudPartInfoFileEx {

    private CloudPartInfoFile dataOne;

    private CloudPartInfoFile dataTwo;

    private CloudPartInfoFile dataThree;

    private String headHour;
    // 是否存在本地数据文件
    private boolean isMore;

    public CloudPartInfoFile getDataOne() {
        return dataOne;
    }

    public void setDataOne(CloudPartInfoFile dataOne) {
        this.dataOne = dataOne;
    }

    public CloudPartInfoFile getDataTwo() {
        return dataTwo;
    }

    public void setDataTwo(CloudPartInfoFile dataTwo) {
        this.dataTwo = dataTwo;
    }

    public CloudPartInfoFile getDataThree() {
        return dataThree;
    }

    public void setDataThree(CloudPartInfoFile dataThree) {
        this.dataThree = dataThree;
    }

    public String getHeadHour() {
        return headHour;
    }

    public void setHeadHour(String headHour) {
        this.headHour = headHour;
    }

    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean isMore) {
        this.isMore = isMore;
    }

}
