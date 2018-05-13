package com.example.noteeditor.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.noteeditor.FriendsSearchActivity;
import com.example.noteeditor.NoteCreatingActivity;
import com.example.noteeditor.R;
import com.example.noteeditor.adapters.Note;
import com.example.noteeditor.adapters.NotesAdapter;
import com.example.noteeditor.adapters.Person;
import com.example.noteeditor.adapters.PersonAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class NotesFragment extends ListFragment {
    FirebaseAuth mAuth;
    NotesAdapter adapter;
    ArrayList<Note> notes;
    FirebaseDatabase fDB;

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

    private void loadNotes() {
        DatabaseReference dbRef = fDB.getReference("members");
        String uid = mAuth.getCurrentUser().getUid();
        Query q = dbRef.orderByChild(uid).startAt(true);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notes = new ArrayList<>();
                adapter = new NotesAdapter(getActivity(), notes);
                setListAdapter(adapter);

                HashMap<String, Object> myNotes = (HashMap<String, Object>) dataSnapshot.getValue();
                if(myNotes==null){
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
                boolean isActive = (boolean) dataSnapshot.child("isActive").getValue();
                Note note = new Note(noteID, title, isActive);
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
