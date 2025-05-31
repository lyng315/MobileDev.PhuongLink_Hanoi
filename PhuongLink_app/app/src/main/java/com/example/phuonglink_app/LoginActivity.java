package com.example.phuonglink_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnSuccessListener;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtPassword;
    private MaterialButton btnLogin;
    private TextView tvError, tvForgot;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ view
        tvError     = findViewById(R.id.tvError);
        tvForgot    = findViewById(R.id.tvForgot);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin    = findViewById(R.id.btnLogin);

        // Ẩn thông báo lỗi ban đầu
        tvError.setVisibility(View.GONE);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Khi bấm "Quên mật khẩu?" -> mở ForgotPasswordActivity
        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );

        // Bắt sự kiện click Đăng nhập
        btnLogin.setOnClickListener(view -> attemptLogin());
    }

    private void attemptLogin() {
        // Ẩn thông báo lỗi mỗi lần thử
        tvError.setVisibility(View.GONE);

        String email    = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Kiểm tra đầu vào
        if (email.isEmpty()) {
            showError("Vui lòng nhập email");
            edtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        // Truy vấn Firestore
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        if (snapshots.isEmpty()) {
                            showError("Tài khoản không tồn tại");
                        } else {
                            DocumentSnapshot userDoc = snapshots.getDocuments().get(0);
                            String storedHash = userDoc.getString("passwordHash");
                            // So sánh plain-text (nếu dùng hash thì apply thuật toán ở đây)
                            if (password.equals(storedHash)) {
                                // Đăng nhập thành công
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.putExtra("USER_ID", userDoc.getId());
                                startActivity(intent);
                                finish();
                            } else {
                                showError("Mật khẩu không chính xác");
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("LoginActivity", "Lỗi khi truy vấn Firestore", e);
                        showError("Lỗi kết nối, vui lòng thử lại");
                    }
                });
    }

    /** Hiển thị thông báo lỗi lên đầu form */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
