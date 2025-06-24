package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.listeners.SetDateListener;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 *       TaskListAdapter that removes tasks from RecyclerView if task is being sent to Focus or
 *       list or date is set
 */

public class DropTaskListAdapter extends TaskListAdapter{

    public DropTaskListAdapter(SharedData sharedData){
        super(sharedData, DataManager.getDropped(sharedData));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        //Remove task from View if task is being sent to Focus
        holder.focus.setOnClickListener(v -> {
            Task task = tasks.get(position);
            DataManager.setFocus(sharedData, task, !task.getFocus());//change state of focus in task
            tasks.remove(position);
            notifyDataSetChanged();
        });

        //Remove task from View if list is set
        holder.backList.setOnClickListener(view161 -> {
            //Get id of task
            Task task = tasks.get(position);

            //get new list name
            String list = holder.autoCompleteList.getText().toString();

            //set list if AutoComplete is not empty, write back and notify adapter
            if (!list.trim().isEmpty()) {

                //write back
                DataManager.editList(sharedData, task, list);
                tasks.remove(position);
                notifyDataSetChanged();

            }

            //return to original row layout
            setOriginal(holder);

        });

        //Remove task from View if date is set
        holder.setDate.setOnClickListener(new SetDateListener(this, holder,position){

            @Override
            public DatePickerDialog getDatePickerDialog(Task task, int taskDay, int taskMonth, int taskYear, ViewHolder holder, int position){
                DatePickerDialog datePickerDialog;
                datePickerDialog= new DatePickerDialog(sharedData.context, (view, year, month, day) -> {
                    LocalDate date = LocalDate.of(year,month + 1,day);

                    Instant dateInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

                    DataManager.editReminderDate(sharedData, task, dateInstant);//Write back data
                    tasks.remove(position);
                    notifyDataSetChanged();

                }, taskYear, taskMonth, taskDay);
                return datePickerDialog;

            }
        });
    }


    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    /**
     * TODO DESCRIBE
     */
    public void add(String task){
        DataManager.add(sharedData, task);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void reset(){
        //clear ArrayList for Drop, add current tasks from data and notify adapter (in case they have been altered in another layout)
        tasks.clear();
        List<Task> dropped = sharedData.clientStub.loadDropped();
        tasks.addAll(sharedData.clientStub.loadDropped());
        notifyDataSetChanged();
    }
}
