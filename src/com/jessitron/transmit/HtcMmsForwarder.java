package com.jessitron.transmit;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

public class HtcMmsForwarder extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(getClass().getSimpleName(), "Forwarding to HTC send");

        Intent forwardTo = new Intent("android.intent.action.SEND_MSG");
        forwardTo.putExtras(getIntent());
        forwardTo.setType(getIntent().getType());

        if (checkForActivityIntentSupport(forwardTo)) {
            startActivity(forwardTo);
        } else {
            Intent ordinaryMessagingIntent = new Intent(Intent.ACTION_SEND);
            ordinaryMessagingIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
            ordinaryMessagingIntent.setType(getIntent().getType());
            ordinaryMessagingIntent.putExtras(getIntent());
            startActivity(ordinaryMessagingIntent);
        }


//        try {
//        startActivity(forwardTo);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(getApplicationContext(), "This does not appear to be an HTC phone", Toast.LENGTH_LONG).show();
//        }
        finish();
    }

    private boolean checkForActivityIntentSupport(Intent intent) {
        List<ResolveInfo> result = getPackageManager().queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS | PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
        return (result != null && !result.isEmpty());
    }
}