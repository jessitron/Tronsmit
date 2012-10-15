package com.jessitron.tronsmit;

import android.content.Intent;

class SendIntentCreator {

    public static Intent createSendIntent(String attributionString, Destination destination, PictureKnowerAbouter picInfo) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(picInfo.getImageType());
        shareIntent.putExtra(Intent.EXTRA_STREAM, picInfo.getImageLocation());

        if (destination.getPhoneNumber() != null) {
            shareIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, destination.getPhoneNumber());
            shareIntent.putExtra("address", destination.getPhoneNumber());
        }
        shareIntent.putExtra("sms_body", attributionString);
        shareIntent.putExtra(Intent.EXTRA_TEXT, attributionString);

        if (destination.getEmail() != null) {
            shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{destination.getEmail()});
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, attributionString);

        return shareIntent;
    }
}
