package net.berndreiss.zentodo.adapters;

/*
 *      Shows all tasks the user chose to focus on today.
 *      Tasks are usually chosen in PICK but can also be manually added via the FOCUS-button in the menu of a task.
 *      Recurring tasks are added automatically to FOCUS (see also MainActivity.java).
 *
 *      Extends TaskListAdapter but removes tasks when focus or date is changed.
 *
 */

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import net.berndreiss.zentodo.R;
import net.berndreiss.zentodo.adapters.listeners.SetDateListener;
import net.berndreiss.zentodo.dataManipulation.Data;
import net.berndreiss.zentodo.dataManipulation.Entry;

import java.time.LocalDate;

public class FocusTaskListAdapter extends TaskListAdapter {

    public FocusTaskListAdapter(Context context, Data data){
        super(context, data, data.getFocus());
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {

        super.onBindViewHolder(holder, position);

        if (entries.get(position).getList() != null){

            String color = data.getListColor(entries.get(position).getList());

            holder.linearLayout.setBackgroundColor(Color.parseColor(color));

        } else{
            holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context,R.color.color_primary));

        }

        //set Listener for checkbox: basically if checked the item is removed, additionally motivating picture of Panda is shown when all tasks are completed
        holder.checkBox.setOnClickListener(view -> {

            //current entry
            Entry entry = entries.get(position);

            //get ID for manipulation in data
            int id = entry.getId();

            //see if item is recurring
            boolean recurring = entry.getRecurrence()!=null;

            //if recurring do not remove but set new reminder date, otherwise remove from data
            if (recurring) {
                //calculate new reminder date and write to data and entries
                entries.get(position).setReminderDate(data.setRecurring(id, LocalDate.now()));

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


            if (entry.getRecurrence() != null)
                data.addToRecurringButRemoved(id);

            if (entry.getReminderDate() == null)
                data.editReminderDate(id, LocalDate.now());

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

                    LocalDate date = LocalDate.of(year, month + 1,day);

                    //write back to data
                    data.editReminderDate(entry.getId(), date);

                    //only remove task from FOCUS, if date is in the future (a scenario where one
                    //might change the date to the past is recurring tasks -> in this scenario
                    //the task should remain in FOCUS)
                    if (date.compareTo(LocalDate.now())>0) {
                        //also set focus to false in data
                        data.setFocus(entry.getId(), false);

                        //remove from adapter
                        entries.remove(position);

                        //notify adapter
                        notifyDataSetChanged();
                    }

                }, entryYear, entryMonth, entryDay);

                datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,context.getResources().getString(R.string.cancel), (dialog, which) -> {

                    //set date when task is due to 0
                    data.editReminderDate(entry.getId(),null);
                    entries.get(position).setReminderDate(null);

                    //change color of reminder date Button marking if Date is set
                    markSet(holder,entry);

                    //notify adapter
                    notifyItemChanged(position);

                    //return to original layout
                    setOriginal(holder);
                });

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

    @SuppressLint("NotifyDataSetChanged")
    public void reset(){
        //clear ArrayList for Focus, add current tasks from data and notify adapter (in case they have been altered in another layout)
        entries.clear();
        entries.addAll(data.getFocus());
        notifyDataSetChanged();

    }


}
