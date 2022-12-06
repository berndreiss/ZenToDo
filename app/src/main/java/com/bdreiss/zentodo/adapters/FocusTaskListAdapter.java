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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bdreiss.zentodo.R;
import com.bdreiss.zentodo.adapters.listeners.SetDateListener;
import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class FocusTaskListAdapter extends TaskListAdapter implements View.OnClickListener {

    public FocusTaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        super(context, data, entries);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {

        super.onBindViewHolder(holder, position);

        if (entries.get(position).getList() != null){

            String color = data.getListColor(entries.get(position).getList());

            holder.linearLayout.setBackgroundColor(Color.parseColor( color));

        } else{
            holder.linearLayout.setBackgroundColor(Color.WHITE);

        }

        //set Listener for checkbox: basically if checked the item is removed, additionally motivating picture of Panda is shown when all tasks are completed
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



        //set Listener for the Focus Button in the menu, item is removed from adapter upon click
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

        //return DatePickerDialog that writes back to data and removes item from adapter
        holder.setDate.setOnClickListener(new SetDateListener(this,holder,position){
            @Override
            public DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, ViewHolder holder, int position){
                DatePickerDialog datePickerDialog;

                datePickerDialog= new DatePickerDialog(context, (view, year, month, day) -> {

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
        });

    }

    private void showImage() {
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

    @Override
    public void onClick(View v){
        Log.d("TEST","FOCUS");
    }

}
