package com.bdreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import com.bdreiss.zentodo.R;
import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class PickTaskListAdapter extends TaskListAdapter{
    private final ArrayList<Integer> idsChecked = new ArrayList<>();//if mode.equals("pick") it stores ids of all checked tasks

    Button delete;
    public PickTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }

    @Override
    protected void setMenuListener(TaskListAdapter.ViewHolder holder){
        //listener that changes to alternative row layout on click
        holder.menu.setOnClickListener(view -> {
            //setting alternative row layout visible and active, everything else disabled
            holder.delete.setVisibility(View.VISIBLE);
            holder.focus.setVisibility(View.GONE);
            setAlt(holder);
            setMenuListener(holder);
        });

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

    @Override
    public void setCheckBoxListener(ViewHolder holder, int position){
        holder.checkBox.setOnClickListener(view -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get ID

            if (idsChecked.contains(id)){
                idsChecked.remove(id);
            } else{
                idsChecked.add(id);
            }


        });

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void setFocusListener(ViewHolder holder,int position){
        holder.delete.setOnClickListener(view -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get ID

            data.remove(id);

            notifyDataSetChanged();

        });
    }


}

