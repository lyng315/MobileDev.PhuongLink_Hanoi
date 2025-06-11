package com.example.phuonglink_hanoi.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
    private FirebaseFirestore      db;
    private FirebaseStorage        storage;
    private Uri                    avatarUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK &&
                                result.getData() != null) {
                            avatarUri = result.getData().getData();
                            uploadAvatar();
                        }
                    }
            );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(),
                    "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
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
        binding.itemChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
        });

        // Đăng xuất
        binding.itemLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void bindUserData(DocumentSnapshot snap) {
        Log.d("DEBUG_PROFILE", "bindUserData()");
        String fullName      = snap.getString("fullName");
        String email         = snap.getString("email");
        String phoneNumber   = snap.getString("phoneNumber");
        String addressDetail = snap.getString("addressDetail");
        String regionId      = snap.getString("regionId");
        String avatarUrl     = snap.getString("avatarUrl");

        binding.tvFullName.setText(fullName != null ? fullName : "");
        binding.tvEmail   .setText(email    != null ? email    : "");
        binding.tvPhone   .setText(phoneNumber != null ? phoneNumber : "");

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_profile)
                .into(binding.imgAvatar);

        // Load tên vùng và ghép địa chỉ chi tiết
        if (regionId != null && !regionId.isEmpty()) {
            db.collection("regions")
                    .document(regionId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String regionName = doc.getString("name");
                        String addr = "";
                        if (regionName != null) addr += regionName;
                        if (addressDetail != null && !addressDetail.isEmpty()) {
                            if (!addr.isEmpty()) addr += ", ";
                            addr += addressDetail;
                        }
                        binding.tvAddress.setText(addr);
                    })
                    .addOnFailureListener(e ->
                            binding.tvAddress.setText(
                                    addressDetail != null ? addressDetail : ""
                            )
                    );
        } else {
            binding.tvAddress.setText(
                    addressDetail != null ? addressDetail : ""
            );
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
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .update("avatarUrl", downloadUri.toString());
                    Glide.with(this)
                            .load(downloadUri)
                            .placeholder(R.drawable.ic_profile)
                            .into(binding.imgAvatar);
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
