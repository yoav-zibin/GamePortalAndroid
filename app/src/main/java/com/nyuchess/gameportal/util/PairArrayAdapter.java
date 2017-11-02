package com.nyuchess.gameportal.util;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nyuchess.gameportal.R;

import java.util.List;

/**
 * Created by Jordan on 11/2/2017.
 */

public class PairArrayAdapter<T, S> extends ArrayAdapter<Pair<T, S>> {


    public PairArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Pair<T, S>> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Pair<T, S> pair = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user, parent, false);
        }
        TextView username = (TextView) convertView.findViewById(R.id.username);
        username.setText(pair.first.toString());
        return convertView;
    }
}
