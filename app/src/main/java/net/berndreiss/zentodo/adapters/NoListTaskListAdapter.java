package net.berndreiss.zentodo.adapters;

/*
 *       Extends AllTaskListAdapter but when list is changed task is removed from adapter.
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.Data.DataManager;
import net.berndreiss.zentodo.Data.Entry;

import java.util.List;

public class NoListTaskListAdapter extends AllTaskListAdapter{

    NoListTaskListAdapter(Context context, List<Entry> entries){
        super(context, entries);
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
                DataManager.editList(context, entries, entry, list);

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
