package com.example.smartassistant;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskAdapter
        extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    ArrayList<TaskModel> taskList;
    OnTaskCompletedListener listener;
    OnTaskDeletedListener deleteListener;

    public interface OnTaskCompletedListener {
        void onTaskCompleted();
    }

    public interface OnTaskDeletedListener {
        void onTaskDeleted();
    }

    public TaskAdapter(
            ArrayList<TaskModel> taskList,
            OnTaskCompletedListener listener,
            OnTaskDeletedListener deleteListener) {
        this.taskList = taskList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TaskViewHolder holder,
            int position) {

        TaskModel task = taskList.get(position);

        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.priority.setText(task.getPriority());
        holder.dueDate.setText("Due: " + task.getDueDate());

        // COMPLETE BUTTON
        holder.completeButton.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;

            TaskModel t = taskList.get(pos);

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Complete Task")
                    .setMessage("Mark this task as completed?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        String docId = t.getDocumentId();

                        if (docId != null && !docId.isEmpty()) {
                            FirebaseFirestore.getInstance()
                                    .collection("tasks")
                                    .document(docId)
                                    .update("completed", true)
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(
                                                holder.itemView.getContext(),
                                                "Sync error: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }

                        taskList.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, taskList.size());
                        listener.onTaskCompleted();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // LONG PRESS TO EDIT
        holder.itemView.setOnLongClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return true;

            TaskModel t = taskList.get(pos);

            View dialogView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.dialog_add_task, null);

            EditText titleInput = dialogView.findViewById(R.id.inputTaskTitle);
            EditText descriptionInput = dialogView.findViewById(R.id.inputTaskDescription);
            Spinner prioritySpinner = dialogView.findViewById(R.id.prioritySpinner);
            EditText dueDateInput = dialogView.findViewById(R.id.inputDueDate);

            titleInput.setText(t.getTitle());
            descriptionInput.setText(t.getDescription());
            dueDateInput.setText(t.getDueDate());

            String[] priorities = {"HIGH", "MEDIUM", "LOW"};
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    holder.itemView.getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    priorities);
            prioritySpinner.setAdapter(spinnerAdapter);

            if ("HIGH".equals(t.getPriority())) prioritySpinner.setSelection(0);
            else if ("MEDIUM".equals(t.getPriority())) prioritySpinner.setSelection(1);
            else prioritySpinner.setSelection(2);

            // Show edit/delete options
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Task Options")
                    .setItems(new String[]{"Edit Task", "Delete Task"}, (dialog, which) -> {

                        if (which == 0) {
                            // EDIT
                            new AlertDialog.Builder(holder.itemView.getContext())
                                    .setTitle("Edit Task")
                                    .setView(dialogView)
                                    .setPositiveButton("Save", (d, w) -> {

                                        String newTitle = titleInput.getText().toString();
                                        String newDesc = descriptionInput.getText().toString();
                                        String newPriority = prioritySpinner.getSelectedItem().toString();
                                        String newDueDate = dueDateInput.getText().toString();

                                        t.setTitle(newTitle);
                                        t.setDescription(newDesc);
                                        t.setPriority(newPriority);
                                        t.setDueDate(newDueDate);
                                        notifyItemChanged(pos);

                                        String docId = t.getDocumentId();
                                        if (docId != null && !docId.isEmpty()) {
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("title", newTitle);
                                            updates.put("description", newDesc);
                                            updates.put("priority", newPriority);
                                            updates.put("dueDate", newDueDate);

                                            FirebaseFirestore.getInstance()
                                                    .collection("tasks")
                                                    .document(docId)
                                                    .update(updates)
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(
                                                                holder.itemView.getContext(),
                                                                "Sync error: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();

                        } else {
                            // DELETE
                            new AlertDialog.Builder(holder.itemView.getContext())
                                    .setTitle("Delete Task")
                                    .setMessage("Are you sure you want to delete this task?")
                                    .setPositiveButton("Delete", (d, w) -> {

                                        String docId = t.getDocumentId();
                                        if (docId != null && !docId.isEmpty()) {
                                            FirebaseFirestore.getInstance()
                                                    .collection("tasks")
                                                    .document(docId)
                                                    .delete()
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(
                                                                holder.itemView.getContext(),
                                                                "Delete error: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    });
                                        }

                                        taskList.remove(pos);
                                        notifyItemRemoved(pos);
                                        notifyItemRangeChanged(pos, taskList.size());
                                        if (deleteListener != null) deleteListener.onTaskDeleted();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        }
                    })
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        TextView priority;
        TextView dueDate;
        MaterialButton completeButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            description = itemView.findViewById(R.id.taskDescription);
            priority = itemView.findViewById(R.id.taskPriority);
            dueDate = itemView.findViewById(R.id.taskDueDate);
            completeButton = itemView.findViewById(R.id.completeButton);
        }
    }
}
