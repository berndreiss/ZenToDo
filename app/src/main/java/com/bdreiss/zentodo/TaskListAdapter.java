package com.bdreiss.zentodo;
/*
 *   A custom ArrayAdapter<Entry> that creates rows with checkboxes that
 *   when checked remove the associated task.
 *
 *   The row can have different views. It initially starts with an original view, that shows the task and the checkbox to remove it.
 *   But there is also a menu button that opens an alternative row view. In this view different Buttons are shown through which certain
 *   information of the task can be changed (for data fields associated with a task see Entry.java).
 *
 *   original view --(menu Button)--> alternative view --(today Button)--> original view
 *                                                     --(edit Button)--> edit row view --(back Button)--> original view
 *                                                     --(set date Button)--> datePickerDialog --(cancel/ok Button)-->original view
 *                                                     --(recurrence Button)--> recurrence view --(back Button)--> original view
 *                                                     --(set list Button)--> set list view --(back Button)-->  original view
 *                                                     --(back Button)--> original view
 *
 */

import static android.provider.Settings.System.getString;

import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

public class TaskListAdapter extends ArrayAdapter<Entry>{

    private ArrayList<Entry> entries;//list of entries (see Entry.java)

    private Context context;

    private Data data;//database from which entries are derived (see Data.java)

    private String mode;

    private ArrayList<Integer> idsChecked;

    private class ViewHolder {//temporary view

        private LinearLayout linearLayout;//"normal" row layout that shows checkbox and task
        protected CheckBox checkBox;//Checkbox to remove entry
        private TextView task;//Description of the task
        private Button menu;//activates alternative layout with menu elements

        private LinearLayout linearLayoutAlt;//"alternative" layout with menu for modifying entries
        private Button toTodays;//Adds task to todays tasks
        private Button edit;//edits the task
        private Button setDate;//sets the date the task is due
        private Button recurrence;//sets the frequency with which the task repeats
        private Button setList;//sets the list the task is assigned to
        private Button back;//returns to normal row layout

        private LinearLayout linearLayoutEdit;//row layout for task editing
        private EditText editText;//field to edit text
        private Button backEdit;//return to normal layout and save

        private LinearLayout linearLayoutRecurrence;//row layout for setting recurrence:
        private TextView textViewRecurrence;//"repeats every... TODO REMOVE?
        private EditText editTextRecurrence;//...number...
        private Spinner spinnerRecurrence;//...days/weeks/months/years"
        private Button clearRecurrence;//clears all fields
        private Button backRecurrence;//write back result

        private LinearLayout linearLayoutList;//row layout for setting lists
        private AutoCompleteTextView autoCompleteList;//AutoComplete to set new list
        private Button clearList;//clears AutoComplete
        private Button backList;//return to original layout and save
    }

    public TaskListAdapter(Context context, Data data, ArrayList<Entry> entries, String mode){

        //super(context, R.layout.row,data.getEntries());
        super(context,R.layout.row,entries);
        this.context = context;
        this.data = data;
        //this.entries = data.getEntries();
        this.entries = entries;
        this.mode = mode;
        this.idsChecked = new ArrayList<>();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        final ViewHolder holder;

        //Connect all the views of different layouts to the holder
        if (convertView == null) {
            holder = new ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row, null, true);

            holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.linear_layout);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.task = (TextView) convertView.findViewById(R.id.task);
            holder.menu = (Button) convertView.findViewById(R.id.button_menu);

            holder.linearLayoutAlt = (LinearLayout) convertView.findViewById(R.id.linear_layout_alt);
            holder.toTodays = (Button) convertView.findViewById(R.id.button_to_todays);
            holder.edit = (Button) convertView.findViewById(R.id.button_edit);
            holder.setDate = (Button) convertView.findViewById(R.id.button_calendar);
            holder.recurrence = (Button) convertView.findViewById(R.id.button_recurrence);
            holder.setList = (Button) convertView.findViewById(R.id.button_list);
            holder.back = (Button) convertView.findViewById(R.id.button_back);

            holder.linearLayoutEdit = (LinearLayout) convertView.findViewById(R.id.linear_layout_edit);
            holder.editText = (EditText) convertView.findViewById(R.id.edit_text_list_view);
            holder.backEdit = (Button) convertView.findViewById(R.id.button_back_edit);

            holder.linearLayoutRecurrence = (LinearLayout) convertView.findViewById(R.id.linear_layout_recurrence);
            holder.textViewRecurrence = (TextView) convertView.findViewById(R.id.text_view_recurrence);
            holder.editTextRecurrence = (EditText) convertView.findViewById(R.id.edit_text_recurrence);
            holder.spinnerRecurrence = (Spinner) convertView.findViewById(R.id.spinner_time_interval);
            holder.clearRecurrence = (Button) convertView.findViewById(R.id.button_recurrence_clear);
            holder.backRecurrence = (Button) convertView.findViewById(R.id.button_back_recurrence);

