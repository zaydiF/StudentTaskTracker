package com.example.studenttasktracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studenttasktracker.R;
import com.example.studenttasktracker.adapters.TaskAdapter;
import com.example.studenttasktracker.dialogs.EditTaskDialog;
import com.example.studenttasktracker.models.Task;
import com.example.studenttasktracker.utils.DatabaseHelper;
import com.example.studenttasktracker.utils.DateSearchHelper;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeekViewFragment extends Fragment implements
        TaskAdapter.OnTaskClickListener,
        EditTaskDialog.OnTaskUpdatedListener {

    private DatabaseHelper databaseHelper;
    private TaskAdapter taskAdapter;
    private List<Task> weekTasks;
    private TextView tvWeekRange, tvTotalTasks, tvCompletedCount, tvActiveCount;
    private RecyclerView recyclerViewWeek;
    private MaterialButton btnPrevWeek, btnNextWeek;

    private Calendar currentWeek;

    public WeekViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = new DatabaseHelper(getContext());
        initViews(view);
        setupWeekNavigation();
        loadWeekTasks();
    }

    private void initViews(View view) {
        tvWeekRange = view.findViewById(R.id.tvWeekRange);
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvActiveCount = view.findViewById(R.id.tvActiveCount);
        recyclerViewWeek = view.findViewById(R.id.recyclerViewWeek);
        btnPrevWeek = view.findViewById(R.id.btnPrevWeek);
        btnNextWeek = view.findViewById(R.id.btnNextWeek);

        weekTasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(weekTasks, this);

        recyclerViewWeek.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewWeek.setAdapter(taskAdapter);
    }

    private void setupWeekNavigation() {
        currentWeek = Calendar.getInstance();

        btnPrevWeek.setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, -1);
            loadWeekTasks();
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, 1);
            loadWeekTasks();
        });
    }

    private void loadWeekTasks() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String startDate = sdf.format(currentWeek.getTime());

        List<Task> allTasks = databaseHelper.getAllTasks();
        weekTasks = DateSearchHelper.getTasksForWeek(allTasks, startDate);

        // Обновляем статистику
        updateStatistics(weekTasks);

        // Обновляем заголовок недели
        Calendar endWeek = (Calendar) currentWeek.clone();
        endWeek.add(Calendar.DAY_OF_YEAR, 6);
        String weekRange = sdf.format(currentWeek.getTime()) + " - " + sdf.format(endWeek.getTime());
        tvWeekRange.setText("Неделя: " + weekRange);

        taskAdapter.updateTasks(weekTasks);
    }

    private void updateStatistics(List<Task> tasks) {
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int activeTasks = 0;

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completedTasks++;
            } else {
                activeTasks++;
            }
        }

        tvTotalTasks.setText(String.valueOf(totalTasks));
        tvCompletedCount.setText(String.valueOf(completedTasks));
        tvActiveCount.setText(String.valueOf(activeTasks));

        // Обновляем цвета в зависимости от количества
        updateStatisticsColors(totalTasks, completedTasks, activeTasks);
    }

    private void updateStatisticsColors(int total, int completed, int active) {
        // Общий цвет - зависит от общего количества
        if (total == 0) {
            tvTotalTasks.setTextColor(getResources().getColor(R.color.text_secondary));
        } else {
            tvTotalTasks.setTextColor(getResources().getColor(R.color.priority_high));
        }

        // Выполненные - зеленый
        if (completed == 0) {
            tvCompletedCount.setTextColor(getResources().getColor(R.color.text_secondary));
        } else {
            tvCompletedCount.setTextColor(getResources().getColor(R.color.priority_low));
        }

        // Активные - желтый/оранжевый
        if (active == 0) {
            tvActiveCount.setTextColor(getResources().getColor(R.color.text_secondary));
        } else {
            tvActiveCount.setTextColor(getResources().getColor(R.color.priority_medium));
        }
    }

    // Реализация методов интерфейса TaskAdapter.OnTaskClickListener
    @Override
    public void onTaskClick(int position) {
        if (position >= 0 && position < weekTasks.size()) {
            Task task = weekTasks.get(position);
            showTaskDetails(task);
        }
    }

    @Override
    public void onEditClick(int position) {
        if (position >= 0 && position < weekTasks.size()) {
            Task task = weekTasks.get(position);
            showEditTaskDialog(task);
        }
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < weekTasks.size()) {
            Task task = weekTasks.get(position);
            deleteTask(task, position);
        }
    }

    @Override
    public void onCompletedChange(int position, boolean isCompleted) {
        if (position >= 0 && position < weekTasks.size()) {
            Task task = weekTasks.get(position);
            task.setCompleted(isCompleted);
            databaseHelper.updateTask(task);

            // Обновляем статистику
            updateStatistics(weekTasks);

            // Обновляем только эту задачу
            recyclerViewWeek.post(() -> {
                taskAdapter.notifyItemChanged(position);
            });
        }
    }

    // Реализация метода интерфейса EditTaskDialog.OnTaskUpdatedListener
    @Override
    public void onTaskUpdated() {
        // При обновлении задачи перезагружаем список
        loadWeekTasks();
    }

    private void showTaskDetails(Task task) {
        Toast.makeText(getContext(),
                "Задача: " + task.getTitle() +
                        "\nПредмет: " + task.getSubject() +
                        "\nПриоритет: " + getPriorityDisplay(task.getPriority()) +
                        "\nСрок: " + task.getDueDate() +
                        "\nСтатус: " + (task.isCompleted() ? "Выполнена" : "Активна") +
                        "\nОписание: " + (task.getDescription().isEmpty() ? "нет" : task.getDescription()),
                Toast.LENGTH_LONG).show();
    }

    private void showEditTaskDialog(Task task) {
        EditTaskDialog dialog = EditTaskDialog.newInstance(task);
        dialog.show(getParentFragmentManager(), "EditTaskDialog");
    }

    private void deleteTask(Task task, int position) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Удаление задачи")
                .setMessage("Удалить задачу '" + task.getTitle() + "'?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    databaseHelper.deleteTask(task.getId());

                    // Удаляем из списка
                    weekTasks.remove(position);
                    taskAdapter.notifyItemRemoved(position);

                    Toast.makeText(getContext(), "Задача удалена", Toast.LENGTH_SHORT).show();

                    // Обновляем статистику
                    updateStatistics(weekTasks);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String getPriorityDisplay(String priority) {
        switch (priority) {
            case "HIGH": return "Высокий";
            case "MEDIUM": return "Средний";
            case "LOW": return "Низкий";
            default: return "Средний";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWeekTasks();
    }
}