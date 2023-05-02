package com.bdreiss.zentodo.adapters;

/*
*       TaskListAdapter that removes tasks from RecyclerView if task is being sent to Focus or
*       list or date is set
 */

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.bdreiss.zentodo.adapters.listeners.SetDateListener;
import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.time.LocalDate;

public class DropTaskListAdapter extends TaskListAdapter{

    public DropTaskListAdapter(Context context, Data data){
        super(context, data, data.getDropped());
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        //Remove task from View if task is being sent to Focus
        holder.focus.setOnClickListener(v -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get id
            data.setFocus(id, !entry.getFocus());//change state of focus in entry

            //change dropped in entry to false
            data.setDropped(id, false);//change to false
            entries.remove(position);
            notifyDataSetChanged();
        });

        //Remove task from View if list is set
        holder.backList.setOnClickListener(view161 -> {
            //Get id of task
            int id = entries.get(position).getId();

            //get new list name
            String list = holder.autoCompleteList.getText().toString();

            //set list if AutoComplete is not empty, write back and notify adapter
            if (!list.trim().isEmpty()) {

                //write back
                entries.get(position).setListPosition(data.editList(id, list));

                data.setDropped(id, false);//set dropped to false

                entries.remove(position);

                notifyDataSetChanged();

            }

            //return to original row layout
            setOriginal(holder);

        });

        //Remove task from View if date is set
        holder.setDate.setOnClickListener(new SetDateListener(this, holder,position){

            @Override
            public DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, ViewHolder holder, int position){
                DatePickerDialog datePickerDialog;
                datePickerDialog= new DatePickerDialog(context, (view, year, month, day) -> {
                    LocalDate date = LocalDate.of(year,month,day);

                    //for some strange reason the month is returned -1 in the DatePickerDialog
                    date = date.plusMonths(1);

                    data.editReminderDate(entry.getId(), date);//Write back data
                    data.setDropped(entry.getId(), false);
                    entries.remove(position);
                    notifyDataSetChanged();

                }, entryYear, entryMonth, entryDay);
                return datePickerDialog;

            }
        });
    }


    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    public void add(String task){
        Entry entry = data.add(task);
        entries.add(entry);
        notifyDataSetChanged();

    }

    @SuppressLint("NotifyDataSetChanged")
    public void reset(){
        //clear ArrayList for Drop, add current tasks from data and notify adapter (in case they have been altered in another layout)
        entries.clear();
        entries.addAll(data.getDropped());
        notifyDataSetChanged();

    }

}
