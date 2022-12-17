package com.jedi.platicar.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.jedi.platicar.Models.UserModel;
import com.jedi.platicar.R;
import com.squareup.picasso.Picasso;

public class RequestFragment extends Fragment {

    private static final String TAG = "RequestFragment";

    private RecyclerView mReqRV;

    private DatabaseReference mChatReqRef, mUserRef, mContactRef;
    private FirebaseUser mUser;

    public RequestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            // TODO: handle null user
            return view;
        }
        mChatReqRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        mReqRV = view.findViewById(R.id.chat_request_rv);
        mReqRV.setLayoutManager(new LinearLayoutManager(requireContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            // TODO: handle null user
            Log.d(TAG, "onStart: User NULL");
            return;
        }

        FirebaseRecyclerOptions<UserModel> options = new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(mChatReqRef.child(mUser.getUid()), UserModel.class)
                .build();

        FirebaseRecyclerAdapter<UserModel, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<UserModel, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull UserModel model) {
                holder.accBtn.setVisibility(View.VISIBLE);
                holder.decBtn.setVisibility(View.VISIBLE);

                String reqUserID = getRef(position).getKey();
                if (reqUserID == null) {
                    Log.d(TAG, "onBindViewHolder: key NULL at position: " + position);
                    return;
                }
                getRef(position).child("request_type").getRef().get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "onBindViewHolder: Pos: " + position + " - Fetching dataSnapshot failed");
                        return;
                    }
                    DataSnapshot snapshot = task.getResult();
                    if (!snapshot.exists()) {
                        Log.d(TAG, "onBindViewHolder: Snapshot doesn't exist: " + reqUserID);
                        return;
                    }

                    Object objType = snapshot.getValue();
                    if (objType == null) {
                        Log.d(TAG, "onBindViewHolder: Snapshot Type error");
                        return;
                    }
                    String type = snapshot.getValue().toString();

                    if (!type.equals("Received")) {
                        // TODO: naam bhi bada ajeeb rakha hai tumne
                        holder.daddy.setVisibility(View.GONE); // todo: ajeeb trika to remove sent requests;
                        holder.userName.setVisibility(View.GONE);
                        holder.userStatus.setVisibility(View.GONE);
                        holder.userProfile.setVisibility(View.GONE);
                        holder.accBtn.setVisibility(View.GONE);
                        holder.decBtn.setVisibility(View.GONE);
                        return;
                    }

                    mUserRef.child(reqUserID).get().addOnCompleteListener(userRefTask -> {
                        if (!userRefTask.isSuccessful()) {
                            Log.d(TAG, "onBindViewHolder: [userRefTask] Pos: " + position + " - Fetching dataSnapshot failed");
                            return;
                        }
                        DataSnapshot userRefSnapshot = userRefTask.getResult();

                        if (userRefSnapshot.hasChild("ImageUrl")) {
                            Object objProfileImgUrl = userRefSnapshot.child("ImageUrl").getValue();
                            if (objProfileImgUrl != null) {
                                String profileImgUrl = objProfileImgUrl.toString();
                                Picasso.get().load(profileImgUrl).placeholder(R.drawable.man).into(holder.userProfile);
                            }
                        }
                        Object objName = userRefSnapshot.child("Name").getValue(),
                                objStatus = userRefSnapshot.child("Status").getValue();
                        holder.userName.setText(objName == null ? ":??" : objName.toString());
                        holder.userStatus.setText(objStatus == null ? ":??" : objStatus.toString());

                        // remove from requestList and add user to friends list;
                        holder.accBtn.setOnClickListener(v -> {
                            mContactRef.child(mUser.getUid()).child(reqUserID)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(save_XY_Task -> {
                                        if (!save_XY_Task.isSuccessful()) {
                                            Log.d(TAG, "onBindViewHolder: [save_XY_Task] failed");
                                            return;
                                        }
                                        mContactRef.child(reqUserID).child(mUser.getUid())
                                                .child("Contact").setValue("Saved")
                                                .addOnCompleteListener(save_YX_Task -> {
                                                    if (!save_YX_Task.isSuccessful()) {
                                                        Log.d(TAG, "onBindViewHolder: [save_YX_Task] failed");
                                                        return;
                                                    }
                                                    mChatReqRef.child(mUser.getUid()).child(reqUserID).removeValue();
                                                    mChatReqRef.child(reqUserID).child(mUser.getUid()).removeValue();
                                                });
                                    });
                        });

                        holder.decBtn.setOnClickListener(v -> {
                            mChatReqRef.child(mUser.getUid()).child(reqUserID).removeValue();
                            mChatReqRef.child(reqUserID).child(mUser.getUid()).removeValue();
                        });
                    });
                });
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout, parent, false);
                return new RequestsViewHolder(view);
            }
        };

        mReqRV.setAdapter(adapter);
        adapter.startListening();
    }

    // inner VH class;
    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout daddy;
        TextView userName, userStatus;
        ImageView userProfile;
        Button accBtn, decBtn;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            daddy = itemView.findViewById(R.id.daddy);
            userName = itemView.findViewById(R.id._user_name);
            userStatus = itemView.findViewById(R.id._user_status);
            userProfile = itemView.findViewById(R.id.user_profile_img);
            accBtn = itemView.findViewById(R.id.req_acc_btn);
            decBtn = itemView.findViewById(R.id.req_cancel_btn);
        }
    }
}