package com.bdreiss.zentodo.adapters;

/*
 *       Extends AllTaskListAdapter but when list is changed task is removed from adapter.
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class NoListTaskListAdapter extends AllTaskListAdapter{

    NoListTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context,data,entries);
    }

    //set Listener to change list of task, if the new list is not empty also remove task from adadpter
    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    @Override
    public void setBackListListener(ViewHolder holder, int position){

        holder.backList.setOnClickListener(view161 -> {

            //get id of task
            int id = entries.get(position).getId();

            //get name of new list
            String list = holder.autoCompleteList.getText().toString();

            //if old and new list differ write back data and remove item from adapter
            if (!list.isEmpty()) {

                //write back new list
                data.editList(id, list);

                //remove entry from adapter
                entries.remove(position);

                //notify adapter
                notifyDataSetChanged();

            }

            //return to original row layout
            setOriginal(holder);

            //reset Listener
            setBackListListener(holder,position);

        });
    }


}
