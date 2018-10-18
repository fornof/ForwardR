package com.example.rob.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    protected SQLiteDatabase db = null;
    private final BroadcastReceiver mybroadcast = new SmsListener(MainActivity.this, this);
    protected class SMSMessage{
//        private String _id;
//        private String address;
//        private String person;
//        private String body;
//        private Date date;
//        private String type;
        private Map<String,String> content;
        public SMSMessage(){
            //_id", "address", "person", "body", "date", "type"
            content = new HashMap<String,String>();
            content.put("_id","");
            content.put("address","");
            content.put("body","");
            content.put("date","");
            content.put("type","");
        }
        public SMSMessage(String keyValue){
            String[] values = keyValue.split(":");
            String key= values[0];
            if(!key.isEmpty() && values.length > 1) {
                content.put(key, values[1]);
            }

        }
        public SMSMessage(String key, String value){

           ;
            if(!key.isEmpty() && !value.isEmpty()) {
                content.put(key, value);
            }

        }
        public void add(String keyValue){
            String[] values = keyValue.split(":");
            String key= values[0];
            if(key !=null && !key.isEmpty() && values.length > 1) {
                content.put(key, values[1]);
            }

        }
        public void add(String key, String value){

            ;
            if(key !=null && value != null && !key.isEmpty() && !value.isEmpty()) {
                content.put(key, value);
            }

        }
        @Override
        public String toString(){
            StringBuilder result = new StringBuilder();
            for(Map.Entry<String,String> entries :this.content.entrySet()){
                result.append(entries.getKey() +":" + entries.getValue() +"|");
            }
            return result.toString();
        }
    }
    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);


                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    protected void sendText(String message,String phoneNo){

        //Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        //sendIntent.putExtra("sms_body", "default content");
        //sendIntent.setType("vnd.android-dir/mms-sms");

        //startActivity(sendIntent);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        // unregister listener
        unregisterReceiver(mybroadcast);

        super.onDestroy();

    }



    private void appendLog(String message) {
        Log.d("robert", message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // register a listener to listen to ids.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");

//     <receiver android:name=".BinarySMSReceiver">
//            <intent-filter>
//                <action android:name="android.provider.Telephony.WAP_PUSH_RECEIVED" />
//                <data android:mimeType="application/vnd.wap.mms-message" />
//            </intent-filter>
//        </receiver>
        IntentFilter mmsData = new IntentFilter("android.provider.Telephony.WAP_PUSH_RECEIVED");
        try {
            mmsData.addDataType("application/vnd.wap.mms-message");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        //IntentFilter mmsReceived = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

        registerReceiver(mybroadcast, filter);
        registerReceiver(mybroadcast, mmsData);
        // get database
        db = getDb();
        SmsListener listener = new SmsListener();
        PhoneDB.createIfNotExists(db);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            if (!checkPermsssionsSms()) {
                setlblMessage(this, "Something, something permissions...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                setlblMessage(this, "Permissions are alright.");
            }

//        if(checkPermsssionsSms()){
//            List<SMSMessage> message  = getMessage(null,5);
//            System.out.println(message.get(0));
//
//        }
        try {
            Setting current = SettingDB.getCurrentSetting(db);
            setlblMsgCommander(MainActivity.this, "Commander phone is set to:\n " + current.commanderAddress);
        }catch(Exception ex){
            setlblMsgCommander(MainActivity.this, "Commander phone is NOT SET\n PLEASE SET IT");
        }
        final TextView txtCommander =(TextView) findViewById(R.id.txtCommanderAddress);
        Button btnSetCommander = (Button)findViewById(R.id.btnSetCommander);
        final Setting setting = new Setting();

        btnSetCommander.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                setting.commanderAddress = txtCommander.getText().toString();
                SettingDB.insertSettings(setting, db);
                Setting current = SettingDB.getCurrentSetting(db);
                setlblMsgCommander(MainActivity.this, "Commander phone is set to:\n " + current.commanderAddress );
            }
        } );


//        txtSend.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//
//                // send the message to the phone number provided
//                sendText(message.getText().toString(),phoneNo.getText().toString());
//
//            }
//        });


        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
    protected static void setlblMessage(MainActivity main, String message){

        TextView lblMessage = (TextView) main.findViewById(R.id.lblMessage);
        lblMessage.setText(message);
    }
    protected static void setlblMsgCommander(MainActivity main, String message ){

        TextView lblMessage = (TextView) main.findViewById(R.id.lblMsgCommander);
        lblMessage.setText(message);
    }

    public SQLiteDatabase getDb(){
        SmsListener listener = new SmsListener();
        boolean result = listener.checkDatabasePath("/data/data/com.example.rob.myapplication/databases/forwardr.db");
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.example.rob.myapplication/databases/forwardr.db", null);
        return db;
   }
    protected boolean checkPermsssionsSms(){
        boolean nr =false;
        String[] permissionArrays = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.VIBRATE,
                Manifest.permission.RECEIVE_MMS,

               // android.Manifest.permission.WRITE_SMS,

//
//   Manifest.permission.WRITE_CONTACTS,
////Manifest.permission.READ_PROFILE,
//Manifest.permission.READ_PHONE_STATE,
//
//
//Manifest.permission.ACCESS_NETWORK_STATE,
//Manifest.permission.CHANGE_NETWORK_STATE,
// Manifest.permission.WRITE_SETTINGS,
//    Manifest.permission.INTERNET,

                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionArrays, 11111);
            for( String permission : permissionArrays) {
                nr = checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
                if(!nr){
                    return false;
                }
            }
            return true;
        }
        for( String permission : permissionArrays) {

            nr = PermissionChecker.checkSelfPermission(this, permission)
                    == PermissionChecker.PERMISSION_GRANTED;
            if(!nr){
                return false;
            }
        }
        return true;
    }
    protected List<SMSMessage> getMessage(String nameToFind, int messageCount){
        String msgData = "";
        // public static final String INBOX = "content://sms/inbox";
        // public static final String SENT = "content://sms/sent";
        // public static final String DRAFT = "content://sms/draft";
        final String SMS_URI_INBOX = "content://sms/inbox";
        Uri uri = Uri.parse(SMS_URI_INBOX);
        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        //address LIKE '%"+nameToFind+"%'
        Cursor cursor = getContentResolver().query(uri, projection, null, null, "date desc");
        int count = messageCount*6;
        List<SMSMessage> mySMSList = new ArrayList<SMSMessage>();
        SMSMessage result = new SMSMessage();
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {

                for(int idx=0;idx<cursor.getColumnCount();idx++)
                {
                    if(cursor.getColumnName(idx).equals("_id")){
                        result= new SMSMessage();

                    }

                    result.add( cursor.getColumnName(idx),cursor.getString(idx)) ;
                    if(cursor.getColumnName(idx).equals("type")){
                        mySMSList.add(result);
                    }
                }
                // use msgData
                count--;
            } while (cursor.moveToNext() && count >0);
        } else {
            // empty box, no SMS
        }
        cursor.close();
        return mySMSList;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

    }
}
