// PostFragment.java
package com.example.phuonglink_hanoi.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.phuonglink_hanoi.databinding.FragmentPostBinding;

public class PostFragment extends Fragment {

    private FragmentPostBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout fragment_post.xml thông qua ViewBinding
        binding = FragmentPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ví dụ: lắng nghe sự kiện nhấn nút TẠO
        binding.btnCreatePost.setOnClickListener(v -> {
            String content = binding.edtNewPost.getText().toString().trim();
            if (!content.isEmpty()) {
                // TODO: gọi API hoặc thêm vào RecyclerView adapter
                binding.edtNewPost.setText(""); // xóa nội dung sau khi tạo
            }
        });

        // TODO: khởi tạo RecyclerView
        // binding.rvPosts.setAdapter(yourAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
