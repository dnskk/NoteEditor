package com.example.noteeditor.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.noteeditor.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<Message> objects;

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        objects = messages;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;

        final Message m = getMessage(position);

        // if message is mine then align to right
        if (!m.isMine) {
            view = lInflater.inflate(R.layout.list_item_message_received, parent, false);

            TextView msg = view.findViewById(R.id.message_text);
            msg.setText(m.message);
            final TextView nick = view.findViewById(R.id.nick);
            FirebaseDatabase.getInstance().getReference("users/" + m.uid + "/login")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    nick.setText(dataSnapshot.getValue().toString() + ":");

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            msg.setTextColor(Color.BLACK);
            return view;
        }

        view = lInflater.inflate(R.layout.list_item_message_sended, parent, false);

        TextView msg = view.findViewById(R.id.message_text_sended);
        msg.setText(m.message);
        msg.setTextColor(Color.BLACK);

        return view;
    }

    private Message getMessage(int position) {
        return ((Message) getItem(position));
    }
}
