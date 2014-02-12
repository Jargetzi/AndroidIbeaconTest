package com.jargetzi.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

public class MonitoringActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        iBeaconManager.bind(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an iBeacon for the first time!");
                Log.i(TAG,"This is region info: " + region.getUniqueId() +region.getMajor() + region.getMinor());
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing iBeacons: "+state);
                if(state == 0) {
                    //  Then I do not see any iBeacons
                    Toast.makeText(getBaseContext(),"No iBeacons detected",Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "There are not any iBeacons being detected.");
                } else if(state ==1) {
                    //  I do see iBeacons
                    ToastState("iBeacons detected");
                    Log.i(TAG, "There are iBeacons being detected.");
                } else {
                    //  Not sure if there are other states, so if so log it
                    Log.e(TAG, "New state in didDetermineStateForRegion: " + state);
                }
            }
        });

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    public void ToastState(String s) {
        Toast.makeText(getBaseContext(),s,Toast.LENGTH_SHORT).show();
    }

}