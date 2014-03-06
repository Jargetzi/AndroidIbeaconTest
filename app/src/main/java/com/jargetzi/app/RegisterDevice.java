package com.jargetzi.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.HashMap;

public class RegisterDevice extends Activity {
    protected static final String TAG = "RangingActivity";
    private TextView mHash;
    private EditText mInputPassword;
    private EditText mInputNickname;
    private iBeaconInfo mIbeaconInfo;
    private Button mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device);

        //  Need this to interact with Parse Server

        Parse.initialize(this, "gYJPKww2de65Qq5rDXXdp4C6fqZbvxFttbOqbyQg", "g5NbntohgJ2wQwNhWrEnG0nun5QrDulsDgQfTnHF");
        //sPushService.setDefaultPushCallback(this, MainActivity.class);

        Intent intent = getIntent();
        String uuid = intent.getStringExtra("uuid");
        String major = intent.getStringExtra("major");
        String minor = intent.getStringExtra("minor");

        Log.v(TAG,"major: " + major +" minor: " + minor);

        mIbeaconInfo = new iBeaconInfo(uuid,null,major,minor);

        final String hash = hashFunction(uuid,major,minor);
        final String subHash = hash.substring(0,10);

        mHash = (TextView)findViewById(R.id.register_device_hash);
        mInputPassword = (EditText)findViewById(R.id.register_input_password);
        mInputNickname = (EditText)findViewById(R.id.register_input_nickname);
        mHash.setText(subHash);

        mAddButton = (Button)findViewById(R.id.button_add);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = String.valueOf(mInputPassword.getText());
                final String nickname = String.valueOf(mInputNickname.getText());
                if(password.isEmpty() || nickname.isEmpty()) {
                    //  Did not supply required input parameters
                    Toast.makeText(getBaseContext(),"Need to supply a password and nickname",Toast.LENGTH_SHORT).show();
                } else {
                    //  Validate password with Parse
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("fullnameHash",hash);
                    params.put("devicePassword",password);
                    ParseCloud.callFunctionInBackground("registerDevice",params, new FunctionCallback<Object>() {
                        public void done(Object o, ParseException e) {
                            if (e == null) {
                                // Got a response from cloud code
                                Log.v(TAG,o.toString());
                                Log.v(TAG,hash);
                                if(o.toString().equals("true")) {
                                    Log.v(TAG,"success");
                                    //  Save data to file
                                    mIbeaconInfo.setNickname(nickname);
                                    mIbeaconInfo.setHash(subHash);
                                    addDevice();
                                } else {
                                    Toast.makeText(getBaseContext(),"Incorrect Password",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Log.v(TAG,"This was the exception: "+ e.toString());
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register_device, menu);
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
            Log.e(TAG,"File not found " + e.toString());
        } catch(Exception e) {
            Log.e(TAG, "Cannot read file: " + e.toString());
        }
        return ret;
    }

    public void addDevice() {
        String filename = getString(R.string.my_devices_file);
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
        String hash = mIbeaconInfo.getHash().substring(0,10);
        if (jsonObject.has(hash)) {
            //	this device is already saved
            Toast.makeText(getBaseContext(), "Device is already saved", Toast.LENGTH_SHORT).show();
        } else {
            JSONObject beaconJsonObject = new JSONObject();
            try {
                beaconJsonObject.put("major",mIbeaconInfo.getMajor());
                beaconJsonObject.put("minor",mIbeaconInfo.getMinor());
                beaconJsonObject.put("nickname",mIbeaconInfo.getNickname());
                beaconJsonObject.put("uuid", mIbeaconInfo.getUuid());
                beaconJsonObject.put("distance",mIbeaconInfo.getDistance());
                beaconJsonObject.put("hash",mIbeaconInfo.getHash());

                jsonObject.put(hash, beaconJsonObject);
                String saveBeaconInfo = jsonObject.toString();
                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);	//was append
                    outputStream.write(saveBeaconInfo.getBytes());
                    outputStream.close();
                    Toast.makeText(getBaseContext(), "Saving Device", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //  After Save go to main page
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void deleteFile() {
        String filename = getString(R.string.my_devices_file);
        File dir = getFilesDir();
        File file = new File(dir,filename);
        file.delete();
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
