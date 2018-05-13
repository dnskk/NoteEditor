package com.example.noteeditor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.noteeditor.adapters.CheckableFriend;
import com.example.noteeditor.adapters.CheckableFriendsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NoteAddingFriendsActivity extends AppCompatActivity {
    Button addButton;
    ListView friendsLV;
    FirebaseDatabase fDB;
    CheckableFriendsAdapter adapter;
    ArrayList<CheckableFriend> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_adding_friends);
        setResult(RESULT_OK, null);

        final String noteID = getIntent().getStringExtra("noteID");
        String title = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fDB = FirebaseDatabase.getInstance();

        Toast.makeText(NoteAddingFriendsActivity.this,
                "Note created! You can add participants.", Toast.LENGTH_LONG).show();

        addButton = (Button) findViewById(R.id.noteAddingFriends_buttonAdd);
        friendsLV = (ListView) findViewById(R.id.noteAddingFriends_listView);
        friends = new ArrayList<>();
        adapter = new CheckableFriendsAdapter(NoteAddingFriendsActivity.this, friends);
        friendsLV.setAdapter(adapter);
        loadFriends();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAdding(noteID);
            }
        });
    }

    private void finishAdding(String noteID) {
        for (CheckableFriend f : friends) {
            if (f.isChecked) {
                fDB.getReference("members/" + noteID + "/" + f.uid).setValue(true);
            }
        }

        Intent intent = new Intent(NoteAddingFriendsActivity.this, CurrentNoteActivity.class);
        intent.putExtra("noteID", noteID);
        startActivityForResult(intent, Constants.REQUEST_EXIT);
    }

    private void loadFriends() {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fDB.getReference("friends/" + myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> fUids = new HashSet<>();
                Map<String, Object> friends = (Map<String, Object>) dataSnapshot.getValue();
                if (friends != null) {
                    fUids = friends.keySet();
                }
                for (String uid : fUids) {
                    getFriends(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFriends(final String uid) {
        fDB.getReference("users/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String login = (String) dataSnapshot.child("login").getValue();
                String email = (String) dataSnapshot.child("email").getValue();
                CheckableFriend p = new CheckableFriend(login, email);
                p.uid = uid;
                friends.add(p);
                adapter.notifyDataSetChanged();
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
