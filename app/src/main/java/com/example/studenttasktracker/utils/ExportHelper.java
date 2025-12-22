package com.example.studenttasktracker.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import com.example.studenttasktracker.models.Task;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportHelper {

    public static boolean exportTasksToCsv(Context context, List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            Toast.makeText(context, "Нет задач для экспорта", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
            File file = new File(downloadsDir, "student_tasks_" + timestamp + ".csv");

            FileWriter writer = new FileWriter(file);

            writer.write("Название;Предмет;Приоритет;Срок;Статус;Описание;Дата создания\n");

            for (Task task : tasks) {
                writer.write(String.format(Locale.getDefault(),
                        "\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\"\n",
                        task.getTitle(),
                        task.getSubject(),
                        getPriorityDisplay(task.getPriority()),
                        task.getDueDate(),
                        task.isCompleted() ? "Выполнена" : "Активна",
                        task.getDescription(),
                        task.getCreatedAt()
                ));
            }

            writer.flush();
            writer.close();

            Toast.makeText(context, "Задачи экспортированы в: " + file.getName(), Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException e) {
            Toast.makeText(context, "Ошибка экспорта: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private static String getPriorityDisplay(String priority) {
        switch (priority) {
            case "HIGH": return "Высокий";
            case "MEDIUM": return "Средний";
            case "LOW": return "Низкий";
            default: return "Средний";
        }
    }
}