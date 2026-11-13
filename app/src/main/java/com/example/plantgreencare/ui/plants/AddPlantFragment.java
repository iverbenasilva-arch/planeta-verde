package com.example.plantgreencare.ui.plants;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.R;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.utils.DateUtils;
import com.example.plantgreencare.utils.ImageUtils;
import com.example.plantgreencare.viewmodel.PlantDetailsViewModel;
import com.example.plantgreencare.viewmodel.PlantViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddPlantFragment extends Fragment {

    private static final int MAX_PLANT_NAMES = 20;

    private long plantId = -1;
    private PlantViewModel plantViewModel;
    private PlantDetailsViewModel plantDetailsViewModel;

    private TextView tvTitle;
    private ImageButton btnBack;
    private ShapeableImageView ivPreview;
    private MaterialButton btnPickPhoto;
    private MaterialButton btnRemovePhoto;

    // Multi-plant name additions (up to 20)
    private TextView tvPlantNamesCounter;
    private TextInputLayout tilName;
    private TextInputEditText etName;
    private MaterialButton btnAddPlantName;
    private TextView tvPlantsListLabel;
    private ChipGroup chipGroupPlantNames;
    private final List<String> plantNamesList = new ArrayList<>();

    // Plant details
    private TextInputEditText etScientificName;
    private TextInputEditText etLocation;
    private TextInputEditText etAcquisitionDate;
    private AutoCompleteTextView actvLight;
    private AutoCompleteTextView actvHumidity;
    private TextInputEditText etObservations;

    // Care switches & frequencies
    private MaterialSwitch switchWatering;
    private AutoCompleteTextView actvWateringFreq;
    private MaterialSwitch switchFertilizing;
    private AutoCompleteTextView actvFertilizingFreq;
    private MaterialSwitch switchPruning;
    private AutoCompleteTextView actvPruningFreq;

    private MaterialButton btnSave;
    private Uri selectedPhotoUri = null;
    private String currentPhotoUriString = null;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    Uri savedUri = ImageUtils.copyAndCompressImage(requireContext(), uri);
                    if (savedUri != null) {
                        selectedPhotoUri = savedUri;
                        currentPhotoUriString = savedUri.toString();
                        ivPreview.setImageURI(savedUri);
                        btnRemovePhoto.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plantId = getArguments().getLong("plantId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_plant, container, false);

        tvTitle = view.findViewById(R.id.tv_add_edit_title);
        btnBack = view.findViewById(R.id.btn_back_add_plant);
        ivPreview = view.findViewById(R.id.iv_plant_preview);
        btnPickPhoto = view.findViewById(R.id.btn_pick_photo);
        btnRemovePhoto = view.findViewById(R.id.btn_remove_photo);

        // Multi plant names
        tvPlantNamesCounter = view.findViewById(R.id.tv_plant_names_counter);
        tilName = view.findViewById(R.id.til_plant_name);
        etName = view.findViewById(R.id.et_plant_name);
        btnAddPlantName = view.findViewById(R.id.btn_add_plant_name);
        tvPlantsListLabel = view.findViewById(R.id.tv_plants_list_label);
        chipGroupPlantNames = view.findViewById(R.id.chip_group_plant_names);

        // Details
        etScientificName = view.findViewById(R.id.et_scientific_name);
        etLocation = view.findViewById(R.id.et_location);
        etAcquisitionDate = view.findViewById(R.id.et_acquisition_date);
        actvLight = view.findViewById(R.id.actv_light);
        actvHumidity = view.findViewById(R.id.actv_humidity);
        etObservations = view.findViewById(R.id.et_observations);

        // Care
        switchWatering = view.findViewById(R.id.switch_watering);
        actvWateringFreq = view.findViewById(R.id.actv_watering_freq);
        switchFertilizing = view.findViewById(R.id.switch_fertilizing);
        actvFertilizingFreq = view.findViewById(R.id.actv_fertilizing_freq);
        switchPruning = view.findViewById(R.id.switch_pruning);
        actvPruningFreq = view.findViewById(R.id.actv_pruning_freq);

        btnSave = view.findViewById(R.id.btn_save_plant);

        setupDropdowns();
        setupEvents(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        plantViewModel = new ViewModelProvider(requireActivity()).get(PlantViewModel.class);
        plantDetailsViewModel = new ViewModelProvider(requireActivity()).get(PlantDetailsViewModel.class);

        if (plantId > 0) {
            tvTitle.setText(R.string.edit_plant_title);
            // In edit mode, hide batch adding container
            View namesContainer = view.findViewById(R.id.card_plant_names_container);
            if (namesContainer != null) {
                if (btnAddPlantName != null) btnAddPlantName.setVisibility(View.GONE);
                if (tvPlantNamesCounter != null) tvPlantNamesCounter.setVisibility(View.GONE);
                if (chipGroupPlantNames != null) chipGroupPlantNames.setVisibility(View.GONE);
                if (tvPlantsListLabel != null) tvPlantsListLabel.setVisibility(View.GONE);
            }
            plantDetailsViewModel.getPlant(plantId).observe(getViewLifecycleOwner(), plant -> {
                if (plant != null) {
                    populatePlantFields(plant);
                }
            });
        } else {
            updatePlantNamesCounter();
        }
    }

    private void setupDropdowns() {
        String[] lights = new String[]{"Pouca luz", "Luz indireta", "Luz intensa indireta", "Luz direta"};
        actvLight.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, lights));
        actvLight.setText("Luz indireta", false);

        String[] humidities = new String[]{"Baixa", "Moderada", "Alta"};
        actvHumidity.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, humidities));
        actvHumidity.setText("Moderada", false);

        String[] waterFreqs = new String[]{"Diariamente (1 dia)", "A cada 2 dias", "A cada 3 dias", "Semanalmente (7 dias)", "A cada 15 dias", "Mensalmente (30 dias)"};
        actvWateringFreq.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, waterFreqs));
        actvWateringFreq.setText("A cada 3 dias", false);

        String[] fertFreqs = new String[]{"Semanalmente (7 dias)", "A cada 15 dias", "Mensalmente (30 dias)"};
        actvFertilizingFreq.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, fertFreqs));
        actvFertilizingFreq.setText("Mensalmente (30 dias)", false);

        String[] pruneFreqs = new String[]{"Mensalmente (30 dias)", "A cada 60 dias", "A cada 90 dias"};
        actvPruningFreq.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, pruneFreqs));
        actvPruningFreq.setText("A cada 60 dias", false);
    }

    private void setupEvents(View rootView) {
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        btnPickPhoto.setOnClickListener(v -> {
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnRemovePhoto.setOnClickListener(v -> {
            selectedPhotoUri = null;
            currentPhotoUriString = null;
            ivPreview.setImageResource(R.drawable.plant_care_icon);
            btnRemovePhoto.setVisibility(View.GONE);
        });

        etAcquisitionDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, dayOfMonth);
                etAcquisitionDate.setText(DateUtils.formatDate(chosen.getTimeInMillis()));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        // Add plant name to list (supports up to 20 plant names in same field)
        if (btnAddPlantName != null) {
            btnAddPlantName.setOnClickListener(v -> addPlantNameToList());
        }

        if (etName != null) {
            etName.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    if (plantId <= 0) {
                        addPlantNameToList();
                        return true;
                    }
                }
                return false;
            });
        }

        btnSave.setOnClickListener(v -> savePlant());

        ImageButton btnSaveToolbar = rootView.findViewById(R.id.btn_save_toolbar);
        if (btnSaveToolbar != null) {
            btnSaveToolbar.setOnClickListener(v -> savePlant());
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void addPlantNameToList() {
        if (etName == null) return;
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_required));
            return;
        }
        tilName.setError(null);

        if (plantNamesList.size() >= MAX_PLANT_NAMES) {
            Toast.makeText(requireContext(), R.string.toast_max_plants_reached, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check duplicate
        for (String existing : plantNamesList) {
            if (existing.equalsIgnoreCase(name)) {
                Toast.makeText(requireContext(), R.string.toast_plant_name_duplicate, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        plantNamesList.add(name);
        addChipForPlantName(name);
        etName.setText("");
        updatePlantNamesCounter();
    }

    private void addChipForPlantName(String plantName) {
        if (chipGroupPlantNames == null) return;

        Chip chip = new Chip(requireContext());
        chip.setText(plantName);
        chip.setCloseIconVisible(true);
        chip.setCloseIconResource(R.drawable.ic_close);
        chip.setChipBackgroundColorResource(R.color.sage_light);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_green_dark));
        chip.setCloseIconTintResource(R.color.status_error);
        chip.setTextSize(13f);

        chip.setOnCloseIconClickListener(v -> {
            chipGroupPlantNames.removeView(chip);
            plantNamesList.remove(plantName);
            updatePlantNamesCounter();
        });

        chipGroupPlantNames.addView(chip);
    }

    private void updatePlantNamesCounter() {
        if (tvPlantNamesCounter != null) {
            tvPlantNamesCounter.setText(getString(R.string.plant_counter_format, plantNamesList.size(), MAX_PLANT_NAMES));
        }
        if (tvPlantsListLabel != null) {
            tvPlantsListLabel.setVisibility(plantNamesList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void populatePlantFields(PlantEntity plant) {
        etName.setText(plant.getName());
        etScientificName.setText(plant.getScientificName());
        etLocation.setText(plant.getLocation());
        etAcquisitionDate.setText(plant.getAcquisitionDate());
        if (plant.getLightLevel() != null) actvLight.setText(plant.getLightLevel(), false);
        if (plant.getHumidityLevel() != null) actvHumidity.setText(plant.getHumidityLevel(), false);
        etObservations.setText(plant.getDescription());

        if (plant.getPhotoUri() != null && !plant.getPhotoUri().isEmpty()) {
            currentPhotoUriString = plant.getPhotoUri();
            ImageUtils.loadIntoImageView(requireContext(), ivPreview, plant.getPhotoUri());
            btnRemovePhoto.setVisibility(View.VISIBLE);
        }
    }

    private void savePlant() {
        int waterDays = parseDaysFromText(actvWateringFreq.getText().toString(), 3);
        int fertDays = parseDaysFromText(actvFertilizingFreq.getText().toString(), 30);
        int pruneDays = parseDaysFromText(actvPruningFreq.getText().toString(), 60);

        String scientificName = etScientificName.getText() != null ? etScientificName.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        String acqDate = etAcquisitionDate.getText() != null ? etAcquisitionDate.getText().toString().trim() : "";
        String light = actvLight.getText().toString();
        String humidity = actvHumidity.getText().toString();
        String observations = etObservations.getText() != null ? etObservations.getText().toString().trim() : "";

        // CASE 1: Editing existing plant
        if (plantId > 0) {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                tilName.setError(getString(R.string.error_name_required));
                return;
            }
            tilName.setError(null);

            PlantEntity plant = new PlantEntity();
            plant.setId(plantId);
            plant.setName(name);
            plant.setScientificName(scientificName);
            plant.setLocation(location);
            plant.setAcquisitionDate(acqDate);
            plant.setLightLevel(light);
            plant.setHumidityLevel(humidity);
            plant.setDescription(observations);
            plant.setPhotoUri(currentPhotoUriString);

            plantViewModel.savePlant(
                    plant,
                    waterDays, switchWatering.isChecked(),
                    fertDays, switchFertilizing.isChecked(),
                    pruneDays, switchPruning.isChecked(),
                    savedId -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), R.string.plant_saved_success, Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(btnSave).navigateUp();
                            });
                        }
                    }
            );
            return;
        }

        // CASE 2: Creating plants
        // Check if there are chips added in the list
        if (!plantNamesList.isEmpty()) {
            // Also check if user typed a name and didn't hit (+)
            String typedName = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (!typedName.isEmpty() && !plantNamesList.contains(typedName) && plantNamesList.size() < MAX_PLANT_NAMES) {
                plantNamesList.add(typedName);
            }

            List<PlantEntity> plantsToInsert = new ArrayList<>();
            for (String plantName : plantNamesList) {
                PlantEntity plant = new PlantEntity();
                plant.setName(plantName);
                plant.setScientificName(scientificName);
                plant.setLocation(location);
                plant.setAcquisitionDate(acqDate);
                plant.setLightLevel(light);
                plant.setHumidityLevel(humidity);
                plant.setDescription(observations);
                plant.setPhotoUri(currentPhotoUriString);
                plantsToInsert.add(plant);
            }

            plantViewModel.saveMultiplePlants(
                    plantsToInsert,
                    waterDays, switchWatering.isChecked(),
                    fertDays, switchFertilizing.isChecked(),
                    pruneDays, switchPruning.isChecked(),
                    () -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                String msg = getString(R.string.toast_multiple_plants_saved, plantsToInsert.size());
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(btnSave).navigateUp();
                            });
                        }
                    }
            );
        } else {
            // No chips added, check single name in input field
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                tilName.setError(getString(R.string.error_name_required));
                return;
            }
            tilName.setError(null);

            PlantEntity plant = new PlantEntity();
            plant.setName(name);
            plant.setScientificName(scientificName);
            plant.setLocation(location);
            plant.setAcquisitionDate(acqDate);
            plant.setLightLevel(light);
            plant.setHumidityLevel(humidity);
            plant.setDescription(observations);
            plant.setPhotoUri(currentPhotoUriString);

            plantViewModel.savePlant(
                    plant,
                    waterDays, switchWatering.isChecked(),
                    fertDays, switchFertilizing.isChecked(),
                    pruneDays, switchPruning.isChecked(),
                    savedId -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), R.string.plant_saved_success, Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(btnSave).navigateUp();
                            });
                        }
                    }
            );
        }
    }

    private int parseDaysFromText(String text, int defaultDays) {
        if (text == null) return defaultDays;
        if (text.contains("1 dia")) return 1;
        if (text.contains("2 dias")) return 2;
        if (text.contains("3 dias")) return 3;
        if (text.contains("7 dias")) return 7;
        if (text.contains("15 dias")) return 15;
        if (text.contains("30 dias")) return 30;
        if (text.contains("60 dias")) return 60;
        if (text.contains("90 dias")) return 90;
        return defaultDays;
    }
}
