package com.bdreiss.zentodo.adapters.listeners;

import android.view.View;

import com.bdreiss.zentodo.adapters.TaskListAdapter;

public class BackEditListener extends BasicListener implements View.OnClickListener{

    public BackEditListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){

        super(adapter, holder,position);
    }

    @Override
    public void onClick(View v){
        //getting the id of task
        int id = adapter.entries.get(position).getId();

        //get new Task from EditText
        String newTask = holder.editText.getText().toString();

        //if EditText is not empty write changes to data and return to original layout
        if (!newTask.isEmpty()) {

            //save new task description in data
            adapter.data.setTask(id, newTask);
            adapter.entries.get(position).setTask(newTask);

            //notify adapter
            adapter.notifyItemChanged(position);

            //return to original layout
            adapter.setOriginal(holder);
        }

    }
}
