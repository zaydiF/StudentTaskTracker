package com.example.studenttasktracker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.studenttasktracker.models.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "student_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Таблица задач
    private static final String TABLE_TASKS = "tasks";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_DUE_DATE = "due_date";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_CREATED_AT = "created_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_SUBJECT + " TEXT,"
                + KEY_PRIORITY + " TEXT,"
                + KEY_DUE_DATE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // Добавление задачи
    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, task.getTitle());
        values.put(KEY_SUBJECT, task.getSubject());
        values.put(KEY_PRIORITY, task.getPriority());
        values.put(KEY_DUE_DATE, task.getDueDate());
        values.put(KEY_DESCRIPTION, task.getDescription());
        values.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    // Получение всех задач
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " ORDER BY " + KEY_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setSubject(cursor.getString(2));
                task.setPriority(cursor.getString(3));
                task.setDueDate(cursor.getString(4));
                task.setDescription(cursor.getString(5));
                task.setCompleted(cursor.getInt(6) == 1);
                task.setCreatedAt(cursor.getString(7));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Обновление задачи
    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, task.getTitle());
        values.put(KEY_SUBJECT, task.getSubject());
        values.put(KEY_PRIORITY, task.getPriority());
        values.put(KEY_DUE_DATE, task.getDueDate());
        values.put(KEY_DESCRIPTION, task.getDescription());
        values.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);

        db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    // Удаление задачи
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Получение статистики
    public int getTotalTasks() {
        String countQuery = "SELECT * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getCompletedTasks() {
        String countQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + KEY_COMPLETED + " = 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getPendingTasks() {
        String countQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + KEY_COMPLETED + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Получение задач с фильтрацией
    public List<Task> getFilteredTasks(String subjectFilter, String priorityFilter, Boolean completedFilter) {
        List<Task> taskList = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + TABLE_TASKS + " WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (subjectFilter != null && !subjectFilter.equals("Предметы")) {
            queryBuilder.append(" AND ").append(KEY_SUBJECT).append(" = ?");
            params.add(subjectFilter);
        }

        if (priorityFilter != null && !priorityFilter.equals("Приоритеты")) {
            String priorityValue = getPriorityValue(priorityFilter);
            queryBuilder.append(" AND ").append(KEY_PRIORITY).append(" = ?");
            params.add(priorityValue);
        }

        if (completedFilter != null) {
            queryBuilder.append(" AND ").append(KEY_COMPLETED).append(" = ?");
            params.add(completedFilter ? "1" : "0");
        }

        queryBuilder.append(" ORDER BY ").append(KEY_CREATED_AT).append(" DESC");

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(queryBuilder.toString(), params.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setSubject(cursor.getString(2));
                task.setPriority(cursor.getString(3));
                task.setDueDate(cursor.getString(4));
                task.setDescription(cursor.getString(5));
                task.setCompleted(cursor.getInt(6) == 1);
                task.setCreatedAt(cursor.getString(7));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Поиск задач
    public List<Task> searchTasks(String query) {
        List<Task> taskList = new ArrayList<>();
        String searchQuery = "SELECT * FROM " + TABLE_TASKS +
                " WHERE " + KEY_TITLE + " LIKE ? OR " + KEY_SUBJECT + " LIKE ? OR " + KEY_DESCRIPTION + " LIKE ?" +
                " ORDER BY " + KEY_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        String searchTerm = "%" + query + "%";
        Cursor cursor = db.rawQuery(searchQuery, new String[]{searchTerm, searchTerm, searchTerm});

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setSubject(cursor.getString(2));
                task.setPriority(cursor.getString(3));
                task.setDueDate(cursor.getString(4));
                task.setDescription(cursor.getString(5));
                task.setCompleted(cursor.getInt(6) == 1);
                task.setCreatedAt(cursor.getString(7));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    private String getPriorityValue(String displayPriority) {
        switch (displayPriority) {
            case "Высокий": return "HIGH";
            case "Средний": return "MEDIUM";
            case "Низкий": return "LOW";
            default: return displayPriority;
        }
    }

    // Получение всех уникальных предметов
    public List<String> getAllSubjects() {
        List<String> subjects = new ArrayList<>();
        subjects.add("Предметы");

        String query = "SELECT DISTINCT " + KEY_SUBJECT + " FROM " + TABLE_TASKS + " ORDER BY " + KEY_SUBJECT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                subjects.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return subjects;
    }

    // Получение количества задач по приоритетам
    public int getTasksCountByPriority(String priority) {
        String countQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + KEY_PRIORITY + " = ? AND " + KEY_COMPLETED + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{priority});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Получение количества задач по предметам
    public int getTasksCountBySubject(String subject) {
        String countQuery = "SELECT * FROM " + TABLE_TASKS + " WHERE " + KEY_SUBJECT + " = ? AND " + KEY_COMPLETED + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{subject});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Получение просроченных задач
    public List<Task> getOverdueTasks() {
        List<Task> taskList = new ArrayList<>();
        String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());

        String query = "SELECT * FROM " + TABLE_TASKS +
                " WHERE " + KEY_COMPLETED + " = 0 AND " + KEY_DUE_DATE + " < ?" +
                " ORDER BY " + KEY_DUE_DATE + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{currentDate});

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setSubject(cursor.getString(2));
                task.setPriority(cursor.getString(3));
                task.setDueDate(cursor.getString(4));
                task.setDescription(cursor.getString(5));
                task.setCompleted(cursor.getInt(6) == 1);
                task.setCreatedAt(cursor.getString(7));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Получение ближайших задач (на сегодня и завтра)
    public List<Task> getUpcomingTasks() {
        List<Task> taskList = new ArrayList<>();

        // Получаем сегодняшнюю и завтрашнюю дату
        Calendar calendar = Calendar.getInstance();
        String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.getTime());

        String query = "SELECT * FROM " + TABLE_TASKS +
                " WHERE " + KEY_COMPLETED + " = 0 AND (" + KEY_DUE_DATE + " = ? OR " + KEY_DUE_DATE + " = ?)" +
                " ORDER BY " + KEY_DUE_DATE + " ASC, " + KEY_PRIORITY + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{today, tomorrow});

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setSubject(cursor.getString(2));
                task.setPriority(cursor.getString(3));
                task.setDueDate(cursor.getString(4));
                task.setDescription(cursor.getString(5));
                task.setCompleted(cursor.getInt(6) == 1);
                task.setCreatedAt(cursor.getString(7));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }

    // Отметить все задачи выполненными
    public int markAllTasksCompleted() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_COMPLETED, 1);

        int count = db.update(TABLE_TASKS, values, KEY_COMPLETED + " = ?", new String[]{"0"});
        db.close();
        return count;
    }

    // Удалить выполненные задачи
    public int deleteCompletedTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(TABLE_TASKS, KEY_COMPLETED + " = ?", new String[]{"1"});
        db.close();
        return count;
    }

    // Получить задачи на сегодня
    public List<Task> getTodayTasks() {
        List<Task> taskList = new ArrayList<>();
        String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());

        String query = "SELECT * FROM " + TABLE_TASKS +
                " WHERE " + KEY_DUE_DATE + " = ? AND " + KEY_COMPLETED + " = 0" +
                " ORDER BY " + KEY_PRIORITY + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{today});

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
                task.setSubject(cursor.getString(2));
                task.setPriority(cursor.getString(3));
                task.setDueDate(cursor.getString(4));
                task.setDescription(cursor.getString(5));
                task.setCompleted(cursor.getInt(6) == 1);
                task.setCreatedAt(cursor.getString(7));

                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }
}