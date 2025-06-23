package net.berndreiss.zentodo.adapters.listeners;

import android.os.UserHandle;
import android.view.View;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;
import net.berndreiss.zentodo.data.TaskList;

import java.util.Optional;

/**
 *
 *  Implements listener for the Button to return from editing the list of a task.
 * <p>
 *  Sets list to null if new list-String is empty. Writes back data and resets color of the list Button.
 *
 */
public class BackListListener extends BasicListener implements View.OnClickListener{


    public BackListListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @Override
    public void onClick(View v){
        //Get id of task
        Task task = adapter.tasks.get(position);

        //get new list name
        String list = holder.autoCompleteList.getText().toString();

        //set to no list if AutoComplete is empty
        if (list.trim().isEmpty() || list.isEmpty()) {
            //reset to no list
            DataManager.editList(adapter.sharedData, task, null);
        } else {
            //write back otherwise
            DataManager.editList(adapter.sharedData, task, list);
        }

        //change Color of setList Button to mark if list is set
        adapter.markSet(holder,adapter.tasks.get(position));

        //notify adapter
        adapter.notifyItemChanged(position);

        //return to original row layout
        adapter.setOriginal(holder);
    }
}
