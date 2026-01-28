package com.inventorymanager.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    private static final String IMAGE_DIR = "inventory_images";

    public static File createImageFile(Context context) {
        try {
            File storageDir = new File(context.getFilesDir(), IMAGE_DIR);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            String imageFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            return new File(storageDir, imageFileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveImageFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            File imageFile = createImageFile(context);
            if (imageFile == null) return null;

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadImage(Context context, ImageView imageView, String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Glide.with(context)
                        .load(imageFile)
                        .into(imageView);
            }
        }
    }
}
