package net.berndreiss.zentodo.adapters.listeners;

import android.annotation.SuppressLint;
import android.view.View;

import net.berndreiss.zentodo.adapters.PickTaskListAdapter;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.Data.DataManager;
import net.berndreiss.zentodo.Data.Entry;

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
        //get new list name
        String list = holder.autoCompleteList.getText().toString();

        Entry entry = adapter.entries.get(position);

        //set to no list if AutoComplete is empty
        if (list.trim().isEmpty() || list.isEmpty()) {
            //reset to no list
            DataManager.editList(adapter.context, adapter.entries, entry, null);
        } else {
            //write back otherwise
            DataManager.editList(adapter.context, adapter.entries,  entry, list);
        }

        if (entry.getReminderDate()==null && !moveToListAdapter.entries.contains(entry)){

            moveToListAdapter.entries.add(entry);
            moveToListAdapter.notifyDataSetChanged();
            moveToListAdapter.itemCountChanged();
            adapter.entries.remove(entry);
            adapter.notifyDataSetChanged();
            currentAdapter.itemCountChanged();
        }

        if (entry.getList() == null && moveToListAdapter.entries.contains(entry)){
            moveToListAdapter.entries.remove(entry);
            moveToListAdapter.notifyDataSetChanged();
            moveToListAdapter.itemCountChanged();
            pickAdapter.entries.add(entry);
            pickAdapter.notifyDataSetChanged();
            pickAdapter.itemCountChanged();
        }

        adapter.notifyDataSetChanged();


    }

}
