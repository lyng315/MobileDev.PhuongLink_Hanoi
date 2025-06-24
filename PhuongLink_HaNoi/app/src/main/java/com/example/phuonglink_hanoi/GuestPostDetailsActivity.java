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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GuestPostDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";

    private MaterialToolbar toolbar;
    private ImageView ivAuthorAvatar, ivPostImage;
    private TextView tvAuthorName, tvPostTime, tvUrgency, tvPostTitle, tvPostContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_post_details);

        // Ánh xạ view
        toolbar         = findViewById(R.id.toolbar);
        ivAuthorAvatar  = findViewById(R.id.ivAuthorAvatar);
        tvAuthorName    = findViewById(R.id.tvAuthorName);
        tvPostTime      = findViewById(R.id.tvPostTime);
        tvUrgency       = findViewById(R.id.tvUrgency);
        tvPostTitle     = findViewById(R.id.tvPostTitle);
        tvPostContent   = findViewById(R.id.tvPostContent);
        ivPostImage     = findViewById(R.id.ivPostImage);

        // Back button
        toolbar.setNavigationOnClickListener(v -> finish());

        String postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null) {
            Toast.makeText(this, "Không tìm thấy bài viết!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPostDetails(postId);
    }

    private void loadPostDetails(String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Bài viết không tồn tại!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // 1. Author
                    String authorId = doc.getString("authorUserId"); // hoặc "authorId" tuỳ trường trong Firestore
                    if (authorId != null) {
                        db.collection("users")
                                .document(authorId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("name");
                                    tvAuthorName.setText(name != null ? name : "—");
                                    String avatarUrl = userDoc.getString("avatarUrl");
                                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                        Glide.with(this)
                                                .load(avatarUrl)
                                                .circleCrop()
                                                .into(ivAuthorAvatar);
                                    }
                                });
                    }

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

                    // 3. Urgency
                    String priority = doc.getString("priority");
                    if (priority == null) {
                        Long lvl = doc.getLong("urgencyLevel");
                        int i = (lvl != null) ? lvl.intValue() : 1;
                        priority = (i >= 3 ? "urgent" : i == 2 ? "important" : "normal");
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
                    }
                    tvUrgency.setTextColor(Color.WHITE);

                    // 4. Title & Content
                    tvPostTitle.setText(doc.getString("title"));
                    String content = doc.getString("content");
                    tvPostContent.setText(content != null ? content : "");

                    // 5. Ảnh minh họa
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
