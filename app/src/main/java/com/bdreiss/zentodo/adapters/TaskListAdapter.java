package com.bdreiss.zentodo.adapters;
/*
 *   A custom ArrayAdapter<Entry> that creates rows with checkboxes that
 *   when checked remove the associated task.
 *
 *   The row can have different views. It initially starts with a default view, that shows the task and the checkbox to remove it.
 *   There is also a menu button that opens an alternative row view. In this view different Buttons are shown through which certain
 *   information of the task can be changed (for data fields associated with a task see Entry.java).
 *
 *   original view --(menu Button)--> alternative view --(focus Button)--> default view
 *                                                     --(edit Button)--> edit task view --(back Button)--> original view
 *                                                     --(set date Button)--> datePickerDialog --(cancel/ok Button)-->original view
 *                                                     --(recurrence Button)--> recurrence view --(back Button)--> original view
 *                                                     --(set list Button)--> set list view --(back Button)-->  original view
 *                                                     --(back Button)--> original view
 *
 */


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bdreiss.zentodo.R;
import com.bdreiss.zentodo.adapters.recyclerViewHelper.ItemTouchHelperAdapter;
import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> implements ItemTouchHelperAdapter {


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;//"normal" row layout that shows checkbox and task
        protected CheckBox checkBox;//Checkbox to remove entry
        public TextView task;//Description of the task
        public Button menu;//activates alternative layout with menu elements

        private final LinearLayout linearLayoutAlt;//"alternative" layout with menu for modifying entries
        public Button focus;//Adds task to todays tasks
        public Button delete;//Adds task to todays tasks
        public Button edit;//edits the task
        public Button setDate;//sets the date the task is due
        public Button recurrence;//sets the frequency with which the task repeats
        public Button setList;//sets the list the task is assigned to
        public Button back;//returns to normal row layout

        public LinearLayout linearLayoutEdit;//row layout for task editing
        public EditText editText;//field to edit text
        public Button backEdit;//return to normal layout and save

        public LinearLayout linearLayoutRecurrence;//row layout for setting recurrence:
        //TextView                                 //"repeats every...
        public EditText editTextRecurrence;//...number...
        public Spinner spinnerRecurrence;//...days/weeks/months/years"
        public Button clearRecurrence;//clears all fields
        public Button backRecurrence;//write back result

        public LinearLayout linearLayoutList;//row layout for setting lists
        public AutoCompleteTextView autoCompleteList;//AutoComplete to set new list
        public Button clearList;//clears AutoComplete
        public Button backList;//return to original layout and save

        public ViewHolder(View view){

            super(view);

            linearLayout = view.findViewById(R.id.linear_layout);
            checkBox = view.findViewById(R.id.checkbox);
            task = view.findViewById(R.id.task);
            menu = view.findViewById(R.id.button_menu);

            linearLayoutAlt = view.findViewById(R.id.linear_layout_alt);
            focus = view.findViewById(R.id.button_focus);
            delete = view.findViewById(R.id.button_delete);
            edit = view.findViewById(R.id.button_edit);
            setDate = view.findViewById(R.id.button_calendar);
            recurrence = view.findViewById(R.id.button_recurrence);
            setList = view.findViewById(R.id.button_list);
            back = view.findViewById(R.id.button_back);

            linearLayoutEdit = view.findViewById(R.id.linear_layout_edit);
            editText = view.findViewById(R.id.edit_text_list_view);
            backEdit = view.findViewById(R.id.button_back_edit);

            linearLayoutRecurrence = view.findViewById(R.id.linear_layout_recurrence);
            editTextRecurrence = view.findViewById(R.id.edit_text_recurrence);
            spinnerRecurrence = view.findViewById(R.id.spinner_time_interval);
            clearRecurrence = view.findViewById(R.id.button_recurrence_clear);
            backRecurrence = view.findViewById(R.id.button_back_recurrence);

            linearLayoutList = view.findViewById(R.id.linear_layout_list);
            autoCompleteList = view.findViewById(R.id.auto_complete_list);
            clearList = view.findViewById(R.id.button_list_clear);
            backList = view.findViewById(R.id.button_back_list);
        }
    }

    protected ArrayList<Entry> entries;//ArrayList that holds task shown in RecyclerView

    protected Context context;

    protected Data data;

    public TaskListAdapter(Context context, Data data, ArrayList<Entry> entries){
        this.data = data;
        this.entries = entries;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskListAdapter.ViewHolder holder, int position) {

        //set TextView to task
        holder.task.setText(entries.get(position).getTask());

        //set up checkbox of the task
        holder.checkBox.setChecked(false);
        holder.checkBox.setTag(position);

        //establish routine to remove task when checkbox is clicked
        setCheckBoxListener(holder, position);

        //Creating adapter for spinner with entries days/weeks/months/years
        /* Adapter has to be declared here so that the dropdown element of the spinner is not shown in the background */
        /* I do not understand why, but this fixed it */
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(context,R.array.time_interval, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        holder.spinnerRecurrence.setAdapter(adapterSpinner);

        //setting default row layout visible and active and all others to invisible and invalid
        setOriginal(holder);


        //sets buttons that have edited data that has been set to a different color
        markSet(holder,this.entries.get(position));
        setMenuListener(holder);

        //setting Entry.focus to true/false, which means the task is/is not listed in focus mode
        setFocusListener(holder, position);
        setEditListener(holder);
        setBackEditListener(holder, position);
        setSetDateListener(holder, position);
        setRecurrenceListener(holder, position);
        setClearRecurrenceListener(holder);
        setBackRecurrenceListener(holder, position);
        setSetListListener(holder,position);
        setClearListenerList(holder);
        setBackListListener(holder,position);
        setBackListener(holder);


    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return entries.size();
    }

    //set Listener for checkbox: basically if checked the item is removed
    @SuppressLint("NotifyDataSetChanged")//although notifyDataSetChanged might not be ideal the graphics are much smoother
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
            notifyDataSetChanged();
        });

    }

    //set Listener for showing menu of task
    protected void setMenuListener(TaskListAdapter.ViewHolder holder){
        //listener that changes to alternative row layout on click
        holder.menu.setOnClickListener(view -> {
            //setting alternative row layout visible and active, everything else disabled
            setAlt(holder);
            setMenuListener(holder); //reset Listener
        });

    }

    //set Listener for the Focus Button in the menu, item is being marked/unmarked as focused
    protected void setFocusListener(TaskListAdapter.ViewHolder holder, int position){
        holder.focus.setOnClickListener(view12 -> {

            //get current entry
            Entry entry = entries.get(position);

            boolean focused = entry.getFocus();

            //get ID for manipulation in data
            int id = entry.getId();//get id

            //set dropped to false: as soon as item is in focus it is not dropped anymore
            data.setDropped(id, false);

            //change state of focus in entry
            data.setFocus(id, !focused);
            entry.setFocus(!focused);

            //change color of Focus Button marking whether task is focused or not
            markSet(holder,entry);

            //notify the adapter
            notifyItemChanged(position);

            //return to original row layout
            setOriginal(holder);

            //reset the Listener
            setFocusListener(holder, position);

        });

    }

    //set Listener for Edit Task Button
    protected void setEditListener(TaskListAdapter.ViewHolder holder){
        //onClickListener for Button to change the task name
        holder.edit.setOnClickListener(view13 -> {

            //setting edit row visible and active, everything else disabled
            setEdit(holder);

            //setting editText to task
            holder.editText.setText(holder.task.getText());

            //reset Listener
            setEditListener(holder);

        });

    }

    //set Listener for returning from Edit Task
    protected void setBackEditListener(TaskListAdapter.ViewHolder holder, int position){

        //Listener to write back changes
        holder.backEdit.setOnClickListener(view131 -> {

            //getting the id of task
            int id = entries.get(position).getId();

            //get new Task from EditText
            String newTask = holder.editText.getText().toString();

            //if EditText is not empty write changes to data and return to original layout
            if (!newTask.isEmpty()) {

                //save new task description in data
                data.setTask(id, newTask);
                entries.get(position).setTask(newTask);

                //notify adapter
                notifyItemChanged(position);

                //return to original layout
                setOriginal(holder);
            }

            //reset Listener
            setBackEditListener(holder, position);

        });
    }

    //set Listener for Due Date Button: sets a new Date when task is due
    protected void setSetDateListener(TaskListAdapter.ViewHolder holder, int position){

        //Listener to add date when task is due
        holder.setDate.setOnClickListener(view14 -> {

            //opens date pickerDialog
            openDatePickerDialog(context,holder,position);

            //reset Listener
            setSetDateListener(holder, position);
        });
    }

    //choose date for which task is due and write back data, if "no date" is pressed, date is set to 0
    protected void openDatePickerDialog(Context context, TaskListAdapter.ViewHolder holder, int position) {

        //Get entry
        Entry entry = entries.get(position);

        //get current date when task is due
        int entryDate = entry.getDue();

        //variables for setting picker
        int entryDay;
        int entryMonth;
        int entryYear;

        //if current date greater then 0 set value, otherwise set today
        if(entryDate>0) {

            //resolve format "yyyymmdd"
            entryDay = entryDate%100;
            entryMonth = ((entryDate%10000)-entryDay)/100-1;
            entryYear = (entryDate-entryMonth*100-entryDay)/10000;
        }
        else {

            //get todays date
            Calendar c = Calendar.getInstance();
            entryYear = c.get(Calendar.YEAR);
            entryMonth = c.get(Calendar.MONTH);
            entryDay = c.get(Calendar.DAY_OF_MONTH);
        }

        //create DatePickerDialog and set listener
        DatePickerDialog datePickerDialog = getDatePickerDialog(entry, entryDay,entryMonth,entryYear,holder,position);

        //set listener for cancel button
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,context.getResources().getString(R.string.cancel), (dialog, which) -> {

            //set date when task is due to 0
            data.editDue(entry.getId(),0);
            entries.get(position).setDue(0);

            //change color of Due Date Button marking if Date is set
            markSet(holder,entry);

            //notify adapter
            notifyItemChanged(position);

            //return to original layout
            setOriginal(holder);
        });

        //show the dialog
        datePickerDialog.show();
    }

    //return DatePickerDialog
    protected DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, TaskListAdapter.ViewHolder holder, int position){

        //DatePickerDialog to be returned
        DatePickerDialog datePickerDialog;

        //initialize DatePickerDialog
        datePickerDialog= new DatePickerDialog(context, (view, year, month, day) -> {

            //Encode format "YYYYMMDD"
            int date = year*10000+(month+1)*100+day;

            //Write back data
            data.editDue(entry.getId(), date);
            entries.get(position).setDue(date);

            //change color of Due Date Button marking if Date is set
            markSet(holder,entry);

            //notify adapter
            notifyItemChanged(position);

            //return to original row layout
            setOriginal(holder);

        }, entryYear, entryMonth, entryDay);

        return datePickerDialog;
    }

    //Listener to edit how often task repeats
    protected void setRecurrenceListener(TaskListAdapter.ViewHolder holder, int position){
        holder.recurrence.setOnClickListener(view15 -> {

            //setting recurrence row visible and active, everything else disabled
            setRecurrence(holder);

            //get entry to set spinner on current recurrence
            Entry entry = entries.get(position);
            String rec = entry.getRecurrence();

            //initialize spinner if recurrence is set
            if (rec!=null) {
                //set spinner according to first letter y/w/m/y
                switch (rec.charAt(0)) {
                    case 'd':
                        holder.spinnerRecurrence.setSelection(0);
                        break;
                    case 'w':
                        holder.spinnerRecurrence.setSelection(1);
                        break;
                    case 'm':
                        holder.spinnerRecurrence.setSelection(2);
                        break;
                    case 'y':
                        holder.spinnerRecurrence.setSelection(3);
                        break;
                    default://sets spinner to days but this should actually never happen

                }

                //String for editText
                StringBuilder recEdit = new StringBuilder();

                //add all digits after the first char
                for (int i = 1; i < rec.length(); i++) {
                    recEdit.append(rec.charAt(i));
                }

                //set editText
                holder.editTextRecurrence.setText(recEdit.toString());
            }

            //reset Listener
            setRecurrenceListener(holder, position);
        });
    }

    //reset all fields in recurrence row view to default
    protected void setClearRecurrenceListener(TaskListAdapter.ViewHolder holder){
        holder.clearRecurrence.setOnClickListener(view -> {

            //reset spinner to days
            holder.spinnerRecurrence.setSelection(0);

            //reset edit Text to empty
            holder.editTextRecurrence.setText("");

            //reset Listener
            setClearRecurrenceListener(holder);
        });

    }

    //set Listener for returning from Recurrence Editing layout and writing back changes
    protected  void setBackRecurrenceListener(TaskListAdapter.ViewHolder holder, int position){

        //Listener for writing back data
        holder.backRecurrence.setOnClickListener(view151 -> {

            //get id for manipulation in data
            int id = entries.get(position).getId(); //get id

            //number of repeats
            String interval = holder.editTextRecurrence.getText().toString();

            //the same number but as Integer
            int intervalInt;

            //if editText is empty number is set to 0, assign content otherwise
            if (interval.equals("")){
                intervalInt = 0;
            } else {
                intervalInt = Integer.parseInt(interval);
            }

            //String that will be written back
            String recurrence = "";

            //if editText was empty or value=0 then recurrence is set to null, otherwise number and interval are written back
            if (intervalInt == 0){
                data.editRecurrence(id, null);
                entries.get(position).setRecurrence(null);
            }
            else{

                //add spinner values first character as lower case (d/w/m/y)
                recurrence += Character.toLowerCase(holder.spinnerRecurrence.getSelectedItem().toString().charAt(0));

                //add number of editText
                recurrence += interval;

                //write back
                data.editRecurrence(id,recurrence);
                entries.get(position).setRecurrence(recurrence);

            }

            //change color of recurrence Button to mark if recurrence is set
            markSet(holder,entries.get(position));

            //notify adapter
            notifyItemChanged(position);

            //return to original row layout
            setOriginal(holder);

            //reset Listener
            setBackRecurrenceListener(holder, position);

        });
    }

    //set Listener for setting list of Task
    protected void setSetListListener(TaskListAdapter.ViewHolder holder, int position){

        //Listener for Button to change list task is assigned to
        holder.setList.setOnClickListener(view16 -> {
            //set list row view
            setList(holder);

            //array of names of all lists in task (as singletons)
            String[] array = data.returnListsAsArray();

            //initialize adapter with all existing list options
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, array);

            //edit Text that autocompletes with already existing lists
            holder.autoCompleteList.setAdapter(adapter);

            //set edit Text to current list
            holder.autoCompleteList.setText(entries.get(position).getList());

            //reset Listener
            setSetListListener(holder,position);
        });

    }

    //clear edit Text when editing list
    protected void setClearListenerList(TaskListAdapter.ViewHolder holder){
        holder.clearList.setOnClickListener(view -> {

            //set edit Text to empty
            holder.autoCompleteList.setText("");

            //reset Listener
            setClearListenerList(holder);
        });
    }

    //set Listener for returning from editing list and write back data
    protected void setBackListListener(TaskListAdapter.ViewHolder holder, int position){

        holder.backList.setOnClickListener(view161 -> {

            //Get id of task
            int id = entries.get(position).getId();

            //get new list name
            String list = holder.autoCompleteList.getText().toString();

            //get old list name
            String oldList = entries.get(position).getList();

            //if there was a list before and it was different to the new one, decrement the count of items in this list
            // (this counter is used to keep track of list positions -> see Data.java)
            if (oldList != null && !list.equals(oldList))
                data.decrementListHashPositionCount(oldList, entries.get(position).getListPosition());

            //set to no list if AutoComplete is empty
            if (list.trim().isEmpty()) {
                //reset to no list
                entries.get(position).setListPosition(data.editList(id, null));
                entries.get(position).setList(null);
            } else {
                //write back otherwise
                entries.get(position).setListPosition(data.editList(id, list));
                entries.get(position).setList(list);
            }

            //change Color of setList Button to mark if list is set
            markSet(holder,entries.get(position));

            //notify adapter
            notifyItemChanged(position);

            //return to original row layout
            setOriginal(holder);

            //reset Listener
            setBackListListener(holder, position);

        });
    }

    //set Listener for returning from main menu
    protected void setBackListener(TaskListAdapter.ViewHolder holder){
        //Listener to return to default layout
        holder.back.setOnClickListener(view17 -> {
            //setting original row visible and active, everything else disabled
            setOriginal(holder);

            //reset Listener
            setBackListener(holder);
        });

    }

    //Setting original row view
    protected void setOriginal(TaskListAdapter.ViewHolder holder){
        //Set original row view to visible and active
        holder.linearLayout.setAlpha(1);
        enable(holder.linearLayout);
        holder.linearLayout.bringToFront();

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);

        //Set edit row view to invisible and inactive
        holder.linearLayoutEdit.setAlpha(0);
        disable(holder.linearLayoutEdit);

        //Set recurrence row view to invisible and inactive
        holder.linearLayoutRecurrence.setAlpha(0);
        disable(holder.linearLayoutRecurrence);

        //Set list row view to invisible and inactive
        holder.linearLayoutList.setAlpha(0);
        disable(holder.linearLayoutList);
    }

    //Setting alternative row view coming from original
    protected void setAlt(TaskListAdapter.ViewHolder holder){
        //Set alternative row view to visible and active
        holder.linearLayoutAlt.bringToFront();
        holder.linearLayoutAlt.setAlpha(1);
        enable(holder.linearLayoutAlt);

        //Set original row view to invisible and inactive
        holder.linearLayout.setAlpha(0);
        disable(holder.linearLayout);

    }

    //Setting edit row view coming from alternative view
    private void setEdit(TaskListAdapter.ViewHolder holder){

        //Set edit row view to visible and active
        holder.linearLayoutEdit.bringToFront();
        holder.linearLayoutEdit.setAlpha(1);
        enable(holder.linearLayoutEdit);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);

    }

    //Setting recurrence row view coming from alternative view
    private void setRecurrence(TaskListAdapter.ViewHolder holder){
        //Set recurrence row view to visible and active
        holder.linearLayoutRecurrence.bringToFront();
        holder.linearLayoutRecurrence.setAlpha(1);
        enable(holder.linearLayoutRecurrence);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);
    }

    //Setting list row view coming from alternative view
    private void setList(TaskListAdapter.ViewHolder holder){
        //Set list row view to visible and active
        holder.linearLayoutList.bringToFront();
        holder.linearLayoutList.setAlpha(1);
        enable(holder.linearLayoutList);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);

    }

    //write item movement to data and notify adapter: tasks can be dragged and dropped.
    // Upon drop notifyDataSetChanged is invoked -> see recyclerViewHelper.CustomItemTouchHelperCallback
    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        //swap entries in data distinguishing between item being moved up or down
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                data.swap(entries.get(i).getId(),entries.get(i+1).getId());
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                data.swap(entries.get(i).getId(),entries.get(i-1).getId());
            }
        }

        //swap items in entries
        Collections.swap(entries, fromPosition, toPosition);

        //notify the adapter
        notifyItemMoved(fromPosition,toPosition);

    }

    //Disables view and first generation children
    private void disable(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);

        }

    }

    //Enables view and first generation children
    private void enable(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }

    }

    //marks Buttons that have been set in a different color
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void markSet(TaskListAdapter.ViewHolder holder, Entry entry){

        //Set date button to alternative color if !=0, original color otherwise
        if (entry.getDue()!=0){
            holder.setDate.setBackground(context.getResources().getDrawable(R.drawable.button_alt_edited));
        }else{
            holder.setDate.setBackground(context.getResources().getDrawable(R.drawable.button_alt));
        }

        //Set recurrence button to alternative color if !isEmpty(), original color otherwise
        if (entry.getRecurrence()!=null){
            holder.recurrence.setBackground(context.getResources().getDrawable(R.drawable.button_alt_edited));
        }else{
            holder.recurrence.setBackground(context.getResources().getDrawable(R.drawable.button_alt));
        }

        //Set list button to alternative color if !isEmpty(), original color otherwise
        if (entry.getList()!=null){
            holder.setList.setBackground(context.getResources().getDrawable(R.drawable.button_alt_edited));
        }else{
            holder.setList.setBackground(context.getResources().getDrawable(R.drawable.button_alt));
        }

        if (entry.getFocus()){
            holder.focus.setBackground(context.getResources().getDrawable(R.drawable.button_alt_edited));
        } else{
            holder.focus.setBackground(context.getResources().getDrawable(R.drawable.button_alt));
        }

    }

}