package com.jargetzi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
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
import java.util.Map;

public class RangingMarkedActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    public ListView mListView;

    public List<iBeaconInfo> mIBeaconInfo = new ArrayList<iBeaconInfo>();

    private HashMap<String,String> mHashNicknames = new HashMap<String, String>();
    //private List<String> mActiveBeacons = new ArrayList<String>();
    private ProgressBar mProgressBar;
    private boolean firstTime;
    private customMarkedListAdapter mAdapter;
    protected MediaPlayer _mediaPlayer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging_marked);
        //verifyBluetooth();

        firstTime = true;

        mListView = (ListView)findViewById(R.id.marked_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_marked);
        //mProgressBar.setVisibility(View.INVISIBLE);
        if(!IBeaconManager.getInstanceForApplication(this).checkAvailability()){
            // Bluetooth not enabled
            mProgressBar.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        getSavedDevices();

        //  This is how long a scan will be, 1.1 seconds for back and fore ground
        //iBeaconManager.setBackgroundScanPeriod(1100l);
        //iBeaconManager.setForegroundScanPeriod(1100l);
        //iBeaconManager.setForegroundScanPeriod(2000l);
        iBeaconManager.setBackgroundBetweenScanPeriod(2000l);
        iBeaconManager.setForegroundBetweenScanPeriod(2000l);
        //if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, false);



        //  this is time between each scan background, 15 mins, foreground 1 second
        //iBeaconManager.setBackgroundBetweenScanPeriod(900000l);
        //iBeaconManager.setForegroundBetweenScanPeriod(1000l);

        //iBeaconManager.bind(this);
        if (!IBeaconManager.getInstanceForApplication(this).checkAvailability()) {
            setMarkedDevices();
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            iBeaconManager.bind(this);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //@Override
            public void onItemClick(AdapterView <? > arg0, View arg1, int arg2, long arg3) {
                if(arg2!=0){
                    iBeaconInfo selectedBeacon = mIBeaconInfo.get(arg2-1);  //  subtract 1 to because header is in position 0
                    String uuid = selectedBeacon.getUuid();
                    String major = selectedBeacon.getMajor();
                    String minor = selectedBeacon.getMinor();
                    confirmDelete(selectedBeacon.getNickname(),selectedBeacon.getHash());
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
        //if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, true);
        try {
            iBeaconManager.unBind(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"This was caught " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        iBeaconManager.bind(this);
        //if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(getBaseContext(),"was stopped",Toast.LENGTH_SHORT).show();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, true);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(getBaseContext(),"restarted",Toast.LENGTH_SHORT).show();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, false);
    }

    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    //Log.i(TAG, "The first iBeacon I see is about " + iBeacons.iterator().next().getAccuracy() + " meters away.");

                    final List<iBeaconInfo> devices = new ArrayList<iBeaconInfo>();
                    //mHashToBeacon.clear();
                    /*try {
                        playSound(getBaseContext());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    playFromResource(R.raw.sound);
                    for (IBeacon iBeacon : iBeacons) {

                        //String displayString = iBeacon.getProximityUuid() + " " + iBeacon.getMajor() + " " + iBeacon.getMinor() + "\n";

                        String uuid = iBeacon.getProximityUuid();
                        double distance = iBeacon.getAccuracy();
                        int major = iBeacon.getMajor();
                        int minor = iBeacon.getMinor();

                        distance = distance * 3.28084;
                        String distance_string = String.format("%.2f",distance);

                        String hash = hashFunction(uuid.toUpperCase(),major + "",minor + "");
                        hash = hash.substring(0,10);
                        if(mHashNicknames.containsKey(hash)) {
                            //  If saved file has this device, add it mIBeaconInfo to be put in the adapter
                            iBeaconInfo device = new iBeaconInfo(uuid,distance_string + " feet","major " + major, "minor " + minor);
                            device.setNickname(mHashNicknames.get(hash));
                            device.setHash(hash);
                            devices.add(device);

                            Log.v(TAG,uuid + " distance " + distance_string);

                            //  Keep track of the active beacons
                            /*if(!mHashToBeacon.contains(hash)) {
                                HashMap<String,iBeaconInfo> tempMap = new HashMap<String, iBeaconInfo>();
                                tempMap.put(hash,device);
                                mHashToBeacon.add(tempMap);
                            }*/
                        }
                    }
                    mIBeaconInfo = devices;

                    //  If mActiveBeacons is not the same size as mHashNicknames, then show that it hasn't been reached
                    if(mIBeaconInfo.size() != mHashNicknames.size()) {
                        /*Log.v(TAG,"didn't get a signal from a saved beacon");
                        HashMap<String,String> nonActiveSavedDevices = new HashMap<String, String>(mHashNicknames);
                        //  Loop through to get a hashmap of devices that are saved but we didn't get info for
                        for(int i = 0; i< mIBeaconInfo.size(); i++) {
                            iBeaconInfo temp = mIBeaconInfo.get(i);
                            nonActiveSavedDevices.remove(temp.getHash());
                            Log.v(TAG,"This is being removed: " + temp.getNickname());
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
                                Log.v(TAG,"not active beacon: " + device.getNickname());
                            }
                        }*/
                        setSavedBeaconsNotPinged();
                    }
                    sortSavedBeacon();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mAdapter==null){
                                mAdapter = new customMarkedListAdapter(RangingMarkedActivity.this,R.layout.listview_item_row_marked,mIBeaconInfo);
                                if(firstTime) {
                                    View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row,null);
                                    mListView.addHeaderView(header);
                                    firstTime = false;
                                }
                                mListView.setAdapter(mAdapter);
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Log.v(TAG,"first");
                            } else {
                                // update adapter
                                Log.v(TAG,"update");
                                //mAdapter = new customMarkedListAdapter(RangingMarkedActivity.this,R.layout.listview_item_row_marked,mIBeaconInfo);
                                //mAdapter.notifyDataSetChanged();
                                mAdapter.refresh(mIBeaconInfo);
                            }

                        }
                    });
                } else {
                    //  When bluetooth is enabled but there aren't any beacons to detect


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(),"No beacons sensed",Toast.LENGTH_SHORT).show();
                            if(firstTime) {
                                setMarkedDevices();
                                mProgressBar.setVisibility(View.INVISIBLE);
                                alertNoBeaconsSensed();
                            }
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

    /*private void verifyBluetooth() {

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

    }*/

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
            //List<iBeaconInfo> devices = new ArrayList<iBeaconInfo>();
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

    private void deleteItem(String hash) {
        String filename = getString(R.string.my_devices_file);
        JSONObject jsonObject = new JSONObject();
        File dir = getFilesDir();
        File file = new File(dir,filename);
        if(file.exists()) {
            try {
                jsonObject = new JSONObject(readFromFile());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (jsonObject.has(hash)) {
                //	this device is already saved
                jsonObject.remove(hash);
                String saveBeaconInfo = jsonObject.toString();
                //Log.v(TAG,"This is saved "+ saveBeaconInfo);
                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);	//was append
                    outputStream.write(saveBeaconInfo.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void confirmDelete (String nickname, final String hash) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete " + nickname +"?");
        builder.setMessage("Delete " +nickname + " from saved devices?");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getBaseContext(),"delete",Toast.LENGTH_SHORT).show();
                deleteItem(hash);
                //  After Save go to main page
                Intent intent = new Intent(RangingMarkedActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getBaseContext(),"cancel",Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();

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
                //tempInfo.setDistance("? Meters");
                tempInfo.setDistance(getString(R.string.not_in_range_txt));
                devices.add(tempInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mIBeaconInfo = devices;
        //  new adapter
        mAdapter = new customMarkedListAdapter(RangingMarkedActivity.this,R.layout.listview_item_row_marked,mIBeaconInfo);
        if(firstTime) {
            View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row,null);
            mListView.addHeaderView(header);
            firstTime = false;
        }
        mListView.setAdapter(mAdapter);
    }

    public void alertNoBeaconsSensed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Beacons in the region");
        builder.setMessage("There are no beacons sensed in the region.");
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
    public void sortSavedBeacon() {
        Collections.sort(mIBeaconInfo, new Comparator<iBeaconInfo>() {
            public int compare(iBeaconInfo o1, iBeaconInfo o2) {
                String nicknane1 = o1.getNickname();
                String nickname2 = o2.getNickname();
                return nicknane1.compareTo(nickname2);
            }
        });
    }
    public void setSavedBeaconsNotPinged() {
        Log.v(TAG,"didn't get a signal from a saved beacon");
        HashMap<String,String> nonActiveSavedDevices = new HashMap<String, String>(mHashNicknames);
        //  Loop through to get a hashmap of devices that are saved but we didn't get info for
        for(int i = 0; i< mIBeaconInfo.size(); i++) {
            iBeaconInfo temp = mIBeaconInfo.get(i);
            nonActiveSavedDevices.remove(temp.getHash());
            Log.v(TAG,"This is being removed: " + temp.getNickname());
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
                Log.v(TAG,"not active beacon: " + device.getNickname());
            }
        }
    }

    /*public void playSound(Context context) throws IllegalArgumentException,
            SecurityException,
            IllegalStateException,
            IOException {

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(context, soundUri);
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            //mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }
    }*/

    public void playFromResource(int resId)
    {
        if (_mediaPlayer != null)
        {
            // _mediaPlayer.stop();     freeze on some emulator snapshot
            // _mediaPlayer.release();
            _mediaPlayer.reset();     // reset stops and release on any state of the player
        }
        _mediaPlayer = MediaPlayer.create(this, resId);
        _mediaPlayer.start();
    }
}