package com.example.smartassistant;

public class NoteModel {

    String documentId;
    String title;
    String content;
    long createdAt;
    String type;

    public NoteModel() {}

    public NoteModel(String title, String content, long createdAt) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    // GETTERS
    public String getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getCreatedAt() { return createdAt; }
    public String getType() { return type; }

    // SETTERS
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setType(String type) { this.type = type; }
}
