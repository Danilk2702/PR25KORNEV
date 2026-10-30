package com.example.pr25;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFY_ID = 101;
    private static final String CHANNEL_ID = "Cat channel";

    private TextView tvInfo;
    private NotificationBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInfo = findViewById(R.id.tvInfo);

        // 1. Запрос разрешения для Android 13+
        requestNotificationPermission();

        // 2. Создаем канал уведомлений
        createNotificationChannel();

        // 3. Регистрируем приемник для службы уведомлений (ИСПРАВЛЕНО ДЛЯ ANDROID 13+)
        mReceiver = new NotificationBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ru.example.NOTIFICATION_LISTENER_EXAMPLE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mReceiver, filter);
        }

        // 4. Настройка кнопок
        setupButtons();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Канал для кота",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Уведомления о кормлении кота");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void setupButtons() {
        findViewById(R.id.btnShowBasic).setOnClickListener(v -> showBasicNotification());
        findViewById(R.id.btnShowBigText).setOnClickListener(v -> showBigTextNotification());
        findViewById(R.id.btnShowBigPicture).setOnClickListener(v -> showBigPictureNotification());
        findViewById(R.id.btnShowInbox).setOnClickListener(v -> showInboxNotification());
        findViewById(R.id.btnShowMessaging).setOnClickListener(v -> showMessagingNotification());
        findViewById(R.id.btnListNotifications).setOnClickListener(v -> sendCommandToService("list"));
        findViewById(R.id.btnClearNotifications).setOnClickListener(v -> sendCommandToService("clearall"));
    }


    private void showBasicNotification() {
        // Задание 2: Реакция на уведомление (PendingIntent) + Флаг IMMUTABLE (обязателен для Android 12+)
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                // Вместо R.drawable.ic_pets_black_24dp используем стандартную иконку
                .setSmallIcon(R.drawable.ic_pets_black_24dp)
                .setContentTitle("Напоминание")
                .setContentText("Пора покормить кота")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)


                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.hungrycat))
                .setTicker("Последнее китайское предупреждение!")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Нет разрешения на уведомления", Toast.LENGTH_SHORT).show();
            return;
        }
        notificationManager.notify(NOTIFY_ID, builder.build());
    }



    private void showBigTextNotification() {
        String bigText = "Это я, почтальон Печкин. Принёс для вас посылку. " +
                "Только я вам её не отдам. Потому что у вас документов нету.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("Посылка")
                .setContentText("Это я, почтальон Печкин...")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(NOTIFY_ID + 1, builder.build());
        }
    }

    private void showBigPictureNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentTitle("Посылка")
                .setContentText("Это я, почтальон Печкин.")
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery)))
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(NOTIFY_ID + 2, builder.build());
        }
    }

    private void showInboxNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Посылка")
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Первое сообщение")
                        .addLine("Второе сообщение")
                        .addLine("Третье сообщение")
                        .addLine("Четвертое сообщение")
                        .setSummaryText("+2 more"))
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(NOTIFY_ID + 3, builder.build());
        }
    }

    private void showMessagingNotification() {
        Person murzik = new Person.Builder().setName("Мурзик").build();
        Person vaska = new Person.Builder().setName("Васька").build();

        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(murzik)
                .setConversationTitle("Android chat")
                .addMessage("Привет котаны!", System.currentTimeMillis(), murzik)
                .addMessage("А вы знали, что чат по-французски кошка?", System.currentTimeMillis(), murzik)
                .addMessage("Круто!", System.currentTimeMillis(), vaska)
                .addMessage("Ми-ми-ми", System.currentTimeMillis(), vaska);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setStyle(messagingStyle)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(NOTIFY_ID + 4, builder.build());
        }
    }


    private void sendCommandToService(String command) {
        Intent intent = new Intent("ru.example.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        intent.setPackage(getPackageName());
        intent.putExtra("command", command);
        sendBroadcast(intent);
    }

    class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra("notification_event") + "\n" + tvInfo.getText();
            tvInfo.setText(temp);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}