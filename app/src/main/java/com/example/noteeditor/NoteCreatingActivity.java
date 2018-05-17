package com.example.noteeditor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class NoteCreatingActivity extends AppCompatActivity {
    private FirebaseDatabase fDB;
    private EditText titleET;
    private EditText descriptionET;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_creating);
        getSupportActionBar().setTitle("New note");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fDB = FirebaseDatabase.getInstance();
        titleET = (EditText) findViewById(R.id.noteCreating_editTextTitle);
        descriptionET = (EditText) findViewById(R.id.noteCreating_editTextDescription);
        nextButton = (Button) findViewById(R.id.noteCreating_buttonNext);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleET.getText().toString();
                String description = descriptionET.getText().toString();

                if (title.equals("")) {
                    Toast.makeText(NoteCreatingActivity.this, "Title can not be empty!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (description.equals("")) {
                    Toast.makeText(NoteCreatingActivity.this, "Description can not be empty!", Toast.LENGTH_LONG).show();
                    return;
                }

                String[] lines = description.split("\r\n|\r|\n");
                if (lines.length > 2) {
                    Toast.makeText(NoteCreatingActivity.this, "Description should not exceed 2 lines!", Toast.LENGTH_LONG).show();
                    return;
                }

                createNote(title, description);
            }
        });
    }

    private void createNote(final String title, final String description) {
        final DatabaseReference dbRef = fDB.getReference("notes/numberOfNotes");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int n = Integer.parseInt(dataSnapshot.getValue().toString());
                n++;
                dbRef.setValue(n);
                fDB.getReference("notes/n" + n + "/title").setValue(title);
                fDB.getReference("notes/n" + n + "/description").setValue(description);
                fDB.getReference("notes/n" + n + "/isActive").setValue(true);
                fDB.getReference("variants/n" + n + "/numberOfVariants").setValue(0);
                fDB.getReference("conversation/n" + n + "/numberOfMessages").setValue(0);
                fDB.getReference("history/n" + n + "/numberOfStories").setValue(0);

                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                fDB.getReference("members/n" + n + "/" + myUid).setValue(true);

                // Go to adding members.
                Intent intent = new Intent(NoteCreatingActivity.this, NoteAddingFriendsActivity.class);
                intent.putExtra("noteID", "n" + n);
                intent.putExtra("title", title);
                startActivityForResult(intent, Constants.REQUEST_EXIT);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_EXIT) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }
}
