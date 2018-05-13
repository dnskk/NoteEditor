package com.example.noteeditor;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText passwordET;
    EditText mailET;
    TextView registrationTV;
    Button enterButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setResult(RESULT_OK, null);
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        initListeners();

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Toast.makeText(LoginActivity.this, "USER" + user.getEmail(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "no user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initViews() {
        mailET = (EditText) findViewById(R.id.activityLogin_editTextMail);
        passwordET = (TextInputEditText) findViewById(R.id.activityLogin_editTextPassword);
        enterButton = (Button) findViewById(R.id.activityLogin_button);
        registrationTV = (TextView) findViewById(R.id.activityLogin_textViewReg);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    if (!user.isEmailVerified()) {
                        mAuth.signOut();
                        Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivityForResult(intent, Constants.REQUEST_EXIT);
                }
            }
        };
    }

    private void initListeners() {
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = mailET.getText().toString();
                String password = passwordET.getText().toString();

                if (!Pattern.matches("(.+)@(.+)\\.(.+)", mail) || mail.contains(" ")) {
                    Toast.makeText(LoginActivity.this, "Incorrect mail", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: authentication.
                mAuth.signInWithEmailAndPassword(mail, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Login failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        registrationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_EXIT) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }
}
