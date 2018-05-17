package com.example.noteeditor.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.noteeditor.CurrentNoteActivity;
import com.example.noteeditor.NoteCreatingActivity;
import com.example.noteeditor.R;
import com.example.noteeditor.ResolvedNoteActivity;
import com.example.noteeditor.adapters.Note;
import com.example.noteeditor.adapters.NotesAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class NotesFragment extends ListFragment {
    private FirebaseAuth mAuth;
    private FirebaseDatabase fDB;
    private NotesAdapter adapter;
    private ArrayList<Note> notes;

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        fDB = FirebaseDatabase.getInstance();
        loadNotes();

        FloatingActionButton fab = getView().findViewById(R.id.fabNotes);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NoteCreatingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Note n = (Note) adapter.getItem(position);
        fDB.getReference("notes/" + n.noteID + "/isActive").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isActive = (boolean) dataSnapshot.getValue();
                if (isActive) {
                    Intent intent = new Intent(getActivity(), CurrentNoteActivity.class);
                    intent.putExtra("noteID", n.noteID);
                    startActivity(intent);
                } else {

                    Intent intent = new Intent(getActivity(), ResolvedNoteActivity.class);
                    intent.putExtra("noteID", n.noteID);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void loadNotes() {
        DatabaseReference dbRef = fDB.getReference("members");
        String uid = mAuth.getCurrentUser().getUid();
        Query q = dbRef.orderByChild(uid);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notes = new ArrayList<>();
                adapter = new NotesAdapter(getActivity(), notes);
                setListAdapter(adapter);

                HashMap<String, Object> myNotes = (HashMap<String, Object>) dataSnapshot.getValue();
                if (myNotes == null) {
                    return;
                }

                for (String noteID : myNotes.keySet()) {
                    getNote(noteID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getNote(final String noteID) {
        fDB.getReference("notes/" + noteID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue().toString();
                String description = dataSnapshot.child("description").getValue().toString();
                boolean isActive = (boolean) dataSnapshot.child("isActive").getValue();
                Note note = new Note(noteID, title, description, isActive);
                notes.add(note);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }
}
