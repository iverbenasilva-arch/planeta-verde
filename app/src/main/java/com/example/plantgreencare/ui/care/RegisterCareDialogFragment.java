package com.example.plantgreencare.ui.care;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.plantgreencare.utils.DateUtils;
import com.example.plantgreencare.viewmodel.CareViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class RegisterCareDialogFragment extends DialogFragment {

    public interface CareRegisteredCallback {
        void onCareRegistered();
    }

    private static final String ARG_PLANT_ID = "plant_id";
    private static final String ARG_PLANT_NAME = "plant_name";
    private static final String ARG_CARE_TYPE_ID = "care_type_id";

    private long plantId;
    private String plantName;
    private long careTypeId;
    private long selectedDateMillis = System.currentTimeMillis();
    private CareRegisteredCallback callback;

    public static RegisterCareDialogFragment newInstance(long plantId, String plantName, long careTypeId) {
        RegisterCareDialogFragment fragment = new RegisterCareDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLANT_ID, plantId);
        args.putString(ARG_PLANT_NAME, plantName);
        args.putLong(ARG_CARE_TYPE_ID, careTypeId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCallback(CareRegisteredCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plantId = getArguments().getLong(ARG_PLANT_ID);
            plantName = getArguments().getString(ARG_PLANT_NAME, "Planta");
            careTypeId = getArguments().getLong(ARG_CARE_TYPE_ID, 1);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_register_care, null);

        TextView tvPlantName = view.findViewById(R.id.tv_dialog_plant_name);
        AutoCompleteTextView actvType = view.findViewById(R.id.actv_care_type);
        TextInputEditText etDate = view.findViewById(R.id.et_care_date);
        TextInputEditText etNotes = view.findViewById(R.id.et_care_notes);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_care);
        MaterialButton btnConfirm = view.findViewById(R.id.btn_confirm_care);

        tvPlantName.setText("Planta: " + plantName);

        String[] types = new String[]{"Rega", "Adubação", "Poda", "Exposição à luz", "Pulverização", "Outro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        actvType.setAdapter(adapter);

        // Preencher tipo correspondente
        if (careTypeId == 2) actvType.setText("Adubação", false);
        else if (careTypeId == 3) actvType.setText("Poda", false);
        else actvType.setText("Rega", false);

        etDate.setText(DateUtils.getTodayFormatted());

        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDateMillis);
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth, 8, 0);
                        selectedDateMillis = chosen.getTimeInMillis();
                        etDate.setText(DateUtils.formatDate(selectedDateMillis));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        CareViewModel careViewModel = new ViewModelProvider(requireActivity()).get(CareViewModel.class);

        btnConfirm.setOnClickListener(v -> {
            String selectedTypeName = actvType.getText().toString();
            long typeId = 1;
            if ("Adubação".equalsIgnoreCase(selectedTypeName)) typeId = 2;
            else if ("Poda".equalsIgnoreCase(selectedTypeName)) typeId = 3;
            else if ("Exposição à luz".equalsIgnoreCase(selectedTypeName)) typeId = 4;
            else if ("Pulverização".equalsIgnoreCase(selectedTypeName)) typeId = 5;

            String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

            long finalTypeId = typeId;
            careViewModel.registerCare(
                    new com.example.plantgreencare.data.entities.CareScheduleEntity(plantId, finalTypeId, 3, selectedDateMillis, true, true),
                    notes,
                    () -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), R.string.care_registered_success, Toast.LENGTH_SHORT).show();
                                if (callback != null) callback.onCareRegistered();
                                dismiss();
                            });
                        }
                    }
            );
        });

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
