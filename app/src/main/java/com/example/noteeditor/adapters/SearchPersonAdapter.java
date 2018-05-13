package com.example.noteeditor.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noteeditor.FriendsSearchActivity;
import com.example.noteeditor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class SearchPersonAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<Person> objects;
    private Context context;

    public SearchPersonAdapter(Context context, ArrayList<Person> persons) {
        objects = persons;
        this.context = context;
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
            view = lInflater.inflate(R.layout.list_item_search_person, parent, false);
        }

        final Person p = getPerson(position);

        ((TextView) view.findViewById(R.id.textPersonLogin)).setText(p.login);
        ((TextView) view.findViewById(R.id.textPersonEmail)).setText(p.email);
        ImageView imageViewAdd = view.findViewById(R.id.imageViewAdd);

        imageViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
                builder.setTitle("Adding");
                builder.setMessage("Do you want to add " + p.login + " to your friends list?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendFriendRequest(p.uid);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.create().show();
            }
        });

        return view;
    }

    private void sendFriendRequest(String uid) {
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("requests");
        String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendsRef.child(uid).child(myID).setValue(true);
        Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show();
    }

    private Person getPerson(int position) {
        return ((Person) getItem(position));
    }
}