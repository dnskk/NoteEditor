package com.example.noteeditor.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.noteeditor.MainActivity;
import com.example.noteeditor.R;
import com.example.noteeditor.TabChange;
import com.example.noteeditor.adapters.Person;
import com.example.noteeditor.adapters.PersonAdapter;
import com.example.noteeditor.adapters.RequestPersonAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;


public class RequestsFragment extends ListFragment {
    TabChange delegate;
    private FirebaseAuth mAuth;
    private RequestPersonAdapter adapter;
    private ArrayList<Person> persons;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        loadRequests();
    }

    private void loadRequests() {
        FirebaseUser user = mAuth.getCurrentUser();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference friendsRef = database.getReference("requests/" + user.getUid());
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                persons = new ArrayList<>();
                adapter = new RequestPersonAdapter(getActivity(), persons, delegate);
                setListAdapter(adapter);

                Map<String, Object> friends = (Map<String, Object>) dataSnapshot.getValue();
                if(friends==null){
                    return;
                }

                for (String uid : friends.keySet()) {
                    getRequest(uid);
                    adapter.delegate.incrementRequests();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRequest(final String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String login = (String) dataSnapshot.child("login").getValue();
                String email = (String) dataSnapshot.child("email").getValue();
                Person p = new Person(login, email);
                p.uid = uid;
                persons.add(p);
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
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }
}
