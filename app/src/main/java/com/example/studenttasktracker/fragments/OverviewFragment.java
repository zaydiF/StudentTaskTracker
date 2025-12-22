package com.example.studenttasktracker.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studenttasktracker.R;
import com.example.studenttasktracker.adapters.TaskAdapter;
import com.example.studenttasktracker.dialogs.AddTaskDialog;
import com.example.studenttasktracker.dialogs.EditTaskDialog;
import com.example.studenttasktracker.models.Task;
import com.example.studenttasktracker.utils.DatabaseHelper;
import com.example.studenttasktracker.utils.ExportHelper;
import com.example.studenttasktracker.utils.QuickActionsHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment implements
        TaskAdapter.OnTaskClickListener,
        AddTaskDialog.OnTaskAddedListener,
        EditTaskDialog.OnTaskUpdatedListener {

    private DatabaseHelper databaseHelper;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();

    // TextViews статистики
    private TextView tvTotalTasks, tvCompletedTasks, tvRemainingTasks;
    private TextView tvActiveFilters;
    private RecyclerView recyclerViewTasks;

    // Кнопки (MaterialButton)
    private MaterialButton btnAddTask, btnMarkAllCompleted, btnDeleteCompleted, btnExport;

    // Кнопка сброса (TextView в layout)
    private TextView btnClearFilters;

    // Кнопка очистки поиска
    private ImageView btnClearSearch;

    // Фильтры
    private TextInputEditText etSearch;
    private Spinner spinnerSubjectFilter, spinnerPriorityFilter, spinnerStatusFilter;

    private String currentSubjectFilter = "Предметы";
    private String currentPriorityFilter = "Приоритеты";
    private String currentStatusFilter = "Задачи";
    private String currentSearchQuery = "";

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) return;

        databaseHelper = new DatabaseHelper(getContext());
        initViews(view);
        setupRecyclerView();
        setupFilters();
        setupSearch();
        loadStatistics();
        loadTasks();
    }

    private void initViews(View view) {
        // Статистика
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvRemainingTasks = view.findViewById(R.id.tvRemainingTasks);

        // Фильтры
        tvActiveFilters = view.findViewById(R.id.tvActiveFilters);
        etSearch = view.findViewById(R.id.etSearch);
        spinnerSubjectFilter = view.findViewById(R.id.spinnerSubjectFilter);
        spinnerPriorityFilter = view.findViewById(R.id.spinnerPriorityFilter);
        spinnerStatusFilter = view.findViewById(R.id.spinnerStatusFilter);
        btnClearFilters = view.findViewById(R.id.btnClearFilters);

        // Кнопка очистки поиска
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        // Кнопки MaterialButton
        btnAddTask = view.findViewById(R.id.btnAddTask);
        btnMarkAllCompleted = view.findViewById(R.id.btnMarkAllCompleted);
        btnDeleteCompleted = view.findViewById(R.id.btnDeleteCompleted);
        btnExport = view.findViewById(R.id.btnExport);

        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);

        // Обработчики кликов БЕЗОПАСНЫЕ
        if (btnAddTask != null) btnAddTask.setOnClickListener(v -> showAddTaskDialog());
        if (btnMarkAllCompleted != null) btnMarkAllCompleted.setOnClickListener(v -> markAllCompleted());
        if (btnDeleteCompleted != null) btnDeleteCompleted.setOnClickListener(v -> deleteCompletedTasks());
        if (btnExport != null) btnExport.setOnClickListener(v -> exportTasks());
        if (btnClearFilters != null) btnClearFilters.setOnClickListener(v -> clearFilters());
        if (btnClearSearch != null) btnClearSearch.setOnClickListener(v -> clearSearch());

        // tvActiveFilters - ТОЛЬКО ВИЗУАЛИЗАЦИЯ, БЕЗ КЛИКА!
        if (tvActiveFilters != null) {
            updateActiveFiltersCount(); // Инициализируем цвета
        }

        // Цвета карточек статистики
        MaterialCardView cardTotal = view.findViewById(R.id.cardTotal);
        MaterialCardView cardCompleted = view.findViewById(R.id.cardCompleted);
        MaterialCardView cardRemaining = view.findViewById(R.id.cardRemaining);

        if (cardTotal != null && getResources() != null) {
            cardTotal.setCardBackgroundColor(getResources().getColor(R.color.priority_high, null));
        }
        if (cardCompleted != null && getResources() != null) {
            cardCompleted.setCardBackgroundColor(getResources().getColor(R.color.priority_low, null));
        }
        if (cardRemaining != null && getResources() != null) {
            cardRemaining.setCardBackgroundColor(getResources().getColor(R.color.priority_medium, null));
        }
    }

    private void setupRecyclerView() {
        if (recyclerViewTasks == null || getContext() == null) return;

        taskAdapter = new TaskAdapter(taskList, this);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupFilters() {
        // Предметы
        try {
            List<String> subjects = new ArrayList<>();
            if (databaseHelper != null) {
                subjects = databaseHelper.getAllSubjects();
            }
            if (subjects.isEmpty()) {
                subjects.add("Предметы");
            }
            if (spinnerSubjectFilter != null && getContext() != null) {
                ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, subjects);
                subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSubjectFilter.setAdapter(subjectAdapter);
                spinnerSubjectFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        if (parent != null) {
                            currentSubjectFilter = parent.getItemAtPosition(position).toString();
                            applyFilters();
                            updateActiveFiltersCount();
                        }
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            }
        } catch (Exception ignored) {}

        // Приоритеты
        String[] priorities = {"Приоритеты", "Высокий", "Средний", "Низкий"};
        if (spinnerPriorityFilter != null && getContext() != null) {
            ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, priorities);
            priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPriorityFilter.setAdapter(priorityAdapter);
            spinnerPriorityFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (parent != null) {
                        currentPriorityFilter = parent.getItemAtPosition(position).toString();
                        applyFilters();
                        updateActiveFiltersCount();
                    }
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }

        // Статус
        String[] statuses = {"Задачи", "Активные", "Выполненные"};
        if (spinnerStatusFilter != null && getContext() != null) {
            ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, statuses);
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatusFilter.setAdapter(statusAdapter);
            spinnerStatusFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (parent != null) {
                        currentStatusFilter = parent.getItemAtPosition(position).toString();
                        applyFilters();
                        updateActiveFiltersCount();
                    }
                }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s != null ? s.toString() : "";
                    applyFilters();
                    updateActiveFiltersCount();

                    // Показываем/скрываем кнопку очистки поиска
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(currentSearchQuery.trim().isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    // Анимация поиска при вводе
                    if (etSearch != null && !currentSearchQuery.trim().isEmpty()) {
                        etSearch.setHint(""); // Убираем подсказку когда есть текст
                    } else {
                        etSearch.setHint("Поиск задач...");
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void clearSearch() {
        if (etSearch != null) {
            etSearch.setText("");
            if (btnClearSearch != null) {
                btnClearSearch.setVisibility(View.GONE);
            }
        }
    }

    private void applyFilters() {
        if (taskAdapter == null) return;

        try {
            List<Task> filteredList = new ArrayList<>();
            if (databaseHelper != null) {
                if (!currentSearchQuery.trim().isEmpty()) {
                    filteredList = databaseHelper.searchTasks(currentSearchQuery);
                } else {
                    Boolean completedFilter = null;
                    if ("Активные".equals(currentStatusFilter)) completedFilter = false;
                    else if ("Выполненные".equals(currentStatusFilter)) completedFilter = true;

                    String subjectFilter = "Предметы".equals(currentSubjectFilter) ? null : currentSubjectFilter;
                    String priorityFilter = "Приоритеты".equals(currentPriorityFilter) ? null : currentPriorityFilter;

                    filteredList = databaseHelper.getFilteredTasks(subjectFilter, priorityFilter, completedFilter);
                }
            }
            taskList.clear();
            taskList.addAll(filteredList);
            taskAdapter.updateTasks(taskList);

            if (taskList.isEmpty()) showEmptyState();
            else hideEmptyState();

            // Обновляем статистику после фильтрации
            loadStatistics();

        } catch (Exception e) {
            taskList.clear();
            taskAdapter.updateTasks(taskList);
            showEmptyState();
        }
    }

    private int countActiveFilters() {
        int count = 0;
        if (!currentSearchQuery.trim().isEmpty()) count++;
        if (!"Предметы".equals(currentSubjectFilter)) count++;
        if (!"Приоритеты".equals(currentPriorityFilter)) count++;
        if (!"Задачи".equals(currentStatusFilter)) count++;
        return count;
    }

    private void updateActiveFiltersCount() {
        if (tvActiveFilters == null || getResources() == null) return;

        int count = countActiveFilters();
        tvActiveFilters.setText("Фильтры: " + count);

        // 🎨 ЦВЕТА В ЗАВИСИМОСТИ ОТ КОЛИЧЕСТВА ФИЛЬТРОВ
        if (count == 0) {
            // 0 фильтров - обычный белый фон, серый текст
            tvActiveFilters.setTextColor(getResources().getColor(R.color.text_primary, null));
            tvActiveFilters.setBackgroundColor(getResources().getColor(android.R.color.white, null));
        } else if (count == 1) {
            // 1 фильтр - ЗЕЛЁНЫЙ
            tvActiveFilters.setTextColor(getResources().getColor(android.R.color.white, null));
            tvActiveFilters.setBackgroundColor(getResources().getColor(R.color.priority_low, null));
        } else if (count == 2) {
            // 2 фильтра - ЖЁЛТЫЙ
            tvActiveFilters.setTextColor(getResources().getColor(android.R.color.white, null));
            tvActiveFilters.setBackgroundColor(getResources().getColor(R.color.priority_medium, null));
        } else if (count >= 3) {
            // 3+ фильтров - КРАСНЫЙ
            tvActiveFilters.setTextColor(getResources().getColor(android.R.color.white, null));
            tvActiveFilters.setBackgroundColor(getResources().getColor(R.color.priority_high, null));
        }
    }

    private void clearFilters() {
        if (spinnerSubjectFilter != null) spinnerSubjectFilter.setSelection(0, false);
        if (spinnerPriorityFilter != null) spinnerPriorityFilter.setSelection(0, false);
        if (spinnerStatusFilter != null) spinnerStatusFilter.setSelection(0, false);
        clearSearch(); // Используем метод очистки поиска

        currentSubjectFilter = "Предметы";
        currentPriorityFilter = "Приоритеты";
        currentStatusFilter = "Задачи";

        loadTasks();
    }

    private void showEmptyState() {
        TextView tvEmptyState = getView() != null ? getView().findViewById(R.id.tvEmptyState) : null;
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            if (!currentSearchQuery.trim().isEmpty()) {
                tvEmptyState.setText("Задачи не найдены: \"" + currentSearchQuery + "\"");
            } else if (countActiveFilters() > 0) {
                tvEmptyState.setText("Нет задач по выбранным фильтрам");
            } else {
                tvEmptyState.setText("Нет задач");
            }
        }
    }

    private void hideEmptyState() {
        TextView tvEmptyState = getView() != null ? getView().findViewById(R.id.tvEmptyState) : null;
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
    }

    private void loadStatistics() {
        if (databaseHelper == null) return;
        try {
            int total = databaseHelper.getTotalTasks();
            int completed = databaseHelper.getCompletedTasks();
            int remaining = databaseHelper.getPendingTasks();

            if (tvTotalTasks != null) tvTotalTasks.setText(String.valueOf(total));
            if (tvCompletedTasks != null) tvCompletedTasks.setText(String.valueOf(completed));
            if (tvRemainingTasks != null) tvRemainingTasks.setText(String.valueOf(remaining));
        } catch (Exception ignored) {}
    }

    private void loadTasks() {
        if (taskAdapter == null) return;
        try {
            if (databaseHelper != null) {
                taskList = databaseHelper.getAllTasks();
            }
        } catch (Exception e) {
            taskList = new ArrayList<>();
        }

        taskAdapter.updateTasks(taskList);
        loadStatistics();
        updateActiveFiltersCount();

        if (taskList.isEmpty()) showEmptyState();
        else hideEmptyState();
    }

    private void showAddTaskDialog() {
        if (getContext() == null || getParentFragmentManager() == null) return;
        try {
            AddTaskDialog dialog = new AddTaskDialog();
            dialog.show(getParentFragmentManager(), "AddTaskDialog");
        } catch (Exception e) {
            android.widget.Toast.makeText(getContext(), "Ошибка добавления", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskClick(int position) {
        if (position >= 0 && position < taskList.size()) {
            Task task = taskList.get(position);
            showTaskDetails(task);
        }
    }

    @Override
    public void onEditClick(int position) {
        if (position >= 0 && position < taskList.size() && getParentFragmentManager() != null) {
            Task task = taskList.get(position);
            EditTaskDialog dialog = EditTaskDialog.newInstance(task);
            dialog.show(getParentFragmentManager(), "EditTaskDialog");
        }
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < taskList.size() && databaseHelper != null) {
            Task task = taskList.get(position);
            databaseHelper.deleteTask(task.getId());
            loadTasks();
        }
    }

    @Override
    public void onCompletedChange(int position, boolean isCompleted) {
        if (position >= 0 && position < taskList.size() && databaseHelper != null && recyclerViewTasks != null) {
            Task task = taskList.get(position);
            task.setCompleted(isCompleted);
            databaseHelper.updateTask(task);
            recyclerViewTasks.post(() -> {
                loadStatistics();
                if (taskAdapter != null) taskAdapter.notifyItemChanged(position);
            });
        }
    }

    @Override
    public void onTaskAdded() { loadTasks(); }
    @Override
    public void onTaskUpdated() { loadTasks(); }

    private void showTaskDetails(Task task) {
        if (getContext() == null || task == null) return;
        String details = String.format("📋 Задача: %s\n📚 Предмет: %s\n⚡ Приоритет: %s\n📅 Срок: %s\n✅ Статус: %s",
                task.getTitle(), task.getSubject(), getPriorityDisplay(task.getPriority()),
                task.getDueDate(), task.isCompleted() ? "Выполнена" : "Активна");
        android.widget.Toast.makeText(getContext(), details, android.widget.Toast.LENGTH_LONG).show();
    }

    private String getPriorityDisplay(String priority) {
        if (priority == null) return "Средний";
        switch (priority) {
            case "HIGH": return "Высокий";
            case "MEDIUM": return "Средний";
            case "LOW": return "Низкий";
            default: return priority;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();

        // Восстанавливаем состояние поиска
        if (etSearch != null && !currentSearchQuery.trim().isEmpty()) {
            etSearch.setText(currentSearchQuery);
            if (btnClearSearch != null) {
                btnClearSearch.setVisibility(View.VISIBLE);
            }
        }
    }

    private void markAllCompleted() {
        if (getContext() == null) return;
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Подтверждение")
                .setMessage("Отметить все задачи выполненными?")
                .setPositiveButton("Да", (d, w) -> {
                    try {
                        QuickActionsHelper.markAllCompleted(getContext());
                        loadTasks();
                        android.widget.Toast.makeText(getContext(), "✅ Все задачи отмечены выполненными", android.widget.Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        android.widget.Toast.makeText(getContext(), "❌ Ошибка", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteCompletedTasks() {
        if (getContext() == null) return;
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Подтверждение")
                .setMessage("Удалить все выполненные задачи?")
                .setPositiveButton("Да", (d, w) -> {
                    try {
                        QuickActionsHelper.deleteCompletedTasks(getContext());
                        loadTasks();
                        android.widget.Toast.makeText(getContext(), "✅ Выполненные задачи удалены", android.widget.Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        android.widget.Toast.makeText(getContext(), "❌ Ошибка", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void exportTasks() {
        if (getContext() == null) return;
        try {
            boolean success = ExportHelper.exportTasksToCsv(getContext(), taskList);
            android.widget.Toast.makeText(getContext(),
                    success ? "✅ Задачи экспортированы в папку Downloads" : "❌ Ошибка экспорта",
                    android.widget.Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.widget.Toast.makeText(getContext(), "❌ Ошибка экспорта", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}