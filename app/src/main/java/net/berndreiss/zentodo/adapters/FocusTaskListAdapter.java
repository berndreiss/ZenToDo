package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.listeners.SetDateListener;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;
import net.berndreiss.zentodo.data.TaskList;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

/**
 *      Shows all tasks the user chose to focus on today.
 *      Tasks are usually chosen in PICK but can also be manually added via the FOCUS-button in the menu of a task.
 *      Recurring tasks are added automatically to FOCUS (see also MainActivity.java).
 * <p>
 *      Extends TaskListAdapter but removes tasks when focus or date is changed.
 *
 */
public class FocusTaskListAdapter extends TaskListAdapter {

    public FocusTaskListAdapter(SharedData sharedData){
        super(sharedData, DataManager.getFocus(sharedData));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {

        super.onBindViewHolder(holder, position);

        if (tasks.get(position).getList() != null){

            Optional<TaskList> list = sharedData.database.getListManager().getList(tasks.get(position).getList());
            if (list.isEmpty())
                return;
            String color = list.get().getColor();
            if (color != null)
                holder.linearLayout.setBackgroundColor(Color.parseColor(color));
        } else{
            holder.linearLayout.setBackgroundColor(ContextCompat.getColor(sharedData.context,R.color.color_primary));

        }

        //set Listener for checkbox: basically if checked the item is removed, additionally motivating picture of Panda is shown when all tasks are completed
        holder.checkBox.setOnClickListener(view -> {

            //current task
            Task task = tasks.get(position);

            //see if item is recurring
            boolean recurring = task.getRecurrence()!=null;

            //if recurring do not remove but set new reminder date, otherwise remove from data
            if (recurring) {
                //calculate new reminder date and write to data and tasks
                DataManager.setRecurring(sharedData, task);

                //reset focus in data and tasks
                DataManager.setFocus(sharedData, task,false);

                //reset dropped in data and tasks
                DataManager.setDropped(sharedData, task,false);

            } else {
                DataManager.remove(sharedData, task);
            }

            if (tasks.isEmpty())
                showImage();


            notifyDataSetChanged();

        });



        //set Listener for the Focus Button in the menu, item is removed from adapter upon click
        holder.focus.setOnClickListener(view12 -> {
            //get task
            Task task = tasks.get(position);

            if (task.getRecurrence() != null)
                DataManager.addToRecurringButRemoved(sharedData, task.getId());

            if (task.getReminderDate() == null)
                DataManager.editReminderDate(sharedData, task, Instant.now());

            //write back change to data
            DataManager.setFocus(sharedData, task, false);
        });

        //return DatePickerDialog that writes back to data and removes item from adapter
        holder.setDate.setOnClickListener(new SetDateListener(this,holder,position){
            @Override
            public DatePickerDialog getDatePickerDialog(Task task, int taskDay, int taskMonth, int taskYear, ViewHolder holder, int position){
                DatePickerDialog datePickerDialog;

                datePickerDialog= new DatePickerDialog(sharedData.context, (view, year, month, day) -> {

                    LocalDate date = LocalDate.of(year, month + 1,day);

                    Instant dateInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

                    //write back to data
                    DataManager.editReminderDate(sharedData, task, dateInstant);

                    //only remove task from FOCUS, if date is in the future (a scenario where one
                    //might change the date to the past is recurring tasks -> in this scenario
                    //the task should remain in FOCUS)
                    if (date.isAfter(LocalDate.now())) {
                        //also set focus to false in data
                        DataManager.setFocus(sharedData, task, false);
                    }

                }, taskYear, taskMonth, taskDay);

                datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,sharedData.context.getResources().getString(R.string.cancel), (dialog, which) -> {

                    //set date when task is due to 0
                    DataManager.editReminderDate(sharedData, task,null);

                    //change color of reminder date Button marking if Date is set
                    markSet(holder,task);

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

    //TODO COMMENT
    private void showImage() {
        Dialog builder = new Dialog(sharedData.context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(builder.getWindow()).setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        builder.setOnDismissListener(dialogInterface -> {
            //nothing;
        });

        ImageView imageView = new ImageView(sharedData.context);

        //I don't care whatever you change in this code, but THIS has to be a PANDA!
        imageView.setImageResource(R.drawable.panda);


        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
        imageView.setOnClickListener(v -> builder.dismiss());
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void reset(){
        //clear ArrayList for Focus, add current tasks from data and notify adapter (in case they have been altered in another layout)
        tasks.clear();
        tasks.addAll(DataManager.getFocus(sharedData));
        for (Task t: tasks)
            t.setFocus(true);
        notifyDataSetChanged();

    }
}
