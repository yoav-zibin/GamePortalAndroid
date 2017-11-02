package com.nyuchess.gameportal.chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.nyuchess.gameportal.groups.GroupActivity;
import com.nyuchess.gameportal.util.PairArrayAdapter;
import com.nyuchess.gameportal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsListActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "ChatsList";

    private PairArrayAdapter<String, String> mChatsAdapter;
    private FirebaseAuth mAuth;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_chats_list);

        mAuth = FirebaseAuth.getInstance();

        List<Pair<String, String>> availableChats = new ArrayList<>();
        mChatsAdapter = new PairArrayAdapter<>(this, R.layout.chat, availableChats);
        mChatsAdapter.clear();
        ListView availableChatsList = findViewById(R.id.chats_list);
        availableChatsList.setAdapter(mChatsAdapter);

        DatabaseReference ref = mDatabase.getReference("/users/" + mAuth.getCurrentUser().getUid() + "/privateButAddable/groups");


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChatsAdapter.clear();
                Log.d(TAG, "onDataChange");
                for(DataSnapshot chat: dataSnapshot.getChildren()) {
                    Log.d(TAG, chat.getKey());
                    DatabaseReference ref2 = mDatabase.getReference("gamePortal/groups/" + chat.getKey());
                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot group) {
                            Log.d(TAG, group.child("groupName").toString());
                            Log.d(TAG, group.getKey());
                            Pair<String, String> inGroup = new Pair<>(group.child("groupName").getValue().toString(), group.getKey());
                            boolean found = false;
                            for(int i = 0; i < mChatsAdapter.getCount(); i++) {
                                if(mChatsAdapter.getItem(i).first.equals(inGroup.first)
                                        && mChatsAdapter.getItem(i).second.equals(inGroup.second)) {
                                    found = true;
                                }
                            }
                            if(!found) {
                                mChatsAdapter.add(inGroup);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        findViewById(R.id.newChat).setOnClickListener(this);

        // Wahhhh cuz Android cries if this is in overriden onclick
        ListView list = (ListView)findViewById(R.id.chats_list);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(v.getContext(), GroupActivity.class);
                intent.putExtra("GROUP_ID", mChatsAdapter.getItem(position).second);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.newChat) {
            //Add to global chat

            EditText groupEditText = (EditText) findViewById(R.id.chatName);
            String groupName = groupEditText.getText().toString();
            if (groupName.matches("")) {
                Toast.makeText(this, "You did not enter a group name", Toast.LENGTH_SHORT).show();
                return;
            } else {

                DatabaseReference chats = mDatabase.getReference("/gamePortal/groups");

                Map<String, Object> index = new HashMap<>();
                index.put("participantIndex", 0);

                Map<String, Object> inChat = new HashMap<>();
                inChat.put(mAuth.getCurrentUser().getUid(), index);

                EditText edit = (EditText) findViewById(R.id.chatName);
                String name = edit.getText().toString(); //gets you the contents of edit text

                Map<String, Object> chat = new HashMap<>();
                chat.put("participants", inChat);
                chat.put("groupName", name);
                chat.put("createdOn", ServerValue.TIMESTAMP);

                String push = chats.push().toString().replace(chats.getRef().toString() + "/", "");
                chats.child(push).setValue(chat);
                mChatsAdapter.add(new Pair<>(name, push));

                DatabaseReference pba = mDatabase.getReference("/users/" + mAuth.getCurrentUser().getUid());
                Map<String, Object> chatInfo = new HashMap<>();
                chatInfo.put("addedByUid", mAuth.getCurrentUser().getUid());
                chatInfo.put("timestamp", ServerValue.TIMESTAMP);

                pba.child("privateButAddable").child("groups").child(push).setValue(chatInfo);
                groupEditText.setText("");
            }
        }
    }
}
