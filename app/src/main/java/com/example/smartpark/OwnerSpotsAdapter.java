package com.example.smartpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.ParkingLot;
import java.util.List;

public class OwnerSpotsAdapter extends RecyclerView.Adapter<OwnerSpotsAdapter.ViewHolder> {

    public interface OnLotClickListener {
        void onLotClick(ParkingLot lot);
    }

    private final List<ParkingLot> lots;
    private final OnLotClickListener listener;

    public OwnerSpotsAdapter(List<ParkingLot> lots, OnLotClickListener listener) {
        this.lots = lots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_owner_lot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingLot lot = lots.get(position);
        holder.tvLotName.setText(lot.getName());
        holder.tvLotSubtitle.setText(lot.getTotalSpots() + " spots · " + lot.getAddress());

        // Status badge color
        String status = lot.getStatus();
        if ("Open".equals(status)) {
            holder.tvStatus.setText("Open");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_green);
        } else if ("Closed".equals(status)) {
            holder.tvStatus.setText("Closed");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_grey);
        } else {
            holder.tvStatus.setText("Paid");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_orange);
        }

        // Stats
        int available = lot.getAvailableSpots();
        int occupied = lot.getTotalSpots() - available;
        holder.tvStats.setText(available + " free · " + occupied + " occupied · ★ " +
                String.format("%.1f", lot.getRating()));

        // Progress bar (approximation using layout weight)
        int total = lot.getTotalSpots();
        if (total > 0) {
            float occupiedFraction = (float) occupied / total;
            holder.progressOccupied.setLayoutParams(
                    new android.widget.LinearLayout.LayoutParams(0,
                            (int)(4 * holder.itemView.getResources().getDisplayMetrics().density),
                            occupiedFraction));
            holder.progressFree.setLayoutParams(
                    new android.widget.LinearLayout.LayoutParams(0,
                            (int)(4 * holder.itemView.getResources().getDisplayMetrics().density),
                            1 - occupiedFraction));
        }

        holder.btnEdit.setOnClickListener(v -> listener.onLotClick(lot));
        holder.btnDelete.setOnClickListener(v -> {
            // Delete handled separately
        });
    }

    @Override
    public int getItemCount() { return lots.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLotName, tvLotSubtitle, tvStatus, tvStats;
        View progressOccupied, progressFree;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvLotName = itemView.findViewById(R.id.tvLotName);
            tvLotSubtitle = itemView.findViewById(R.id.tvLotSubtitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStats = itemView.findViewById(R.id.tvStats);
            progressOccupied = itemView.findViewById(R.id.progressOccupied);
            progressFree = itemView.findViewById(R.id.progressFree);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
