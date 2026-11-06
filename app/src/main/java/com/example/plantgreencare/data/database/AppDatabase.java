package com.example.plantgreencare.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.plantgreencare.data.dao.CareRecordDao;
import com.example.plantgreencare.data.dao.CareScheduleDao;
import com.example.plantgreencare.data.dao.PlantDao;
import com.example.plantgreencare.data.dao.SettingsDao;
import com.example.plantgreencare.data.entities.AppSettingsEntity;
import com.example.plantgreencare.data.entities.CareRecordEntity;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.CareTypeEntity;
import com.example.plantgreencare.data.entities.PlantEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        PlantEntity.class,
        CareTypeEntity.class,
        CareScheduleEntity.class,
        CareRecordEntity.class,
        AppSettingsEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract PlantDao plantDao();
    public abstract CareScheduleDao careScheduleDao();
    public abstract CareRecordDao careRecordDao();
    public abstract SettingsDao settingsDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "plant_green_care_database"
                    )
                    .addCallback(sRoomDatabaseCallback)
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                // Initialize default AppSettings
                AppSettingsEntity defaultSettings = new AppSettingsEntity();
                defaultSettings.setId(1);
                defaultSettings.setNotificationsEnabled(true);
                defaultSettings.setNotificationHour(8);
                defaultSettings.setNotificationMinute(0);
                defaultSettings.setDarkMode(0);
                defaultSettings.setOnboardingCompleted(false);

                if (INSTANCE != null) {
                    INSTANCE.settingsDao().insert(defaultSettings);
                }
            });
        }
    };
}
