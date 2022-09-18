package com.bdreiss.zentodo;
/*
*   A custom ArrayAdapter<Entry> that creates rows with checkboxes that
*   when checked remove the associated task.
*/

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

    private ArrayList<Entry> entries;//list of entries (see Entry.java)

    Context context;

    private Data data;//database from which entries are derived (see Data.java)

    private class ViewHolder {//temporary view

        protected CheckBox checkBox;//Checkbox to remove entry
        private TextView task;//Description of the task

    }

    public TaskListAdapter(Context context, Data data, TextView textView){
        super(context, R.layout.row,data.getEntries());
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

        //set Text of the task
        holder.task.setText(entries.get(position).getTask());

        holder.checkBox.setChecked(false);

        holder.checkBox.setTag( position);

        //establish routine to remove task when checkbox is clicked
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View viewHolder = (View) holder.checkBox;
                TextView textView = (TextView) viewHolder.findViewById(R.id.task);
                Integer position = (Integer)  holder.checkBox.getTag();
                data.remove(entries.get(position).getID());//remove entry from dataset by ID
                notifyDataSetChanged();//update the adapter

            }
        });

        return convertView;
    }

    //returns the entry at specified position
    public Entry getEntry(int position){
        return entries.get(position);
    }
}
