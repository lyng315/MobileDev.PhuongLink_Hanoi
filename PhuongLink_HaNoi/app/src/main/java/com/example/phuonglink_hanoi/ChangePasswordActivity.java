package com.example.phuonglink_hanoi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextInputEditText edtOldPassword;
    private TextInputEditText edtNewPassword;
    private TextInputEditText edtConfirmPassword;
    private MaterialButton    btnCancel;
    private MaterialButton    btnSave;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Toolbar với nút back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Ánh xạ view
        edtOldPassword     = findViewById(R.id.edtOldPassword);
        edtNewPassword     = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnCancel          = findViewById(R.id.btnCancel);
        btnSave            = findViewById(R.id.btnSave);

        // Hủy → đóng Activity
        btnCancel.setOnClickListener(v -> finish());

        // Lưu → đổi mật khẩu
        btnSave.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPass = edtOldPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPass)
                || TextUtils.isEmpty(newPass)
                || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirm)) {
            edtConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tái xác thực bằng mật khẩu cũ
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), oldPass);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(
                                    ChangePasswordActivity.this,
                                    "Mật khẩu cũ không đúng",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }
                        // Cập nhật mật khẩu mới
                        user.updatePassword(newPass)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> updateTask) {
                                        if (updateTask.isSuccessful()) {
                                            // 🟢 Ghi thông báo đổi mật khẩu thành công vào SharedPreferences
                                            String userId = Utils.getCurrentUserId(ChangePasswordActivity.this);
                                            SharedPreferences notiPrefs = getSharedPreferences("notifications_" + userId, MODE_PRIVATE);

                                            String existingLogs = notiPrefs.getString("notification_list", "");

                                            // Thêm thông báo mới ở đầu danh sách
                                            String time = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                                    .format(new java.util.Date());
                                            String message = "🔒 Bạn đã đổi mật khẩu thành công ";
                                            String newLog = "msg=" + message + "&&time=" + time;

                                            String updatedLogs = newLog + (existingLogs.isEmpty() ? "" : ";;" + existingLogs);

                                            notiPrefs.edit().putString("notification_list", updatedLogs).apply();
                                            Log.d("NOTI_SAVE", updatedLogs);

                                            Toast.makeText(
                                                    ChangePasswordActivity.this,
                                                    "Đổi mật khẩu thành công",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            finish();
                                        } else {
                                            Toast.makeText(
                                                    ChangePasswordActivity.this,
                                                    "Lỗi đổi mật khẩu: " +
                                                            updateTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    }
                                });
                    }
                });
    }
}
