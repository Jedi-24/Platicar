package com.jedi.platicar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupActivity extends AppCompatActivity {

    private static final String TAG = "lawda";
    private MaterialToolbar toolbar;
    private ScrollView mScroll;
    private ImageButton sendImgbtn;
    private EditText inputField;
    private TextView displayTxt;

    FirebaseAuth mAuth;

    private String grpName;
    private String userName;
    private String userID;
    private String currentDate;
    private String currentTime;
    private DatabaseReference grpTextKeyRef,userRef, grpNameRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        grpName = getIntent().getExtras().getString("GroupName");
        init();
        getUserInfo();

        toolbar.setTitle(grpName);

        sendImgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTxtToFirebase();
                inputField.setText("");

                mScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        grpNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    displayTexts(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                displayTexts(snapshot);
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
    }

    private void displayTexts(DataSnapshot snapshot){

        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()){
//            Log.d(TAG, "displayTexts: hereeee");
            String date = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTxt.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "\n" + date + "\n\n");
            mScroll.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void getUserInfo(){
        userRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userName = snapshot.child("Name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init(){
        toolbar = findViewById(R.id.tool_bar);
        mScroll = findViewById(R.id.scroller_);
        sendImgbtn = findViewById(R.id._send);
        inputField = findViewById(R.id.input_txt);
        displayTxt = (TextView) findViewById(R.id.display_txt_vw);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        grpNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(grpName);
    }

    private void saveTxtToFirebase(){
        String txt = inputField.getText().toString();
        String messageKey = grpNameRef.push().getKey();

        if(txt.matches("")){
            Log.d(TAG, "saveTxtToFirebase: ");
        }
        else{
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currDateFormat = new SimpleDateFormat("MM, dd, yyyy");
            SimpleDateFormat currTimeFormat = new SimpleDateFormat("hh:mm a");

            currentDate = currDateFormat.format(calendar.getTime());
            currentTime = currTimeFormat.format(calendar.getTime());

            HashMap<String,Object> Texts = new HashMap<>();
            grpNameRef.updateChildren(Texts);

            grpTextKeyRef = grpNameRef.child(messageKey);

            HashMap<String,Object> messageInfo = new HashMap<>();
            messageInfo.put("name", userName);
            messageInfo.put("message", txt);
            messageInfo.put("Date", currentDate);
            messageInfo.put("Time", currentTime);

            grpTextKeyRef.updateChildren(messageInfo);
        }
    }
}