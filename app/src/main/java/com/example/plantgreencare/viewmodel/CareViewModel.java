package com.example.plantgreencare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.data.repository.CareRepository;
import com.example.plantgreencare.model.CareTaskItem;
import com.example.plantgreencare.utils.CareScheduleCalculator;
import com.example.plantgreencare.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class CareViewModel extends AndroidViewModel {

    public enum CareFilter {
        TODAY,
        OVERDUE,
        TOMORROW,
        THIS_WEEK,
        ALL
    }

    private final CareRepository careRepository;
    private final MutableLiveData<CareFilter> currentFilter = new MutableLiveData<>(CareFilter.TODAY);
    private final MediatorLiveData<List<CareTaskItem>> careTasks = new MediatorLiveData<>();

    public CareViewModel(@NonNull Application application) {
        super(application);
        careRepository = new CareRepository(application);

        LiveData<List<CareScheduleEntity>> allSchedulesLive = careRepository.getAllEnabledSchedules();

        careTasks.addSource(allSchedulesLive, schedules -> filterSchedules(schedules, currentFilter.getValue()));
        careTasks.addSource(currentFilter, filter -> filterSchedules(allSchedulesLive.getValue(), filter));
    }

    public LiveData<List<CareTaskItem>> getCareTasks() {
        return careTasks;
    }

    public void setFilter(CareFilter filter) {
        currentFilter.setValue(filter);
    }

    private void filterSchedules(List<CareScheduleEntity> schedules, CareFilter filter) {
        if (schedules == null || schedules.isEmpty()) {
            careTasks.setValue(new ArrayList<>());
            return;
        }

        final CareFilter activeFilter = filter != null ? filter : CareFilter.TODAY;

        com.example.plantgreencare.data.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CareTaskItem> items = new ArrayList<>();

            for (CareScheduleEntity schedule : schedules) {
                if (!schedule.isEnabled()) continue;

                boolean matches = false;
                switch (activeFilter) {
                    case TODAY:
                        matches = CareScheduleCalculator.isDueToday(schedule.getNextDate());
                        break;
                    case OVERDUE:
                        matches = CareScheduleCalculator.getCareStatus(schedule.getNextDate()) == CareScheduleCalculator.CareStatus.OVERDUE;
                        break;
                    case TOMORROW:
                        matches = CareScheduleCalculator.isDueTomorrow(schedule.getNextDate());
                        break;
                    case THIS_WEEK:
                        matches = CareScheduleCalculator.isDueThisWeek(schedule.getNextDate());
                        break;
                    case ALL:
                        matches = true;
                        break;
                }

                if (matches) {
                    PlantEntity plant = careRepository.getPlantByIdSync(schedule.getPlantId());
                    if (plant != null) {
                        String typeName = getCareTypeName(schedule.getCareTypeId());
                        boolean isOverdue = CareScheduleCalculator.getCareStatus(schedule.getNextDate()) == CareScheduleCalculator.CareStatus.OVERDUE;
                        long daysOverdue = CareScheduleCalculator.getDaysOverdue(schedule.getNextDate());

                        String statusText;
                        if (isOverdue) {
                            statusText = "Atrasada há " + daysOverdue + " dia(s)";
                        } else if (CareScheduleCalculator.isDueToday(schedule.getNextDate())) {
                            statusText = "Previsto para hoje";
                        } else {
                            statusText = DateUtils.formatRelativeOrDate(schedule.getNextDate());
                        }

                        items.add(new CareTaskItem(plant, schedule, typeName, statusText, isOverdue, daysOverdue));
                    }
                }
            }

            careTasks.postValue(items);
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

    public void registerCare(CareScheduleEntity schedule, String notes, Runnable onComplete) {
        careRepository.registerCare(
                schedule.getPlantId(),
                schedule.getCareTypeId(),
                System.currentTimeMillis(),
                notes != null && !notes.trim().isEmpty() ? notes : "Cuidado registrado via tela Cuidados.",
                () -> {
                    // Recalcular lista
                    filterSchedules(careRepository.getAllEnabledSchedules().getValue(), currentFilter.getValue());
                    if (onComplete != null) onComplete.run();
                }
        );
    }
}
