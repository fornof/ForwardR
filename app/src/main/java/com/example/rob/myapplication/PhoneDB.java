package com.example.rob.myapplication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class PhoneDB {
    public static String getContactName(Context context, String phoneNumber) {

        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    public static String getContactNumber( Context context, String displayName) {

        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(displayName));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.NUMBER}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.NUMBER));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
    public static Message getMessage(Context context, String phoneNumber){
        ArrayList<Message> result =getMessage(context, phoneNumber, 1);
        if( result != null){
            return result.get(0);
        }
        return null;
    }
    public static ArrayList<Message> getMessage(Context context, String phoneNumber, int msgCount){
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
        String[] projection = { "address", "body, date"};
        Cursor cursor = context.getContentResolver().query(mSmsinboxQueryUri,
                new String[] { "_id", "thread_id", "address", "date", "body",
                        "type" },
                "address LIKE ?",
                new String[]{"%" + phoneNumber},
                "_id DESC LIMIT " + msgCount);
        String body = "";
        ArrayList<Message> messages = new ArrayList<Message>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Message msg = new Message();
                msg.setMessage( cursor.getString(cursor.getColumnIndex("body")));
                msg.setFromAddress(cursor.getString(cursor.getColumnIndex("address")));
                messages.add(msg);
                //Log.d("Registration", body);
            } while (cursor.moveToNext());
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if(messages.isEmpty()){

            return null;
        }


        return messages;
    }
    protected static long addPhone(Context context , String phone, SQLiteDatabase db){

        ContentValues values = new ContentValues();
        values.put(SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER, phone);
        values.put(SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME, PhoneDB.getContactName( context,phone) );
        long newRowId = db.insert(SmsReaderContract.PhoneEntry.TABLE_NAME, null, values);
        return newRowId;
    }

    protected static Integer deletePhoneById(String id, SQLiteDatabase db) {
        String whereClause = SmsReaderContract.PhoneEntry._ID +" = ?";
        String[] whereValues = {id +""};
        return db.delete(SmsReaderContract.PhoneEntry.TABLE_NAME,whereClause, whereValues);
    }

    public static void createIfNotExists(SQLiteDatabase db){
        try {
            db.execSQL(SmsReaderContract.PHONE_CREATE_ENTRIES);
            db.execSQL(SmsReaderContract.SETTINGS_CREATE_ENTRIES);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
    public static ArrayList<Phone> listOfAllPhone(SQLiteDatabase db){
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
            phone.name=cursor.getString(
                    cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME));
            itemIds.add(phone);
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return itemIds;
    }

    public static Integer indexOfPhone(String phone,SQLiteDatabase db, boolean countResults){

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

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if(countResults){
            return itemIds.size();
        }

        if(itemIds.size() == 0){
            return -1;
        }
        int item = (int)itemIds.get(0);
        return item;
    }


    protected static boolean setPhone(int indexValue,String columnNamePnName,  String value, SQLiteDatabase db) {
        if(value == null){
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(columnNamePnName, value);
        String whereClause =   SmsReaderContract.PhoneEntry._ID +" = ?";
        String[] whereValues = {indexValue +""};
        //update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        db.update(SmsReaderContract.PhoneEntry.TABLE_NAME, values, whereClause, whereValues);
        return true;
    }

    public static Phone getPhoneById(String column, int value,SQLiteDatabase db ){


        String[] projection = {
                BaseColumns._ID,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME
        };

// Filter results WHERE "title" = 'My Title'
        String selection = column + " = ?";
        String[] selectionArgs = { value +"" };

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
            phone.name=cursor.getString(
                    cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME));
            itemIds.add(phone);
        }




        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if(itemIds.size() == 0){

            return null;
        }
        Phone item = (Phone)itemIds.get(0);
        return item;
    }

    public static Phone getPhoneById( int id,SQLiteDatabase db ){


        String[] projection = {
                BaseColumns._ID,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NUMBER,
                SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME
        };

// Filter results WHERE "title" = 'My Title'
        String selection = SmsReaderContract.PhoneEntry._ID + " = ?";
        String[] selectionArgs = { id +"" };

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
            phone.name=cursor.getString(
                    cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry.COLUMN_NAME_PN_NAME));
            itemIds.add(phone);
        }




        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if(itemIds.size() == 0){

            return null;
        }
        Phone item = (Phone)itemIds.get(0);
        return item;
    }

}
