package com.jessitron.tronsmit;

import com.jessitron.tronsmit.database.TronsmitOpenHelper;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class TronsmitApplication extends Application {

    private SQLiteDatabase database;

    @Override
    public void onCreate() {
        database = new TronsmitOpenHelper(this).getWritableDatabase();
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }
}
