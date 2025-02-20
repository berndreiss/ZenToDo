package net.berndreiss.zentodo.adapters.listeners;

import android.view.View;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.Data.DataManager;
import net.berndreiss.zentodo.api.Entry;

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
        //get current entry
        Entry entry = adapter.entries.get(position);

        boolean focused = entry.getFocus();

        //change state of focus in entry
        DataManager.setFocus(adapter.context, entry, !focused);

        //change color of Focus Button marking whether task is focused or not
        adapter.markSet(holder,entry);

        //notify the adapter
        adapter.notifyItemChanged(position);

        //return to original row layout
        adapter.setOriginal(holder);

    }

}
