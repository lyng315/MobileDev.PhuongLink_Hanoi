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
//import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Step 1 of “Forgot Password”:
 * – User enters their Email/Phone
 * – We check it against Firestore (or call FirebaseAuth.sendPasswordResetEmail)
 * – On success we launch the verification screen
 */
public class ForgotPasswordActivity extends AppCompatActivity {
}