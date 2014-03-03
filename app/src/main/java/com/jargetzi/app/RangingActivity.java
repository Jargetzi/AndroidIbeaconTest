package com.jargetzi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RangingActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    public ListView mListView;
    //private List<Map<String, String>> mDevices = new ArrayList<Map<String, String>>();
    //public List<iBeaconInfo> mIBeaconInfo = new ArrayList<iBeaconInfo>();
    public List<iBeaconInfo> mEverySeenBeacon = new ArrayList<iBeaconInfo>();
    //public List<Map<String,String>> mEverySeenMap = new ArrayList<Map<String, String>>();
    public List<String> mEverySeenList = new ArrayList<String>();
    private HashMap<String,String> mActiveBeacons = new HashMap<String, String>();
    private customListAdapter mAdapter;
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
        if(MainActivity.pause == false) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        getSavedDevices();

        iBeaconManager.bind(this);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //@Override
            public void onItemClick(AdapterView <? > arg0, View arg1, int arg2, long arg3) {
                if(arg2!=0){
                    iBeaconInfo selectedBeacon = mEverySeenBeacon.get(arg2-1);  //  subtract 1 to because header is in position 0
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
        //iBeaconManager.unBind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            iBeaconManager.unBind(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"Illegal Arguement Exception in RangingActivity: " + e);
        }
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
                if (iBeacons.size() > 0 && (firstTime || !MainActivity.pause)) {
                    //Log.i(TAG, "The first iBeacon I see is about " + iBeacons.iterator().next().getAccuracy() + " meters away.");

                    //List<iBeaconInfo> devices = new ArrayList<iBeaconInfo>();
                    HashMap<String,String> activeDevices = new HashMap<String, String>();
                    for (IBeacon iBeacon : iBeacons) {
                        //iBeacon.requestData(this);
                        String displayString = iBeacon.getProximityUuid() + " " + iBeacon.getMajor() + " " + iBeacon.getMinor() + "\n";
                        //displayTableRow(iBeacon, displayString, false);

                        String uuid = iBeacon.getProximityUuid();
                        double distance = iBeacon.getAccuracy();
                        int major = iBeacon.getMajor();
                        int minor = iBeacon.getMinor();

                        distance = distance * 3.28084;
                        String distance_string = String.format("%.2f",distance);

                        String hash = hashFunction(uuid.toUpperCase(), major +"", minor + "");

                        iBeaconInfo device = new iBeaconInfo(uuid,distance_string + " feet","major " + major, "minor " + minor);
                        device.setActive();
                        device.setHash(hash);
                        device.setNickname("");


                        // add it to my all added
                        activeDevices.put(hash.substring(0,10),distance_string);
                        //devices.add(device);

                        if(!mEverySeenList.contains(hash.substring(0,10))){
                            // this is a new beacon
                            addBeacon(device);
                        }
                        Log.i(TAG, "displayString: " + displayString);
                        //String hash = hashFunction(uuid.toUpperCase(),major + "",minor + "");
                        //String labelHash = hash.substring(0,10);
                    }
                    mActiveBeacons = activeDevices;
                    //mIBeaconInfo = devices;
                    // here I need to update mEverSeenBeacons with the info that is in mIBeaconInfo
                    updateActive();
                    sortEverySeenBeacon();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (MainActivity.pause == false) {
                                //Log.v(TAG,"Size of mESB: " + mEverySeenBeacon.size());
                                if(mAdapter==null) {
                                    mAdapter = new customListAdapter(RangingActivity.this, R.layout.listview_item_row, mEverySeenBeacon); //  was mIBeaconInfo
                                    if (firstTime) {
                                        View header = (View) getLayoutInflater().inflate(R.layout.listview_header_row, null);
                                        mListView.addHeaderView(header);
                                        firstTime = false;
                                    }
                                    mListView.setAdapter(mAdapter);
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    mAdapter.notifyDataSetChanged();
                                }
                                //customListAdapter adapter1


                            }

                        }
                    });
                }
            }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

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

    //  This is to create a static table that with show every device I have ever seen
    public String readFromFile() {
        String ret="";
        try {
            InputStream inputStream = openFileInput(getString(R.string.all_seen_devices_file));
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
            Log.e(TAG,"File not found " + e.toString());
        } catch(Exception e) {
            Log.e(TAG, "Cannot read file: " + e.toString());
        }
        return ret;
    }

    public void addBeacon(iBeaconInfo beacon) {
        String filename = getString(R.string.all_seen_devices_file);
        JSONObject jsonObject = new JSONObject();
        File dir = getFilesDir();
        File file = new File(dir,filename);
        if(file.exists()) {
            //  Can add to the file
            try {
                jsonObject = new JSONObject(readFromFile());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String hash = beacon.getHash().substring(0,10);
        JSONObject beaconJsonObject = new JSONObject();
        try {
            if(!jsonObject.has(hash)){
                //  Add it if it is new
                beaconJsonObject.put("uuid",beacon.getUuid());
                beaconJsonObject.put("major",beacon.getMajor());
                beaconJsonObject.put("minor",beacon.getMinor());
                beaconJsonObject.put("nickname",beacon.getNickname());
                beaconJsonObject.put("uuid", beacon.getUuid());
                beaconJsonObject.put("distance",beacon.getDistance());
                beaconJsonObject.put("hash",beacon.getHash());
                beaconJsonObject.put("active",beacon.isActive());

                jsonObject.put(hash, beaconJsonObject);

                //  log it in my map
                if(!mEverySeenList.contains(hash)) {
                    mEverySeenList.add(hash);
                    iBeaconInfo temp = new iBeaconInfo(beacon.getUuid(),beacon.getDistance(),beacon.getMajor(),beacon.getMinor());
                    temp.setHash(beacon.getHash());
                    temp.setNickname(beacon.getNickname());
                    temp.setActive();
                    mEverySeenBeacon.add(temp);
                }

                String saveBeaconInfo = jsonObject.toString();
                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);	//was append
                    outputStream.write(saveBeaconInfo.getBytes());
                    Log.v(TAG,"tried to save this: " + saveBeaconInfo);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                }
            } else {
                //  This has already been saved as a beacon we have seen
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getSavedDevices() {
        JSONObject jsonObject = new JSONObject();
        String file = readFromFile();
        Log.v(TAG,"Getting saved devices");

        if(!file.equals("")) {
            try {
                jsonObject = new JSONObject(file);
                //Log.v(TAG,"This was saved: " + jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Iterator<String> iter = jsonObject.keys();
            while( iter.hasNext()) {
                String hash = iter.next();
                try {
                    JSONObject tempObject = (JSONObject) jsonObject.get(hash);
                    String nickname = tempObject.getString("nickname");
                    String uuid = tempObject.getString("uuid");
                    String major = tempObject.getString("major");
                    String minor = tempObject.getString("minor");
                    String full_hash = tempObject.getString("hash");
                    iBeaconInfo tempBeacon = new iBeaconInfo(uuid,getString(R.string.not_in_range_txt),major,minor);
                    tempBeacon.setNickname(nickname);
                    tempBeacon.setHash(full_hash);
                    //  Set saved device in mAllSeenDevice
                    mEverySeenBeacon.add(tempBeacon);
                    mEverySeenList.add(tempObject.getString("hash").substring(0,10));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String hashFunction(String uuid,String major, String minor) {
        String beforeHash = uuid + "-" + major + "-" + minor;
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            byte[] hash = digest.digest(beforeHash.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {

                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void sortEverySeenBeacon() {
        Collections.sort(mEverySeenBeacon, new Comparator<iBeaconInfo>(){
            public int compare(iBeaconInfo o1, iBeaconInfo o2) {
                String major1 = o1.getMajor();
                String minor1 = o1.getMinor();
                String major2 = o2.getMajor();
                String minor2 = o2.getMinor();
                if(major1.equals(major2)) {
                    return minor1.compareTo(minor2);
                } else {
                    return major1.compareTo(major2);
                }
            }
        });
    }

    public void updateActive() {
        if(mEverySeenBeacon.size() < mActiveBeacons.size()) {
            Log.e(TAG,"Should not have more active beacons than I have saved");
        } else {
            for(int i =0; i < mEverySeenBeacon.size(); i++) {
                iBeaconInfo tempBeacon = mEverySeenBeacon.get(i);
                String hash = tempBeacon.getHash().substring(0,10);

                if(mActiveBeacons.containsKey(hash)) {
                    Log.v(TAG,"updating distance for " + hash);
                    mEverySeenBeacon.get(i).setDistance(mActiveBeacons.get(hash));
                } else {
                    mEverySeenBeacon.get(i).setDistance(String.valueOf(R.string.not_in_range_txt));
                }
            }
        }
    }
}