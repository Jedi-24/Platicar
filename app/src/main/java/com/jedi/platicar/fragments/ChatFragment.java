package com.jedi.platicar.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jedi.platicar.ChatActivity;
import com.jedi.platicar.Models.UserModel;
import com.jedi.platicar.R;
import com.squareup.picasso.Picasso;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private RecyclerView mChatsRV;

    // UserRef to access user Data; ChatRef has the UIDs of friends;
    private DatabaseReference mChatRef, mUserRef;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatsRV = view.findViewById(R.id.chats_list);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // TODO: handle null user properly
            return view;
        }
        mChatRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(user.getUid());
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mChatsRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(mChatRef, UserModel.class)
                        .build();

        FirebaseRecyclerAdapter<UserModel, ChatsViewHolder> recyclerAdapter = new FirebaseRecyclerAdapter<UserModel, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull UserModel model) {
                String friendUID = getRef(position).getKey();
                if (friendUID == null) {
                    Log.d(TAG, "onBindViewHolder: key NULL at position: " + position);
                    return;
                }

                final String[] profileImgUrl = {"default_img"};
                mUserRef.child(friendUID).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "onBindViewHolder: Pos: " + position + " - Fetching dataSnapshot failed");
                        return;
                    }
                    DataSnapshot snapshot = task.getResult();

                    if (snapshot.hasChild("ImageUrl")) {
                        Object imgUrl = snapshot.child("ImageUrl").getValue();
                        if (imgUrl != null) {
                            profileImgUrl[0] = imgUrl.toString();
                            Picasso.get().load(profileImgUrl[0]).placeholder(R.drawable.man).into(holder.userProfile);
                        }
                    }

                    Object objFriendName = snapshot.child("Name").getValue(),
                            objFriendStatus = snapshot.child("Status").getValue();
                    String friendName = objFriendName == null ? ":??" : objFriendName.toString();

                    holder.userName.setText(friendName);
                    holder.userStatus.setText(objFriendStatus == null ? ":/" : objFriendStatus.toString());

                    if (snapshot.child("userStatus").hasChild("state")) {
                        String onlineStatus = (String) snapshot.child("userStatus").child("state").getValue();
                        if (onlineStatus != null && onlineStatus.matches("online")) {
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
                        chatIntent.putExtra("userImgUrl", profileImgUrl[0]);
                        startActivity(chatIntent);
                    });
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout, parent, false);
                return new ChatsViewHolder(view);
            }
        };
        mChatsRV.setAdapter(recyclerAdapter);
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