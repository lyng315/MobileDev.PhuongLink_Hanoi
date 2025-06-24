package com.example.phuonglink_hanoi;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    public static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        return prefs.getString("userId", "guest"); // fallback nếu chưa đăng nhập
    }
    public static String getUserAvatarUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        return prefs.getString("avatarUrl", null);  // null nếu chưa có
    }
}
