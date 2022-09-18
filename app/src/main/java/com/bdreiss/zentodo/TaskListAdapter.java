package com.bdreiss.zentodo;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class TaskListAdapter extends ArrayAdapter<Entry>{

    private ArrayList<Entry> entries;
    Context context;

    private Data data;

    private TextView textView; //REMOVE


    private class ViewHolder {

        protected CheckBox checkBox;
        private TextView task;

    }

    public TaskListAdapter(Context context, Data data, TextView textView){//REMOVE TextView
        super(context, R.layout.row,data.getEntries());
        this.textView = textView;//REMOVE
        this.context = context;
        this.data = data;
        this.entries = data.getEntries();
 }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row, null, true);

            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.task = (TextView) convertView.findViewById(R.id.task);

            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }


        holder.task.setText(entries.get(position).getTask());

        holder.checkBox.setChecked(false);

        holder.checkBox.setTag( position);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View tempview = (View) holder.checkBox;
                TextView tv = (TextView) tempview.findViewById(R.id.task);
                Integer position = (Integer)  holder.checkBox.getTag();
                data.remove(entries.get(position).getID());
                notifyDataSetChanged();

            }
        });

        return convertView;
    }

    public Entry getEntry(int position){
        return entries.get(position);
    }
}
