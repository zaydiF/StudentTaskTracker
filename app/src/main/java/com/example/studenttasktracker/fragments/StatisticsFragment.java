package com.example.studenttasktracker.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studenttasktracker.R;
import com.example.studenttasktracker.models.Task;
import com.example.studenttasktracker.utils.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class StatisticsFragment extends Fragment {

    private DatabaseHelper databaseHelper;
    private PieChart pieChartPriority, pieChartSubjects;
    private TextView tvTotalTasks, tvCompletedTasks, tvCompletionRate, tvOverdueTasks, tvUpcomingTasks;
    private ProgressBar progressCompletion;
    private MaterialCardView cardOverdue, cardUpcoming;

    // Карта для хранения цветов предметов (для сохранения одинаковых цветов между обновлениями)
    private HashMap<String, Integer> subjectColors = new HashMap<>();

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = new DatabaseHelper(getContext());
        initViews(view);
        loadStatistics();
        setupCharts();
    }

    private void initViews(View view) {
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvCompletionRate = view.findViewById(R.id.tvCompletionRate);
        tvOverdueTasks = view.findViewById(R.id.tvOverdueTasks);
        tvUpcomingTasks = view.findViewById(R.id.tvUpcomingTasks);
        progressCompletion = view.findViewById(R.id.progressCompletion);
        pieChartPriority = view.findViewById(R.id.pieChartPriority);
        pieChartSubjects = view.findViewById(R.id.pieChartSubjects);
        cardOverdue = view.findViewById(R.id.cardOverdue);
        cardUpcoming = view.findViewById(R.id.cardUpcoming);
    }

    private void loadStatistics() {
        int total = databaseHelper.getTotalTasks();
        int completed = databaseHelper.getCompletedTasks();
        int completionRate = total > 0 ? (completed * 100) / total : 0;

        tvTotalTasks.setText(String.valueOf(total));
        tvCompletedTasks.setText(String.valueOf(completed));
        tvCompletionRate.setText(completionRate + "%");
        progressCompletion.setProgress(completionRate);

        // Просроченные задачи
        List<Task> overdueTasks = databaseHelper.getOverdueTasks();
        tvOverdueTasks.setText(String.valueOf(overdueTasks.size()));
        if (overdueTasks.size() > 0) {
            cardOverdue.setCardBackgroundColor(getResources().getColor(R.color.priority_high));
        }

        // Ближайшие задачи
        List<Task> upcomingTasks = databaseHelper.getUpcomingTasks();
        tvUpcomingTasks.setText(String.valueOf(upcomingTasks.size()));
        if (upcomingTasks.size() > 0) {
            cardUpcoming.setCardBackgroundColor(getResources().getColor(R.color.priority_medium));
        }
    }

    private void setupCharts() {
        setupPriorityChart();
        setupSubjectsChart();
    }

    private void setupPriorityChart() {
        List<PieEntry> entries = new ArrayList<>();

        int highPriority = databaseHelper.getTasksCountByPriority("HIGH");
        int mediumPriority = databaseHelper.getTasksCountByPriority("MEDIUM");
        int lowPriority = databaseHelper.getTasksCountByPriority("LOW");

        if (highPriority > 0) entries.add(new PieEntry(highPriority, "Высокий"));
        if (mediumPriority > 0) entries.add(new PieEntry(mediumPriority, "Средний"));
        if (lowPriority > 0) entries.add(new PieEntry(lowPriority, "Низкий"));

        if (entries.isEmpty()) {
            // Если нет данных, показываем заглушку
            entries.add(new PieEntry(1f, "Нет данных"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        if (entries.size() == 1 && entries.get(0).getLabel().equals("Нет данных")) {
            dataSet.setColors(new int[]{Color.GRAY});
        } else {
            dataSet.setColors(new int[]{
                    getResources().getColor(R.color.priority_high),
                    getResources().getColor(R.color.priority_medium),
                    getResources().getColor(R.color.priority_low)
            });
        }
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        pieChartPriority.setData(data);
        pieChartPriority.getDescription().setEnabled(false);
        pieChartPriority.setCenterText("Приоритеты");
        pieChartPriority.setCenterTextSize(14f);
        pieChartPriority.setHoleRadius(45f);
        pieChartPriority.setTransparentCircleRadius(50f);
        pieChartPriority.setDrawEntryLabels(false);

        Legend legend = pieChartPriority.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);

        pieChartPriority.invalidate();
    }

    private void setupSubjectsChart() {
        List<PieEntry> entries = new ArrayList<>();

        // Получаем все уникальные предметы из базы данных (исключаем "Предметы")
        List<String> subjectsList = databaseHelper.getAllSubjects();

        boolean hasData = false;

        // Готовим данные для графика
        for (String subject : subjectsList) {
            if (subject.equals("Предметы")) {
                continue; // Пропускаем заголовок
            }

            int count = databaseHelper.getTasksCountBySubject(subject);
            if (count > 0) {
                entries.add(new PieEntry(count, subject));
                hasData = true;

                // Если у предмета еще нет цвета, генерируем его
                if (!subjectColors.containsKey(subject)) {
                    subjectColors.put(subject, generateColorForSubject(subject));
                }
            }
        }

        if (!hasData) {
            // Если нет данных, показываем заглушку
            entries.add(new PieEntry(1f, "Нет данных"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        if (entries.size() == 1 && entries.get(0).getLabel().equals("Нет данных")) {
            dataSet.setColors(new int[]{Color.GRAY});
        } else {
            // Создаем массив цветов для каждого элемента
            int[] colors = new int[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                String subject = entries.get(i).getLabel();
                colors[i] = subjectColors.get(subject);
            }
            dataSet.setColors(colors);
        }

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        pieChartSubjects.setData(data);
        pieChartSubjects.getDescription().setEnabled(false);
        pieChartSubjects.setCenterText("Предметы");
        pieChartSubjects.setCenterTextSize(14f);
        pieChartSubjects.setHoleRadius(45f);
        pieChartSubjects.setTransparentCircleRadius(50f);
        pieChartSubjects.setDrawEntryLabels(false);

        Legend legend = pieChartSubjects.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(10f);
        legend.setMaxSizePercent(0.8f); // Ограничиваем размер легенды
        legend.setYOffset(15f); // Отступ снизу

        pieChartSubjects.invalidate();
    }

    /**
     * Генерирует цвет для предмета на основе его названия
     * Один и тот же предмет всегда будет получать один и тот же цвет
     */
    private int generateColorForSubject(String subject) {
        // Используем хэш названия предмета как seed для генерации
        long seed = subject.hashCode();
        Random random = new Random(seed);

        // Генерация цвета в HSL пространстве для более приятных цветов
        float hue = random.nextFloat() * 360f; // 0-360 градусов цветового круга
        float saturation = 0.5f + random.nextFloat() * 0.3f; // 0.5-0.8
        float lightness = 0.5f + random.nextFloat() * 0.2f; // 0.5-0.7

        // Конвертация HSL в RGB через HSV (Android использует HSV)
        float[] hsv = {hue, saturation, lightness};
        return Color.HSVToColor(hsv);
    }

    /**
     * Альтернативный метод генерации цвета через RGB
     * (более яркие и насыщенные цвета)
     */
    private int generateColorForSubjectRGB(String subject) {
        // Используем хэш названия предмета как seed для генерации
        long seed = subject.hashCode();
        Random random = new Random(seed);

        // Генерация компонентов RGB с ограничениями для избежания слишком темных/светлых цветов
        int r = 100 + random.nextInt(156); // 100-255
        int g = 100 + random.nextInt(156); // 100-255
        int b = 100 + random.nextInt(156); // 100-255

        return Color.rgb(r, g, b);
    }

    /**
     * Метод для получения более пастельных цветов
     */
    private int generatePastelColorForSubject(String subject) {
        long seed = subject.hashCode();
        Random random = new Random(seed);

        // Пастельные цвета имеют высокую яркость и низкую насыщенность
        float hue = random.nextFloat() * 360f;
        float saturation = 0.3f + random.nextFloat() * 0.2f; // 0.3-0.5
        float lightness = 0.7f + random.nextFloat() * 0.2f; // 0.7-0.9

        float[] hsv = {hue, saturation, lightness};
        return Color.HSVToColor(hsv);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
        setupCharts();
    }
}