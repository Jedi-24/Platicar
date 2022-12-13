package com.jedi.platicar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jedi.platicar.Models.UserModel;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    MaterialToolbar mToolbar;
    RecyclerView mRecyclerView;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.tool_bar);
        mRecyclerView = findViewById(R.id.user_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mToolbar.setTitle(R.string.app_name);
        mToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        mToolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(FindFriendsActivity.this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(userRef,UserModel.class)
                        .build();

        FirebaseRecyclerAdapter<UserModel,FindFriendsViewHolder> rvAdapter =
                new FirebaseRecyclerAdapter<UserModel, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull UserModel model) {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImageUrl()).placeholder(R.drawable.man).into(holder.userProfile);

                holder.itemView.setOnClickListener(v -> {
                    String visit_user_id = getRef(holder.getBindingAdapterPosition()).getKey();

                    Intent viewProfileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                    viewProfileIntent.putExtra("visit_user_id", visit_user_id);
                    startActivity(viewProfileIntent);
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout,parent,false);

                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return  viewHolder;
            }
        };
        mRecyclerView.setAdapter(rvAdapter);
        rvAdapter.startListening();
    }

    // inner VH class;
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        ImageView userProfile;
        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id._user_name);
            userStatus = itemView.findViewById(R.id._user_status);
            userProfile = itemView.findViewById(R.id.user_profile_img);
        }
    }


}