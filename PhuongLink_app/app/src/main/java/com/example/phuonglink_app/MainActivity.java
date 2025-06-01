package com.example.phuonglink_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Edge-to-edge support
        EdgeToEdge.enable(this);

        // 2. Đổ layout splash (activity_main.xml), nhớ thẻ gốc có id="@+id/main"
        setContentView(R.layout.activity_main);

        // 3. Áp inset cho toàn màn
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // 4. Lấy ProgressBar và load animation xoay 1 vòng
        ProgressBar spinner = findViewById(R.id.progressBar);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_once);
        spinner.startAnimation(rotate);

        // 5. Nghe kết thúc animation rồi chuyển sang LoginActivity
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();  // tránh về lại splash khi bấm back
            }
        });
    }
}
