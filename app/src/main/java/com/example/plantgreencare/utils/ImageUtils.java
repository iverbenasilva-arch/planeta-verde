package com.example.plantgreencare.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.example.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageUtils {

    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int JPEG_QUALITY = 85;

    /**
     * Salva a imagem selecionada ou capturada no armazenamento interno protegido do aplicativo,
     * redimensionando proporcionalmente para evitar OutOfMemoryError e economizar espaço.
     */
    public static Uri copyAndCompressImage(Context context, Uri sourceUri) {
        if (sourceUri == null) return null;
        try {
            File imagesDir = new File(context.getFilesDir(), "images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            File destFile = new File(imagesDir, "plant_" + System.currentTimeMillis() + ".jpg");

            // 1. Obter dimensões originais sem carregar o bitmap inteiro
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            try (InputStream is = context.getContentResolver().openInputStream(sourceUri)) {
                BitmapFactory.decodeStream(is, null, boundsOptions);
            }

            int sampleSize = calculateInSampleSize(boundsOptions, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION);

            // 2. Decodificar com subamostragem
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = sampleSize;
            Bitmap bitmap;
            try (InputStream is = context.getContentResolver().openInputStream(sourceUri)) {
                bitmap = BitmapFactory.decodeStream(is, null, decodeOptions);
            }

            if (bitmap == null) return null;

            // 3. Redimensionar se ainda exceder MAX_IMAGE_DIMENSION
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
                float ratio = Math.min((float) MAX_IMAGE_DIMENSION / width, (float) MAX_IMAGE_DIMENSION / height);
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                if (resized != bitmap) {
                    bitmap.recycle();
                    bitmap = resized;
                }
            }

            // 4. Salvar comprimido em JPEG
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            }
            bitmap.recycle();

            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", destFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Carrega a imagem com segurança em um ImageView, tratando nulls e erros com fallback.
     */
    public static void loadIntoImageView(Context context, ImageView imageView, String photoUriString) {
        if (photoUriString == null || photoUriString.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.plant_care_icon);
            return;
        }

        try {
            Uri uri = Uri.parse(photoUriString);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Decodificação eficiente para miniaturas

            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.plant_care_icon);
                }
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.plant_care_icon);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
