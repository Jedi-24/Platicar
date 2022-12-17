package com.jedi.platicar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi.platicar.ChatActivity;
import com.jedi.platicar.Models.UserModel;
import com.jedi.platicar.R;
import com.squareup.picasso.Picasso;

public class ChatFragment extends Fragment {

    View chatsView;
    RecyclerView chatsRV;
    private DatabaseReference chatRef, userRef; // UserRef to access user Data; ContactRef has the Uids of friends;
    FirebaseAuth mAuth;
    String currentUserID;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        chatsView = inflater.inflate(R.layout.fragment_chat, container, false);
        chatsRV = chatsView.findViewById(R.id.chats_list);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid(); // online user's ID;
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsRV.setLayoutManager(new LinearLayoutManager(requireContext()));

        return chatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(chatRef, UserModel.class)
                        .build();

        FirebaseRecyclerAdapter<UserModel, ChatsViewHolder> recyclerAdapter = new FirebaseRecyclerAdapter<UserModel, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull UserModel model) {
                String friendUID = getRef(position).getKey();
                final String[] profileImgUrl = {"default_img"};

                userRef.child(friendUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("ImageUrl")) {
                            profileImgUrl[0] = snapshot.child("ImageUrl").getValue().toString();
                            Picasso.get().load(profileImgUrl[0]).placeholder(R.drawable.man).into(holder.userProfile);
                        }

                        String friendName = snapshot.child("Name").getValue().toString();
                        String friendStatus = snapshot.child("Status").getValue().toString();

                        holder.userName.setText(friendName);
                        holder.userStatus.setText(friendStatus);

                        if (snapshot.child("userStatus").hasChild("state")) {
                            String onlineStatus = (String) snapshot.child("userStatus").child("state").getValue();
                            if (onlineStatus.matches("online")) {
                                holder.userOnlineStatus.setVisibility(View.VISIBLE);
                            } else {
                                holder.userOnlineStatus.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            holder.userOnlineStatus.setVisibility(View.INVISIBLE);
                        }

                        holder.itemView.setOnClickListener(v -> {
                            Intent chatIntent = new Intent(requireContext(), ChatActivity.class);
                            chatIntent.putExtra("userID", friendUID);
                            chatIntent.putExtra("userName", friendName);
                            chatIntent.putExtra("userImgUrl", profileImgUrl[0]); // TODO: 12/1/2022 bug fix laterrr.
                            startActivity(chatIntent);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout, parent, false);
                ChatsViewHolder chatsViewHolder = new ChatsViewHolder(view);

                return chatsViewHolder;
            }
        };
        chatsRV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    // inner VH class;
    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        ImageView userProfile, userOnlineStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id._user_name);
            userStatus = itemView.findViewById(R.id._user_status);
            userProfile = itemView.findViewById(R.id.user_profile_img);
            userOnlineStatus = itemView.findViewById(R.id.user_online_status);
        }
    }

}