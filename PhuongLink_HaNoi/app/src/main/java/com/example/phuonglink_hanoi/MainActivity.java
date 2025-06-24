package com.example.phuonglink_hanoi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.phuonglink_hanoi.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG                  = "MainActivity";
    private static final String LEADER_ROLE_ID       = "0sqsVe3Aymc7wBVC9hgU";
    private static final int    PERMISSION_REQUEST_CODE = 1001;

    private ActivityMainBinding binding;
    private FirebaseAuth        auth;
    private FirebaseFirestore   db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Giữ nguyên system window flags
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);

        // 1. Kiểm tra login
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2. Inflate layout và thiết lập Navigation
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment_activity_main
        );
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 3. Ẩn tab "Bài đăng" mặc định
        binding.navView.getMenu()
                .findItem(R.id.navigation_post)
                .setVisible(false);

        // 4. Lấy roleId từ Firestore, nếu là Leader thì show tab Post
        db = FirebaseFirestore.getInstance();
        String uid = currentUser.getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String roleId = doc.getString("roleId");
                    Log.d(TAG, "Fetched roleId = " + roleId);
                    if (LEADER_ROLE_ID.equals(roleId)) {
                        binding.navView.getMenu()
                                .findItem(R.id.navigation_post)
                                .setVisible(true);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Không lấy được roleId của user", e)
                );

        // 5. Yêu cầu quyền thông báo (Android 13+)
        requestNotificationPermission();

        // 6. Thiết lập listener cho bottom navigation bằng if-else
        binding.navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int currentDest = navController.getCurrentDestination() == null
                    ? -1
                    : navController.getCurrentDestination().getId();

            if (itemId == R.id.navigation_home) {
                if (currentDest != R.id.navigation_home) {
                    navController.popBackStack(R.id.navigation_home, false);
                    navController.navigate(R.id.navigation_home);
                }
                return true;
            } else if (itemId == R.id.navigation_post) {
                if (currentDest != R.id.navigation_post) {
                    navController.popBackStack(R.id.navigation_post, false);
                    navController.navigate(R.id.navigation_post);
                }
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                if (currentDest != R.id.navigation_notifications) {
                    navController.popBackStack(R.id.navigation_notifications, false);
                    navController.navigate(R.id.navigation_notifications);
                }
                return true;
            } else if (itemId == R.id.navigation_profile) {
                if (currentDest != R.id.navigation_profile) {
                    navController.popBackStack(R.id.navigation_profile, false);
                    navController.navigate(R.id.navigation_profile);
                }
                return true;
            } else {
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Mỗi khi Activity bắt đầu, nếu đã có user đăng nhập thì lấy FCM token mới nhất
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            Log.d(TAG, "FCM Token: " + token);
                            saveFcmTokenToFirestore(userId, token);
                        } else {
                            Log.e(TAG, "Không lấy được FCM token");
                        }
                    });
        }
    }

    /**
     * Lưu FCM token vào Firestore (trường 'fcmToken' trong document user).
     */
    private void saveFcmTokenToFirestore(String userId, String token) {
        if (db == null) {
            Log.e(TAG, "Firestore instance is null. Không thể lưu FCM token.");
            return;
        }
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);

        db.collection("users").document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "FCM token cập nhật thành công cho user: " + userId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Lỗi cập nhật FCM token cho user: " + userId, e));
    }

    /**
     * Yêu cầu quyền POST_NOTIFICATIONS cho Android 13+.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            } else {
                Log.d(TAG, "Quyền POST_NOTIFICATIONS đã được cấp.");
            }
        } else {
            Log.d(TAG, "Thiết bị dưới Android 13, không cần runtime permission.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Người dùng đã cấp quyền thông báo.");
            } else {
                Log.d(TAG, "Người dùng đã từ chối quyền thông báo.");
            }
        }
    }
}
