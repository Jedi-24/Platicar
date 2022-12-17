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
import com.jedi.platicar.ProfileActivity;
import com.jedi.platicar.R;
import com.squareup.picasso.Picasso;

public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    private RecyclerView friendsRV;

    // UserRef to access user Data; ContactRef has the UIDs of friends;
    private DatabaseReference mUserRef, mContactRef;

    public FriendsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsRV = view.findViewById(R.id.friends_list);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // TODO: handle user null
            return view;
        }
        mContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(user.getUid());
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        friendsRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(mContactRef, UserModel.class)
                        .build();

        FirebaseRecyclerAdapter<UserModel, FriendsViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<UserModel, FriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull UserModel model) {
                        holder.startChat.setVisibility(View.VISIBLE);

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
                            holder.userStatus.setText(objFriendStatus == null ? ":??" : objFriendStatus.toString());

                            holder.startChat.setOnClickListener(v -> {
                                Intent chatIntent = new Intent(requireContext(), ChatActivity.class);
                                chatIntent.putExtra("userID", friendUID);
                                chatIntent.putExtra("userName", friendName);
                                chatIntent.putExtra("userImgUrl", profileImgUrl[0]);
                                startActivity(chatIntent);
                            });

                            holder.itemView.setOnClickListener(v -> {
                                String visit_user_id = getRef(holder.getBindingAdapterPosition()).getKey();

                                Intent viewProfileIntent = new Intent(requireContext(), ProfileActivity.class);
                                viewProfileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(viewProfileIntent);
                            });
                        });
                    }

                    @NonNull
                    @Override
                    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout, parent, false);
                        return new FriendsViewHolder(view);
                    }
                };
        friendsRV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    // inner VH class;
    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
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