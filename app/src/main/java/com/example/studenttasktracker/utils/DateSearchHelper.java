package com.example.studenttasktracker.utils;

import com.example.studenttasktracker.models.Task;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateSearchHelper {

    public static List<Task> getTasksForDate(List<Task> tasks, String date) {
        List<Task> result = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getDueDate() != null && task.getDueDate().equals(date)) {
                result.add(task);
            }
        }
        return result;
    }

    public static List<Task> getTasksForWeek(List<Task> tasks, String startDate) {
        List<Task> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        try {
            Date start = sdf.parse(startDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

            List<String> weekDates = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                weekDates.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            for (Task task : tasks) {
                if (weekDates.contains(task.getDueDate())) {
                    result.add(task);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
}