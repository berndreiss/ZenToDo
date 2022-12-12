package com.bdreiss.zentodo.adapters.listeners;

import android.view.View;

import com.bdreiss.zentodo.adapters.TaskListAdapter;

/*
 *
 *  Implements listener for the Button to return from editing recurrence of a task.
 *
 */


public class BackRecurrenceListener extends BasicListener implements View.OnClickListener{

    public BackRecurrenceListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @Override
    public void onClick(View v){

        //get id for manipulation in data
        int id = adapter.entries.get(position).getId(); //get id

        //number of repeats
        String interval = holder.editTextRecurrence.getText().toString();

        //the same number but as Integer
        int intervalInt;

        //if editText is empty number is set to 0, assign content otherwise
        if (interval.equals("")){
            intervalInt = 0;
        } else {
            intervalInt = Integer.parseInt(interval);
        }

        //String that will be written back
        String recurrence = "";

        //if editText was empty or value=0 then recurrence is set to null, otherwise number and interval are written back
        if (intervalInt == 0){
            adapter.data.editRecurrence(id, null);
            adapter.entries.get(position).setRecurrence(null);
        }
        else{

            //add spinner values first character as lower case (d/w/m/y)
            recurrence += Character.toLowerCase(holder.spinnerRecurrence.getSelectedItem().toString().charAt(0));

            //add number of editText
            recurrence += interval;

            //write back
            adapter.data.editRecurrence(id,recurrence);
            adapter.entries.get(position).setRecurrence(recurrence);

        }

        //change color of recurrence Button to mark if recurrence is set
        adapter.markSet(holder,adapter.entries.get(position));

        //notify adapter
        adapter.notifyItemChanged(position);

        //return to original row layout
        adapter.setOriginal(holder);


    }

}
