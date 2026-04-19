package com.example.smartpark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OwnerRequestAdapter extends RecyclerView.Adapter<OwnerRequestAdapter.ViewHolder> {
    
    private Context context;
    private ArrayList<ParkingRequest> list;
    private FirebaseFirestore db;

    public OwnerRequestAdapter(Context context, ArrayList<ParkingRequest> list) {
        this.context = context;
        this.list = list;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_owner_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingRequest request = list.get(position);

        holder.tvSpotName.setText(request.getSpotName());
        holder.tvTime.setText("Requested at: " + request.getTimeString());

        holder.btnReject.setOnClickListener(v -> {
            db.collection("parking_requests").document(request.getId())
                    .update("status", "rejected")
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Request rejected", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to reject", Toast.LENGTH_SHORT).show());
        });

        holder.btnApprove.setOnClickListener(v -> {
            // Update request
            db.collection("parking_requests").document(request.getId())
                    .update("status", "approved")
                    .addOnSuccessListener(aVoid -> {
                        // Update spot
                        db.collection("parking_spots").document(request.getSpotId())
                                .update("status", "occupied")
                                .addOnSuccessListener(ignored -> {
                                    Toast.makeText(context, "Request approved", Toast.LENGTH_SHORT).show();
                                    
                                    // Log to history
                                    Map<String, Object> history = new HashMap<>();
                                    history.put("spotId", request.getSpotId());
                                    history.put("spotName", request.getSpotName());
                                    history.put("ownerId", request.getOwnerId());
                                    history.put("status", "occupied");
                                    history.put("timestamp", FieldValue.serverTimestamp());
                                    db.collection("spot_history").add(history);
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to approve", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpotName, tvTime;
        Button btnReject, btnApprove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSpotName = itemView.findViewById(R.id.tv_request_spot_name);
            tvTime = itemView.findViewById(R.id.tv_request_time);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnApprove = itemView.findViewById(R.id.btn_approve);
        }
    }
}
