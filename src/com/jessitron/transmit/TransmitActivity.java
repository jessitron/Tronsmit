package com.jessitron.transmit;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TransmitActivity extends Activity {
    private static final int PICK_CONTACT_REQUEST_CODE = 1;
    private static final int CHOOSE_INTENT_CODE = 2;
    private static final int TAKE_PICTURE_CODE = 3;
    private static final String LOG_PREFIX = "TransmitActivity";

    private PictureManager pictureManager;

    private String phoneNumber;
    private String email;
    private String name;

    private Button chooseActionButton;
    private static final View.OnLongClickListener BUTTON_DELETING_LISTENER = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            ((ViewGroup)view.getParent()).removeView(view);
            return true;
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        say("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        loadPreferences();
        examineDevice();

        pictureManager = new PictureManager((ImageView) findViewById(R.id.pictureView), getApplicationContext());
        pictureManager.reset();
        if (!pictureManager.hasPicture()) {
            disableAllButtons();
            toast("This app is useless without pictures");
        }
    }

    private boolean checkForServiceIntentSupport(Intent intent) {
        List<ResolveInfo> result = getPackageManager().queryIntentServices(intent, 0);
        return (result != null && !result.isEmpty());
    }
    private boolean checkForActivityIntentSupport(Intent intent) {
        List<ResolveInfo> result = getPackageManager().queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS | PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
        return (result != null && !result.isEmpty());
    }

    private Intent createEditImageIntent() {
        Intent editIntent = new Intent(Intent.ACTION_EDIT, pictureManager.getImageLocation());
        editIntent.setType(pictureManager.getImageType());
        return editIntent;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setMenuItemEnablement(menu, R.id.flashy, checkForServiceIntentSupport(createFlashlightIntent()));
        setMenuItemEnablement(menu, R.id.editpic, checkForActivityIntentSupport(createEditImageIntent()));
        return super.onPrepareOptionsMenu(menu);
    }


    private Intent createSendIntent(String action) {
        Intent shareIntent = new Intent(action);
        shareIntent.setType(pictureManager.getImageType());
        shareIntent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);

        // everything needs the data
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureManager.getImageLocation());

        // MMS on non-HTC
        shareIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);

        // HTC
        shareIntent.putExtra("address", phoneNumber);
        shareIntent.putExtra("sms_body", R.string.attribution);

        // Email
        if (email != null) {
            shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "sent by Tronsmit");

        return shareIntent;
    }


    public void sendPicture(View v) {
        Intent shareIntent = createSendIntent(Intent.ACTION_SEND);

        startActivity(shareIntent);
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
        pickActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
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
                savePreferences(data.getData());
            }
        } else if (requestCode == CHOOSE_INTENT_CODE && resultCode == RESULT_OK) {
            gotAnAction(data);
            addAbutton();
        } else if (requestCode == TAKE_PICTURE_CODE && resultCode == RESULT_OK) {
            pictureManager.reset(); // find the new picture
        }
    }

    private void addAbutton() {
        final LinearLayout buttonContainer = findButtonContainer();
        final Button newButton = new Button(this);
        newButton.setText("Choose an action");
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseAction(view);
            }
        });
        newButton.setOnLongClickListener(BUTTON_DELETING_LISTENER);
        buttonContainer.addView(newButton);
    }

    private void gotAnAction(final Intent data) {
        // note: this might not be a default action. could be trouble.
        chooseActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityLike(data);
            }
        });
        ActivityInfo info = data.resolveActivityInfo(getPackageManager(), 0);
        chooseActionButton.setText("Send to " + info.loadLabel(getPackageManager()));
    }

    private void startActivityLike(final Intent data) {
        Intent send = createSendIntent(data.getAction());
        send.setComponent(data.getComponent());
        startActivity(send);
    }

    private void gotAContact(android.net.Uri uri) {

        long contactId = loadNameAndId(uri);

        loadPhoneNumber(contactId);

        loadEmail(contactId);

        updateContactDescription();
    }

    private long loadNameAndId(Uri uri) {
        long contactId = -1;
        name = "Nobody";
        Cursor cursor = getContentResolver().query(uri,
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

    private void loadEmail(long contactId) {
        email = null;
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
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

    private void loadPhoneNumber(long contactId) {
        phoneNumber = null;
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.printstuff:
                printStuff();
                return true;
            case R.id.dial:
                dial();
                return true;
            case R.id.camera:
                takePicture();
                return true;
            case R.id.flashy:
                flashSomeLights();
                return true;
            case R.id.editpic:
                editPicture();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editPicture() {
        startActivity(createEditImageIntent());
    }

    private void takePicture() {
       Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(pictureIntent, TAKE_PICTURE_CODE);
    }

    private void flashSomeLights() {
        Intent intent = createFlashlightIntent();
        startService(intent);
    }

    private Intent createFlashlightIntent() {
        Intent intent = new Intent("com.teslacoilsw.intent.FLASHLIGHT");
        intent.putExtra("strobe", 10);
        intent.putExtra("timeout", 5);
        return intent;
    }

    private void dial() {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber) );
        dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialIntent);
    }

    private LinearLayout findButtonContainer() {
        return (LinearLayout) findViewById(R.id.buttonContainer);
    }

    public void sendPictureHtc(View v) {
        Intent shareIntent = new Intent("android.intent.action.SEND_MSG");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureManager.getImageLocation());
        shareIntent.putExtra("address", phoneNumber);
        shareIntent.putExtra("sms_body", "sent by Transmit");
        shareIntent.setType(pictureManager.getImageType());

        startActivity(shareIntent);
    }

    private void say(String s) {
        Log.d(LOG_PREFIX, "JessiTRON! " + s);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
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


    @Override
    protected void onResume() {
        super.onResume();
        say("onResume called");
        pictureManager.reset();
    }

    @Override
    protected void onStop() {
        say("onStop");
        super.onStop();    
    }

    @Override
    protected void onDestroy() {
        pictureManager.shutDown();
        say("onDestroy");
        super.onDestroy();
        unbindDrawables(findViewById(R.id.rootContainer));
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    @Override
    protected void onPause() {
        say("onPause");
        super.onPause();    
    }

    @Override
    protected void onUserLeaveHint() {
        say("onUserLeaveHint");
        super.onUserLeaveHint();    
    }

    @Override
    protected void onPostResume() {
        say("onPostResume");
        super.onPostResume();    
    }

    @Override
    protected void onRestart() {
        say("onRestart");
        super.onRestart();    
    }

    @Override
    protected void onStart() {
        say("onStart");
        super.onStart();    
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        say("onConfigChanged");
        super.onConfigurationChanged(newConfig);    
    }

    @Override
    public void onBackPressed() {
        say("onBackPressed");
        super.onBackPressed();    
    }

    private void examineDevice() {
        remarkAboutHtc();
    }

    private void remarkAboutHtc() {
        Intent htcSpecialSend = new Intent("android.intent.action.SEND_MSG");
        htcSpecialSend.setType("image/jpeg");
        boolean htcDevice = checkForActivityIntentSupport(htcSpecialSend);
        say("HTC device? " + htcDevice);
    }

    private void disableAllButtons() {
        LinearLayout container = findButtonContainer();
        for (int i = 0; i < container.getChildCount(); i++) {
            if (container.getChildAt(i) instanceof Button) {
                container.getChildAt(i).setEnabled(false);
            }
        }
    }


    private void setMenuItemEnablement(Menu menu, int menuItemId, boolean enabled) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == menuItemId) {
                menu.getItem(i).setEnabled(enabled);
            }
        }
    }

    public void pictureTouched(View v) {
        //Toast.makeText(this, "you touched me", 3).show();
        pictureManager.advance();
    }



    private void savePreferences(Uri contactUri) {
        getPreferences(MODE_PRIVATE).edit().putString("contactUri", contactUri.toString()).apply();
    }

    private void loadPreferences() {
        String uriString = getPreferences(MODE_PRIVATE).getString("contactUri", "");
        if ("" != uriString) {
            gotAContact(Uri.parse(uriString));
        }
        updateContactDescription();
    }

    private void updateContactDescription() {
        TextView contactDescription = (TextView) findViewById(R.id.contactName);
        contactDescription.setText(name);
        contactDescription.invalidate();
    }
}
