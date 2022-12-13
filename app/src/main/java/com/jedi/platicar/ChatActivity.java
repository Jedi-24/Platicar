package com.jedi.platicar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi.platicar.Utils.NotificationDispatcher;
import com.jedi.platicar.Utils.TextAdapter;
import com.jedi.platicar.Models.TextModal;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String friendName,friendImg,friendUid,currUid;
    MaterialToolbar mToolbar;
    CircleImageView mUserProfile;
    ImageView backBtn;
    TextView mUsername, mLastSeen;

    RecyclerView chatRV;
    EditText inputBox;
    ImageButton sendBtn;

    FirebaseAuth mAuth;
    DatabaseReference rootRef,chatRef,tokenRef;
    private final ArrayList<TextModal> texts = new ArrayList<>();
    private TextAdapter textAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();


        friendName = getIntent().getExtras().get("userName").toString();
        friendImg = getIntent().getExtras().get("userImgUrl").toString();

        friendUid = getIntent().getExtras().get("userID").toString();
        currUid = mAuth.getCurrentUser().getUid();

        mUsername.setText(friendName);
        Picasso.get().load(friendImg).placeholder(R.drawable.man).into(mUserProfile);

        sendBtn.setOnClickListener(v -> sendMessage());

        backBtn.setOnClickListener(v -> super.onBackPressed());
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
        texts.clear();
        // retrieving the texts from Firebase into our ArrayList;
        rootRef.child("Messages").child(currUid)
                .child(friendUid)
                .addChildEventListener(new ChildEventListener() { // whenever a change is made in child (a text is added or whatever...)
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        TextModal text = snapshot.getValue(TextModal.class);
                        texts.add(text);

                        textAdapter.notifyDataSetChanged();

                        chatRV.smoothScrollToPosition(texts.size());
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    }
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        displayLastSeen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }

    private void sendMessage(){
        String txt = inputBox.getText().toString();
        if(!txt.equals("")){
            String messageSenderRef = "Messages/" + currUid + "/" + friendUid;
            String messageReceiverRef = "Messages/" + friendUid + "/" + currUid;

            // a key for each message;
            DatabaseReference messageKeyRef = rootRef.child("Messages")
                    .child(currUid)
                    .child(friendUid).push(); // creates a key for each message;

            String messageId = messageKeyRef.getKey();
            HashMap<String,String> textBody = new HashMap<>();

            textBody.put("message" , txt);
            textBody.put("type" , "text");
            textBody.put("from" , currUid);

            HashMap<String,Object> textBodyDetails = new HashMap<>();
            textBodyDetails.put(messageSenderRef+ "/" + messageId, textBody);
            textBodyDetails.put(messageReceiverRef+ "/" + messageId, textBody);

            rootRef.updateChildren(textBodyDetails).addOnSuccessListener(unused -> {
                getReceiverToken(textBody);
                inputBox.setText("");
            });

            chatRef.child(currUid)
                    .child(friendUid).child("chat").setValue("live");

            chatRef.child(friendUid)
                    .child(currUid).child("chat").setValue("live");
        }
    }

    private void getReceiverToken(HashMap<String, String> textBody){
        tokenRef.child(friendUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            NotificationDispatcher.dispatchNotification(getApplicationContext(), (String) snapshot.getValue(), textBody);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void init(){
        mToolbar = findViewById(R.id._custom_chat_toolbar);
        mUsername = findViewById(R.id.custom_chat_username);
        mLastSeen = findViewById(R.id.custom_chat_last_seen);
        mUserProfile = findViewById(R.id.custom_chat_user_img);
        backBtn = findViewById(R.id.back_btn);

        chatRV = findViewById(R.id.chat_rv);
        chatRV.setLayoutManager(new LinearLayoutManager(this));
        textAdapter = new TextAdapter(texts); // adapter pe texts ki arrayList is initiated using constructor.
        chatRV.setAdapter(textAdapter);

        inputBox = findViewById(R.id.input_txt_);
        sendBtn = findViewById(R.id._send_);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        tokenRef = FirebaseDatabase.getInstance().getReference().child("tokens");
    }

    void displayLastSeen(){
        rootRef.child("Users").child(friendUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("userStatus").hasChild("state")){
                            String onlineStatus = (String) snapshot.child("userStatus").child("state").getValue();
                            String date = (String) snapshot.child("userStatus").child("date").getValue();
                            String time = (String) snapshot.child("userStatus").child("time").getValue();

                            if(onlineStatus.matches("online")){
                                mLastSeen.setText("online");
                            }
                            else if(onlineStatus.matches("offline")){
                                mLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        } else{
                            mLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    void updateUserStatus(String onlineState){
        String  saveCurrDate, saveCurrTime;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd, yyyy");
        saveCurrDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        saveCurrTime = timeFormat.format(calendar.getTime());

        HashMap<String, Object> onlineStatus = new HashMap<>();
        onlineStatus.put("date" , saveCurrDate);
        onlineStatus.put("time" , saveCurrTime);
        onlineStatus.put("state", onlineState);

        rootRef.child("Users").child(currUid)
                .child("userStatus")
                .updateChildren(onlineStatus);

    }

}