package com.example.smartassistant;

public class TaskModel {

    String documentId;
    String title;
    String description;
    String priority;
    String dueDate;
    boolean completed;

    public TaskModel() {}

    public TaskModel(
            String title,
            String description,
            String priority,
            String dueDate,
            boolean completed) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    // GETTERS
    public String getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getDueDate() { return dueDate; }
    public boolean isCompleted() { return completed; }

    // SETTERS
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
