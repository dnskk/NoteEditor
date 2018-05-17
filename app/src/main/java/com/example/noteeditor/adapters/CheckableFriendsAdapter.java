package com.example.noteeditor.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.noteeditor.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

        final ImageView photoIV = view.findViewById(R.id.imageView);
        photoIV.setImageResource(R.drawable.icon_user);
        CheckableFriend p = getFriend(position);

        ((TextView) view.findViewById(R.id.textPersonLogin)).setText(p.login);
        ((TextView) view.findViewById(R.id.textPersonEmail)).setText(p.email);
        CheckBox checkBox = view.findViewById(R.id.checkBox);
        checkBox.setChecked(p.isChecked);
        checkBox.setOnCheckedChangeListener(myCheckChangeList);
        checkBox.setTag(position);
        checkBox.setChecked(false);

        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        final File localFile;
        try {
            localFile = File.createTempFile(p.uid, "jpg");

            mStorageRef.child("profile_photo/" + p.uid).getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            photoIV.setImageURI(Uri.fromFile(localFile));
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

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

