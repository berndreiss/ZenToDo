package com.bdreiss.zentodo.adapters.listeners;

import android.view.View;

import com.bdreiss.zentodo.adapters.TaskListAdapter;
import com.bdreiss.zentodo.dataManipulation.Entry;

/*
 *
 *  Implements listener for the Button to edit recurrence of a task.
 *
 *  Loads current recurrence settings from task and sets Views accordingly.
 *
 */


public class RecurrenceListener extends BasicListener implements View.OnClickListener{

    public RecurrenceListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @Override
    public void onClick(View v){
        //setting recurrence row visible and active, everything else disabled
        adapter.setRecurrence(holder);

        //get entry to set spinner on current recurrence
        Entry entry = adapter.entries.get(position);
        String rec = entry.getRecurrence();

        //initialize spinner if recurrence is set
        if (rec!=null) {
            //set spinner according to first letter y/w/m/y
            switch (rec.charAt(0)) {
                case 'd':
                    holder.spinnerRecurrence.setSelection(0);
                    break;
                case 'w':
                    holder.spinnerRecurrence.setSelection(1);
                    break;
                case 'm':
                    holder.spinnerRecurrence.setSelection(2);
                    break;
                case 'y':
                    holder.spinnerRecurrence.setSelection(3);
                    break;
                default://sets spinner to days but this should actually never happen

            }

            //String for editText
            StringBuilder recEdit = new StringBuilder();

            //add all digits after the first char
            for (int i = 1; i < rec.length(); i++) {
                recEdit.append(rec.charAt(i));
            }

            //set editText
            holder.editTextRecurrence.setText(recEdit.toString());
        }
    }
}
