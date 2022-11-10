package com.bdreiss.zentodo.adapters;

/*
 *       Extends TaskListAdapter but removes tasks when focus or date is changed.
 */

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

    //set Listener for the Focus Button in the menu, item is removed from adapter upon click
    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    @Override
    public void setFocusListener(ViewHolder holder,int position){
        holder.focus.setOnClickListener(view12 -> {

            //get entry
            Entry entry = entries.get(position);

            //get id
            int id = entry.getId();

            //write back change to data
            data.setFocus(id, false);

            //remove from adapter
            entries.remove(position);

            //notify adapter
            notifyDataSetChanged();
        });

    }

    //return DatePickerDialog that writes back to data and removes item from adapter
    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    public DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, ViewHolder holder, int position){

        DatePickerDialog datePickerDialog;

        datePickerDialog= new DatePickerDialog(super.context, (view, year, month, day) -> {

            //encode format "YYYYMMDD"
            int date = year*10000+(month+1)*100+day;

            //write back to data
            data.editDue(entry.getId(), date);

            //also set focus to false in data
            data.setFocus(entry.getId(),false);

            //remove from adapter
            entries.remove(position);

            //notify adapter
            notifyDataSetChanged();

        }, entryYear, entryMonth, entryDay);

        //return Dialog
        return datePickerDialog;
    }

}
