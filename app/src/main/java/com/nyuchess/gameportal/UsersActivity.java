package com.nyuchess.gameportal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    public static final String TAG = "UsersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        List<String> usernames = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.user, usernames);

        final ListView onlineUsersList = findViewById(R.id.online_users_list);
        onlineUsersList.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                Log.d(TAG, dataSnapshot.getKey());
                for (DataSnapshot user: dataSnapshot.getChildren()){
                    DataSnapshot isConnected = user.child("public_fields").child("isConnected");
                    Log.d(TAG, isConnected.toString());
                    if (isConnected.getValue() == null || isConnected.getValue().equals("False")){
                        continue;
                    }

                    Log.d(TAG, user.getKey());
                    DataSnapshot publicFields = user.child("public_fields");
                    Log.d(TAG, publicFields.getKey());
                    DataSnapshot displayName = publicFields.child("displayName");
                    Log.d(TAG, displayName.getKey());
                    adapter.add(displayName.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


//        FirebaseListAdapter<User> adapter = new FirebaseListAdapter<User>(this, User.class,
//                R.layout.user, FirebaseDatabase.getInstance().getReference("/users")) {
//            @Override
//            protected void populateView(View v, User user, int position) {
//                Log.d(TAG, user.toString());
//                Log.d(TAG, user.getDisplayName());
//                // Get references to the views of user.xml
//                TextView usernameText = v.findViewById(R.id.username);
//
//                // Set their text
//                usernameText.setText(user.getDisplayName());
//            }
//        };

        onlineUsersList.setAdapter(adapter);

//        Firebase ref = new Firebase("");
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                ArrayList<User> onlineUsers = new ArrayList<>():
//                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                    //Getting the data from snapshot
//                    User user = postSnapshot.getValue(User.class);
//
//                    //add person to your list
//                    onlineUsers.add(user);
//                    //create a list view, and add the apapter, passing in your list
//
//                }
//
//                onlineUsersList.setAdapter(onlineUsers);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getMessage());
//            }
//        });

    }
}
