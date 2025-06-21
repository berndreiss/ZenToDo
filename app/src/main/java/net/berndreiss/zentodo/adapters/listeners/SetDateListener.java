package net.berndreiss.zentodo.adapters.listeners;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.view.View;

import net.berndreiss.zentodo.R;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

/**
 *
 *  Implements listener for the setting the reminder date of a task.
 *  <p>
 *  Gets current reminder date from task and shows datePickerDialog.
 *
 */
public class SetDateListener extends BasicListener implements View.OnClickListener {

    public SetDateListener(TaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position){
        super(adapter, holder, position);
    }

    @Override
    public void onClick(View v){

        //Get task
        Task task = adapter.tasks.get(position);


        //variables for setting picker
        int taskDay;
        int taskMonth;
        int taskYear;


        Calendar c = Calendar.getInstance();
        taskYear = c.get(Calendar.YEAR);
        taskMonth = c.get(Calendar.MONTH);
        taskDay = c.get(Calendar.DAY_OF_MONTH);

        //create DatePickerDialog and set listener
        DatePickerDialog datePickerDialog = getDatePickerDialog(task, taskDay,taskMonth,taskYear,holder,position);

        //show the dialog
        datePickerDialog.show();

    }

    /**
     * TODO DESCRIBE
     * @param entry
     * @param entryDay
     * @param entryMonth
     * @param entryYear
     * @param holder
     * @param position
     * @return
     */
    //return DatePickerDialog
    public DatePickerDialog getDatePickerDialog(Task task, int taskDay, int taskMonth, int taskYear, TaskListAdapter.ViewHolder holder, int position){

        //DatePickerDialog to be returned
        DatePickerDialog datePickerDialog;

        //initialize DatePickerDialog
        datePickerDialog= new DatePickerDialog(adapter.sharedData.context, (view, year, month, day) -> {

            LocalDate date = LocalDate.of(year, month, day);

            //for some strange reason the month is returned -1 in the DatePickerDialog
            date = date.plusMonths(1);

            Instant dateInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

            //Write back data
            DataManager.editReminderDate(adapter.sharedData, task, dateInstant);

            //change color of reminder date Button marking if Date is set
            adapter.markSet(holder,task);

            //notify adapter
            adapter.notifyItemChanged(position);

            //return to original row layout
            adapter.setOriginal(holder);

        }, taskYear, taskMonth, taskDay);

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,adapter.sharedData.context.getResources().getString(R.string.cancel), (dialog, which) -> {

            //set date when task is due to null
            DataManager.editReminderDate(adapter.sharedData, task,null);

            //change color of reminder date Button marking if Date is set
            adapter.markSet(holder,task);

            //notify adapter
            adapter.notifyItemChanged(position);

            //return to original layout
            adapter.setOriginal(holder);
        });

        return datePickerDialog;
    }
}
