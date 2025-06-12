package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {
    // Thời gian hiển thị splash screen (ms)
    private static final long SPLASH_DELAY_MS = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kết nối tới layout splash (FrameLayout bạn đã tạo)
        setContentView(R.layout.activity_loading);

        // Sau SPLASH_DELAY_MS ms, chuyển sang MainActivity và kết thúc LoadingActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DELAY_MS);
    }
}
