package com.example.pr25;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ru.example.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationPosted: " + sbn.getPackageName());

        Intent intent = new Intent("ru.example.NOTIFICATION_LISTENER_EXAMPLE");
        intent.setPackage(getPackageName()); // ВАЖНО
        intent.putExtra("notification_event", "onNotificationPosted:\n" + sbn.getPackageName() + "\n");
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationRemoved: " + sbn.getPackageName());

        Intent intent = new Intent("ru.example.NOTIFICATION_LISTENER_EXAMPLE");
        intent.setPackage(getPackageName()); // ВАЖНО
        intent.putExtra("notification_event", "onNotificationRemoved:\n" + sbn.getPackageName() + "\n");
        sendBroadcast(intent);
    }

    class NLServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra("command");
            if (command != null) {
                if (command.equals("clearall")) {
                    Log.i(TAG, "Команда clearall получена! Очищаем...");
                    NLService.this.cancelAllNotifications();
                } else if (command.equals("list")) {
                    Log.i(TAG, "Команда list получена! Собираем список...");

                    Intent notificationIntent = new Intent("ru.example.NOTIFICATION_LISTENER_EXAMPLE");
                    notificationIntent.setPackage(getPackageName());
                    notificationIntent.putExtra("notification_event", "==========");
                    sendBroadcast(notificationIntent);

                    int i = 1;
                    for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                        Intent listIntent = new Intent("ru.example.NOTIFICATION_LISTENER_EXAMPLE");
                        listIntent.setPackage(getPackageName());
                        listIntent.putExtra("notification_event", i + " " + sbn.getPackageName() + "\n");
                        sendBroadcast(listIntent);
                        i++;
                    }
                    Intent listIntent = new Intent("ru.example.NOTIFICATION_LISTENER_EXAMPLE");
                    listIntent.setPackage(getPackageName());
                    listIntent.putExtra("notification_event", "Notification List");
                    sendBroadcast(listIntent);
                }
            }
        }
    }
}