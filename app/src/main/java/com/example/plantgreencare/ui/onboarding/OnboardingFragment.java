package com.example.plantgreencare.ui.onboarding;

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

import com.example.R;
import com.example.plantgreencare.viewmodel.SettingsViewModel;
import com.google.android.material.button.MaterialButton;

public class OnboardingFragment extends Fragment {

    private int currentStep = 0;
    private SettingsViewModel settingsViewModel;

    private TextView tvTitle;
    private TextView tvDesc;
    private TextView tvStepIndicator;
    private MaterialButton btnAction;
    private MaterialButton btnSkip;

    private final String[] titles = new String[]{
            "Bem-vindo ao Plant Green Care 🌱",
            "Cadastre suas plantas",
            "Não esqueça dos cuidados",
            "Pronto para começar?"
    };

    private final String[] descriptions = new String[]{
            "Organize os cuidados das suas plantas de forma simples e natural.",
            "Registre suas plantas com fotos, nomes, locais e acompanhe cada detalhe.",
            "Receba lembretes automáticos para regar, adubar e podar no tempo certo.",
            "Suas plantas merecem o melhor cuidado. Vamos dar o primeiro passo!"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);

        tvTitle = view.findViewById(R.id.tv_onboarding_title);
        tvDesc = view.findViewById(R.id.tv_onboarding_desc);
        tvStepIndicator = view.findViewById(R.id.tv_onboarding_step_indicator);
        btnAction = view.findViewById(R.id.btn_onboarding_action);
        btnSkip = view.findViewById(R.id.btn_onboarding_skip);

        updateStepUI();

        btnAction.setOnClickListener(v -> {
            if (currentStep < 3) {
                currentStep++;
                updateStepUI();
            } else {
                finishOnboarding(true);
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding(false));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
    }

    private void updateStepUI() {
        tvTitle.setText(titles[currentStep]);
        tvDesc.setText(descriptions[currentStep]);
        tvStepIndicator.setText("Passo " + (currentStep + 1) + " de 4");

        if (currentStep == 0) {
            btnAction.setText(R.string.onboarding_start);
        } else if (currentStep < 3) {
            btnAction.setText(R.string.onboarding_next);
        } else {
            btnAction.setText(R.string.onboarding_first_plant);
        }
    }

    private void finishOnboarding(boolean openAddPlant) {
        if (getContext() != null) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences(com.example.PlantCareApplication.PREFS_NAME, android.content.Context.MODE_PRIVATE);
            prefs.edit().putBoolean(com.example.PlantCareApplication.KEY_ONBOARDING_COMPLETED, true).apply();
        }
        if (settingsViewModel != null) {
            settingsViewModel.setOnboardingCompleted();
        }
        if (getView() != null) {
            try {
                Navigation.findNavController(getView()).navigate(R.id.action_onboarding_to_home);
            } catch (Exception ignored) {}
        }
    }
}
