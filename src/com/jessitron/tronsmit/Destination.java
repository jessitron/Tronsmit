package com.jessitron.tronsmit;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class Destination {

    private final Uri contactUrl;
    private String phoneNumber;
    private String email;
    private String name;

    public Destination(ContentResolver cr, Uri contactUri) {
        this.contactUrl = contactUri;

        long contactId = loadNameAndId(cr, contactUri);

        loadPhoneNumber(cr, contactId);

        loadEmail(cr, contactId);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    private long loadNameAndId(ContentResolver cr, Uri uri) {
        long contactId = -1;
        name = "Nobody";
        Cursor cursor = cr.query(uri,
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
        return contactId;
    }

    private void loadEmail(ContentResolver cr, long contactId) {
        email = null;
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.DATA1},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");
        try {
            if (cursor.moveToFirst()) {
                email = cursor.getString(0);
                say("Chose email: " + email);
            } else {
                say("Email not found");
            }
        } finally {
            cursor.close();
        }
    }

    private void loadPhoneNumber(ContentResolver cr, long contactId) {
        phoneNumber = null;
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");
        try {
            if (cursor.moveToFirst()) {
                phoneNumber = cursor.getString(0);
                say("Chose phone number: " + phoneNumber);
            } else {
                say("Phone number not found");
            }
        } finally {
            cursor.close();
        }
    }

    private void say(String s) {
        Log.d(TronsmitActivity.LOG_PREFIX, "JessiTRON! " + s);
    }
}
