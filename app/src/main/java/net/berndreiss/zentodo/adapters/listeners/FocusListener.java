package net.berndreiss.zentodo.adapters.listeners;

import android.view.View;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;

/**
 *
 *  Implements listener for the focus Button os a task.
 *  <p>
 *  Sets dropped to false and inverts focus attribute. Also changes color of focus Button.
 *
 */
public class FocusListener extends BasicListener implements View.OnClickListener{

    public FocusListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @Override
    public void onClick(View v){
        //get current task
        Task task = adapter.tasks.get(position);

        boolean focused = task.getFocus();

        //change state of focus in task
        DataManager.setFocus(adapter.sharedData, task, !focused);

        //change color of Focus Button marking whether task is focused or not
        adapter.markSet(holder,task);

        //notify the adapter
        adapter.notifyItemChanged(position);

        //return to original row layout
        adapter.setOriginal(holder);

    }

}
