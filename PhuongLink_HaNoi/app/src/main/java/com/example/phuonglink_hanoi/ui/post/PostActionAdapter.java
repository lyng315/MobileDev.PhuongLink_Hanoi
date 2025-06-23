package com.example.phuonglink_hanoi.ui.post;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.phuonglink_hanoi.Post;
import com.example.phuonglink_hanoi.R;
import com.example.phuonglink_hanoi.ui.post.CreatePostActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter riêng cho màn PostFragment, dùng layout item_post_action.xml
 * Xử lý Edit & Delete trực tiếp trong adapter.
 */
public class PostActionAdapter extends RecyclerView.Adapter<PostActionAdapter.ViewHolder> {
    private final Context context;
    private final List<Post> postList;
    private final FirebaseFirestore db;

    public PostActionAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_post_action, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        // MAPPING SEVERITY THEO urgencyLevel
        int level = post.getUrgencyLevel();
        String severityText;
        int colorResId;
        if (level == 2) {
            severityText = "Quan trọng";
            colorResId = android.R.color.holo_orange_dark;
        } else if (level == 3) {
            severityText = "Khẩn cấp";
            colorResId = android.R.color.holo_red_dark;
        } else {
            severityText = "Bình thường";
            colorResId = android.R.color.holo_green_dark;
        }
        holder.tvSeverity.setText(severityText);
        holder.tvSeverity.setTextColor(ContextCompat.getColor(context, colorResId));

        // Tiêu đề và thời gian
        holder.tvTitle.setText(post.getTitle());
        holder.tvTime.setText(post.getTimeAgo());

        // Ảnh thumbnail
        String url = post.getThumbnailUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(context).load(url).into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.loading);
        }

        // Nút SỬA
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, CreatePostActivity.class);
            intent.putExtra("mode", "edit");         // bạn tự đặt key để nhận bên Activity
            intent.putExtra("postId", post.getId()); // truyền ID để tải dữ liệu
            context.startActivity(intent);
        });

        // Nút XÓA
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa bài đăng")
                    .setMessage("Bạn có chắc muốn xóa bài này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        String postId = post.getId();
                        if (postId == null || postId.isEmpty()) {
                            Toast.makeText(context,
                                    "Không tìm thấy ID bài đăng",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        db.collection("posts")
                                .document(postId)
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    postList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context,
                                            "Xóa thành công",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context,
                                            "Lỗi khi xóa: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeverity, tvTitle, tvTime;
        ImageView ivThumbnail;
        MaterialButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeverity  = itemView.findViewById(R.id.tvSeverity);
            tvTitle     = itemView.findViewById(R.id.tvTitle);
            tvTime      = itemView.findViewById(R.id.tvTime);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            btnEdit     = itemView.findViewById(R.id.btnEdit);
            btnDelete   = itemView.findViewById(R.id.btnDelete);
        }
    }
}
