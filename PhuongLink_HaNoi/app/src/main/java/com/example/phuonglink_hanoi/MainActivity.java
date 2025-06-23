package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.phuonglink_hanoi.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // ID của role LEADER lưu trong users/{uid}.roleId
    private static final String LEADER_ROLE_ID = "0sqsVe3Aymc7wBVC9hgU";

    private ActivityMainBinding binding;
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
        db = FirebaseFirestore.getInstance();
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
}
