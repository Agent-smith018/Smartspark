package com.example.smartpark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParkingSpotListAdapter extends RecyclerView.Adapter<ParkingSpotListAdapter.ViewHolder> {

    private List<ParkingSpotMapInfo> spotList;
    private OnSpotClickListener clickListener;
    private Context context;

    public interface OnSpotClickListener {
        void onSpotClick(ParkingSpotMapInfo spot);
    }

    public ParkingSpotListAdapter(Context context, List<ParkingSpotMapInfo> spotList, OnSpotClickListener clickListener) {
        this.context = context;
        this.spotList = spotList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking_spot_list, parent, false);
        return new ViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSpotMapInfo spot = spotList.get(position);
        holder.bind(spot);
    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }

    public void updateList(List<ParkingSpotMapInfo> newList) {
        this.spotList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSpotName;
        private TextView tvSpotStatus;
        private TextView tvSpotDistance;
        private OnSpotClickListener clickListener;
        private ParkingSpotMapInfo currentSpot;

        public ViewHolder(@NonNull View itemView, OnSpotClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;

            tvSpotName = itemView.findViewById(R.id.tv_spot_list_name);
            tvSpotStatus = itemView.findViewById(R.id.tv_spot_list_status);
            tvSpotDistance = itemView.findViewById(R.id.tv_spot_list_distance);

            itemView.setOnClickListener(v -> {
                if (clickListener != null && currentSpot != null) {
                    clickListener.onSpotClick(currentSpot);
                }
            });
        }

        public void bind(ParkingSpotMapInfo spot) {
            this.currentSpot = spot;
            tvSpotName.setText(spot.name);

            // Status with color coding
            String statusText = "available".equalsIgnoreCase(spot.status) ? "✓ Available" : "✗ Occupied";
            tvSpotStatus.setText(statusText);
            int statusColor = "available".equalsIgnoreCase(spot.status) ? 0xFF4CAF50 : 0xFFE53935;
            tvSpotStatus.setTextColor(statusColor);

            // Distance formatting
            if (spot.distance >= 0) {
                String distanceText = spot.distance < 1000 
                    ? String.format("%.0f m", spot.distance) 
                    : String.format("%.1f km", spot.distance / 1000);
                tvSpotDistance.setText(distanceText);
            } else {
                tvSpotDistance.setText("- km");
            }
        }
    }
}
