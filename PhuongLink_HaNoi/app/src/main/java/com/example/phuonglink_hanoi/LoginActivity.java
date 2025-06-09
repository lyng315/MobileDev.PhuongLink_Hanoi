package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private TextView tvError;
    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private TextView tvForgot;
    private TextView tvSignupLink;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();

        // Ánh xạ view
        tvError      = findViewById(R.id.tvError);
        edtUsername  = findViewById(R.id.edtUsername);
        edtPassword  = findViewById(R.id.edtPassword);
        btnLogin     = findViewById(R.id.btnLogin);
        tvForgot     = findViewById(R.id.tvForgot);
        tvSignupLink = findViewById(R.id.tvSignupLink);

        // Ẩn khung lỗi ban đầu
        tvError.setVisibility(View.GONE);

        // Chuyển sang màn hình Quên mật khẩu
        tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Chuyển sang màn hình Đăng ký
        tvSignupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                showError("Vui lòng nhập đầy đủ tài khoản và mật khẩu");
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        // Đăng nhập thành công → về MainActivity
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LoginActivity", "signInWithEmail:failure", e);
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            edtUsername.setError("Email chưa được đăng ký");
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            edtPassword.setError("Mật khẩu không đúng");
                        } else {
                            showError("Đăng nhập thất bại: " + e.getMessage());
                        }
                    });
        });
    }

    /**
     * Hiển thị thông báo lỗi lên đầu form
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
