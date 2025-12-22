

package com.example.studenttasktracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.studenttasktracker.R;
import com.example.studenttasktracker.models.Task;
import com.example.studenttasktracker.utils.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskDialog extends DialogFragment implements SubjectSearchDialog.OnSubjectSelectedListener {

    private EditText etTitle, etDescription;
    private Spinner spinnerPriority;
    private TextView tvSelectedSubject;
    private Button btnSelectSubject, btnDate;
    private DatabaseHelper databaseHelper;
    private OnTaskAddedListener listener;

    private Calendar selectedDate = Calendar.getInstance();
    private String selectedSubject = "";

    public interface OnTaskAddedListener {
        void onTaskAdded();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTaskAddedListener) {
            listener = (OnTaskAddedListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_task_with_search, null);

        databaseHelper = new DatabaseHelper(getContext());
        initViews(view);
        setupPrioritySpinner();
        setupDatePicker();
        updateDateButton();

        builder.setView(view)
                .setTitle("Добавить задачу")
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", (dialog, which) -> dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> saveTask());
        });

        return dialog;
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        spinnerPriority = view.findViewById(R.id.spinnerPriority);
        tvSelectedSubject = view.findViewById(R.id.tvSelectedSubject);
        btnSelectSubject = view.findViewById(R.id.btnSelectSubject);
        btnDate = view.findViewById(R.id.btnDate);

        btnSelectSubject.setOnClickListener(v -> showSubjectSearchDialog());
    }

    private void setupPrioritySpinner() {
        String[] priorityDisplay = {"Высокий", "Средний", "Низкий"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, priorityDisplay);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
    }

    private void setupDatePicker() {
        btnDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showSubjectSearchDialog() {
        SubjectSearchDialog dialog = SubjectSearchDialog.newInstance(selectedSubject, this);
        dialog.show(getParentFragmentManager(), "SubjectSearchDialog");
    }

    @Override
    public void onSubjectSelected(String subject) {
        selectedSubject = subject;
        tvSelectedSubject.setText(subject);
        tvSelectedSubject.setTextColor(getResources().getColor(R.color.text_primary));
    }

    private void showDatePickerDialog() {
        Calendar today = Calendar.getInstance();
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateButton();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis() - 1000);
        datePickerDialog.show();
    }

    private void updateDateButton() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        btnDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Введите название задачи");
            return;
        }

        if (selectedSubject.isEmpty()) {
            tvSelectedSubject.setError("Выберите предмет");
            tvSelectedSubject.setTextColor(getResources().getColor(R.color.priority_high));
            return;
        }

        String priority = getPriorityFromDisplay(spinnerPriority.getSelectedItem().toString());
        String dueDate = btnDate.getText().toString();

        Task task = new Task(title, selectedSubject, priority, dueDate, description);
        databaseHelper.addTask(task);

        if (listener != null) {
            listener.onTaskAdded();
        }

        Toast.makeText(getContext(), "Задача добавлена!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private String getPriorityFromDisplay(String displayPriority) {
        switch (displayPriority) {
            case "Высокий": return "HIGH";
            case "Средний": return "MEDIUM";
            case "Низкий": return "LOW";
            default: return "MEDIUM";
        }
    }
}