package com.jedi.platicar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jedi.platicar.Utils.VPadapter;
import com.jedi.platicar.fragments.ChatFragment;
import com.jedi.platicar.fragments.FriendsFragment;
import com.jedi.platicar.fragments.GroupsFragment;
import com.jedi.platicar.fragments.RequestFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    FirebaseAuth mAuth;
    private DatabaseReference reference;
    private FirebaseUser currentUser = null;

    MaterialToolbar toolbar;

    ChatFragment chatfragment;
    FriendsFragment friendsfragment;
    GroupsFragment groupsfragment;
    RequestFragment requestfragment;

    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.tool_bar);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        chatfragment = new ChatFragment();
        friendsfragment = new FriendsFragment();
        groupsfragment = new GroupsFragment();
        requestfragment = new RequestFragment();

        tabLayout.setupWithViewPager(viewPager);

        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.drop_dowm_options);

        VPadapter vpAdapter = new VPadapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(chatfragment, "CHATS");
        vpAdapter.addFragment(groupsfragment, "GROUPS");
        vpAdapter.addFragment(friendsfragment, "FRIENDS");
        vpAdapter.addFragment(requestfragment, "REQUESTS");
        viewPager.setAdapter(vpAdapter);

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id._find_friends:
                    startActivity(new Intent(MainActivity.this, FindFriendsActivity.class));
                    return true;
                case R.id._settings:
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return true;
                case R.id._signout:
                    updateUserStatus("offline");
                    clearToken(mAuth.getCurrentUser().getUid());
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
            }
            return false;
        });
    }

    private void clearToken(String currUid) {
        reference.child("tokens")
                .child(currUid).removeValue();
    }

    private void requestNewGrp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("New Group");
        EditText grpName = new EditText(MainActivity.this);
        grpName.setHint("Jedi_24");
        builder.setView(grpName);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String newGrpName = grpName.getText().toString();
            if (newGrpName.matches("")) {
                Toast.makeText(MainActivity.this, "Add a Name", Toast.LENGTH_SHORT).show();
            } else {
                createNewGrp(newGrpName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createNewGrp(String newGrpName) {
        reference.child("Groups")
                .child(newGrpName).setValue("")
                .addOnCompleteListener(task -> {
                });
    }

    private void verifyUser() {
        String currUserId = mAuth.getCurrentUser().getUid();

        reference.child("Users").child(currUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(snapshot.child("Name").exists())) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            updateUserStatus("online");
            verifyUser();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentUser = mAuth.getCurrentUser();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    void updateUserStatus(String onlineState) {
        String saveCurrDate, saveCurrTime;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd, yyyy");
        saveCurrDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        saveCurrTime = timeFormat.format(calendar.getTime());

        HashMap<String, Object> onlineStatus = new HashMap<>();
        onlineStatus.put("date", saveCurrDate);
        onlineStatus.put("time", saveCurrTime);
        onlineStatus.put("state", onlineState);

        reference.child("Users").child(currentUser.getUid())
                .child("userStatus")
                .updateChildren(onlineStatus);

    }
}