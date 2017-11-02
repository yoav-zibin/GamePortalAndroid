package com.nyuchess.gameportal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchesListActivity extends AppCompatActivity implements View.OnClickListener {

    private String GROUP_ID;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    final int ADD_MATCH = 3;

    private final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    private MatchArrayAdapter mMatchesAdapter;

    public static final String TAG = "MatchesListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches_list);

        GROUP_ID = getIntent().getStringExtra("GROUP_ID");
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.new_match).setOnClickListener(this);

        List<MatchArrayItem> availableMatches = new ArrayList<>();
        mMatchesAdapter = new MatchArrayAdapter(this, 0, availableMatches);
        mMatchesAdapter.clear();
        ListView availableMatchesList = findViewById(R.id.list_of_matches);
        availableMatchesList.setAdapter(mMatchesAdapter);

        DatabaseReference ref = mDatabase.getReference("gamePortal/groups/" + GROUP_ID + "/matches");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMatchesAdapter.clear();
                Log.d(TAG, "onDataChange");
                for(final DataSnapshot match: dataSnapshot.getChildren()) {
                    Log.d(TAG, match.getKey());
                    final String gameSpecId = match.child("gameSpecId").getValue().toString();
                    mDatabase.getReference("gameBuilder/gameSpecs/" + gameSpecId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String gamename = dataSnapshot.child("gameName").getValue().toString();
                            MatchArrayItem matchInList = new MatchArrayItem(gamename, gameSpecId, match.getKey());
                            boolean found = false;
                            for(int i = 0; i < mMatchesAdapter.getCount(); i++) {
                                if(mMatchesAdapter.getItem(i).getGameName().equals(matchInList.getGameName())
                                        && mMatchesAdapter.getItem(i).getgameId().equals(matchInList.getgameId())
                                        && mMatchesAdapter.getItem(i).getMatchId().equals(matchInList.getMatchId())) {
                                    found = true;
                                }
                            }
                            if(!found) {
                                mMatchesAdapter.add(matchInList);
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

        // Wahhhh cuz Android cries if this is in overriden onclick
        availableMatchesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(v.getContext(), GameActivity.class);
                Log.d(TAG, mMatchesAdapter.getItem(position).getMatchId());
                intent.putExtra("GAME_ID", mMatchesAdapter.getItem(position).getgameId());
                intent.putExtra("MATCH_ID", mMatchesAdapter.getItem(position).getMatchId());
                intent.putExtra("GROUP_ID", GROUP_ID);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        int v = view.getId();
        if(v == R.id.new_match) {
            Intent intent = new Intent(this, MatchActivity.class);
            startActivityForResult(intent, ADD_MATCH);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ADD_MATCH) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                final String GAME_ID = data.getStringExtra("GAME_ID");

                DatabaseReference userRef = mDatabase.getReference("/gameBuilder/gameSpecs").child(GAME_ID);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, dataSnapshot.toString());
                        DatabaseReference ref = database.getReference("/gamePortal/groups/" + GROUP_ID + "/matches");

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
                        ref.push().setValue(match);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
