package com.example.noteeditor;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;


public class PreAuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_auth);
        getSupportActionBar().hide();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(PreAuthActivity.this, MainActivity.class);
            startActivityForResult(intent, Constants.REQUEST_EXIT);
        } else {
            Intent intent = new Intent(PreAuthActivity.this, LoginActivity.class);
            startActivityForResult(intent, Constants.REQUEST_EXIT);
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
