package com.leanhquan.deliveryfoodserver.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.leanhquan.deliveryfoodserver.Model.Request;
import com.leanhquan.deliveryfoodserver.OrderActivity;
import com.leanhquan.deliveryfoodserver.R;

import java.util.Random;

public class OrderListenerService extends Service implements ChildEventListener {

    private FirebaseDatabase database;
    private DatabaseReference order;

    public OrderListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        database = FirebaseDatabase.getInstance();
        order = database.getReference("requests");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        order.addChildEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Request request = snapshot.getValue(Request.class);
        if (request.getStatus().equals("0")){
            showNotification(snapshot.getKey(), request);
        }
    }

    private void showNotification(String key, Request request) {
        String channelId = "default_channel_id";
        Intent i = new Intent(getBaseContext(), OrderActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),0, i, 0);

        NotificationCompat.Builder notice = new NotificationCompat.Builder(getBaseContext(), channelId);
        notice.setAutoCancel(true)
                .setPriority(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("Delivery App")
                .setContentInfo("Order đơn hàng mới")
                .setContentText("Bạn vừa nhận được đơn hàng mới #"+key)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_box);
        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

        int ramdonInt = new Random().nextInt(9999-1)+1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }


        assert notificationManager != null;
        notificationManager.notify(ramdonInt,notice.build());
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}
