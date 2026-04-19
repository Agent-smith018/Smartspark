package com.example.smartpark;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OwnerHistoryAdapter extends RecyclerView.Adapter<OwnerHistoryAdapter.ViewHolder> {
    
    private Context context;
    private ArrayList<OwnerHistory> list;

    public OwnerHistoryAdapter(Context context, ArrayList<OwnerHistory> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_owner_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OwnerHistory history = list.get(position);
        
        holder.tvSpotName.setText(history.getSpotName());
        holder.tvTime.setText(history.getTimeString());
        
        String status = history.getStatus();
        holder.tvStatus.setText(status);
        
        if ("occupied".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#C5221F"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#137333"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpotName, tvStatus, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSpotName = itemView.findViewById(R.id.tv_history_spot_name);
            tvStatus = itemView.findViewById(R.id.tv_history_status);
            tvTime = itemView.findViewById(R.id.tv_history_time);
        }
    }
}
