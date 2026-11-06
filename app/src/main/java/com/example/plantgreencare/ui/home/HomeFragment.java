package com.example.plantgreencare.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.plantgreencare.adapter.CareTodayAdapter;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.ui.care.RegisterCareDialogFragment;
import com.example.plantgreencare.viewmodel.HomeViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private CareTodayAdapter todayAdapter;

    private TextView tvPlantsCount;
    private TextView tvTodayCount;
    private TextView tvOverdueCount;
    private TextView tvHomeGreeting;
    private TextView tvNextCarePlant;
    private TextView tvNextCareInfo;
    private MaterialButton btnNextCareRegister;
    private TextView tvEmptyToday;
    private RecyclerView rvToday;
    private ExtendedFloatingActionButton fabAddPlant;
    private MaterialCardView cardNextCare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvHomeGreeting = view.findViewById(R.id.tv_home_greeting);
        tvPlantsCount = view.findViewById(R.id.tv_stat_plants_count);
        tvTodayCount = view.findViewById(R.id.tv_stat_today_count);
        tvOverdueCount = view.findViewById(R.id.tv_stat_overdue_count);
        tvNextCarePlant = view.findViewById(R.id.tv_next_care_plant_name);
        tvNextCareInfo = view.findViewById(R.id.tv_next_care_info);
        btnNextCareRegister = view.findViewById(R.id.btn_next_care_register);
        tvEmptyToday = view.findViewById(R.id.tv_empty_today_care);
        rvToday = view.findViewById(R.id.rv_today_care);
        fabAddPlant = view.findViewById(R.id.fab_add_plant);
        cardNextCare = view.findViewById(R.id.card_next_care);

        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));
        todayAdapter = new CareTodayAdapter(requireContext(), taskItem -> {
            RegisterCareDialogFragment dialog = RegisterCareDialogFragment.newInstance(
                    taskItem.getPlant().getId(),
                    taskItem.getPlant().getName(),
                    taskItem.getSchedule().getCareTypeId()
            );
            dialog.show(getParentFragmentManager(), "RegisterCareHome");
        });
        rvToday.setAdapter(todayAdapter);

        fabAddPlant.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_addPlant);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        homeViewModel.getTotalPlantsCount().observe(getViewLifecycleOwner(), count -> {
            tvPlantsCount.setText(String.valueOf(count != null ? count : 0));
        });

        homeViewModel.getTodaySchedules().observe(getViewLifecycleOwner(), schedules -> {
            int count = schedules != null ? schedules.size() : 0;
            tvTodayCount.setText(String.valueOf(count));
        });

        homeViewModel.getOverdueSchedules().observe(getViewLifecycleOwner(), schedules -> {
            int count = schedules != null ? schedules.size() : 0;
            tvOverdueCount.setText(String.valueOf(count));
        });

        homeViewModel.getTodayCareTasks().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                tvEmptyToday.setVisibility(View.VISIBLE);
                rvToday.setVisibility(View.GONE);
            } else {
                tvEmptyToday.setVisibility(View.GONE);
                rvToday.setVisibility(View.VISIBLE);
                todayAdapter.submitList(items);
            }
        });

        homeViewModel.getNextCareInfo().observe(getViewLifecycleOwner(), info -> {
            if (info != null) {
                tvNextCareInfo.setText(info);
            }
        });

        homeViewModel.getActiveNextPlant().observe(getViewLifecycleOwner(), plant -> {
            if (plant != null) {
                tvNextCarePlant.setText(plant.getName());
                btnNextCareRegister.setVisibility(View.VISIBLE);
                btnNextCareRegister.setOnClickListener(v -> {
                    CareScheduleEntity sched = homeViewModel.getActiveNextSchedule().getValue();
                    if (sched != null) {
                        RegisterCareDialogFragment dialog = RegisterCareDialogFragment.newInstance(
                                plant.getId(),
                                plant.getName(),
                                sched.getCareTypeId()
                        );
                        dialog.show(getParentFragmentManager(), "RegisterCareNext");
                    }
                });
            } else {
                tvNextCarePlant.setText("Nenhum cuidado agendado");
                btnNextCareRegister.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && tvHomeGreeting != null) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
            String userName = prefs.getString("user_name", "");
            if (isLoggedIn && !userName.isEmpty()) {
                tvHomeGreeting.setText("Olá, " + userName + "! Como estão suas plantas hoje?");
            } else {
                tvHomeGreeting.setText(R.string.home_greeting);
            }
        }
    }
}
