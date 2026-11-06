package com.example.plantgreencare.ui.plants;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.R;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.utils.PlantShareManager;
import com.example.plantgreencare.viewmodel.SocialViewModel;
import com.google.android.material.button.MaterialButton;

public class SharePlantDialogFragment extends DialogFragment {

    private PlantEntity plant;

    public static SharePlantDialogFragment newInstance(PlantEntity plant) {
        SharePlantDialogFragment fragment = new SharePlantDialogFragment();
        fragment.setPlant(plant);
        return fragment;
    }

    public void setPlant(PlantEntity plant) {
        this.plant = plant;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_share_plant, null);

        TextView tvPreview = view.findViewById(R.id.tv_share_preview);
        MaterialButton btnFacebook = view.findViewById(R.id.btn_share_facebook);
        MaterialButton btnInstagram = view.findViewById(R.id.btn_share_instagram);
        MaterialButton btnSystem = view.findViewById(R.id.btn_share_system);
        MaterialButton btnCancel = view.findViewById(R.id.btn_share_cancel);

        SocialViewModel socialViewModel = new ViewModelProvider(requireActivity()).get(SocialViewModel.class);

        if (plant != null) {
            String previewText = PlantShareManager.buildPlantShareText(requireContext(), plant);
            tvPreview.setText(previewText);

            btnFacebook.setOnClickListener(v -> {
                socialViewModel.shareToFacebook(plant, previewText);
                dismiss();
            });

            btnInstagram.setOnClickListener(v -> {
                Uri photoUri = plant.getPhotoUri() != null ? Uri.parse(plant.getPhotoUri()) : null;
                socialViewModel.shareToInstagram(photoUri, previewText);
                dismiss();
            });

            btnSystem.setOnClickListener(v -> {
                PlantShareManager.shareSystemWide(requireContext(), plant);
                dismiss();
            });
        }

        btnCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
