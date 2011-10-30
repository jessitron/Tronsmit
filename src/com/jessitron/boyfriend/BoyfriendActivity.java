package com.jessitron.boyfriend;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import android.provider.ContactsContract;

public class BoyfriendActivity extends Activity
{
     private static final int PICK_CONTACT_REQUEST_CODE = 1;
     private static final String LOG_PREFIX = "BoyfriendActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

  Button pickContact = (Button) findViewById(R.id.pickContact);
  pickContact.setOnClickListener(getPickContactListener());
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
      protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	if (requestCode == PICK_CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {
	  gotAContact(data.getData());
	}

      }

    private void gotAContact(android.net.Uri uri) {  
      TextView phoneView = (TextView) findViewById(R.id.phoneNumber);

   long contactId = -1;

        // Load the display name for the specified person
        android.database.Cursor cursor = getContentResolver().query(uri,
                new String[]{ContactsContract.Contacts._ID,
		       	ContactsContract.Contacts.DISPLAY_NAME}, 
			null, null, null);
        try {
            if (cursor.moveToFirst()) {
                contactId = cursor.getLong(0);
            } 
	    else {
	    say("Contact not found");}
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
      phoneView.setText("" + cursor.getString(0));
            }
	    else {
	    say("Phone number not found");}
        } finally {
            cursor.close();
        }
      
    }
    


    private void say(String s)
    {
      Log.d(LOG_PREFIX, s);
    }

}
