package com.example.studenttasktracker.models;

import java.io.Serializable;

public class CustomSubject implements Serializable {
    private int id;
    private String name;
    private boolean isCustom; // true - пользовательский, false - системный
    private boolean isDeleted; // помечен как удаленный
    private String createdAt;

    public CustomSubject() {}

    public CustomSubject(String name, boolean isCustom) {
        this.name = name;
        this.isCustom = isCustom;
        this.isDeleted = false;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}