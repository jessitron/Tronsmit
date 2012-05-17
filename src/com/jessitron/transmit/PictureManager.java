package com.jessitron.transmit;

import static android.provider.MediaStore.Images.ImageColumns.DATE_TAKEN;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

public class PictureManager {

    private static final String LOG_PREFIX = "JessiTRON";
    public static final String[] SELECTED_COLUMNS = new String[]{MediaStore.Images.ImageColumns._ID,    // 0
            MediaStore.Images.ImageColumns.DATA,                              // 1
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,               // 2
            DATE_TAKEN,                                                       // 3
            MediaStore.Images.ImageColumns.MIME_TYPE,           // 4
            DATE_ADDED                                                        // 5
    };
    private Cursor cursor;
    private final ImageView imageView;
    private final Activity context;


    public PictureManager(ImageView imageView, Activity context) {
        this.imageView = imageView;
        this.context = context;
    }
    
    public void reset() {
        queryPictures();
        updateImageToCurrentPicture();
        
    }

    private void updateImageToCurrentPicture() {
       if (cursor.isAfterLast()) {
           Log.d(LOG_PREFIX, "No pictures found");
           clearView();
           return;
       }
        putPicInView(imageView);
        imageView.invalidate();
    }

    private void clearView() {
        imageView.setImageDrawable(null);
    }

    public String getImageType() {
        String imageType = cursor.getString(4);
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
        updateImageToCurrentPicture();
        putPicInView(imageView);
    }

    private void queryPictures() {
        closeCursor();
        // TODO: is a managed query correct? we don't want to close it when we lose focus.
        cursor = context.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, SELECTED_COLUMNS, null, null, DATE_ADDED + " DESC");
        // TODO: this cursor is occasionally null ?!?
        // like if the disk is not available -- check whether the disk is available.
        cursor.moveToFirst();
    }

    private void closeCursor() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}
