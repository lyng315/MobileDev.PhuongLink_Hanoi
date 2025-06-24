package com.example.phuonglink_hanoi;

import android.app.AlertDialog;
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

import com.bumptech.glide.Glide;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    private final Context context;
    private List<Post> postList;
    private OnItemClickListener listener;
    private final boolean isGuest;

    public PostAdapter(Context context, List<Post> postList, boolean isGuest) {
        this.context  = context;
        this.postList = postList;
        this.isGuest  = isGuest;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Post> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_post_preview, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // 1. Mức độ khẩn cấp
        switch (post.getUrgencyLevel()) {
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
            default:
                holder.tvSeverity.setText("Bình thường");
                holder.tvSeverity.setTextColor(
                        ContextCompat.getColor(context, R.color.colorSeverityNormal));
                break;
        }

        // 2. Tiêu đề
        holder.tvTitle.setText(post.getTitle());

        // 3. Hiển thị thời gian đăng (tiếng Việt)
        if (post.getCreatedAt() != null) {
            long now    = System.currentTimeMillis();
            long past   = post.getCreatedAt().toDate().getTime();
            long diff   = now - past;
            String timeText;
            if (diff < DateUtils.MINUTE_IN_MILLIS) {
                timeText = "Vừa xong";
            } else if (diff < DateUtils.HOUR_IN_MILLIS) {
                long mins = diff / DateUtils.MINUTE_IN_MILLIS;
                timeText = mins + " phút trước";
            } else if (diff < DateUtils.DAY_IN_MILLIS) {
                long hrs = diff / DateUtils.HOUR_IN_MILLIS;
                timeText = hrs + " giờ trước";
            } else {
                long days = diff / DateUtils.DAY_IN_MILLIS;
                timeText = days + " ngày trước";
            }
            holder.tvTime.setText(timeText);
        } else {
            holder.tvTime.setText("");
        }

        // 4. Ảnh thumbnail
        Glide.with(context)
                .load(post.getThumbnailUrl())
                .placeholder(R.drawable.loading)
                .into(holder.ivThumbnail);

        // 5. Click vào item chính
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(post);
        });

        // 6. Click vào Favorite
        holder.ivFavorite.setOnClickListener(v -> {
            if (isGuest) showLoginDialog();
            else {
                // TODO: xử lý favorite
            }
        });

        // 7. Click vào Comment
        holder.ivComment.setOnClickListener(v -> {
            if (isGuest) showLoginDialog();
            else {
                // TODO: mở màn comment
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage("Bạn cần đăng nhập để thực hiện thao tác này.")
                .setPositiveButton("Đăng nhập", (d, w) ->
                        context.startActivity(new Intent(context, LoginActivity.class))
                )
                .setNegativeButton("Hủy", null)
                .show();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView  tvSeverity, tvTitle, tvTime;
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
