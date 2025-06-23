package com.example.phuonglink_hanoi.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phuonglink_hanoi.FilterActivity;
import com.example.phuonglink_hanoi.PostAdapter;
import com.example.phuonglink_hanoi.PostDetailsActivity;
import com.example.phuonglink_hanoi.databinding.FragmentHomeBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private PostAdapter postAdapter;

    private final Handler handler = new Handler();
    private Runnable searchRunnable;

    private String selectedCategoryId = null;
    private String selectedUrgencyLevel = null;

    private final ActivityResultLauncher<Intent> filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedCategoryId = data.getStringExtra("categoryId");
                    selectedUrgencyLevel = data.getStringExtra("urgencyLevel");
                    viewModel.filterPostsByCategoryAndUrgency(selectedCategoryId, selectedUrgencyLevel);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Setup RecyclerView
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        // ← isGuest = false để user đã login không thấy dialog
        postAdapter = new PostAdapter(requireContext(), new ArrayList<>(), false);
        binding.rvPosts.setAdapter(postAdapter);

        // Item click opens details
        postAdapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(requireContext(), PostDetailsActivity.class);
            intent.putExtra(PostDetailsActivity.EXTRA_POST_ID, post.getId());
            startActivity(intent);
        });

        // Observe posts và load initial data
        viewModel.getPosts().observe(getViewLifecycleOwner(), postAdapter::updateList);
        viewModel.loadPostsByUserRegion();

        // Search với debounce
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    String keyword = s.toString().trim();
                    if (!keyword.isEmpty()) {
                        viewModel.searchPostsByTitleInUserRegion(keyword);
                    } else {
                        viewModel.loadPostsByUserRegion();
                    }
                };
                handler.postDelayed(searchRunnable, 300);
            }
        });

        // Nút Filter
        binding.btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FilterActivity.class);
            filterLauncher.launch(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
