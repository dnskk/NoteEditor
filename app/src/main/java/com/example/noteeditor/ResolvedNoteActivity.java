package com.example.noteeditor;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ResolvedNoteActivity extends AppCompatActivity {
    private TextView titleTV;
    private TextView descriptionTV;
    private TextView linkTV;
    private ImageView photoIV;
    private String noteID;
    private CardView linkCV;
    private FirebaseDatabase fDB;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolved_note);

        noteID = getIntent().getStringExtra("noteID");

        fDB = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        fDB.getReference("notes/" + noteID + "/title").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setTitle((String) dataSnapshot.getValue());
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        initViews();

        try {
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        titleTV = (TextView) findViewById(R.id.resolvedNote_textViewTitle);
        descriptionTV = (TextView) findViewById(R.id.resolvedNote_textViewDescription);
        linkTV = (TextView) findViewById(R.id.resolvedNote_textViewLink);
        photoIV = (ImageView) findViewById(R.id.resolvedNote_imageViewPhoto);
        linkCV = (CardView) findViewById(R.id.cardView2);
    }

    private void loadData() {
        fDB.getReference("resolved/" + noteID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = (String) dataSnapshot.child("title").getValue();
                String description = (String) dataSnapshot.child("description").getValue();
                String link = (String) dataSnapshot.child("link").getValue();
                titleTV.setText(title);
                descriptionTV.setText(description);
                if (link == null) {
                    linkTV.setVisibility(View.INVISIBLE);
                    linkCV.setVisibility(View.GONE);
                } else {
                    linkTV.setText(link);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        fDB.getReference("resolved/" + noteID + "/variantID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String variantID = (String)dataSnapshot.getValue();
                        try {
                            final File localFile = File.createTempFile(noteID + "_" + variantID, "jpg");

                            mStorageRef.child("variant_photo/" + noteID + "/" + variantID).getFile(localFile)
                                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            photoIV.setImageURI(Uri.fromFile(localFile));
                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
