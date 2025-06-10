package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoadingActivity extends AppCompatActivity {
    // Thời gian hiển thị splash screen (ms)
    private static final long SPLASH_DELAY_MS = 300;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kết nối tới layout splash
        setContentView(R.layout.activity_loading);

        // Khởi tạo Firebase instances
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Sau SPLASH_DELAY_MS ms, kiểm tra login và chuyển màn
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                // Đồng bộ document users/{UID}
                syncUserDocument(currentUser);
                // Chuyển tiếp vào MainActivity
                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
            } else {
                // Chưa đăng nhập → chuyển vào LoginActivity
                startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY_MS);
    }

    /**
     * Nếu document users/{UID} chưa tồn tại, tạo mới với trường regionId.
     */
    private void syncUserDocument(FirebaseUser user) {
        String uid = user.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("email",    user.getEmail());
                        data.put("fullName", user.getDisplayName());
                        data.put("regionId","region01");  // ← default region
                        userRef.set(data)
                                .addOnSuccessListener(aVoid ->
                                        Log.d("SyncUserDoc","Created userDoc for UID=" + uid))
                                .addOnFailureListener(e ->
                                        Log.e("SyncUserDoc","Failed to create userDoc", e));
                    } else {
                        Log.d("SyncUserDoc","userDoc already exists for UID=" + uid);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("SyncUserDoc","Error fetching userDoc", e));
    }
}
