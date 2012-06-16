package com.jessitron.tronsmit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TronsmitOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tronsmit.db";
    private static final int VERSION = 1;

    public TronsmitOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Button.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // nada
    }
}
