package com.example.smartassistant;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TasksFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<TaskModel> taskList;
    TaskAdapter adapter;
    FloatingActionButton addTaskButton;
    LinearLayout emptyStateLayout;
    TextView doneCount;
    TextView pendingCount;
    TextView progressPercent;
    int completedTasks = 0;

    FirebaseFirestore db;
    FirebaseAuth auth;

    public TasksFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        recyclerView = view.findViewById(R.id.tasksRecyclerView);
        addTaskButton = view.findViewById(R.id.addTaskButton);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        doneCount = view.findViewById(R.id.doneCount);
        pendingCount = view.findViewById(R.id.pendingCount);
        progressPercent = view.findViewById(R.id.progressPercent);

        taskList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new TaskAdapter(
                taskList,
                () -> {
                    completedTasks++;
                    updateProgress();
                    updateEmptyState();
                },
                () -> {
                    updateProgress();
                    updateEmptyState();
                });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        updateEmptyState();
        updateProgress();

        loadTasksFromFirestore();

        addTaskButton.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    private void loadTasksFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("tasks")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("completed", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) return;
                    if (!task.isSuccessful()) return;

                    taskList.clear();
                    completedTasks = 0;

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        TaskModel t = new TaskModel(
                                doc.getString("title"),
                                doc.getString("description"),
                                doc.getString("priority"),
                                doc.getString("dueDate"),
                                Boolean.TRUE.equals(doc.getBoolean("completed")));
                        t.setDocumentId(doc.getId());
                        taskList.add(t);
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    updateProgress();
                });
    }

    private void showAddTaskDialog() {
        if (!isAdded() || getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_task, null);

        EditText titleInput = dialogView.findViewById(R.id.inputTaskTitle);
        EditText descriptionInput = dialogView.findViewById(R.id.inputTaskDescription);
        Spinner prioritySpinner = dialogView.findViewById(R.id.prioritySpinner);
        EditText dueDateInput = dialogView.findViewById(R.id.inputDueDate);

        String[] priorities = {"HIGH", "MEDIUM", "LOW"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                priorities);
        prioritySpinner.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(getContext())
                .setTitle("Add Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {

                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Title required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String description = descriptionInput.getText().toString();
                    String priority = prioritySpinner.getSelectedItem().toString();
                    String dueDate = dueDateInput.getText().toString();

                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) return;

                    Map<String, Object> taskData = new HashMap<>();
                    taskData.put("userId", user.getUid());
                    taskData.put("title", title);
                    taskData.put("description", description);
                    taskData.put("priority", priority);
                    taskData.put("dueDate", dueDate);
                    taskData.put("completed", false);
                    taskData.put("createdAt", System.currentTimeMillis());

                    db.collection("tasks")
                            .add(taskData)
                            .addOnSuccessListener(docRef -> {
                                if (!isAdded() || getContext() == null) return;

                                TaskModel newTask = new TaskModel(title, description, priority, dueDate, false);
                                newTask.setDocumentId(docRef.getId());
                                taskList.add(newTask);
                                adapter.notifyItemInserted(taskList.size() - 1);
                                updateEmptyState();
                                updateProgress();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded() || getContext() == null) return;
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        if (!isAdded() || getView() == null) return;

        if (taskList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateProgress() {
        if (!isAdded() || getView() == null) return;

        int pending = taskList.size();
        int total = completedTasks + pending;
        int progress = 0;
        if (total > 0) {
            progress = (completedTasks * 100) / total;
        }

        doneCount.setText(String.valueOf(completedTasks));
        pendingCount.setText(String.valueOf(pending));
        progressPercent.setText(progress + "%");
    }
}
