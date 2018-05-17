package com.example.noteeditor.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.noteeditor.CurrentNoteActivity;
import com.example.noteeditor.CurrentVariantActivity;
import com.example.noteeditor.FriendsSearchActivity;
import com.example.noteeditor.R;
import com.example.noteeditor.RegistrationActivity;
import com.example.noteeditor.VariantCreatingActivity;
import com.example.noteeditor.adapters.Note;
import com.example.noteeditor.adapters.Person;
import com.example.noteeditor.adapters.PersonAdapter;
import com.example.noteeditor.adapters.Variant;
import com.example.noteeditor.adapters.VariantAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class VariantsFragment extends ListFragment {
    private VariantAdapter adapter;
    private ArrayList<Variant> variants;
    private FirebaseDatabase fDB;
    public String noteID;

    public VariantsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = getView().findViewById(R.id.fabVariants);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), VariantCreatingActivity.class);
                intent.putExtra("noteID", noteID);
                startActivity(intent);
            }
        });

        loadVariants();
    }

    private void loadVariants() {
        fDB = FirebaseDatabase.getInstance();
        fDB.getReference("variants/" + noteID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                variants = new ArrayList<>();
                adapter = new VariantAdapter(getActivity(), variants);
                setListAdapter(adapter);
                setOnLongClick();

                if (dataSnapshot.getValue().equals(0)) {
                    return;
                }

                HashMap<String, HashMap<String, String>> vars = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                if (vars == null) {
                    return;
                }

                for (Map.Entry<String, HashMap<String, String>> v : vars.entrySet()) {
                    if (v.getKey().equals("numberOfVariants")) {
                        continue;
                    }

                    Variant var = new Variant();
                    var.noteID = noteID;
                    var.variantID = v.getKey();
                    var.title = v.getValue().get("title");
                    loadVotes(var);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadVotes(final Variant var) {
        String variantID = var.variantID;
        fDB.getReference("votes/" + noteID + "/" + variantID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Boolean> votes = (HashMap<String, Boolean>) dataSnapshot.getValue();
                if (votes == null) {
                    variants.add(var);
                    adapter.notifyDataSetChanged();
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

                var.likes = likes;
                var.dislikes = dislikes;
                variants.add(var);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setOnLongClick() {
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final Variant v = (Variant) adapter.getItem(i);
                fDB.getReference("members/" + noteID + "/" + uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean isAdmin = (boolean) dataSnapshot.getValue();
                                if (isAdmin) {
                                    createAlert(v.variantID, v.title, i);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                return true;
            }
        });
    }

    private void createAlert(final String variantID, final String title, final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        builder.setTitle(title);
        builder.setMessage("What do you want to do?");
        builder.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                fDB.getReference("notes/" + noteID + "/isActive").setValue(false);
                fDB.getReference("resolved/" + noteID + "/title").setValue(title);
                fDB.getReference("resolved/" + noteID + "/variantID").setValue(variantID);
                fDB.getReference("variants/" + noteID + "/" + variantID + "/description")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String description = (String) dataSnapshot.getValue();
                                fDB.getReference("resolved/" + noteID + "/description").setValue(description);
                                fDB.getReference("variants/" + noteID + "/" + variantID + "/link")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() == null) {
                                                    getActivity().finish();
                                                }
                                                String link = (String) dataSnapshot.getValue();
                                                fDB.getReference("resolved/" + noteID + "/link").setValue(link);
                                                getActivity().finish();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
            }
        });
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                variants.remove(pos);
                adapter.notifyDataSetChanged();
                fDB.getReference("variants/" + noteID + "/" + variantID).removeValue();
                updateHistory(title);
            }
        });

        builder.setNeutralButton("Nothing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();
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
                                .setValue(" deleted option \"" + title + "\"");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                        String currentDateandTime = sdf.format(new Date());
                        fDB.getReference("history/" + noteID + "/" + n).child("date").setValue(currentDateandTime);
                        fDB.getReference("history/" + noteID + "/" + n).child("type").setValue("Deleting an option");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Variant var = (Variant) adapter.getItem(position);
        Intent intent = new Intent(getActivity(), CurrentVariantActivity.class);
        intent.putExtra("noteID", var.noteID);
        intent.putExtra("variantID", var.variantID);
        intent.putExtra("title", var.title);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_variants, container, false);
    }
}
