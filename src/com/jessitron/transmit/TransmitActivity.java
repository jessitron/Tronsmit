package com.jessitron.transmit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TransmitActivity extends Activity {
    private static final int PICK_CONTACT_REQUEST_CODE = 1;
    private static final String LOG_PREFIX = "TransmitActivity";

    private String phoneNumber;
    
    private PictureManager pictureManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        loadPreferences();
        Button pickContact = (Button) findViewById(R.id.pickContact);
        pickContact.setOnClickListener(getPickContactListener());
        
        pictureManager = new PictureManager((ImageView) findViewById(R.id.pictureView), this); 
        pictureManager.reset();

        if (getPhoneNumber() != null) {
            activateTransmitButton();
        } else {
            deactivateTransmitButton();
            say("transmit not activated because phone number unknown");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.history:
               // TODO: make add button option instead
               return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deactivateTransmitButton() {
        final Button transmitButton = (Button) findViewById(R.id.transmitXX);
        transmitButton.setEnabled(false);
    }

    private void activateTransmitButton() {
        final Button transmitButton = (Button) findViewById(R.id.transmitXX);
        transmitButton.setEnabled(true);
        transmitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendPictureHtc();
            }
        });
    }

    private void sendPicture() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureManager.getImageLocation());
        shareIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
        shareIntent.setType(pictureManager.getImageType());

        startActivity(shareIntent);
    }

    private void sendPictureHtc() {
        Intent shareIntent = new Intent("android.intent.action.SEND_MSG");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureManager.getImageLocation());
        shareIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
        shareIntent.putExtra("address", phoneNumber);
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
        getPreferences(MODE_PRIVATE).edit().putString("phoneNumber", phoneNumber).commit();
        // apply would be better than commit (doesn't block for disk i/o) but that's API-9
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

    public View.OnClickListener getPickContactListener() {
        return new View.OnClickListener() {
            public void onClick(View v) {
                final Intent pickContactsIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(pickContactsIntent, PICK_CONTACT_REQUEST_CODE);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {
            gotAContact(data.getData());
        }
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
            } else {
                say("Phone number not found");
            }
        } finally {
            cursor.close();
        }

    }


    private void say(String s) {
        Log.d(LOG_PREFIX, "JessiTRON! " + s);
    }

}
