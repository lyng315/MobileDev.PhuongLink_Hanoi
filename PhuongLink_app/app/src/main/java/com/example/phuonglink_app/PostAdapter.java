package com.example.phuonglink_app;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Đảm bảo bạn đã thêm Glide dependency

import java.util.List;

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
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post_preview, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // 1. Severity (mức độ) và màu sắc
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

        // 2. Title
        holder.tvTitle.setText(post.getTitle());

        // 3. Time ago
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

        // 4. Thumbnail (Glide) với placeholder 'loading'
        String thumbUrl = post.getThumbnailUrl();
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            Glide.with(context)
                    .load(thumbUrl)
                    .placeholder(R.drawable.loading)   // dùng loading.png làm placeholder
                    .into(holder.ivThumbnail);
        } else {
            // Nếu không có URL, hiển thị trực tiếp loading.png
            holder.ivThumbnail.setImageResource(R.drawable.loading);
        }

        // 5. Icon Favorite / Comment
        holder.ivFavorite.setOnClickListener(v -> {
            // Thay icon khi đã like (nếu bạn đã thêm ic_favorite_filled)
            // tạm thời dùng lại ic_favorite_border nếu chưa có ic_favorite_filled
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
        });
        holder.ivComment.setOnClickListener(v -> {
            // Mở màn hình comment (nếu có)
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
     * Cập nhật lại danh sách bài đăng, rồi refresh RecyclerView
     */
    public void updateList(List<Post> newList) {
        postList = newList;
        notifyDataSetChanged();
    }
}
