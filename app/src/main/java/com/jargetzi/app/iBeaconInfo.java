package com.jargetzi.app;

/**
 * Created by michaellee on 2/10/14.
 */
public class iBeaconInfo {
    public String uuid;
    public String distance;
    public String major;
    public String minor;
    public String nickname;
    public String hash;


    public iBeaconInfo() {
        super();
    }

    public iBeaconInfo(String uu_id, String dist, String maj,String min) {
        super();
        this.uuid = uu_id;
        this.distance = dist;
        this.major = maj;
        this.minor = min;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDistance() {
        return distance;
    }

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHash() {
        return hash;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}