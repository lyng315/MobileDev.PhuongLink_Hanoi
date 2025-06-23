package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class GuestActivity extends AppCompatActivity {

    private static final String PREFS_NAME      = "GuestPrefs";
    private static final String KEY_REGION_ID   = "regionId";
    private static final String KEY_REGION_NAME = "regionName";

    private MaterialButton btnSignup, btnLogin;
    private TextView       tvRegion;
    private RecyclerView   rvPosts;

    private final List<String> regionNames = new ArrayList<>();
    private final List<String> regionIds   = new ArrayList<>();
    private final FirebaseFirestore db     = FirebaseFirestore.getInstance();

    private final List<Post> postList = new ArrayList<>();
    private PostAdapter       postAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        // Ánh xạ view
        btnSignup = findViewById(R.id.btnGuestSignup);
        btnLogin  = findViewById(R.id.btnGuestLogin);
        tvRegion  = findViewById(R.id.tvSelectedRegion);
        rvPosts   = findViewById(R.id.rvPosts);

        // Nút chuyển màn Đăng ký / Đăng nhập
        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        // Thiết lập RecyclerView + Adapter + Listener
        // ← isGuest = true để chỉ show dialog ở Guest
        postAdapter = new PostAdapter(this, postList, true);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(postAdapter);

        postAdapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(GuestActivity.this, GuestPostDetailsActivity.class);
            intent.putExtra(GuestPostDetailsActivity.EXTRA_POST_ID, post.getId());
            startActivity(intent);
        });

        // Kiểm tra SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedId   = prefs.getString(KEY_REGION_ID, null);
        String savedName = prefs.getString(KEY_REGION_NAME, null);

        if (savedId != null && savedName != null) {
            tvRegion.setText(savedName);
            loadPostsByRegion(savedId);
        } else {
            fetchRegions();
        }
    }

    private void fetchRegions() {
        db.collection("regions")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    regionNames.clear();
                    regionIds.clear();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) {
                            regionNames.add(name);
                            regionIds.add(doc.getId());
                        }
                    }
                    showRegionDialog();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không thể tải danh sách khu vực.", Toast.LENGTH_LONG).show()
                );
    }

    private void showRegionDialog() {
        final String[] items = regionNames.toArray(new String[0]);
        final int[] chosen = {-1};

        new AlertDialog.Builder(this)
                .setTitle("Chọn khu vực")
                .setSingleChoiceItems(items, -1, (d, which) -> chosen[0] = which)
                .setCancelable(false)
                .setPositiveButton("Xác nhận", (d, which) -> {
                    if (chosen[0] < 0) {
                        Toast.makeText(this, "Vui lòng chọn khu vực.", Toast.LENGTH_SHORT).show();
                        showRegionDialog();
                    } else {
                        String regionName = regionNames.get(chosen[0]);
                        String regionId   = regionIds.get(chosen[0]);
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                .edit()
                                .putString(KEY_REGION_ID, regionId)
                                .putString(KEY_REGION_NAME, regionName)
                                .apply();
                        tvRegion.setText(regionName);
                        loadPostsByRegion(regionId);
                    }
                })
                .show();
    }

    private void loadPostsByRegion(String regionId) {
        db.collection("posts")
                .whereEqualTo("targetRegionId", regionId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    postList.clear();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setId(doc.getId());
                            postList.add(post);
                        }
                    }
                    postAdapter.updateList(postList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không thể tải được bài viết.", Toast.LENGTH_SHORT).show()
                );
    }
}
