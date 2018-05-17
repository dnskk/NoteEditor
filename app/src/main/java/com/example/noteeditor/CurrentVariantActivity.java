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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class CurrentVariantActivity extends AppCompatActivity {
    private TextView titleTV;
    private TextView descriptionTV;
    private TextView linkTV;
    private ImageView photoIV;
    private ImageView likeIV;
    private ImageView dislikeIV;
    private TextView likesTV;
    private TextView dislikesTV;
    private CardView linkCV;
    private FirebaseDatabase fDB;
    private StorageReference mStorageRef;

    private String noteID;
    private String variantID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_variant);

        noteID = getIntent().getStringExtra("noteID");
        variantID = getIntent().getStringExtra("variantID");

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
        initListeners();
        try {
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        titleTV = (TextView) findViewById(R.id.currentVariant_textViewTitle);
        descriptionTV = (TextView) findViewById(R.id.currentVariant_textViewDescription);
        linkTV = (TextView) findViewById(R.id.currentVariant_textViewLink);
        photoIV = (ImageView) findViewById(R.id.currentVariant_imageViewPhoto);
        linkCV = (CardView) findViewById(R.id.cardView2);
        likeIV = (ImageView) findViewById(R.id.currentVariant_imageViewLike);
        dislikeIV = (ImageView) findViewById(R.id.currentVariant_imageViewDislike);
        likesTV = (TextView) findViewById(R.id.currentVariant_textViewLikes);
        dislikesTV = (TextView) findViewById(R.id.currentVariant_textViewDislikes);
    }

    private void initListeners() {
        likeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                fDB.getReference("votes/" + noteID + "/" + variantID + "/" + uid).setValue(true);
            }
        });

        dislikeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                fDB.getReference("votes/" + noteID + "/" + variantID + "/" + uid).setValue(false);
            }
        });

        fDB.getReference("votes/" + noteID + "/" + variantID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Boolean> votes = (HashMap<String, Boolean>) dataSnapshot.getValue();
                if (votes == null) {
                    return;
                }

                int likes = 0;
                int dislikes = 0;

                for (Boolean v : votes.values()) {
                    if (v) {
                        likes++;
                    } else {
                        dislikes++;
                    }
                }

                likesTV.setText(String.valueOf(likes));
                dislikesTV.setText(String.valueOf(dislikes));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadData() {
        fDB.getReference("variants/" + noteID + "/" + variantID).addValueEventListener(new ValueEventListener() {
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
