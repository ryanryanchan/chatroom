package com.ryanchan.chatroom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by ryanchan on 8/30/16.
 */

public class MyService extends Service {

    private Firebase f = new Firebase("https://chat-bbfbf.firebaseio.com/chat");
    private ValueEventListener handler;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(MyService.this, "SERVICE STARTED BITCH", Toast.LENGTH_SHORT).show();

        handler = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notif("");
                //dataSnapshot.getValue().toString()

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        f.addValueEventListener(handler);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(MyService.this, "SERVICE ENDED BITCH", Toast.LENGTH_SHORT).show();
        handler = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notif(String notifString) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = R.drawable.ic_stat_ic_notification;
        Notification.Builder builder = new Notification.Builder(this);


        Context context = getApplicationContext();

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        builder.setSmallIcon(icon)
                .setContentTitle("chatroom")
                .setContentIntent(contentIntent)
                .setContentText("oh boy someone said something")
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(new long[]{500}).build();
        mNotificationManager.notify(1, builder.build());
    }

}
