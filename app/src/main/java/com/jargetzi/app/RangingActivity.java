package com.jargetzi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private boolean firstTime;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        //verifyBluetooth();

        firstTime = true;

        mListView = (ListView)findViewById(android.R.id.list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        //  This is how long a scan will be, 1.1 seconds for back and fore ground
        //iBeaconManager.setBackgroundScanPeriod(1100l);
        iBeaconManager.setForegroundScanPeriod(1100l);


        //  this is time between each scan background, 15 mins, foreground 1 second
        //iBeaconManager.setBackgroundBetweenScanPeriod(900000l);
        iBeaconManager.setForegroundBetweenScanPeriod(1100l);

        iBeaconManager.bind(this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //@Override
            public void onItemClick(AdapterView <? > arg0, View arg1, int arg2, long arg3) {
                if(arg2!=0){
                    iBeaconInfo selectedBeacon = mIBeaconInfo.get(arg2-1);  //  subtract 1 to because header is in position 0
                    String uuid = selectedBeacon.getUuid();
                    String major = selectedBeacon.getMajor();
                    String minor = selectedBeacon.getMinor();
                    //Toast.makeText(getBaseContext(),"clicked "+arg2,Toast.LENGTH_SHORT ).show();
                    RegisterDeviceCall(uuid,major,minor);
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        iBeaconManager.unBind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        iBeaconManager.bind(this);
    }

    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    //Log.i(TAG, "The first iBeacon I see is about " + iBeacons.iterator().next().getAccuracy() + " meters away.");

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
                            if(firstTime) {
                                View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row,null);
                                mListView.addHeaderView(header);
                                firstTime = false;
                            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Toast.makeText(getBaseContext(),"Search",Toast.LENGTH_SHORT).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ranging, menu);
        return true;
    }

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


    public void RegisterDeviceCall (String uuid, String major, String minor) {
        major = major.substring(6);
        minor = minor.substring(6);
        uuid = uuid.toUpperCase();
        Intent intent = new Intent(this,RegisterDevice.class);
        intent.putExtra("uuid",uuid);
        intent.putExtra("major",major);
        intent.putExtra("minor",minor);
        startActivity(intent);
    }
    /*
    public void filterRanging() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter iBeacons to be monitored");
        builder.setMessage("Limit the uuid, minor, or major to limit what devices are being monitored");
        final EditText input = new EditText(this);

        input.setId(432);
        builder.setView(input);
        final Editable[] out = new Editable[1];
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
               input.setInputType(InputType.TYPE_CLASS_TEXT);
               //EditText input = (EditText) findViewById(432);

               out[0] = input.getText();
               //Editable value = input.getText();
               //out[0] = value.toString();
               //Toast.makeText(getBaseContext(),input.getText(),Toast.LENGTH_SHORT).show();
               Log.v(TAG,"builder clicked " + out[0].toString());
           }
        });


        builder.show();
    }
    */


}