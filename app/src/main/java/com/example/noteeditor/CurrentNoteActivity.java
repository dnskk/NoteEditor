package com.example.noteeditor;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.noteeditor.adapters.VariantAdapter;
import com.example.noteeditor.adapters.ViewPagerAdapter;
import com.example.noteeditor.fragments.AllFriendsFragment;
import com.example.noteeditor.fragments.ConversationFragment;
import com.example.noteeditor.fragments.FriendsFragment;
import com.example.noteeditor.fragments.HistoryFragment;
import com.example.noteeditor.fragments.RequestsFragment;
import com.example.noteeditor.fragments.VariantsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CurrentNoteActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FirebaseDatabase fDB;
    private String title;
    private String noteID;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_note);
        setResult(RESULT_OK, null);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        noteID = getIntent().getStringExtra("noteID");

        fDB = FirebaseDatabase.getInstance();
        fDB.getReference("notes/" + noteID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                title = (String) dataSnapshot.child("title").getValue();
                getSupportActionBar().setTitle(title);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        isAdmin = false;
        fDB.getReference("members/" + noteID + "/" + myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isAdmin = (boolean) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        String noteID = getIntent().getStringExtra("noteID");

        VariantsFragment vf = new VariantsFragment();
        vf.noteID = noteID;
        adapter.addFragment(vf, "Options");

        ConversationFragment cf = new ConversationFragment();
        cf.noteID = noteID;
        adapter.addFragment(cf, "Chat");

        HistoryFragment hf = new HistoryFragment();
        hf.noteID = noteID;
        adapter.addFragment(hf, "History");

        viewPager.setAdapter(adapter);
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
