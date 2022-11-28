package com.bdreiss.zentodo.adapters;

/*
 *       Extends TaskListAdapter but removes tasks when focus or date is changed.
 */

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.bdreiss.zentodo.R;
import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class FocusTaskListAdapter extends TaskListAdapter{

    public FocusTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {

        initializeViews(holder, position);


        if (entries.get(position).getList() != null){

            String color = data.getListColor(entries.get(position).getList());
            //color = color.substring(2,8);

            holder.linearLayout.setBackgroundColor(Color.parseColor( color));

        }

    }

    //set Listener for checkbox: basically if checked the item is removed, additionally motivating picture of Panda is shown when all tasks are completed
    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    @Override
    protected void setCheckBoxListener(TaskListAdapter.ViewHolder holder, int position){
        holder.checkBox.setOnClickListener(view -> {

            //current entry
            Entry entry = entries.get(position);

            //get ID for manipulation in data
            int id = entry.getId();

            //see if item is recurring
            boolean recurring = entry.getRecurrence()!=null;

            //if recurring do not remove but set new due date, otherwise remove from data
            if (recurring) {
                //calculate new due date and write to data and entries
                entries.get(position).setDue(data.setRecurring(id));

                //reset focus in data and entries
                data.setFocus(id,false);
                entries.get(position).setFocus(false);

                //reset dropped in data and entries
                data.setDropped(id,false);
                entries.get(position).setDropped(false);

                notifyItemChanged(position);
            } else {
                data.remove(id);
            }


            //remove from adapter and notify
            entries.remove(position);

            if (entries.size()==0)
                showImage();

            notifyDataSetChanged();


        });

    }

    //set Listener for the Focus Button in the menu, item is removed from adapter upon click
    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    @Override
    public void setFocusListener(ViewHolder holder,int position){
        holder.focus.setOnClickListener(view12 -> {

            //get entry
            Entry entry = entries.get(position);

            //get id
            int id = entry.getId();

            //write back change to data
            data.setFocus(id, false);

            //remove from adapter
            entries.remove(position);

            //notify adapter
            notifyDataSetChanged();
        });

    }

    //return DatePickerDialog that writes back to data and removes item from adapter
    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    public DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, ViewHolder holder, int position){

        DatePickerDialog datePickerDialog;

        datePickerDialog= new DatePickerDialog(super.context, (view, year, month, day) -> {

            //encode format "YYYYMMDD"
            int date = year*10000+(month+1)*100+day;

            //write back to data
            data.editDue(entry.getId(), date);

            //also set focus to false in data
            data.setFocus(entry.getId(),false);

            //remove from adapter
            entries.remove(position);

            //notify adapter
            notifyDataSetChanged();

        }, entryYear, entryMonth, entryDay);

        //return Dialog
        return datePickerDialog;
    }

    public void showImage() {
        Dialog builder = new Dialog(context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        builder.setOnDismissListener(dialogInterface -> {
            //nothing;
        });

        ImageView imageView = new ImageView(context);

        //I don't care whatever you change in this code, but THIS has to be a PANDA!
        imageView.setImageResource(R.drawable.panda);


        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
        imageView.setOnClickListener(v -> builder.dismiss());
    }
}
