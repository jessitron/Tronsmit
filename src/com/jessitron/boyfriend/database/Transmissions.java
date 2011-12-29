package com.jessitron.boyfriend.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class Transmissions {
    
    public static void onCreate(SQLiteDatabase db) {
            db.execSQL("create table TRANSMISSIONS (_id integer primary key autoincrement," +
                    " picture_uri text not null, " +
                    " phone_number text, " +
                    " transmission_date text default current_timestamp" +
                    ")");
    }
                                      
    public void saveTransmission(SQLiteDatabase db, Uri pictureId, String phoneNumber) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("PICTURE_URI", pictureId.toString());
        contentValues.put("PHONE_NUMBER", phoneNumber);
        
        db.insert("TRANSMISSIONS", null, contentValues) ;
        
    }
    
    public Cursor retrieveTransmissions(SQLiteDatabase db) {
        return db.rawQuery("select _ID, PICTURE_URI, PHONE_NUMBER, TRANSMISSION_DATE from TRANSMISSIONS order by TRANSMISSION_DATE DESC", null);
    }
}
