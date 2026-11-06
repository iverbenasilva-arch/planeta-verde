package com.example.plantgreencare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.plantgreencare.data.entities.CareRecordEntity;
import com.example.plantgreencare.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class CareHistoryAdapter extends RecyclerView.Adapter<CareHistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<CareRecordEntity> items = new ArrayList<>();

    public CareHistoryAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<CareRecordEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_care_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        CareRecordEntity record = items.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIcon;
        private final TextView tvTitle;
        private final TextView tvNotes;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_history_icon);
            tvTitle = itemView.findViewById(R.id.tv_history_type_and_date);
            tvNotes = itemView.findViewById(R.id.tv_history_notes);
        }

        public void bind(CareRecordEntity record) {
            String dateFormatted = DateUtils.formatDate(record.getPerformedAt());
            String typeName = getTypeName(record.getCareTypeId());

            tvTitle.setText(dateFormatted + " — " + typeName + " realizada");

            if (record.getNotes() != null && !record.getNotes().trim().isEmpty()) {
                tvNotes.setText(record.getNotes());
                tvNotes.setVisibility(View.VISIBLE);
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            if (record.getCareTypeId() == 2) {
                ivIcon.setImageResource(R.drawable.ic_plants);
            } else if (record.getCareTypeId() == 3) {
                ivIcon.setImageResource(R.drawable.ic_edit);
            } else {
                ivIcon.setImageResource(R.drawable.ic_water_drop);
            }
        }

        private String getTypeName(long careTypeId) {
            if (careTypeId == 1) return "Rega";
            if (careTypeId == 2) return "Adubação";
            if (careTypeId == 3) return "Poda";
            if (careTypeId == 4) return "Exposição solar";
            if (careTypeId == 5) return "Pulverização";
            return "Cuidado";
        }
    }
}
