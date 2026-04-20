package com.example.smartpark;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ParkingSpotAdapter extends RecyclerView.Adapter<ParkingSpotAdapter.ViewHolder> {

    Context context;
    ArrayList<ParkingSpot> list;
    FirebaseFirestore db;

    public ParkingSpotAdapter(Context context, ArrayList<ParkingSpot> list) {
        this.context = context;
        this.list = list;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itemparkingspot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSpot spot = list.get(position);

        holder.tvName.setText(spot.getName());
        holder.tvAddress.setText(spot.getAddress());
        holder.tvPrice.setText("$" + spot.getPrice() + "/hr");

        // Remove listener before setting checked state to avoid unwanted triggers
        holder.switchStatus.setOnCheckedChangeListener(null);
        boolean isAvailable = "available".equalsIgnoreCase(spot.getStatus());
        holder.switchStatus.setChecked(isAvailable);
        holder.switchStatus.setText(isAvailable ? "Available" : "Occupied");

        holder.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "available" : "occupied";
            holder.switchStatus.setText(isChecked ? "Available" : "Occupied");
            
            if (spot.getId() != null && !spot.getId().isEmpty()) {
                db.collection("parking_spots").document(spot.getId())
                        .update("status", newStatus)
                        .addOnSuccessListener(unused -> {
                            spot.setStatus(newStatus);
                            
                            // Log to history
                            java.util.Map<String, Object> history = new java.util.HashMap<>();
                            history.put("spotId", spot.getId());
                            history.put("spotName", spot.getName());
                            history.put("ownerId", com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid());
                            history.put("status", newStatus);
                            history.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                            db.collection("spot_history").add(history);
                            
                            Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Revert on failure
                            holder.switchStatus.setOnCheckedChangeListener(null);
                            holder.switchStatus.setChecked(!isChecked);
                            holder.switchStatus.setText(!isChecked ? "Available" : "Occupied");
                            Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                            // Re-attach
                            // (the view will be re-bound anyway if we call notifyItemChanged but this is fine)
                        });
            }
        });

        // Clicking a spot in the list will now open the Edit screen
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddParkingSpotActivity.class);
                intent.putExtra("spot", spot); // Pass the spot object to the edit screen
                context.startActivity(intent);
            }
        });
        
        // Delete button listener
        holder.ivDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                .setTitle("Delete Parking Spot")
                .setMessage("Are you sure you want to delete this parking spot? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (spot.getId() != null && !spot.getId().isEmpty()) {
                        db.collection("parking_spots").document(spot.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Spot deleted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete spot", Toast.LENGTH_SHORT).show();
                            });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvPrice;
        SwitchMaterial switchStatus;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvPrice = itemView.findViewById(R.id.tv_price);
            switchStatus = itemView.findViewById(R.id.switch_status);
            ivDelete = itemView.findViewById(R.id.iv_delete_spot);
        }
    }
}