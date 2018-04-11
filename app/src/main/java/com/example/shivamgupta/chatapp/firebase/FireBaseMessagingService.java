package com.example.shivamgupta.chatapp.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.shivamgupta.chatapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Shivam Gupta on 10-04-2018.
 */

public class FireBaseMessagingService extends FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();

        String from_user_id = remoteMessage.getData().get("from_user_id");

        String clickAction = remoteMessage.getNotification().getClickAction();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resultIntent = new Intent(clickAction);
        resultIntent.putExtra("user_id" , from_user_id);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this ,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifMgr.notify(mNotificationId , mBuilder.build());
    }
}
