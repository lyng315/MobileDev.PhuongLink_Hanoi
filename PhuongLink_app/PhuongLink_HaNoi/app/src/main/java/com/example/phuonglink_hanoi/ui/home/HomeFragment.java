package com.example.phuonglink_hanoi.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        postAdapter = new PostAdapter(requireContext(), viewModel.getPosts().getValue());
        binding.rvPosts.setAdapter(postAdapter);

        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.updateList(posts);
        });

        binding.btnFilter.setOnClickListener(v -> {
            viewModel.loadFilteredPosts("Khẩn cấp", 3); // ví dụ lọc mức độ
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    viewModel.loadPosts();
                } else {
                    viewModel.searchPostsByTitle(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        viewModel.loadPosts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
