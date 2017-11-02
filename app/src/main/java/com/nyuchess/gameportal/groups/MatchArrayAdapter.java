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
 * Created by Jordan on 10/30/2017.
 */

public class MatchArrayAdapter extends ArrayAdapter<MatchArrayItem> {


    public MatchArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<MatchArrayItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        MatchArrayItem match = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user, parent, false);
        }
        TextView username = (TextView) convertView.findViewById(R.id.username);
        username.setText(match.getGameName());
        return convertView;
    }
}
