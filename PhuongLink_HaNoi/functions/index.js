// Import đúng module v2 Firestore trigger
const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const admin                   = require('firebase-admin');
admin.initializeApp();

exports.sendNotificationOnNewPost = onDocumentCreated(
  'posts/{postId}',
  async (event) => {
    const newPost = event.data;

    const payload = {
      notification: {
        title: newPost.title   || 'Bài viết mới',
        body:  newPost.content || ''        // <-- hiển thị nguyên content
      },
      topic: 'all_devices'
    };

    try {
      const response = await admin.messaging().send(payload);
      console.log('✅ FCM sent:', response);
    } catch (error) {
      console.error('❌ FCM error:', error);
    }
  }
);
