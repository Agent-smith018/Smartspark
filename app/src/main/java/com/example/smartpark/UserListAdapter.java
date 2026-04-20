package com.example.smartpark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.AppUser;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(AppUser user);
    }

    private final List<AppUser> users;
    private final OnUserClickListener listener;

    public UserListAdapter(List<AppUser> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppUser user = users.get(position);
        String name = user.getName() != null ? user.getName() : "Unknown";

        // Initials avatar
        String[] parts = name.trim().split(" ");
        String initials = parts.length >= 2
                ? "" + parts[0].charAt(0) + parts[1].charAt(0)
                : name.substring(0, Math.min(2, name.length())).toUpperCase();
        holder.tvAvatar.setText(initials.toUpperCase());

        holder.tvName.setText(name);
        String role = user.getRole() != null ? user.getRole() : "driver";
        holder.tvRole.setText(role.substring(0, 1).toUpperCase() + role.substring(1) + " · Free");

        // Status badge
        if (user.isSuspended()) {
            holder.tvStatus.setText("Suspended");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_red);
        } else if (!user.isApproved() && "owner".equals(user.getRole())) {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_orange);
        } else {
            holder.tvStatus.setText("Available");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_green);
        }

        holder.btnAction.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvRole, tvStatus;
        ImageButton btnAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
