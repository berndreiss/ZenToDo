package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Entry;
import net.berndreiss.zentodo.data.TaskList;

import java.util.List;
import java.util.Optional;

/**
 *       Extends AllTaskListAdapter but when list is changed task is removed from adapter.
 *
 */
public class NoListTaskListAdapter extends AllTaskListAdapter{

    NoListTaskListAdapter(SharedData sharedData, List<Entry> entries){
        super(sharedData, entries);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);

        //set Listener to change list of task, if the new list is not empty also remove task from adapter
        holder.backList.setOnClickListener(v -> {

            //get id of task
            Entry entry = entries.get(position);

            //get name of new list
            String list = holder.autoCompleteList.getText().toString();

            //if old and new list differ write back data and remove item from adapter
            if (!list.isEmpty()) {

                //write back new list
                DataManager.editList(sharedData, this, entry, list);

                //remove entry from adapter
                entries.remove(position);

                //notify adapter
                notifyDataSetChanged();

            }

            //return to original row layout
            setOriginal(holder);

        });

    }

}
