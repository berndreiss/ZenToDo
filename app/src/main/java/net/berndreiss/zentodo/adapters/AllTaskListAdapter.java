package net.berndreiss.zentodo.adapters;

/*
*       Adapter that shows all tasks sorted by their reminder date. Items are removed upon CheckBox-click
*       even if they are recurring.
*
 */

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.Data.DataManager;
import net.berndreiss.zentodo.Data.Entry;

import java.util.List;

public class AllTaskListAdapter extends TaskListAdapter{

    AllTaskListAdapter(Context context, List<Entry> entries){
        super(context, entries);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        //set CheckBoxListener that ignores if task is recurring
        holder.checkBox.setOnClickListener(view -> {

            //get entry
            Entry entry = entries.get(position);

            //remove from data
            DataManager.remove(context, entries, entry);

            //notify adapter
            notifyDataSetChanged();
        });


    }
}
