package com.example.phuonglink_hanoi.ui.post;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phuonglink_hanoi.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreatePostActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "CreatePostActivity";

    private FirebaseFirestore db;
    private StorageReference imagesRef;

    private TextView tvUserName;
    private AutoCompleteTextView actSeverity, actPostType;
    private TextInputEditText edtPostTitle, edtPostContent;
    private MaterialButton btnAttachImage, btnCancel, btnSubmit;

    private Uri imageUri;               // nếu user chọn ảnh mới
    private String existingImageUrl;    // giữ URL cũ khi edit

    private final List<String> categoryNames = new ArrayList<>();
    private final List<String> categoryIds   = new ArrayList<>();
    private final Executor bgExecutor = Executors.newSingleThreadExecutor();

    // mode = "create" hoặc "edit"
    private String mode = "create";
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // 1. Init Firestore & Storage
        db = FirebaseFirestore.getInstance();
        imagesRef = FirebaseStorage.getInstance().getReference("post_images");

        // 2. Bind views
        tvUserName     = findViewById(R.id.tvUserName);
        actSeverity    = findViewById(R.id.actSeverity);
        actPostType    = findViewById(R.id.actPostType);
        edtPostTitle   = findViewById(R.id.edtPostTitle);
        edtPostContent = findViewById(R.id.edtPostContent);
        btnAttachImage = findViewById(R.id.btnAttachImage);
        btnCancel      = findViewById(R.id.btnCancel);
        btnSubmit      = findViewById(R.id.btnSubmit);

        // 3. Hiển thị tên user
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    tvUserName.setText(name != null ? name : "");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không lấy được tên người dùng", Toast.LENGTH_SHORT).show()
                );

        // 4. Setup severity spinner
        ArrayAdapter<CharSequence> sevAdapter = ArrayAdapter.createFromResource(
                this, R.array.severity_levels, R.layout.list_item_dropdown);
        actSeverity.setAdapter(sevAdapter);

        // 5. Setup category spinner (AutoCompleteTextView)
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, R.layout.list_item_dropdown, categoryNames);
        actPostType.setAdapter(catAdapter);
        // load categories
        db.collection("postCategories")
                .get()
                .addOnSuccessListener(snapshots -> {
                    categoryNames.clear();
                    categoryIds.clear();
                    for (DocumentSnapshot d : snapshots.getDocuments()) {
                        String name = d.getString("name");
                        if (name != null) {
                            categoryNames.add(name);
                            categoryIds.add(d.getId());
                        }
                    }
                    catAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không tải được danh mục", Toast.LENGTH_SHORT).show()
                );

        // 6. Nhận intent để biết mode
        Intent intent = getIntent();
        mode   = intent.getStringExtra("mode");
        postId = intent.getStringExtra("postId");
        if ("edit".equals(mode) && postId != null) {
            loadPostForEdit(postId);
            btnSubmit.setText("Cập nhật");
        } else {
            mode = "create";
            btnSubmit.setText("Đăng");
        }

        // 7. Chọn ảnh
        btnAttachImage.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(i, PICK_IMAGE_REQUEST);
        });

        // 8. Hủy
        btnCancel.setOnClickListener(v -> finish());

        // 9. Đăng / Cập nhật
        btnSubmit.setOnClickListener(v -> uploadAndSavePost(uid));
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE_REQUEST && res == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Toast.makeText(this, "Đã chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    /** Load post cũ để đổ lên form khi edit */
    private void loadPostForEdit(@NonNull String postId) {
        db.collection("posts").document(postId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Không tìm thấy bài đăng", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    // Đổ data
                    String title        = doc.getString("title");
                    String content      = doc.getString("content");
                    Long urgLevelLong   = doc.getLong("urgencyLevel");
                    String categoryId   = doc.getString("categoryId");
                    existingImageUrl    = doc.getString("thumbnailUrl");

                    if (title != null)    edtPostTitle.setText(title);
                    if (content != null)  edtPostContent.setText(content);
                    if (urgLevelLong != null) {
                        int urg = urgLevelLong.intValue(); // 1-3
                        actSeverity.setText(
                                getResources()
                                        .getStringArray(R.array.severity_levels)[urg - 1],
                                false
                        );
                    }
                    if (categoryId != null) {
                        int idx = categoryIds.indexOf(categoryId);
                        if (idx >= 0) {
                            actPostType.setText(categoryNames.get(idx), false);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải bài: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    /** Upload ảnh (nếu có) rồi gọi savePost */
    private void uploadAndSavePost(String uid) {
        String title   = edtPostTitle.getText().toString().trim();
        String content = edtPostContent.getText().toString().trim();
        String severity= actSeverity.getText().toString();
        String catName = actPostType.getText().toString();

        if (title.isEmpty() || content.isEmpty()
                || severity.isEmpty() || catName.isEmpty()) {
            Toast.makeText(this,
                    "Nhập đủ tiêu đề, nội dung, mức độ và loại tin",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int urgencyLevel = severity.equals("Khẩn cấp")  ? 3
                : severity.equals("Quan trọng") ? 2
                : 1;
        int idx = categoryNames.indexOf(catName);
        if (idx < 0) {
            Toast.makeText(this, "Loại tin không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        String categoryId = categoryIds.get(idx);

        // Truy vấn regionId (tương tự trước)
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String regionId = userDoc.getString("regionId");
                    if (imageUri != null) {
                        // upload ảnh mới
                        String mime = getContentResolver().getType(imageUri);
                        String ext  = MimeTypeMap.getSingleton()
                                .getExtensionFromMimeType(mime);
                        StorageReference fileRef = imagesRef
                                .child(System.currentTimeMillis() + "." + ext);
                        fileRef.putFile(imageUri)
                                .continueWithTask(task -> {
                                    if (!task.isSuccessful()) throw task.getException();
                                    return fileRef.getDownloadUrl();
                                })
                                .addOnSuccessListener(uri ->
                                        savePost(uid, categoryId, title, content,
                                                urgencyLevel, regionId,
                                                uri.toString())
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Lỗi upload ảnh: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show()
                                );
                    } else {
                        // không chọn ảnh mới => dùng existingImageUrl (có thể null)
                        savePost(uid, categoryId, title, content,
                                urgencyLevel, regionId,
                                existingImageUrl != null ? existingImageUrl : "");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Không lấy vùng user: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Tạo mới hoặc cập nhật post tùy mode
     */
    private void savePost(String uid,
                          String categoryId,
                          String title,
                          String content,
                          int urgencyLevel,
                          String regionId,
                          String imageUrl) {
        CollectionReference posts = db.collection("posts");
        Map<String,Object> data = new HashMap<>();
        data.put("authorUserId", uid);
        data.put("categoryId",    categoryId);
        data.put("title",         title);
        data.put("content",       content);
        data.put("urgencyLevel",  urgencyLevel);
        data.put("targetRegionId", regionId);
        data.put("thumbnailUrl",  imageUrl);
        if ("create".equals(mode)) {
            data.put("createdAt", FieldValue.serverTimestamp());
            data.put("editedAt",  FieldValue.serverTimestamp());
            posts.add(data)
                    .addOnSuccessListener(d -> {
                        Toast.makeText(this,
                                "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Lỗi đăng bài: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        } else {
            // chỉ update các field và editedAt
            data.put("editedAt", FieldValue.serverTimestamp());
            posts.document(postId)
                    .update(data)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,
                                "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Lỗi cập nhật: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        }
    }
}
