package com.jedi.platicar.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi.platicar.R;
import com.jedi.platicar.Models.UserModel;
import com.squareup.picasso.Picasso;

public class RequestFragment extends Fragment {

    View reqFragView;
    RecyclerView reqRV;
    DatabaseReference chatReqRef, userRef,contactRef;
    FirebaseAuth mAuth;
    String currUserID;

    public RequestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        reqFragView =  inflater.inflate(R.layout.fragment_request, container, false);
        mAuth = FirebaseAuth.getInstance();
        currUserID = mAuth.getCurrentUser().getUid();
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        reqRV = reqFragView.findViewById(R.id.chat_request_rv);
        reqRV.setLayoutManager(new LinearLayoutManager(requireContext()));

        return reqFragView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<UserModel> options = new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(chatReqRef.child(currUserID),UserModel.class)
                .build();

        FirebaseRecyclerAdapter<UserModel, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<UserModel, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull UserModel model) {
                holder.accBtn.setVisibility(View.VISIBLE);
                holder.decBtn.setVisibility(View.VISIBLE);

                String reqUserID = getRef(position).getKey();
                DatabaseReference reqTypeRef = getRef(position).child("request_type").getRef();

                reqTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String type = snapshot.getValue().toString();

                            if(type.equals("Received")){
                                userRef.child(reqUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("ImageUrl")){
                                            String profileImgUrl = snapshot.child("ImageUrl").getValue().toString();
                                            Picasso.get().load(profileImgUrl).placeholder(R.drawable.man).into(holder.userProfile);
                                        }
                                        String name = snapshot.child("Name").getValue().toString();
                                        String status = snapshot.child("Status").getValue().toString();

                                        holder.userName.setText(name);
                                        holder.userStatus.setText(status);

                                        holder.accBtn.setOnClickListener(v -> { // remove from requestList and add user to friends list;
                                            contactRef.child(currUserID).child(reqUserID)
                                                    .child("Contact").setValue("Saved")
                                                    .addOnCompleteListener(task -> {
                                                        if(task.isSuccessful()){
                                                            contactRef.child(reqUserID).child(currUserID)
                                                                    .child("Contact").setValue("Saved")
                                                                    .addOnCompleteListener(task1 -> {
                                                                        if(task1.isSuccessful()){
                                                                            chatReqRef.child(currUserID).child(reqUserID)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(task2 -> {
                                                                                        if(task2.isSuccessful()){
                                                                                            chatReqRef.child(reqUserID).child(currUserID)
                                                                                                    .removeValue();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        });

                                        holder.decBtn.setOnClickListener(v -> chatReqRef.child(currUserID).child(reqUserID)
                                                .removeValue()
                                                .addOnCompleteListener(task2 -> {
                                                    if(task2.isSuccessful()){
                                                        chatReqRef.child(reqUserID).child(currUserID)
                                                                .removeValue();
                                                    }
                                                }));
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                            else{
                                holder.daddy.setVisibility(View.GONE); // todo: ajeeb trika to remove sent requests;
                                holder.userName.setVisibility(View.GONE);
                                holder.userStatus.setVisibility(View.GONE);
                                holder.userProfile.setVisibility(View.GONE);
                                holder.accBtn.setVisibility(View.GONE);
                                holder.decBtn.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout,parent,false);

                RequestsViewHolder requestsViewHolder = new RequestsViewHolder(view);
                return requestsViewHolder;
            }
        };

        reqRV.setAdapter(adapter);
        adapter.startListening();
    }

    // inner VH class;
    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
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