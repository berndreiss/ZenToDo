package net.berndreiss.zentodo.adapters;

/*
 *       Extends TaskListAdapter but removes tasks when list is changed. Also removes item when
 *       CheckBox is clicked regardless of whether task is recurring or not. Also upon swap
 *        the positions in the list get swapped and stored additionally to the the overall positions.
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.dataManipulation.Data;
import net.berndreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;
import java.util.Collections;

public class ListTaskListAdapter extends TaskListAdapter{

    public ListTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        //set CheckBoxListener that ignores if task is recurring
        holder.checkBox.setOnClickListener(view -> {

            //get entry
            Entry entry = entries.get(position);

            //get id
            int id = entry.getId();//get ID

            //remove from data
            data.remove(id);

            //remove from adapter
            entries.remove(position);

            //notify adapter
            notifyDataSetChanged();
        });

        //set Listener to change list of task, if the new list differs from the old also remove task from adapter
        holder.backList.setOnClickListener(view161 -> {

            //get id of task
            int id = entries.get(position).getId();

            //get name of new list
            String list = holder.autoCompleteList.getText().toString();

            //get name of old list
            String oldList = entries.get(position).getList();

            //if old and new list differ write back data and remove item from adapter
            if (!list.equals(oldList)) {

                //set to null if AutoComplete is empty, write back otherwise
                if (list.trim().isEmpty())
                    data.editList(id, null);

                else
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

    //write item movement to data and notify adapter: tasks can be dragged and dropped. Additionally write back list positions.
    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        //swap entries in data distinguishing between item being moved up or down
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                data.swapList(entries.get(i).getId(),entries.get(i+1).getId());
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                data.swapList(entries.get(i).getId(),entries.get(i-1).getId());
            }
        }

        //swap items in entries
        Collections.swap(entries, fromPosition, toPosition);

        //notify the adapter
        notifyItemMoved(fromPosition,toPosition);

    }
}
