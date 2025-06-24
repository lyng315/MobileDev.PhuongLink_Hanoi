package com.example.phuonglink_hanoi.ui.post;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phuonglink_hanoi.Post;
import com.example.phuonglink_hanoi.R;
import com.example.phuonglink_hanoi.ui.post.CreatePostActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách bài viết của chính user đã đăng
 */
public class PostFragment extends Fragment {
    private static final String TAG = "PostFragment";

    private RecyclerView rvPosts;
    private PostActionAdapter adapter;
    private final List<Post> posts = new ArrayList<>();
    private MaterialButton btnCreatePost;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 0. Nút TẠO bài mới
        btnCreatePost = view.findViewById(R.id.btnCreatePost);
        btnCreatePost.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreatePostActivity.class))
        );

        // 1. Thiết lập RecyclerView
        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 2. Khởi tạo adapter (xử lý edit/delete bên trong adapter)
        adapter = new PostActionAdapter(requireContext(), posts);
        rvPosts.setAdapter(adapter);

        // 3. Load danh sách bài của user từ Firestore
        loadMyPosts();
    }

    private void loadMyPosts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "User chưa đăng nhập");
            Toast.makeText(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .whereEqualTo("authorUserId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(this::onPostsLoaded)
                .addOnFailureListener(e ->
                        Log.e(TAG, "Load posts của user lỗi", e)
                );
    }

    private void onPostsLoaded(QuerySnapshot snapshot) {
        posts.clear();
        Log.d(TAG, "Số post load được: " + snapshot.size());
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Post p = doc.toObject(Post.class);
            if (p == null) continue;

            // Gán ID và thời gian
            p.setId(doc.getId());
            p.setTimeAgo(calculateTimeAgo(p.getCreatedAt()));

            posts.add(p);
        }
        adapter.notifyDataSetChanged();
    }

    private String calculateTimeAgo(Timestamp ts) {
        long seconds = (System.currentTimeMillis() - ts.toDate().getTime()) / 1000;
        if (seconds < 60) return seconds + " giây trước";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " phút trước";
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";
        return (hours / 24) + " ngày trước";
    }
}
