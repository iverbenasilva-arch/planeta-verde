package com.example.plantgreencare.ui.settings;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.plantgreencare.data.entities.AppSettingsEntity;
import com.example.plantgreencare.data.repository.SettingsRepository;
import com.example.plantgreencare.viewmodel.SettingsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsFragment extends Fragment {

    private static final String PREFS_SESSION = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_LOGIN_PROVIDER = "login_provider";

    private SettingsViewModel viewModel;

    // Account & Profile views
    private ImageView ivUserAvatar;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private LinearLayout layoutLoginButtons;
    private MaterialButton btnLoginGoogle;
    private MaterialButton btnLoginEmail;
    private MaterialButton btnLogout;

    // Settings views
    private MaterialSwitch switchNotifications;
    private LinearLayout layoutTimePicker;
    private TextView tvReminderTime;

    private RadioGroup rgTheme;
    private MaterialSwitch switchHighContrast;
    private MaterialSwitch switchVibration;

    private MaterialButton btnExport;
    private MaterialButton btnImport;
    private MaterialButton btnPrivacyPolicy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Account
        ivUserAvatar = view.findViewById(R.id.iv_user_avatar);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        layoutLoginButtons = view.findViewById(R.id.layout_login_buttons);
        btnLoginGoogle = view.findViewById(R.id.btn_login_google);
        btnLoginEmail = view.findViewById(R.id.btn_login_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Notifications
        switchNotifications = view.findViewById(R.id.switch_notifications);
        layoutTimePicker = view.findViewById(R.id.layout_time_picker);
        tvReminderTime = view.findViewById(R.id.tv_reminder_time);

        // Appearance
        rgTheme = view.findViewById(R.id.rg_theme);
        switchHighContrast = view.findViewById(R.id.switch_high_contrast);
        switchVibration = view.findViewById(R.id.switch_vibration);

        // Data & About
        btnExport = view.findViewById(R.id.btn_export_data);
        btnImport = view.findViewById(R.id.btn_import_data);
        btnPrivacyPolicy = view.findViewById(R.id.btn_privacy_policy);

        setupAccountSection();
        setupEvents();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        viewModel.getSettings().observe(getViewLifecycleOwner(), this::bindSettings);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAccountUI();
    }

    private void setupAccountSection() {
        refreshAccountUI();

        btnLoginGoogle.setOnClickListener(v -> showGoogleLoginDialog());
        btnLoginEmail.setOnClickListener(v -> showEmailLoginDialog());
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void refreshAccountUI() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        String name = prefs.getString(KEY_USER_NAME, "");
        String email = prefs.getString(KEY_USER_EMAIL, "");
        String provider = prefs.getString(KEY_LOGIN_PROVIDER, "");

        if (isLoggedIn && !email.isEmpty()) {
            tvUserName.setText(name.isEmpty() ? email : name);
            String providerLabel = "google".equalsIgnoreCase(provider) ? "Conta Google conectada" : "E-mail verificado";
            tvUserEmail.setText(email + " • " + providerLabel);
            layoutLoginButtons.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            tvUserName.setText(R.string.settings_account_guest);
            tvUserEmail.setText(R.string.settings_account_guest_desc);
            layoutLoginButtons.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
        }
    }

    private void showGoogleLoginDialog() {
        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 24, 48, 12);

        TextView desc = new TextView(requireContext());
        desc.setText(R.string.dialog_login_google_desc);
        desc.setTextSize(14f);
        desc.setTextColor(getResources().getColor(R.color.text_secondary, null));
        dialogLayout.addView(desc);

        final EditText etGoogleEmail = new EditText(requireContext());
        etGoogleEmail.setHint(R.string.dialog_login_google_email_hint);
        etGoogleEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etGoogleEmail.setTextSize(15f);
        etGoogleEmail.setSingleLine(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 16;
        etGoogleEmail.setLayoutParams(params);
        dialogLayout.addView(etGoogleEmail);

        new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle(R.string.dialog_login_google_title)
                .setView(dialogLayout)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.settings_login_google, (dialog, which) -> {
                    String email = etGoogleEmail.getText().toString().trim();
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(getContext(), R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String displayName = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                    displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

                    saveUserSession(displayName, email, "google");
                    refreshAccountUI();
                    String msg = getString(R.string.msg_login_success, displayName);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showEmailLoginDialog() {
        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 24, 48, 12);

        final EditText etName = new EditText(requireContext());
        etName.setHint(R.string.dialog_login_name_hint);
        etName.setTextSize(15f);
        etName.setSingleLine(true);
        dialogLayout.addView(etName);

        final EditText etEmail = new EditText(requireContext());
        etEmail.setHint(R.string.dialog_login_email_hint);
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setTextSize(15f);
        etEmail.setSingleLine(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 20;
        etEmail.setLayoutParams(params);
        dialogLayout.addView(etEmail);

        new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle(R.string.dialog_login_email_title)
                .setView(dialogLayout)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Conectar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();

                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(getContext(), R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (name.isEmpty()) {
                        name = email.substring(0, email.indexOf("@"));
                    }

                    saveUserSession(name, email, "email");
                    refreshAccountUI();
                    String msg = getString(R.string.msg_login_success, name);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void saveUserSession(String name, String email, String provider) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_LOGIN_PROVIDER, provider)
                .apply();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle(R.string.settings_logout)
                .setMessage("Deseja realmente desconectar sua conta?")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Desconectar", (dialog, which) -> {
                    SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean(KEY_IS_LOGGED_IN, false)
                            .remove(KEY_USER_NAME)
                            .remove(KEY_USER_EMAIL)
                            .remove(KEY_LOGIN_PROVIDER)
                            .apply();
                    refreshAccountUI();
                    Toast.makeText(getContext(), R.string.msg_logout_success, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void bindSettings(AppSettingsEntity settings) {
        if (settings == null) return;

        switchNotifications.setChecked(settings.isNotificationsEnabled());
        String timeStr = String.format("%02d:%02d", settings.getNotificationHour(), settings.getNotificationMinute());
        tvReminderTime.setText(timeStr);

        if (settings.getDarkMode() == 1) {
            rgTheme.check(R.id.rb_theme_light);
        } else if (settings.getDarkMode() == 2) {
            rgTheme.check(R.id.rb_theme_dark);
        } else {
            rgTheme.check(R.id.rb_theme_system);
        }

        switchHighContrast.setChecked(settings.isHighContrast());
        switchVibration.setChecked(!settings.isReducedMotion());
    }

    private void setupEvents() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setNotificationsEnabled(isChecked);
        });

        layoutTimePicker.setOnClickListener(v -> {
            AppSettingsEntity s = viewModel.getSettings().getValue();
            int curHour = s != null ? s.getNotificationHour() : 8;
            int curMinute = s != null ? s.getNotificationMinute() : 0;

            TimePickerDialog tpd = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                viewModel.setReminderTime(hourOfDay, minute);
                tvReminderTime.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, curHour, curMinute, true);
            tpd.show();
        });

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_theme_light) {
                viewModel.setDarkMode(1);
            } else if (checkedId == R.id.rb_theme_dark) {
                viewModel.setDarkMode(2);
            } else {
                viewModel.setDarkMode(0);
            }
        });

        switchHighContrast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setHighContrast(isChecked);
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setVibration(isChecked);
        });

        btnExport.setOnClickListener(v -> {
            viewModel.exportData(new SettingsRepository.OnBackupCallback() {
                @Override
                public void onSuccess(String jsonString) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/json");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Plant Green Care - Backup");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, jsonString);
                            startActivity(Intent.createChooser(shareIntent, "Exportar Backup JSON"));
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
                    }
                }
            });
        });

        btnImport.setOnClickListener(v -> showImportDialog());

        btnPrivacyPolicy.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                    .setTitle(R.string.settings_privacy_policy)
                    .setMessage("O Plant Green Care respeita sua privacidade. Todos os dados das suas plantas e cuidados são armazenados estritamente no seu dispositivo local. Nenhuma informação pessoal ou foto é enviada a servidores externos sem sua ação explícita.")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void showImportDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Cole o JSON de backup aqui");
        input.setMinLines(4);

        new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle(R.string.settings_import_data)
                .setMessage("Atenção: Os dados importados serão adicionados às suas plantas cadastradas.")
                .setView(input)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton("Importar", (dialog, which) -> {
                    String json = input.getText().toString();
                    if (!json.trim().isEmpty()) {
                        viewModel.importData(json, new SettingsRepository.OnRestoreCallback() {
                            @Override
                            public void onSuccess(int count) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), count + " plantas importadas com sucesso!", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }

                            @Override
                            public void onError(String message) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        });
                    }
                })
                .show();
    }
}
