package com.jessitron.transmit.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class Transmissions {

    public static final String ID = "_id";
    public static final String PICTURE_URI = "picture_uri";
    public static final String TRANSMISSION_DATE = "transmission_date";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String TABLE = "TRANSMISSIONS";
    
    private static final String[] ALL_COLUMNS = { ID, PICTURE_URI, TRANSMISSION_DATE, PHONE_NUMBER};

    public static void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE +" ( " + ID + " integer primary key autoincrement, " +
                    PICTURE_URI + " text not null, " +
                    PHONE_NUMBER + " text, " +
                    TRANSMISSION_DATE + " text default current_timestamp" +
                    ")");
    }
                                      
    public void saveTransmission(SQLiteDatabase db, Uri pictureId, String phoneNumber) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(PICTURE_URI, pictureId.toString());
        contentValues.put(PHONE_NUMBER, phoneNumber);
        
        db.insert(TABLE, null, contentValues) ;
        
    }
    
    public Cursor retrieveTransmissions(SQLiteDatabase db) {
       // return db.rawQuery("select _ID, PICTURE_URI, PHONE_NUMBER, " + TRANSMISSION_DATE + " from TRANSMISSIONS order by " + TRANSMISSION_DATE + " DESC", null);
        
        Cursor stuff = db.query(TABLE, ALL_COLUMNS, null, null, null, null, TRANSMISSION_DATE);
        
         stuff.moveToFirst();
        int i = stuff.getColumnIndex("_id");
        if (i != 0 ) {
            throw new RuntimeException("Dammit, where is that stupid column? " + i);
        }
        return stuff;
    }
}
