package com.jargetzi.app;

/**
 * Created by michaellee on 2/10/14.
 */
public class iBeaconInfo {
    public String uuid;
    public String distance;
    public String major;
    public String minor;

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
}