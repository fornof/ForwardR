package com.example.rob.myapplication;

import android.provider.BaseColumns;

public final class SmsReaderContract {

        // To prevent someone from accidentally instantiating the contract class,
        // make the constructor private.
        private SmsReaderContract() {}

        /* Inner class that defines the table contents */
        public static class SettingsEntry implements BaseColumns {
            public static final String TABLE_NAME = "user";
            public static final String _ID = "_id";
            public static final String COLUMN_NAME_USR_COMMAND_ADDRESS = "usr_command_address";
            public static final String COLUMN_NAME_USR_SPECIAL_CHAR_1 = "usr_special_char_1";
            public static final String COLUMN_NAME_USR_SPECIAL_CHAR_2 = "usr_special_char_2";
            public static final String COLUMN_NAME_USR_LAST_INDEX ="usr_last_index";
        }
    /* Inner class that defines the table contents */
    public static class PhoneEntry implements BaseColumns {
        public static final String TABLE_NAME = "phone";
        public static final String _ID = "_id";
        public static final String COLUMN_NAME_PN_NUMBER = "pn_number";
        public static final String COLUMN_NAME_PN_NAME = "pn_name";
        public static final String COLUMN_NAME_PN_NICKNAME = "pn_nickname";
    }
    public static final String SETTINGS_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + SettingsEntry.TABLE_NAME + " (" +
                    SettingsEntry._ID + " INTEGER PRIMARY KEY," +
                    SettingsEntry.COLUMN_NAME_USR_COMMAND_ADDRESS + " TEXT," +
                    SettingsEntry.COLUMN_NAME_USR_SPECIAL_CHAR_1 + " TEXT," +
                    SettingsEntry.COLUMN_NAME_USR_LAST_INDEX + " INTEGER," +
                    SettingsEntry.COLUMN_NAME_USR_SPECIAL_CHAR_2 + "TEXT )";

    public static final String SETTINGS_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SettingsEntry.TABLE_NAME;

    public static final String PHONE_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + PhoneEntry.TABLE_NAME + " (" +
                    PhoneEntry._ID + " INTEGER PRIMARY KEY," +
                    PhoneEntry.COLUMN_NAME_PN_NUMBER + " TEXT," +
                    PhoneEntry.COLUMN_NAME_PN_NAME + " TEXT," +
                    PhoneEntry.COLUMN_NAME_PN_NICKNAME + "TEXT )";

    public static final String PHONE_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PhoneEntry.TABLE_NAME;

}

