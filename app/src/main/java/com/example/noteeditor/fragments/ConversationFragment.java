package com.example.noteeditor.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.noteeditor.LoginActivity;
import com.example.noteeditor.NoteCreatingActivity;
import com.example.noteeditor.R;
import com.example.noteeditor.adapters.Message;
import com.example.noteeditor.adapters.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class ConversationFragment extends ListFragment {
    private FirebaseAuth mAuth;
    private FirebaseDatabase fDB;
    private ArrayList<Message> messages;
    private MessageAdapter adapter;
    private EditText editText;
    private ImageButton sendButton;
    public String noteID;
    private String myUid;

    public ConversationFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        myUid = mAuth.getCurrentUser().getUid();
        fDB = FirebaseDatabase.getInstance();

        editText = getView().findViewById(R.id.edittext_chatbox);
        sendButton = getView().findViewById(R.id.button_chatbox_send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String messageToSend = editText.getText().toString();
                if (messageToSend.isEmpty()) {
                    return;
                }

                final DatabaseReference dbRef = fDB.getReference("conversation/" + noteID + "/numberOfMessages");
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int n = Integer.parseInt(dataSnapshot.getValue().toString());
                        n++;
                        fDB.getReference("conversation/" + noteID + "/" + n)
                                .setValue(myUid + "_msg_" + messageToSend);
                        dbRef.setValue(n);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                editText.setText("");
            }
        });

        loadMessages();
    }

    private void loadMessages() {

        fDB.getReference("conversation/" + noteID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getActivity() == null) {
                    return;
                }

                messages = new ArrayList<>();
                adapter = new MessageAdapter(getContext(), messages);
                setListAdapter(adapter);

                HashMap<String, Object> msgs
                        = (HashMap<String, Object>) dataSnapshot.getValue();

                if (msgs == null) {
                    return;
                }

                long n = (long) msgs.get("numberOfMessages");

                for (long i = 1; i <= n; i++) {
                    String[] line = ((String) msgs.get(String.valueOf(i))).split("_msg_");
                    Message currMessage = new Message(line[0], line[1], line[0].equals(myUid));
                    messages.add(currMessage);
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
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

}
