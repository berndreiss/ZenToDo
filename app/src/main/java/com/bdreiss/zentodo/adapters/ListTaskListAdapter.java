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
    public void setCheckBoxListener(ViewHolder holder, int position){
        holder.checkBox.setOnClickListener(view -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get ID

            Boolean recurring = !entry.getRecurrence().equals(" ");

            //because lists are dynamically generated the DataSet has to be manually updated
            if (recurring) {
                data.setRecurring(id);
                data.setFocus(id,false);
            } else {
                data.remove(id);
                entries.remove(position);
            }


            notifyDataSetChanged();//update the adapter


        });

    }


    @Override
    public void setListListener(ViewHolder holder, int position){

        holder.backList.setOnClickListener(view161 -> {
            int id = entries.get(position).getId();//Get id of task
            String list = holder.autoCompleteList.getText().toString();//get list name

            //set to no list if AutoComplete is empty
            if (list.equals(" ") || list.equals("")) {
                data.editList(id, " ");//reset to no list

            } else {
                if (!list.equals(entries.get(position).getList())) {
                    data.editList(id, list);//write back otherwise
                    entries.remove(position);
                }
            }
            notifyDataSetChanged();

        });
    }
}
