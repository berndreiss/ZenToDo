package net.berndreiss.zentodo.adapters.listeners;

import android.annotation.SuppressLint;
import android.view.View;

import net.berndreiss.zentodo.adapters.PickTaskListAdapter;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.Task;

/**
 * TODO DESCRIBE
 */
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
    public void onClick(View v) {
        //get new list name
        String list = holder.autoCompleteList.getText().toString();

        Task task = adapter.tasks.get(position);

        if (list.trim().isEmpty() || list.isEmpty()) {
            pickAdapter.unSetList(task.getId());
            task.setList(null);
        }
        else {
            pickAdapter.setList(task.getId(), list);
            task.setList(0L);
        }

        if (task.getReminderDate() == null && !moveToListAdapter.tasks.contains(task)) {
            moveToListAdapter.tasks.add(task);
            moveToListAdapter.notifyDataSetChanged();
            moveToListAdapter.itemCountChanged();
            adapter.tasks.remove(task);
            adapter.notifyDataSetChanged();
            currentAdapter.itemCountChanged();
        }

        if (task.getList() == null && moveToListAdapter.tasks.contains(task)) {
            moveToListAdapter.tasks.remove(task);
            moveToListAdapter.notifyDataSetChanged();
            moveToListAdapter.itemCountChanged();
            pickAdapter.tasks.add(task);
            pickAdapter.notifyDataSetChanged();
            pickAdapter.itemCountChanged();
        }
        //TODO handler reminder date set but list chosen

        adapter.notifyDataSetChanged();
    }
}
