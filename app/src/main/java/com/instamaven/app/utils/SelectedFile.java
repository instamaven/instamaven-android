package com.instamaven.app.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class SelectedFile {

    private String fileName;
    private byte[] content;

    public SelectedFile(ContentResolver cr, Uri image) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, image);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            content = byteArrayOutputStream.toByteArray();
            fileName = Hash.md5(image.toString()) + ".jpg";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFilename() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }
/*
    private String getFilename(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String[] parts = cursor.getString(column_index).split("/");
        if (parts.length > 1) {
            return parts[parts.length-1];
        }

        return Hash.md5(uri.toString()) + ".png";
    }
*/
}
