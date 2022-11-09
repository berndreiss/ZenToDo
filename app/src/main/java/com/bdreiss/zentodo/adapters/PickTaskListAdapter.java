package com.bdreiss.zentodo.adapters;

/*
*       Extends TaskListAdapter but does not remove tasks when CheckBox is clicked. Instead it stores
*       all chosen tasks and upon clicking the PickButton in MainActivity sends all chosen tasks to Focus.
*       Because of that the Focus Button does not have much use in this adapter. Therefore its new
*       function is to delete tasks (being marked by a delete drawable).
*
 */

import android.content.Context;
import android.view.View;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class PickTaskListAdapter extends TaskListAdapter{
    private final ArrayList<Integer> idsChecked = new ArrayList<>();//stores ids of all checked tasks

    public PickTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }

    //listener that changes to alternative menu row layout on click, but setting a delete drawable for the focus Button
    @Override
    protected void setMenuListener(TaskListAdapter.ViewHolder holder){
        holder.menu.setOnClickListener(view -> {

            //setting alternative row layout visible and active, everything else disabled
            setAlt(holder);

            //setting delete drawable VISIBLE
            holder.delete.setVisibility(View.VISIBLE);

            //setting focus drawable GONE
            holder.focus.setVisibility(View.GONE);

            //reset Listener
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


    //set Listener for CheckBox. Add all picked tasks to idsChecked
    @Override
    public void setCheckBoxListener(ViewHolder holder, int position){
        holder.checkBox.setOnClickListener(view -> {

            //get current entry
            Entry entry = entries.get(position);

            //get ID
            int id = entry.getId();

            //if id has been checked before remove from idsChecked, else add
            if (idsChecked.contains(id)){

                int pos = 0;
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

            //reset Listener
            setCheckBoxListener(holder, position);

        });

    }

    //set Focus Listener that deletes entry upon click
    @Override
    public void setFocusListener(ViewHolder holder,int position){
        holder.delete.setOnClickListener(view -> {

            //get current entry
            Entry entry = entries.get(position);

            //get ID for manipulation in data
            int id = entry.getId();//get id

            //remove entry from data
            data.remove(id);

            //remove entry from adapter
            entries.remove(position);

            //notify adapter
            notifyItemRemoved(position);
        });
    }

}

