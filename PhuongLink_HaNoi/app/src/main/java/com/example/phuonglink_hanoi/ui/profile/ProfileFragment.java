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
import com.example.phuonglink_hanoi.EditProfileActivity;
import com.example.phuonglink_hanoi.LoginActivity;
import com.example.phuonglink_hanoi.R;
import com.example.phuonglink_hanoi.databinding.FragmentProfileBinding;
import com.example.phuonglink_hanoi.ChangePasswordActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri avatarUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK
                                && result.getData() != null) {
                            avatarUri = result.getData().getData();
                            uploadAvatar();
                        }
                    }
            );

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

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();

        // Lắng nghe dữ liệu user
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

        // Chọn avatar
        binding.imgAvatar.setOnClickListener(v -> {
            Intent pick = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            pickImageLauncher.launch(pick);
        });

        // Đổi mật khẩu
        binding.itemChangePassword.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class))
        );

        //Chinh sua trang ca nhan
        binding.itemEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        // Đăng xuất
        binding.itemLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void bindUserData(DocumentSnapshot snap) {
        // Đảm bảo các biến là final để dùng trong lambda
        final String fullName = snap.getString("fullName");
        final String email = snap.getString("email");
        final String phoneNumber = snap.getString("phoneNumber");
        final String regionId = snap.getString("regionId");
        final String avatarUrl = snap.getString("avatarUrl");

        binding.tvFullName.setText(fullName != null ? fullName : "");
        binding.tvEmail.setText(email != null ? email : "");
        binding.tvPhone.setText(phoneNumber != null ? phoneNumber : "");

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_profile)
                .into(binding.imgAvatar);

        // Chỉ tải và hiển thị tên vùng, bỏ qua addressDetail
        if (regionId != null && !regionId.isEmpty()) {
            db.collection("regions")
                    .document(regionId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        final String regionName = doc.getString("name");
                        binding.tvAddress.setText(regionName != null ? regionName : "");
                    })
                    .addOnFailureListener(e ->
                            // Nếu không tải được tên vùng, hiển thị chuỗi rỗng
                            binding.tvAddress.setText("")
                    );
        } else {
            // Nếu không có regionId, hiển thị chuỗi rỗng
            binding.tvAddress.setText("");
        }
    }

    private void uploadAvatar() {
        if (avatarUri == null) return;
        final String uid = FirebaseAuth
                .getInstance()
                .getCurrentUser()
                .getUid();

        StorageReference ref = storage
                .getReference()
                .child("avatars/" + uid + ".jpg");

        ref.putFile(avatarUri)
                .continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    // Cập nhật avatarUrl trong Firestore
                    db.collection("users")
                            .document(uid)
                            .update("avatarUrl", downloadUri.toString());
                    // Hiển thị ảnh đại diện mới ngay lập tức
                    Glide.with(this)
                            .load(downloadUri)
                            .placeholder(R.drawable.ic_profile)
                            .into(binding.imgAvatar);
                    Toast.makeText(getContext(), "Tải ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
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