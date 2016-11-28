package com.geewhizstuff.aquariummanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import static android.R.attr.key;
import static android.R.id.message;

public class HungryAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // For our recurring task, we'll just display a message
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.fishright)
                        .setContentTitle("Aquarium needs your attention")
                        .setContentText("Your fishes are hungry.");

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(2, mBuilder.getNotification());


    }
}
