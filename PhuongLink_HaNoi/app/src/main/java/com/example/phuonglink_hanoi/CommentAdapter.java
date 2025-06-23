package com.example.phuonglink_hanoi;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Giữ lại ImageView nếu bạn muốn hiển thị avatar

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvCommentUser.setText(comment.getAuthorName()); // <--- CẬP NHẬT ĐỂ DÙNG GETTER MỚI

        holder.tvCommentContent.setText(comment.getContent());

        Timestamp ts = comment.getCreatedAt();
        if (ts != null) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    ts.toDate().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            holder.tvCommentTime.setText(relativeTime);
        } else {
            holder.tvCommentTime.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser;
        TextView tvCommentContent;
        TextView tvCommentTime;
        // ImageView ivCommentUserAvatar; // Bỏ nếu bạn đã xóa ImageView khỏi layout

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUserName);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            // ivCommentUserAvatar = itemView.findViewById(R.id.ivCommentUserAvatar); // Bỏ nếu đã xóa khỏi layout
        }
    }
}