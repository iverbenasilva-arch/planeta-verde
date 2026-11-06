package com.example.plantgreencare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.plantgreencare.data.entities.CareRecordEntity;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.data.repository.CareRepository;
import com.example.plantgreencare.data.repository.PlantRepository;
import com.example.plantgreencare.model.PlantCardItem;
import com.example.plantgreencare.utils.CareScheduleCalculator;
import com.example.plantgreencare.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class PlantViewModel extends AndroidViewModel {

    public enum FilterMode {
        ALL,
        NEEDS_CARE,
        OVERDUE,
        UP_TO_DATE
    }

    private final PlantRepository plantRepository;
    private final CareRepository careRepository;

    private final MutableLiveData<FilterMode> currentFilter = new MutableLiveData<>(FilterMode.ALL);
    private final MutableLiveData<String> currentSearchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<PlantCardItem>> filteredPlants = new MediatorLiveData<>();

    private List<PlantEntity> rawPlants = new ArrayList<>();

    public PlantViewModel(@NonNull Application application) {
        super(application);
        plantRepository = new PlantRepository(application);
        careRepository = new CareRepository(application);

        LiveData<List<PlantEntity>> allPlantsLive = plantRepository.getAllPlants();

        filteredPlants.addSource(allPlantsLive, plants -> {
            rawPlants = plants != null ? plants : new ArrayList<>();
            applyFilterAndSearch();
        });

        filteredPlants.addSource(currentFilter, filter -> applyFilterAndSearch());
        filteredPlants.addSource(currentSearchQuery, query -> applyFilterAndSearch());
    }

    public LiveData<List<PlantCardItem>> getFilteredPlants() {
        return filteredPlants;
    }

    public void setFilter(FilterMode filter) {
        currentFilter.setValue(filter);
    }

    public void setSearchQuery(String query) {
        currentSearchQuery.setValue(query != null ? query.trim() : "");
    }

    private void applyFilterAndSearch() {
        final String query = currentSearchQuery.getValue() != null ? currentSearchQuery.getValue().toLowerCase() : "";
        final FilterMode filter = currentFilter.getValue() != null ? currentFilter.getValue() : FilterMode.ALL;

        com.example.plantgreencare.data.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PlantCardItem> items = new ArrayList<>();

            for (PlantEntity plant : rawPlants) {
                // Filtro de pesquisa textual
                if (!query.isEmpty()) {
                    boolean matchName = plant.getName() != null && plant.getName().toLowerCase().contains(query);
                    boolean matchScientific = plant.getScientificName() != null && plant.getScientificName().toLowerCase().contains(query);
                    boolean matchLocation = plant.getLocation() != null && plant.getLocation().toLowerCase().contains(query);
                    if (!matchName && !matchScientific && !matchLocation) {
                        continue;
                    }
                }

                // Obter próximo cuidado e último cuidado para o card
                CareScheduleEntity nextSched = null;
                List<CareScheduleEntity> schedules = careRepository.getSchedulesByPlant(plant.getId()).getValue();
                if (schedules != null && !schedules.isEmpty()) {
                    nextSched = schedules.get(0);
                }

                CareRecordEntity lastRecord = careRepository.getLastRecordForPlant(plant.getId()).getValue();

                String nextCareText;
                CareScheduleCalculator.CareStatus status = CareScheduleCalculator.CareStatus.UP_TO_DATE;

                if (nextSched != null) {
                    status = CareScheduleCalculator.getCareStatus(nextSched.getNextDate());
                    String typeName = getCareTypeName(nextSched.getCareTypeId());
                    if (status == CareScheduleCalculator.CareStatus.OVERDUE) {
                        long days = CareScheduleCalculator.getDaysOverdue(nextSched.getNextDate());
                        nextCareText = typeName + ": Atrasado há " + days + " dia(s)";
                    } else if (status == CareScheduleCalculator.CareStatus.PENDING_TODAY) {
                        nextCareText = typeName + ": Hoje";
                    } else {
                        nextCareText = typeName + ": " + DateUtils.formatRelativeOrDate(nextSched.getNextDate());
                    }
                } else {
                    nextCareText = "Sem cuidados agendados";
                }

                String lastCareText = lastRecord != null
                        ? "Último cuidado: " + DateUtils.formatDate(lastRecord.getPerformedAt())
                        : "Nenhum cuidado anterior";

                // Aplicar filtro de status
                boolean include = false;
                switch (filter) {
                    case ALL:
                        include = true;
                        break;
                    case NEEDS_CARE:
                        include = (status == CareScheduleCalculator.CareStatus.PENDING_TODAY || status == CareScheduleCalculator.CareStatus.OVERDUE);
                        break;
                    case OVERDUE:
                        include = (status == CareScheduleCalculator.CareStatus.OVERDUE);
                        break;
                    case UP_TO_DATE:
                        include = (status == CareScheduleCalculator.CareStatus.UP_TO_DATE);
                        break;
                }

                if (include) {
                    items.add(new PlantCardItem(plant, nextCareText, lastCareText, status));
                }
            }

            filteredPlants.postValue(items);
        });
    }

    private String getCareTypeName(long typeId) {
        if (typeId == 1) return "Rega";
        if (typeId == 2) return "Adubação";
        if (typeId == 3) return "Poda";
        return "Cuidado";
    }

    public void savePlant(PlantEntity plant,
                          int wateringFreq, boolean wateringEnabled,
                          int fertilizingFreq, boolean fertilizingEnabled,
                          int pruningFreq, boolean pruningEnabled,
                          PlantRepository.OnPlantSavedCallback callback) {
        if (plant.getId() > 0) {
            plantRepository.updatePlantWithSchedules(
                    plant, wateringFreq, wateringEnabled,
                    fertilizingFreq, fertilizingEnabled,
                    pruningFreq, pruningEnabled,
                    () -> {
                        if (callback != null) callback.onSaved(plant.getId());
                    }
            );
        } else {
            plantRepository.insertPlantWithSchedules(
                    plant, wateringFreq, wateringEnabled,
                    fertilizingFreq, fertilizingEnabled,
                    pruningFreq, pruningEnabled,
                    callback
            );
        }
    }

    public void saveMultiplePlants(List<PlantEntity> plants,
                                  int wateringFreq, boolean wateringEnabled,
                                  int fertilizingFreq, boolean fertilizingEnabled,
                                  int pruningFreq, boolean pruningEnabled,
                                  Runnable onComplete) {
        plantRepository.insertMultiplePlantsWithSchedules(
                plants, wateringFreq, wateringEnabled,
                fertilizingFreq, fertilizingEnabled,
                pruningFreq, pruningEnabled,
                () -> {
                    applyFilterAndSearch();
                    if (onComplete != null) onComplete.run();
                }
        );
    }

    public void quickRegisterCare(PlantEntity plant, Runnable onComplete) {
        careRepository.registerCare(
                plant.getId(),
                1, // Default para Rega
                System.currentTimeMillis(),
                "Rega rápida registrada",
                () -> {
                    applyFilterAndSearch();
                    if (onComplete != null) onComplete.run();
                }
        );
    }
}
