package com.example.phuonglink_app;

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

    // 1. Khai báo biến để ánh xạ các view
    private TextInputEditText edtEmail;
    private MaterialButton btnSendReset;
    private FirebaseAuth auth;  // Đối tượng FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // 2. Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // 3. Ánh xạ MaterialToolbar để hiện nút Back (nếu có)
        MaterialToolbar toolbar = findViewById(R.id.forgotToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Hiển thị mũi tên back trên toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Khi bấm mũi tên back, đóng Activity và quay về màn trước
        toolbar.setNavigationOnClickListener(view -> finish());

        // 4. Ánh xạ EditText và Button theo id trong XML
        edtEmail = findViewById(R.id.edtUsername);     // EditText để nhập email
        btnSendReset = findViewById(R.id.btnLogin);     // Nút “Tiếp” (gọi sendPasswordResetEmail)

        // 5. Bắt sự kiện click cho nút “Tiếp”
        btnSendReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    // Nếu chưa nhập email, báo lỗi
                    edtEmail.setError("Vui lòng nhập Email");
                    return;
                }

                // 6. Gọi FirebaseAuth để gửi email reset password
                auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(aVoid -> {
                            // Thành công: Firebase đã gửi email reset
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Đã gửi liên kết đặt lại mật khẩu vào email của bạn",
                                    Toast.LENGTH_LONG).show();
                            // Đóng Activity này, quay về màn đăng nhập
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Xử lý khi lỗi (ví dụ: email không tồn tại trong FirebaseAuth)
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                // Nếu email chưa được đăng ký
                                edtEmail.setError("Email chưa được đăng ký");
                            } else {
                                // Lỗi khác (kết nối, định dạng email không đúng, v.v.)
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Gửi email thất bại: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
