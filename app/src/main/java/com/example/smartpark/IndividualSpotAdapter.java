package com.example.smartpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.ParkingSpot;
import java.util.List;

public class IndividualSpotAdapter extends RecyclerView.Adapter<IndividualSpotAdapter.ViewHolder> {

    public interface OnSpotClickListener {
        void onSpotClick(ParkingSpot spot);
    }

    private final List<ParkingSpot> spots;
    private final OnSpotClickListener listener;

    public IndividualSpotAdapter(List<ParkingSpot> spots, OnSpotClickListener listener) {
        this.spots = spots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_individual_spot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSpot spot = spots.get(position);

        // Row letter (A1, A2 etc.)
        holder.tvSpotLetter.setText(String.valueOf((char)('A' + position % 26)));
        holder.tvSpotName.setText(spot.getName() != null ? spot.getName() : "Spot A-0" + (position + 1));
        holder.tvSpotType.setText((spot.getType() != null ? spot.getType() : "Standard") +
                " · " + (spot.getPricing() != null ? spot.getPricing() : "Free"));

        String status = spot.getStatus();
        if ("Available".equals(status)) {
            holder.tvSpotStatus.setText("Available");
            holder.tvSpotStatus.setBackgroundResource(R.drawable.bg_badge_green);
        } else if ("Occupied".equals(status)) {
            holder.tvSpotStatus.setText("Occupied");
            holder.tvSpotStatus.setBackgroundResource(R.drawable.bg_badge_red);
        } else if ("Reserved".equals(status)) {
            holder.tvSpotStatus.setText("Reserved");
            holder.tvSpotStatus.setBackgroundResource(R.drawable.bg_badge_orange);
        } else {
            holder.tvSpotStatus.setText(status != null ? status : "Unknown");
            holder.tvSpotStatus.setBackgroundResource(R.drawable.bg_badge_grey);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onSpotClick(spot));
    }

    @Override
    public int getItemCount() { return spots.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpotLetter, tvSpotName, tvSpotType, tvSpotStatus;
        ImageButton btnEdit;

        ViewHolder(View itemView) {
            super(itemView);
            tvSpotLetter = itemView.findViewById(R.id.tvSpotLetter);
            tvSpotName = itemView.findViewById(R.id.tvSpotName);
            tvSpotType = itemView.findViewById(R.id.tvSpotType);
            tvSpotStatus = itemView.findViewById(R.id.tvSpotStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
