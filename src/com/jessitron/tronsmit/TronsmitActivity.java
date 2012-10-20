package com.jessitron.tronsmit;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class TronsmitActivity extends Activity {
    private static final int REQUEST_CODE_TAKE_PICTURE = 3;
    public static final String LOG_PREFIX = "TronsmitActivity";

    private PictureFragment pictureFragment;
    private ButtonsFragment buttonsFragment;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        say("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pictureFragment = (PictureFragment)
                getFragmentManager().findFragmentById(R.id.pictureFragment);
        buttonsFragment = (ButtonsFragment)
                getFragmentManager().findFragmentById(R.id.buttonsFragment);
    }


    /*
     * Tronsmission
     */
    public Intent createSendIntent() {
        return SendIntentCreator.createSendIntent(getString(R.string.attribution), buttonsFragment.getDestination(), pictureFragment.getPicInfo());
    }

    public void tronsmit(View v) {
        Intent shareIntent = createSendIntent();

        startActivity(shareIntent);
    }

    /*
    * Menu
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setMenuItemEnablement(menu, R.id.flashy, isServiceSupported(createFlashlightIntent()));
        setMenuItemEnablement(menu, R.id.editpic, isActivitySupported(createEditImageIntent()));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera:
                takePicture();
                return true;
            case R.id.printstuff:
                printStuff();
                return true;
            case R.id.editpic:
                editPicture();
                return true;
            case R.id.reset:
                resetButtons();
                return true;
            case R.id.flashy:
                flashSomeLights();
                return true;
            case R.id.dial:
                dial();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isServiceSupported(Intent intent) {
        List<ResolveInfo> result = getPackageManager().queryIntentServices(intent, 0);
        return (result != null && !result.isEmpty());
    }

    private boolean isActivitySupported(Intent intent) {
        List<ResolveInfo> result = getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY
                        | PackageManager.GET_RESOLVED_FILTER
                        | PackageManager.GET_INTENT_FILTERS);
        return (result != null && !result.isEmpty());
    }

    private void setMenuItemEnablement(Menu menu, int menuItemId, boolean enabled) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == menuItemId) {
                menu.getItem(i).setEnabled(enabled);
            }
        }
    }

    private Intent createEditImageIntent() {
        Intent editIntent = new Intent(Intent.ACTION_EDIT, pictureFragment.getPictureManager().getImageLocation());
        editIntent.setType(pictureFragment.getPictureManager().getImageType());
        return editIntent;
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    final Intent data) {

        if (requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == RESULT_OK) {
            pictureFragment.getPictureManager().reset(); // find the new picture
        } else super.onActivityResult(requestCode, resultCode, data);
    }


    private void editPicture() {
        startActivity(createEditImageIntent());
    }

    private void takePicture() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(pictureIntent, REQUEST_CODE_TAKE_PICTURE);
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
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + buttonsFragment.getDestination().getPhoneNumber()));
        dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialIntent);
    }

    /*
     * Passing messages to fragments
     */
    public void reallyDeletePicture() {
        pictureFragment.deleteCurrentPicture();
    }

    private void resetButtons() {
        buttonsFragment.resetButtons();
        pictureFragment.reset();
    }

    public void noPictureAvailable() {
        buttonsFragment.disableAllButtons();
    }

    public void pictureChanged() {
        buttonsFragment.resetButtonColors();
    }

    public void buttonsFragment_pickContact(View v) {
        buttonsFragment.pickContact(v);
    }

    public void buttonsFragment_chooseAction(View v) {
        buttonsFragment.chooseAction(v);
    }

    // a non-constant picture info.
    public PictureKnowerAbouter latestPictureInfo() {
        return new PictureKnowerAbouter() {
            @Override
            public String getImageType() {
                return pictureFragment.getPicInfo().getImageType();
            }

            @Override
            public Uri getImageLocation() {
                return pictureFragment.getPicInfo().getImageLocation();
            }
        };
    }
    /*
    * Logging utilities. This is a demo app, after all.
    */
    public static void say(String s) {
        Log.d(LOG_PREFIX, "Tronsmit says: " + s);
    }

    public static final int PACKAGE_MANAGER_GET_INFO_FLAGS = PackageManager.GET_ACTIVITIES
            | PackageManager.GET_INTENT_FILTERS
            | PackageManager.GET_CONFIGURATIONS
            | PackageManager.GET_META_DATA;

    private void printStuff() {
        List<ResolveInfo> result = getPackageManager().queryIntentActivityOptions(null, null, createSendIntent(),
                PackageManager.MATCH_DEFAULT_ONLY | PACKAGE_MANAGER_GET_INFO_FLAGS | PackageManager.GET_RESOLVED_FILTER);

        say("Activities available for send: -----------------------  ");
        for (ResolveInfo resolveInfo : result) {
            say("Activity option: " + resolveInfo.toString());
            say("  " + resolveInfo.activityInfo.toString());
            say("  " + resolveInfo.activityInfo.packageName);
            say("  " + resolveInfo.activityInfo.name);
            say("  " + resolveInfo.filter);
            say("  " + resolveInfo.loadLabel(getPackageManager()));
        }

        printInfoAboutAllApplications();
        printInfoAboutAllPackages();

    }


    private void printInfoAboutAllApplications() {
        say("========================");

        for (ApplicationInfo applicationInfo : getPackageManager().getInstalledApplications(PACKAGE_MANAGER_GET_INFO_FLAGS)) {
            say("Application info: " + applicationInfo);
            say(applicationInfo.packageName);
        }
    }

    private void printInfoAboutAllPackages() { // This is more useful than the applications. PackageInfo has an ApplicationInfo, and it has a list of the activities.
        say("========================");

        final List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(
                PackageManager.GET_ACTIVITIES
                        | PackageManager.GET_INTENT_FILTERS
                        | PackageManager.GET_CONFIGURATIONS
                        | PackageManager.GET_META_DATA);

        for (PackageInfo packageInfo : installedPackages) {
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

    }

    @Override
    protected void onStop() {
        say("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        say("onDestroy");
        super.onDestroy();
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




}
