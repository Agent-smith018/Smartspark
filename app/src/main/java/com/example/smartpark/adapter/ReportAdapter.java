package com.example.smartpark.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpark.R;
import com.example.smartpark.model.ReportItem;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.Holder> {
    private final List<ReportItem> list;

    public ReportAdapter(List<ReportItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ReportItem item = list.get(position);
        h.tvTitle.setText(item.getTitle());
        h.tvDesc.setText(item.getDescription());
        h.btn1.setText(item.getAction1());
        h.btn2.setText(item.getAction2());
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;
        Button btn1, btn2;
        Holder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            btn1 = itemView.findViewById(R.id.tvAction1);
            btn2 = itemView.findViewById(R.id.tvAction2);
        }
    }
}
