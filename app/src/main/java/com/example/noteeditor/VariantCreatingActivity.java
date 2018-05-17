package com.example.noteeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.noteeditor.adapters.Variant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;


public class VariantCreatingActivity extends AppCompatActivity {
    EditText titleET;
    EditText linkET;
    EditText descriptionET;
    Button completeButton;
    ImageView picture;
    private String noteID;
    private StorageReference mStorageRef;
    private Bitmap selectedImage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variant_creating);
        getSupportActionBar().setTitle("New option");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        noteID = getIntent().getStringExtra("noteID");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        imageUri = null;
        initViews();
        initListeners();
    }

    private void initViews() {
        titleET = (EditText) findViewById(R.id.variantCreating_editTextTitle);
        linkET = (EditText) findViewById(R.id.variantCreating_editTextLink);
        descriptionET = (EditText) findViewById(R.id.variantCreating_editTextDescription);
        completeButton = (Button) findViewById(R.id.variantCreating_buttonComplete);
        picture = (ImageView) findViewById(R.id.variantCreating_imageView);
    }

    private void initListeners() {
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleET.getText().toString();
                String link = linkET.getText().toString();
                String description = descriptionET.getText().toString();

                if (title.equals("")) {
                    Toast.makeText(VariantCreatingActivity.this, "Set the title", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.equals("")) {
                    Toast.makeText(VariantCreatingActivity.this, "Set the description", Toast.LENGTH_SHORT).show();
                    return;
                }

                createVariant(title, link, description);
            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, Constants.REQUEST_EXIT);
            }
        });
    }

    private void createVariant(final String title, final String link, final String description) {
        final FirebaseDatabase fDB = FirebaseDatabase.getInstance();
        final DatabaseReference dbRef = fDB.getReference("variants/" + noteID + "/numberOfVariants");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int n = Integer.parseInt(dataSnapshot.getValue().toString());
                n++;
                dbRef.setValue(n);

                fDB.getReference("variants/" + noteID + "/v" + n + "/title").setValue(title);
                fDB.getReference("variants/" + noteID + "/v" + n + "/description").setValue(description);
                if (!link.equals("")) {
                    fDB.getReference("variants/" + noteID + "/v" + n + "/link").setValue(link);
                }

                updateHistory(title);


                loadImage(n);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateHistory(final String title) {
        final FirebaseDatabase fDB = FirebaseDatabase.getInstance();
        fDB.getReference("history/" + noteID + "/numberOfStories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long n = (long) dataSnapshot.getValue();
                        n++;
                        fDB.getReference("history/" + noteID + "/numberOfStories").setValue(n);
                        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        fDB.getReference("history/" + noteID + "/" + n).child("uid").setValue(myUid);
                        fDB.getReference("history/" + noteID + "/" + n).child("action")
                                .setValue(" created new option \"" + title + "\"");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                        String currentDateandTime = sdf.format(new Date());
                        fDB.getReference("history/" + noteID + "/" + n).child("date").setValue(currentDateandTime);
                        fDB.getReference("history/" + noteID + "/" + n).child("type").setValue("New option");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadImage(int n) {
        if (imageUri == null) {
            VariantCreatingActivity.this.finish();
            return;
        }

        Bitmap bitmap = getResizedBitmap(selectedImage, 1000);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] arr = baos.toByteArray();

        mStorageRef.child("variant_photo/" + noteID + "/v" + n).putBytes(arr)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(VariantCreatingActivity.this, "New option created", Toast.LENGTH_LONG).show();
                        VariantCreatingActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(VariantCreatingActivity.this, "New option created", Toast.LENGTH_LONG).show();
                        VariantCreatingActivity.this.finish();
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

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                picture.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(VariantCreatingActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(VariantCreatingActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
