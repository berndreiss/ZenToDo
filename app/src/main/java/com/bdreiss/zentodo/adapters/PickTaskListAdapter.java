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

    private static PickTaskListAdapter pickAdapter;
    private static PickTaskListAdapter doNowAdapter;
    private static PickTaskListAdapter doLaterAdapter;
    private static PickTaskListAdapter moveToListAdapter;
    private boolean checkboxTicked;//Tasks that are in DO NOW are ticked

    public PickTaskListAdapter(Context context, Data data,  ArrayList<Entry> entries, boolean checkboxTicked){
        super(context, data, entries);

        this.checkboxTicked = checkboxTicked;

    }

    public void setPickAdapter (PickTaskListAdapter pickAdapter){
        this.pickAdapter = pickAdapter;
    }
    public void setDoNowAdapter(PickTaskListAdapter doNowAdapter){
        this.doNowAdapter = doNowAdapter;
    }

    public void setDoLaterAdapter(PickTaskListAdapter doLaterAdapter){
        this.doLaterAdapter = doLaterAdapter;
    }

    public void setMoveToListAdapter (PickTaskListAdapter moveToListAdapter){
        this.moveToListAdapter = moveToListAdapter;
    }

    public static PickTaskListAdapter getPickAdapter (){
        return pickAdapter;
    }
    public static PickTaskListAdapter getDoNowAdapter(){
        return doNowAdapter;
    }

    public static PickTaskListAdapter getDoLaterAdapter(){
        return doLaterAdapter;
    }

    public static PickTaskListAdapter getMoveToListAdapter(){
        return moveToListAdapter;
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

            if (doNowAdapter.entries.contains(entry)){
                doNowAdapter.entries.remove(entry);
                doNowAdapter.notifyDataSetChanged();

                if (entry.getReminderDate()> data.getToday()){
                    doLaterAdapter.entries.add(entry);
                    doLaterAdapter.notifyDataSetChanged();
                } else if (entry.getReminderDate() == 0 && entry.getList() != null){
                    moveToListAdapter.entries.add(entry);
                    moveToListAdapter.notifyDataSetChanged();
                } else{
                    pickAdapter.entries.add(entry);
                    pickAdapter.notifyDataSetChanged();
                }



            } else{
                doNowAdapter.entries.add(entry);
                doNowAdapter.notifyDataSetChanged();
                entries.remove(entry);
                notifyDataSetChanged();
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

        holder.setDate.setOnClickListener(new SetDateListenerPick(this, holder, position));
        holder.backList.setOnClickListener(new BackListListenerPick(this,holder,position));

        if (entries.get(position).getList() != null){

            String color = data.getListColor(entries.get(position).getList());
            //color = color.substring(2,8);

            holder.linearLayout.setBackgroundColor(Color.parseColor( color));

        } else{
            holder.linearLayout.setBackgroundColor(Color.WHITE);

        }


    }

}

