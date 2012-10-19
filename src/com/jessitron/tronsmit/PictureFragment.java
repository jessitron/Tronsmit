package com.jessitron.tronsmit;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class PictureFragment extends Fragment {
    private static final int REQUEST_CODE_PICK_IMAGE = 4;

    private PictureManager pictureManager;

    public PictureManager getPictureManager() {
        return pictureManager; //TODO: eliminate
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.picture_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        TronsmitActivity.say("PictureFragment onActivityCreated");
        loadGestures();
        pictureManager = new PictureManager((ImageView) getView().findViewById(R.id.pictureView), getActivity().getApplicationContext());
        pictureManager.reset();
        if (!pictureManager.hasPicture()) {
            getTronsmit().noPictureAvailable();
            toast("This app is useless without pictures");
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        pictureManager.shutDown();
        super.onDestroy();
    }

    private TronsmitActivity getTronsmit() {
        return (TronsmitActivity) getActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deletePicture();
                return true;
            case R.id.choosepic:
                pickArbitraryImage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode,
                                    int resultCode,
                                    final Intent data) {
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            gotAnImage(data.getData());
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    private void pickArbitraryImage() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setType("image/*");
        // final ResolveInfo resolveInfo = getPackageManager().resolveActivity(pickIntent, PackageManager.MATCH_DEFAULT_ONLY);
        startActivityForResult(pickIntent, REQUEST_CODE_PICK_IMAGE);
    }

    private void gotAnImage(Uri data) {
        pictureManager.useThisOne(data);
    }

    private void loadGestures() {
        final GestureLibrary gestureLibrary = GestureLibraries.fromRawResource(getActivity().getApplicationContext(),
                getResources().getIdentifier("raw/gestures", null, getActivity().getPackageName()));
        if (!gestureLibrary.load()) {
            toast("Warning: unable to load gestures");
            return;
        }

        GestureOverlayView gestures = (GestureOverlayView) getView().findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
                ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
                if (predictions.size() > 0) {
                    Prediction prediction = predictions.get(0);
                    if (prediction.score > 1.0) {
                        if ("older".equals(prediction.name)) {
                            getTronsmit().pictureChanged();
                            pictureManager.older();
                        } else if ("newer".equals(prediction.name)) {
                            getTronsmit().pictureChanged();
                            pictureManager.newer();
                        } else if ("tronsmit".equals(prediction.name)) {
                            getTronsmit().tronsmit(null);
                        }
                    }
                }
            }
        });
    }

    public PictureKnowerAbouter getPicInfo() {
        final String imageType = pictureManager.getImageType();
        final Uri imageLocation = pictureManager.getImageLocation();
        return new PictureKnowerAbouter() {
            @Override
            public String getImageType() {
                return  imageType;
            }

            @Override
            public Uri getImageLocation() {
                return imageLocation;
            }
        }   ;
    }

    private void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    private void deletePicture() {
        new DeleteConfirmation().show(getFragmentManager(), "dialog");
    }

    public void deleteCurrentPicture() {
        pictureManager.delete();
    }

    public void reset() {
        pictureManager.reset();
    }

    public static class DeleteConfirmation extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = R.string.confirmDeletion;

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((TronsmitActivity) getActivity()).reallyDeletePicture();
                                }
                            }
                    )
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create();
        }
    }
}
