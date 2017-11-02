package com.nyuchess.gameportal.groups;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.nyuchess.gameportal.R;
import com.nyuchess.gameportal.gameplay.GameActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                String quick = getIntent().getStringExtra("QUICKSTART");
                if(quick != null && quick.equals("true")) {
                    quickstart(v, position, true);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("PERSONID", mOnlineAdapter.getItem(position).getUid());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });

        offlineUsersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String quick = getIntent().getStringExtra("QUICKSTART");
                if(quick != null && quick.equals("true")) {
                    quickstart(v, position, false);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("PERSONID", mOfflineAdapter.getItem(position).getUid());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private void quickstart(View v, int position, boolean online) {
        final Intent intent = new Intent(v.getContext(), GameActivity.class);
        final String GAME_ID = getIntent().getStringExtra("GAME_ID");
        final String PERSON_ID = (online ? mOnlineAdapter.getItem(position).getUid() : mOfflineAdapter.getItem(position).getUid());
        final String pName = (online ? mOnlineAdapter.getItem(position).getDisplayName() : mOfflineAdapter.getItem(position).getDisplayName());

        final String gName = getIntent().getStringExtra("GAME_NAME");

        DatabaseReference groups = mDatabase.getReference("/gamePortal/groups");

        Map<String, Object> index0 = new HashMap<>();
        index0.put("participantIndex", 0);

        Map<String, Object> index1 = new HashMap<>();
        index1.put("participantIndex", 1);

        Map<String, Object> inGroup = new HashMap<>();
        inGroup.put(mAuth.getCurrentUser().getUid(), index0);
        inGroup.put(PERSON_ID, index1);

        Map<String, Object> chat = new HashMap<>();
        chat.put("participants", inGroup);
        chat.put("groupName", mAuth.getCurrentUser().getDisplayName() + " vs " + pName + " at " + gName);
        chat.put("createdOn", ServerValue.TIMESTAMP);

        final String GROUP_ID = groups.push().toString().replace(groups.getRef().toString() + "/", "");
        groups.child(GROUP_ID).setValue(chat);

        Log.d(TAG, "SETTING VALUE");
        Log.d(TAG, "SETTING VALUE");
        Log.d(TAG, "SETTING VALUE");
        Log.d(TAG, "SETTING VALUE");

        DatabaseReference pba = mDatabase.getReference("/users/" + mAuth.getCurrentUser().getUid());
        Map<String, Object> chatInfo = new HashMap<>();
        chatInfo.put("addedByUid", mAuth.getCurrentUser().getUid());
        chatInfo.put("timestamp", ServerValue.TIMESTAMP);

        pba.child("privateButAddable").child("groups").child(GROUP_ID).setValue(chatInfo);

        DatabaseReference pba2 = mDatabase.getReference("/users/" + PERSON_ID);
        Map<String, Object> chatInfo2 = new HashMap<>();
        chatInfo2.put("addedByUid", mAuth.getCurrentUser().getUid());
        chatInfo2.put("timestamp", ServerValue.TIMESTAMP);

        pba2.child("privateButAddable").child("groups").child(GROUP_ID).setValue(chatInfo2);

        // Create match for group

        DatabaseReference gameRef = mDatabase.getReference("/gameBuilder/gameSpecs").child(GAME_ID);
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString());
                DatabaseReference ref = mDatabase.getReference("/gamePortal/groups/" + GROUP_ID + "/matches");

                Map<String, Object> match = new HashMap<>();
                match.put("gameSpecId", GAME_ID);
                match.put("createdOn", ServerValue.TIMESTAMP);
                match.put("lastUpdatedOn", ServerValue.TIMESTAMP);

                Map<String, Object> pieces = new HashMap<>();
                for(DataSnapshot piece: dataSnapshot.child("pieces").getChildren()) {
                    Log.d(TAG, piece.toString());

                    Map<String, Object> curState = new HashMap<>();

                    curState.put("x", piece.child("initialState").child("x").getValue());
                    curState.put("y", piece.child("initialState").child("y").getValue());
                    curState.put("zDepth", piece.child("initialState").child("zDepth").getValue());
                    curState.put("currentImageIndex", piece.child("initialState").child("currentImageIndex").getValue());

                    Map<String, Object> state = new HashMap<>();

                    state.put("currentState", curState);

                    pieces.put(piece.getKey(), state);
                }
                Log.d(TAG, pieces.toString());
                match.put("pieces", pieces);

                final String MATCH_ID = ref.push().toString().replace(ref.getRef().toString() + "/", "");
                ref.child(MATCH_ID).setValue(match);

                intent.putExtra("GAME_ID", GAME_ID);
                intent.putExtra("MATCH_ID", MATCH_ID);
                intent.putExtra("GROUP_ID", GROUP_ID);
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                            User user = new User(username, userid);
                            boolean found = false;
                            for(int i = 0; i < mOnlineAdapter.getCount(); i++) {
                                if(mOnlineAdapter.getItem(i).getUid().equals(user.getUid())) {
                                    found = true;
                                }
                            }
                            if(!found) {
                                mOnlineAdapter.add(user);
                            }
                        } else {
                            Log.d(TAG, username + " offline");
                            User user = new User(username, userid);
                            boolean found = false;
                            for(int i = 0; i < mOfflineAdapter.getCount(); i++) {
                                if(mOfflineAdapter.getItem(i).getUid().equals(user.getUid())) {
                                    found = true;
                                }
                            }
                            if(!found) {
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
