package net.berndreiss.zentodo.adapters.listeners;

import net.berndreiss.zentodo.adapters.TaskListAdapter;

/*
 *
 *  Blueprint for listeners for Buttons in the menu of a task.
 *
 */


public class BasicListener{

    TaskListAdapter adapter;
    TaskListAdapter.ViewHolder holder;
    int position;

    public BasicListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        this.adapter = adapter;
        this.holder = holder;
        this.position = position;
    }

}
