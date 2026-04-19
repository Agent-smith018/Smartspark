package com.example.smartpark.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpark.R;
import com.example.smartpark.model.UserItem;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Holder> {
    private final List<UserItem> list;

    public UserAdapter(List<UserItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        UserItem item = list.get(position);
        h.tvInitials.setText(item.getInitials());
        h.tvName.setText(item.getName());
        h.tvRole.setText(item.getRole());
        h.tvStatus.setText(item.getStatus());
        if (item.getStatus().toLowerCase().contains("active") || item.getStatus().toLowerCase().contains("available")) {
            h.tvStatus.setBackgroundResource(R.drawable.bg_chip_green);
            h.tvStatus.setTextColor(h.itemView.getResources().getColor(R.color.green_text));
        } else {
            h.tvStatus.setBackgroundResource(R.drawable.bg_chip_orange);
            h.tvStatus.setTextColor(h.itemView.getResources().getColor(R.color.orange_text));
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvRole, tvStatus;
        Holder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
