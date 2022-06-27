package com.zego.express;


import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;

public class ZegoParticipant {

    public String userID;
    public String name;
    public String streamID;
    public boolean camera;
    public boolean mic;
    public ZegoStreamQualityLevel network;

    public ZegoParticipant(String userID, String userName) {
        this.userID = userID;
        this.name = userName;
    }

    public ZegoParticipant(String userID) {
        this.userID = userID;
    }

}
