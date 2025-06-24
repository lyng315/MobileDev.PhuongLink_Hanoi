package com.example.phuonglink_hanoi;

import android.Manifest; // Import Manifest
import android.content.Intent;
import android.content.pm.PackageManager; // Import PackageManager
import android.os.Build; // Import Build
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // Import ActivityCompat
import androidx.core.content.ContextCompat; // Import ContextCompat
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.phuonglink_hanoi.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // ID của role LEADER lưu trong users/{uid}.roleId
    private static final String LEADER_ROLE_ID = "0sqsVe3Aymc7wBVC9hgU";

    // Request code cho quyền thông báo
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private     ActivityMainBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Giữ nguyên hệ thống window flags
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

        // 3. Ẩn trước tab "Bài đăng"
        binding.navView.getMenu()
                .findItem(R.id.navigation_post)
                .setVisible(false);

        // 4. Check roleId trong Firestore
        db = FirebaseFirestore.getInstance(); // Khởi tạo db ở đây
        String uid = currentUser.getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String roleId = doc.getString("roleId");
                    Log.d(TAG, "Fetched roleId = " + roleId);
                    if (LEADER_ROLE_ID.equals(roleId)) {
                        // nếu là Leader, hiện tab Post
                        binding.navView.getMenu()
                                .findItem(R.id.navigation_post)
                                .setVisible(true);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Không lấy được roleId của user", e)
                );

        // --- Bắt đầu thêm logic FCM token và yêu cầu quyền thông báo vào đây ---
        // Yêu cầu quyền thông báo cho Android 13+
        requestNotificationPermission();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM_TOKEN", token);
                    }
                });

        // --- Kết thúc logic FCM token và yêu cầu quyền ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 5. Bắt sự kiện chọn item bằng if-else
        binding.navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int currentDest = navController.getCurrentDestination() == null
                    ? -1
                    : navController.getCurrentDestination().getId();

            // Home
            if (itemId == R.id.navigation_home) {
                if (currentDest != R.id.navigation_home) {
                    navController.popBackStack(R.id.navigation_home, false);
                    navController.navigate(R.id.navigation_home);
                }
                return true;
            }
            // Post
            else if (itemId == R.id.navigation_post) {
                if (currentDest != R.id.navigation_post) {
                    navController.popBackStack(R.id.navigation_post, false);
                    navController.navigate(R.id.navigation_post);
                }
                return true;
            }
            // Notifications
            else if (itemId == R.id.navigation_notifications) {
                if (currentDest != R.id.navigation_notifications) {
                    navController.popBackStack(R.id.navigation_notifications, false);
                    navController.navigate(R.id.navigation_notifications);
                }
                return true;
            }
            // Profile
            else if (itemId == R.id.navigation_profile) {
                if (currentDest != R.id.navigation_profile) {
                    navController.popBackStack(R.id.navigation_profile, false);
                    navController.navigate(R.id.navigation_profile);
                }
                return true;
            }
            // Fallback cho NavigationUI nếu có
            else {
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });

    }

    /**
     * Phương thức để lưu FCM token vào Firestore.
     * Phương thức này sẽ cập nhật trường 'fcmToken' trong tài liệu người dùng.
     * @param userId UID của người dùng hiện tại.
     * @param token FCM Registration Token.
     */
    private void saveFcmTokenToFirestore(String userId, String token) {
        if (db == null) {
            Log.e(TAG, "Firestore instance is null. Cannot save FCM token.");
            return;
        }

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);

        db.collection("users").document(userId)
                .update(tokenData) // Sử dụng update để chỉ cập nhật trường 'fcmToken'
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token updated successfully for user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating FCM token for user: " + userId, e));
    }

    /**
     * Yêu cầu quyền POST_NOTIFICATIONS cho Android 13 (API 33) trở lên.
     * Quyền này cần thiết để hiển thị thông báo đẩy.
     */
    private void requestNotificationPermission() {
        // Chỉ yêu cầu quyền nếu chạy trên Android 13 (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Kiểm tra xem quyền POST_NOTIFICATIONS đã được cấp hay chưa
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Quyền chưa được cấp, yêu cầu quyền từ người dùng
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            } else {
                Log.d(TAG, "Quyền POST_NOTIFICATIONS đã được cấp.");
            }
        } else {
            Log.d(TAG, "Thiết bị dưới Android 13, không cần yêu cầu quyền POST_NOTIFICATIONS runtime.");
        }
    }

    /**
     * Callback khi người dùng phản hồi yêu cầu quyền.
     * @param requestCode Mã yêu cầu quyền.
     * @param permissions Các quyền được yêu cầu.
     * @param grantResults Kết quả cấp quyền (GRANTED hoặc DENIED).
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Người dùng đã cấp quyền thông báo.");
            } else {
                Log.d(TAG, "Người dùng đã từ chối quyền thông báo. Thông báo đẩy có thể không hoạt động.");
                // Tùy chọn: Bạn có thể hiển thị một Toast hoặc Dialog để thông báo cho người dùng
                // Toast.makeText(this, "Bạn sẽ không nhận được thông báo đẩy nếu không cấp quyền.", Toast.LENGTH_LONG).show();
            }
        }
    }
}