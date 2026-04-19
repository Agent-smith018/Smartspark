package com.example.smartpark;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoriteSpotAdapter extends RecyclerView.Adapter<FavoriteSpotAdapter.FavoriteViewHolder> {

    private final Context context;
    private final List<FavoriteSpot> favoriteList;
    private final OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onRemoveFavorite(FavoriteSpot spot, int position);
        void onNavigateClick(FavoriteSpot spot);
    }

    public FavoriteSpotAdapter(Context context, List<FavoriteSpot> favoriteList, OnFavoriteClickListener listener) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_spot, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteSpot spot = favoriteList.get(position);

        holder.tvName.setText(spot.spotName);
        holder.tvStatus.setText(spot.spotStatus);
        holder.tvDate.setText("Saved: " + spot.savedAtText);

        if ("available".equalsIgnoreCase(spot.spotStatus)) {
            holder.tvStatus.setTextColor(Color.parseColor("#10B981")); // Green
            holder.btnNavigate.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            holder.btnNavigate.setVisibility(View.GONE);
        }

        holder.btnRemove.setOnClickListener(v -> listener.onRemoveFavorite(spot, position));
        holder.btnNavigate.setOnClickListener(v -> listener.onNavigateClick(spot));
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvDate;
        ImageButton btnRemove;
        MaterialButton btnNavigate;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_fav_spot_name);
            tvStatus = itemView.findViewById(R.id.tv_fav_spot_status);
            tvDate = itemView.findViewById(R.id.tv_fav_spot_date);
            btnRemove = itemView.findViewById(R.id.btn_fav_remove);
            btnNavigate = itemView.findViewById(R.id.btn_fav_navigate);
        }
    }
}
