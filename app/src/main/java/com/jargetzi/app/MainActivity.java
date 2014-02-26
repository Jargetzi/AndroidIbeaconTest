package com.jargetzi.app;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends TabActivity {
    protected static final String TAG = "RangingActivity";
    private Button mMonitorButton;
    private Button mRangingButton;
    private Button mMarkedButton;
    public List<iBeaconInfo> mIBeaconInfo = new ArrayList<iBeaconInfo>();
    private ListView mListView;
    private boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        mListView = (ListView) findViewById(R.id.main_list_view);
        firstTime = true;


        mMonitorButton = (Button) findViewById(R.id.button_monitor);
        mMonitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "monitoring", Toast.LENGTH_SHORT).show();
                //CallMonitor();
                deleteFile();
            }
        });

        mRangingButton = (Button) findViewById(R.id.button_ranging);
        mRangingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "ranging", Toast.LENGTH_SHORT).show();
                CallRanging();
            }
        });

        mMarkedButton = (Button)findViewById(R.id.button_marked);
        mMarkedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  call RangingMarkedActivity
                CallMarked();
            }

        });

        setMarkedDevices();
        */

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

        //  Add all TabSpec to TabHost
        tabHost.addTab(rangingMarked);
        tabHost.addTab(ranging);


        /*
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
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

    private void CallMonitor() {
        Intent intent = new Intent(this, MonitoringActivity.class);
        startActivity(intent);
    }

    private void CallRanging() {
        Intent intent = new Intent(this, RangingActivity.class);
        startActivity(intent);
    }

    private void CallMarked() {
        Intent intent = new Intent(this, RangingMarkedActivity.class);
        startActivity(intent);
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void setMarkedDevices() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(readFromFile());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<iBeaconInfo> devices = new ArrayList<iBeaconInfo>();
        Iterator<String> iter = jsonObject.keys();
        while( iter.hasNext()) {
            String hash = iter.next();
            iBeaconInfo tempInfo = new iBeaconInfo();
            try {
                JSONObject tempObject = (JSONObject) jsonObject.get(hash);
                tempInfo.setHash(hash);
                tempInfo.setNickname(tempObject.getString("nickname"));
                tempInfo.setDistance("? Meters");
                devices.add(tempInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mIBeaconInfo = devices;
        //  new adapter
        customMarkedListAdapter adapter2 = new customMarkedListAdapter(MainActivity.this,R.layout.listview_item_row_marked,mIBeaconInfo);
        if(firstTime) {
            View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row,null);
            mListView.addHeaderView(header);
            firstTime = false;
        }
        mListView.setAdapter(adapter2);
    }


    public String readFromFile() {
        String ret="";
        try {
            InputStream inputStream = openFileInput(getString(R.string.my_devices_file));
            if( inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found " + e.toString());
        } catch(Exception e) {
            Log.e(TAG, "Cannot read file: " + e.toString());
        }
        return ret;
    }

    public void deleteFile() {
        String filename = getString(R.string.my_devices_file);
        File dir = getFilesDir();
        File file = new File(dir,filename);
        file.delete();
    }
}


