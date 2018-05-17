package com.example.noteeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.noteeditor.adapters.Person;
import com.example.noteeditor.adapters.PersonAdapter;
import com.example.noteeditor.adapters.SearchPersonAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class FriendsSearchActivity extends AppCompatActivity {
    private EditText loginET;
    private Button findButton;
    private ListView personsLV;
    private FirebaseDatabase fDB;
    private SearchPersonAdapter adapter;
    private ArrayList<Person> persons;
    private ArrayList<String> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_search);
        getSupportActionBar().setTitle("Search friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fDB = FirebaseDatabase.getInstance();

        loginET = (EditText) findViewById(R.id.activityFriendsSearch_editText);
        findButton = (Button) findViewById(R.id.activityFriendsSearch_buttonComplete);
        personsLV = (ListView) findViewById(R.id.activityFriendsSearch_listView);

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                DatabaseReference dbRef = fDB.getReference("users/");
                String str = loginET.getText().toString();
                Query q = dbRef.orderByChild("login").startAt(str).endAt(str + "\uf8ff");
                persons = new ArrayList<>();
                friends = new ArrayList<>();
                adapter = new SearchPersonAdapter(FriendsSearchActivity.this, persons);
                personsLV.setAdapter(adapter);

                getPersons(q);
            }
        });
    }

    private void getPersons(final Query q) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fDB.getReference("friends").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> fUids = new HashSet<>();
                Map<String, Object> friends = (Map<String, Object>) dataSnapshot.getValue();
                if (friends != null) {
                    fUids = friends.keySet();
                }

                removeExtras(q, fUids);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeExtras(Query q, final Set<String> friends) {
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> persons = (Map<String, Object>) dataSnapshot.getValue();
                if (persons == null) {
                    return;
                }

                String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Set<String> personsUids = persons.keySet();
                personsUids.removeAll(friends);
                for (String uid : personsUids) {
                    if (myID.equals(uid)) {
                        continue;
                    }

                    getFriend(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getFriend(final String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String login = (String) dataSnapshot.child("login").getValue();
                String email = (String) dataSnapshot.child("email").getValue();
                Person p = new Person(login, email);
                p.uid = uid;
                persons.add(p);
                adapter.notifyDataSetChanged();
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
