package net.berndreiss.zentodo.adapters.listeners;

import android.view.View;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.Task;

/**
 *
 *  Implements listener for the Button to edit recurrence of a task.
 *  <p>
 *  Loads current recurrence settings from task and sets Views accordingly.
 *
 */
public class RecurrenceListener extends BasicListener implements View.OnClickListener{

    public RecurrenceListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @Override
    public void onClick(View v) {
        //setting recurrence row visible and active, everything else disabled
        adapter.setRecurrence(holder);


        //get task to set spinner on current recurrence
        Task task = adapter.tasks.get(position);
        String rec = task.getRecurrence();

        if (rec == null) {
            holder.spinnerRecurrence.setSelection(0);
            holder.editTextRecurrence.setText("");
            return;
        }
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
        for (int i = 1; i < rec.length(); i++)
            recEdit.append(rec.charAt(i));

        //set editText
        holder.editTextRecurrence.setText(recEdit.toString());
    }
}
