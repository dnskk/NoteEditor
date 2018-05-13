package com.example.noteeditor.adapters;


public class Note {
    public String name;
    public boolean isActive;
    public String noteID;

    public Note(String noteID, String name, boolean isActive) {
        this.noteID = noteID;
        this.name = name;
        this.isActive = isActive;
    }
}
