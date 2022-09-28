package com.bdreiss.zentodo;
/*
 *   A custom ArrayAdapter<Entry> that creates rows with checkboxes that
 *   when checked remove the associated task.
 */

import static android.provider.Settings.System.getString;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;
import java.util.Calendar;

public class TaskListAdapter extends ArrayAdapter<Entry>{

    private ArrayList<Entry> entries;//list of entries (see Entry.java)

    private Context context;

    private Data data;//database from which entries are derived (see Data.java)

    private class ViewHolder {//temporary view

        private LinearLayout linearLayout;//"normal" row layout that shows checkbox and task
        protected CheckBox checkBox;//Checkbox to remove entry
        private TextView task;//Description of the task
        private Button menu;//activates alternative layout with menu elements

        private LinearLayout linearLayoutAlt;//"alternative" layout with menu for modifying entries
        private Button edit;//edits the task
        private Button setDate;//sets the date the task is due
        private Button recurrence;//sets the frequency with which the task repeats
        private Button setList;//sets the list the task is assigned to
        private Button back;//returns to normal row layout

        private LinearLayout linearLayoutEdit;//row layout for task editing
        private EditText editText;//field to edit text
        private Button backEdit;//return to normal layout and save

        private LinearLayout linearLayoutRecurrence;
        private TextView textViewRecurrence;
        private EditText editTextRecurrence;
        private Spinner spinnerRecurrence;
        private Button backRecurrence;
    }

    public TaskListAdapter(Context context, Data data){
        super(context, R.layout.row,data.getEntries());
        this.context = context;
        this.data = data;
        this.entries = data.getEntries();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row, null, true);

            holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.linear_layout);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.task = (TextView) convertView.findViewById(R.id.task);
            holder.menu = (Button) convertView.findViewById(R.id.button_menu);

            holder.linearLayoutAlt = (LinearLayout) convertView.findViewById(R.id.linear_layout_alt);
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
            holder.backRecurrence = (Button) convertView.findViewById(R.id.button_back_recurrence);


            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        //set Text of the task
        holder.task.setText(entries.get(position).getTask());

        holder.checkBox.setChecked(false);

        holder.checkBox.setTag(position);

        //establish routine to remove task when checkbox is clicked
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = entries.get(position).getId();
                data.remove(id);//remove entry from dataset by ID
                notifyDataSetChanged();//update the adapter

            }
        });

        //setting "normal" row visible and active and all others to invisible and invalid
        setOriginal(holder);//TODO: for some reason element of spinner is shown

        //listener that changes to alternative row layout on click
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setting alternative row visible and active
                setAlt(holder);

                holder.back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        notifyDataSetChanged();
                    }
                });

                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       setEdit(holder);

                        holder.editText.setText(holder.task.getText());

                        holder.backEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int id = entries.get(position).getId();
                                data.editTask(id,holder.editText.getText().toString());
                                notifyDataSetChanged();
                            }
                        });

                    }
                });

                holder.setDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = getContext();
                        openDatePickerDialog(context,data,entries.get(position));
                     }
                });

                holder.recurrence.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setRecurrence(holder);

                        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(context,R.array.time_interval, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
                        holder.spinnerRecurrence.setAdapter(adapterSpinner);

                        holder.backRecurrence.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //TODO: implement data routine
                                notifyDataSetChanged();
                            }
                        });
                    }
                });

                holder.setList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setList(holder);
                        notifyDataSetChanged();
                        //TODO: implement data routine
                    }
                });
                holder.back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setOriginal(holder);
                    }
                });
            }
        });

        return convertView;
    }

    //returns the entry at specified position
    public Entry getEntry(int position){
        return entries.get(position);
    }

    public void openDatePickerDialog(Context context,Data data,Entry entry) {

        int entryDate = entry.getDue();
        int entryDay;
        int entryMonth;
        int entryYear;

        if(entryDate>0) {
            entryDay = entryDate%100;
            entryMonth = ((entryDate%10000)-entryDay)/100;
            entryYear = (entryDate-entryMonth*100-entryDay)/10000;
        }
        else {
            Calendar c = Calendar.getInstance();
            entryYear = c.get(Calendar.YEAR);
            entryMonth = c.get(Calendar.MONTH);
            entryDay = c.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog;
        datePickerDialog= new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {
                int date = year*10000+month*100+day;
                data.editDate(entry.getId(), date);
                notifyDataSetChanged();
            }
        }, entryYear, entryMonth, entryDay);

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){

                data.editDate(entry.getId(),0);
                notifyDataSetChanged();
            }

        });

        datePickerDialog.show();
    }
    public void setOriginal(ViewHolder holder){

        holder.linearLayout.setAlpha(1);
        enable(holder.linearLayout);
        holder.linearLayout.bringToFront();

        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);

        holder.linearLayoutEdit.setAlpha(0);
        disable(holder.linearLayoutEdit);

        holder.linearLayoutRecurrence.setAlpha(0);
        disable(holder.linearLayoutRecurrence);
    }

    public void setAlt(ViewHolder holder){
        holder.linearLayoutAlt.bringToFront();
        holder.linearLayoutAlt.setAlpha(1);
        enable(holder.linearLayoutAlt);

        holder.linearLayout.setAlpha(0);
        disable(holder.linearLayout);

    }

    public void setEdit(ViewHolder holder){
        holder.linearLayoutEdit.bringToFront();
        holder.linearLayoutEdit.setAlpha(1);
        enable(holder.linearLayoutEdit);

        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);

    }


    public void setRecurrence(ViewHolder holder){
        holder.linearLayoutRecurrence.bringToFront();
        holder.linearLayoutRecurrence.setAlpha(1);
        enable(holder.linearLayoutRecurrence);

        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);
    }

    public void setList(ViewHolder holder){


    }

    public void disable(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);

        }

    }
    public void enable(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }

    }


}