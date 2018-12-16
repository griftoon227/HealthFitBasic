//Author: Griffin Flaxman
package com.example.tmdm9.healthfitbasic;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

class HistoryListAdapter extends ArrayAdapter<String> {
    //the constructor for a list adapter with the activity it is being called from, the resource used for each list
    //element, the list of dates, as well as the list of step data
    private List<String> dates;
    private int resource;
    HistoryListAdapter(Context context, int resource, List<String> objects, List<String> dates) {
        super(context,resource,objects);
        this.dates = dates;
        this.resource = resource;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        //if the convertView is nonexistent, create and inflate it
        if(convertView==null)
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(
                    resource,parent,false);

        TextView day=convertView.findViewById(R.id.Day);
        TextView steps=convertView.findViewById(R.id.Steps);

        day.setText(dates.get(position));
        steps.setText(String.format("%s: %s", "Steps", getItem(position)));

        return convertView;
    }
}
