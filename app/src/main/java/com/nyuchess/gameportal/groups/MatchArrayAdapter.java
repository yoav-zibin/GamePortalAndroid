package com.nyuchess.gameportal.groups;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
//        convertView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 120));
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        username.setLayoutParams(params);
//        username.setPadding(10, 30, 10, 30);
//        username.setGravity(Gravity.CENTER);
        username.setText(match.getGameName());
        ImageView imageView = convertView.findViewById(R.id.avatar);
        Bitmap avatar = match.getImage();
        if (avatar != null){
            imageView.setImageBitmap(Bitmap.createScaledBitmap(match.getImage(),
                    150, 150, false));
        }
        return convertView;
    }
}
