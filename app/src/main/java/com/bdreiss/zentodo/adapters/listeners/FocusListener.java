package com.bdreiss.zentodo.adapters.listeners;

import android.view.View;

import com.bdreiss.zentodo.adapters.TaskListAdapter;
import com.bdreiss.zentodo.dataManipulation.Entry;

public class FocusListener extends BasicListener implements View.OnClickListener{

    public FocusListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @Override
    public void onClick(View v){
        //get current entry
        Entry entry = adapter.entries.get(position);

        boolean focused = entry.getFocus();

        //get ID for manipulation in data
        int id = entry.getId();//get id

        //set dropped to false: as soon as item is in focus it is not dropped anymore
        adapter.data.setDropped(id, false);

        //change state of focus in entry
        adapter.data.setFocus(id, !focused);
        entry.setFocus(!focused);

        //change color of Focus Button marking whether task is focused or not
        adapter.markSet(holder,entry);

        //notify the adapter
        adapter.notifyItemChanged(position);

        //return to original row layout
        adapter.setOriginal(holder);

    }

}
