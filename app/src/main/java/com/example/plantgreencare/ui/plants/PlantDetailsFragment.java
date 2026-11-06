package com.example.plantgreencare.ui.plants;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.plantgreencare.adapter.CareHistoryAdapter;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.ui.care.RegisterCareDialogFragment;
import com.example.plantgreencare.utils.CareScheduleCalculator;
import com.example.plantgreencare.utils.DateUtils;
import com.example.plantgreencare.utils.ImageUtils;
import com.example.plantgreencare.viewmodel.PlantDetailsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class PlantDetailsFragment extends Fragment {

    private long plantId = -1;
    private PlantDetailsViewModel viewModel;
    private PlantEntity currentPlant;

    private ShapeableImageView ivImage;
    private TextView tvName;
    private TextView tvScientific;
    private TextView tvLocation;
    private TextView tvLight;
    private TextView tvHumidity;
    private TextView tvAcquisition;
    private TextView tvObservations;

    private ImageButton btnBack;
    private ImageButton btnShare;
    private ImageButton btnEdit;
    private ImageButton btnDelete;
    private MaterialButton btnMainRegisterCare;
    private MaterialButton btnDetailsGoogleToxicity;
    private MaterialButton btnDetailsGoogleCare;

    private LinearLayout layoutSchedules;
    private RecyclerView rvHistory;
    private TextView tvNoHistory;
    private CareHistoryAdapter historyAdapter;

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
        View view = inflater.inflate(R.layout.fragment_plant_details, container, false);

        ivImage = view.findViewById(R.id.iv_details_image);
        tvName = view.findViewById(R.id.tv_details_name);
        tvScientific = view.findViewById(R.id.tv_details_scientific);
        tvLocation = view.findViewById(R.id.tv_details_location);
        tvLight = view.findViewById(R.id.tv_details_light);
        tvHumidity = view.findViewById(R.id.tv_details_humidity);
        tvAcquisition = view.findViewById(R.id.tv_details_acquisition);
        tvObservations = view.findViewById(R.id.tv_details_observations);

        btnBack = view.findViewById(R.id.btn_back_details);
        btnShare = view.findViewById(R.id.btn_share_details);
        btnEdit = view.findViewById(R.id.btn_edit_details);
        btnDelete = view.findViewById(R.id.btn_delete_details);
        btnMainRegisterCare = view.findViewById(R.id.btn_details_register_care);
        btnDetailsGoogleToxicity = view.findViewById(R.id.btn_details_google_toxicity);
        btnDetailsGoogleCare = view.findViewById(R.id.btn_details_google_care);

        layoutSchedules = view.findViewById(R.id.layout_schedules_container);
        rvHistory = view.findViewById(R.id.rv_care_history);
        tvNoHistory = view.findViewById(R.id.tv_no_history);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new CareHistoryAdapter(requireContext());
        rvHistory.setAdapter(historyAdapter);

        setupEvents();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PlantDetailsViewModel.class);

        if (plantId > 0) {
            viewModel.getPlant(plantId).observe(getViewLifecycleOwner(), plant -> {
                if (plant != null) {
                    currentPlant = plant;
                    bindPlantData(plant);
                }
            });

            viewModel.getSchedules(plantId).observe(getViewLifecycleOwner(), schedules -> {
                renderSchedules(schedules);
            });

            viewModel.getCareHistory(plantId).observe(getViewLifecycleOwner(), records -> {
                if (records == null || records.isEmpty()) {
                    tvNoHistory.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                } else {
                    tvNoHistory.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                    historyAdapter.submitList(records);
                }
            });
        }
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        btnShare.setOnClickListener(v -> {
            if (currentPlant != null) {
                SharePlantDialogFragment dialog = SharePlantDialogFragment.newInstance(currentPlant);
                dialog.show(getParentFragmentManager(), "SharePlantDialog");
            }
        });

        btnEdit.setOnClickListener(v -> {
            if (currentPlant != null) {
                Bundle args = new Bundle();
                args.putLong("plantId", currentPlant.getId());
                Navigation.findNavController(v).navigate(R.id.action_details_to_edit, args);
            }
        });

        btnDelete.setOnClickListener(v -> confirmDelete());

        btnMainRegisterCare.setOnClickListener(v -> {
            if (currentPlant != null) {
                RegisterCareDialogFragment dialog = RegisterCareDialogFragment.newInstance(
                        currentPlant.getId(),
                        currentPlant.getName(),
                        1 // Rega
                );
                dialog.show(getParentFragmentManager(), "RegisterCareDetails");
            }
        });

        if (btnDetailsGoogleToxicity != null) {
            btnDetailsGoogleToxicity.setOnClickListener(v -> openGoogleSearch(true));
        }

        if (btnDetailsGoogleCare != null) {
            btnDetailsGoogleCare.setOnClickListener(v -> openGoogleSearch(false));
        }
    }

    private void openGoogleSearch(boolean isToxicity) {
        String plantName = currentPlant != null && currentPlant.getName() != null ? currentPlant.getName() : "planta";
        String query;
        if (isToxicity) {
            query = "planta " + plantName + " e toxica venenosa para humanos e animais de estimacao";
        } else {
            query = "como cuidar de " + plantName + " frequencia rega adubacao iluminacao ideal";
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + encodedQuery));
            startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + query));
            startActivity(intent);
        }
    }

    private void bindPlantData(PlantEntity plant) {
        tvName.setText(plant.getName());
        tvScientific.setText(plant.getScientificName() != null ? plant.getScientificName() : "");
        tvLocation.setText("Local: " + (plant.getLocation() != null ? plant.getLocation() : "Não informado"));
        tvLight.setText("Luz: " + (plant.getLightLevel() != null ? plant.getLightLevel() : "Indireta"));
        tvHumidity.setText("Umidade: " + (plant.getHumidityLevel() != null ? plant.getHumidityLevel() : "Moderada"));

        if (plant.getAcquisitionDate() != null && !plant.getAcquisitionDate().isEmpty()) {
            tvAcquisition.setText("Data de aquisição: " + plant.getAcquisitionDate());
            tvAcquisition.setVisibility(View.VISIBLE);
        } else {
            tvAcquisition.setVisibility(View.GONE);
        }

        if (plant.getDescription() != null && !plant.getDescription().isEmpty()) {
            tvObservations.setText("Observações: " + plant.getDescription());
            tvObservations.setVisibility(View.VISIBLE);
        } else {
            tvObservations.setVisibility(View.GONE);
        }

        ImageUtils.loadIntoImageView(requireContext(), ivImage, plant.getPhotoUri());
    }

    private void renderSchedules(List<CareScheduleEntity> schedules) {
        layoutSchedules.removeAllViews();
        if (schedules == null || schedules.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText(R.string.details_no_schedules);
            empty.setTextColor(getResources().getColor(R.color.text_secondary, null));
            empty.setPadding(0, 16, 0, 16);
            layoutSchedules.addView(empty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (CareScheduleEntity schedule : schedules) {
            View row = inflater.inflate(R.layout.item_care_schedule_row, layoutSchedules, false);

            TextView tvTitle = row.findViewById(R.id.tv_schedule_title);
            TextView tvStatus = row.findViewById(R.id.tv_schedule_due_status);
            MaterialButton btnRegister = row.findViewById(R.id.btn_schedule_register);
            android.widget.ImageView ivIcon = row.findViewById(R.id.iv_schedule_type_icon);

            String typeName = getTypeName(schedule.getCareTypeId());
            tvTitle.setText(typeName);
            if (schedule.getCareTypeId() == 1 && ivIcon != null) {
                ivIcon.setImageResource(R.drawable.ic_water_drop);
            } else if (schedule.getCareTypeId() == 2 && ivIcon != null) {
                ivIcon.setImageResource(R.drawable.ic_plants);
            } else if (ivIcon != null) {
                ivIcon.setImageResource(R.drawable.ic_edit);
            }

            CareScheduleCalculator.CareStatus status = CareScheduleCalculator.getCareStatus(schedule.getNextDate());
            String dueText;
            if (status == CareScheduleCalculator.CareStatus.OVERDUE) {
                dueText = "Atrasado • " + DateUtils.formatDate(schedule.getNextDate());
                tvStatus.setTextColor(getResources().getColor(R.color.status_error, null));
            } else if (status == CareScheduleCalculator.CareStatus.PENDING_TODAY) {
                dueText = "Hoje • Frequência: " + schedule.getFrequency() + " dias";
                tvStatus.setTextColor(getResources().getColor(R.color.status_warning, null));
            } else {
                dueText = "Próximo: " + DateUtils.formatDate(schedule.getNextDate()) + " • A cada " + schedule.getFrequency() + " dias";
                tvStatus.setTextColor(getResources().getColor(R.color.status_success, null));
            }
            tvStatus.setText(dueText);

            btnRegister.setOnClickListener(v -> {
                if (currentPlant != null) {
                    RegisterCareDialogFragment dialog = RegisterCareDialogFragment.newInstance(
                            currentPlant.getId(),
                            currentPlant.getName(),
                            schedule.getCareTypeId()
                    );
                    dialog.show(getParentFragmentManager(), "RegisterCareFromRow");
                }
            });

            layoutSchedules.addView(row);
        }
    }

    private String getTypeName(long typeId) {
        if (typeId == 1) return "Rega";
        if (typeId == 2) return "Adubação";
        if (typeId == 3) return "Poda";
        return "Cuidado";
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    if (currentPlant != null) {
                        viewModel.deletePlant(currentPlant, () -> {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), R.string.plant_deleted, Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(btnDelete).navigateUp();
                                });
                            }
                        });
                    }
                })
                .show();
    }
}
