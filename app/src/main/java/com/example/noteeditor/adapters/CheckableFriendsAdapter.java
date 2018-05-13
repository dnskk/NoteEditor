package com.example.noteeditor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.noteeditor.R;

import java.util.ArrayList;

/**
 * Created by Динар on 12.05.2018.
 */

public class CheckableFriendsAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<CheckableFriend> objects;

    public CheckableFriendsAdapter(Context context, ArrayList<CheckableFriend> friends) {
        objects = friends;
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
            view = lInflater.inflate(R.layout.list_item_checkable_friend, parent, false);
        }

        CheckableFriend p = getFriend(position);

        ((TextView) view.findViewById(R.id.textPersonLogin)).setText(p.login);
        ((TextView) view.findViewById(R.id.textPersonEmail)).setText(p.email);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        checkBox.setChecked(p.isChecked);
        checkBox.setOnCheckedChangeListener(myCheckChangeList);
        checkBox.setTag(position);
        checkBox.setChecked(false);

        return view;
    }

    // обработчик для чекбоксов
    private CompoundButton.OnCheckedChangeListener myCheckChangeList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            getFriend((Integer) buttonView.getTag()).isChecked = isChecked;
        }
    };

    private CheckableFriend getFriend(int position) {
        return ((CheckableFriend) getItem(position));
    }
}

