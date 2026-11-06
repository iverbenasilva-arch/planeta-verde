package com.example.plantgreencare.ui.plants;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.plantgreencare.adapter.PlantAdapter;
import com.example.plantgreencare.model.PlantCardItem;
import com.example.plantgreencare.ui.care.RegisterCareDialogFragment;
import com.example.plantgreencare.viewmodel.PlantViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class PlantsFragment extends Fragment {

    private PlantViewModel plantViewModel;
    private PlantAdapter plantAdapter;

    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvPlants;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMsg;
    private ExtendedFloatingActionButton fabAddPlant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plants, container, false);

        etSearch = view.findViewById(R.id.et_search_plants);
        chipGroupFilters = view.findViewById(R.id.chip_group_plant_filters);
        rvPlants = view.findViewById(R.id.rv_plants);
        layoutEmpty = view.findViewById(R.id.layout_empty_plants);
        tvEmptyMsg = view.findViewById(R.id.tv_empty_plants_message);
        fabAddPlant = view.findViewById(R.id.fab_add_plant);

        rvPlants.setLayoutManager(new LinearLayoutManager(getContext()));
        plantAdapter = new PlantAdapter(requireContext(), new PlantAdapter.PlantActionListener() {
            @Override
            public void onViewDetails(PlantCardItem item) {
                Bundle args = new Bundle();
                args.putLong("plantId", item.getPlant().getId());
                Navigation.findNavController(rvPlants).navigate(R.id.action_plants_to_details, args);
            }

            @Override
            public void onQuickRegisterCare(PlantCardItem item) {
                RegisterCareDialogFragment dialog = RegisterCareDialogFragment.newInstance(
                        item.getPlant().getId(),
                        item.getPlant().getName(),
                        1 // Rega como padrão rápido
                );
                dialog.show(getParentFragmentManager(), "QuickRegisterPlant");
            }
        });
        rvPlants.setAdapter(plantAdapter);

        fabAddPlant.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_plants_to_addPlant);
        });

        setupFiltersAndSearch();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        plantViewModel = new ViewModelProvider(requireActivity()).get(PlantViewModel.class);

        plantViewModel.getFilteredPlants().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                rvPlants.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvPlants.setVisibility(View.VISIBLE);
                plantAdapter.submitList(items);
            }
        });
    }

    private void setupFiltersAndSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (plantViewModel != null) {
                    plantViewModel.setSearchQuery(s != null ? s.toString() : "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (plantViewModel == null || checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_filter_needs_care) {
                plantViewModel.setFilter(PlantViewModel.FilterMode.NEEDS_CARE);
            } else if (id == R.id.chip_filter_overdue) {
                plantViewModel.setFilter(PlantViewModel.FilterMode.OVERDUE);
            } else if (id == R.id.chip_filter_up_to_date) {
                plantViewModel.setFilter(PlantViewModel.FilterMode.UP_TO_DATE);
            } else {
                plantViewModel.setFilter(PlantViewModel.FilterMode.ALL);
            }
        });
    }
}
