package com.example.rob.myapplication;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SmsListener extends BroadcastReceiver {
    protected class Phone {
        public String phoneNumber;
        public int id ;
        public String name;
        public Phone(){

        }
        public void add(String phone ,int id){
            this.phoneNumber = phone;
            this.id = id;
        }

    }
    private String commandAddress = ""; //this is the address to forward all messages to.
    protected List<String> rules = new ArrayList<String>();
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    String messageBody = smsMessage.getMessageBody();
                }
            }
            //MainActivity main = new MainActivity();
            //List<MainActivity.SMSMessage> message = main.getMessage(null, 5);
            //main.setlblMessage(message.get(0).toString());
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        processMessageBody(msgBody, msg_from);
                    }
                }catch(Exception e){
//                            Log.d("Exception caught",e.getMessage());
                   System.out.println( e.getMessage());
                }
            }
        }
    }


    public int getHashtagValue(String msg){
        int hashNum = -1;
        int hashIndex = msg.indexOf('#');
        if(hashIndex == -1){
           return -1;

        }
        StringBuilder result = new StringBuilder();
        for(int i = hashIndex +1 ; i <= msg.length(); i++){
            if(i == msg.length() || msg.charAt(i) == ' '){
                if(result.length() == 0){
                    return -1;
                }
                try{
                    hashNum = Integer.parseInt(result.toString());
                    return hashNum;
                }catch(Exception ex){
                    return -1;
                }
            }

                if(Character.isDigit(msg.charAt(i))){
                    result.append(msg.charAt(i));
                }


        }
        return -1;
    }
    public boolean isCommandAddress(String msgFrom){
        if(msgFrom.contains(commandAddress)){
            return true;
        }
        return false;
    }

    public boolean checkDatabasePath(String DB_NAME){
        try {
            File dbFile = new File(DB_NAME);
            if (dbFile.exists()) {
                return true;
            } else {
                //This'll create the directories you wanna write to, so you
                //can put the DB in the right spot.
                dbFile.getParentFile().mkdirs();
                return false;
            }
        }
        catch(Exception ex){
            return false;
        }
        }


    public void processMessageBody(String msg, String msgFrom) {

       //SmsReaderDbHelper mDbHelper = new SmsReaderDbHelper();
        // Gets the data repository in write mode
        //String query = "select sqlite_version() AS sqlite_version";
        boolean result = checkDatabasePath("/data/data/com.example.rob.myapplication/databases/forwardr.db");
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.example.rob.myapplication/databases/forwardr.db", null);
        createIfNotExists(db);
        int count = indexOfPhone("",db, true);
        //Cursor cursor = db.rawQuery(query, null);
        String sqliteVersion = "";
       // if (cursor.moveToNext()) {
       //     sqliteVersion = cursor.getString(0);
       // }
        // Create a new map of values, where column names are the keys

        boolean isCommander = isCommandAddress(msgFrom);
        if(!isCommander){
            // if this message is not from commander, send message to commander, add a #<index>
            long index = indexOfPhone( msgFrom, db,  false);
            if(index == -1){
                index = addPhone(msgFrom,db);
            }

            if(commandAddress != "") {
                sendText(msg + "[" + msgFrom + "]#" + index, commandAddress);
            }
            return; //do not forward the messages if the commander is not sending commands

        }
        // IT'S THE COMMANDER! FORWARD TEXTS BY #<index>

        //see if there's a hashtag character in the message, if so , get the index value
        int hashValue = getHashtagValue(msg);
        int ampersand = msg.indexOf("&");
        if(hashValue < 0 ){

           if(ampersand >-1 ){
               String phone = getPhone(msg);
          ;

               // if already in the db,
               long index = indexOfPhone( phone, db, false);
               if(index != -1){
                   sendText("phone exists, #"+index+ ":" + phone  , commandAddress);
               }
               else{

                   index = addPhone(phone, db);
                   count = indexOfPhone("",db, true);
                   sendText("phone added!, #"+index+": " + phone, commandAddress);
               }

           }
            StringBuilder builder= new StringBuilder();
            ArrayList<Phone> phones =  listOfAllPhone(db);

            for(int i = 0; i < phones.size() ; i++){
                Phone phone=phones.get(i);
                builder.append("\n#"+phone.id+" : " + phone.phoneNumber );

            }

            sendText("Hello Commander! You have " + count+ " contacts."+ builder.toString() +"\n type &<phone> , to add to a rule" , commandAddress);
        }
        String forwardToAddress = "";
        if(hashValue < count  && hashValue > -1 ) {
            Phone myPhone = getPhoneById(SmsReaderContract.PhoneEntry._ID,hashValue+"",db);
            if(myPhone == null){}
            else{
                forwardToAddress = myPhone.phoneNumber; // store the address to an index.
            }

            msg = stripMessageOfHashValue(msg);
            sendText(msg  , forwardToAddress);

        }
        else{

        }
        db.close();
    }


    public long addPhone(String phone, SQLiteDatabase db){

        ContentValues values = new ContentValues();
        values.put(SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER, phone);
        long newRowId = db.insert(SmsReaderContract.PhoneEntry.TABLE_NAME, null, values);
        return newRowId;
    }


    public void createIfNotExists(SQLiteDatabase db){
    try {
        db.execSQL(SmsReaderContract.PHONE_CREATE_ENTRIES);
        db.execSQL(SmsReaderContract.SETTINGS_CREATE_ENTRIES);
    }catch(Exception ex){
        System.out.println(ex.getMessage());
    }
    }
    public ArrayList<Phone> listOfAllPhone(SQLiteDatabase db){
        String[] projection = {
                BaseColumns._ID,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME
        };
        String sortOrder =
                SmsReaderContract.PhoneEntry._ID + " DESC";

        Cursor cursor = db.query(
                SmsReaderContract.PhoneEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList itemIds = new ArrayList<Phone>();
        while(cursor.moveToNext()) {
            Phone phone = new Phone();
            phone.id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry._ID));
            phone.phoneNumber=cursor.getString(
                    cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER));
            itemIds.add(phone);
        }
        return itemIds;
    }

    public Integer indexOfPhone(String phone,SQLiteDatabase db, boolean countResults){

        //SQLiteDatabase db = mDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME
        };

