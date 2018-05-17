package com.example.noteeditor.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.noteeditor.R;
import com.example.noteeditor.adapters.History;
import com.example.noteeditor.adapters.HistoryAdapter;
import com.example.noteeditor.adapters.Message;
import com.example.noteeditor.adapters.MessageAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HistoryFragment extends ListFragment {
    private FirebaseDatabase fDB;
    private ArrayList<History> histories;
    private HistoryAdapter adapter;
    public String noteID;

    public HistoryFragment() {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fDB = FirebaseDatabase.getInstance();
        try {
            updateHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHistory() {
        fDB.getReference("history/" + noteID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(getContext()==null){
                    return;
                }

                histories = new ArrayList<>();
                adapter = new HistoryAdapter(getContext(), histories);
                setListAdapter(adapter);
                HashMap<String, HashMap<String, Object>> stories = null;
                try {
                    stories
                            = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                long n = (long) dataSnapshot.child("numberOfStories").getValue();

                for (long i = n; i >=1; i--){
                    History h = new History();
                    h.uid = (String) dataSnapshot.child(String.valueOf(i)).child("uid").getValue();
                    h.action = (String) dataSnapshot.child(String.valueOf(i)).child("action").getValue();
                    h.date = (String) dataSnapshot.child(String.valueOf(i)).child("date").getValue();
                    h.type = (String) dataSnapshot.child(String.valueOf(i)).child("type").getValue();
                    histories.add(h);
                }

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
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
}
