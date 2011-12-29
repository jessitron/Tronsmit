package com.jessitron.boyfriend.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TransmitOpenHelper extends SQLiteOpenHelper{

    public TransmitOpenHelper(Context context) {
        super(context, "jessitron-transmit", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Transmissions.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
       // nothing yet.
    }
}
