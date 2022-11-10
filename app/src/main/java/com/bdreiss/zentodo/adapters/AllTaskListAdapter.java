package com.bdreiss.zentodo.adapters;

/*
*       Adapter that shows all tasks sorted by their due date. Items are removed upon CheckBox-click
*       even if they are recurring.
*
 */

import android.annotation.SuppressLint;
import android.content.Context;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class AllTaskListAdapter extends TaskListAdapter{

    AllTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context,data,entries);
    }

    //set CheckBoxListener that ignores if task is recurring
    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    @Override
    public void setCheckBoxListener(ViewHolder holder, int position){
        holder.checkBox.setOnClickListener(view -> {

            //get entry
            Entry entry = entries.get(position);

            //get id
            int id = entry.getId();//get ID

            //remove from data
            data.remove(id);

            //remove from adapter
            entries.remove(position);

            //notify adapter
            notifyDataSetChanged();
        });

    }

}