            holder.linearLayoutList = (LinearLayout) convertView.findViewById(R.id.linear_layout_list);
            holder.autoCompleteList = (AutoCompleteTextView) convertView.findViewById(R.id.auto_complete_list);
            holder.clearList = (Button) convertView.findViewById(R.id.button_list_clear);
            holder.backList = (Button) convertView.findViewById(R.id.button_back_list);

            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        //set up checkbox of the task
        //holder.task.setText(entries.get(position).getTask());
        holder.task.setText(entries.get(position).getTask());
        holder.checkBox.setChecked(false);
        holder.checkBox.setTag(position);

        //establish routine to remove task when checkbox is clicked
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = entries.get(position).getId();//get ID

                if (!mode.equals("potentials")) {
                    data.remove(id);//remove entry from dataset by ID
                    notifyDataSetChanged();//update the adapter
                }else{
                    if (idsChecked.contains(id)){
                        idsChecked.remove(id);
                    } else{
                        idsChecked.add(id);
                    }
                }

            }
        });



        //Creating adapter for spinner with entries days/weeks/months/years
        /* Adapter has to be declared here so that the dropdown element of the spinner is not shown in the background */
        /* I do not understand why, but this fixed it */
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(context,R.array.time_interval, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        holder.spinnerRecurrence.setAdapter(adapterSpinner);

        initialize(holder,position);
        return convertView;
    }

    public void initialize(ViewHolder holder, int position){
        //setting "normal" row visible and active and all others to invisible and invalid
        setOriginal(holder);

        //Sets buttons that have edited data that has been set to a different color
        markSet(holder,this.entries.get(position));

        //listener that changes to alternative row layout on click
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setting alternative row visible and active, everything else disabled
                setAlt(holder);

                holder.back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initialize(holder,position);//return
                    }
                });

                holder.toTodays.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int id = entries.get(position).getId();
                        data.setToday(id, !entries.get(position).getToday());
                        if (mode.equals("todays")){
                                notifyDataSetChanged();
                        } else {

                            if (entries.get(position).getDropped()) {
                                data.setDropped(id, false);
                                if (mode.equals("dropped")) {
                                    notifyDataSetChanged();
                                } else {
                                    initialize(holder, position);
                                }
                            } else {

                                initialize(holder, position);
                            }
                        }
                    }
                });

                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //setting edit row visible and active, everything else disabled
                        setEdit(holder);

                        //setting editText to task
                        holder.editText.setText(holder.task.getText());

                        //Listener to write back changes
                        holder.backEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int id = entries.get(position).getId();//getting the id of task
                                String newTask = holder.editText.getText().toString();//get new Task
                                data.editTask(id,newTask);//save changes
                                holder.task.setText(newTask);//set new Task in list
                                initialize(holder,position);//returning to original row view
                            }
                        });

                    }
                });

                //Listener to add date when task is due
                holder.setDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = getContext();
                        openDatePickerDialog(context,holder,position);//opens date pickerDialog
                    }
                });

                //Listener to edit how often task repeats
                holder.recurrence.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //setting recurrence row visible and active, everything else disabled
                        setRecurrence(holder);

                        //get entry to set spinner on current recurrence
                        Entry entry = entries.get(position);
                        String rec = entry.getRecurrence();

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
                        String recEdit = "";

                        //add all digits after the first char
                        for (int i=1;i<rec.length();i++){
                            recEdit += rec.charAt(i);
                        }

                        //set editText
                        holder.editTextRecurrence.setText(recEdit);

                        //setting listener to clear fields
                        setClearRecurrenceListener(holder);

                        //Listener for writing back data
                        holder.backRecurrence.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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
                                    data.editRecurrence(id, " ");
                                }
                                else{//otherwise number and interval are written back

                                    //add spinner values first character as lower case (d/w/m/y)
                                    recurrence += Character.toLowerCase(holder.spinnerRecurrence.getSelectedItem().toString().charAt(0));
                                    //add number of editText
                                    recurrence += interval;

                                    //write back
                                    data.editRecurrence(id,recurrence);
                                }
                                initialize(holder,position);//returning to original row view


                            }
                        });
                    }
                });

                holder.setList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setList(holder);//set list row view
                        String array[] = data.returnListsAsArray();//array of names of all lists in task (as singletons)
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,array);
                        holder.autoCompleteList.setAdapter(adapter);//edit Text that autocompletes already existing lists
                        holder.autoCompleteList.setText(entries.get(position).getList());
                        setClearListenerList(holder);//clears AutoComplete
                        holder.backList.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View view) {
                                int id = entries.get(position).getId();//Get id of task
                                String list = holder.autoCompleteList.getText().toString();//get list name
                                if (list.equals("")){
                                    data.editList(id," ");//reset to no list
                                    initialize(holder,position);//returning to original row view

                                } else{
                                    data.editList(id,list);//write back otherwise
                                    if (entries.get(position).getDropped()){
                                        data.setDropped(id,false);
                                        if (mode.equals("dropped")) {
                                            notifyDataSetChanged();
                                        } else{
                                            initialize(holder,position);
                                        }


                                    } else{

                                        initialize(holder,position);//returning to original row view

                                    }
                                }




                            }
                        });

                    }
                });

                //Listener to return to "normal" view
                holder.back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setOriginal(holder);//setting original row visible and active, everything else disabled

                    }
                });
            }
        });

    }

    //clears all fields in recurrence row view
    public void setClearRecurrenceListener(ViewHolder holder){
        holder.clearRecurrence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.spinnerRecurrence.setSelection(0);
                holder.editTextRecurrence.setText("");
                setClearRecurrenceListener(holder);
            }
        });

    }

    //clears Autocomplete in list row view
    public void setClearListenerList(ViewHolder holder){
        holder.clearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.autoCompleteList.setText("");
                setClearListenerList(holder);
            }
        });
    }

    //returns the entry at specified position
    public Entry getEntry(int position){
        return entries.get(position);
    }

    //chooses date task is due and writes back data, if cancel is pressed, date is set to 0
    public void openDatePickerDialog(Context context,ViewHolder holder, int position) {

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
            entryMonth = ((entryDate%10000)-entryDay)/100;
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
        DatePickerDialog datePickerDialog;
        datePickerDialog= new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {
                int date = year*10000+(month+1)*100+day;//Encode format "YYYYMMDD"
                data.editDue(entry.getId(), date);//Write back data
                    if (mode.equals("dropped")){
                        data.setDropped(entries.get(position).getId(), false);
                        notifyDataSetChanged();
                    } else if( mode.equals("todays")){
                        data.setToday(entries.get(position).getId(),false);
                        notifyDataSetChanged();
                    } else
                    {
                        initialize(holder, position);
                    }

            }
        }, entryYear, entryMonth, entryDay);

        //set listener for cancel button
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){

                data.editDue(entry.getId(),0);//set date when task is due to 0
                initialize(holder,position);//returning to original row view
            }

        });

        datePickerDialog.show();//Show the dialog
    }

    //Setting original row view
    public void setOriginal(ViewHolder holder){
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
    public void setAlt(ViewHolder holder){
        //Set alternative row view to visible and active
        holder.linearLayoutAlt.bringToFront();
        holder.linearLayoutAlt.setAlpha(1);
        enable(holder.linearLayoutAlt);

        //Set original row view to invisible and inactive
        holder.linearLayout.setAlpha(0);
        disable(holder.linearLayout);

    }

    //Setting edit row view coming from alternative view
    public void setEdit(ViewHolder holder){

        //Set edit row view to visible and active
        holder.linearLayoutEdit.bringToFront();
        holder.linearLayoutEdit.setAlpha(1);
        enable(holder.linearLayoutEdit);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);

    }


    //Setting recurrence row view coming from alternative view
    public void setRecurrence(ViewHolder holder){
        //Set recurrence row view to visible and active
        holder.linearLayoutRecurrence.bringToFront();
        holder.linearLayoutRecurrence.setAlpha(1);
        enable(holder.linearLayoutRecurrence);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);
    }

    //Setting list row view coming from alternative view
    public void setList(ViewHolder holder){
        //Set list row view to visible and active
        holder.linearLayoutList.bringToFront();
        holder.linearLayoutList.setAlpha(1);
        enable(holder.linearLayoutList);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);



    }

    //Disables view and first generation children
    public void disable(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);

        }

    }

    //Enables view and first generation children
    public void enable(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }

    }

    //Sets buttons that have edited data that has been set to a different color
    public void markSet(ViewHolder holder,Entry entry){

        //Set date button to alternative color if !=0, original color otherwise
        if (entry.getDue()!=0){
            holder.setDate.setBackground(context.getResources().getDrawable(R.drawable.button_alt_edited));
        }else{
            holder.setDate.setBackground(context.getResources().getDrawable(R.drawable.button_alt));
        }

        //Set recurrence button to alternative color if ==" ", original color otherwise
        if (!entry.getRecurrence().equals(" ")){
            holder.recurrence.setBackground(context.getResources().getDrawable(R.drawable.button_alt_edited));
        }else{
            holder.recurrence.setBackground(context.getResources().getDrawable(R.drawable.button_alt));
        }

        //Set list button to alternative color if ==" ", original color otherwise
        if (!entry.getList().equals(" ")){
            holder.setList.setBackground(context.getResources().getDrawable(R.drawable.button_alt_edited));
        }else{
            holder.setList.setBackground(context.getResources().getDrawable(R.drawable.button_alt));
        }

        if (entry.getToday()){
            holder.toTodays.setBackground(context.getResources().getDrawable(R.drawable.button_alt_edited));
        } else{
            holder.toTodays.setBackground(context.getResources().getDrawable(R.drawable.button_alt));
        }

    }

    public ArrayList<Integer> getIdsChecked(){
        return idsChecked;
    }

    public ArrayList<Integer> getIdsNotChecked(){
        ArrayList<Integer> notChecked = new ArrayList<>();

        for (Entry e: entries){
            if (!idsChecked.contains(e.getId())){
                notChecked.add(e.getId());
            }
        }
        return notChecked;
    }
}