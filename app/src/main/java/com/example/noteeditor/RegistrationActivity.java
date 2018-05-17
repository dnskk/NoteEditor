package com.example.noteeditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.internal.zzbjp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;


public class RegistrationActivity extends AppCompatActivity {
    private EditText mailET;
    private EditText loginET;
    private TextInputEditText passwordET;
    private Button completeButton;
    private ImageView picture;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorageRef;
    private Bitmap selectedImage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setTitle(R.string.registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        imageUri = null;
        initViews();
        initListeners();
    }

    private void initViews() {
        mailET = (EditText) findViewById(R.id.activityRegistration_editTextMail);
        loginET = (EditText) findViewById(R.id.activityRegistration_editTextLogin);
        passwordET = (TextInputEditText) findViewById(R.id.activityRegistration_editTextPassword);
        completeButton = (Button) findViewById(R.id.activityRegistration_buttonComplete);
        picture = (ImageView) findViewById(R.id.activityRegistration_imageView);
        picture.setImageResource(R.drawable.icons_camera);
    }

    private void initListeners() {
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = mailET.getText().toString();
                String password = passwordET.getText().toString();
                String login = loginET.getText().toString();

                if (!Pattern.matches("(.+)@(.+)\\.(.+)", mail) || mail.contains(" ")) {
                    Toast.makeText(RegistrationActivity.this, "Incorrect email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (login.length() < 3) {
                    Toast.makeText(RegistrationActivity.this, "Login must be at least 3 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (login.contains(" ")) {
                    Toast.makeText(RegistrationActivity.this, "Login must not contain whitespaces", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegistrationActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                createUser(mail, password, login);
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("AUTH", "onAuthStateChanged:signed_in:" + user.getUid());
                    user.sendEmailVerification();
                } else {
                    // User is signed out
                    Log.d("AUTH", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, Constants.REQUEST_EXIT);
            }
        });
    }

    private void createUser(final String email, String password, final String login) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("REG", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(RegistrationActivity.this, "Registration error",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(login).build());

                        // Adding user to DB.
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final String ref = "users/" + mAuth.getCurrentUser().getUid() + "/";
                        database.getReference(ref + "login").setValue(login)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        database.getReference(ref + "email").setValue(email)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        loadImage();
                                                    }
                                                });
                                    }
                                });


                        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this, R.style.MyDialogTheme);
                        builder.setTitle("Registration");
                        builder.setMessage("Registration successfully completed!\nVerify your email and sign in.");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                RegistrationActivity.this.finish();
                            }
                        });
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                RegistrationActivity.this.finish();
                            }
                        });

                        builder.create().show();
                    }
                });
    }

    private void loadImage() {
        if (imageUri == null) {
            return;
        }

        Bitmap bitmap = getResizedBitmap(selectedImage, 400);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] arr = baos.toByteArray();

        mStorageRef.child("profile_photo/" + mAuth.getCurrentUser().getUid()).putBytes(arr)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        mAuth.signOut();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
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
                Toast.makeText(RegistrationActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(RegistrationActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
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
