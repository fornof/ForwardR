package com.example.rob.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.xlythe.textmanager.text.pdu.PduPart;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmsListener extends BroadcastReceiver {
    private static final String SET = "set" ; // %set name 2 bobert , Sets the name
                                              // %set number 2 7195555555, sets the number
                                              // %set name 2 !lookup  , tries to find the name in contacts and sets it
    private static final String GET = "get" ; // %get 2 , gets the name and number of 2.

    private static final String DEL = "delete" ; //%delete 2  , hard deletes entry #2
    private static final String HELP = "?" ; //%?   , displays help
    private static final String DEBUG_TAG = "DEBUG";
    private static final String STATUS = "status";
    private static final String MMS_TYPE = "application/vnd.wap.mms-message";
    protected Context context = null;
    protected MainActivity main = null;
    private int lastIndex;

    public SmsListener(){

    }
    public SmsListener(Context context){
        this.context = context;
    }
    public SmsListener(Context context, MainActivity main){
        this.context = context;
        this.main = main;
    }

    private String commanderAddress = ""; //this is the address to forward all messages to.
    protected List<String> rules = new ArrayList<String>();
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("robert",intent.getAction());

        if(intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED") && intent.getType().equals(MMS_TYPE)){
            Log.d("robert","WE GOT DATA!");


            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;

            String str = "";
            int contactId = -1;
            String address;
            String incomingNumber = "";
            if (bundle != null) {
//
                byte[] buffer = bundle.getByteArray("data");
                incomingNumber = new String(buffer);
                int indx = incomingNumber.indexOf("/TYPE");
                if(indx>0 && (indx-15)>0){
                    int newIndx = indx - 15;
                    incomingNumber = incomingNumber.substring(newIndx, indx);
                    indx = incomingNumber.indexOf("+");
                    if(indx>0){
                        incomingNumber
                                = incomingNumber.substring(indx);
                    }

                   }
                }
            incomingNumber = trimPlusSignAndOne(incomingNumber);
            SQLiteDatabase db = getDb();
            Setting current = SettingDB.getCurrentSetting(db);
            if(current != null) {
                this.commanderAddress = current.commanderAddress;
            }
                boolean isCommander = iscommanderAddress(incomingNumber);
//                msgFrom = trimPlusSignAndOne(msgFrom);


            MainActivity.setlblMsgCommander(main, "Commander phone is set to: " + this.commanderAddress );

            MmsMessage message = getLastMmsId();
               message.setFromAddress(incomingNumber)   ;
                if(!isCommander){
                    // if this message is not from commander, send message to commander, add a #<index>
                    long index = PhoneDB.indexOfPhone( incomingNumber, db,  false);
                    if(index == -1){
                        index = PhoneDB.addPhone(context,incomingNumber,db);
                    }
                    Phone phone = PhoneDB.getPhoneById((int)index,db);
                    if(commanderAddress != "") {
                        if(message.getData() != null ){
                            Bitmap bmp = message.getImage();
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            bmp.recycle();
                            sendText(message.getText()+"[" + phone.name + "]#" + index, byteArray , commanderAddress, message);
                        }
                        else {
                            sendText("(Picture Received) + " +message.getText()+  "[" + phone.name + "]#" + index, commanderAddress);
                        }
                    }
                    return; //do not forward the messages if the commander is not sending commands

                }
               // if(contactId != -1){
                   //main.showNotification(contactId, str);
               // }

              //  Object[] pdus = (Object[]) bundle.get("pdus");
                //getMessagesFromIntent(intent);
//                if(pdus == null){
//                    Log.d("Robert", "pdus are null");
//
//                }
//                msgs = new SmsMessage[pdus.length];
//                byte[] data = null;
//
//                for (int i = 0; i < msgs.length; i++) {
//                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
//                    String msg_from = msgs[i].getOriginatingAddress();
//                    String msgBody = msgs[i].getMessageBody();
//
//                    data = msgs[i].getUserData();
//                    processMessageBody(msgBody, msg_from, data);
//
//          //      }
            //    return;
           // }
        }
        else if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
//                    String msgBody = smsMessage.getMessageBody();
//                    String msg_from = smsMessage.getOriginatingAddress();
//                    //processMessageBody(msgBody, msg_from);
//                }
//
//            }
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public MmsMessage getLastMmsId(){
       int id = getLastMmsIds(1).get(0);

        //byte[] part = getPartOfMMS(id);
       MmsMessage message = getMmsText(id);

        return message;
    }
    public List<Integer> getLastMmsIds(int count){
        String selection = "";
        String[] selectionArgs = new  String[]{""};
        // new String[]{"_id", "thread_id", "date","address", "body"}

        String[] projection =  new String[] {"_id", "date", "msg_box", "read", "sub", "thread_id"};

        Cursor cursor = getContentResolver().query(Telephony.Mms.Inbox.CONTENT_URI,
                       projection, null, null, "date DESC LIMIT "+ count);
        Long threadId = -1L;
        if(cursor == null){
            return null;
        }
        ArrayList<Integer> ids = new ArrayList<Integer>();
        while (cursor.moveToNext())
        {
            int key = cursor.getInt (cursor.getColumnIndexOrThrow ("_id"));
            threadId = cursor.getLong (cursor.getColumnIndexOrThrow ("thread_id"));
            String read = cursor.getString (cursor.getColumnIndexOrThrow ("read")); // phone #
            long date = cursor.getLong (cursor.getColumnIndexOrThrow ("date"));
            String sub = cursor.getString (cursor.getColumnIndexOrThrow ("sub"));
            String body = cursor.getString (cursor.getColumnIndexOrThrow ("msg_box"));

            String q = String.format ("%04d %04d %10s %s %s",
                    key, threadId, read,
                    DateUtils.formatDateTime(context,date*1000,0),
                    body == null ? "" : body.substring (0,Math.min(10,body.length()-1)));
            Log.d ("robert", q); // simple wrapper for Log.d()
            ids.add(key);
        }

        cursor.close();
        return ids;
    }

    private ArrayList<PduPart> getPartOfMMS(String mmsID) {
        String selectionPart = "mid=" + mmsID;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = context.getContentResolver().query(uri, null,
                selectionPart, null, null);
        ArrayList<PduPart> parts = new ArrayList<PduPart>();

        try {
            if (cursor.moveToFirst()) {
                do {

                    PduPart tempPart= new PduPart();


                    tempPart.setData(cursor.getBlob(cursor.getColumnIndex(Telephony.Mms.Part._DATA)));
                    tempPart.setName(cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.NAME)).getBytes());
                    //tempPart.setContentId(cursor.getBlob(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_ID)));
                    tempPart.setContentType(cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE)).getBytes());
                    //tempPart.setContentDisposition(cursor.getBlob(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_DISPOSITION)));
                    //tempPart.setContentLocation(cursor.getBlob(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_LOCATION)));
                    //tempPart.setCharset(cursor.getInt(cursor.getColumnIndex(Telephony.Mms.Part.CHARSET)));
                    parts.add(tempPart);
                } while (cursor.moveToNext());
            }
            return null;
        } finally {
            cursor.close();
        }

    }


    private MmsMessage getMmsText(int id) {
        String selectionPart = "mid=" + id;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = context.getContentResolver().query(uri, null,
                selectionPart, null, null);
        MmsMessage message = new MmsMessage();
        try {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
                    if ("text/plain".equals(type)) {
                        String path = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.TEXT));
                        message.setText(path);
                    }
                    if ((type).contains("image")) {
                        byte[] path = cursor.getBlob(cursor.getColumnIndex(Telephony.Mms.Part._DATA));
                        String partId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part._ID));
                        message.setPduParts(getPartOfMMS(id +""));
                        message.setImage(getMmsImage(partId));
                        message.setData(path);
                        message.setType(type);
                    }
                    if ((type).contains("smil")) {
                        byte[] path = cursor.getBlob(cursor.getColumnIndex(Telephony.Mms.Part._DATA));
                        message.setSmil(path);
                    }

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return message;
    }

    private Bitmap getMmsImage(String _id) {
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = getContentResolver().openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return bitmap;
    }
    private String getMmsType(int id) {
        String selectionPart = "mid=" + id;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = context.getContentResolver().query(uri, null,
                selectionPart, null, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
                    if (!type.equals("application/smil")) {
                        return type;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return null;
    }




    private ContentResolver getContentResolver(){
        return this.context.getContentResolver();
    }


    public static String getMMSAddress(Context context, String id) {
        String addrSelection = "type=137 AND msg_id=" + id;
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        String[] columns = { "address" };
        Cursor cursor = context.getContentResolver().query(uriAddress, columns,
                addrSelection, null, null);
        String address = "";
        String val;
        if (cursor.moveToFirst()) {
            do {
                val = cursor.getString(cursor.getColumnIndex("address"));
                if (val != null) {
                    address = val;
                    // Use the first one found if more than one
                    break;
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        // return address.replaceAll("[^0-9]", "");
        return address;
    }

    private String getAddressNumber(int id) {
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = getContentResolver().query(uriAddress, null,
                selectionAdd, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    try {
                        Long.parseLong(number.replace("-", ""));
                        name = number;
                    } catch (NumberFormatException nfe) {
                        if (name == null) {
                            name = number;
                        }
                    }
                }
            } while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }
        return name;
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
    public boolean iscommanderAddress(String msgFrom){
        if(msgFrom.contains(commanderAddress)){
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

    public void processMessageBody(String msg, String msgFrom, byte[] data) {
        // process byte data as picture
        // save it to global
        processMessageBody( msg, msgFrom);

    }
    public SQLiteDatabase getDb(){
        boolean result = checkDatabasePath("/data/data/com.example.rob.myapplication/databases/forwardr.db");
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.example.rob.myapplication/databases/forwardr.db", null);
        PhoneDB.createIfNotExists(db);
        return db;
    }
    /**
     * This processes a mini cli based on input from the message
     * #[index] [message] sends a message to the index in contact list
     * &[phone_number] adds a phone number to the contact list
     *
     * @param msg : incoming text messsage
     * @param msgFrom : phone number of who the message is from
     */
    public void processMessageBody(String msg, String msgFrom) {
        msgFrom = trimPlusSignAndOne(msgFrom);
        SQLiteDatabase db = getDb();
        //createSettingsIfNotExists(db); // insert a row into settings if not exist already.
        int count = PhoneDB.indexOfPhone("",db, true);
        String sqliteVersion = "";
       // if (cursor.moveToNext()) {
       //     sqliteVersion = cursor.getString(0);
       // }
        // Create a new map of values, where column names are the keys
        Setting current = SettingDB.getCurrentSetting(db);
        if(current.lastIndex > 0 ){
            this.lastIndex = current.lastIndex;
        }
        if(current != null) {
            this.commanderAddress = current.commanderAddress;
        }
        MainActivity.setlblMsgCommander(main, "Commander phone is set to: " + this.commanderAddress );
        
        boolean isCommander = iscommanderAddress(msgFrom);
        if(!isCommander){
            // if this message is not from commander, send message to commander, add a #<index>
            long index = PhoneDB.indexOfPhone( msgFrom, db,  false);
            if(index == -1){
                index = PhoneDB.addPhone(context,msgFrom,db);
            }
            Phone phone = PhoneDB.getPhoneById((int)index,db);
            if(commanderAddress != "") {
                sendText(msg + "[" + phone.name + "]#" + index, commanderAddress);
            }
            return; //do not forward the messages if the commander is not sending commands

        }
        // IT'S THE COMMANDER! FORWARD TEXTS BY #<index>

        //see if there's a hashtag character in the message, if so , get the index value
        int hashValue = getHashtagValue(msg);
        int percent = -1;
        if(msg.charAt(0) == '%'){
            percent = 0;
        }

        if(percent > -1){
             // %set name 2 bobert , Sets the name
            // %set number 2 7195555555, sets the number
            // %set name 2 !lookup  , tries to find the name in contacts and sets it

            // %get 2 , gets the name and number of 2.
            // %get last 2 , gets the last message for index 2.
            // %get msg 2 5 , gets last 5 messages for index 2
            //%delete 2  , hard deletes entry #2
            String[] commands = msg.split(" ");
            if(commands[0].equals("%"+SET)){
                int indexValue = -1;
                if(commands.length < 4){
                    sendText("Error: try '%set name 2 bobert' which sets 2's name to bobert", commanderAddress);
                    return;
                }
                if(commands[1].equals("name")){

                    try{
                        indexValue = Integer.parseInt(commands[2]);
                    }
                    catch(NumberFormatException ex){
                        sendText("Error: third word needs to be an integer", commanderAddress);
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for(int i = 3 ; i < commands.length; i++){
                        sb.append(commands[i]+" ");

                    }

                    String restOfCommands =  sb.toString().trim();;
                    if(restOfCommands.equals("@")){
                        try {
                            PhoneDB.setPhone(indexValue, SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME, PhoneDB.getContactName(context,
                                    PhoneDB.getPhoneById(indexValue, db).phoneNumber), db);
                            sendText("Success", commanderAddress);
                        }catch(Exception ex){
                            sendText("Lookup failed. Sorry.", commanderAddress);
                        }
                        return;
                    }
                    String myResult = restOfCommands.trim().replace("\"", "").replace("'","");
                    PhoneDB.setPhone(indexValue,SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME, myResult, db);
                    sendText("Success", commanderAddress);
                    return;
                }
                if(commands[1].equals("phone") || commands[1].equals("number")){

                    try{
                        indexValue = Integer.parseInt(commands[2]);
                    }
                    catch(NumberFormatException ex){
                        sendText("Error: third word needs to be an integer", commanderAddress);
                        return;
                    }
                    // this is untested and quite frankly , I don't think it's going to work - going from a contact name to number, its too dicey.
                    // plus I haven't googled it that much.
                    if(commands[3].equals("@")){
                        try {
                            if(PhoneDB.setPhone(indexValue, SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER, PhoneDB.getContactNumber(context,
                                    PhoneDB.getPhoneById(indexValue, db).name), db))
                            {
                                sendText("Success", commanderAddress);
                                return;
                            }
                            sendText("Lookup failed. Sorry.", commanderAddress);
                        }catch(Exception ex){
                            sendText("Lookup failed. Sorry.", commanderAddress);
                        }
                        return;
                    }

                    PhoneDB.setPhone(indexValue,SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER, commands[3], db);
                    sendText("Success", commanderAddress);
                    return;
                }

            }
            else  if(commands[0].equals("%"+GET)){
                if(commands.length == 4){
                    int indexValue = -1;
                    int msgCount = -1;
                    if(commands[1].equals("msg")){
                        try{
                            indexValue = Integer.parseInt(commands[2]);
                        }catch(Exception ex){
                            sendText("Error: expecting number for third word but received: " + commands[2], commanderAddress );
                        }
                        try{
                            msgCount = Integer.parseInt(commands[3]);
                        }catch(Exception ex){
                            sendText("Error: expecting number for msgCount fourth word but received: " + commands[3], commanderAddress );
                            return;
                        }
                        ArrayList<Message> messages = PhoneDB.getMessage(context, PhoneDB.getPhoneById(indexValue,db).phoneNumber, msgCount);
                        if(messages.isEmpty()){
                            sendText("Error, something went wrong with getting the last phone message for: " + commands[2], commanderAddress );
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        for(int i = messages.size()-1;  i >= 0; i--){
                            sb.append("#").append(indexValue).append(": ").append(messages.get(i).getMessage()).append("\n");
                        }
                        sendText(sb.toString() , commanderAddress);
                        return;
                    }
                    else{
                        sendText("Error: expecting command but received" + commands[1], commanderAddress );
                        return;
                    }
                }
                if(commands.length == 3){
                    int indexValue = -1;
                   if(commands[1].equals("last")){
                       try{
                           indexValue = Integer.parseInt(commands[2]);
                       }catch(Exception ex){
                           sendText("Error: expecting number for third word but received: " + commands[2], commanderAddress );
                           return;
                       }
                       Message message = PhoneDB.getMessage(context, PhoneDB.getPhoneById(indexValue,db).phoneNumber);
                       if(message == null){
                           sendText("Error, last msg not found. Have you contacted this person recently? " + commands[2], commanderAddress );
                           return;
                       }
                       sendText(message.getMessage() + ":LAST: #"+indexValue, commanderAddress);
                       return;
                    }
                    else{
                       sendText("Error: expecting command but received" + commands[1], commanderAddress );
                       return;
                   }
                }
                if(commands.length != 2){
                    sendText("Error: try '%get 2'", commanderAddress);
                    return;
                }
                    int indexValue = -1;
                    try{
                        indexValue = Integer.parseInt(commands[1]);
                    }
                    catch(NumberFormatException ex){
                        sendText("Error: second word must be an integer", commanderAddress);
                        return;
                    }
                    Phone phone = PhoneDB.getPhoneById(SmsReaderContract.PhoneEntry._ID,indexValue, db);
                    if(phone == null){
                        sendText("Error: Either not a valid id or something else", commanderAddress);
                        return;
                    }
                    sendText("#"+ phone.id +":" +  phone.phoneNumber +":"+ phone.name == null? "":phone.name , commanderAddress);
                    return;


            }
            else if(commands[0].equals("%"+STATUS)){
                sendText("Hello Commander! You have " + count + " contacts. " + getContactList(db)
                        + "\n type &<phone> , to add to a rule or %? for help. Last index is:"+ lastIndex, commanderAddress);
                return;
            }
            else  if(commands[0].equals("%"+DEL)){
                if(commands.length != 2){
                    sendText("Error: try '%delete 2'", commanderAddress);
                    return;
                }
                int indexValue = -1;
                try{
                    indexValue = Integer.parseInt(commands[1]);
                }
                catch(NumberFormatException ex){
                    sendText("Error: second word must be an number", commanderAddress);
                    return;
                }
                int deleted =    PhoneDB.deletePhoneById(indexValue+"", db);
                if(deleted  == -1){
                    sendText("Error: Either not a valid id or something else", commanderAddress);
                    return;
                }
                sendText("#"+ indexValue +": deleted. response:"+ deleted  , commanderAddress);
                return;


            }
            else if( commands[0].equals("%"+HELP)){
                sendText("&7195555555 :: adds number to contact list and pulls from contacts\n" +
                                  "#1 Hi There! :: sends a message 'Hi There' to #1 in contacts \n" +
                                  "%set name 2 bob :: sets name of #2 to bob\n" +
                                    "set name 2 @ :: sets name of #2 to whatever is in your contacts.\n"+
                                   "%set phone 2 7195555555 :: sets phone of #2 to 7195555555\n" +
                                    "%get 2 :: gets name and number for #2\n" +
                                    "%get last 2 :: gets last message for #2\n" +
                                    "%get msg 2 5 :: gets previous 5 messages for #2\n"+
                                    "%status :: gets a list of current info\n"+
                                    "%delete 2 :: deletes 2 from contact list" , commanderAddress);
                return;
            }

        }
        int ampersand = msg.indexOf("&");
        if(hashValue < 0 ){

           if(ampersand >-1 ){
               Phone phone = getPhoneFromMessage(msg);
          ;

               // if already in the db,
               long index = PhoneDB.indexOfPhone( phone.phoneNumber, db, false);
               if(index != -1){
                   sendText("phone exists, #"+index+ ":" + phone.phoneNumber  , commanderAddress);
               }
               else{

                   index = PhoneDB.addPhone(context,phone.phoneNumber, db);
                   count = PhoneDB.indexOfPhone("",db, true);
                   sendText("phone added!, #"+index+": " + phone.phoneNumber, commanderAddress);
               }
                return;
           }

            /// send message to last texted.
             current = SettingDB.getCurrentSetting(db);
            lastIndex = current.lastIndex;
            if(lastIndex > 0){
                sendToId(lastIndex, msg, db);
            }
            else {
                sendText("Hello Commander! You have " + count + " contacts. " + getContactList(db)
                        + "\n type &<phone> , to add to a rule or %? for help. Last index is:"+ lastIndex, commanderAddress);
            }
        }
        String forwardToAddress = "";
        if(hashValue <= count  && hashValue > -1 ) {
            SettingDB.setLastIndex(hashValue,db);
            sendToId( hashValue,msg, db);
        }
        else{

        }
        db.close();
    }
   private String getContactList(SQLiteDatabase db){
       StringBuilder builder= new StringBuilder();
       ArrayList<Phone> phones =  PhoneDB.listOfAllPhone(db);

       for(int i = 0; i < phones.size() ; i++){
           Phone phone=phones.get(i);
           String namer = "";
           if(phone.name !=null){
               namer =phone.name.replace(" ","_");
           }
           else{
               namer = "[blank]";
           }
           builder.append("\n#"+phone.id+" : " + phone.phoneNumber+ ":" + namer );

       }
       return builder.toString();
   }
    private void sendToId(int index,String msg, SQLiteDatabase db){
        Phone myPhone = PhoneDB.getPhoneById(SmsReaderContract.PhoneEntry._ID,index,db);
        String forwardToAddress = "";
        if(myPhone == null ){
            sendText("Error:Could not find phone number with that id"  , commanderAddress);
            return;
        }
        else{
            forwardToAddress = myPhone.phoneNumber; // store the address to an index.
        }

        msg = stripMessageOfHashValue(msg);
        sendText(msg  , forwardToAddress);


    }
    private String trimPlusSignAndOne(String msgFrom) {
        if(msgFrom.length() == 12 && msgFrom.contains("+")){
            return msgFrom.substring(2);
        }
        return msgFrom;
    }


    public Phone getPhoneFromMessage(String msg){
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
//            else if(msg.charAt(i) == '+'){
//                sb.append(msg.charAt(i));
//            }
            else if(Character.isDigit(msg.charAt(i))){
                sb.append(msg.charAt(i));
            }
        }
        Phone phone = new Phone();
//        if(sb.toString().length() == 10){
//            phone.phoneNumber = "+1"+sb.toString();
//        }
//        else if(sb.toString().length() == 11){
//            if(sb.toString().charAt(0) != '+'){
//
//            }
//            phone.phoneNumber = "+"+sb.toString();
//        }
        String[] splitter = msg.split(" ");
        if(splitter.length > 1){
            boolean isFirst = true;
           StringBuilder sb2 = new StringBuilder();
            for(String str : splitter){
                if(isFirst){
                    isFirst = false;
                    continue;
                }
                sb2.append(str +" ");
            }
            phone.name = sb2.toString().trim();
        }

            phone.phoneNumber = sb.toString();

        return phone ;
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

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void sendText(String message, byte[] image, String phoneNo, MmsMessage myMessage){
        // Get the default instance of
        // This doesn't work - the image can be viewed fine on a bitmap, but I can't create a pdu - its the most undocumented old thing I've seen in awhile.
        // sending message passthrough to text
         sendText(message, phoneNo);

        SavePhotoTask saveToFile = new SavePhotoTask();

        SmsManager smsManager = SmsManager.getDefault();
        String path = saveToFile.doInBackground(image);
        Uri uri = Uri.parse("file://"+ path);
        //TextManager manager = TextManager.getInstance(context);

       // manager.send(message).to(phoneNo);
//        Intent i = new Intent(Intent.ACTION_VIEW);
//        i.putExtra("address",phoneNo);
//        i.putExtra("sms_body",message);
//        i.putExtra(Intent.EXTRA_STREAM,uri );
//        i.setType("vnd.android-dir/mms-sms");
//        main.startActivity(i);



       // final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //final int result = connMgr.get( ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_MMS);
//        String subject = "hi there!";
//        String recipient = phoneNo;
//        final SendReq sendRequest = new SendReq();
//
//        final EncodedStringValue[] sub = EncodedStringValue.extract(subject);
//        if (sub != null && sub.length > 0) {
//            sendRequest.setSubject(sub[0]);
//        }
//        final EncodedStringValue[] phoneNumbers = EncodedStringValue
//                .extract(recipient);
//        if (phoneNumbers != null && phoneNumbers.length > 0) {
//            sendRequest.addTo(phoneNumbers[0]);
//        }
//        ArrayList<PduPart> parts = myMessage.getPduParts();
//        final PduBody pduBody = new PduBody();

//            for(PduPart part : parts){
////                final PduPart partPdu = new PduPart();
////                partPdu.setName("image".getBytes());
////                partPdu.setContentType("image/png".getBytes());
////                partPdu.setData(image);
//                pduBody.addPart(part);
//            }
       // }

//        sendRequest.setBody(pduBody);
//    final PduComposer composer = new PduComposer(this.context, sendRequest);
//    final byte[] bytesToSend = composer.make();

//        smsManager.sendDataMessage(phoneNo,null,(short)2948,bytesToSend,null, null);
//        smsManager.sendMultimediaMessage(context,uri,null,null,null);
//
//    HttpUtils.httpConnection(context, 4444L, MMSCenterUrl,
//    bytesToSendFromPDU, HttpUtils.HTTP_POST_METHOD, !TextUtils
//            .isEmpty(MMSProxy), MMSProxy, port);

// Send a text based SMS
        //smsManager.sendMultimediaMessage();
        //SendReq request = new SendReq();
//        smsManager.sendDataMessage(phoneNo, null, port, smsBody, null, null);

    }

    protected void sendText(String message, String phoneNo){
       // if(message.length() > 338){

        phoneNo = trimPlusSignAndOne(phoneNo);
        MainActivity.setlblMessage(main, "Last message sent to " + phoneNo + " was:\n" + message);
      

            ArrayList<String> parts = new ArrayList<String>();
            if(phoneNo.isEmpty()){
                message = "Error:could not parse phone number";
                phoneNo = commanderAddress;
                if(phoneNo.isEmpty()){
                    Log.d("ERROR","Command address is not set");
                    return;
                }
            }
            
            
//            int cutoff = 160;
//            if(message.length() > cutoff) {
//
//
//                for (int i = 0; i < message.length(); i++) {
//                    if(cutoff*(i+1) >= message.length()){
//                        String mymessage = message.substring(i*cutoff, message.length());
//                        if(mymessage !=null && !mymessage.isEmpty()) {
//                            parts.add(mymessage);
//                        }
//                        break;
//                    }
//                    String mymessage =message.substring(cutoff*i, cutoff*(i+1)).trim();
//                    if(mymessage !=null && !mymessage.isEmpty()) {
//                        parts.add(mymessage);
//                    }
//
//                }
//            }
//            else{
//                parts.add(message);
//            }
//
//
//            try {
               SmsManager smsManager = SmsManager.getDefault();
        String SMS_SENT = "SMS_SENT";
        String SMS_DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(SMS_DELIVERED), 0);

        ArrayList<String> smsBodyParts = smsManager.divideMessage(message);
        //smsBodyParts = reverseArrayList(smsBodyParts);
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();

        for (int i = 0; i < smsBodyParts.size(); i++) {
            sentPendingIntents.add(sentPendingIntent);
            deliveredPendingIntents.add(deliveredPendingIntent);
        }

// For when the SMS has been sent
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_SENT));

// For when the SMS has been delivered
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SMS_DELIVERED));
        // Send a text based SMS

        smsManager.sendMultipartTextMessage(phoneNo, null, smsBodyParts, sentPendingIntents, deliveredPendingIntents);

// Send a text based SMS
       // SmsManager.sendMultipartTextMessage(phoneNo, null, smsBodyParts, sentPendingIntents, deliveredPendingIntents);
//                if(isMultipart){
//                    smsManager.sendMultipartTextMessage(phoneNo, null, parts, null, null);
//                    return;
//                }
//
//
//                for(int i = 0 ; i < parts.size(); i++){
//
//                       smsManager.sendTextMessage(phoneNo, null, parts.get(i), null, null);
//                        Thread.sleep(200);
//
//                }
//
//                
//            } catch (Exception e) {
//              
//                e.printStackTrace();
//            }


//        Uri smsUri = Uri.parse("tel:"+phoneNo);
//        Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
//        intent.putExtra("sms_body", message);
//        intent.setType("vnd.android-dir/mms-sms");
//        context.startActivity(intent);
//        Uri uri = Uri.parse("smsto:"+phoneNo);
//        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
//        it.putExtra("sms_body", message);
//        context.startActivity(it);

        }

    public ArrayList<String> reverseArrayList(ArrayList<String> alist)
    {
        // Arraylist for storing reversed elements
        ArrayList<String> revArrayList = new ArrayList<String>();
        for (int i = alist.size() - 1; i >= 0; i--) {

            // Append the elements in reverse order
            revArrayList.add(alist.get(i));
        }

        // Return the reversed arraylist
        return revArrayList;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private static byte[] createFakeSms(Context context, String sender,
                                      String body) {
        byte[] pdu = null;
        byte[] scBytes = PhoneNumberUtils
                .networkPortionToCalledPartyBCD("0000000000");
        byte[] senderBytes = PhoneNumberUtils
                .networkPortionToCalledPartyBCD(sender);
        int lsmcs = scBytes.length;
        byte[] dateBytes = new byte[7];
        Calendar calendar = new GregorianCalendar();
        dateBytes[0] = reverseByte((byte) (calendar.get(Calendar.YEAR)));
        dateBytes[1] = reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
        dateBytes[2] = reverseByte((byte) (calendar.get(Calendar.DAY_OF_MONTH)));
        dateBytes[3] = reverseByte((byte) (calendar.get(Calendar.HOUR_OF_DAY)));
        dateBytes[4] = reverseByte((byte) (calendar.get(Calendar.MINUTE)));
        dateBytes[5] = reverseByte((byte) (calendar.get(Calendar.SECOND)));
        dateBytes[6] = reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) + calendar
                .get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bo.write(lsmcs);
            bo.write(scBytes);
            bo.write(0x04);
            bo.write((byte) sender.length());
            bo.write(senderBytes);
            bo.write(0x00);
            bo.write(0x00); // encoding: 0 for default 7bit
            bo.write(dateBytes);
            try {
                String sReflectedClassName = "com.android.internal.telephony.GsmAlphabet";
                Class cReflectedNFCExtras = Class.forName(sReflectedClassName);
                Method stringToGsm7BitPacked = cReflectedNFCExtras.getMethod(
                        "stringToGsm7BitPacked", new Class[] { String.class });
                stringToGsm7BitPacked.setAccessible(true);
                byte[] bodybytes = (byte[]) stringToGsm7BitPacked.invoke(null,
                        body);
                bo.write(bodybytes);
            } catch (Exception e) {
            }

            pdu = bo.toByteArray();
        } catch (IOException e) {
        }
        return pdu;
//        Intent intent = new Intent();
//        intent.setClassName("com.android.mms",
//                "com.android.mms.transaction.SmsReceiverService");
//        intent.setAction("android.provider.Telephony.SMS_RECEIVED");
//        intent.putExtra("pdus", new Object[] { pdu });
//        intent.putExtra("format", "3gpp");
//        context.startService(intent);
    }

    private static byte reverseByte(byte b) {
        return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
    }

}
