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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jessitron.tronsmit.database.CustomButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public static final String BUTTONS_THAT_APPEAR_USED = "buttonsThatAppearUsed";

    private CustomButton.Helper buttonHelper;

    private Destination destination;
    private static List<Long> buttonsThatAreUsed = new ArrayList<Long>(4);

    /**
     * Initialization **
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.buttons_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        loadPreferences();
        buttonHelper = new CustomButton.Helper(getApplicationContext());
        createButtons(listOfUsedButtons(savedInstanceState));
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Picking a Contact **
     */
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
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    private void gotAContact(android.net.Uri uri) {
        destination = new Destination(getApplicationContext().getContentResolver(), uri);
        updateContactDescription();
    }

    /*-- Preferences --*/
    private SharedPreferences getPreferences() {
        return getActivity().getPreferences(Activity.MODE_PRIVATE);
    }

    private void savePreferences(Uri contactUri) {
        getPreferences().edit()   // start transaction
                .putString("contactUri", contactUri.toString())
                .apply();         // commit, off-thread
    }

    private void loadPreferences() {
        String uriString = getPreferences().getString("contactUri", "");
        if (!uriString.isEmpty()) {
            gotAContact(Uri.parse(uriString));
        }
    }

    /*-- save and restore state --*/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLongArray(BUTTONS_THAT_APPEAR_USED, toArray(buttonsThatAreUsed));
        super.onSaveInstanceState(outState);
    }

    private List<Long> listOfUsedButtons(Bundle savedInstanceState) {
        return (savedInstanceState != null && savedInstanceState.containsKey(BUTTONS_THAT_APPEAR_USED)) ?
                arrayAsList(savedInstanceState.getLongArray(BUTTONS_THAT_APPEAR_USED)) :
                Collections.<Long>emptyList();
    }

    /*-- other --*/
    public TronsmitApplication getApplicationContext() {
        return (TronsmitApplication) getActivity().getApplicationContext();
    }

    public TronsmitActivity getTronsmitActivity() {
        return (TronsmitActivity) getActivity();
    }

    private void createButtons(List<Long> usedButtons) {
        for (CustomButton.ButtonConfig buttonConfig : buttonHelper.getButtons()) {
            addButtonFor(buttonConfig, usedButtons.contains(buttonConfig.id));
        }
    }

    public void resetButtons() {
        findButtonContainer().removeAllViews();
        buttonHelper.deleteAll();
    }


    private void updateContactDescription() {
        TextView contactDescription = (TextView) getView().findViewById(R.id.contactName);
        contactDescription.setText(destination.getName());
    }

    public void resetButtonColors() {
        for (int i = 0; i < findButtonContainer().getChildCount(); i++) {
            appearUnused((Button) findButtonContainer().getChildAt(i));
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


    // please tell me there is a cleaner way to do this. I miss Scala.
    private long[] toArray(List<Long> buttonsThatAreUsed) {
        long[] result = new long[buttonsThatAreUsed.size()];
        for (int i = 0; i < buttonsThatAreUsed.size(); i++) {
            result[i] = buttonsThatAreUsed.get(i);
        }
        return result;
    }

    private List<Long> arrayAsList(long[] array) {
        List<Long> result = new ArrayList<Long>(array.length);
        for (long l : array) {
            result.add(l);
        }
        return result;
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

    private void addButtonFor(CustomButton.ButtonConfig config, boolean isUsed) {
        final android.widget.Button newButton = new android.widget.Button(getActivity());
        newButton.setOnClickListener(new StartActivityLike(getTronsmitActivity(), config, knowAboutThePicture()));
        newButton.setText("Send to " + config.destination.getName() + " by " + getLabel(createIntentFrom(config.component)));
        if (isUsed) {
            appearUsed(newButton);
        } else {
            appearUnused(newButton);
        }
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
        Long id = addToSavedButtonConfiguration(data.getComponent());
        addButtonFor(new CustomButton.ButtonConfig(data.getComponent(), destination, id), false);
    }

    // TODO: make the appearance not suck
    private void appearUnused(android.widget.Button newButton) {
        newButton.setBackgroundColor(Color.BLUE);
    }

    private static void markUsed(Button button, long id) {
        appearUsed(button);
        buttonsThatAreUsed.add(id);
    }

    private static void appearUsed(Button button) {
        button.setBackgroundColor(Color.GREEN);
    }

    private CharSequence getLabel(Intent data) {
        ActivityInfo info = data.resolveActivityInfo(getApplicationContext().getPackageManager(), 0);
        return info.loadLabel(getApplicationContext().getPackageManager());
    }


    public Destination getDestination() {
        return destination;
    }

    private long addToSavedButtonConfiguration(ComponentName component) {
        return buttonHelper.store(component, destination);
    }

    private static class StartActivityLike implements View.OnClickListener {
        private final Context c;
        private CustomButton.ButtonConfig config;
        private final PictureKnowerAbouter picInfo;


        public StartActivityLike(Context c, CustomButton.ButtonConfig config, PictureKnowerAbouter picInfo) {
            this.c = c;
            this.config = config;
            this.picInfo = picInfo;
        }

        @Override
        public void onClick(View view) {
            markUsed((android.widget.Button) view, config.id);
            startActivityLike(config.destination, picInfo, config.component);
        }

        private void startActivityLike(final Destination destination, PictureKnowerAbouter picInfo, ComponentName dataComponent) {
            Intent send = SendIntentCreator.createSendIntent(c.getString(R.string.attribution), destination, picInfo);
            send.setComponent(dataComponent);
            c.startActivity(send);
        }
    }
}

