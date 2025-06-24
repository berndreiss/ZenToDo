package net.berndreiss.zentodo.adapters.recyclerViewHelper;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.Task;

import java.util.Comparator;

/**
 * TODO DESCRIBE
 */
public class CustomListItemTouchHelperCallback extends CustomItemTouchHelperCallback {

    public CustomListItemTouchHelperCallback(TaskListAdapter adapter, RecyclerView recyclerView) {
        super(adapter, recyclerView);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            recyclerView.post(() -> {
                adapter.tasks.sort(Comparator.comparingInt(Task::getListPosition));
                adapter.notifyDataSetChanged();
            });
        }
        adapter.sharedData.itemIsInMotion = false;
    }
}