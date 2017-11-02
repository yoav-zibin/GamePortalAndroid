package com.nyuchess.gameportal.groups;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nyuchess.gameportal.R;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    public static final String TAG = "UsersActivity";

    private UserArrayAdapter mOnlineAdapter;
    private UserArrayAdapter mOfflineAdapter;

    private FirebaseAuth mAuth;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();
        List<User> onlineUsers = new ArrayList<>();
        final List<User> offlineUsers = new ArrayList<>();
        mOnlineAdapter = new UserArrayAdapter(this, onlineUsers);
        mOfflineAdapter = new UserArrayAdapter(this, offlineUsers);

        ListView onlineUsersList = findViewById(R.id.online_users_list);
        ListView offlineUsersList = findViewById(R.id.offne_users_list);
        onlineUsersList.setAdapter(mOnlineAdapter);
        offlineUsersList.setAdapter(mOfflineAdapter);

        DatabaseReference ref = mDatabase.getReference("gamePortal/recentlyConnected");
        Log.d(TAG, "users ref " + ref.toString());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");
                updateUserLists(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        onlineUsersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("PERSONID", mOnlineAdapter.getItem(position).getUid());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        offlineUsersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("PERSONID", mOfflineAdapter.getItem(position).getUid());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void updateUserLists(DataSnapshot dataSnapshot){
        // When the list of online users in the database changes, remake the list adapter here

        Log.d(TAG, "updateUserLists");
        mOnlineAdapter.clear();
        mOfflineAdapter.clear();
        Log.d(TAG, dataSnapshot.getKey());
        for (DataSnapshot user: dataSnapshot.getChildren()) {
            final String userid = (String) user.child("userId").getValue();
            if (!userid.equals(mAuth.getCurrentUser().getUid())) {
                DatabaseReference userRef = mDatabase.getReference("/users/" + userid + "/publicFields/");
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("displayName").getValue() == null) {
                            return;
                        }
                        String username = (String) dataSnapshot.child("displayName").getValue();
                        Object isConnectedValue = dataSnapshot.child("isConnected").getValue();
                        Log.d(TAG, username + " " + isConnectedValue);
                        if (isConnectedValue == null) {
                            return;
                        }
                        boolean isConnected = (boolean) isConnectedValue;
                        if (isConnected) {
                            Log.d(TAG, username + " online");
                            mOnlineAdapter.add(new User(username, userid));
                        } else {
                            Log.d(TAG, username + " offline");
                            mOfflineAdapter.add(new User(username, userid));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "DatabaseError:" + databaseError);
                    }
                });
            }
        }
    }
}
