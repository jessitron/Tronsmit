package com.jessitron.tronsmit.database;

import java.util.ArrayList;
import java.util.List;

import com.jessitron.tronsmit.Destination;
import com.jessitron.tronsmit.TronsmitActivity;
import com.jessitron.tronsmit.TronsmitApplication;

import android.content.ComponentName;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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

        public Iterable<ButtonConfig> getButtons() {

            final Cursor cursor = app.getDatabase().query(TABLE_NAME, new String[]{ID, COMPONENT_PACKAGE, COMPONENT_CLASS, CONTACT_URI}, null, null, null, null, ID);

            List<ButtonConfig> result = new ArrayList<ButtonConfig>(cursor.getCount());
            while(!cursor.isAfterLast()) {
                ComponentName component = new ComponentName(cursor.getString(1), cursor.getString(2));
                Destination destination = new Destination(app.getContentResolver(), Uri.parse(cursor.getString(3)));
                result.add(new ButtonConfig(component, destination));
            }
            return result;
        }
    }

    public static class ButtonConfig {
        public final ComponentName component;
        public final Destination destination;

        public ButtonConfig(ComponentName component, Destination destination) {
            this.component = component;
            this.destination = destination;
        }

        public ComponentName getComponent() {
            return component;
        }

        public Destination getDestination() {
            return destination;
        }
    }

}
