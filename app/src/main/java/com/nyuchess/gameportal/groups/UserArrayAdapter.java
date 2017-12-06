package com.nyuchess.gameportal.groups;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nyuchess.gameportal.R;

import java.util.List;

/**
 * Created by Jordan on 10/12/2017.
 */

public class UserArrayAdapter extends ArrayAdapter<User> {

    public UserArrayAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user, parent, false);
        }
        convertView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 120));
        TextView username = (TextView) convertView.findViewById(R.id.username);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        username.setLayoutParams(params);
        username.setPadding(10, 30, 10, 30);
        username.setGravity(Gravity.CENTER);
        username.setText(user.getDisplayName());
        return convertView;
    }
}
