package com.example.studenttasktracker.activities;

import android.os.Bundle;
import com.example.studenttasktracker.R;
import com.example.studenttasktracker.fragments.OverviewFragment;
import com.example.studenttasktracker.fragments.StatisticsFragment;
import com.example.studenttasktracker.fragments.WeekViewFragment;
import com.example.studenttasktracker.models.Task;
import com.example.studenttasktracker.utils.DatabaseHelper;
import com.example.studenttasktracker.utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();
        checkOverdueTasks();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OverviewFragment())
                    .commit();
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_overview) {
                selectedFragment = new OverviewFragment();
            } else if (itemId == R.id.nav_week) {
                selectedFragment = new WeekViewFragment();
            } else if (itemId == R.id.nav_statistics) {
                selectedFragment = new StatisticsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }

    private void checkOverdueTasks() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Task> overdueTasks = dbHelper.getOverdueTasks();
        List<Task> todayTasks = dbHelper.getTodayTasks();

        if (!overdueTasks.isEmpty()) {
            NotificationHelper.showOverdueNotification(this, overdueTasks);
        }

        if (!todayTasks.isEmpty()) {
            NotificationHelper.showTodayTasksNotification(this, todayTasks);
        }
    }
}