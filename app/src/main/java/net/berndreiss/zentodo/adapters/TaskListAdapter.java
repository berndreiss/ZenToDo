package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.berndreiss.zentodo.R;
import net.berndreiss.zentodo.adapters.listeners.BackEditListener;
import net.berndreiss.zentodo.adapters.listeners.BackListListener;
import net.berndreiss.zentodo.adapters.listeners.BackRecurrenceListener;
import net.berndreiss.zentodo.adapters.listeners.CheckBoxListener;
import net.berndreiss.zentodo.adapters.listeners.FocusListener;
import net.berndreiss.zentodo.adapters.listeners.RecurrenceListener;
import net.berndreiss.zentodo.adapters.listeners.SetDateListener;
import net.berndreiss.zentodo.adapters.recyclerViewHelper.ItemTouchHelperAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Entry;
import net.berndreiss.zentodo.data.SQLiteHelper;

import java.util.List;

/**
 *   A custom ArrayAdapter<Entry> that creates rows with checkboxes that
 *   when checked remove the associated task.
 * <p>
 *   The row can have different views. It initially starts with a default view, that shows the task and the checkbox to remove it.
 *   There is also a menu button that opens an alternative row view. In this view different Buttons are shown through which certain
 *   information of the task can be changed (for data fields associated with a task see Entry.java).
 * <p>
 *   original view --(menu Button)--> alternative view --(focus Button)--> default view
 *                                                     --(edit Button)--> edit task view --(back Button)--> original view
 *                                                     --(set date Button)--> datePickerDialog --(cancel/ok Button)-->original view
 *                                                     --(recurrence Button)--> recurrence view --(back Button)--> original view
 *                                                     --(set list Button)--> set list view --(back Button)-->  original view
 *                                                     --(back Button)--> original view
 *
 */

