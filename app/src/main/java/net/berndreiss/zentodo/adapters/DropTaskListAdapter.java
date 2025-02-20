package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import net.berndreiss.zentodo.adapters.listeners.SetDateListener;
import net.berndreiss.zentodo.Data.DataManager;
import net.berndreiss.zentodo.api.Entry;
import net.berndreiss.zentodo.Data.SQLiteHelper;

import java.time.LocalDate;

/**
 *       TaskListAdapter that removes tasks from RecyclerView if task is being sent to Focus or
 *       list or date is set
 */

public class DropTaskListAdapter extends TaskListAdapter{

    public DropTaskListAdapter(Context context){
        super(context, DataManager.getDropped(context));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        //Remove task from View if task is being sent to Focus
        holder.focus.setOnClickListener(v -> {
            Entry entry = entries.get(position);
            DataManager.setFocus(context, entry, !entry.getFocus());//change state of focus in entry
            entries.remove(position);
            notifyDataSetChanged();
        });

        //Remove task from View if list is set
        holder.backList.setOnClickListener(view161 -> {
            //Get id of task
            Entry entry = entries.get(position);

            //get new list name
            String list = holder.autoCompleteList.getText().toString();

            //set list if AutoComplete is not empty, write back and notify adapter
            if (!list.trim().isEmpty()) {

                //write back
                DataManager.editList(context, entries, entry, list);
                entries.remove(position);
                notifyDataSetChanged();

            }

            //return to original row layout
            setOriginal(holder);

        });

        //Remove task from View if date is set
        holder.setDate.setOnClickListener(new SetDateListener(this, holder,position){

            @Override
            public DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, ViewHolder holder, int position){
                DatePickerDialog datePickerDialog;
                datePickerDialog= new DatePickerDialog(context, (view, year, month, day) -> {
                    LocalDate date = LocalDate.of(year,month + 1,day);

                    DataManager.editReminderDate(context, entry, date);//Write back data
                    entries.remove(position);
                    notifyDataSetChanged();

                }, entryYear, entryMonth, entryDay);
                return datePickerDialog;

            }
        });
    }


    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
    /**
     * TODO DESCRIBE
     */
    public void add(String task){
        DataManager.add(context, entries, task);
        notifyDataSetChanged();

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void reset(){
        //clear ArrayList for Drop, add current tasks from data and notify adapter (in case they have been altered in another layout)
        entries.clear();
        try (SQLiteHelper db = new SQLiteHelper(context)) {
            entries.addAll(db.loadDropped());
        }
        notifyDataSetChanged();

    }

}
