package com.example.plantgreencare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.plantgreencare.model.CareTaskItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CareTodayAdapter extends RecyclerView.Adapter<CareTodayAdapter.CareViewHolder> {

    public interface OnCareActionListener {
        void onRegisterCare(CareTaskItem taskItem);
    }

    private final Context context;
    private final List<CareTaskItem> items = new ArrayList<>();
    private final OnCareActionListener listener;

    public CareTodayAdapter(Context context, OnCareActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<CareTaskItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_care_today, parent, false);
        return new CareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CareViewHolder holder, int position) {
        CareTaskItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CareViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIcon;
        private final TextView tvPlantName;
        private final TextView tvStatusDesc;
        private final MaterialButton btnComplete;

        public CareViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_care_icon);
            tvPlantName = itemView.findViewById(R.id.tv_care_plant_name);
            tvStatusDesc = itemView.findViewById(R.id.tv_care_status_desc);
            btnComplete = itemView.findViewById(R.id.btn_complete_care);
        }

        public void bind(CareTaskItem item) {
            tvPlantName.setText(item.getPlant().getName());
            tvStatusDesc.setText(item.getStatusText());

            if (item.isOverdue()) {
                tvStatusDesc.setTextColor(ContextCompat.getColor(context, R.color.status_error));
                btnComplete.setText("Registrar agora");
            } else {
                tvStatusDesc.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                btnComplete.setText("Registrar");
            }

            if ("Adubação".equalsIgnoreCase(item.getCareTypeName())) {
                ivIcon.setImageResource(R.drawable.ic_plants);
            } else if ("Poda".equalsIgnoreCase(item.getCareTypeName())) {
                ivIcon.setImageResource(R.drawable.ic_edit);
            } else {
                ivIcon.setImageResource(R.drawable.ic_water_drop);
            }

            btnComplete.setOnClickListener(v -> {
                if (listener != null) listener.onRegisterCare(item);
            });
        }
    }
}