public abstract class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    /**
     * TODO DESCRIBE
     */
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

        /**
         * TODO DECRIBE
         * @param view
         */
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

    /** TODO COMMENT */
    public List<Entry> entries;//ArrayList that holds task shown in RecyclerView

    /** TODO COMMENT */
    public Context context;

    /** TODO COMMENT */
    public TaskListAdapter(Context context, List<Entry> entries){
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
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {

        //set TextView to task
        holder.task.setText(entries.get(position).getTask());

        //set up checkbox of the task
        holder.checkBox.setChecked(false);
        holder.checkBox.setTag(position);

        //establish routine to remove task when checkbox is clicked
        holder.checkBox.setOnClickListener(new CheckBoxListener(this,holder, position));

        //Creating adapter for spinner with entries days/weeks/months/years
        /* Adapter has to be declared here so that the dropdown element of the spinner is not shown in the background */
        /* I do not understand why, but this fixed it */
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(context,R.array.time_interval, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        holder.spinnerRecurrence.setAdapter(adapterSpinner);

        //setting default row layout visible and active and all others to invisible and invalid
        setOriginal(holder);


        //sets buttons that have edited data that has been set to a different color
        markSet(holder,this.entries.get(position));

        //set Listener for showing menu of task
        holder.menu.setOnClickListener(view -> {
            //setting alternative row layout visible and active, everything else disabled
            setAlt(holder);
        });

        //setting Entry.focus to true/false, which means the task is/is not listed in focus mode
        holder.focus.setOnClickListener(new FocusListener(this,holder,position));

        //onClickListener for Button to change the task name
        holder.edit.setOnClickListener(v -> {

            //setting edit row visible and active, everything else disabled
            setEdit(holder);

            //setting editText to task
            holder.editText.setText(holder.task.getText());

        });

        //Listener to write back changes
        holder.backEdit.setOnClickListener(new BackEditListener(this, holder, position));

        //Listener to add date when task is due
        holder.setDate.setOnClickListener(new SetDateListener(this,holder,position));

        //Listener to edit how often task repeats
        holder.recurrence.setOnClickListener(new RecurrenceListener(this,holder,position));

        //reset all fields in recurrence row view to default
        holder.clearRecurrence.setOnClickListener(view -> {

            //reset spinner to days
            holder.spinnerRecurrence.setSelection(0);

            //reset edit Text to empty
            holder.editTextRecurrence.setText("");

        });


        //set Listener for returning from Recurrence Editing layout and writing back changes
        holder.backRecurrence.setOnClickListener(new BackRecurrenceListener(this, holder, position));

        //Listener for Button to change list task is assigned to
        holder.setList.setOnClickListener(view16 -> {
            //set list row view
            setList(holder);

            SQLiteHelper db = new SQLiteHelper(context);
            //array of names of all lists in task (as singletons)
            List<String> array = db.getLists();
            db.close();

            //initialize adapter with all existing list options
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, array);

            //edit Text that autocompletes with already existing lists
            holder.autoCompleteList.setAdapter(adapter);

            //set edit Text to current list
            holder.autoCompleteList.setText(entries.get(position).getList());

        });

        //clear edit Text when editing list
        holder.clearList.setOnClickListener(view -> {
            //set edit Text to empty
            holder.autoCompleteList.setText("");
        });

        //set Listener for returning from editing list and write back data
        holder.backList.setOnClickListener(new BackListListener(this,holder,position));

        //Listener to return to default layout
        holder.back.setOnClickListener(v -> {
            //setting original row visible and active, everything else disabled
            setOriginal(holder);

        });



    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return entries.size();
    }


    /**
     * TODO DESCRIBE
     * @param holder
     */
    //Setting original row view
    public void setOriginal(TaskListAdapter.ViewHolder holder){
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

    /**
     * TODO DESCRIBE
     * @param holder
     */
    //Setting alternative row view coming from original
    public void setAlt(TaskListAdapter.ViewHolder holder){
        //Set alternative row view to visible and active
        holder.linearLayoutAlt.bringToFront();
        holder.linearLayoutAlt.setAlpha(1);
        enable(holder.linearLayoutAlt);

        //Set original row view to invisible and inactive
        holder.linearLayout.setAlpha(0);
        disable(holder.linearLayout);

    }

    /**
     * TODO DESCRIBE
     * @param holder
     */
    //Setting edit row view coming from alternative view
    public void setEdit(TaskListAdapter.ViewHolder holder){

        //Set edit row view to visible and active
        holder.linearLayoutEdit.bringToFront();
        holder.linearLayoutEdit.setAlpha(1);
        enable(holder.linearLayoutEdit);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);

    }

    /**
     * TODO DESCRIBE
     * @param holder
     */
    //Setting recurrence row view coming from alternative view
    public void setRecurrence(TaskListAdapter.ViewHolder holder){
        //Set recurrence row view to visible and active
        holder.linearLayoutRecurrence.bringToFront();
        holder.linearLayoutRecurrence.setAlpha(1);
        enable(holder.linearLayoutRecurrence);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);
    }

    /**
     * TODO DESCRIBE
     * @param holder
     */
    //Setting list row view coming from alternative view
    public void setList(TaskListAdapter.ViewHolder holder){
        //Set list row view to visible and active
        holder.linearLayoutList.bringToFront();
        holder.linearLayoutList.setAlpha(1);
        enable(holder.linearLayoutList);

        //Set alternative row view to invisible and inactive
        holder.linearLayoutAlt.setAlpha(0);
        disable(holder.linearLayoutAlt);

    }

    /**
     * TODO DESCRIBE
     * @param fromPosition
     * @param toPosition
     */
    //write item movement to data and notify adapter: tasks can be dragged and dropped.
    // Upon drop notifyDataSetChanged is invoked -> see recyclerViewHelper.CustomItemTouchHelperCallback
    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        //swap entries in data distinguishing between item being moved up or down
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                DataManager.swap(context, entries, entries.get(i),entries.get(i+1));
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                DataManager.swap(context, entries, entries.get(i),entries.get(i-1));
            }
        }

        //notify the adapter
        notifyItemMoved(fromPosition,toPosition);

    }

    /**
     * TODO DESCRIBE
     * @param layout
     */
    //Disables view and first generation children
    public void disable(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);

        }

    }

    /**
     * TODO DESCRIBE
     * @param layout
     */
    //Enables view and first generation children
    public void enable(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }

    }

    /**
     * TODO DESCRIBE
     * @param holder
     * @param entry
     */
    //marks Buttons that have been set in a different color
    @SuppressLint("UseCompatLoadingForDrawables")
    public void markSet(TaskListAdapter.ViewHolder holder, Entry entry){

        //Set date button to alternative color if !=0, original color otherwise
        if (entry.getReminderDate()!=null){

            holder.setDate.setBackground(ContextCompat.getDrawable(context, R.drawable.button_alt_edited));

        }else{
            holder.setDate.setBackground(ContextCompat.getDrawable(context, R.drawable.button_alt));
        }

        //Set recurrence button to alternative color if !isEmpty(), original color otherwise
        if (entry.getRecurrence()!=null){
            holder.recurrence.setBackground(ContextCompat.getDrawable(context, R.drawable.button_alt_edited));
        }else{
            holder.recurrence.setBackground(ContextCompat.getDrawable(context, R.drawable.button_alt));
        }

        //Set list button to alternative color if !isEmpty(), original color otherwise
        if (entry.getList()!=null){
            holder.setList.setBackground(ContextCompat.getDrawable(context, R.drawable.button_alt_edited));
        }else{
            holder.setList.setBackground(ContextCompat.getDrawable(context, R.drawable.button_alt));
        }

        if (entry.getFocus()){
            holder.focus.setBackground(ContextCompat.getDrawable(context, R.drawable.button_alt_edited));


        } else{
            holder.focus.setBackground(ContextCompat.getDrawable(context, R.drawable.button_alt));
        }

    }

    /**
     * TODO DESCRIBE
     */
    public abstract void reset();


}