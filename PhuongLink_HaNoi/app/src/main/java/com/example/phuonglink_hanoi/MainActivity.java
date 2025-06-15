package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.phuonglink_hanoi.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        super.onCreate(savedInstanceState);

        // 1. Kiểm tra đã đăng nhập chưa
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2. Inflate layout, thiết lập BottomNavigationView với NavController
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Khai báo navController 1 lần duy nhất
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment_activity_main
        );
        NavigationUI.setupWithNavController(binding.navView, navController);

        binding.navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (navController.getCurrentDestination() == null) {
                return false;
            }

            if (itemId == R.id.navigation_home) {
                if (navController.getCurrentDestination().getId() != R.id.navigation_home) {
                    navController.popBackStack(R.id.navigation_home, false);
                    navController.navigate(R.id.navigation_home);
                }
                return true;
            } else if (itemId == R.id.navigation_post) {
                if (navController.getCurrentDestination().getId() != R.id.navigation_post) {
                    navController.popBackStack(R.id.navigation_post, false);
                    navController.navigate(R.id.navigation_post);
                }
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                if (navController.getCurrentDestination().getId() != R.id.navigation_notifications) {
                    navController.popBackStack(R.id.navigation_notifications, false);
                    navController.navigate(R.id.navigation_notifications);
                }
                return true;
            } else if (itemId == R.id.navigation_profile) {
                if (navController.getCurrentDestination().getId() != R.id.navigation_profile) {
                    navController.popBackStack(R.id.navigation_profile, false);
                    navController.navigate(R.id.navigation_profile);
                }
                return true;
            }

            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }}
