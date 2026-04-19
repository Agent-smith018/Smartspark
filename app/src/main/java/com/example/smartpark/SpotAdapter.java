package com.example.smartpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
//import com.parking.manager.R;
//import com.parking.manager.data.Spot;
import java.text.DecimalFormat;
import java.util.List;
public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.ViewHolder {
    private List<Spot> spots;
    private OnSpotEditListener editListener;

    public interface OnSpotEditListener {
        void onEditSpot(Spot spot);
    }

    public SpotAdapter(List<Spot> spots, OnSpotEditListener listener) {
        this.spots = spots;
        this.editListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Spot spot = spots.get(position);
        holder.tvSpotName.setText(spot.getName());
        holder.tvSpotDesc.setText(spot.getDescription());

        // Add time-price rows
        holder.tableLayout.removeAllViews();
        addTimeRow(holder.tableLayout, spot.getOpenTime(), spot.getHourlyRate());
        addTimeRow(holder.tableLayout, getNextHour(spot.getOpenTime()), spot.getHourlyRate());

        holder.btnEdit.setOnClickListener(v -> editListener.onEditSpot(spot));
    }

    private void addTimeRow(TableLayout table, String time, double price) {
        TableRow row = new TableRow(table.getContext());
        TextView tvTime = new TextView(table.getContext());
        tvTime.setText(time);
        tvTime.setPadding(16, 8, 16, 8);
        TextView tvPrice = new TextView(table.getContext());
        DecimalFormat df = new DecimalFormat("#.0");
        tvPrice.setText(df.format(price));
        tvPrice.setPadding(16, 8, 16, 8);
        row.addView(tvTime);
        row.addView(tvPrice);
        table.addView(row);
    }

    private String getNextHour(String time) {
        try {
            int hour = Integer.parseInt(time.split(":")[0]);
            int nextHour = (hour + 1) % 24;
            return String.format("%02d:00", nextHour);
        } catch (Exception e) {
            return "18:00";
        }
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpotName, tvSpotDesc;
        TableLayout tableLayout;
        Button btnEdit;

        ViewHolder(View itemView) {
            super(itemView);
            tvSpotName = itemView.findViewById(R.id.tvSpotName);
            tvSpotDesc = itemView.findViewById(R.id.tvSpotDesc);
            tableLayout = itemView.findViewById(R.id.tableTimePrice);
            btnEdit = itemView.findViewById(R.id.btnEditSpot);
        }
    }
}
