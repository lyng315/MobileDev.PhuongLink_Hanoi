package com.example.phuonglink_hanoi.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        TextView tvNotification;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNotification = itemView.findViewById(R.id.tvNotification);
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
        holder.tvNotification.setText(notificationList.get(position));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}
