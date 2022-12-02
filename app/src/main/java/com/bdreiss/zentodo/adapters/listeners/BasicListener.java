package com.bdreiss.zentodo.adapters.listeners;

import com.bdreiss.zentodo.adapters.TaskListAdapter;

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
