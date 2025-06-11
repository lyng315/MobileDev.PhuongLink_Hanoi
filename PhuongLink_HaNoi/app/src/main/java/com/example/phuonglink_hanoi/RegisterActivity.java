package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText edtFullName;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtConfirmPassword;
    private TextInputEditText edtPhone;
    private Spinner           spnRegion;
    private MaterialButton    btnRegister;

    private FirebaseAuth      auth;
    private FirebaseFirestore db;
    private List<String>      regionNames;
    private List<String>      regionIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Toolbar với nút back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // Ánh xạ view
        edtFullName        = findViewById(R.id.edtFullName);
        edtEmail           = findViewById(R.id.edtEmail);
        edtPassword        = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPhone           = findViewById(R.id.edtPhone);
        spnRegion          = findViewById(R.id.spnRegion);
        btnRegister        = findViewById(R.id.btnRegister);

        // Chuẩn bị Spinner region
        regionNames = new ArrayList<>();
        regionIds   = new ArrayList<>();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                regionNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRegion.setAdapter(spinnerAdapter);

        // Load danh sách regions từ Firestore
        db.collection("regions")
                .get()
                .addOnSuccessListener(query -> {
                    for (var doc : query.getDocuments()) {
                        regionNames.add(doc.getString("name"));
                        regionIds.add(doc.getId());
                    }
                    spinnerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải danh sách khu vực", Toast.LENGTH_SHORT).show()
                );

        // Xử lý đăng ký
        btnRegister.setOnClickListener(v -> {
            String fullName    = edtFullName.getText().toString().trim();
            String email       = edtEmail.getText().toString().trim();
            String password    = edtPassword.getText().toString().trim();
            String confirmPass = edtConfirmPassword.getText().toString().trim();
            String phone       = edtPhone.getText().toString().trim();
            int    idxRegion   = spnRegion.getSelectedItemPosition();

            // Validate input
            if (TextUtils.isEmpty(fullName)
                    || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(confirmPass)
                    || TextUtils.isEmpty(phone)
                    || idxRegion < 0) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPass)) {
                edtConfirmPassword.setError("Mật khẩu không khớp");
                return;
            }

            String regionId = regionIds.get(idxRegion);

            // Tạo user trên FirebaseAuth
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(
                                    this,
                                    "Đăng ký thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        // Gửi email xác thực
                        FirebaseUser user = auth.getCurrentUser();
                        user.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> verifTask) {
                                        if (!verifTask.isSuccessful()) {
                                            Toast.makeText(
                                                    RegisterActivity.this,
                                                    "Gửi email xác thực thất bại: " + verifTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                            return;
                                        }

                                        // Lưu profile vào Firestore
                                        Map<String,Object> data = new HashMap<>();
                                        data.put("fullName", fullName);
                                        data.put("email", email);
                                        data.put("phoneNumber", phone);
                                        data.put("regionId", regionId);
                                        data.put("roleId",  "role01");

                                        db.collection("users")
                                                .document(user.getUid())
                                                .set(data)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Sign out để trở về Login
                                                    auth.signOut();
                                                    Toast.makeText(
                                                            RegisterActivity.this,
                                                            "Đăng ký thành công! Vui lòng kiểm tra email để xác thực.",
                                                            Toast.LENGTH_LONG
                                                    ).show();
                                                    startActivity(new Intent(
                                                            RegisterActivity.this,
                                                            LoginActivity.class
                                                    ));
                                                    finish();
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(
                                                                RegisterActivity.this,
                                                                "Lỗi ghi thông tin user: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT
                                                        ).show()
                                                );
                                    }
                                });
                    });
        });
    }
}
