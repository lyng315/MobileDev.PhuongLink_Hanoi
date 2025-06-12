package com.example.phuonglink_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FilterActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private Button btnThongBaoChung, btnCanhBaoAnNinh, btnCanhBaoDichVu, btnSuKienCongDong;
    private Button btnKhanCap, btnQuanTrong, btnBinhThuong;

    private Button[] categoryButtons;
    private Button[] levelButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        btnThongBaoChung = findViewById(R.id.btnThongBaoChung);
        btnCanhBaoAnNinh = findViewById(R.id.btnCanhBaoAnNinh);
        btnCanhBaoDichVu = findViewById(R.id.btnCanhBaoDichVu);
        btnSuKienCongDong = findViewById(R.id.btnSuKienCongDong);
        btnKhanCap = findViewById(R.id.btnKhanCap);
        btnQuanTrong = findViewById(R.id.btnQuanTrong);
        btnBinhThuong = findViewById(R.id.btnBinhThuong);

        categoryButtons = new Button[]{btnThongBaoChung, btnCanhBaoAnNinh, btnCanhBaoDichVu, btnSuKienCongDong};
        levelButtons = new Button[]{btnKhanCap, btnQuanTrong, btnBinhThuong};

        // Gán sự kiện click cho nhóm danh mục
        for (Button btn : categoryButtons) {
            btn.setOnClickListener(v -> {
                for (Button b : categoryButtons) {
                    b.setSelected(false);
                }
                v.setSelected(true);
            });
        }

        // Gán sự kiện click cho nhóm mức độ
        for (Button btn : levelButtons) {
            btn.setOnClickListener(v -> {
                for (Button b : levelButtons) {
                    b.setSelected(false);
                }
                v.setSelected(true);
            });
        }

        // Nhận dữ liệu nếu có (để giữ lại lựa chọn khi quay lại)
        Intent intent = getIntent();
        String selectedCategory = intent.getStringExtra("category");
        String selectedLevel = intent.getStringExtra("level");
        selectButtonByCategory(selectedCategory);
        selectButtonByLevel(selectedLevel);

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Nút Apply gửi dữ liệu về
        Button btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> {
            String categoryId = getSelectedCategoryId();
            String level = getSelectedLevel();

            if (categoryId.isEmpty() || level.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn danh mục và mức độ", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("category", categoryId);
            resultIntent.putExtra("level", level);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private String getSelectedCategoryId() {
        if (btnThongBaoChung.isSelected()) return "thong-bao-chung";
        if (btnCanhBaoAnNinh.isSelected()) return "canh-bao-an-ninh";
        if (btnCanhBaoDichVu.isSelected()) return "canh-bao-dich-vu";
        if (btnSuKienCongDong.isSelected()) return "su-kien-cong-dong";
        return "";
    }


    private String getSelectedLevel() {
        if (btnKhanCap.isSelected()) return "Khẩn cấp";
        if (btnQuanTrong.isSelected()) return "Quan trọng";
        if (btnBinhThuong.isSelected()) return "Bình thường";
        return "";
    }

    private void selectButtonByCategory(String category) {
        if (category == null) return;
        for (Button btn : categoryButtons) {
            btn.setSelected(false);
        }
        if (category.equals("Thông báo chung")) btnThongBaoChung.setSelected(true);
        else if (category.equals("Cảnh báo an ninh")) btnCanhBaoAnNinh.setSelected(true);
        else if (category.equals("Cảnh báo dịch vụ")) btnCanhBaoDichVu.setSelected(true);
        else if (category.equals("Sự kiện cộng đồng")) btnSuKienCongDong.setSelected(true);
    }

    private void selectButtonByLevel(String level) {
        if (level == null) return;
        for (Button btn : levelButtons) {
            btn.setSelected(false);
        }
        if (level.equals("Khẩn cấp")) btnKhanCap.setSelected(true);
        else if (level.equals("Quan trọng")) btnQuanTrong.setSelected(true);
        else if (level.equals("Bình thường")) btnBinhThuong.setSelected(true);
    }
}
