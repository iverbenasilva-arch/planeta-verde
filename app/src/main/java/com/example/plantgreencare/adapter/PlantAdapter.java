package com.example.plantgreencare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.plantgreencare.model.PlantCardItem;
import com.example.plantgreencare.utils.CareScheduleCalculator;
import com.example.plantgreencare.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    public interface PlantActionListener {
        void onViewDetails(PlantCardItem item);
        void onQuickRegisterCare(PlantCardItem item);
    }

    private final Context context;
    private final List<PlantCardItem> items = new ArrayList<>();
    private final PlantActionListener listener;

    public PlantAdapter(Context context, PlantActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<PlantCardItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantCardItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class PlantViewHolder extends RecyclerView.ViewHolder {

        private final ShapeableImageView ivThumb;
        private final TextView tvName;
        private final TextView tvScientific;
        private final TextView tvLocation;
        private final TextView tvStatusBadge;
        private final TextView tvLastCare;
        private final MaterialButton btnViewDetails;
        private final MaterialButton btnQuickRegister;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_plant_thumb);
            tvName = itemView.findViewById(R.id.tv_plant_name);
            tvScientific = itemView.findViewById(R.id.tv_plant_scientific);
            tvLocation = itemView.findViewById(R.id.tv_plant_location);
            tvStatusBadge = itemView.findViewById(R.id.tv_plant_status_badge);
            tvLastCare = itemView.findViewById(R.id.tv_last_care_date);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnQuickRegister = itemView.findViewById(R.id.btn_quick_register_care);
        }

        public void bind(PlantCardItem item) {
            tvName.setText(item.getPlant().getName());

            if (item.getPlant().getScientificName() != null && !item.getPlant().getScientificName().trim().isEmpty()) {
                tvScientific.setText(item.getPlant().getScientificName());
                tvScientific.setVisibility(View.VISIBLE);
            } else {
                tvScientific.setVisibility(View.GONE);
            }

            if (item.getPlant().getLocation() != null && !item.getPlant().getLocation().trim().isEmpty()) {
                tvLocation.setText("Local: " + item.getPlant().getLocation());
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }

            tvLastCare.setText(item.getLastCareText());
            tvStatusBadge.setText(item.getNextCareText());

            // Estilização do badge de status
            if (item.getStatus() == CareScheduleCalculator.CareStatus.OVERDUE) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_overdue);
                tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_error));
            } else if (item.getStatus() == CareScheduleCalculator.CareStatus.PENDING_TODAY) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending);
                tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending));
            } else {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_up_to_date);
                tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_success));
            }

            // Carregamento de imagem eficiente
            ImageUtils.loadIntoImageView(context, ivThumb, item.getPlant().getPhotoUri());

            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetails(item);
            });

            btnQuickRegister.setOnClickListener(v -> {
                if (listener != null) listener.onQuickRegisterCare(item);
            });
        }
    }
}
