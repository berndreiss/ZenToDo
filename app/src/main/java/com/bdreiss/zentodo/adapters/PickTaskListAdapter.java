package com.bdreiss.zentodo.adapters;

import android.content.Context;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class PickTaskListAdapter extends TaskListAdapter{
    private final ArrayList<Integer> idsChecked = new ArrayList<>();//if mode.equals("pick") it stores ids of all checked tasks

    public PickTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
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

    @Override
    public void setFocusListener(ViewHolder holder,int position){}

}

