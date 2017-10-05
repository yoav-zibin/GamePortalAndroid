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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        List<String> usernames = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.user, usernames);

        final ListView onlineUsersList = findViewById(R.id.online_users_list);
        onlineUsersList.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

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
                    String username = user.child("public_fields").child("displayName")
                            .getValue().toString();

                    Log.d(TAG, username);
                    adapter.add(username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }
}
