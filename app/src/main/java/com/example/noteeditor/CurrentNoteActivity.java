package com.example.noteeditor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CurrentNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_note);
        setResult(RESULT_OK, null);

        String noteID = getIntent().getStringExtra("noteID");
        getSupportActionBar().setTitle(noteID);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
