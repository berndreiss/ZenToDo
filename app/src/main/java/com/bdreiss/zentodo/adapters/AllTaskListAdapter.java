package com.bdreiss.zentodo.adapters;

import android.content.Context;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class AllTaskListAdapter extends TaskListAdapter{

    AllTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context,data,entries);
    }
    @Override
    public void setCheckBoxListener(ViewHolder holder, int position){
        holder.checkBox.setOnClickListener(view -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get ID

            data.remove(id);

            notifyDataSetChanged();//update the adapter


        });

    }

}
