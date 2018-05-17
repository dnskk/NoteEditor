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


public class HistoryAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<History> objects;

    public HistoryAdapter(Context context, ArrayList<History> notes) {
        objects = notes;
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
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_history, parent, false);
        }

        History h = getHistory(position);

        ((TextView) view.findViewById(R.id.textViewType)).setText(h.type);
        ((TextView) view.findViewById(R.id.textViewAction)).setText(h.action);
        ((TextView) view.findViewById(R.id.textViewDate)).setText(h.date);

        final TextView tv = view.findViewById(R.id.textViewLogin);
        FirebaseDatabase.getInstance().getReference("users/" + h.uid + "/login")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tv.setText((String) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        return view;
    }

    History getHistory(int position) {
        return ((History) getItem(position));
    }
}
