/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright null
 * 
 * @FileName TitleMenuItem.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2015-5-12
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.widget;

public class TitleMenuItem {

    public TitleMenuItem() {
    }

    public TitleMenuItem(String type, String callbackId, String icon, String text) {
        super();
        setType(type);
        this.callbackId = callbackId;
        this.icon = icon;
        this.text = text;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private String type;
    public String callbackId;
    public String icon;
    public int iconResId;
    public String text;
}
