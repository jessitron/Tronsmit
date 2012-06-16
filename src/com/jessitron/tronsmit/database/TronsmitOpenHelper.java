package com.jessitron.tronsmit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TronsmitOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tronsmit.db";
    private static final int VERSION = 1;

    public TronsmitOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        throw new UnsupportedOperationException();
    }
}
