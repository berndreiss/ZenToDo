package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.widget.Button;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.Mode;
import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;
import net.berndreiss.zentodo.data.TaskList;

import java.util.List;

/**
 *       Extends TaskListAdapter but removes tasks when list is changed. Also removes item when
 *       CheckBox is clicked regardless of whether task is recurring or not. Also upon swap
 *        the positions in the list get swapped and stored additionally to the the overall positions.
 *
 */

public class ListTaskListAdapter extends TaskListAdapter{

    public ListTaskListAdapter(SharedData sharedData, TaskList taskList, List<Task> tasks){
        super(sharedData, tasks);
        sharedData.listAdapter = this;
        this.taskList = taskList;
    }

    /** TODO DESCRIBE */
    public static String DEFAULT_COLOR = "#00ffffff";
    public TaskList taskList;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.backList.setOnClickListener(_ -> {
            //Get id of task
            Task task = tasks.get(position);
            //get new list name
            String list = holder.autoCompleteList.getText().toString();
            //set to no list if AutoComplete is empty
            if (list.trim().isEmpty() || list.isEmpty()) {
                //reset to no list
                DataManager.editList(sharedData, task, null);
            } else {
                //write back otherwise
                DataManager.editList(sharedData, task, list);
            }
        });
    }

    //write item movement to data and notify adapter: tasks can be dragged and dropped. Additionally write back list positions.
    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        //swap tasks in data distinguishing between item being moved up or down
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                DataManager.swapLists(sharedData, tasks.get(i),tasks.get(i+1));
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                DataManager.swapLists(sharedData, tasks.get(i),tasks.get(i-1));
            }
        }

        //notify the adapter
        notifyItemMoved(fromPosition,toPosition);

    }

    @Override
    public void reset() {}
}
