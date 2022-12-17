package com.jedi.platicar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "settingsActivity";
    private int GALLERY = 1, CAMERA = 2;
    CircleImageView profile_img;
    EditText mUserName, mUserStatus;
    MaterialButton mbtn;

    private StorageReference userProfileImagesRef;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;

    String userID;

    private ActivityResultLauncher<Intent> mActivityAddNoteResultLauncher;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        init();

        mActivityAddNoteResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == this.RESULT_OK) {
                        loadingBar.setTitle("Set Profile Picture");
                        loadingBar.setMessage("Your Profile Pic is being Updated");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        Intent data = result.getData();
                        if (data == null) return;

                        Uri contentURI = data.getData();
                        StorageReference filePath = userProfileImagesRef.child(userID + ".jpg");

                        filePath.putFile(contentURI).addOnSuccessListener(taskSnapshot ->
                                taskSnapshot.getStorage().getDownloadUrl()
                                        .addOnSuccessListener(uri -> rootRef.child("Users")
                                                .child(userID)
                                                .child("ImageUrl").setValue(uri.toString())
                                                .addOnCompleteListener(task -> loadingBar.dismiss())));
                    } else {
                        loadingBar.dismiss();
                    }
                });

        mbtn.setOnClickListener(v -> {
            String newUserName = mUserName.getText().toString();
            String newStatus = mUserStatus.getText().toString();


            if (newUserName.matches("")) {
                Toast.makeText(SettingsActivity.this, "Enter a Username...", Toast.LENGTH_SHORT).show();
            } else if (newStatus.matches("")) {
                Toast.makeText(SettingsActivity.this, "Enter a Status...", Toast.LENGTH_SHORT).show();
            } else {
                HashMap<String, Object> userInfo = new HashMap();

                userInfo.put("Name", newUserName);
                userInfo.put("Status", newStatus);

                rootRef.child("Users").child(userID).updateChildren(userInfo)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else
                                Toast.makeText(SettingsActivity.this, "Error " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        retrieveUserInfo();

        profile_img.setOnClickListener(v -> choosePhotoFromGallery());
    }

    private void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mActivityAddNoteResultLauncher.launch(galleryIntent);
    }

    private void retrieveUserInfo() {
        rootRef.child("Users").child(userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("Name") && snapshot.hasChild("Status") && snapshot.hasChild("ImageUrl")) {
                            String userName = snapshot.child("Name").getValue().toString();
                            String userStatus = snapshot.child("Status").getValue().toString();
                            String userImage = snapshot.child("ImageUrl").getValue().toString();
                            // todo:
                            mUserName.setText(userName);
                            mUserStatus.setText(userStatus);
                            Picasso.get().load(userImage).into(profile_img);
                        } else if (snapshot.exists() && snapshot.hasChild("Name") && snapshot.hasChild("Status")) {
                            String userName = snapshot.child("Name").getValue().toString();
                            String userStatus = snapshot.child("Status").getValue().toString();
                            // todo:
                            mUserName.setText(userName);
                            mUserStatus.setText(userStatus);
                        } else {
                            Toast.makeText(SettingsActivity.this, "Complete Your Profile...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void init() {
        profile_img = findViewById(R.id.profile_image);
        mUserName = findViewById(R.id.user_name);
        mUserStatus = findViewById(R.id.user_status);
        mbtn = findViewById(R.id.update_profile_btn);
        userID = mAuth.getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);
    }
}