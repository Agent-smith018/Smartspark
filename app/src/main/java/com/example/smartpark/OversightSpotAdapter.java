package com.example.smartpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.ParkingLot;
import java.util.List;

public class OversightSpotAdapter extends RecyclerView.Adapter<OversightSpotAdapter.ViewHolder> {

    public interface OnSpotClickListener {
        void onSpotClick(ParkingLot lot);
    }

    private final List<ParkingLot> lots;
    private final OnSpotClickListener listener;

    public OversightSpotAdapter(List<ParkingLot> lots, OnSpotClickListener listener) {
        this.lots = lots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_oversight_spot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingLot lot = lots.get(position);
        holder.tvLotName.setText(lot.getName() != null ? lot.getName() : "Unnamed Lot");
        holder.tvLotOwner.setText("Sophie M. · " + lot.getTotalSpots() + " spots");

        if (lot.isSuspended()) {
            holder.tvStatus.setText("Suspended(" + lot.getTotalSpots() + ")");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_red);
        } else if ("Partial".equals(lot.getStatus())) {
            holder.tvStatus.setText("Partial");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_orange);
        } else {
            holder.tvStatus.setText("Open");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_green);
        }

        holder.btnAction.setOnClickListener(v -> listener.onSpotClick(lot));
    }

    @Override
    public int getItemCount() { return lots.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLotName, tvLotOwner, tvStatus;
        ImageButton btnAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvLotName = itemView.findViewById(R.id.tvLotName);
            tvLotOwner = itemView.findViewById(R.id.tvLotOwner);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
