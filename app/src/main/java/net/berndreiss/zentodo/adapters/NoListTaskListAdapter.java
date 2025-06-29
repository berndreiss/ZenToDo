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
    }
}
