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

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "extra_post_id";
    private static final String TAG = "PostDetailsActivity";

    private ImageView ivAuthorAvatar, ivPostImage;
    private TextView tvAuthorName, tvPostTime, tvUrgency,
            tvPostTitle, tvPostContent,
            tvLikeCount, tvCommentCount;
    private ImageButton btnLike, btnComment, btnPostComment;
    private EditText edtComment;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private String postId;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration commentsListenerRegistration;
    private DocumentReference likeDocRef;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_details);

        // init
        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivAuthorAvatar    = findViewById(R.id.ivAuthorAvatar);
        tvAuthorName      = findViewById(R.id.tvAuthorName);
        tvPostTime        = findViewById(R.id.tvPostTime);
        tvUrgency         = findViewById(R.id.tvUrgency);
        tvPostTitle       = findViewById(R.id.tvPostTitle);
        tvPostContent     = findViewById(R.id.tvPostContent);
        ivPostImage       = findViewById(R.id.ivPostImage);
        btnLike           = findViewById(R.id.btnLike);
        tvLikeCount       = findViewById(R.id.tvLikeCount);
        btnComment        = findViewById(R.id.btnComment);
        tvCommentCount    = findViewById(R.id.tvCommentCount);
        edtComment        = findViewById(R.id.edtComment);
        btnPostComment    = findViewById(R.id.btnPostComment);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);

        // setup comments RecyclerView
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        commentList    = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        recyclerViewComments.setAdapter(commentAdapter);

        // get postId
        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if(postId==null){
            finish(); return;
        }

        // load post data
        db.collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(this::populatePost)
                .addOnFailureListener(e -> Log.e(TAG,"Error loading post",e));

        // load comments + avatars
        loadComments();

        // post comment button
        btnPostComment.setOnClickListener(v -> postComment());

        // like setup
        likeDocRef = db.collection("userLikes")
                .document(postId + "_" + mAuth.getUid());
        likeDocRef.get()
                .addOnSuccessListener(doc->{
                    isLiked = doc.exists();
                    updateLikeUI();
                });
        btnLike.setOnClickListener(v-> toggleLike());

        // live update likeCount
        db.collection("posts").document(postId)
                .addSnapshotListener((snap,e)->{
                    if(e!=null || snap==null||!snap.exists()) return;
                    Long lc = snap.getLong("likeCount");
                    tvLikeCount.setText(String.valueOf(lc!=null?lc:0));
                });
    }

    private void populatePost(DocumentSnapshot doc){
        if(!doc.exists()) return;

        tvPostTitle.setText(doc.getString("title"));
        tvPostContent.setText(doc.getString("content"));

        Timestamp ts = doc.getTimestamp("createdAt");
        if(ts!=null){
            tvPostTime.setText(DateUtils.getRelativeTimeSpanString(
                    ts.toDate().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS));
        }

        Integer lvl = doc.getLong("urgencyLevel").intValue();
        switch(lvl){
            case 3: tvUrgency.setText("Khẩn cấp");
                tvUrgency.setBackgroundResource(R.drawable.bg_urgency_critical);
                break;
            case 2: tvUrgency.setText("Quan trọng");
                tvUrgency.setBackgroundResource(R.drawable.bg_urgency_important);
                break;
            default:tvUrgency.setText("Bình thường");
                tvUrgency.setBackgroundResource(R.drawable.bg_urgency_normal);
        }
        tvUrgency.setTextColor(Color.WHITE);

        String img = doc.getString("thumbnailUrl");
        if(img!=null && !img.isEmpty()){
            ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(img)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .into(ivPostImage);
        } else ivPostImage.setVisibility(View.GONE);

        String authId = doc.getString("authorUserId");
        if(authId!=null){
            db.collection("users").document(authId)
                    .get()
                    .addOnSuccessListener(userDoc->{
                        String name = userDoc.getString("fullName");
                        tvAuthorName.setText(name!=null?name:"");
                        String avatar = userDoc.getString("avatarUrl");
                        if(avatar!=null && !avatar.isEmpty()){
                            Glide.with(this)
                                    .load(avatar)
                                    .circleCrop()
                                    .into(ivAuthorAvatar);
                        }
                    });
        }
    }

    private void loadComments(){
        commentsListenerRegistration = db.collection("comments")
                .whereEqualTo("postId",postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snaps,e)->{
                    if(e!=null) {
                        Log.e(TAG,"Comments listen failed",e);
                        return;
                    }
                    commentList.clear();
                    for(DocumentSnapshot d:snaps){
                        Comment c = d.toObject(Comment.class);
                        if(c==null) continue;
                        c.setId(d.getId());
                        // fetch avatar
                        db.collection("users").document(c.getAuthorUserId())
                                .get()
                                .addOnSuccessListener(u->{
                                    c.setAuthorAvatarUrl(u.getString("avatarUrl"));
                                    commentAdapter.notifyDataSetChanged();
                                });
                        commentList.add(c);
                    }
                    commentAdapter.notifyDataSetChanged();
                    tvCommentCount.setText(String.valueOf(commentList.size()));
                    if(!commentList.isEmpty()){
                        recyclerViewComments.scrollToPosition(commentList.size()-1);
                    }
                });
    }

    private void postComment(){
        String text = edtComment.getText().toString().trim();
        if(text.isEmpty()){
            Toast.makeText(this,"Nội dung trống",Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        Map<String,Object> data=new HashMap<>();
        data.put("postId", postId);
        data.put("authorUserId", uid);
        data.put("authorName", mAuth.getCurrentUser().getDisplayName());
        data.put("content",text);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("comments").add(data)
                .addOnSuccessListener(doc->{
                    edtComment.setText("");
                    db.collection("posts").document(postId)
                            .update("commentCount",FieldValue.increment(1));
                });
    }

    private void toggleLike(){
        isLiked = !isLiked;
        updateLikeUI();
        DocumentReference postRef = db.collection("posts").document(postId);
        if(isLiked){
            likeDocRef.set(new HashMap<String,Object>(){{
                put("postId",postId);
                put("userId",mAuth.getUid());
                put("createdAt",FieldValue.serverTimestamp());
            }}).addOnSuccessListener(a->postRef.update("likeCount",FieldValue.increment(1)));
        } else {
            likeDocRef.delete().addOnSuccessListener(a->postRef.update("likeCount",FieldValue.increment(-1)));
        }
    }

    // ← Đảm bảo phương thức này tồn tại!
    private void updateLikeUI(){
        btnLike.setImageResource(
                isLiked
                        ? R.drawable.ic_favorite_red
                        : R.drawable.ic_favorite_base
        );
    }

    @Override protected void onDestroy(){
        super.onDestroy();
        if(commentsListenerRegistration!=null) commentsListenerRegistration.remove();
    }
}
