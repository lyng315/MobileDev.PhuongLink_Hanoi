package com.example.phuonglink_hanoi;

import android.text.format.DateUtils;
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

        // Tên người comment
        holder.tvCommentUser.setText(comment.getAuthorName());

        // Nội dung comment
        holder.tvCommentContent.setText(comment.getContent());

        // Thời gian relative
        Timestamp ts = comment.getCreatedAt();
        if (ts != null) {
            CharSequence relative = DateUtils.getRelativeTimeSpanString(
                    ts.toDate().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            holder.tvCommentTime.setText(relative);
        } else {
            holder.tvCommentTime.setText("");
        }

        // Avatar người comment
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
