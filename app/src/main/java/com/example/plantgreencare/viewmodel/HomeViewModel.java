package com.example.plantgreencare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.plantgreencare.data.database.AppDatabase;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.data.repository.CareRepository;
import com.example.plantgreencare.data.repository.PlantRepository;
import com.example.plantgreencare.model.CareTaskItem;
import com.example.plantgreencare.utils.CareScheduleCalculator;
import com.example.plantgreencare.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final PlantRepository plantRepository;
    private final CareRepository careRepository;

    private final LiveData<Integer> totalPlantsCount;
    private final LiveData<List<CareScheduleEntity>> todaySchedules;
    private final LiveData<List<CareScheduleEntity>> overdueSchedules;
    private final LiveData<CareScheduleEntity> nextSchedule;

    private final MediatorLiveData<List<CareTaskItem>> todayCareTasks = new MediatorLiveData<>();
    private final MutableLiveData<String> nextCareInfo = new MutableLiveData<>();
    private final MutableLiveData<CareScheduleEntity> activeNextSchedule = new MutableLiveData<>();
    private final MutableLiveData<PlantEntity> activeNextPlant = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        plantRepository = new PlantRepository(application);
        careRepository = new CareRepository(application);

        totalPlantsCount = plantRepository.getPlantsCount();
        todaySchedules = careRepository.getTodaySchedules();
        overdueSchedules = careRepository.getOverdueSchedules();
        nextSchedule = careRepository.getNextSchedule();

        todayCareTasks.addSource(todaySchedules, this::buildTodayTasks);

        todayCareTasks.addSource(nextSchedule, schedule -> {
            if (schedule != null) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    PlantEntity plant = careRepository.getPlantByIdSync(schedule.getPlantId());
                    if (plant != null) {
                        String typeName = getCareTypeName(schedule.getCareTypeId());
                        String dateStr = DateUtils.formatRelativeOrDate(schedule.getNextDate());
                        nextCareInfo.postValue(typeName + " para " + plant.getName() + " (" + dateStr + ")");
                        activeNextSchedule.postValue(schedule);
                        activeNextPlant.postValue(plant);
                    }
                });
            } else {
                nextCareInfo.postValue("Nenhum cuidado agendado.");
                activeNextSchedule.postValue(null);
                activeNextPlant.postValue(null);
            }
        });
    }

    private void buildTodayTasks(List<CareScheduleEntity> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            todayCareTasks.setValue(new ArrayList<>());
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CareTaskItem> items = new ArrayList<>();
            for (CareScheduleEntity schedule : schedules) {
                PlantEntity plant = careRepository.getPlantByIdSync(schedule.getPlantId());
                if (plant != null) {
                    String typeName = getCareTypeName(schedule.getCareTypeId());
                    String desc = typeName + " necessária hoje";
                    items.add(new CareTaskItem(plant, schedule, typeName, desc, false, 0));
                }
            }
            todayCareTasks.postValue(items);
        });
    }

    private String getCareTypeName(long typeId) {
        if (typeId == 1) return "Rega";
        if (typeId == 2) return "Adubação";
        if (typeId == 3) return "Poda";
        if (typeId == 4) return "Exposição à luz";
        if (typeId == 5) return "Pulverização";
        return "Cuidado";
    }

    public LiveData<Integer> getTotalPlantsCount() {
        return totalPlantsCount;
    }

    public LiveData<List<CareScheduleEntity>> getTodaySchedules() {
        return todaySchedules;
    }

    public LiveData<List<CareScheduleEntity>> getOverdueSchedules() {
        return overdueSchedules;
    }

    public LiveData<List<CareTaskItem>> getTodayCareTasks() {
        return todayCareTasks;
    }

    public LiveData<String> getNextCareInfo() {
        return nextCareInfo;
    }

    public LiveData<CareScheduleEntity> getActiveNextSchedule() {
        return activeNextSchedule;
    }

    public LiveData<PlantEntity> getActiveNextPlant() {
        return activeNextPlant;
    }

    public void quickRegisterCare(CareScheduleEntity schedule, Runnable onComplete) {
        careRepository.registerCare(
                schedule.getPlantId(),
                schedule.getCareTypeId(),
                System.currentTimeMillis(),
                "Cuidado rápido registrado pelo painel inicial.",
                onComplete
        );
    }
}
