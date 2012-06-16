package com.jessitron.tronsmit.database;

import com.jessitron.tronsmit.Destination;
import com.jessitron.tronsmit.TronsmitActivity;
import com.jessitron.tronsmit.TronsmitApplication;

import android.content.ComponentName;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Button {

    private static final String TABLE_NAME = "BUTTON";
    public static final String ID = "_id";
    public static final String COMPONENT_PACKAGE = "COMPONENT_PACKAGE";
    public static final String COMPONENT_CLASS = "COMPONENT_CLASS";
    public static final String CONTACT_URI = "CONTACT_URI";

    static void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + ID + " integer primary key autoincrement , " +
                " " + COMPONENT_PACKAGE + " text not null , " +
                " " + COMPONENT_CLASS + " text not null ," +
                " " + CONTACT_URI + " text not null)");
    }

    public static class Helper {

        private final TronsmitApplication app;

        public Helper(TronsmitApplication app) {
            this.app = app;
        }

        public long store(ComponentName component, Destination destination) {
            long id;
            ContentValues cv = new ContentValues();
            cv.put(COMPONENT_PACKAGE, component.getPackageName());
            cv.put(COMPONENT_CLASS, component.getClassName());
            cv.put(CONTACT_URI, destination.getContactUri().toString());


            final SQLiteDatabase db = app.getDatabase();
            db.beginTransaction();
            try {
                id = db.insert(TABLE_NAME, null, cv);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            Log.d(TronsmitActivity.LOG_PREFIX, "Created new game with ID: " + id);

            return id;
        }

    }

}
