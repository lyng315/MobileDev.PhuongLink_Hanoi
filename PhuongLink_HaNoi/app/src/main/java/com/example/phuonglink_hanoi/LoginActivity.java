package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

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

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Ánh xạ view
        tvError      = findViewById(R.id.tvError);
        edtUsername  = findViewById(R.id.edtUsername);
        edtPassword  = findViewById(R.id.edtPassword);
        btnLogin     = findViewById(R.id.btnLogin);
        tvForgot     = findViewById(R.id.tvForgot);
        tvSignupLink = findViewById(R.id.tvSignupLink);

        tvError.setVisibility(TextView.GONE);

        // Chuyển sang màn Quên mật khẩu
        tvForgot.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        // Chuyển sang màn Đăng ký
        tvSignupLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email    = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                showError("Vui lòng nhập đầy đủ tài khoản và mật khẩu");
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Email đã xác thực → vào Main
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Chưa xác thực → sign out, nhắc xác thực
                            auth.signOut();
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Vui lòng kiểm tra email để xác thực trước khi đăng nhập.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
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

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(TextView.VISIBLE);
    }
}
