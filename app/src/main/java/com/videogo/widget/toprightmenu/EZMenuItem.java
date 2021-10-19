package com.videogo.widget.toprightmenu;


public class EZMenuItem {
    private String id;
    private int icon;
    private String text;

    public EZMenuItem() {
    }

    public EZMenuItem(String text) {
        this.text = text;
    }

    public EZMenuItem(int iconId, String text) {
        this.icon = iconId;
        this.text = text;
    }

    public EZMenuItem(String id, int iconId, String text) {
        this.id = id;
        this.icon = iconId;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;

    }

    public void setIcon(int iconId) {
        this.icon = iconId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
