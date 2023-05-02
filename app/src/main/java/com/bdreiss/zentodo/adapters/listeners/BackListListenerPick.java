package com.bdreiss.zentodo.adapters.listeners;

import android.annotation.SuppressLint;
import android.view.View;

import com.bdreiss.zentodo.adapters.PickTaskListAdapter;
import com.bdreiss.zentodo.adapters.TaskListAdapter;
import com.bdreiss.zentodo.dataManipulation.Entry;

public class BackListListenerPick extends BackListListener{

    PickTaskListAdapter currentAdapter;
    private final PickTaskListAdapter pickAdapter;
    private final PickTaskListAdapter moveToListAdapter;


    public BackListListenerPick(PickTaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position, PickTaskListAdapter pickAdapter, PickTaskListAdapter moveToListAdapter){
        super(adapter, holder, position);
        this.currentAdapter = adapter;
        this.pickAdapter = pickAdapter;
        this.moveToListAdapter = moveToListAdapter;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View v){
        //Get id of task
        int id = adapter.entries.get(position).getId();

        //get new list name
        String list = holder.autoCompleteList.getText().toString();

        Entry e = adapter.entries.get(position);

        //set to no list if AutoComplete is empty
        if (list.trim().isEmpty() || list.equals("")) {
            //reset to no list
            e.setListPosition(adapter.data.editList(id, null));
            e.setList(null);
        } else {
            //write back otherwise
            e.setListPosition(adapter.data.editList(id, list));
            e.setList(list);
        }

        if (e.getReminderDate()==null && !moveToListAdapter.entries.contains(e)){

            moveToListAdapter.entries.add(e);
            moveToListAdapter.notifyDataSetChanged();
            moveToListAdapter.itemCountChanged();
            adapter.entries.remove(e);
            adapter.notifyDataSetChanged();
            currentAdapter.itemCountChanged();
        }

        if (e.getList() == null && moveToListAdapter.entries.contains(e)){
            moveToListAdapter.entries.remove(e);
            moveToListAdapter.notifyDataSetChanged();
            moveToListAdapter.itemCountChanged();
            pickAdapter.entries.add(e);
            pickAdapter.notifyDataSetChanged();
            pickAdapter.itemCountChanged();
        }

        adapter.notifyDataSetChanged();


    }

}
