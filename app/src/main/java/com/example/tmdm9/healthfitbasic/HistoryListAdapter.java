package com.example.tmdm9.healthfitbasic;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

class HistoryListAdapter extends ArrayAdapter<String> {
    private List<String> dates;
    HistoryListAdapter(Context context, int resource, List<String> objects, List<String> dates) {
        super(context,resource,objects);
        this.dates = dates;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if(convertView==null)
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(
                    R.layout.steps_item,parent,false);

        TextView day=convertView.findViewById(R.id.Day);
        TextView steps=convertView.findViewById(R.id.Steps);

        day.setText(dates.get(position));
        steps.setText(String.format("%s: %s", "Steps", getItem(position)));

        return convertView;
    }
}
