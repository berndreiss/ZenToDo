package net.berndreiss.zentodo.adapters.listeners;

import android.annotation.SuppressLint;
import android.view.View;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Entry;

import java.time.LocalDate;

/**
 *
 *  Implements listener for checkBox of a task.
 *  <p>
 *  Removes task if it is not recurring. Otherwise resets the reminder date adding the given interval.
 *
 */
public class CheckBoxListener extends BasicListener implements View.OnClickListener{

    public CheckBoxListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View v){
        //current entry
        Entry entry = adapter.entries.get(position);

        //see if item is recurring
        boolean recurring = entry.getRecurrence()!=null;

        //if recurring do not remove but set new reminder date, otherwise remove from data
        if (recurring) {
            //calculate new reminder date and write to data and entries
            DataManager.setRecurring(adapter.context, entry, LocalDate.now());

            //reset focus in data and entries
            DataManager.setFocus(adapter.context, entry, false);

            adapter.entries.remove(position);

            adapter.notifyItemChanged(position);
        } else {
            DataManager.remove(adapter.context, adapter.entries, entry);
        }

        adapter.notifyDataSetChanged();

    }

}
