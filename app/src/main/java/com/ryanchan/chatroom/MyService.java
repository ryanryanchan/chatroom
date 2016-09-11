package com.ryanchan.chatroom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

/**
 * Created by ryanchan on 8/30/16.
 */

public class MyService extends Service {

    private Firebase f = new Firebase("https://chat-bbfbf.firebaseio.com/chat");
    Query q = f.limitToLast(1);
    static boolean notify_on = false;
    private final IBinder myBinder = new MyLocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        q.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(notify_on){
                    notif("");
                    Log.v("CHILD ADDED", dataSnapshot.toString());
                }else{
                    Log.v("SUCCESSFULLY IGNORED", dataSnapshot.toString());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notify_on = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notify_on = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }



    public static void setNotify_on(){
        notify_on = true;
    }

    public static void setNotify_off(){
        notify_on = false;
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

    public class MyLocalBinder extends Binder{
        MyService getService(){
            return MyService.this;
        }
    }



}
