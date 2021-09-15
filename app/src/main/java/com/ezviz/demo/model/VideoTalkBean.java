package com.ezviz.demo.model;

public class VideoTalkBean {

    private int clientId;

    private String userName;

    //1表示加入房间，-1表示退出房间，0表示已经加入房间之后的状态
    private int isJoinRoom;

    private int volume;

    public VideoTalkBean(int clientId, String userName){
        this.clientId = clientId;
        this.userName = userName;
    }

//    public VideoTalkBean(int clientId, String userName){
//        this.clientId = clientId;
//        this.userName = userName;
//        this.isHost = isHost;
//    }

    public VideoTalkBean(int clientId, String userName, int volume){
        this.clientId = clientId;
        this.userName = userName;
        this.volume = volume;
    }

    public VideoTalkBean(int clientId, String userName, int isJoinRoom, int volume) {
        this.clientId = clientId;
        this.userName = userName;
        this.isJoinRoom = isJoinRoom;
        this.volume = volume;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

//    public boolean isHost() {
//        return isHost;
//    }

//    public void setHost(boolean host) {
//        isHost = host;
//    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getIsJoinRoom() {
        return isJoinRoom;
    }

    public void setIsJoinRoom(int isJoinRoom) {
        this.isJoinRoom = isJoinRoom;
    }
}
