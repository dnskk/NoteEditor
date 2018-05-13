package com.example.noteeditor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.noteeditor.fragments.FriendsFragment;
import com.example.noteeditor.fragments.AllFriendsFragment;
import com.example.noteeditor.fragments.NotesFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Toolbar toolbar;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setResult(RESULT_OK, null);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        updateHeader();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Notes");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
        fTransaction.replace(R.id.container, new NotesFragment());
        fTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();

        if (id == R.id.nav_notes) {
            toolbar.setTitle("Notes");
            fTransaction.replace(R.id.container, new NotesFragment());
        } else if (id == R.id.nav_friends) {
            toolbar.setTitle("Friends");
            fTransaction.replace(R.id.container, new FriendsFragment());
        } else if (id == R.id.nav_settigs) {

        } else if (id == R.id.nav_sign_out) {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivityForResult(intent, Constants.REQUEST_EXIT);
        }

        fTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateHeader(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView tvLogin = headerView.findViewById(R.id.header_textViewLogin);
        TextView tvEmail = headerView.findViewById(R.id.header_textViewMail);
        final ImageView ivPhoto = headerView.findViewById(R.id.header_imageView);
        FirebaseUser user= mAuth.getCurrentUser();
        tvLogin.setText(user.getDisplayName());
        tvEmail.setText(user.getEmail());

        try {
            String myUid = mAuth.getCurrentUser().getUid();
            final File localFile = File.createTempFile(myUid, "jpg");
            mStorageRef.child("profile_photo/" + myUid).getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    ivPhoto.setImageURI(Uri.fromFile(localFile));
//                    Bitmap bitmap = BitmapHelper.getBitmapFromFile(localFile);
//                    bitmap = BitmapHelper.getResizedBitmap(bitmap,60,60);
//                    bitmap = BitmapHelper.getRoundedCornerBitmap(bitmap);
//                    ivPhoto.setImageBitmap(null);
//                    ivPhoto.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
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
