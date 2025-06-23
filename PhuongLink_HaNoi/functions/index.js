exports.sendPostNotification = onDocumentCreated(
  { region: "asia-east1" },
  "posts/{postId}",
  async (event) => {
    console.log("Full event received:", JSON.stringify(event, null, 2));
    const snapshot = event.data;
    if (!snapshot) {
      console.log("No data found in event.data. Event might be malformed or trigger incorrectly.");
      return;
    }

    const newPost = snapshot.data();
    const postId = event.params.postId;
    
    // Kiểm tra dữ liệu trước khi sử dụng
    const postTitle = newPost.title || "No Title";  // Nếu title không có, sử dụng giá trị mặc định
    const postContent = newPost.content || "No Content Available";  // Nếu content không có, sử dụng giá trị mặc định

    const notificationBody = postContent.length > 100 ? postContent.substring(0, 97) + "..." : postContent;

    console.log(`Bài viết mới được tạo: ${postTitle} (ID: ${postId})`);

    // Cấu hình gửi thông báo FCM (nếu cần)
    const payload = {
      notification: {
        title: `Bài viết mới: ${postTitle}`,
        body: notificationBody,
        icon: "ic_notification",  // Đảm bảo bạn có icon này trong drawable
        sound: "default"
      },
      data: {
        postId: postId,
        type: "new_post_alert"
      }
    };

    // Gửi notification đến các token (nếu có token)
    try {
      const usersSnapshot = await admin.firestore().collection("users").get();
      const tokens = [];
      usersSnapshot.forEach(doc => {
        const userData = doc.data();
        if (userData.fcmToken) {
          tokens.push(userData.fcmToken);
        }
      });

      if (tokens.length === 0) {
        console.log("Không có FCM token nào được tìm thấy để gửi thông báo.");
        return;
      }

      const response = await admin.messaging().sendToDevice(tokens, payload);
      console.log("Thông báo đã được gửi thành công:", response);

      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error("Lỗi khi gửi thông báo tới token:", tokens[index], error);
        }
      });

    } catch (error) {
      console.error("Lỗi khi gửi thông báo bài đăng mới:", error);
    }
  }
);
