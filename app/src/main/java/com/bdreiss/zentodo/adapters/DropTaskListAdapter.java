package com.bdreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class DropTaskListAdapter extends TaskListAdapter{

    public DropTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void setFocusListener(ViewHolder holder,int position){
        holder.focus.setOnClickListener(view12 -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get id
            data.setFocus(id, !entry.getFocus());//change state of focus in entry

            //change dropped in entry to false
            data.setDropped(id, false);//change to false
            entries.remove(position);
            notifyItemRemoved(position);
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
                //remove task from ListView is mode.equals("list")
                setOriginal(holder);
                setBackListListener(holder, position);

            } else {
                data.editList(id, list);//write back otherwise
                data.setDropped(id, false);//set dropped to false
                entries.remove(position);
                notifyItemRemoved(position);
            }

        });
    }

    @Override
    public DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, ViewHolder holder, int position){
        DatePickerDialog datePickerDialog;
        datePickerDialog= new DatePickerDialog(context, (view, year, month, day) -> {
            int date = year*10000+(month+1)*100+day;//Encode format "YYYYMMDD"
            data.editDue(entry.getId(), date);//Write back data
            data.setDropped(entry.getId(), false);
            entries.remove(position);
            notifyItemRemoved(position);

        }, entryYear, entryMonth, entryDay);
        return datePickerDialog;
    }

    public void add(String task){
        Entry entry = data.add(task);
        entries.add(entry);
        notifyItemInserted(entries.size());

    }

}
