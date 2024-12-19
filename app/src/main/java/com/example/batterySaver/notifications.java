package com.example.batterySaver;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class notifications extends NotificationListenerService {

    private static final String TAG = "InappropriateNotifService";

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        super.onNotificationPosted(notification);
        // Get the notification title and content
        String title = notification.getNotification().extras.getString("android.title");
        String content = notification.getNotification().extras.getString("android.text");

        // Log for debugging
        Log.d(TAG, "Notification Title: " + title);
        Log.d(TAG, "Notification Content: " + content);

        // Perform your content filtering logic here
        if (isInappropriate(title, content)) {
            // Handle inappropriate notification
            handleInappropriateNotification(title, content);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        super.onNotificationRemoved(notification);
        // Handle notification removal if needed
    }

    // Example method for content filtering
    private boolean isInappropriate(String title, String content) {
        // Implement your logic to detect inappropriate content
        // For instance, check for forbidden words, phrases, or URLs
        return content != null && (content.contains("badword") || content.contains("inappropriate phrase"));
    }

    private void handleInappropriateNotification(String title, String content) {
        // Implement how you want to handle inappropriate notifications
        // This could include logging the event, sending alerts, or notifying parents
        Log.w(TAG, "Inappropriate Notification Detected - Title: " + title + ", Content: " + content);
        // Additional handling logic here
    }
}
