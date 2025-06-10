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

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter postAdapter;

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
