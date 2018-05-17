package com.example.noteeditor.adapters;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.noteeditor.R;
import com.example.noteeditor.TabChange;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RequestPersonAdapter extends BaseAdapter {
    public TabChange delegate;
    private FirebaseAuth mAuth;
    private FirebaseDatabase fDB;
    private LayoutInflater lInflater;
    private ArrayList<Person> objects;

    public RequestPersonAdapter(Context context, ArrayList<Person> persons, TabChange delegate) {
        this.delegate=delegate;
        this.delegate.resetRequests();
        objects = persons;
        mAuth = FirebaseAuth.getInstance();
        fDB = FirebaseDatabase.getInstance();

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_request_person, parent, false);
        }

        final ImageView photoIV = view.findViewById(R.id.imageView);
        photoIV.setImageResource(R.drawable.icon_user);
        final Person p = getPerson(position);

        ((TextView) view.findViewById(R.id.textPersonLogin)).setText(p.login);
        ((TextView) view.findViewById(R.id.textPersonEmail)).setText(p.email);
        ImageView imageViewAccept = view.findViewById(R.id.imageViewAccept);
        ImageView imageViewDecline = view.findViewById(R.id.imageViewDecline);

        imageViewAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(p.uid, position);
                delegate.decrementRequests();
            }
        });

        imageViewDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineRequest(p.uid, position);
                delegate.decrementRequests();
            }
        });

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

    private void addFriend(String uid, int position) {
        String myUid = mAuth.getCurrentUser().getUid();
        fDB.getReference("requests").child(myUid).child(uid).removeValue();
        fDB.getReference("friends").child(myUid).child(uid).setValue(true);
        fDB.getReference("friends").child(uid).child(myUid).setValue(true);

//        objects.remove(position);
//        this.notifyDataSetChanged();
    }

    private void declineRequest(String uid, int position) {
        String myUid = mAuth.getCurrentUser().getUid();
        fDB.getReference("requests").child(myUid).child(uid).removeValue();

//        objects.remove(position);
//        this.notifyDataSetChanged();
    }

    private Person getPerson(int position) {
        return ((Person) getItem(position));
    }
}
