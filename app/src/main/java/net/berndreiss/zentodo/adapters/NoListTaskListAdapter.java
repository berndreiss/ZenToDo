package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;

import java.util.List;

/**
 *       Extends AllTaskListAdapter but when list is changed task is removed from adapter.
 *
 */
public class NoListTaskListAdapter extends AllTaskListAdapter{

    NoListTaskListAdapter(SharedData sharedData, List<Task> tasks){
        super(sharedData, tasks);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);
        //set Listener to change list of task, if the new list is not empty also remove task from adapter
        holder.backList.setOnClickListener(v -> {
            //get id of task
            Task task = tasks.get(position);
            //get name of new list
            String list = holder.autoCompleteList.getText().toString();
            //if old and new list differ write back data and remove item from adapter
            if (!list.isEmpty()) {
                //write back new list
                DataManager.editList(sharedData, task, list);
            }
        });
    }
}
