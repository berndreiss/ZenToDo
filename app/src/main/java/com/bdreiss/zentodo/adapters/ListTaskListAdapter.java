package com.bdreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class ListTaskListAdapter extends TaskListAdapter{

    public ListTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void setCheckBoxListener(ViewHolder holder, int position){
        holder.checkBox.setOnClickListener(view -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get ID
            data.remove(id);
            entries.remove(position);

            notifyItemRemoved(position);
            notifyDataSetChanged();

        });

    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void setBackListListener(ViewHolder holder, int position){

        holder.backList.setOnClickListener(view161 -> {
            int id = entries.get(position).getId();//Get id of task
            String list = holder.autoCompleteList.getText().toString();//get list name

            //set to no list if AutoComplete is empty
            if (list.trim().isEmpty()) {
                data.editList(id, null);//reset to no list
                setBackListListener(holder,position);

            } else {
                if (!list.equals(entries.get(position).getList())) {
                    data.editList(id, list);//write back otherwise
                    entries.remove(position);
                }
            }
            notifyDataSetChanged();
        });
    }
}
