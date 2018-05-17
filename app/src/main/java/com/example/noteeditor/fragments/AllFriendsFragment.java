package com.example.noteeditor.fragments;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.noteeditor.R;
import com.example.noteeditor.adapters.Person;
import com.example.noteeditor.adapters.PersonAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;


public class AllFriendsFragment extends ListFragment {
    PersonAdapter adapter;
    ArrayList<Person> friends;

    public AllFriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadFriends();
    }

    private void loadFriends() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference friendsRef = database.getReference("friends/" + user.getUid());
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends = new ArrayList<>();
                adapter = new PersonAdapter(getActivity(), friends);
                setListAdapter(adapter);

                Map<String, Object> friends = (Map<String, Object>) dataSnapshot.getValue();
                if (friends == null) {
                    return;
                }

                for (String uid : friends.keySet()) {
                    getFriend(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFriend(final String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String login = (String) dataSnapshot.child("login").getValue();
                String email = (String) dataSnapshot.child("email").getValue();
                Person p = new Person(login, email);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_friends, container, false);
    }
}
