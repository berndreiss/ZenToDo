package net.berndreiss.zentodo.adapters.listeners;

import android.view.View;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Entry;

/**
 *
 *  Implements listener for the Button to return from editing a task.
 * <p>
 *  Sets TextView to new task and writes back data if new task is not empty.
 *
 */
public class BackEditListener extends BasicListener implements View.OnClickListener{

    public BackEditListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder,position);
    }

    @Override
    public void onClick(View v){
        //getting the id of task
        Entry entry = adapter.entries.get(position);

        //get new Task from EditText
        String newTask = holder.editText.getText().toString();

        //if EditText is not empty write changes to data and return to original layout
        if (!newTask.isEmpty()) {

            //save new task description in data
            DataManager.setTask(adapter.context, entry, newTask);

            //notify adapter
            adapter.notifyItemChanged(position);

            //return to original layout
            adapter.setOriginal(holder);
        }

    }
}
