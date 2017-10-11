package com.nyuchess.gameportal;

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private FirebaseAuth mAuth;
    private FirebaseListAdapter<ChatMessage> adapter;
    private String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        chatID = getIntent().getStringExtra("CHATID");

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        Log.d(TAG, FirebaseDatabase.getInstance().getReference("chats/" + chatID + "/messages").toString());

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
                        .getReference("chats/" + chatID + "/messages")
                        .push()
                        .setValue(chat);

                // Clear the input
                input.setText("");
            }
        });

        displayChatMessages();
    }

    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference("chats/" + chatID + "/messages")) {
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
}
