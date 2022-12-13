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
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi.platicar.Models.UserModel;
import com.jedi.platicar.fragments.FriendsFragment;
import com.squareup.picasso.Picasso;

public class NewGroupActivity extends AppCompatActivity {

    private MaterialToolbar mToolbar;
    private MaterialCardView mCardView;
    private RecyclerView mFriendsRV;
    private DatabaseReference contactRef, userRef; // UserRef to access user Data; ContactRef has the Uids of friends;
    FirebaseAuth mAuth;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        init();

        mToolbar.setTitle("New Group");
        mToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        mToolbar.setNavigationOnClickListener(v -> super.onBackPressed());
        // todo : laterrr
//        mCardView.setOnLongClickListener(v -> {
//
//        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(contactRef,UserModel.class)
                        .build();

        FirebaseRecyclerAdapter<UserModel, FriendsViewHolder> recyclerAdapter = new FirebaseRecyclerAdapter<UserModel, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull UserModel model) {
                String friendUID = getRef(position).getKey();
                final String[] profileImgUrl = {"default_img"};

                userRef.child(friendUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild("ImageUrl")){
                            profileImgUrl[0] = snapshot.child("ImageUrl").getValue().toString();
                            Picasso.get().load(profileImgUrl[0]).placeholder(R.drawable.man).into(holder.friendProfile);
                        }
                        String friendName = snapshot.child("Name").getValue().toString();
                        String friendStatus = snapshot.child("Status").getValue().toString();

                        holder.friendName.setText(friendName);
                        holder.friendStatus.setText(friendStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_friend_layout,parent,false);

                FriendsViewHolder friendsViewHolder = new FriendsViewHolder(view);
                return  friendsViewHolder;
            }
        };

        mFriendsRV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    // inner VH class;
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        TextView friendName, friendStatus;
        ImageView friendProfile;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            friendName = itemView.findViewById(R.id.friend_username);
            friendStatus = itemView.findViewById(R.id.friend_status);
            friendProfile = itemView.findViewById(R.id.friend_img);
        }
    }

    private void init(){
        mCardView = findViewById(R.id.friend_card);
        mToolbar = findViewById(R.id.grp_toolbar);
        mFriendsRV = findViewById(R.id.friends_list_);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid(); // online user's ID;
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendsRV.setLayoutManager(new LinearLayoutManager(this));
    }
}