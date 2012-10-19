package com.jessitron.tronsmit;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jessitron.tronsmit.database.Button;

public class ButtonsFragment extends Fragment {

    private static final int REQUEST_CODE_PICK_CONTACT = 1;
    private static final int REQUEST_CODE_CHOOSE_INTENT = 2;

    private static final View.OnLongClickListener BUTTON_DELETING_LISTENER = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            ((ViewGroup) view.getParent()).removeView(view);
            return true;
        }
    };

    private com.jessitron.tronsmit.database.Button.Helper buttonHelper;

    private Destination destination;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.buttons_fragment, container, false) ;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        loadPreferences();
        buttonHelper = new com.jessitron.tronsmit.database.Button.Helper( getApplicationContext());
        createButtons();
        super.onActivityCreated(savedInstanceState);
    }

    public void pickContact(View v) {
        final Intent pickContactsIntent =
                new Intent(
                        Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(
                pickContactsIntent,
                REQUEST_CODE_PICK_CONTACT);

    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 final Intent data) {

        if (requestCode == REQUEST_CODE_PICK_CONTACT
                && resultCode == Activity.RESULT_OK) {
            gotAContact(data.getData());
            savePreferences(data.getData());

        } else if (requestCode == REQUEST_CODE_CHOOSE_INTENT && resultCode == Activity.RESULT_OK) {
            gotAnAction(data, destination);
        }  else super.onActivityResult(requestCode, resultCode, data);
    }

    public TronsmitApplication getApplicationContext() {
        return (TronsmitApplication) getActivity().getApplicationContext();
    }

    public TronsmitActivity getTronsmitActivity() {
            return (TronsmitActivity) getActivity();
    }

    private void createButtons() {
        for (Button.ButtonConfig buttonConfig : buttonHelper.getButtons()) {
            addButtonFor(createIntentFrom(buttonConfig.component), buttonConfig.destination);
        }
    }

    public void resetButtons() {
        findButtonContainer().removeAllViews();
        buttonHelper.deleteAll();
    }

    private void savePreferences(Uri contactUri) {
        getPreferences().edit().putString("contactUri", contactUri.toString()).apply();
    }

    private SharedPreferences getPreferences() {
        return getActivity().getPreferences(Activity.MODE_PRIVATE);
    }

    private void loadPreferences() {
        String uriString = getPreferences().getString("contactUri", "");
        if ("" != uriString) {
            gotAContact(Uri.parse(uriString));
            updateContactDescription();
        }
    }

    private void gotAContact(android.net.Uri uri) {
        destination = new Destination(getApplicationContext().getContentResolver(), uri);
        updateContactDescription();
    }

    private void updateContactDescription() {
        TextView contactDescription = (TextView) getView().findViewById(R.id.contactName);
        contactDescription.setText(destination.getName());
    }

    public void resetButtonColors() {
        for (int i = 0; i < findButtonContainer().getChildCount(); i++) {
            appearUnused((android.widget.Button) findButtonContainer().getChildAt(i));
        }
    }

    public void disableAllButtons() {
        LinearLayout container = findButtonContainer();
        for (int i = 0; i < container.getChildCount(); i++) {
            if (container.getChildAt(i) instanceof android.widget.Button) {
                container.getChildAt(i).setEnabled(false);
            }
        }
    }

    public void chooseAction(View v) {
        final Intent pickActivityIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        pickActivityIntent.putExtra(Intent.EXTRA_INTENT, getTronsmitActivity().createSendIntent());
        pickActivityIntent.putExtra(Intent.EXTRA_TITLE, "what should this button do?");

        startActivityForResult(pickActivityIntent, REQUEST_CODE_CHOOSE_INTENT);
    }

    private Intent createIntentFrom(ComponentName component) {
        final Intent intent = new Intent();
        intent.setComponent(component);
        return intent;
    }

    private void addButtonFor(Intent data, Destination destination) {
        final android.widget.Button newButton = new android.widget.Button(getActivity());
        newButton.setOnClickListener(new StartActivityLike(getTronsmitActivity(), data.getComponent(), destination, knowAboutThePicture()));
        newButton.setText("Send to " + destination.getName() + " by " + getLabel(data));
        appearUnused(newButton);
        newButton.setOnLongClickListener(BUTTON_DELETING_LISTENER);

        findButtonContainer().addView(newButton);
    }

    private PictureKnowerAbouter knowAboutThePicture() {
        return getTronsmitActivity().latestPictureInfo();
    }

    private LinearLayout findButtonContainer() {
        return (LinearLayout) getView().findViewById(R.id.buttonContainer);
    }

    private void gotAnAction(final Intent data, Destination destination) {
        // note: this might not be a default action. could be trouble.

        addButtonFor(data, destination);

        addToSavedButtonConfiguration(data.getComponent());
    }



    // TODO: make the appearance not suck
    private void appearUnused(android.widget.Button newButton) {
        newButton.setBackgroundColor(Color.BLUE);
    }

    private static void appearUsed(android.widget.Button button) {
        button.setBackgroundColor(Color.GREEN);
    }

    private CharSequence getLabel(Intent data) {
        ActivityInfo info = data.resolveActivityInfo(getApplicationContext().getPackageManager(), 0);
        return info.loadLabel(getApplicationContext().getPackageManager());
    }



    public Destination getDestination() {
        return destination;
    }

    private void addToSavedButtonConfiguration(ComponentName component) {
        buttonHelper.store(component, destination);
    }

    private static class StartActivityLike implements View.OnClickListener {
        private final Context c;
        private final ComponentName component;
        private final Destination destination;
        private final PictureKnowerAbouter picInfo;

        private StartActivityLike(Context c, ComponentName component, Destination destination, PictureKnowerAbouter picInfo) {
            this.c = c;
            this.component = component;
            this.destination = destination;
            this.picInfo = picInfo;
        }

        @Override
        public void onClick(View view) {
            appearUsed((android.widget.Button) view);
            startActivityLike(destination, picInfo, component);
        }

        private void startActivityLike(final Destination destination, PictureKnowerAbouter picInfo, ComponentName dataComponent) {
            Intent send = SendIntentCreator.createSendIntent(c.getString(R.string.attribution), destination, picInfo);
            send.setComponent(dataComponent);
            c.startActivity(send);
        }
    }
}

