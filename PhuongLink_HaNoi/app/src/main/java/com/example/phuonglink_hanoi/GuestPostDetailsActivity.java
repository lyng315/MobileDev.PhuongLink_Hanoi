package com.example.phuonglink_hanoi;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class GuestPostDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";

    private ImageView ivPostImage;
    private TextView  tvPostTitle, tvUrgency, tvPostTime;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_post_details);

        // Ánh xạ view
        toolbar      = findViewById(R.id.toolbar);
        ivPostImage  = findViewById(R.id.ivPostImage);
        tvPostTitle  = findViewById(R.id.tvPostTitle);
        tvUrgency    = findViewById(R.id.tvUrgency);
        tvPostTime   = findViewById(R.id.tvPostTime);

        // Nút quay lại
        toolbar.setNavigationOnClickListener(v -> finish());

        // Lấy ID từ Intent
        String postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null) {
            Toast.makeText(this, "Không tìm thấy bài viết!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPostDetails(postId);
    }

    private void loadPostDetails(String postId) {
        FirebaseFirestore.getInstance()
                .collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Bài viết không tồn tại!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // 1. Title
                    String title = doc.getString("title");
                    tvPostTitle.setText(title != null ? title : "");

                    // 2. Time (relative)
                    Timestamp ts = doc.getTimestamp("createdAt");
                    if (ts != null) {
                        CharSequence rel = DateUtils.getRelativeTimeSpanString(
                                ts.toDate().getTime(),
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS
                        );
                        tvPostTime.setText(rel);
                    }

                    // 3. Priority / Urgency
                    // Thử đọc string "priority" trước
                    String priority = doc.getString("priority");
                    if (priority == null) {
                        // fallback nếu bạn vẫn lưu số
                        Long lvl = doc.getLong("urgencyLevel");
                        int  i    = (lvl != null) ? lvl.intValue() : 1;
                        if (i >= 3)           priority = "urgent";
                        else if (i == 2)      priority = "important";
                        else                  priority = "normal";
                    }

                    switch (priority) {
                        case "urgent":
                            tvUrgency.setText("Khẩn cấp");
                            tvUrgency.setBackgroundResource(R.drawable.bg_urgency_critical);
                            break;
                        case "important":
                            tvUrgency.setText("Quan trọng");
                            tvUrgency.setBackgroundResource(R.drawable.bg_urgency_important);
                            break;
                        default:
                            tvUrgency.setText("Bình thường");
                            tvUrgency.setBackgroundResource(R.drawable.bg_urgency_normal);
                            break;
                    }
                    tvUrgency.setTextColor(Color.WHITE);

                    // 4. Ảnh minh họa
                    String imgUrl = doc.getString("thumbnailUrl");
                    if (imgUrl == null || imgUrl.isEmpty()) {
                        imgUrl = doc.getString("imageUrl");
                    }
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imgUrl)
                                .placeholder(R.drawable.loading)
                                .into(ivPostImage);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi tải bài viết.", Toast.LENGTH_SHORT).show()
                );
    }
}
