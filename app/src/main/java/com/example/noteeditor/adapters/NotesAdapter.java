package com.example.noteeditor.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.noteeditor.R;

import java.util.ArrayList;


public class NotesAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<Note> objects;

    public NotesAdapter(Context context, ArrayList<Note> notes) {
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
            view = lInflater.inflate(R.layout.list_item_note, parent, false);
        }

        Note n = getNote(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.textNoteTitle)).setText(n.name);
        ((TextView) view.findViewById(R.id.textDescription)).setText(n.description);
        TextView tv = view.findViewById(R.id.textNoteCondition);
        if (n.isActive) {
            tv.setText("Active");
            tv.setTextColor(Color.parseColor("#0277bd"));
        } else{
            tv.setText("Closed");
            tv.setTextColor(Color.parseColor("#BDBDBD"));
        }

        return view;
    }

    Note getNote(int position) {
        return ((Note) getItem(position));
    }
}
