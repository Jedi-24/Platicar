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

    View grpFragmentView;
    private RecyclerView groupRV;
    private FloatingActionButton floatingActionButton;

    DatabaseReference grpRef;


    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        grpFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        initializeViews();

        floatingActionButton.setOnClickListener(v -> {
            // todo: creating new grps;
            startActivity(new Intent(requireContext(), NewGroupActivity.class));
        });

        return grpFragmentView;
    }

    void initializeViews() {
        grpRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        groupRV = grpFragmentView.findViewById(R.id.grp_rv);
        floatingActionButton = grpFragmentView.findViewById(R.id.floating_action_button);
    }
}