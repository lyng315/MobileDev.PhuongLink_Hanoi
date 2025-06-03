package com.example.phuonglink_app;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // đảm bảo Glide đã được khai báo trong Gradle
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout item_post_preview.xml đã có sẵn
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post_preview, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        // Giữ post làm "effectively final" để dùng trong lambda
        final Post post = postList.get(position);

        // 1. Hiển thị mức độ (urgencyLevel) và đổi màu theo level
        int level = post.getUrgencyLevel();
        switch (level) {
            case 3:
                holder.tvSeverity.setText("Khẩn cấp");
                holder.tvSeverity.setTextColor(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark));
                break;
            case 2:
                holder.tvSeverity.setText("Quan trọng");
                holder.tvSeverity.setTextColor(
                        ContextCompat.getColor(context, R.color.colorSeverityImportant));
                break;
            case 1:
            default:
                holder.tvSeverity.setText("Bình thường");
                holder.tvSeverity.setTextColor(
                        ContextCompat.getColor(context, R.color.colorSeverityNormal));
                break;
        }

        // 2. Hiển thị title
        holder.tvTitle.setText(post.getTitle());

        // 3. Hiển thị "time ago" (ví dụ: "20 phút trước")
        if (post.getCreatedAt() != null) {
            long millis = post.getCreatedAt().toDate().getTime();
            String timeAgo = DateUtils.getRelativeTimeSpanString(
                    millis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            ).toString();
            holder.tvTime.setText(timeAgo);
        } else {
            holder.tvTime.setText("");
        }

        // 4. Load thumbnail (Glide), nếu không có thumbnailUrl thì hiện ảnh loading mặc định
        String thumbUrl = post.getThumbnailUrl();
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            Glide.with(context)
                    .load(thumbUrl)
                    .placeholder(R.drawable.loading)  // cần có drawable/loading.png
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.loading);
        }

        // 5. Khi click vào item, mở PostDetailActivity và gửi qua các trường hiện có
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", post.getTitle());
            intent.putExtra("urgency", post.getUrgencyLevel());

            // Format createdAt thành chuỗi "dd/MM/yyyy HH:mm" để hiển thị trong Detail
            String formattedTime = "";
            if (post.getCreatedAt() != null) {
                long millis = post.getCreatedAt().toDate().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                formattedTime = sdf.format(millis);
            }
            intent.putExtra("createdAt", formattedTime);

            // Gửi thumbnailUrl (nếu bạn muốn hiển thị ảnh trong Detail)
            intent.putExtra("imageUrl", post.getThumbnailUrl());

            // Vì hiện tại bạn chưa có field content, nên không truyền "content"
            // Nếu sau này thêm field "content" vào Post.java thì mới gọi post.getContent() ở đây

            context.startActivity(intent);
        });

        // 6. Xử lý click trên biểu tượng Favorite / Comment (tùy nhu cầu)
        holder.ivFavorite.setOnClickListener(v -> {
            // Ví dụ: đổi sang icon filled (nếu có)
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_base);
        });
        holder.ivComment.setOnClickListener(v -> {
            // TODO: mở màn hình bình luận nếu đã implement
        });
    }

    @Override
    public int getItemCount() {
        return (postList != null) ? postList.size() : 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeverity, tvTitle, tvTime;
        ImageView ivThumbnail, ivFavorite, ivComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeverity  = itemView.findViewById(R.id.tvSeverity);
            tvTitle     = itemView.findViewById(R.id.tvTitle);
            tvTime      = itemView.findViewById(R.id.tvTime);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivFavorite  = itemView.findViewById(R.id.ivFavorite);
            ivComment   = itemView.findViewById(R.id.ivComment);
        }
    }

    /**
     * Nếu bạn muốn cập nhật danh sách post mới (ví dụ sau khi load thêm từ Firestore),
     * gọi adapter.updateList(mListMới) rồi adapter.notifyDataSetChanged() bên Activity/Fragment.
     */
    public void updateList(List<Post> newList) {
        postList = newList;
        notifyDataSetChanged();
    }
}
