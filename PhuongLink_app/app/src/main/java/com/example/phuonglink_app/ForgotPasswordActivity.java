package com.example.phuonglink_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Step 1 of “Forgot Password”:
 * – User enters their Email/Phone
 * – We check it against Firestore (or call FirebaseAuth.sendPasswordResetEmail)
 * – On success we launch the verification screen
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText edtUsername;
    private MaterialButton btnLogin;
    private MaterialTextView tvError;

    // if using Firestore to lookup
    private FirebaseFirestore db;
    // if using FirebaseAuth to send reset email
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // --- toolbar back button ---
        toolbar = findViewById(R.id.forgotToolbar);
        setSupportActionBar(toolbar);
        // remove default title if you want custom
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- view bindings ---
        edtUsername = findViewById(R.id.edtUsername);
        btnLogin     = findViewById(R.id.btnLogin);
        // Optional: if you add a tvError in xml
        // tvError   = findViewById(R.id.tvError);

        // init Firebase
        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> onNextClicked());
    }

    private void onNextClicked() {
        String emailOrPhone = edtUsername.getText().toString().trim();
        if (emailOrPhone.isEmpty()) {
            edtUsername.setError("Vui lòng nhập Email/SĐT");
            edtUsername.requestFocus();
            return;
        }

        // Example: if you want to use FirebaseAuth to send a reset link:
        mAuth.sendPasswordResetEmail(emailOrPhone)
                .addOnSuccessListener(aVoid -> {
                    // move to verification code screen
                    Intent i = new Intent(ForgotPasswordActivity.this, ForgotVerifyActivity.class);
                    i.putExtra("email", emailOrPhone);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    Log.e("ForgotPassword", "sendPasswordResetEmail failed", e);
                    edtUsername.setError("Không thể gửi email. Vui lòng thử lại.");
                    edtUsername.requestFocus();
                });

        // If you instead want to query Firestore for existence:
        // db.collection("users")
        //   .whereEqualTo("email", emailOrPhone)
        //   .get()
        //   .addOnSuccessListener(q -> { ... })
        //   .addOnFailureListener(e -> { ... });
    }
}
