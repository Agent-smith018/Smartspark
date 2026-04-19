package com.example.smartpark.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpark.R;
import com.example.smartpark.model.ParkingSpot;

import java.util.List;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.Holder> {
    private final List<ParkingSpot> list;
    private final int layoutId;

    public SpotAdapter(List<ParkingSpot> list, int layoutId) {
        this.list = list;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ParkingSpot item = list.get(position);
        h.tvName.setText(item.getName());
        h.tvAddress.setText(item.getAddress());
        if (h.tvMeta != null) {
            h.tvMeta.setText(item.getType() + " • " + item.getPriceType());
        }
        h.tvStatus.setText(item.getStatus());

        String s = item.getStatus().toLowerCase();
        if (s.contains("open") || s.contains("available") || s.contains("active")) {
            h.tvStatus.setBackgroundResource(R.drawable.bg_chip_green);
            h.tvStatus.setTextColor(h.itemView.getResources().getColor(R.color.green_text));
        } else if (s.contains("partial") || s.contains("reserved") || s.contains("pending")) {
            h.tvStatus.setBackgroundResource(R.drawable.bg_chip_orange);
            h.tvStatus.setTextColor(h.itemView.getResources().getColor(R.color.orange_text));
        } else if (s.contains("suspended") || s.contains("closed") || s.contains("occupied")) {
            h.tvStatus.setBackgroundResource(R.drawable.bg_chip_red);
            h.tvStatus.setTextColor(h.itemView.getResources().getColor(R.color.red_text));
        } else {
            h.tvStatus.setBackgroundResource(R.drawable.bg_chip_gray);
            h.tvStatus.setTextColor(h.itemView.getResources().getColor(R.color.gray_text));
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvMeta, tvStatus;
        Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
