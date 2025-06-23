package com.example.phuonglink_hanoi.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phuonglink_hanoi.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<String> notificationList;

    public NotificationAdapter(List<String> notificationList) {
        this.notificationList = notificationList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar; // Avatar người dùng
        TextView tvNotification;

        TextView tvTime; // Thời gian

        public ViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvNotification = itemView.findViewById(R.id.tvNotification);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        String fullLog = notificationList.get(position);
        String message = "", time = "";

        if (fullLog.contains("&&")) {
            String[] parts = fullLog.split("&&");
            for (String part : parts) {
                if (part.startsWith("msg=")) {
                    message = part.replace("msg=", "").trim();
                } else if (part.startsWith("time=")) {
                    time = part.replace("time=", "").trim();
                }
            }
        } else {
            // fallback nếu không có định dạng đặc biệt
            message = fullLog;
        }

        holder.tvNotification.setText(message);
        holder.tvTime.setText(time);
        holder.imgAvatar.setImageResource(R.drawable.ic_profile);
    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}
