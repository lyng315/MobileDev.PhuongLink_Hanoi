package com.example.phuonglink_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

/**
 * Activity màn Login chính. Khi người dùng bấm "Quên mật khẩu?", sẽ chuyển sang ForgotPasswordActivity.
 * Đăng nhập sử dụng FirebaseAuth thay vì query Firestore.
 */
public class LoginActivity extends AppCompatActivity {
    private TextView            tvError;
    private TextInputEditText   edtUsername;
    private TextInputEditText   edtPassword;
    private MaterialButton      btnLogin;
    private TextView            tvForgot;

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ các View trong activity_login.xml
        tvError     = findViewById(R.id.tvError);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        tvForgot    = findViewById(R.id.tvForgot);

        // Bấm vào "Quên mật khẩu?" -> chạy ForgotPasswordActivity
        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Logic đăng nhập sử dụng FirebaseAuth
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email    = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    showError("Vui lòng nhập đầy đủ tài khoản và mật khẩu");
                    return;
                }

                auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            // Đăng nhập thành công -> chuyển sang HomeActivity
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LoginActivity", "signInWithEmail:failure", e);
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                // Email chưa được đăng ký
                                edtUsername.setError("Email chưa được đăng ký");
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Mật khẩu không đúng
                                edtPassword.setError("Mật khẩu không đúng");
                            } else {
                                showError("Đăng nhập thất bại: " + e.getMessage());
                            }
                        });
            }
        });
    }

    /** Hiển thị thông báo lỗi lên đầu form login */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
