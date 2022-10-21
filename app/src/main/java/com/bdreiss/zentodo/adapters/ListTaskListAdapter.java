package com.bdreiss.zentodo.adapters;

import android.content.Context;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class ListTaskListAdapter extends TaskListAdapter{

    public ListTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }

    @Override
    public void setListListener(ViewHolder holder, int position){

        holder.backList.setOnClickListener(view161 -> {
            int id = entries.get(position).getId();//Get id of task
            String list = holder.autoCompleteList.getText().toString();//get list name

            //set to no list if AutoComplete is empty
            if (list.equals(" ") || list.equals("")) {

                data.editList(id, " ");//reset to no list
                entries.remove(position);
                notifyDataSetChanged();

            } else {
                data.editList(id, list);//write back otherwise
                data.setDropped(id, false);//set dropped to false

                 entries.remove(position);
                    notifyDataSetChanged();

            }

        });
    }
}