// Filter results WHERE "title" = 'My Title'
        String selection = SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER + " = ?";
        String[] selectionArgs = { phone };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                SmsReaderContract.PhoneEntry._ID + " DESC";
        Cursor cursor;
        if(!countResults) {
             cursor = db.query(
                    SmsReaderContract.PhoneEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
        }
        else{
           cursor = db.query(
                    SmsReaderContract.PhoneEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
        }

        ArrayList itemIds = new ArrayList<Integer>();
        while(cursor.moveToNext()) {
            int itemId = cursor.getInt(
                    cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry._ID));
            itemIds.add(itemId);
        }
        cursor.close();
        if(countResults){
            return itemIds.size();
        }

        if(itemIds.size() == 0){
            return -1;
        }
        int item = (int)itemIds.get(0);
        return item;
    }

    public Phone getPhoneById(String column, String value,SQLiteDatabase db ){


        String[] projection = {
                BaseColumns._ID,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME
        };

// Filter results WHERE "title" = 'My Title'
        String selection = column + " = ?";
        String[] selectionArgs = { value };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                SmsReaderContract.PhoneEntry._ID + " DESC";
        Cursor cursor;


            cursor = db.query(
                    SmsReaderContract.PhoneEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

        ArrayList<Phone> itemIds = new ArrayList<Phone>();
        while(cursor.moveToNext()) {
            Phone phone = new Phone();
            phone.id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry._ID));
            phone.phoneNumber=cursor.getString(
                    cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER));
            itemIds.add(phone);
        }




        if(itemIds.size() == 0){
            return null;
        }
        Phone item = (Phone)itemIds.get(0);
        return item;
    }



    public String getPhone(String msg){
        if(msg.length() == 0){
            return null;
        }
        int ampersand = msg.indexOf("&");
        StringBuilder sb = new StringBuilder();
        for(int i = ampersand + 1 ; i <= msg.length(); i++){
            if(i == msg.length() || msg.charAt(i) == ' '){
                if(sb.length() == 0 ){
                    return null;
                }
                break;
            }
            if(Character.isDigit(msg.charAt(i))){
                sb.append(msg.charAt(i));
            }
        }
        return sb.toString();
    }


    public String stripMessageOfHashValue(String msg){
        int hashIndex = msg.indexOf('#');
        if(hashIndex == -1){
            return msg;

        }
        StringBuilder result = new StringBuilder();
        for(int i = hashIndex ; i <= msg.length(); i++){
            if(i == msg.length() || msg.charAt(i) == ' ') {
                if (result.length() == 0) {
                    return msg;
                }
                msg =  msg.replace(result.toString(),"");
                return msg;
            }
            if(msg.charAt(i) == '#'){
                result.append(msg.charAt(i));
            }
            if(Character.isDigit(msg.charAt(i))){
                result.append(msg.charAt(i));
            }


        }
        return msg;

    }

    ////


    ////


        protected void sendText(String message,String phoneNo){

            //Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            //sendIntent.putExtra("sms_body", "default content");
            //sendIntent.setType("vnd.android-dir/mms-sms");

            //startActivity(sendIntent);

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, message, null, null);
                //Toast.makeText(getApplicationContext(), "SMS Sent!",
                    //    Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                //Toast.makeText(getApplicationContext(),
                  //      "SMS faild, please try again later!",
                   //     Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
}
