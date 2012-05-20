package com.jessitron.transmit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class HtcMmsForwarder extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(getClass().getSimpleName(), "Forwarding to HTC send");

        Intent forwardTo = new Intent("android.intent.action.SEND_MSG");
        forwardTo.putExtras(getIntent());
        forwardTo.setType(getIntent().getType());
        startActivity(forwardTo);
        finish();
    }
}