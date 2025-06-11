package com.example.phuonglink_hanoi.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewModel extends ViewModel {
    public static class ProfileData {
        public String fullName;
        public String email;
        public String cccd;
        public String phoneNumber;
        public String addressDetail;
        public String regionId;
        public String avatarUrl;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<ProfileData> profileLiveData = new MutableLiveData<>();

    public ProfileViewModel() {
        loadProfile();
    }

    private void loadProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        db.collection("users")
                .document(uid)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null || !snap.exists()) return;
                    ProfileData d = new ProfileData();
                    d.fullName      = snap.getString("fullName");
                    d.email         = snap.getString("email");
                    d.cccd          = snap.getString("cccd");
                    d.phoneNumber   = snap.getString("phoneNumber");
                    d.addressDetail = snap.getString("addressDetail");
                    d.regionId      = snap.getString("regionId");
                    d.avatarUrl     = snap.getString("avatarUrl");
                    profileLiveData.postValue(d);
                });
    }

    /** LiveData để Fragment quan sát và bind lên UI */
    public LiveData<ProfileData> getProfileLiveData() {
        return profileLiveData;
    }

    /** Đăng xuất */
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}
