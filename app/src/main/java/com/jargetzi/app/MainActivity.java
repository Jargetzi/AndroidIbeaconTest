package com.jargetzi.app;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeaconManager;

import java.io.File;

public class MainActivity extends TabActivity {
    protected static final String TAG = "RangingActivity";
    public static boolean pause;
    /*
    private Button mMonitorButton;
    private Button mRangingButton;
    private Button mMarkedButton;
    public List<iBeaconInfo> mIBeaconInfo = new ArrayList<iBeaconInfo>();
    private ListView mListView;
    private boolean firstTime;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        verifyBluetooth();

        pause = false;
        //  This is for the navigation tabs

        TabHost tabHost = getTabHost();

        //  Tab for RangingMarkedActivity
        TabHost.TabSpec rangingMarked = tabHost.newTabSpec("My iBeacons");
        rangingMarked.setIndicator("My iBeacons");
        Intent rangingMarkedIntent = new Intent(this,RangingMarkedActivity.class);
        rangingMarked.setContent(rangingMarkedIntent);

        //  Tab for RangingActivity
        TabHost.TabSpec ranging = tabHost.newTabSpec("All iBeacons");
        ranging.setIndicator("All iBeacons");
        Intent rangingIntent = new Intent(this,RangingActivity.class);
        ranging.setContent(rangingIntent);

        //  Tab for SettingsActivity
        TabHost.TabSpec settings = tabHost.newTabSpec("Settings");
        settings.setIndicator("Settings");
        Intent settingsIntent = new Intent(this,SettingsActivity.class);
        settings.setContent(settingsIntent);

        //  Add all TabSpec to TabHost
        tabHost.addTab(rangingMarked);
        tabHost.addTab(ranging);
        tabHost.addTab(settings);

    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setMarkedDevices();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_pause) {
            pause = ! pause;
            Toast.makeText(getBaseContext(), "pause: " + pause, Toast.LENGTH_SHORT).show();
            return true;
        } else if(id == R.id.action_delete_allseen) {
            String file = getString(R.string.all_seen_devices_file);
            deleteAFile(file);
            Toast.makeText(getBaseContext(),"Deleting all seen file",Toast.LENGTH_SHORT).show();
        } else if(id == R.id.action_test_call_rm) {
            Intent intent = new Intent(this,RangingMarkedActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteAFile(String fileName) {
        String filename = fileName;
        File dir = getFilesDir();
        File file = new File(dir,filename);
        file.delete();
    }

    public boolean getPause() {
        return pause;
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
                        //finish();
                        //System.exit(0);
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
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();
        }
    }
}


