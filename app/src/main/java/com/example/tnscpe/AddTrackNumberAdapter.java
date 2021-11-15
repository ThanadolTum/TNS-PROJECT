package com.example.tnscpe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AddTrackNumberAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> newTrackNumber;

    public AddTrackNumberAdapter(Context context, ArrayList<String> newTrackNumber) {
        this.context = context;
        this.newTrackNumber = newTrackNumber;
    }

    @Override
    public int getCount() {
        return newTrackNumber.size();
    }

    @Override
    public Object getItem(int position) {
        return newTrackNumber.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.new_track_number,parent,false);
        TextView newTrackNumberView = view.findViewById(R.id.tracknumber);
        ImageView iconDeleteTrackNumber = view.findViewById(R.id.delete_track_number_icon);

        newTrackNumberView.setText(newTrackNumber.get(position));
        iconDeleteTrackNumber.setImageResource(R.drawable.ic_baseline_delete_24);
        return view;
    }
}
