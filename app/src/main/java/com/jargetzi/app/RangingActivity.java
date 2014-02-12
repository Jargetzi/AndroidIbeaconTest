package com.jargetzi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RangingActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    public ListView mListView;
    //private List<Map<String, String>> mDevices = new ArrayList<Map<String, String>>();
    public List<iBeaconInfo> mIBeaconInfo = new ArrayList<iBeaconInfo>();
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        verifyBluetooth();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);


        iBeaconManager.bind(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    Log.i(TAG, "The first iBeacon I see is about " + iBeacons.iterator().next().getAccuracy() + " meters away.");

                    List<iBeaconInfo> devices = new ArrayList<iBeaconInfo>();
                    for (IBeacon iBeacon : iBeacons) {
                        //iBeacon.requestData(this);
                        String displayString = iBeacon.getProximityUuid() + " " + iBeacon.getMajor() + " " + iBeacon.getMinor() + "\n";
                        //displayTableRow(iBeacon, displayString, false);
                        String uuid = iBeacon.getProximityUuid();
                        double distance = iBeacon.getAccuracy();
                        int major = iBeacon.getMajor();
                        int minor = iBeacon.getMinor();


                        iBeaconInfo device = new iBeaconInfo(uuid,distance + " meters","major " + major, "minor " + minor);
                        devices.add(device);

                        Log.i(TAG, "displayString: " + displayString);
                    }
                    mIBeaconInfo = devices;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            customListAdapter adapter1 = new customListAdapter(RangingActivity.this,R.layout.listview_item_row, mIBeaconInfo);
                            mListView = (ListView)findViewById(android.R.id.list);

                            mListView.setAdapter(adapter1);
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    /*public void testPrint() {
        List<iBeaconInfo> devices = new ArrayList<iBeaconInfo>();
        for(int i = 0;i<3;i++){
            iBeaconInfo device = new iBeaconInfo("uuid" + i,"distance","major","minor");
            devices.add(device);
        }
        mIBeaconInfo = devices;
        customListAdapter adapter1 = new customListAdapter(this,R.layout.listview_item_row, devices);
        mListView = (ListView)findViewById(android.R.id.list);
        mListView.setAdapter(adapter1);
    }*/

    private void verifyBluetooth() {

        try {
            if (!IBeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

    }
}