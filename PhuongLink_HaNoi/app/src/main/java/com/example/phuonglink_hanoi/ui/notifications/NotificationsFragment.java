package com.example.phuonglink_hanoi.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.Log;
import com.example.phuonglink_hanoi.R;
import com.example.phuonglink_hanoi.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private List<String> notiList;
    private NotificationAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Điều hướng sang màn hình cài đặt
        binding.ivSettings.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_notifications_to_settings);
        });

        // 👉 Thêm LayoutManager cho RecyclerView
        binding.rvNotifications.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));

        // Khởi tạo list và adapter
        notiList = new ArrayList<>();
        adapter = new NotificationAdapter(notiList);
        binding.rvNotifications.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Đảm bảo tab đang hiển thị là "Thông báo"
        View view = requireActivity().findViewById(R.id.nav_view);
        if (view instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
            ((com.google.android.material.bottomnavigation.BottomNavigationView) view)
                    .setSelectedItemId(R.id.navigation_notifications);
        }

        // Load lại dữ liệu
        SharedPreferences prefs = requireContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);
        String stored = prefs.getString("notification_list", "");
        Log.d("NOTI_READ", stored);

        notiList.clear();
        if (!stored.isEmpty()) {
            String[] split = stored.split(";;");
            notiList.addAll(Arrays.asList(split));
        }

        adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
