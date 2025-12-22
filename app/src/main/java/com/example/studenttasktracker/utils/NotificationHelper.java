package com.example.studenttasktracker.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.studenttasktracker.R;
import com.example.studenttasktracker.activities.MainActivity;
import com.example.studenttasktracker.models.Task;
import java.util.List;

public class NotificationHelper {
    private static final String CHANNEL_ID = "student_tracker_channel";
    private static final String CHANNEL_NAME = "Student Tracker Notifications";
    private static final int OVERDUE_NOTIFICATION_ID = 1;
    private static final int TODAY_NOTIFICATION_ID = 2;

    public static void showOverdueNotification(Context context, List<Task> overdueTasks) {
        if (overdueTasks.isEmpty()) return;

        createNotificationChannel(context);

        String message = overdueTasks.size() + " задач просрочено!";
        if (overdueTasks.size() == 1) {
            message = "Задача '" + overdueTasks.get(0).getTitle() + "' просрочена!";
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Твоя иконка
                // Или используй системную: .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("📅 Просроченные задачи")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(OVERDUE_NOTIFICATION_ID, builder.build());
    }

    public static void showTodayTasksNotification(Context context, List<Task> todayTasks) {
        if (todayTasks.isEmpty()) return;

        createNotificationChannel(context);

        String message = "Сегодня: " + todayTasks.size() + " задач";
        if (todayTasks.size() == 1) {
            message = "Сегодня: '" + todayTasks.get(0).getTitle() + "'";
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Твоя иконка
                // Или используй системную: .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle("🎯 Задачи на сегодня")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(TODAY_NOTIFICATION_ID, builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Уведомления о просроченных задачах");

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}