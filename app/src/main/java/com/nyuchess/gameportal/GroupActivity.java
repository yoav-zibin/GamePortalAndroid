package com.nyuchess.gameportal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener {

    private String GROUP_ID;
    final int ADD_PEOPLE = 1;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        GROUP_ID = getIntent().getStringExtra("GROUP_ID");
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.chat_button).setOnClickListener(this);
        findViewById(R.id.matches_button).setOnClickListener(this);
        findViewById(R.id.addPpl).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.chat_button) {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("GROUP_ID", GROUP_ID);
            startActivity(intent);
        } else if(i == R.id.matches_button) {
            Intent intent = new Intent(this, MatchesListActivity.class);
            intent.putExtra("GROUP_ID", GROUP_ID);
            startActivity(intent);
//            Intent intent = new Intent(this, GameActivity.class);
//            intent.putExtra("GAME_ID", "-Kx61wq6VG5K5NNxbF5L");
        } else if(i == R.id.addPpl) {
            Intent intent = new Intent(this, UsersActivity.class);
            startActivityForResult(intent, ADD_PEOPLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ADD_PEOPLE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                final String add = data.getStringExtra("PERSONID");

                DatabaseReference ref = database.getReference("/gamePortal/groups/" + GROUP_ID + "/participants");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int i = 0;

                        for(int x = 0; x < 10; x ++) {
                            for(DataSnapshot user : dataSnapshot.getChildren()) {
                                if(Integer.parseInt(user.child("participantIndex").getValue().toString()) == i) {
                                    i++;
                                }
                            }
                        }

                        if(i <= 9) {

                            DatabaseReference chats = database.getReference("/gamePortal/groups");

                            Map<String, Object> index = new HashMap<>();
                            index.put("participantIndex", i);

                            Map<String, Object> inChat = new HashMap<>();
                            inChat.put(add, index);

                            chats.child(GROUP_ID).child("participants").updateChildren(inChat);

                            DatabaseReference pba = database.getReference("/users/" + add);
                            Map<String, Object> chatInfo = new HashMap<>();
                            chatInfo.put("addedByUid", mAuth.getCurrentUser().getUid());
                            chatInfo.put("timestamp", ServerValue.TIMESTAMP);

                            pba.child("privateButAddable").child("groups").child(GROUP_ID).setValue(chatInfo);
                            // Do something with the contact here (bigger example below)
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
