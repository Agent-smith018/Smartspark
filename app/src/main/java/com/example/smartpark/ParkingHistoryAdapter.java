package com.example.smartpark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParkingHistoryAdapter extends RecyclerView.Adapter<ParkingHistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<ParkingHistory> historyList;
    private final OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onNavigateClick(ParkingHistory history);
    }

    public ParkingHistoryAdapter(Context context, List<ParkingHistory> historyList, OnHistoryClickListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ParkingHistory history = historyList.get(position);

        holder.tvName.setText(history.spotName);
        holder.tvDate.setText("Parked: " + history.parkedAtText);

        holder.btnNavigate.setOnClickListener(v -> listener.onNavigateClick(history));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;
        MaterialButton btnNavigate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_history_spot_name);
            tvDate = itemView.findViewById(R.id.tv_history_date);
            btnNavigate = itemView.findViewById(R.id.btn_history_navigate);
        }
    }
}
