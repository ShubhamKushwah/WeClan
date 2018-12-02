package com.syberkeep.weclan;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * To create notifications even while app is in foreground.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationMessage = remoteMessage.getNotification().getBody();
        //this will also help to get the exact same message from the functions file which contains the full_name of the request sender.

        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage);

        Intent notifResultIntent = new Intent(click_action);
        notifResultIntent.putExtra("user_id", from_user_id);

        //Also add 'action' and 'category' in the tags of the target activity in the AndroidManifest.xml
        //Also add it in the index.js file of firebase functions.

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notifResultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(pendingIntent);

        int notification_Id = (int) System.currentTimeMillis();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(notification_Id, mBuilder.build());

    }
}