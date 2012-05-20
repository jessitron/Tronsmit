package com.jessitron.transmit;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TransmitActivity extends Activity {
    private static final int PICK_CONTACT_REQUEST_CODE = 1;
    private static final String LOG_PREFIX = "TransmitActivity";

    private String phoneNumber;

    private PictureManager pictureManager;
    private static final int CHOOSE_INTENT_CODE = 2;
    private String email;
    private boolean htcDevice;
    private Button chooseActionButton;
    private String name;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        loadPreferences();

        examineDevice();

        pictureManager = new PictureManager((ImageView) findViewById(R.id.pictureView), this);
        pictureManager.reset();
        if (!pictureManager.hasPicture()) {
            disableAllButtons();
            toast("This app is useless without pictures");
        }
    }

    private void disableAllButtons() {
        LinearLayout container = (LinearLayout) findViewById(R.id.buttonContainer);
        for(int i = 0; i < container.getChildCount(); i++) {
            if (container.getChildAt(i) instanceof Button) {
                container.getChildAt(i).setEnabled(false);
            }
        }
    }

    private void examineDevice() {
        Intent htcSpecialSend = new Intent("android.intent.action.SEND_MSG");
        htcSpecialSend.setType("image/jpg");
        final List<ResolveInfo> htcSendResolution = getPackageManager().queryIntentActivities(htcSpecialSend, PackageManager.GET_INTENT_FILTERS | PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
        htcDevice = (htcSendResolution != null && !htcSendResolution.isEmpty());
        say("HTC device? " + htcDevice);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history:
                printStuff();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void printStuff() {
        Intent shareIntent = createSendIntent(Intent.ACTION_SEND);
        List<ResolveInfo> result = getPackageManager().queryIntentActivityOptions(null, null, shareIntent,
                PackageManager.MATCH_DEFAULT_ONLY & PackageManager.GET_INTENT_FILTERS & PackageManager.GET_RESOLVED_FILTER);

        say("Activities available for send: -----------------------  ");
        for (ResolveInfo resolveInfo : result) {
            say("Activity option: " + resolveInfo.toString());
            say("  " + resolveInfo.activityInfo.toString());
            say("  " + resolveInfo.activityInfo.packageName);
            say("  " + resolveInfo.activityInfo.name);
            say("  " + resolveInfo.filter);
            say("  " + resolveInfo.loadLabel(getPackageManager()));
        }

        final Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, createSendIntent(Intent.ACTION_SEND));
        startActivityForResult(pickIntent, CHOOSE_INTENT_CODE);

        //printInfoAboutAllApplications();
        //printInfoAboutAllPackages();

    }

    private Intent createSendIntent(String action) {
        Intent shareIntent = new Intent(action);
        shareIntent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureManager.getImageLocation());
        shareIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
        shareIntent.putExtra("address", phoneNumber);
        if (email != null) { shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {email}); }

        shareIntent.putExtra("sms_body", "sent by Transmitron");
        shareIntent.setType(pictureManager.getImageType());
        return shareIntent;
    }

    private void printInfoAboutAllApplications() {
        say("========================");

        for (ApplicationInfo applicationInfo : getPackageManager().getInstalledApplications(0)) {
            say("Application info: " + applicationInfo);
            say(applicationInfo.className);
        }
    }

    private void printInfoAboutAllPackages() {
        say("========================");

        for (PackageInfo packageInfo : getPackageManager().getInstalledPackages(0)) {
            say("Package info: " + packageInfo);
            say(packageInfo.packageName);
            if (packageInfo.activities != null) {
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    say("  " + activityInfo.name);
                    say("  " + activityInfo.loadLabel(getPackageManager()));
                }
            }
        }
    }

    public void pictureTouched(View v) {
        Toast.makeText(this, "you touched me", 3).show();
        pictureManager.advance();
    }

    public void sendPicture(View v) {
        Intent shareIntent = createSendIntent(Intent.ACTION_SEND);

        startActivity(shareIntent);
    }

    public void sendPictureHtc(View v) {
        Intent shareIntent = new Intent("android.intent.action.SEND_MSG");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureManager.getImageLocation());
        shareIntent.putExtra("address", phoneNumber);
        shareIntent.putExtra("sms_body", "sent by Transmit");
        shareIntent.setType(pictureManager.getImageType());

        startActivity(shareIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pictureManager.reset();
    }


    @Override
    protected void onPause() {
        super.onPause();
        savePreferences();
    }

    private void savePreferences() {
        getPreferences(MODE_PRIVATE).edit().putString("phoneNumber", phoneNumber).apply();
    }

    private void loadPreferences() {
        setPhoneNumber(getPhoneNumber());
    }

    private String getPhoneNumber() {
        return getPreferences(MODE_PRIVATE).getString("phoneNumber", "");
    }

    private void setPhoneNumber(String phoneNumber) {
        TextView phoneView = (TextView) findViewById(R.id.phoneNumber);
        phoneView.setText(phoneNumber);
        phoneView.invalidate();
        this.phoneNumber = phoneNumber;
    }

    public void pickContact(View v) {
        final Intent pickContactsIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        ResolveInfo resolved = getPackageManager().resolveActivity(pickContactsIntent, 0);
        say("pick contact resolves to: " + resolved.activityInfo.name);
        startActivityForResult(pickContactsIntent, PICK_CONTACT_REQUEST_CODE);
    }

    public void chooseAction(View v) {
        //final Intent pickActivityIntent = Intent.createChooser(createSendIntent(Intent.ACTION_SEND), "What should this button do?");
        final Intent pickActivityIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickActivityIntent.putExtra(Intent.EXTRA_INTENT, createSendIntent(Intent.ACTION_SEND));
        pickActivityIntent.putExtra(Intent.EXTRA_TITLE, "what should this button do?");
        chooseActionButton = (Button) v;
        startActivityForResult(pickActivityIntent, CHOOSE_INTENT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {
           if (resultCode == RESULT_OK) {
            gotAContact(data.getData());
           }
        } else if (requestCode == CHOOSE_INTENT_CODE && resultCode == RESULT_OK) {
            gotAnAction(data);
        }
    }

    private void gotAnAction(final Intent data) {
        // note: this might not be a default action. could be trouble.
        chooseActionButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityLike(data);
            }
        });
        // change the text on the button.
        ActivityInfo info = data.resolveActivityInfo(getPackageManager(), 0);
        chooseActionButton.setText("Send to " + info.loadLabel(getPackageManager()));
    }

    private void startActivityLike(final Intent data) {
        Intent send = createSendIntent(data.getAction());
        send.setComponent(data.getComponent());
        startActivity(send);
    }

    private void gotAContact(android.net.Uri uri) {

        long contactId = -1;

        // Load the display name for the specified person
        android.database.Cursor cursor = getContentResolver().query(uri,
                new String[]{ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                contactId = cursor.getLong(0);
                name = cursor.getString(1);
            } else {
                say("Contact not found");
            }
        } finally {
            cursor.close();
        }
        say("contactId is " + contactId);

        // Load the phone number (if any).
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");
        try {
            if (cursor.moveToFirst()) {
                setPhoneNumber(cursor.getString(0));
                say("Chose phone number: " + phoneNumber);
            } else {
                phoneNumber = null;
                say("Phone number not found");
            }
        } finally {
            cursor.close();
        }

        // look for an email
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.DATA1},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");
        try {
            if (cursor.moveToFirst()) {
               email = cursor.getString(0);
                say("Chose email: " + email);
            } else {
                email = null;
                say("Email not found");
            }
        } finally {
            cursor.close();
        }

    }


    private void say(String s) {
        Log.d(LOG_PREFIX, "JessiTRON! " + s);
    }

}
