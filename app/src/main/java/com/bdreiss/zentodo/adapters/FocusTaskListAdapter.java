package com.bdreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class FocusTaskListAdapter extends TaskListAdapter{

    public FocusTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void setFocusListener(ViewHolder holder,int position){
        holder.focus.setOnClickListener(view12 -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get id
            data.setFocus(id, !entry.getFocus());//change state of focus in entry
            if (!entry.getFocus()){
                notifyDataSetChanged();            }
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    public DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, ViewHolder holder, int position){
        DatePickerDialog datePickerDialog;
        datePickerDialog= new DatePickerDialog(super.context, (view, year, month, day) -> {
            int date = year*10000+(month+1)*100+day;//Encode format "YYYYMMDD"
            data.editDue(entry.getId(), date);//Write back data

            data.setFocus(entry.getId(),false);
            notifyDataSetChanged();

        }, entryYear, entryMonth, entryDay);
        return datePickerDialog;
    }

}
