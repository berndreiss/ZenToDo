package net.berndreiss.zentodo.adapters;

/*
 *       Extends AllTaskListAdapter but when list is changed task is removed from adapter.
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.dataManipulation.Data;
import net.berndreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class NoListTaskListAdapter extends AllTaskListAdapter{

    NoListTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context,data,entries);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);

        //set Listener to change list of task, if the new list is not empty also remove task from adapter
        holder.backList.setOnClickListener(v -> {

            //get id of task
            int id = entries.get(position).getId();

            //get name of new list
            String list = holder.autoCompleteList.getText().toString();

            //if old and new list differ write back data and remove item from adapter
            if (!list.isEmpty()) {

                //write back new list
                data.editList(id, list);

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
