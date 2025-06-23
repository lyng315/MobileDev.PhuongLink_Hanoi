package com.example.phuonglink_hanoi;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration; // Đảm bảo import này có sẵn
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "extra_post_id";
    private static final String TAG = "PostDetailsActivity";

    private ImageView ivAuthorAvatar, ivPostImage;
    private TextView tvAuthorName, tvPostTime, tvUrgency, tvPostTitle, tvPostContent, tvLikeCount, tvCommentCount;
    private ImageButton btnLike, btnComment;
    private EditText edtComment;
    private ImageButton btnPostComment;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private String postId;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ListenerRegistration commentsListenerRegistration; // Đã khai báo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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
        edtComment     = findViewById(R.id.edtComment);
        btnPostComment = findViewById(R.id.btnPostComment);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);

        // Setup RecyclerView
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setAdapter(commentAdapter);

        // Get postId
        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null) {
            finish();
            return;
        }

        // Load post data
        db.collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(this::populatePost)
                .addOnFailureListener(e -> Log.e(TAG, "Error loading post", e));

        // Load comments
        loadComments(); // Vẫn gọi ở đây

        // Handle post comment button click
        btnPostComment.setOnClickListener(v -> postComment());
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
            db.collection("users")
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

        // Load like/comment counts
        updateLikeCommentCounts();
    }

    private void updateLikeCommentCounts() {
        db.collection("posts")
                .document(postId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Long likeCount = snapshot.getLong("likeCount");
                        Long commentCount = snapshot.getLong("commentCount");

                        tvLikeCount.setText(String.valueOf(likeCount != null ? likeCount : 0));
                        tvCommentCount.setText(String.valueOf(commentCount != null ? commentCount : 0));
                    }
                });
    }

    private void postComment() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }
        String commentContent = edtComment.getText().toString().trim();
        if (commentContent.isEmpty()) {
            Toast.makeText(this, "Nội dung bình luận không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        // Lấy thông tin người dùng để có tên và avatar cho bình luận
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String authorName = userDoc.getString("fullName");
                        if (authorName == null) authorName = userDoc.getString("name");
                        String authorAvatarUrl = userDoc.getString("avatarUrl");

                        Map<String, Object> comment = new HashMap<>();
                        comment.put("postId", postId);
                        comment.put("authorUserId", userId);
                        comment.put("authorName", authorName != null ? authorName : "Ẩn danh");
                        comment.put("content", commentContent);
                        comment.put("createdAt", FieldValue.serverTimestamp());

                        // 👇 Thêm dòng này: Tạo đối tượng Comment tạm thời
                        Comment tempComment = new Comment();
                        tempComment.setPostId(postId);
                        tempComment.setAuthorUserId(userId);
                        tempComment.setAuthorName(authorName != null ? authorName : "Ẩn danh");
                        tempComment.setContent(commentContent);
                        tempComment.setCreatedAt(null); // createdAt sẽ do Firebase cung cấp sau

                        // Thêm comment tạm thời vào list
                        commentList.add(tempComment);
                        commentAdapter.notifyDataSetChanged();

                        // Cuộn xuống cuối RecyclerView
                        recyclerViewComments.post(() -> {
                            recyclerViewComments.smoothScrollToPosition(commentList.size() - 1);
                        });

                        // Gửi lên Firebase Firestore
                        db.collection("comments")
                                .add(comment)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Bình luận đã được đăng", Toast.LENGTH_SHORT).show();
                                    edtComment.setText(""); // Xóa nội dung EditText

                                    // Cập nhật số lượng bình luận trên bài viết
                                    db.collection("posts").document(postId)
                                            .update("commentCount", FieldValue.increment(1))
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Comment count updated"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Error updating comment count", e));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi khi đăng bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error adding comment", e);

                                    // Nếu lỗi, xóa comment tạm thời khỏi UI
                                    commentList.remove(tempComment);
                                    commentAdapter.notifyDataSetChanged();
                                });
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching user info", e);
                });
    }

    private void loadComments() {
        // Gán ListenerRegistration để có thể hủy đăng ký sau này
        commentsListenerRegistration = db.collection("comments") // <--- THAY ĐỔI Ở ĐÂY
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen for comments failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        commentList.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                commentList.add(comment);
                            }
                        }
                        commentAdapter.notifyDataSetChanged();
                        // Cuộn xuống bình luận cuối cùng nếu có bình luận
                        if (commentList.size() > 0) { // <--- THÊM DÒNG NÀY ĐỂ CUỘN
                            recyclerViewComments.scrollToPosition(commentList.size() - 1);
                        }
                    }
                });
    }

    // GHI ĐÈ PHƯƠNG THỨC onDestroy() ĐỂ HỦY ĐĂNG KÝ LISTENER
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsListenerRegistration != null) {
            commentsListenerRegistration.remove(); // <--- Hủy đăng ký listener
            Log.d(TAG, "Comments listener removed.");
        }
    }
}