package com.example.noteeditor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.noteeditor.R;

import java.util.ArrayList;

public class PersonAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<Person> objects;

    public PersonAdapter(Context context, ArrayList<Person> persons) {
        objects = persons;
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
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_person, parent, false);
        }

        Person p = getPerson(position);

        ((TextView) view.findViewById(R.id.textPersonLogin)).setText(p.login);
        ((TextView) view.findViewById(R.id.textPersonEmail)).setText(p.email);

        return view;
    }

    private Person getPerson(int position) {
        return ((Person) getItem(position));
    }
}