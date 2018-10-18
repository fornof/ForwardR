package com.example.rob.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;

public class SettingDB {
    public static Setting getCurrentSetting(SQLiteDatabase db){



        String[] projection = {
                BaseColumns._ID,
                SmsReaderContract.SettingsEntry.COLUMN_NAME_USR_COMMAND_ADDRESS,
                SmsReaderContract.SettingsEntry.COLUMN_NAME_USR_LAST_INDEX,

        };

// Filter results WHERE "title" = 'My Title'
        //String selection = SmsReaderContract.PhoneEntry._ID + " = ?";
        ///String[] selectionArgs = { id +"" };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                SmsReaderContract.SettingsEntry._ID + " DESC";
        Cursor cursor;

        ArrayList<Setting> itemIds;
        try {
            cursor = db.query(
                    SmsReaderContract.SettingsEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
        }catch(Exception ex){
            return null;
        }
        try {
            itemIds = new ArrayList<Setting>();
            while (cursor.moveToNext()) {
                Setting setting = new Setting();
                setting.id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(SmsReaderContract.PhoneEntry._ID));
                setting.commanderAddress = cursor.getString(
                        cursor.getColumnIndexOrThrow(SmsReaderContract.SettingsEntry.COLUMN_NAME_USR_COMMAND_ADDRESS));
                setting.lastIndex = cursor.getInt(cursor.getColumnIndexOrThrow(SmsReaderContract.SettingsEntry.COLUMN_NAME_USR_LAST_INDEX));
                //setting.special1=cursor.getString(
                //    cursor.getColumnIndexOrThrow(SmsReaderContract.SettingsEntry.COLUMN_NAME_USR_SPECIAL_CHAR_1));
                //setting.special2=cursor.getString(
                //   cursor.getColumnIndexOrThrow(SmsReaderContract.SettingsEntry.COLUMN_NAME_USR_SPECIAL_CHAR_2));
                itemIds.add(setting);
            }
        }
        catch(Exception ex){
            cursor.close();
            return null;
        }


        cursor.close();
        if(itemIds.size() == 0){

            return null;
        }
        Setting item = (Setting)itemIds.get(0);
        return item;
    }

    public static void insertSettings(Setting setting, SQLiteDatabase db){
        // todo : put more stuff in here
        ContentValues values = new ContentValues();
        values.put(SmsReaderContract.SettingsEntry.COLUMN_NAME_USR_COMMAND_ADDRESS, setting.commanderAddress);
        if(getCurrentSetting(db) == null){
            db.insert(SmsReaderContract.SettingsEntry.TABLE_NAME,null, values);
        }
        else{
            db.update(SmsReaderContract.SettingsEntry.TABLE_NAME,values, SmsReaderContract.SettingsEntry._ID+" = ?", new String[] {"(SELECT MAX(ID)  FROM TABLE)"});
        }


    }
    public static void setLastIndex(int id, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SmsReaderContract.SettingsEntry.COLUMN_NAME_USR_LAST_INDEX, id);
        db.update(SmsReaderContract.SettingsEntry.TABLE_NAME,values, null, null);
    }
}
