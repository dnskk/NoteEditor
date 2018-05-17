package com.example.noteeditor.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.noteeditor.FriendsSearchActivity;
import com.example.noteeditor.R;
import com.example.noteeditor.TabChange;
import com.example.noteeditor.adapters.RequestPersonAdapter;
import com.example.noteeditor.adapters.ViewPagerAdapter;


public class FriendsFragment extends Fragment implements TabChange {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public FriendsFragment() {
    }

    public void resetRequests(){
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        tab.setText("Requests");
    }

    public void incrementRequests() {
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        String title = tab.getText().toString();
        if (title.equals("Requests")) {
            tab.setText("Requests (1)");
            return;
        }

        String str = title.substring(10, title.length() - 1);
        int n = Integer.parseInt(str) + 1;
        tab.setText("Requests (" + n + ")");
    }

    public void decrementRequests() {
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        String title = tab.getText().toString();
        if (title.equals("Requests") || title.equals("Requests (1)")) {
            tab.setText("Requests");
            return;
        }

        String str = title.substring(10, title.length() - 1);
        int n = Integer.parseInt(str) - 1;
        tab.setText("Requests (" + n + ")");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewPager = getView().findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = getView().findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);


        FloatingActionButton fab = getView().findViewById(R.id.fabFriends);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FriendsSearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new AllFriendsFragment(), "All");
        RequestsFragment requestsFragment = new RequestsFragment();
        requestsFragment.delegate = FriendsFragment.this;
        adapter.addFragment(requestsFragment, "Requests");

        viewPager.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

}
