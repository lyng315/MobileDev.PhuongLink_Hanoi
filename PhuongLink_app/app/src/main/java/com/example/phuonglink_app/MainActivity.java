package com.example.phuonglink_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * MainActivity chỉ là Splash Screen (loading), sau 1.5s tự động chuyển sang Login hoặc Home tùy trạng thái đăng nhập.
 */
public class MainActivity extends AppCompatActivity {

    // Thời gian delay cho splash (1.5 giây = 1500ms)
    private static final long SPLASH_DELAY = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chỉ hiển thị layout splash (activity_main.xml)
        setContentView(R.layout.activity_main);

        // Sau SPLASH_DELAY mili-giây, kiểm tra trạng thái đăng nhập và chuyển tiếp
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isLoggedIn = (FirebaseAuth.getInstance().getCurrentUser() != null);

            Intent intent;
            if (isLoggedIn) {
                // Nếu đã đăng nhập, đi thẳng vào HomeActivity
                intent = new Intent(MainActivity.this, HomeActivity.class);
            } else {
                // Nếu chưa, chuyển đến LoginActivity
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish(); // Kết thúc splash, không cho bấm Back quay lại
        }, SPLASH_DELAY);
    }
}
