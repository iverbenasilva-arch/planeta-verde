package com.example.plantgreencare.ui.care;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.plantgreencare.adapter.CareTodayAdapter;
import com.example.plantgreencare.viewmodel.CareViewModel;
import com.google.android.material.chip.ChipGroup;

public class CareFragment extends Fragment {

    private CareViewModel careViewModel;
    private CareTodayAdapter careAdapter;

    private ChipGroup chipGroupFilters;
    private RecyclerView rvTasks;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_care, container, false);

        chipGroupFilters = view.findViewById(R.id.chip_group_care_filters);
        rvTasks = view.findViewById(R.id.rv_care_tasks);
        layoutEmpty = view.findViewById(R.id.layout_empty_care);

        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        careAdapter = new CareTodayAdapter(requireContext(), taskItem -> {
            RegisterCareDialogFragment dialog = RegisterCareDialogFragment.newInstance(
                    taskItem.getPlant().getId(),
                    taskItem.getPlant().getName(),
                    taskItem.getSchedule().getCareTypeId()
            );
            dialog.show(getParentFragmentManager(), "RegisterCareFromCareTab");
        });
        rvTasks.setAdapter(careAdapter);

        setupFilters();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        careViewModel = new ViewModelProvider(requireActivity()).get(CareViewModel.class);

        careViewModel.getCareTasks().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                rvTasks.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvTasks.setVisibility(View.VISIBLE);
                careAdapter.submitList(items);
            }
        });
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (careViewModel == null || checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_care_overdue) {
                careViewModel.setFilter(CareViewModel.CareFilter.OVERDUE);
            } else if (id == R.id.chip_care_tomorrow) {
                careViewModel.setFilter(CareViewModel.CareFilter.TOMORROW);
            } else if (id == R.id.chip_care_week) {
                careViewModel.setFilter(CareViewModel.CareFilter.THIS_WEEK);
            } else if (id == R.id.chip_care_all) {
                careViewModel.setFilter(CareViewModel.CareFilter.ALL);
            } else {
                careViewModel.setFilter(CareViewModel.CareFilter.TODAY);
            }
        });
    }
}
