package com.nyuchess.gameportal.groups;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nyuchess.gameportal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "FriendsActivity";
    public static final int ADD_FRIENDS = 8;

    private UserArrayAdapter mOnlineAdapter;
    private UserArrayAdapter mOfflineAdapter;

    private FirebaseAuth mAuth;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Log.d(TAG, "onCreate");
        mAuth = FirebaseAuth.getInstance();
        List<User> onlineUsers = new ArrayList<>();
        final List<User> offlineUsers = new ArrayList<>();
        mOnlineAdapter = new UserArrayAdapter(this, onlineUsers);
        mOfflineAdapter = new UserArrayAdapter(this, offlineUsers);

        ListView onlineUsersList = findViewById(R.id.online_friends_list);
        ListView offlineUsersList = findViewById(R.id.offne_friends_list);
        Log.w(TAG, "" + onlineUsersList);
        Log.w(TAG, "" + offlineUsersList);
        onlineUsersList.setAdapter(mOnlineAdapter);
        offlineUsersList.setAdapter(mOfflineAdapter);

        final DatabaseReference databaseReference = mDatabase.getReference();
        final DatabaseReference onlineViewersCountRef = databaseReference.child("/users/" + mAuth.getCurrentUser().getUid() + "/privateFields/friends");
        onlineViewersCountRef.addValueEventListener(new ValueEventListener() {
            private ArrayList<String> online;
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                online = new ArrayList<>();
                Log.d(TAG, " NUMBER USERS " + dataSnapshot.getChildrenCount());
                final DataSnapshot snap = dataSnapshot;
                for (final DataSnapshot user: dataSnapshot.getChildren()) {
                    final String userid = (String) user.getKey();
                    if(!userid.equals(mAuth.getCurrentUser().getUid())) {
                        DatabaseReference isOnlineRef = databaseReference.child("/users/" + userid + "/publicFields");
                        isOnlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("isConnected").getValue() != null) {
                                    dataSnapshot.child("isConnected").getRef().addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.d(TAG, dataSnapshot.toString());
                                            updateUserLists(snap);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });

        findViewById(R.id.addFriends).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int v = view.getId();
        if(v == R.id.addFriends) {
            Intent intent = new Intent(this, UsersActivity.class);
            startActivityForResult(intent, ADD_FRIENDS);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ADD_FRIENDS) {
            Log.w(TAG, data.getStringExtra("PERSONID"));
            final DatabaseReference databaseReference = mDatabase.getReference();
            final DatabaseReference friends = databaseReference.child("/users/" + mAuth.getCurrentUser().getUid() + "/privateFields/friends");
            Map<String, Object> friend = new HashMap<>();
            friend.put(data.getStringExtra("PERSONID"), true);
            friends.updateChildren(friend);
        }
    }

    private void updateUserLists(final DataSnapshot dataSnapshot){
        // When the list of online users in the database changes, remake the list adapter here
            Log.d(TAG, "updateUserLists");
            mOnlineAdapter.clear();
            mOfflineAdapter.clear();
            Log.d(TAG, dataSnapshot.getKey());
            for (DataSnapshot user : dataSnapshot.getChildren()) {
                final String userid = (String) user.getKey();
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
                                User user = new User(username, userid);
                                boolean found = false;
                                for (int i = 0; i < mOnlineAdapter.getCount(); i++) {
                                    if (mOnlineAdapter.getItem(i).getUid().equals(user.getUid())) {
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    mOnlineAdapter.add(user);
                                }
                            } else {
                                Log.d(TAG, username + " offline");
                                User user = new User(username, userid);
                                boolean found = false;
                                for (int i = 0; i < mOfflineAdapter.getCount(); i++) {
                                    if (mOfflineAdapter.getItem(i).getUid().equals(user.getUid())) {
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    mOfflineAdapter.add(user);
                                }
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