package com.example.phuonglink_hanoi;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "extra_post_id";
    private static final String TAG = "PostDetailsActivity";

    private ImageView ivAuthorAvatar, ivPostImage;
    private TextView tvAuthorName, tvPostTime, tvUrgency, tvPostTitle, tvPostContent, tvLikeCount, tvCommentCount;
    private ImageButton btnLike, btnComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_details);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind views
        ivAuthorAvatar = findViewById(R.id.ivAuthorAvatar);
        tvAuthorName   = findViewById(R.id.tvAuthorName);
        tvPostTime     = findViewById(R.id.tvPostTime);
        tvUrgency      = findViewById(R.id.tvUrgency);
        tvPostTitle    = findViewById(R.id.tvPostTitle);
        tvPostContent  = findViewById(R.id.tvPostContent);
        ivPostImage    = findViewById(R.id.ivPostImage);
        btnLike        = findViewById(R.id.btnLike);
        tvLikeCount    = findViewById(R.id.tvLikeCount);
        btnComment     = findViewById(R.id.btnComment);
        tvCommentCount = findViewById(R.id.tvCommentCount);

        // Get postId
        String postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null) {
            finish();
            return;
        }

        // Load post data
        FirebaseFirestore.getInstance()
                .collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(this::populatePost)
                .addOnFailureListener(e -> Log.e(TAG, "Error loading post", e));
    }

    private void populatePost(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        // Title & content
        tvPostTitle.setText(doc.getString("title"));
        tvPostContent.setText(doc.getString("content"));

        // Time
        Timestamp ts = doc.getTimestamp("createdAt");
        if (ts != null) {
            CharSequence relative = DateUtils.getRelativeTimeSpanString(
                    ts.toDate().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS);
            tvPostTime.setText(relative);
        }

        // Urgency
        Long u = doc.getLong("urgencyLevel");
        int level = (u != null) ? u.intValue() : 1;
        switch (level) {
            case 3:
                tvUrgency.setText("Khẩn cấp");
                tvUrgency.setBackgroundResource(R.drawable.bg_urgency_critical);
                break;
            case 2:
                tvUrgency.setText("Quan trọng");
                tvUrgency.setBackgroundResource(R.drawable.bg_urgency_important);
                break;
            default:
                tvUrgency.setText("Bình thường");
                tvUrgency.setBackgroundResource(R.drawable.bg_urgency_normal);
                break;
        }
        tvUrgency.setTextColor(Color.WHITE);

        // Post image
        String imageUrl = doc.getString("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(ivPostImage);
        }

        // Author: using 'authorUserId' field from posts
        String authorId = doc.getString("authorUserId");
        Log.d(TAG, "AuthorId from post: " + authorId);
        if (authorId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(authorId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        // Update name
                        String name = userDoc.getString("fullName");
                        if (name == null) name = userDoc.getString("name");
                        tvAuthorName.setText(name != null ? name : "");
                        // Update avatar
                        String avatarUrl = userDoc.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(avatarUrl)
                                    .circleCrop()
                                    .into(ivAuthorAvatar);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading author", e));
        }

        // TODO: load like/comment counts and handle button clicks
    }
}
