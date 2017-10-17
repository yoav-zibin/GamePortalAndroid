package com.nyuchess.gameportal;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChatActivity";
    private FirebaseAuth mAuth;
    private FirebaseListAdapter<ChatMessage> adapter;
    private String chatID;

    public final int ADD_PEOPLE = 1;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        chatID = getIntent().getStringExtra("CHATID");

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        Log.d(TAG, FirebaseDatabase.getInstance().getReference("gamePortal/groups/" + chatID + "/messages").toString());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                Map<String, Object> chat = new HashMap<>();
                chat.put("senderUid", mAuth.getCurrentUser().getUid());
                chat.put("message", input.getText().toString());
                chat.put("timestamp", ServerValue.TIMESTAMP);
                FirebaseDatabase.getInstance()
                        .getReference("gamePortal/groups/" + chatID + "/messages")
                        .push()
                        .setValue(chat);

                // Clear the input
                input.setText("");
            }
        });

        displayChatMessages();

        findViewById(R.id.addPpl).setOnClickListener(this);
    }

    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference("gamePortal/groups/" + chatID + "/messages")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessage());
                messageUser.setText(model.getSenderUid());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getTimestamp()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.addPpl) {
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

                DatabaseReference ref = database.getReference("/gamePortal/groups/" + chatID + "/participants");
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

                            chats.child(chatID).child("participants").updateChildren(inChat);

                            DatabaseReference pba = database.getReference("/users/" + add);
                            Map<String, Object> chatInfo = new HashMap<>();
                            chatInfo.put("addedByUid", mAuth.getCurrentUser().getUid());
                            chatInfo.put("timestamp", ServerValue.TIMESTAMP);

                            pba.child("privateButAddable").child("groups").child(chatID).setValue(chatInfo);
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
