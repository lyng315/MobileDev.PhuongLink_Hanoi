package com.example.phuonglink_hanoi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Hiển thị tên người comment
        holder.tvCommentUser.setText(comment.getAuthorName());

        // Hiển thị nội dung comment
        holder.tvCommentContent.setText(comment.getContent());

        // Hiển thị thời gian đăng (tiếng Việt)
        Timestamp ts = comment.getCreatedAt();
        if (ts != null) {
            long now  = System.currentTimeMillis();
            long past = ts.toDate().getTime();
            long diff = now - past;

            String timeText;
            if (diff < 60 * 1000) {
                timeText = "Vừa xong";
            } else if (diff < 60 * 60 * 1000) {
                long mins = diff / (60 * 1000);
                timeText = mins + " phút trước";
            } else if (diff < 24 * 60 * 60 * 1000) {
                long hrs = diff / (60 * 60 * 1000);
                timeText = hrs + " giờ trước";
            } else {
                long days = diff / (24 * 60 * 60 * 1000);
                timeText = days + " ngày trước";
            }
            holder.tvCommentTime.setText(timeText);
        } else {
            holder.tvCommentTime.setText("");
        }

        // Hiển thị avatar
        String avatarUrl = comment.getAuthorAvatarUrl();
        Glide.with(holder.ivCommentAvatar.getContext())
                .load(avatarUrl != null ? avatarUrl : R.drawable.ic_profile)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(holder.ivCommentAvatar);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCommentAvatar;
        TextView tvCommentUser, tvCommentContent, tvCommentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCommentAvatar    = itemView.findViewById(R.id.ivCommentUserAvatar);
            tvCommentUser      = itemView.findViewById(R.id.tvCommentUserName);
            tvCommentContent   = itemView.findViewById(R.id.tvCommentContent);
            tvCommentTime      = itemView.findViewById(R.id.tvCommentTime);
        }
    }
}
