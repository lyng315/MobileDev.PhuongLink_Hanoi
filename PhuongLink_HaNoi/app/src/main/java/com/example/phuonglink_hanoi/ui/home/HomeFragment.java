package com.example.phuonglink_hanoi.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phuonglink_hanoi.PostAdapter;
import com.example.phuonglink_hanoi.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter postAdapter;

    private Handler handler = new Handler(); // handler cũng có thể là field
    private Runnable searchRunnable;         // <-- thêm dòng này

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        postAdapter = new PostAdapter(requireContext(), new ArrayList<>());
        binding.rvPosts.setAdapter(postAdapter);

        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.updateList(posts);
        });

        // Gọi method mới: chỉ lấy posts theo vùng user
        viewModel.loadPostsByUserRegion();

        //tìm kiếm theo tiêu đề của khu vực user
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    String keyword = s.toString().trim();
                    if (!keyword.isEmpty()) {
                        viewModel.searchPostsByTitleInUserRegion(keyword);
                    } else {
                        viewModel.loadPostsByUserRegion();
                    }
                };

                handler.postDelayed(searchRunnable, 300); // Delay 300ms sau khi dừng gõ
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
