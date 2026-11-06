package com.example.plantgreencare.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.plantgreencare.data.dao.CareRecordDao;
import com.example.plantgreencare.data.dao.CareScheduleDao;
import com.example.plantgreencare.data.dao.PlantDao;
import com.example.plantgreencare.data.dao.SettingsDao;
import com.example.plantgreencare.data.database.AppDatabase;
import com.example.plantgreencare.data.entities.AppSettingsEntity;
import com.example.plantgreencare.data.entities.CareRecordEntity;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SettingsRepository {

    public interface OnBackupCallback {
        void onSuccess(String jsonString);
        void onError(String message);
    }

    public interface OnRestoreCallback {
        void onSuccess(int count);
        void onError(String message);
    }

    private final SettingsDao settingsDao;
    private final PlantDao plantDao;
    private final CareScheduleDao careScheduleDao;
    private final CareRecordDao careRecordDao;

    public SettingsRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.settingsDao = db.settingsDao();
        this.plantDao = db.plantDao();
        this.careScheduleDao = db.careScheduleDao();
        this.careRecordDao = db.careRecordDao();
    }

    public LiveData<AppSettingsEntity> getSettings() {
        return settingsDao.getSettings();
    }

    public void updateSettings(AppSettingsEntity settings, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            settingsDao.insert(settings);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /**
     * Exporta todas as plantas e configurações para JSON estruturado (Section 39).
     */
    public void exportDataToJson(OnBackupCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                JSONObject root = new JSONObject();
                root.put("app", "PlantGreenCare");
                root.put("version", 1);
                root.put("exportedAt", System.currentTimeMillis());

                List<PlantEntity> plants = plantDao.getAllPlantsSync();
                JSONArray plantsArray = new JSONArray();

                for (PlantEntity p : plants) {
                    JSONObject plantObj = new JSONObject();
                    plantObj.put("name", p.getName());
                    plantObj.put("scientificName", p.getScientificName());
                    plantObj.put("location", p.getLocation());
                    plantObj.put("acquisitionDate", p.getAcquisitionDate());
                    plantObj.put("description", p.getDescription());
                    plantObj.put("lightLevel", p.getLightLevel());
                    plantObj.put("humidityLevel", p.getHumidityLevel());
                    plantsArray.put(plantObj);
                }
                root.put("plants", plantsArray);

                if (callback != null) {
                    callback.onSuccess(root.toString(2));
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Erro ao exportar dados: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Importa dados a partir de JSON estruturado validado.
     */
    public void importDataFromJson(String jsonContent, OnRestoreCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                JSONObject root = new JSONObject(jsonContent);
                if (!root.has("plants")) {
                    if (callback != null) callback.onError("Formato de backup inválido.");
                    return;
                }

                JSONArray plantsArray = root.getJSONArray("plants");
                int importedCount = 0;

                for (int i = 0; i < plantsArray.length(); i++) {
                    JSONObject obj = plantsArray.getJSONObject(i);
                    PlantEntity plant = new PlantEntity();
                    plant.setName(obj.optString("name", "Planta"));
                    plant.setScientificName(obj.optString("scientificName", ""));
                    plant.setLocation(obj.optString("location", ""));
                    plant.setAcquisitionDate(obj.optString("acquisitionDate", ""));
                    plant.setDescription(obj.optString("description", ""));
                    plant.setLightLevel(obj.optString("lightLevel", ""));
                    plant.setHumidityLevel(obj.optString("humidityLevel", ""));

                    long plantId = plantDao.insert(plant);
                    // Add default watering schedule for imported plant
                    long nextWatering = System.currentTimeMillis() + (3L * 24 * 3600 * 1000);
                    careScheduleDao.insert(new CareScheduleEntity(plantId, 1, 3, nextWatering, true, true));
                    importedCount++;
                }

                if (callback != null) {
                    callback.onSuccess(importedCount);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Falha na importação: " + e.getMessage());
                }
            }
        });
    }
}
