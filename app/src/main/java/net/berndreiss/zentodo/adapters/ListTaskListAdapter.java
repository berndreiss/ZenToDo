package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;
import net.berndreiss.zentodo.data.TaskList;

import java.util.List;
import java.util.Optional;

/**
 *       Extends TaskListAdapter but removes tasks when list is changed. Also removes item when
 *       CheckBox is clicked regardless of whether task is recurring or not. Also upon swap
 *        the positions in the list get swapped and stored additionally to the the overall positions.
 *
 */

public class ListTaskListAdapter extends TaskListAdapter{

    public ListTaskListAdapter(SharedData sharedData, List<Task> tasks){
        super(sharedData, tasks);
    }

    /** TODO DESCRIBE */
    public static String DEFAULT_COLOR = "#00ffffff";

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        //set CheckBoxListener that ignores if task is recurring
        holder.checkBox.setOnClickListener(view -> {

            //get task
            Task task = tasks.get(position);

            //remove from data
            DataManager.remove(sharedData, this, task);

            //notify adapter
            notifyDataSetChanged();
        });

        //set Listener to change list of task, if the new list differs from the old also remove task from adapter
        holder.backList.setOnClickListener(view161 -> {

            //get id of task
            Task task = tasks.get(position);

            //get name of new list
            String list = holder.autoCompleteList.getText().toString();

            //get name of old list
            Optional<TaskList> oldTaskList = sharedData.database.getListManager().getList(tasks.get(position).getList());

            String oldList = oldTaskList.isEmpty() ? "" : oldTaskList.get().getName();

            //if old and new list differ write back data and remove item from adapter
            if (!list.equals(oldList)) {

                //set to null if AutoComplete is empty, write back otherwise
                if (list.trim().isEmpty())
                    DataManager.editList(sharedData, this, task, null);

                else
                    DataManager.editList(sharedData, this, task, list);

                //remove task from adapter
                tasks.remove(position);

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

        //swap tasks in data distinguishing between item being moved up or down
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                DataManager.swapLists(sharedData, this, tasks.get(i),tasks.get(i+1));
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                DataManager.swapLists(sharedData, this, tasks.get(i),tasks.get(i-1));
            }
        }

        //notify the adapter
        notifyItemMoved(fromPosition,toPosition);

    }

    @Override
    public void reset() {}
}
