package com.example.phuonglink_hanoi.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phuonglink_hanoi.Post; // Import Post class của bạn
import com.example.phuonglink_hanoi.R;

import java.text.SimpleDateFormat;
import java.util.Date; // Import Date
import java.util.List;
import java.util.Locale;

import com.google.firebase.Timestamp; // Import Timestamp

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Post> notificationList; // Thay đổi từ List<String> thành List<Post>

    // Dùng SimpleDateFormat để định dạng ngày giờ cụ thể
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());


    public NotificationAdapter(List<Post> notificationList) {
        this.notificationList = notificationList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvNotificationTitle; // TextView mới cho tiêu đề thông báo
        TextView tvNotificationContent; // TextView cho nội dung thông báo
        TextView tvTime; // TextView cho thời gian

        public ViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle); // Ánh xạ ID mới
            tvNotificationContent = itemView.findViewById(R.id.tvNotificationContent); // Ánh xạ ID mới
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_notification.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        Post post = notificationList.get(position); // Lấy đối tượng Post từ danh sách

        // Hiển thị tiêu đề và nội dung của bài đăng
        holder.tvNotificationTitle.setText(String.format("Bài viết mới: %s", post.getTitle()));
        holder.tvNotificationContent.setText(post.getContent());

        // Định dạng và hiển thị thời gian từ Timestamp
        Timestamp createdAt = post.getCreatedAt();
        if (createdAt != null) {
            holder.tvTime.setText(formatTimeAgo(createdAt)); // Sử dụng phương thức formatTimeAgo mới
        } else {
            holder.tvTime.setText("Thời gian không xác định");
        }


        // Tải ảnh thumbnail của bài viết (nếu có) hoặc hiển thị ảnh mặc định
        String thumbnailUrl = post.getThumbnailUrl();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.loading) // Ảnh placeholder khi đang tải
                    .into(holder.imgAvatar);
        } else {
            // Nếu không có ảnh thumbnail, sử dụng ảnh đại diện mặc định
            holder.imgAvatar.setImageResource(R.drawable.ic_profile);
        }

        // TODO: Thêm xử lý click vào item thông báo (ví dụ: mở chi tiết bài đăng)
        holder.itemView.setOnClickListener(v -> {
            // Ví dụ:
            // Intent intent = new Intent(holder.itemView.getContext(), PostDetailActivity.class);
            // intent.putExtra("postId", post.getId());
            // holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    /**
     * Phương thức tiện ích để định dạng Timestamp thành chuỗi "X phút trước" hoặc "dd/MM/yyyy HH:mm".
     * Được chuyển từ Post.java sang NotificationAdapter.java.
     */
    private String formatTimeAgo(Timestamp timestamp) {
        if (timestamp == null) {
            return "Thời gian không xác định";
        }

        long diff = Timestamp.now().toDate().getTime() - timestamp.toDate().getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days < 30) { // Giới hạn là 30 ngày, sau đó hiển thị ngày cụ thể
            return days + " ngày trước";
        } else {
            // Định dạng ngày giờ nếu đã quá 30 ngày
            return dateFormat.format(timestamp.toDate());
        }
    }
}