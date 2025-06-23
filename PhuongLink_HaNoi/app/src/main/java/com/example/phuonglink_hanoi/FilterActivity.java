package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class FilterActivity extends AppCompatActivity {

    private Button[] categoryButtons;
    private Button[] urgencyButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Toolbar: nút quay lại
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ======= Danh mục =========
        Button btnThongBaoChung = findViewById(R.id.btnGeneralNotice);
        Button btnCanhBaoAnNinh = findViewById(R.id.btnSecurityAlert);
        Button btnCanhBaoDichVu = findViewById(R.id.btnServiceAlert);
        Button btnSuKienCongDong = findViewById(R.id.btnCommunityEvents);

        categoryButtons = new Button[]{
                btnThongBaoChung, btnCanhBaoAnNinh, btnCanhBaoDichVu, btnSuKienCongDong
        };
        setupSelectableButtons(categoryButtons);

        // ======= Mức độ =========
        Button btnKhanCap = findViewById(R.id.btnUrgent);
        Button btnQuanTrong = findViewById(R.id.btnImportant);
        Button btnBinhThuong = findViewById(R.id.btnNormal);

        urgencyButtons = new Button[]{
                btnKhanCap, btnQuanTrong, btnBinhThuong
        };
        setupSelectableButtons(urgencyButtons);

        // ======= Nút Thiết lập lại =========
        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(v -> {
            resetSelection(categoryButtons);
            resetSelection(urgencyButtons);
        });

        // ======= Nút Áp dụng =========
        Button btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> {
            String selectedCategoryName = getSelectedText(categoryButtons);
            String selectedUrgencyName = getSelectedText(urgencyButtons);

            String categoryId = mapCategoryNameToId(selectedCategoryName);
            String urgencyLevel = mapUrgencyNameToLevel(selectedUrgencyName);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("categoryId", categoryId);
            resultIntent.putExtra("urgencyLevel", urgencyLevel);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    // Cho phép chọn duy nhất 1 nút trong 1 nhóm
    private void setupSelectableButtons(Button[] buttons) {
        for (Button button : buttons) {
            button.setOnClickListener(v -> {
                for (Button b : buttons) {
                    b.setSelected(false);
                }
                button.setSelected(true);
            });
        }
    }

    // Reset các nút trong nhóm về trạng thái chưa chọn
    private void resetSelection(Button[] buttons) {
        for (Button button : buttons) {
            button.setSelected(false);
        }
    }

    // Lấy text từ nút đang được chọn
    private String getSelectedText(Button[] buttons) {
        for (Button button : buttons) {
            if (button.isSelected()) {
                return button.getText().toString();
            }
        }
        return null;
    }

    // Chuyển tên danh mục sang categoryId
    private String mapCategoryNameToId(String name) {
        if (name == null) return null;
        switch (name) {
            case "Thông báo chung":
                return "category01";
            case "Cảnh báo an ninh":
                return "category02";
            case "Cảnh báo dịch vụ":
                return "category03";
            case "Sự kiện cộng đồng":
                return "category04";
            default:
                return null;
        }
    }

    // Chuyển tên mức độ sang urgencyLevel
    private String mapUrgencyNameToLevel(String name) {
        if (name == null) return null;
        switch (name) {
            case "Khẩn cấp":
                return "3";
            case "Quan trọng":
                return "2";
            case "Bình thường":
                return "1";
            default:
                return null;
        }
    }
}
