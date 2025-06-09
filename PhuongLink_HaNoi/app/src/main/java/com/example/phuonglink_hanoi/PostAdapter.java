package com.example.phuonglink_hanoi;

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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList != null ? postList : new ArrayList<>();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_preview, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Urgency level
        switch (post.getUrgencyLevel()) {
            case 3:
                holder.tvSeverity.setText("Khẩn cấp");
                holder.tvSeverity.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                break;
            case 2:
                holder.tvSeverity.setText("Quan trọng");
                holder.tvSeverity.setTextColor(ContextCompat.getColor(context, R.color.colorSeverityImportant));
                break;
            default:
                holder.tvSeverity.setText("Bình thường");
                holder.tvSeverity.setTextColor(ContextCompat.getColor(context, R.color.colorSeverityNormal));
                break;
        }

        holder.tvTitle.setText(post.getTitle());

        // Time ago
        if (post.getCreatedAt() != null) {
            long millis = post.getCreatedAt().toDate().getTime();
            String timeAgo = DateUtils.getRelativeTimeSpanString(
                    millis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
            holder.tvTime.setText(timeAgo);
        }

        // Thumbnail
        Glide.with(context)
                .load(post.getThumbnailUrl())
                .placeholder(R.drawable.loading)
                .into(holder.ivThumbnail);

        // Favorite and comment icons
        holder.ivFavorite.setOnClickListener(v ->
                holder.ivFavorite.setImageResource(R.drawable.ic_favorite_base)
        );

        holder.ivComment.setOnClickListener(v -> {
            // TODO: mở màn hình comment nếu cần
        });
    }

    @Override
    public int getItemCount() {
        return (postList != null) ? postList.size() : 0;
    }

    public void updateList(List<Post> newList) {
        this.postList = newList;
        notifyDataSetChanged();
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
}
