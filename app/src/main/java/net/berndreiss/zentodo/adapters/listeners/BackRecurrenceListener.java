package net.berndreiss.zentodo.adapters.listeners;

import android.annotation.SuppressLint;
import android.view.View;

import net.berndreiss.zentodo.adapters.DropTaskListAdapter;
import net.berndreiss.zentodo.adapters.PickTaskListAdapter;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Entry;

import java.time.LocalDate;

/**
 *
 *  Implements listener for the Button to return from editing recurrence of a task.
 *
 */
public class BackRecurrenceListener extends BasicListener implements View.OnClickListener{

    public BackRecurrenceListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View v){

        //get id for manipulation in data
        Entry entry = adapter.entries.get(position); //get id

        //number of repeats
        String interval = holder.editTextRecurrence.getText().toString();

        //the same number but as Integer
        int intervalInt;

        //if editText is empty number is set to 0, assign content otherwise
        if (interval.isEmpty()){
            intervalInt = 0;
        } else {
            intervalInt = Integer.parseInt(interval);
        }

        //if editText was empty or value=0 then recurrence is set to null, otherwise number and interval are written back
        if (intervalInt == 0){
            DataManager.editRecurrence(adapter.context, entry, null);
        }
        else{
            //String that will be written back
            String recurrence = "";

            //add spinner values first character as lower case (d/w/m/y)
            recurrence += Character.toLowerCase(holder.spinnerRecurrence.getSelectedItem().toString().charAt(0));

            //add number of editText
            recurrence += interval;

            //write back
            DataManager.editRecurrence(adapter.context, entry,recurrence);

            if (adapter instanceof DropTaskListAdapter || adapter instanceof PickTaskListAdapter) {
                DataManager.editReminderDate(adapter.context, entry, LocalDate.now());
                adapter.entries.remove(position);
                adapter.notifyDataSetChanged();
                if (adapter instanceof PickTaskListAdapter)
                    ((PickTaskListAdapter) adapter).itemCountChanged();
                return;
            }
            else {
                //change color of recurrence Button to mark if recurrence is set
                adapter.markSet(holder, adapter.entries.get(position));
            }
        }

        //notify adapter
        adapter.notifyDataSetChanged();

        //return to original row layout
        adapter.setOriginal(holder);
    }
}
