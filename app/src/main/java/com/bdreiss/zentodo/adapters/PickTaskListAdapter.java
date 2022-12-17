package com.bdreiss.zentodo.adapters;

/*
*       Extends TaskListAdapter but does not remove tasks when CheckBox is clicked. Instead it stores
*       all chosen tasks and upon clicking the PickButton in MainActivity sends all chosen tasks to Focus.
*       Because of that the Focus Button does not have much use in this adapter. Therefore its new
*       function is to delete tasks (being marked by a delete drawable).
*
 */

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;

import com.bdreiss.zentodo.adapters.listeners.BackListListenerPick;
import com.bdreiss.zentodo.adapters.listeners.SetDateListener;
import com.bdreiss.zentodo.adapters.listeners.SetDateListenerPick;
import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class PickTaskListAdapter extends TaskListAdapter {
    private final ArrayList<Integer> idsChecked = new ArrayList<>();//stores ids of all checked tasks

    private PickTaskListAdapter otherAdapter;
    private PickTaskListAdapter doLaterAdapter;
    private PickTaskListAdapter movetoListAdapter;
    private boolean checkboxTicked;//Tasks that are in DO NOW are ticked

    public PickTaskListAdapter(Context context, Data data,  ArrayList<Entry> entries, boolean checkboxTicked){
        super(context, data, entries);

        this.checkboxTicked = checkboxTicked;

    }

    public void setOtherAdapter(PickTaskListAdapter otherAdapter){
        this.otherAdapter = otherAdapter;
    }

    public void setDoLaterAdapter(PickTaskListAdapter doLaterAdapter){
        this.doLaterAdapter = doLaterAdapter;
    }

    public void setMovetoListAdapter (PickTaskListAdapter movetoListAdapter){
        this.movetoListAdapter = movetoListAdapter;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.checkBox.setChecked(checkboxTicked);

        holder.menu.setOnClickListener(v -> {
            //setting alternative row layout visible and active, everything else disabled
            setAlt(holder);

            //setting delete drawable VISIBLE
            holder.delete.setVisibility(View.VISIBLE);

            //setting focus drawable GONE
            holder.focus.setVisibility(View.GONE);
        });

        holder.checkBox.setOnClickListener(v ->{

            //get current entry
            Entry entry = entries.get(position);

            //get ID
            int id = entry.getId();

            entries.remove(position);
            notifyDataSetChanged();

            otherAdapter.entries.add(entry);
            otherAdapter.notifyDataSetChanged();

            //if id has been checked before remove from idsChecked, else add
            if (idsChecked.contains(id)){

                int pos = 0;

                //get position of id
                for (int i=0;i<idsChecked.size();i++){

                    if (idsChecked.get(i) == id) {
                        pos = i;
                        break;
                    }

                }
                idsChecked.remove(pos);

            } else{
                idsChecked.add(id);
            }
        });

        holder.focus.setOnClickListener(c -> {});

        holder.delete.setOnClickListener(v ->{
            //get current entry
            Entry entry = entries.get(position);

            //get ID for manipulation in data
            int id = entry.getId();//get id

            //remove entry from data
            data.remove(id);

            //remove entry from adapter
            entries.remove(position);

            //notify adapter
            notifyDataSetChanged();
        });

//        if (idsChecked.contains(entries.get(position).getId()))
            //holder.checkBox.setChecked(true);

        holder.setDate.setOnClickListener(new SetDateListenerPick(this, holder, position,otherAdapter,doLaterAdapter, movetoListAdapter));
        holder.backList.setOnClickListener(new BackListListenerPick(this,holder,position,otherAdapter,doLaterAdapter,movetoListAdapter));

        if (entries.get(position).getList() != null){

            String color = data.getListColor(entries.get(position).getList());
            //color = color.substring(2,8);

            holder.linearLayout.setBackgroundColor(Color.parseColor( color));

        } else{
            holder.linearLayout.setBackgroundColor(Color.WHITE);

        }


    }



    //returns ids of checked tasks in mode pick
    public ArrayList<Integer> getIdsChecked(){
        return idsChecked;
    }

    //returns ids of tasks that have not been checked in mode pick
    public ArrayList<Integer> getIdsNotChecked(){
        ArrayList<Integer> notChecked = new ArrayList<>();

        for (Entry e: entries){
            if (!idsChecked.contains(e.getId())){
                notChecked.add(e.getId());
            }
        }
        return notChecked;
    }

    //clears the ArrayString
    public void clearIdsChecked(){

        idsChecked.clear();
    }


}

