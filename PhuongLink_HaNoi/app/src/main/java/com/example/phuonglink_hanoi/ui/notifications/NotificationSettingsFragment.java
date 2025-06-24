package com.example.phuonglink_hanoi.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.phuonglink_hanoi.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class NotificationSettingsFragment extends Fragment {

    private Switch switchEnableNotifications;
    private CheckBox cbEvent, cbGeneral, cbSecurity, cbService;
    private RadioButton rbNormal, rbImportant, rbUrgent;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_settings, container, false);

        // Ánh xạ view
        switchEnableNotifications = view.findViewById(R.id.switchEnableNotifications);
        cbEvent = view.findViewById(R.id.cbEvent);
        cbGeneral = view.findViewById(R.id.cbGeneral);
        cbSecurity = view.findViewById(R.id.cbSecurity);
        cbService = view.findViewById(R.id.cbService);
        rbNormal = view.findViewById(R.id.rbNormal);
        rbImportant = view.findViewById(R.id.rbImportant);
        rbUrgent = view.findViewById(R.id.rbUrgent);

        // Nút quay lại
        ImageView ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        // Khởi tạo SharedPreferences
        prefs = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE);

        // Đọc dữ liệu đã lưu
        loadSettings();

        // Lắng nghe sự kiện thay đổi để lưu lại
        setupListeners();

        return view;
    }

    private void loadSettings() {
        switchEnableNotifications.setChecked(prefs.getBoolean("enable_notifications", true));
        cbEvent.setChecked(prefs.getBoolean("cb_event", true));
        cbGeneral.setChecked(prefs.getBoolean("cb_general", true));
        cbSecurity.setChecked(prefs.getBoolean("cb_security", true));
        cbService.setChecked(prefs.getBoolean("cb_service", true));

        String priority = prefs.getString("priority", "important");
        switch (priority) {
            case "normal":
                rbNormal.setChecked(true);
                break;
            case "important":
                rbImportant.setChecked(true);
                break;
            case "urgent":
                rbUrgent.setChecked(true);
                break;
        }
    }

    private void setupListeners() {
        switchEnableNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        cbEvent.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        cbGeneral.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        cbSecurity.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        cbService.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());

        rbNormal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) saveSettings();
        });
        rbImportant.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) saveSettings();
        });
        rbUrgent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) saveSettings();
        });
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("enable_notifications", switchEnableNotifications.isChecked());
        editor.putBoolean("cb_event", cbEvent.isChecked());
        editor.putBoolean("cb_general", cbGeneral.isChecked());
        editor.putBoolean("cb_security", cbSecurity.isChecked());
        editor.putBoolean("cb_service", cbService.isChecked());

        if (rbNormal.isChecked()) {
            editor.putString("priority", "normal");
        } else if (rbImportant.isChecked()) {
            editor.putString("priority", "important");
        } else if (rbUrgent.isChecked()) {
            editor.putString("priority", "urgent");
        }

        editor.apply(); // lưu không đồng bộ, nhẹ
    }

}
