package net.berndreiss.zentodo.adapters.listeners;

import android.view.View;

import net.berndreiss.zentodo.adapters.TaskListAdapter;

/*
 *
 *  Implements listener for the Button to return from editing the list of a task.
 *
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
        int id = adapter.entries.get(position).getId();

        //get new list name
        String list = holder.autoCompleteList.getText().toString();

        //set to no list if AutoComplete is empty
        if (list.trim().isEmpty() || list.equals("")) {
            //reset to no list
            adapter.entries.get(position).setListPosition(adapter.data.editList(id, null));
            adapter.entries.get(position).setList(null);
        } else {
            //write back otherwise
            adapter.entries.get(position).setListPosition(adapter.data.editList(id, list));
            adapter.entries.get(position).setList(list);
        }

        //change Color of setList Button to mark if list is set
        adapter.markSet(holder,adapter.entries.get(position));

        //notify adapter
        adapter.notifyItemChanged(position);

        //return to original row layout
        adapter.setOriginal(holder);
    }
}
