package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import net.berndreiss.zentodo.R;
import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.listeners.BackListListenerPick;
import net.berndreiss.zentodo.adapters.listeners.PickListener;
import net.berndreiss.zentodo.adapters.listeners.SetDateListenerPick;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Entry;
import net.berndreiss.zentodo.data.TaskList;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

/**
 *       Extends TaskListAdapter but does not remove tasks when CheckBox is clicked.
 *       The PickTaskListAdapter actually consists of four distinct adapters:
 *
 *       pickAdapter: contains all tasks that are due today.
 *       doNowAdapter: contains all tasks ticked in any of the three other adapters (and therefore tasks that haven been chosen for today's FOCUS)
 *       doLaterAdapter: contains all tasks for which a reminder date has been set in any of the other adapters
 *       moveToListAdapter: contains all tasks for which a list has been set, but that don't have a reminder date
 *
 *       If the pickAdapter is empty clicking the PickButton in MainActivity sends all ticked tasks to Focus.
 *       Because of that the Focus Button does not have much use in this adapter. Its new
 *       function is to delete tasks (being marked by a delete drawable).
 *
 */

public class PickTaskListAdapter extends TaskListAdapter implements PickListener {

    private PickTaskListAdapter pickAdapter;
    private PickTaskListAdapter doNowAdapter;
    private PickTaskListAdapter doLaterAdapter;
    private PickTaskListAdapter moveToListAdapter;
    private final boolean checkboxTicked;//Tasks that are in the doNowAdapter are ticked

    public PickTaskListAdapter(SharedData sharedData, boolean checkboxTicked){
        super(sharedData, new ArrayList<>());
        this.checkboxTicked = checkboxTicked;
    }

    public void setPickAdapter (PickTaskListAdapter pickAdapter){
        this.pickAdapter = pickAdapter;
    }
    public void setDoNowAdapter(PickTaskListAdapter doNowAdapter){ this.doNowAdapter = doNowAdapter; }
    public void setDoLaterAdapter(PickTaskListAdapter doLaterAdapter){ this.doLaterAdapter = doLaterAdapter; }
    public void setMoveToListAdapter (PickTaskListAdapter moveToListAdapter){ this.moveToListAdapter = moveToListAdapter; }
    public boolean isCheckboxTicked(){
        return checkboxTicked;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        holder.checkBox.setChecked(checkboxTicked);

        //listener for opening the menu
        holder.menu.setOnClickListener(v -> {
            //setting alternative row layout visible and active, everything else disabled
            setAlt(holder);
            //setting delete drawable VISIBLE
            holder.delete.setVisibility(View.VISIBLE);

            //setting focus drawable GONE
            holder.focus.setVisibility(View.GONE);
        });

        //on checkBox tick
        holder.checkBox.setOnClickListener(v ->{

            //get current entry
            Entry entry = entries.get(position);

            //if the entry is in DO NOW, remove it and check in which adapter it should go:
            // (1) -> has a date and date is > today -> doLaterAdapter
            // (2) -> has no date, but a list -> moveToListAdapter
            // (3) -> else move back to pickAdapter

            //if it is not in DO NOW -> move it to doNowAdapter
            if (doNowAdapter.entries.contains(entry)){
                doNowAdapter.entries.remove(entry);
                doNowAdapter.notifyDataSetChanged();
                doNowAdapter.itemCountChanged();

                //(1)
                if (entry.getReminderDate() != null && entry.getReminderDate().isAfter(Instant.now())){
                    doLaterAdapter.entries.add(entry);
                    doLaterAdapter.notifyDataSetChanged();
                    doLaterAdapter.itemCountChanged();
                } else
                //(2)
                if (entry.getReminderDate() == null && entry.getList() != null){
                    moveToListAdapter.entries.add(entry);
                    moveToListAdapter.notifyDataSetChanged();
                    moveToListAdapter.itemCountChanged();
                }
                //(3)
                else{
                    pickAdapter.entries.add(entry);
                    pickAdapter.notifyDataSetChanged();
                    pickAdapter.itemCountChanged();
                }



            }
            //move item to doNowAdapter
            else{
                doNowAdapter.entries.add(entry);
                doNowAdapter.notifyDataSetChanged();
                doNowAdapter.itemCountChanged();

                entries.remove(entry);
                notifyDataSetChanged();
                itemCountChanged();
            }

        });

        //the focus button is disabled in this view
        holder.focus.setOnClickListener(c -> {});

        holder.delete.setOnClickListener(v ->{
            //get current entry
            Entry entry = entries.get(position);

            //remove entry from data
            DataManager.remove(sharedData, this, entry);

            itemCountChanged();

            //notify adapter
            notifyDataSetChanged();
            itemCountChanged();

        });

        holder.setDate.setOnClickListener(new SetDateListenerPick(this, holder, position, pickAdapter, doLaterAdapter, moveToListAdapter));
        holder.backList.setOnClickListener(new BackListListenerPick(this,holder,position, pickAdapter, moveToListAdapter));

        if (entries.get(position).getList() != null){

            String color = DataManager.getListColor(sharedData, entries.get(position).getList());
            //color = color.substring(2,8);

            holder.linearLayout.setBackgroundColor(Color.parseColor( color));

        } else{
            holder.linearLayout.setBackgroundColor(ContextCompat.getColor(sharedData.context, R.color.color_primary));

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void reset(){


        //clear ArrayList for Pick, add current tasks from data and notify adapter (in case they have been altered in another layout)
        entries.clear();
        entries.addAll(DataManager.getTasksToPick(sharedData));

        //Reset and Update Adapters to reflect changes
        //Also update itemCountChanged, so that RecyclerViews get resized properly

        pickAdapter.notifyDataSetChanged();
        pickAdapter.itemCountChanged();

        doNowAdapter.entries.clear();
        doNowAdapter.notifyDataSetChanged();
        doNowAdapter.itemCountChanged();

        doLaterAdapter.entries.clear();
        doLaterAdapter.notifyDataSetChanged();
        doLaterAdapter.itemCountChanged();

        moveToListAdapter.entries.clear();
        moveToListAdapter.notifyDataSetChanged();
        moveToListAdapter.itemCountChanged();

    }

    @Override
    public void itemCountChanged(){}

}

