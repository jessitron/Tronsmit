package com.jessitron.transmit;

import static android.provider.MediaStore.Images.ImageColumns.DATE_TAKEN;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class PictureManager {

    private static final String LOG_PREFIX = "JessiTRON";
    public static final String[] SELECTED_COLUMNS = new String[]{MediaStore.Images.ImageColumns._ID,    // 0
            MediaStore.Images.ImageColumns.DATA,                              // 1
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,               // 2
            DATE_TAKEN,                                                       // 3
            MediaStore.Images.ImageColumns.MIME_TYPE,           // 4
            DATE_ADDED                                                        // 5
    };
    public static final int COL_IMAGE_TYPE = 4;
    private Cursor cursor;
    private ImageView imageView;
    private final Context context;


    public PictureManager(ImageView imageView, Context context) {
        this.imageView = imageView;
        this.context = context;
    }
    
    public void reset() {
        queryPictures();
        updateImageToCurrentPicture();
        
    }

    public boolean hasPicture() {
        return cursor != null && !cursor.isAfterLast();
    }

    private void updateImageToCurrentPicture() {
       if (cursor.isAfterLast()) {
           Log.d(LOG_PREFIX, "No pictures found");
           clearView();
       } else {
        putPicInView(imageView);
        imageView.invalidate();
       }
    }

    private void clearView() {
        imageView.setImageDrawable(null);
    }

    public String getImageType() {
        String imageType = cursor.getString(COL_IMAGE_TYPE);
        Log.d(LOG_PREFIX, "The type of this image is " + imageType);
        return imageType;
    }
    
    public Uri getImageLocation() {
        return  Uri.parse( "file://" + cursor.getString(1));
    }


    private void putPicInView(ImageView imageView) {
        imageView.setImageURI(getImageLocation());
    }

    public void advance() {
        cursor.moveToNext();
        advanceCursorPast3DImages();
        updateImageToCurrentPicture();
        putPicInView(imageView);
    }

    private void queryPictures() {
        closeCursor();
        cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, SELECTED_COLUMNS, null, null, DATE_ADDED + " DESC");
        // TODO: check whether the disk is available.
        if (cursor == null) {
            Toast.makeText(context,"Unable to read photographs", Toast.LENGTH_LONG).show();
            return;
        }
        cursor.moveToFirst();
        advanceCursorPast3DImages();
    }

    private void advanceCursorPast3DImages() {
        while("image/mpo".equals(cursor.getString(COL_IMAGE_TYPE)) && !cursor.isAfterLast()) {
            cursor.moveToNext();
        }
    }

    private void closeCursor() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public void shutDown() {
        closeCursor();
        imageView = null;
    }

}
