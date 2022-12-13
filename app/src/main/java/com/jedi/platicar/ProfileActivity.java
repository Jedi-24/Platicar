package com.jedi.platicar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi.platicar.Utils.NotificationDispatcher;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    CircleImageView circleImageView;
    TextView username, userStatus;

    MaterialButton mBtn, mDeclineBtn;

    DatabaseReference userRef, chatReqRef, contactRef, tokenRef, chatRef;
    FirebaseAuth mAuth;
    String receiverUserId, currentUserId, currentState;

    private interface getDevToken{
        void onRet(String device_token);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        currentState = "new";
        init();
        retrieveInfo();
    }

    private void init() {
        circleImageView = findViewById(R.id.visit_profile_img);
        username = findViewById(R.id.user_name_);
        userStatus = findViewById(R.id.user_status_);
        mBtn = findViewById(R.id.add_friend_btn);
        mDeclineBtn = findViewById(R.id.decline_friend_btn);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("ChatRequests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        tokenRef = FirebaseDatabase.getInstance().getReference().child("tokens");


        mAuth = FirebaseAuth.getInstance();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        currentUserId = mAuth.getCurrentUser().getUid();
    }

    private void retrieveInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("ImageUrl")) {
                    String userImg = snapshot.child("ImageUrl").getValue().toString();
                    String _username = snapshot.child("Name").getValue().toString();
                    String _userStatus = snapshot.child("Status").getValue().toString();

                    Picasso.get().load(userImg).placeholder(R.drawable.man).into(circleImageView);
                    username.setText(_username);
                    userStatus.setText(_userStatus);
                } else {
                    String _username = snapshot.child("Name").getValue().toString();
                    String _userStatus = snapshot.child("Status").getValue().toString();
                    username.setText(_username);
                    userStatus.setText(_userStatus);
                }
                ManageChatRequest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequest() {
        chatReqRef.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild(receiverUserId)) {
                            String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("Sent")) {
                                currentState = "request_sent";
                                mBtn.setText("Cancel chat request");
                            } else if (request_type.equals("Received")) {
                                currentState = "request_received";
                                mBtn.setText("Accept Chat Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                                mDeclineBtn.setOnClickListener(v -> cancelChatRequest());
                            } else {
                                contactRef.child(currentState).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild(receiverUserId)) {
                                            currentState = "Friends";
                                            mBtn.setText("Remove From Contacts");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        contactRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(receiverUserId)) {
                    currentState = "Friends";
                    mBtn.setText("Remove From Contacts");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        if (!receiverUserId.matches(currentUserId)) {
            mBtn.setOnClickListener(v -> {
                mBtn.setEnabled(false);

                if (currentState.equals("new")) {
                    getReceiverToken(); // and eventually send chat request;
                }
                if (currentState.equals("request_sent")) {
                    cancelChatRequest();
                }
                if (currentState.equals("request_received")) {
                    acceptChatRequest();
                }
                if (currentState.equals("Friends")) {
                    removeFriend();
                }
            });
        } else {
            mBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void removeFriend() {
        contactRef.child(currentUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(task -> contactRef.child(receiverUserId).child(currentUserId)
                        .removeValue()
                        .addOnCompleteListener(task1 -> {
                            chatRef.child(currentUserId)
                                    .child(receiverUserId)
                                    .removeValue()
                                    .addOnCompleteListener(task2 -> chatRef.child(receiverUserId)
                                            .child(currentUserId)
                                            .removeValue());

                            mBtn.setEnabled(true);
                            currentState = "new";
                            mBtn.setText("send message");
                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);
                        }));
    }

    private void acceptChatRequest() {
        contactRef.child(currentUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(task -> contactRef.child(receiverUserId).child(currentUserId)
                        .child("Contacts").setValue("Saved")
                        .addOnCompleteListener(task1 -> {
                            chatReqRef.child(currentUserId).child(receiverUserId).removeValue()
                                    .addOnCompleteListener(task2 -> chatReqRef.child(receiverUserId).child(currentUserId).removeValue()
                                            .addOnCompleteListener(task3 -> {
                                                mBtn.setEnabled(true);
                                                currentState = "Friends";
                                                mBtn.setText("Remove From Contacts");
                                                mDeclineBtn.setVisibility(View.GONE);
                                                mDeclineBtn.setEnabled(false);
                                            }));
                        }));
    }

    private void cancelChatRequest() {
        chatReqRef.child(currentUserId)
                .child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatReqRef.child(receiverUserId)
                                .child(currentUserId)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        mBtn.setEnabled(true);
                                        currentState = "new";
                                        mBtn.setText("send message");
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                });
                    }
                });
    }

    private void getReceiverToken(){
        tokenRef.child(receiverUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sendChatRequest((String) snapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendChatRequest(String device_token) {
        chatReqRef.child(currentUserId)
                .child(receiverUserId)
                .child("request_type").setValue("Sent")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatReqRef.child(receiverUserId)
                                .child(currentUserId)
                                .child("request_type").setValue("Received")
                                .addOnCompleteListener(task1 -> {
                                    mBtn.setEnabled(true);
                                    currentState = "request_sent";
                                    mBtn.setText("Cancel chat request");

                                    HashMap<String,String> newChatReq = new HashMap<>();
                                    newChatReq.put("message", "CHECK IT OUT");
                                    newChatReq.put("from", "NEW CHAT REQUEST");

                                    NotificationDispatcher.dispatchNotification(getApplicationContext(),device_token, newChatReq);
                                });
                    }
                });
    }


}