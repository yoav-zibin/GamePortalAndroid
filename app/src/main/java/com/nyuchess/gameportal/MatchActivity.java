package com.nyuchess.gameportal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MatchActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "MatchActivity";

    public static final int FIGHT_PEOPLE = 2;

    private ArrayAdapter<String> mGamesAdapter;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_match);

        mAuth = FirebaseAuth.getInstance();

        List<String> availableGames = new ArrayList<>();
        mGamesAdapter = new ArrayAdapter<>(this, R.layout.chat, availableGames);
        mGamesAdapter.clear();
        mGamesAdapter.add("HEY");
        ListView availableGamesList = findViewById(R.id.games_list);
        availableGamesList.setAdapter(mGamesAdapter);

        DatabaseReference ref = mDatabase.getReference("/gameSpecs/");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGamesAdapter.clear();
                Log.d(TAG, "onDataChange");
                for(DataSnapshot chat: dataSnapshot.getChildren()) {
                    mGamesAdapter.add(chat.getKey().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Wahhhh cuz Android cries if this is in overriden onclick
        ListView list = (ListView)findViewById(R.id.games_list);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(v.getContext(), UsersActivity.class);
                startActivityForResult(intent, FIGHT_PEOPLE);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == FIGHT_PEOPLE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                Log.d(TAG, data.getStringExtra("PERSONID"));
                Intent intent = new Intent(this, GameActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
    }
}
