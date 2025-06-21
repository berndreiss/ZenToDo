package net.berndreiss.zentodo.adapters.listeners;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;

import net.berndreiss.zentodo.R;
import net.berndreiss.zentodo.adapters.PickTaskListAdapter;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * TODO DESCRIBE
 */
public class SetDateListenerPick extends SetDateListener{

    private final PickTaskListAdapter pickAdapter;
    private final PickTaskListAdapter doLaterAdapter;
    private final  PickTaskListAdapter moveToListAdapter;

    public SetDateListenerPick(PickTaskListAdapter adapter, TaskListAdapter.ViewHolder holder, int position, PickTaskListAdapter pickAdapter, PickTaskListAdapter doLaterAdapter, PickTaskListAdapter moveToListAdapter){
        super(adapter, holder, position);
        this.pickAdapter = pickAdapter;
        this.doLaterAdapter = doLaterAdapter;
        this.moveToListAdapter = moveToListAdapter;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public DatePickerDialog getDatePickerDialog(Task task, int taskDay, int taskMonth, int taskYear, TaskListAdapter.ViewHolder holder, int position){

        //DatePickerDialog to be returned
        DatePickerDialog datePickerDialog;

        //initialize DatePickerDialog
        datePickerDialog= new DatePickerDialog(adapter.sharedData.context, (view, year, month, day) -> {

            LocalDate date = LocalDate.of(year, month + 1, day);

            Instant dateInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

            Task e = adapter.tasks.get(position);
            //Write back data
            DataManager.editReminderDate(adapter.sharedData, task, dateInstant);

            doLaterAdapter.tasks.add(e);
            doLaterAdapter.notifyDataSetChanged();
            doLaterAdapter.itemCountChanged();
            adapter.tasks.remove(e);
            adapter.notifyDataSetChanged();
            PickTaskListAdapter adapterTemp = (PickTaskListAdapter) adapter;
            adapterTemp.itemCountChanged();
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

            Task e = adapter.tasks.get(position);

            if (e.getList() != null && !((PickTaskListAdapter) adapter).isCheckboxTicked()){
                moveToListAdapter.tasks.add(e);
                moveToListAdapter.notifyDataSetChanged();
                moveToListAdapter.itemCountChanged();

                adapter.tasks.remove(e);
                adapter.notifyDataSetChanged();
                PickTaskListAdapter adapterTemp = (PickTaskListAdapter) adapter;
                adapterTemp.itemCountChanged();

            }
            else {
                if (doLaterAdapter.tasks.contains(e)) {
                    doLaterAdapter.tasks.remove(e);
                    doLaterAdapter.notifyDataSetChanged();
                    doLaterAdapter.itemCountChanged();
                    pickAdapter.tasks.add(e);
                    pickAdapter.notifyDataSetChanged();
                    pickAdapter.itemCountChanged();
                }
            }
        });

        return datePickerDialog;
    }
}
