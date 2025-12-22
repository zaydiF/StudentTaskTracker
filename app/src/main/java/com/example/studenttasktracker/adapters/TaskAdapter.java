package com.example.studenttasktracker.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studenttasktracker.R;
import com.example.studenttasktracker.models.Task;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onCompletedChange(int position, boolean isCompleted);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> tasks) {
        // Используем DiffUtil для безопасного обновления
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    public Task getTaskAtPosition(int position) {
        return taskList.get(position);
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvTitle, tvSubject, tvDueDate, tvPriority;
        private CheckBox cbCompleted;
        private MaterialButton btnEdit, btnDelete;
        private View priorityIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);

            // Убираем слушатель из конструктора, чтобы избежать рекурсии
            cbCompleted.setOnCheckedChangeListener(null);

            btnEdit.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getAdapterPosition());
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(getAdapterPosition());
                }
            });

            // Добавляем слушатель после инициализации
            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCompletedChange(getAdapterPosition(), isChecked);
                }
            });
        }

        public void bind(Task task) {
            tvTitle.setText(task.getTitle());
            tvSubject.setText(task.getSubject());
            tvDueDate.setText(task.getDueDate());

            String priorityText = "";
            int priorityColor = Color.GRAY;

            switch (task.getPriority()) {
                case "HIGH":
                    priorityText = "Высокий";
                    priorityColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_high);
                    break;
                case "MEDIUM":
                    priorityText = "Средний";
                    priorityColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_medium);
                    break;
                case "LOW":
                    priorityText = "Низкий";
                    priorityColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_low);
                    break;
            }

            tvPriority.setText(priorityText);
            priorityIndicator.setBackgroundColor(priorityColor);

            // Временно отключаем слушатель чтобы избежать рекурсии
            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(task.isCompleted());
            // Включаем обратно
            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCompletedChange(getAdapterPosition(), isChecked);
                }
            });

            if (task.isCompleted()) {
                tvTitle.setAlpha(0.5f);
                tvSubject.setAlpha(0.5f);
                tvDueDate.setAlpha(0.5f);
                tvPriority.setAlpha(0.5f);
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.chart_color_1));
            } else {
                tvTitle.setAlpha(1f);
                tvSubject.setAlpha(1f);
                tvDueDate.setAlpha(1f);
                tvPriority.setAlpha(1f);
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.surface));
            }
        }
    }
}