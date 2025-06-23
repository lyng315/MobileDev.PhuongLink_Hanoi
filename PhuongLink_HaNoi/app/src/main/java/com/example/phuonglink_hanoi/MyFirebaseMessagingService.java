package com.example.phuonglink_hanoi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // Có hai loại tin nhắn FCM: dữ liệu và thông báo. Các tin nhắn dữ liệu được gửi bởi
        // send() hoặc sendToDevice() từ máy chủ phụ trợ của bạn. Tin nhắn dữ liệu được xử lý bởi
        // onMessageReceived trong ứng dụng của bạn. Các tin nhắn thông báo, được gửi bởi
        // bảng điều khiển Thông báo, được xử lý khác nhau tùy thuộc vào trạng thái ứng dụng.
        // Tin nhắn thông báo khi ứng dụng ở chế độ nền sẽ hiển thị thông báo mặc định.
        // Người dùng có thể nhấn vào thông báo để mở trình khởi chạy ứng dụng. Khi ứng dụng ở
        // chế độ nền trước, onMessageReceived được gọi.
        //
        // Khi nhận được tin nhắn, hãy kiểm tra xem nó có chứa tải trọng dữ liệu không.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Bạn có thể xử lý dữ liệu ở đây, ví dụ: lấy postId và mở bài đăng cụ thể.
            // String postId = remoteMessage.getData().get("postId");
            // Intent intent = new Intent(this, PostDetailActivity.class);
            // intent.putExtra("postId", postId);
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            //         PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            // sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), pendingIntent);

        }

        // Kiểm tra xem tin nhắn có chứa tải trọng thông báo không.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Tạo Intent để mở MainActivity khi người dùng chạm vào thông báo
            Intent intent = new Intent(this, com.example.phuonglink_hanoi.MainActivity.class); // Thay MainActivity bằng Activity bạn muốn mở
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            sendNotification(title, body, pendingIntent);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // Gửi mã thông báo này đến máy chủ của bạn hoặc lưu nó vào Firestore cho người dùng hiện tại.
        // Bạn nên cập nhật tài liệu người dùng trong Firestore với mã thông báo mới này.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Gửi mã thông báo đến máy chủ ứng dụng của bạn để lưu trữ.
        // Trong trường hợp này, chúng ta sẽ lưu nó vào Firestore.
        // Bạn sẽ cần FirebaseFirestore.getInstance() ở đây và cập nhật tài liệu người dùng.
        // Ví dụ:
        /*
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating FCM token", e));
        }
        */
    }

    private void sendNotification(String title, String messageBody, PendingIntent pendingIntent) {
        String channelId = getString(R.string.default_notification_channel_id); // Bạn sẽ định nghĩa cái này
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Đảm bảo bạn có icon này trong drawable
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Từ Android 8.0 (Oreo) trở đi, ứng dụng phải triển khai các kênh thông báo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Post Notifications", // Tên hiển thị cho kênh thông báo
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID của thông báo */, notificationBuilder.build());
    }
}