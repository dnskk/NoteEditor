package com.example.noteeditor.adapters;


public class Note {
    public String name;
    public boolean isActive;
    public String noteID;
    public String description;

    public Note(String noteID, String name, String description, boolean isActive) {
        this.noteID = noteID;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
    }
}
