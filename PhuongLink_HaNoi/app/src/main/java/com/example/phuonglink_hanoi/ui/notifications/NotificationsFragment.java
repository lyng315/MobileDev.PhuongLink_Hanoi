package com.example.phuonglink_hanoi.ui.notifications;

import android.os.Bundle;
import android.util.Log; // Import Log để debug
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LinearLayoutManager

import com.example.phuonglink_hanoi.Post; // Import Post class
import com.example.phuonglink_hanoi.R; // Import R để truy cập tài nguyên
import com.example.phuonglink_hanoi.databinding.FragmentNotificationsBinding; // Binding class
import com.google.firebase.firestore.FirebaseFirestore; // Import FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration; // Import ListenerRegistration
import com.google.firebase.firestore.Query; // Import Query
import com.google.firebase.firestore.QueryDocumentSnapshot; // Import QueryDocumentSnapshot

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";
    private FragmentNotificationsBinding binding;
    private List<Post> notificationsList; // Thay đổi từ List<String> notiList thành List<Post>
    private NotificationAdapter adapter;
    private FirebaseFirestore db; // Khai báo FirebaseFirestore instance
    private ListenerRegistration postListener; // Listener để hủy khi Fragment bị hủy

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Điều hướng sang màn hình cài đặt khi click vào icon settings
        binding.ivSettings.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_notifications_to_settings);
        });

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Thiết lập LayoutManager cho RecyclerView
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo danh sách và adapter
        notificationsList = new ArrayList<>(); // Sử dụng notificationsList
        adapter = new NotificationAdapter(notificationsList);
        binding.rvNotifications.setAdapter(adapter);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRealtimeListener(); // Bắt đầu lắng nghe dữ liệu từ Firestore khi View đã được tạo
    }

    private void setupRealtimeListener() {
        // Lắng nghe các bài viết mới trong bộ sưu tập 'posts'
        // Sắp xếp theo thời gian tạo (createdAt) giảm dần để hiển thị bài mới nhất trước
        postListener = db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING) // Sắp xếp theo thời gian tạo mới nhất
                .limit(20) // Giới hạn số lượng bài đăng để hiển thị, tránh tải quá nhiều
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Lỗi khi lắng nghe bài đăng:", e);
                        return;
                    }

                    if (snapshots != null) {
                        notificationsList.clear(); // Xóa danh sách cũ để cập nhật dữ liệu mới
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Post post = doc.toObject(Post.class); // Chuyển đổi DocumentSnapshot sang đối tượng Post
                            post.setId(doc.getId()); // Gán ID của tài liệu Firestore vào đối tượng Post
                            notificationsList.add(post); // Thêm bài đăng vào danh sách
                        }
                        adapter.notifyDataSetChanged(); // Yêu cầu Adapter cập nhật RecyclerView
                        Log.d(TAG, "Danh sách bài đăng đã được cập nhật.");
                    } else {
                        Log.d(TAG, "Dữ liệu hiện tại: null (Không có snapshots)");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Quan trọng: Hủy lắng nghe khi Fragment bị hủy để tránh rò rỉ bộ nhớ
        if (postListener != null) {
            postListener.remove();
        }
        binding = null; // Giải phóng binding để tránh rò rỉ bộ nhớ
    }

    // Xóa bỏ hoàn toàn phương thức onResume() cũ nếu nó chỉ chứa logic SharedPreferences
    // onResume() không còn cần thiết cho việc tải dữ liệu thông báo vì đã có realtime listener
    /*
    @Override
    public void onResume() {
        super.onResume();
        // ... (logic SharedPreferences cũ đã được loại bỏ)
    }
    */
}