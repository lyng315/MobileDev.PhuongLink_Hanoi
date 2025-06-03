package com.example.phuonglink_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PostDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvUrgency, tvCreatedAt;
    private ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        tvTitle     = findViewById(R.id.tvTitle);
        tvUrgency   = findViewById(R.id.tvUrgency);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        ivImage     = findViewById(R.id.ivImage);

        Intent intent = getIntent();
        tvTitle.setText(intent.getStringExtra("title"));
        tvUrgency.setText("Mức độ: " + intent.getIntExtra("urgency", 0));
        tvCreatedAt.setText("Thời gian: " + intent.getStringExtra("createdAt"));

        String imageUrl = intent.getStringExtra("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.loading)
                    .into(ivImage);
        }
    }
}
