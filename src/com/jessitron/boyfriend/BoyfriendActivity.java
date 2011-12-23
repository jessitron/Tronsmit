package com.jessitron.boyfriend;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static android.provider.MediaStore.Images.ImageColumns.DATE_TAKEN;

public class BoyfriendActivity extends Activity {
    private static final int PICK_CONTACT_REQUEST_CODE = 1;
    private static final String LOG_PREFIX = "BoyfriendActivity";

    private String phoneNumber;
    private String imageLocation;
    private String imageType;

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

        doSomethingWithPictures();
        if (getPhoneNumber() != null) {
            activateTransmitButton();
        } else {
            say("transmit not activated because phone number unknown");
        }
    }

    private void activateTransmitButton() {
        final Button transmitButton = (Button) findViewById(R.id.transmitXX);
        transmitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                say("About to send a text message to " + getPhoneNumber());
//                SmsManager.getDefault().sendTextMessage(getPhoneNumber(), null, "Testing 1 2 3. tell me if you get this", null, null);
//                say("Well, I think I sent a text message to " + getPhoneNumber());
                attemptToSendPicture();
            }
        });
    }

    private void attemptToSendPicture() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    //    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "what does this do?");
     //   shareIntent.putExtra(Intent.EXTRA_TEXT, "If this works, ya gotta tell me about it");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageLocation));
        shareIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
      //  shareIntent.putExtra(Intent.EXTRA_EMAIL, phoneNumber);
        shareIntent.setType(imageType);


        final ResolveInfo resolveInfo = getPackageManager().resolveActivity(shareIntent, 0);
        say("resolveInfo " + resolveInfo);
        say("activity info: " + resolveInfo.activityInfo);
        say(" wish this were a string "); resolveInfo.activityInfo.describeContents();

        say("it is named " + resolveInfo.activityInfo.name);
        // startActivity(shareIntent);
    }

    private void doSomethingWithPictures() {
        //final ContentProviderClient contentProviderClient = getContentResolver().acquireContentProviderClient(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, DATE_TAKEN + " DESC");

        if (cursor.moveToFirst()) {
            final String bucket = cursor.getString(2);
            say("The bucket of the first one is " + bucket);
            final ImageView imageView = (ImageView) findViewById(R.id.pictureView);
            imageLocation = "file://" + cursor.getString(1);
            say("Image located at: " + imageLocation);
            putPicInView(imageView);

            imageType = cursor.getString(4);
            say("The type of this image is " + imageType);
        } else {
            // freak out!
            say("No pictures found at all");
        }
    }

    private void putPicInView(ImageView imageView) {
         imageView.setImageURI(Uri.parse(imageLocation));
//        File imageFile = new File(imageLocation);
//        if (imageFile.exists()) {   // TODO: is there a better way to do this?
//            Bitmap bm = BitmapFactory.decodeFile(imageLocation);
//            imageView.setImageBitmap(bm);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePreferences();
    }

    private void savePreferences() {
        getPreferences(MODE_PRIVATE).edit().putString("phoneNumber", phoneNumber).commit();
        // apply would be better (doesn't block for disk i/o) but that's API-9
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
