package com.example.studenttasktracker.models;

public class Subject {
    private int id;
    private String name;
    private String color;

    public Subject() {}

    public Subject(String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}