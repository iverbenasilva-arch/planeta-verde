package com.example.plantgreencare.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.R;
import com.example.plantgreencare.data.entities.PlantEntity;

public class PlantShareManager {

    public static String buildPlantShareText(Context context, PlantEntity plant) {
        StringBuilder sb = new StringBuilder();
        sb.append("🌱 Conheça minha planta: ").append(plant.getName());
        if (plant.getScientificName() != null && !plant.getScientificName().trim().isEmpty()) {
            sb.append(" (").append(plant.getScientificName()).append(")");
        }
        sb.append("\n");

        if (plant.getLocation() != null && !plant.getLocation().trim().isEmpty()) {
            sb.append("📍 Local: ").append(plant.getLocation()).append("\n");
        }
        if (plant.getLightLevel() != null && !plant.getLightLevel().trim().isEmpty()) {
            sb.append("☀️ Luz: ").append(plant.getLightLevel()).append("\n");
        }
        if (plant.getHumidityLevel() != null && !plant.getHumidityLevel().trim().isEmpty()) {
            sb.append("💧 Umidade: ").append(plant.getHumidityLevel()).append("\n");
        }
        if (plant.getDescription() != null && !plant.getDescription().trim().isEmpty()) {
            sb.append("📝 Observações: ").append(plant.getDescription()).append("\n");
        }

        sb.append("\nCuidada com carinho no Plant Green Care! 🌿");
        return sb.toString();
    }

    public static void shareSystemWide(Context context, PlantEntity plant) {
        String shareText = buildPlantShareText(context, plant);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Planta: " + plant.getName());
        intent.putExtra(Intent.EXTRA_TEXT, shareText);

        if (plant.getPhotoUri() != null && !plant.getPhotoUri().trim().isEmpty()) {
            try {
                Uri photoUri = Uri.parse(plant.getPhotoUri());
                intent.putExtra(Intent.EXTRA_STREAM, photoUri);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {
            }
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)));
    }
}
