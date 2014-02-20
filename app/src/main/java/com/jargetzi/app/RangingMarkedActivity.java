package com.jargetzi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RangingMarkedActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    public ListView mListView;
    //private List<Map<String, String>> mDevices = new ArrayList<Map<String, String>>();
    public List<iBeaconInfo> mIBeaconInfo = new ArrayList<iBeaconInfo>();
    //private List<iBeaconInfo> mSavedDevices = new ArrayList<iBeaconInfo>();
    private List<HashMap<String,iBeaconInfo>> mHashToBeacon = new ArrayList<HashMap<String, iBeaconInfo>>();
    private HashMap<String,String> mHashNicknames = new HashMap<String, String>();
    private List<String> mActiveBeacons = new ArrayList<String>();
    private ProgressBar mProgressBar;
    private boolean firstTime;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging_marked);
        verifyBluetooth();

        firstTime = true;

        mListView = (ListView)findViewById(R.id.marked_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_marked);
        mProgressBar.setVisibility(View.VISIBLE);

        getSavedDevices();

        //  This is how long a scan will be, 1.1 seconds for back and fore ground
        //iBeaconManager.setBackgroundScanPeriod(1100l);
        //iBeaconManager.setForegroundScanPeriod(1100l);


        //  this is time between each scan background, 15 mins, foreground 1 second
        //iBeaconManager.setBackgroundBetweenScanPeriod(900000l);
        //iBeaconManager.setForegroundBetweenScanPeriod(1000l);

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
                    //RegisterDeviceCall(uuid,major,minor);
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
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    //Log.i(TAG, "The first iBeacon I see is about " + iBeacons.iterator().next().getAccuracy() + " meters away.");

                    List<iBeaconInfo> devices = new ArrayList<iBeaconInfo>();
                    mHashToBeacon.clear();

                    for (IBeacon iBeacon : iBeacons) {

                        //String displayString = iBeacon.getProximityUuid() + " " + iBeacon.getMajor() + " " + iBeacon.getMinor() + "\n";


                        String uuid = iBeacon.getProximityUuid();
                        double distance = iBeacon.getAccuracy();
                        int major = iBeacon.getMajor();
                        int minor = iBeacon.getMinor();

                        String hash = hashFunction(uuid.toUpperCase(),major + "",minor + "");
                        hash = hash.substring(0,10);
                        //Log.i(TAG,"this is the hash: " + hash);
                        if(mHashNicknames.containsKey(hash)) {
                            //  If saved file has this device, add it mIBeaconInfo to be put in the adapter
                            iBeaconInfo device = new iBeaconInfo(uuid,distance + " meters","major " + major, "minor " + minor);
                            device.setNickname(mHashNicknames.get(hash));
                            device.setHash(hash);
                            devices.add(device);

                            //  Keep track of the active beacons
                            if(!mHashToBeacon.contains(hash)) {
                                HashMap<String,iBeaconInfo> tempMap = new HashMap<String, iBeaconInfo>();
                                //Log.v(TAG,"This is hash and nickname" + hash + device.getNickname());
                                tempMap.put(hash,device);
                                mHashToBeacon.add(tempMap);
                            }
                        }

                    }

                    mIBeaconInfo = devices;

                    //Log.v(TAG,mIBeaconInfo.toString());
                    //  If mActiveBeacons is not the same size as mHashNicknames, then show that it hasn't been reached
                    if(mIBeaconInfo.size() != mHashNicknames.size()) {
                        //Log.v(TAG,"in here");
                        //Log.v(TAG,"Size of mHashNicknames: " + mHashNicknames.size());
                        HashMap<String,String> nonActiveSavedDevices;

                        nonActiveSavedDevices = new HashMap<String, String>(mHashNicknames);
                        //  Loop through to get a hashmap of devices that are saved but we didn't get info for
                        for(int i = 0; i< mIBeaconInfo.size(); i++) {
                            iBeaconInfo temp = mIBeaconInfo.get(i);
                            nonActiveSavedDevices.remove(temp.getHash());
                            //Log.v(TAG,"This is being removed: " + temp.getNickname());
                        }

                        //  Add the nonActiveSavedDevices to mIBeaconInfo
                        if(nonActiveSavedDevices.size()>0) {
                            Iterator iter = nonActiveSavedDevices.entrySet().iterator();
                            while(iter.hasNext()) {
                                Map.Entry pairs = (Map.Entry)iter.next();

                                iBeaconInfo device = new iBeaconInfo();
                                device.setNickname(pairs.getValue().toString());
                                device.setHash(pairs.getKey().toString());
                                device.setDistance(getString(R.string.not_in_range_txt));
                                mIBeaconInfo.add(device);
                                //Log.v(TAG,"Adding this to mIBeaconInfo: " + device.getNickname());
                            }
                        }

                        //Log.v(TAG,"Size of mHashNicknames: " + mHashNicknames.size());
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*
                            customListAdapter adapter1 = new customListAdapter(RangingMarkedActivity.this,R.layout.listview_item_row, mIBeaconInfo);
                            if(firstTime) {
                                View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row,null);
                                mListView.addHeaderView(header);
                                firstTime = false;
                            }
                            mListView.setAdapter(adapter1);
                            */
                            customMarkedListAdapter adapter2 = new customMarkedListAdapter(RangingMarkedActivity.this,R.layout.listview_item_row_marked,mIBeaconInfo);
                            if(firstTime) {
                                View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row,null);
                                mListView.addHeaderView(header);
                                firstTime = false;
                            }
                            mListView.setAdapter(adapter2);

                            mProgressBar.setVisibility(View.INVISIBLE);


                        }
                    });
                }
            }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myMarkedRangingUniqueId", null, null, null));
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

    /*
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
    */


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

    private void getSavedDevices() {
        JSONObject jsonObject = new JSONObject();
        String file = readFromFile();
        if(file != null) {
            try {
                jsonObject = new JSONObject(file);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            List<iBeaconInfo> devices = new ArrayList<iBeaconInfo>();
            Iterator<String> iter = jsonObject.keys();
            while( iter.hasNext()) {

                String hash = iter.next();

                try {
                    JSONObject tempObject = (JSONObject) jsonObject.get(hash);

                    String nickname = tempObject.getString("nickname");
                    //HashMap<String,String> tempMap = new HashMap<String, String>();
                    //tempMap.put(hash,nickname);
                    mHashNicknames.put(hash,nickname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Log.v(TAG,mHashNicknames.toString());
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
}