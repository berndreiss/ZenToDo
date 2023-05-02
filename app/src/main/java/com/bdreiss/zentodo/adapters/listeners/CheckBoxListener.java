package com.bdreiss.zentodo.adapters.listeners;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.bdreiss.zentodo.adapters.TaskListAdapter;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.time.LocalDate;

/*
 *
 *  Implements listener for checkBox of a task.
 *
 *  Removes task if it is not recurring. Otherwise resets the reminder date adding the given interval.
 *
 */


public class CheckBoxListener extends BasicListener implements View.OnClickListener{

    public CheckBoxListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View v){
        //current entry
        Entry entry = adapter.entries.get(position);

        //get ID for manipulation in data
        int id = entry.getId();

        //see if item is recurring
        boolean recurring = entry.getRecurrence()!=null;

        //if recurring do not remove but set new reminder date, otherwise remove from data
        if (recurring) {
            //calculate new reminder date and write to data and entries
            adapter.entries.get(position).setReminderDate(adapter.data.setRecurring(id, LocalDate.now()));

            //reset focus in data and entries
            adapter.data.setFocus(id,false);
            adapter.entries.get(position).setFocus(false);

            //reset dropped in data and entries
            adapter.data.setDropped(id,false);
            adapter.entries.get(position).setDropped(false);

            adapter.notifyItemChanged(position);
        } else {
            adapter.data.remove(id);
        }

        //remove from adapter and notify
        adapter.entries.remove(position);
        adapter.notifyDataSetChanged();

    }

}
