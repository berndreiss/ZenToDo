package com.bdreiss.zentodo;
/*
 *   A custom ArrayAdapter<Entry> that creates rows with checkboxes that
 *   when checked remove the associated task.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class TaskListAdapter extends ArrayAdapter<Entry>{

    private ArrayList<Entry> entries;//list of entries (see Entry.java)

    Context context;

    private Data data;//database from which entries are derived (see Data.java)

    private class ViewHolder {//temporary view

        private LinearLayout linearLayout;
        private LinearLayout linearLayoutAlt;
        protected CheckBox checkBox;//Checkbox to remove entry
        private TextView task;//Description of the task
        private Button menu;
        private Button edit;
        private Button setDate;
        private Button setList;
        private Button back;
    }

    public TaskListAdapter(Context context, Data data){
        super(context, R.layout.row,data.getEntries());
        this.context = context;
        this.data = data;
        this.entries = data.getEntries();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row, null, true);

            holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.linear_layout);
            holder.linearLayoutAlt = (LinearLayout) convertView.findViewById(R.id.linear_layout_alt);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.task = (TextView) convertView.findViewById(R.id.task);
            holder.menu = (Button) convertView.findViewById(R.id.button_menu);

            holder.edit = (Button) convertView.findViewById(R.id.button_edit);
            holder.setDate = (Button) convertView.findViewById(R.id.button_calendar);
            holder.setList = (Button) convertView.findViewById(R.id.button_list);
            holder.back = (Button) convertView.findViewById(R.id.button_back);
            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        //set Text of the task
        holder.task.setText(entries.get(position).getTask());

        holder.checkBox.setChecked(false);

        holder.checkBox.setTag(position);

        //establish routine to remove task when checkbox is clicked
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View viewHolder = (View) holder.checkBox;
                Integer positionTask = (Integer)  holder.checkBox.getTag();
                data.remove(entries.get(positionTask).getID());//remove entry from dataset by ID
                notifyDataSetChanged();//update the adapter

            }
        });

        //setting "normal" row visible and active
        holder.linearLayout.setAlpha(1);
        holder.linearLayout.bringToFront();
        holder.linearLayoutAlt.setAlpha(0);
        holder.linearLayoutAlt.invalidate();


        //listener that changes to alternative row layout on click
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setting alternative row visible and active
                holder.linearLayoutAlt.bringToFront();
                holder.linearLayoutAlt.setAlpha(1);
                holder.linearLayout.setAlpha(0);
                holder.linearLayout.invalidate();

                holder.back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        notifyDataSetChanged();
                    }
                });

/*
                holder.setDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                holder.setList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                holder.back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });*/
            }
        });

        return convertView;
    }

    //returns the entry at specified position
    public Entry getEntry(int position){
        return entries.get(position);
    }
}