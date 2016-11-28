package com.geewhizstuff.aquariummanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;


import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CleanAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // For our recurring task, we'll just display a message
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.fishright)
                        .setContentTitle("Aquarium needs your attention")
                        .setContentText("You need to clean your fish tank");

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.getNotification());
    }
}

