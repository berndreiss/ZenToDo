package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Entry;

import java.util.List;

/**
 *       Adapter that shows all tasks sorted by their reminder date. Items are removed upon CheckBox-click
 *       even if they are recurring.
 *
 */
public class AllTaskListAdapter extends TaskListAdapter{

    AllTaskListAdapter(SharedData sharedData, List<Entry> entries){
        super(sharedData, entries);
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
            DataManager.remove(sharedData, this, entry);

            //notify adapter
            notifyDataSetChanged();
        });


    }

    @Override
    public void reset() {}
}
