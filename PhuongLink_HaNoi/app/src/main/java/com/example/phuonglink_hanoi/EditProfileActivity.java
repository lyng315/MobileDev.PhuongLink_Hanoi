package com.example.phuonglink_hanoi;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private EditText edtFullName, edtEmail, edtPhone;
    private Spinner spinnerRegion;
    private Button btnSave, btnCancel;

    private FirebaseFirestore db;
    private FirebaseUser user;

    private List<String> regionIds = new ArrayList<>();
    private List<String> regionNames = new ArrayList<>();
    private String selectedRegionId = null;

    // Biến tạm để lưu chỉ mục của vùng được chọn từ dữ liệu người dùng
    private int initialRegionSelection = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        spinnerRegion = findViewById(R.id.spinnerRegion);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditProfile);
        toolbar.setNavigationOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Tải dữ liệu người dùng trước để có selectedRegionId
            loadUserData();
            // Sau khi có selectedRegionId, tải các vùng
            // loadRegions() sẽ sử dụng initialRegionSelection sau khi adapter được thiết lập
            btnSave.setOnClickListener(v -> saveUserData());
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserData() {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        edtFullName.setText(documentSnapshot.getString("fullName"));
                        edtEmail.setText(documentSnapshot.getString("email"));
                        edtPhone.setText(documentSnapshot.getString("phoneNumber"));

                        selectedRegionId = documentSnapshot.getString("regionId");

                        // Sau khi tải dữ liệu người dùng, tải các vùng
                        loadRegions();
                    } else {
                        Toast.makeText(this, "Không tìm thấy dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                        loadRegions(); // Vẫn tải vùng ngay cả khi không có dữ liệu người dùng
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không tải được dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadRegions(); // Vẫn tải vùng nếu có lỗi tải dữ liệu người dùng
                });
    }

    private void loadRegions() {
        db.collection("regions").get()
                .addOnSuccessListener(querySnapshot -> {
                    regionIds.clear();
                    regionNames.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        regionIds.add(doc.getId());
                        regionNames.add(doc.getString("name"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            regionNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRegion.setAdapter(adapter);

                    // Thiết lập lựa chọn Spinner dựa trên selectedRegionId sau khi adapter đã được gán
                    if (selectedRegionId != null) {
                        int index = regionIds.indexOf(selectedRegionId);
                        if (index >= 0) {
                            spinnerRegion.setSelection(index);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không tải được danh sách khu vực: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void saveUserData() {
        int selectedIndex = spinnerRegion.getSelectedItemPosition();
        if (selectedIndex >= 0 && selectedIndex < regionIds.size()) { // Kiểm tra chỉ mục hợp lệ
            selectedRegionId = regionIds.get(selectedIndex);
        } else {
            selectedRegionId = null; // Hoặc một giá trị mặc định nếu không có lựa chọn hợp lệ
        }

        Map<String, Object> updatedInfo = new HashMap<>();
        updatedInfo.put("fullName", edtFullName.getText().toString());
        updatedInfo.put("email", edtEmail.getText().toString());
        updatedInfo.put("phoneNumber", edtPhone.getText().toString());
        updatedInfo.put("regionId", selectedRegionId); // Lưu regionId được chọn

        db.collection("users").document(user.getUid()).update(updatedInfo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}