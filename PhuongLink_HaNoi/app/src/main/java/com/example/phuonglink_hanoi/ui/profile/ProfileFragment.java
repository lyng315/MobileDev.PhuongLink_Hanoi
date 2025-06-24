package com.example.phuonglink_hanoi.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.phuonglink_hanoi.ChangePasswordActivity;
import com.example.phuonglink_hanoi.EditProfileActivity;
import com.example.phuonglink_hanoi.LoginActivity;
import com.example.phuonglink_hanoi.R;
import com.example.phuonglink_hanoi.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private String userId;
    private Uri avatarUri;

    // Launcher để chọn ảnh từ gallery
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            avatarUri = result.getData().getData();
                            uploadAvatar();
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db         = FirebaseFirestore.getInstance();
        auth       = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }
        userId = user.getUid();

        // 1) Lắng nghe dữ liệu user từ Firestore
        db.collection("users")
                .document(userId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(),
                                "Lỗi load dữ liệu: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap != null && snap.exists()) {
                        bindUserData(snap);
                    }
                });

        // 2) Chọn avatar
        binding.imgAvatar.setOnClickListener(v -> {
            Intent pick = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            pickImageLauncher.launch(pick);
        });

        // 3) Chỉnh sửa profile
        binding.itemEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class))
        );

        // 4) Đổi mật khẩu
        binding.itemChangePassword.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class))
        );

        // 5) Đăng xuất
        binding.itemLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void bindUserData(DocumentSnapshot snap) {
        String fullName    = snap.getString("fullName");
        String email       = snap.getString("email");
        String phoneNumber = snap.getString("phoneNumber");
        String regionId    = snap.getString("regionId");
        String avatarUrl   = snap.getString("avatarUrl");

        binding.tvFullName.setText(fullName != null ? fullName : "");
        binding.tvEmail.setText(email != null ? email : "");
        binding.tvPhone.setText(phoneNumber != null ? phoneNumber : "");

        // Load avatar (nếu có URL), còn không dùng placeholder
        Glide.with(this)
                .load(avatarUrl != null ? avatarUrl : R.drawable.ic_profile)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.imgAvatar);

        // Load tên vùng
        if (regionId != null && !regionId.isEmpty()) {
            db.collection("regions")
                    .document(regionId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String regionName = doc.getString("name");
                        binding.tvAddress.setText(regionName != null ? regionName : "");
                    })
                    .addOnFailureListener(e ->
                            binding.tvAddress.setText("")
                    );
        } else {
            binding.tvAddress.setText("");
        }
    }

    private void uploadAvatar() {
        if (avatarUri == null) return;

        // Path hợp lệ: avatars/{uid}/avatar.jpg
        StorageReference avatarRef = storageRef
                .child("avatars")
                .child(userId)
                .child("avatar.jpg");

        avatarRef.putFile(avatarUri)
                .continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return avatarRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();
                    // Cập nhật avatarUrl trong users/{uid}
                    db.collection("users")
                            .document(userId)
                            .update("avatarUrl", url)
                            .addOnSuccessListener(a -> {
                                // Hiển thị ngay avatar mới
                                Glide.with(this)
                                        .load(url)
                                        .placeholder(R.drawable.ic_profile)
                                        .circleCrop()
                                        .into(binding.imgAvatar);
                                Toast.makeText(getContext(),
                                        "Tải avatar thành công!",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            "Không lưu được URL avatar: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Upload avatar thất bại: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
