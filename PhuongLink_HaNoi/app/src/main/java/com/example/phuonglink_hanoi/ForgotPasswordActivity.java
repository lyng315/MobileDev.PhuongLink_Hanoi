package com.example.phuonglink_hanoi;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private MaterialButton btnSendReset, btnBack;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Toolbar (không cài navigation icon nữa)
        MaterialToolbar toolbar = findViewById(R.id.forgotToolbar);
        // Nếu bạn không muốn dùng ActionBar thì không gọi setSupportActionBar()

        // Ánh xạ các view
        edtEmail     = findViewById(R.id.edtUsername);
        btnSendReset = findViewById(R.id.btnLogin);
        btnBack      = findViewById(R.id.btnBack);

        // Xử lý click cho nút Quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xử lý gửi email reset
        btnSendReset.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                edtEmail.setError("Vui lòng nhập Email");
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this,
                                "Đã gửi liên kết đặt lại mật khẩu vào email của bạn",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            edtEmail.setError("Email chưa được đăng ký");
                        } else {
                            Toast.makeText(this,
                                    "Gửi email thất bại: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
