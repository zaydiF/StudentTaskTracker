package com.example.studenttasktracker.utils;

import android.content.Context;
import android.widget.Toast;

public class QuickActionsHelper {

    public static void markAllCompleted(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        int updated = dbHelper.markAllTasksCompleted();
        Toast.makeText(context, "Отмечено выполнено: " + updated + " задач", Toast.LENGTH_SHORT).show();
    }

    public static void deleteCompletedTasks(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        int deleted = dbHelper.deleteCompletedTasks();
        Toast.makeText(context, "Удалено: " + deleted + " выполненных задач", Toast.LENGTH_SHORT).show();
    }
}