package com.example.smartpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.Report;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    public interface OnReportActionListener {
        void onAction(Report report);
    }

    private final List<Report> reports;
    private final OnReportActionListener listener;

    public ReportAdapter(List<Report> reports, OnReportActionListener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.tvTitle.setText(report.getTitle() != null ? report.getTitle() : "Report");
        holder.tvDescription.setText(report.getDescription() != null ? report.getDescription() : "");

        if (report.isCritical()) {
            holder.itemView.setBackgroundResource(R.drawable.bg_card_red_tint);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_card_yellow_tint);
        }

        // Primary action button label
        if ("user".equals(report.getType())) {
            holder.btnPrimary.setText("Suspended user");
            holder.btnSecondary.setText("View Profile");
        } else {
            holder.btnPrimary.setText("Warn owner");
            holder.btnSecondary.setText("View Spots");
        }

        holder.btnSecondary.setOnClickListener(v -> listener.onAction(report));
        holder.btnPrimary.setOnClickListener(v -> {
            // Handle suspend/warn action
        });
    }

    @Override
    public int getItemCount() { return reports.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        Button btnPrimary, btnSecondary;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnPrimary = itemView.findViewById(R.id.btnPrimary);
            btnSecondary = itemView.findViewById(R.id.btnSecondary);
        }
    }
}
