package com.jedi.platicar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";
    private static final String CHANNEL_ID = "CHANNEL_69";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "onMessageReceived: ");
        RemoteMessage.Notification notification = message.getNotification();
        if (notification == null) {
            Log.d(TAG, "onMessageReceived: NOTIF NULL");
            return;
        }

        String title = notification.getTitle();
        String msg = notification.getBody();
        getFirebaseMessage(title, msg);
    }

    public void getFirebaseMessage(String title, String msg) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_name)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.createNotificationChannel(channel);
        managerCompat.notify(0, builder.build());
    }

}
