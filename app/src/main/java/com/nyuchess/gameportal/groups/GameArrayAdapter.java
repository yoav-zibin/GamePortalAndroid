package com.nyuchess.gameportal.groups;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nyuchess.gameportal.R;

import java.util.List;

/**
 * Created by Victor on 10/23/2017.
 */

public class GameArrayAdapter extends ArrayAdapter<GameArrayItem> {


    public GameArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<GameArrayItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        GameArrayItem game = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user, parent, false);
        }
        TextView username = (TextView) convertView.findViewById(R.id.username);
        username.setText(game.getGameName());
        return convertView;
    }
}
