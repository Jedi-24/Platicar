package com.jedi.platicar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jedi.platicar.NewGroupActivity;
import com.jedi.platicar.R;

public class GroupsFragment extends Fragment {

    View mView;
    private RecyclerView mGroupRV;
    private FloatingActionButton mFloatingActionButton;

    DatabaseReference mGrpRef;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_groups, container, false);
        initializeViews();

        mFloatingActionButton.setOnClickListener(v -> {
            // todo: creating new groups;
            startActivity(new Intent(requireContext(), NewGroupActivity.class));
        });

        return mView;
    }

    void initializeViews() {
        mGrpRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        mGroupRV = mView.findViewById(R.id.grp_rv);
        mFloatingActionButton = mView.findViewById(R.id.floating_action_button);
    }
}