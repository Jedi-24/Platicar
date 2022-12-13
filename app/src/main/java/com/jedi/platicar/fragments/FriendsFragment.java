package com.jedi.platicar.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi.platicar.ProfileActivity;
import com.jedi.platicar.R;
import com.jedi.platicar.Models.UserModel;
import com.jedi.platicar.ChatActivity;
import com.squareup.picasso.Picasso;

public class FriendsFragment extends Fragment {
    private View friendsView;
    private RecyclerView friendsRV;
    private DatabaseReference contactRef, userRef; // UserRef to access user Data; ContactRef has the Uids of friends;
    FirebaseAuth mAuth;
    String currentUserID;

    public FriendsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        friendsView = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsRV = friendsView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid(); // online user's ID;
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        friendsRV.setLayoutManager(new LinearLayoutManager(requireContext()));

        return friendsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(contactRef,UserModel.class)
                        .build();

        FirebaseRecyclerAdapter<UserModel, FriendsViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<UserModel, FriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull UserModel model) {
                        holder.startChat.setVisibility(View.VISIBLE);

                        String friendUID = getRef(position).getKey();
                        final String[] profileImgUrl = {"default_img"};

                        userRef.child(friendUID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChild("ImageUrl")){
                                    profileImgUrl[0] = snapshot.child("ImageUrl").getValue().toString();
                                    Picasso.get().load(profileImgUrl[0]).placeholder(R.drawable.man).into(holder.userProfile);
                                }
                                String friendName = snapshot.child("Name").getValue().toString();
                                String friendStatus = snapshot.child("Status").getValue().toString();

                                holder.userName.setText(friendName);
                                holder.userStatus.setText(friendStatus);

                                holder.startChat.setOnClickListener(v -> {
                                    Intent chatIntent = new Intent(requireContext(), ChatActivity.class);
                                    chatIntent.putExtra("userID", friendUID);
                                    chatIntent.putExtra("userName",friendName);
                                    chatIntent.putExtra("userImgUrl", profileImgUrl[0]); // TODO: 12/1/2022 bug fix later.
                                    startActivity(chatIntent);
                                });

                                holder.itemView.setOnClickListener(v -> {
                                    String visit_user_id = getRef(holder.getBindingAdapterPosition()).getKey();

                                    Intent viewProfileIntent = new Intent(requireContext(), ProfileActivity.class);
                                    viewProfileIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(viewProfileIntent);
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout,parent,false);

                        FriendsViewHolder friendsViewHolder = new FriendsViewHolder(view);
                        return  friendsViewHolder;
                    }
                };
        friendsRV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    // inner VH class;
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        ImageView userProfile, startChat;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id._user_name);
            userStatus = itemView.findViewById(R.id._user_status);
            userProfile = itemView.findViewById(R.id.user_profile_img);
            startChat = itemView.findViewById(R.id._chat);
        }
    }
}