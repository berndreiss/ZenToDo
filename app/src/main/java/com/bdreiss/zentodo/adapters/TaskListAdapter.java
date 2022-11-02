package com.bdreiss.zentodo.adapters;
/*
 *   A custom ArrayAdapter<Entry> that creates rows with checkboxes that
 *   when checked remove the associated task.
 *
 *   The row can have different views. It initially starts with a default view, that shows the task and the checkbox to remove it.
 *   There is also a menu button that opens an alternative row view. In this view different Buttons are shown through which certain
 *   information of the task can be changed (for data fields associated with a task see Entry.java).
 *
 *   original view --(menu Button)--> alternative view --(today Button)--> default view
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

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> implements ItemTouchHelperAdapter {


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;//"normal" row layout that shows checkbox and task
        protected CheckBox checkBox;//Checkbox to remove entry
        public TextView task;//Description of the task
        public Button menu;//activates alternative layout with menu elements

        private final LinearLayout linearLayoutAlt;//"alternative" layout with menu for modifying entries
        public Button focus;//Adds task to todays tasks
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

    protected ArrayList<Entry> entries;

    protected Context context;

    protected Data data;

    public TaskListAdapter(Context context, Data data, ArrayList<Entry> entries){

        this.entries = entries;
        this.context = context;
        this.data = data;
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
        final Entry entry = entries.get(position);

        holder.task.setText(entry.getTask());
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

    @SuppressLint("NotifyDataSetChanged")
    protected void setCheckBoxListener(TaskListAdapter.ViewHolder holder, int position){
        holder.checkBox.setOnClickListener(view -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get ID

            boolean recurring = entry.getRecurrence()!=null;


            if (recurring) {
                data.setRecurring(id);
                data.setFocus(id,false);
                data.setDropped(id,false);
            } else {
                data.remove(id);
            }


            notifyDataSetChanged();

        });

    }

    protected void setMenuListener(TaskListAdapter.ViewHolder holder){
        //listener that changes to alternative row layout on click
        holder.menu.setOnClickListener(view -> {
            //setting alternative row layout visible and active, everything else disabled
            setAlt(holder);
            setMenuListener(holder);
        });

    }

    protected void setFocusListener(TaskListAdapter.ViewHolder holder, int position){
        holder.focus.setOnClickListener(view12 -> {
            Entry entry = entries.get(position);
            int id = entry.getId();//get id
            data.setDropped(id, false);
            data.setFocus(id, !entry.getFocus());//change state of focus in entry
            setOriginal(holder);
            setFocusListener(holder, position);
            markSet(holder,entry);

        });

    }

    protected void setEditListener(TaskListAdapter.ViewHolder holder){
        //onClickListener for Button to change the task name
        holder.edit.setOnClickListener(view13 -> {

            //setting edit row visible and active, everything else disabled
            setEdit(holder);

            //setting editText to task
            holder.editText.setText(holder.task.getText());

            setEditListener(holder);

        });

    }

    protected void setBackEditListener(TaskListAdapter.ViewHolder holder, int position){
        //Listener to write back changes
        holder.backEdit.setOnClickListener(view131 -> {
            int id = entries.get(position).getId();//getting the id of task
            String newTask = holder.editText.getText().toString();//get new Task
            if (!newTask.isEmpty()) {
                data.editTask(id, newTask);//save changes
                holder.task.setText(newTask);//set new Task in list
                setOriginal(holder);
            }

            setBackEditListener(holder, position);

        });
    }

    protected void setSetDateListener(TaskListAdapter.ViewHolder holder, int position){
        //Listener to add date when task is due
        holder.setDate.setOnClickListener(view14 -> {
            openDatePickerDialog(context,holder,position);//opens date pickerDialog
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

            data.editDue(entry.getId(),0);//set date when task is due to 0
            setOriginal(holder);
            markSet(holder,entry);
        });

        datePickerDialog.show();//Show the dialog
    }

    protected DatePickerDialog getDatePickerDialog(Entry entry, int entryDay, int entryMonth, int entryYear, TaskListAdapter.ViewHolder holder, int position){
        DatePickerDialog datePickerDialog;
        datePickerDialog= new DatePickerDialog(context, (view, year, month, day) -> {
            int date = year*10000+(month+1)*100+day;//Encode format "YYYYMMDD"
            data.editDue(entry.getId(), date);//Write back data
            setOriginal(holder);
            markSet(holder,entry);


        }, entryYear, entryMonth, entryDay);
        return datePickerDialog;
    }

    protected void setRecurrenceListener(TaskListAdapter.ViewHolder holder, int position){            //Listener to edit how often task repeats
        holder.recurrence.setOnClickListener(view15 -> {

            //setting recurrence row visible and active, everything else disabled
            setRecurrence(holder);

            //get entry to set spinner on current recurrence
            Entry entry = entries.get(position);
            String rec = entry.getRecurrence();

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
                    default://sets spinner to days

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
            setRecurrenceListener(holder, position);
        });
    }

    //reset all fields in recurrence row view
    protected void setClearRecurrenceListener(TaskListAdapter.ViewHolder holder){
        holder.clearRecurrence.setOnClickListener(view -> {
            holder.spinnerRecurrence.setSelection(0);
            holder.editTextRecurrence.setText("");
            setClearRecurrenceListener(holder);
        });

    }

    protected  void setBackRecurrenceListener(TaskListAdapter.ViewHolder holder, int position){
        //Listener for writing back data
        holder.backRecurrence.setOnClickListener(view151 -> {
            int id = entries.get(position).getId(); //get id

            String interval = holder.editTextRecurrence.getText().toString();//number of repeats
            int intervalInt;//the same number as Integer

            if (interval.equals("")){//if editText is empty number is set to 0 so data is reset
                intervalInt = 0;
            } else {
                intervalInt = Integer.parseInt(interval);//assign content otherwise
            }
            String recurrence = "";//String that will be written back

            if (intervalInt == 0){//if editText was empty or value=0 then recurrence is reset
                data.editRecurrence(id, null);
            }
            else{//otherwise number and interval are written back

                //add spinner values first character as lower case (d/w/m/y)
                recurrence += Character.toLowerCase(holder.spinnerRecurrence.getSelectedItem().toString().charAt(0));
                //add number of editText
                recurrence += interval;

                //write back
                data.editRecurrence(id,recurrence);
            }
            markSet(holder,entries.get(position));
            setOriginal(holder);
            setBackRecurrenceListener(holder, position);

        });
    }

    protected void setSetListListener(TaskListAdapter.ViewHolder holder, int position){
        //Listener for Button to change list task is assigned to
        holder.setList.setOnClickListener(view16 -> {
            setList(holder);//set list row view
            String[] array = data.returnListsAsArray();//array of names of all lists in task (as singletons)
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, array);
            holder.autoCompleteList.setAdapter(adapter);//edit Text that autocompletes already existing lists
            holder.autoCompleteList.setText(entries.get(position).getList());
            setSetListListener(holder,position);
        });

    }

    protected void setClearListenerList(TaskListAdapter.ViewHolder holder){
        holder.clearList.setOnClickListener(view -> {
            holder.autoCompleteList.setText("");
            setClearListenerList(holder);
        });
    }

    protected void setBackListListener(TaskListAdapter.ViewHolder holder, int position){

        holder.backList.setOnClickListener(view161 -> {
            int id = entries.get(position).getId();//Get id of task
            String list = holder.autoCompleteList.getText().toString();//get list name

            //set to no list if AutoComplete is empty
            if (list.trim().isEmpty()) {
                data.editList(id, null);//reset to no list
            } else {
                data.editList(id, list);//write back otherwise
            }
            setOriginal(holder);
            setBackListListener(holder, position);
            markSet(holder,entries.get(position));
        });
    }


    protected void setBackListener(TaskListAdapter.ViewHolder holder){
        //Listener to return to default layout
        holder.back.setOnClickListener(view17 -> {
            setOriginal(holder);//setting original row visible and active, everything else disabled
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
    private void setAlt(TaskListAdapter.ViewHolder holder){
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

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                data.swap(entries.get(i).getId(),entries.get(i+1).getId());
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                data.swap(entries.get(i).getId(),entries.get(i-1).getId());
            }
        }
        notifyItemMoved(fromPosition,toPosition);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemDismiss(int position) {
        entries.remove(position);
        notifyDataSetChanged();
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

    //Sets buttons that have edited data that has been set to a different color
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