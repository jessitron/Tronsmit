package com.jessitron.tronsmit;

import static android.provider.MediaStore.Images.ImageColumns.DATE_TAKEN;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class PictureManager implements PictureKnowerAbouter {

    private static final String LOG_PREFIX = "JessiTRON";


    private Cursor cursor;
    private final ImageView imageView;
    private final Context context;
    private Uri imageUri;
    private String imageType;
    private long imageId;

    public PictureManager(ImageView imageView, Context context) {
        this.imageView = imageView;
        this.context = context;
    }

    public void reset() {
        queryPictures();
        updateImageToCurrentPicture();
    }

    private static final String[] SELECTED_COLUMNS = new String[]{
            MediaStore.Images.ImageColumns._ID,                               // 0
            MediaStore.Images.ImageColumns.DATA,                              // 1
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,               // 2
            DATE_TAKEN,                                                       // 3
            MediaStore.Images.ImageColumns.MIME_TYPE,                         // 4
            DATE_ADDED                                                        // 5
    };

    private void queryPictures() {
        closeCursor();
        cursor = context.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        SELECTED_COLUMNS, null, null, DATE_ADDED + " DESC");

        // TODO: check whether the disk is available.
        if (cursor == null) {
            Toast.makeText(context, "Unable to read photographs", Toast.LENGTH_LONG).show();
            return;
        }
        cursor.moveToFirst();
        pullInfoFromCursor();
    }

    private static final int COL_IMAGE_TYPE = 4;
    private static final int COL_IMAGE_URI = 1;
    private static final int COL_IMAGE_ID = 0;

    private void pullInfoFromCursor() {
        if (!cursor.isAfterLast()) {
            imageUri = Uri.parse("file://" + cursor.getString(COL_IMAGE_URI));
            imageType = cursor.getString(COL_IMAGE_TYPE);
            imageId = cursor.getLong(COL_IMAGE_ID);
        }
    }

    public void delete() {
        context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.MediaColumns._ID + " = :id", new String[]{"" + imageId});
        reset();
    }

    public void shutDown() {
        closeCursor();
        recycleBitmap();
    }

    public boolean hasPicture() {
        return cursor != null && !cursor.isAfterLast();
    }

    private void updateImageToCurrentPicture() {
        if (cursor.isAfterLast()) {
            Log.d(LOG_PREFIX, "No pictures found");
            clearView();
        } else {
            putPicInView();
        }
    }

    private void putPicInView() {
        // Let's try to completely delete the old one, in the hopes that bitmap space will run out less frequently.
        recycleBitmap();
        imageView.setImageBitmap(null);
        imageView.setImageURI(imageUri);
        imageView.invalidate();
    }

    private void clearView() {
        imageView.setImageDrawable(null);
    }

    public String getImageType() {
        return imageType;
    }

    public Uri getImageLocation() {
        return imageUri;
    }

    private void recycleBitmap() {
        final Drawable drawable = imageView.getDrawable();
        imageView.setImageDrawable(null);
        if (drawable != null && drawable instanceof BitmapDrawable) {
            ((BitmapDrawable) drawable).getBitmap().recycle();
        }
    }

    public void older() {
        if (!cursor.isLast()) {
            cursor.moveToNext();
            pullInfoFromCursor();
            updateImageToCurrentPicture();
        }
    }

    public void newer() {
        if (!cursor.isFirst()) {
            cursor.moveToPrevious();
            pullInfoFromCursor();
            updateImageToCurrentPicture();
        } else {
            // we are on the first picture. Reload
            reset();
        }
    }

    private void closeCursor() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }


    public void useThisOne(Uri data) {
        imageUri = data;
        imageType = "image/jpeg"; // I'm cheating. If this isn't right it will fail
        putPicInView();
    }


}
