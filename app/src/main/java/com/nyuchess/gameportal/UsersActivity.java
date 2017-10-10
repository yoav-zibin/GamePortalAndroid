package com.nyuchess.gameportal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    public static final String TAG = "UsersActivity";

    private ArrayAdapter<String> mOnlineAdapter;
    private ArrayAdapter<String> mOfflineAdapter;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_users);

        List<String> onlineUsers = new ArrayList<>();
        List<String> offlineUsers = new ArrayList<>();
        mOnlineAdapter = new ArrayAdapter<>(this, R.layout.user, onlineUsers);
        mOfflineAdapter = new ArrayAdapter<>(this, R.layout.user, offlineUsers);

        ListView onlineUsersList = findViewById(R.id.online_users_list);
        ListView offlineUsersList = findViewById(R.id.offne_users_list);
        onlineUsersList.setAdapter(mOnlineAdapter);
        offlineUsersList.setAdapter(mOfflineAdapter);

        DatabaseReference ref = mDatabase.getReference("recentlyConnected");
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
    }

    private void updateUserLists(DataSnapshot dataSnapshot){
        // When the list of online users in the database changes, remake the list adapter here

        //TODO: getting a permission denied error from firebase, try checking google-services.json
        Log.d(TAG, "updateUserLists");
        mOnlineAdapter.clear();
        mOfflineAdapter.clear();
        Log.d(TAG, dataSnapshot.getKey());
        for (DataSnapshot user: dataSnapshot.getChildren()){
            String userid = (String) user.child("uid").getValue();
            DatabaseReference userRef = mDatabase.getReference("/users/" + userid + "/publicFields/");
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("displayName").getValue() == null){
                        return;
                    }
                    String username = (String) dataSnapshot.child("displayName").getValue();
                    Object isConnectedValue = dataSnapshot.child("isConnected").getValue();
                    Log.d(TAG, username + " " + isConnectedValue);
                    if (isConnectedValue == null){
                        return;
                    }
                    boolean isConnected = (boolean) isConnectedValue;
                    if (isConnected){
                        Log.d(TAG, username + " online");
                        mOnlineAdapter.add(username);
                    }
                    else{
                        Log.d(TAG, username + " offline");
                        mOfflineAdapter.add(username);
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
