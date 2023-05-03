package com.bdreiss.zentodo.adapters.listeners;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.view.View;

import com.bdreiss.zentodo.R;
import com.bdreiss.zentodo.adapters.TaskListAdapter;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.time.LocalDate;
import java.util.Calendar;

/*
 *
 *  Implements listener for the setting the reminder date of a task.
 *
 *  Gets current reminder date from task and shows datePickerDialog.
 *
 */


public class SetDateListener extends BasicListener implements View.OnClickListener {

    public SetDateListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @Override
    public void onClick(View v){

        //Get entry
        Entry entry = adapter.entries.get(position);


        //variables for setting picker
        int entryDay;
        int entryMonth;
        int entryYear;


        Calendar c = Calendar.getInstance();
        entryYear = c.get(Calendar.YEAR);
        entryMonth = c.get(Calendar.MONTH);
        entryDay = c.get(Calendar.DAY_OF_MONTH);

        //create DatePickerDialog and set listener
        DatePickerDialog datePickerDialog = getDatePickerDialog(entry, entryDay,entryMonth,entryYear,holder,position);

        //show the dialog
        datePickerDialog.show();

    }

    //return DatePickerDialog
    public DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, TaskListAdapter.ViewHolder holder, int position){

        //DatePickerDialog to be returned
        DatePickerDialog datePickerDialog;

        //initialize DatePickerDialog
        datePickerDialog= new DatePickerDialog(adapter.context, (view, year, month, day) -> {

            LocalDate date = LocalDate.of(year, month, day);

            //for some strange reason the month is returned -1 in the DatePickerDialog
            date = date.plusMonths(1);

            //Write back data
            adapter.data.editReminderDate(entry.getId(), date);
            adapter.entries.get(position).setReminderDate(date);

            //change color of reminder date Button marking if Date is set
            adapter.markSet(holder,entry);

            //notify adapter
            adapter.notifyItemChanged(position);

            //return to original row layout
            adapter.setOriginal(holder);

        }, entryYear, entryMonth, entryDay);

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,adapter.context.getResources().getString(R.string.cancel), (dialog, which) -> {

            //set date when task is due to 0
            adapter.data.editReminderDate(entry.getId(),null);
            adapter.entries.get(position).setReminderDate(null);

            //change color of reminder date Button marking if Date is set
            adapter.markSet(holder,entry);

            //notify adapter
            adapter.notifyItemChanged(position);

            //return to original layout
            adapter.setOriginal(holder);
        });

        return datePickerDialog;
    }

}


