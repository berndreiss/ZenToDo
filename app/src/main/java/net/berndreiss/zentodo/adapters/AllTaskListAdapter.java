package net.berndreiss.zentodo.adapters;

/*
*       Adapter that shows all tasks sorted by their reminder date. Items are removed upon CheckBox-click
*       even if they are recurring.
*
 */

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.dataManipulation.Data;
import net.berndreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class AllTaskListAdapter extends TaskListAdapter{

    AllTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context,data,entries);
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


    }
}
