package com.bdreiss.zentodo;

/*
*   A simple To-Do-List.
*
*   The idea is, that you drop tasks you need to do, so you don't have to think about them anymore.
*   When you want to make your list for the day, you pick out of all the task that have been
*   dropped. In this step you decide, which tasks you want to focus on. For all the tasks you don't
*   want to get done right away, you can set a reminder date or add them to a list.
*
*   Basically the app has the following structure implemented in different layouts:
*   Drop -> Pick -> Focus
*
*   Lists: shows all the lists and upon choosing one shows all tasks assigned to the list
*
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import com.bdreiss.zentodo.adapters.DropTaskListAdapter;
import com.bdreiss.zentodo.adapters.recyclerViewHelper.CustomItemTouchHelperCallback;
import com.bdreiss.zentodo.adapters.FocusTaskListAdapter;
import com.bdreiss.zentodo.adapters.ListsListAdapter;
import com.bdreiss.zentodo.adapters.PickTaskListAdapter;
import com.bdreiss.zentodo.dataManipulation.Data;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.databinding.ActivityMainBinding;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Layouts for different modes
    private LinearLayout pick;//Layout to pick tasks that have been dropped and show them in focus
    private LinearLayout drop;//Layout to drop new tasks
    private LinearLayout focus;//Layout to show tasks to do today
    private LinearLayout lists;//Layout for all lists with tasks in it
    //private LinearLayout settings;

    //the following Buttons are components of the toolbar to switch between the different layouts
    private Button toolbarPick;
    private Button toolbarDrop;
    private Button toolbarFocus;
    private Button toolbarLists;
    //private Button toolbarSettings;

    //Data-object that stores all tasks, reminders, lists etc. (See Data.java and Entry.java)
    private Data data;

    //The adapters fill the RecyclerViews of layouts above and are all derived from TaskListAdapter
    //for more information on the implementation see the according java files
    PickTaskListAdapter pickAdapter;
    DropTaskListAdapter dropAdapter;
    FocusTaskListAdapter focusAdapter;
    ListsListAdapter listsListAdapter;

    //ArrayLists for adapters above
    ArrayList<Entry> tasksToPick;
    ArrayList<Entry> droppedTasks;
    ArrayList<Entry> focusTasks;
    ArrayList<String> listNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.bdreiss.zentodo.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Initialize Data-object and load data
        data = new Data(this);

        //Assign views to variables
        drop = findViewById(R.id.layout_drop);
        pick = findViewById(R.id.layout_pick);
        focus = findViewById(R.id.layout_focus);
        lists = findViewById(R.id.layout_lists);
        //settings = findViewById(R.id.layout_settings);

        toolbarDrop = findViewById(R.id.toolbar_drop);
        toolbarPick = findViewById(R.id.toolbar_pick);
        toolbarFocus = findViewById(R.id.toolbar_focus);
        toolbarLists = findViewById(R.id.toolbar_lists);
        //toolbarSettings = findViewById(R.id.toolbar_settings);

        //the following functions set OnClickListeners for buttons in toolbar which show according layout onClick
        toolbarListenerDrop();
        toolbarListenerPick();
        toolbarListenerFocus();
        toolbarListenerLists();
        //toolbarListenerSettings();

        //layouts are initialized
        initializeDrop();
        initializePick();
        initializeFocus();
        initializeLists();


        //Drop is shown when app starts
        showDrop();

    }

    //set listener for Pick button in toolbar
    public void toolbarListenerPick(){
        toolbarPick.setOnClickListener(view -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showPick();//show Pick layout
            toolbarListenerPick();//reset listener
        });

    }

    //set listener for Drop button in toolbar
    public void toolbarListenerDrop(){
        toolbarDrop.setOnClickListener(view -> {
           showDrop();//show Drop layout
           toolbarListenerDrop();//reset listener
        });

    }

    //set listener for Focus button in toolbar
    public void toolbarListenerFocus(){
        toolbarFocus.setOnClickListener(view -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showFocus();//show Focus layout
            toolbarListenerFocus();//reset listener
        });

    }

    //set listener for Lists button in toolbar
    public void toolbarListenerLists(){
        toolbarLists.setOnClickListener(view -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showLists();//show Lists layout
            toolbarListenerLists();//reset listener
        });

    }

    /*public void toolbarListenerSettings() {
        toolbarSettings.setOnClickListener(view -> {
            showSettings();
            toolbarListenerSettings();
        });

    }*/

    //initialize Pick layout
    public void initializePick() {

        //get tasks to pick from data
        tasksToPick = data.getTasksToPick();

        //initialize adapter for RecyclerView with all tasks that have been dropped, have been in Focus or are due today
        pickAdapter = new PickTaskListAdapter(this, data, tasksToPick);

        //assign RecyclerView
        RecyclerView listView = findViewById(R.id.list_view_pick);

        //set adapter
        listView.setAdapter(pickAdapter);

        //set layoutManager
        listView.setLayoutManager(new LinearLayoutManager(this));

        //allows items to be moved and reordered in RecyclerView
        ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(pickAdapter);

        //create ItemTouchHelper and assign to RecyclerView
        ItemTouchHelper iTouchHelper = new ItemTouchHelper(callback);
        iTouchHelper.attachToRecyclerView(listView);


        //button to pick tasks that have been checked
        Button pick = findViewById(R.id.button_pick);

        //if pressed remove tasks from Drop and add to Focus
        pick.setOnClickListener(view -> {

            //list of items that have been checked in RecyclerView (see TaskListAdapter.java)
            ArrayList<Integer> checked = pickAdapter.getIdsChecked();

            //if checked reset dropped and focus attributes of all task in list above
            for (int id : checked) {
                data.setFocus(id, true);
                data.setDropped(id, false);
            }

            //get all tasks that have not been checked
            ArrayList<Integer> notChecked = pickAdapter.getIdsNotChecked();

            //set focused tasks to false so they are not shown in Focus (in case they have been there before)
            for (int id : notChecked) {
                if (data.getEntries().get(data.getPosition(id)).getFocus())
                    data.setFocus(id, false);
            }

            //re-initialize listener for Pick button in toolbar
            toolbarListenerPick();

            //show Focus layout
            showFocus();

        });

    }

    //show Pick layout
    @SuppressLint("NotifyDataSetChanged")
    public void showPick(){

        //clear ArrayList for Pick, add current tasks from data and notify adapter (in case they have been altered in another layout)
        tasksToPick.clear();
        pickAdapter.clearIdsChecked();
        tasksToPick.addAll(data.getTasksToPick());
        pickAdapter.notifyDataSetChanged();

        //enable components of Pick layout (setVisibility = VISIBLE)
        enableLayout(pick);

        //disable components of all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(focus);
        disableLayout(lists);
        //disableLayout(settings);

        //set color of Pick-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        //toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
    }

    //initialize Drop layout
    public void initializeDrop(){

        //get dropped tasks from data
        droppedTasks = data.getDropped();

        //initialize adapter for ListView that shows tasks dropped
        dropAdapter = new DropTaskListAdapter(this, data, droppedTasks);

        //assign ListView
        RecyclerView listView = findViewById(R.id.list_view_drop);

        //set adapter
        listView.setAdapter(dropAdapter);

        //set layoutManager
        listView.setLayoutManager(new LinearLayoutManager(this));

        //allows items to be moved and reordered in RecyclerView
        ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(dropAdapter);

        //create ItemTouchHelper and assign to RecyclerView
        ItemTouchHelper iTouchHelper = new ItemTouchHelper(callback);
        iTouchHelper.attachToRecyclerView(listView);

        //assign EditText to add new tasks
        final EditText editText = findViewById(R.id.edit_text_drop);

        //button to drop task in EditText
        Button buttonDrop = findViewById(R.id.button_drop);

        //set Button BackgroundColor to toolbar default color
        buttonDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        //set listener for drop button
        dropListener(editText, buttonDrop);
    }

    //initialize drop button that takes text from editText and passes it to the adapter
    public void dropListener(EditText editText, Button buttonDrop){

        //OnClickListener that adds task in EditText to Data
        buttonDrop.setOnClickListener(view -> {
            //get task from EditText
            String task = editText.getText().toString();
            //reset EditText
            editText.setText("");

            //add task to Data if it is not empty
            if (!task.trim().isEmpty()) {
                dropAdapter.add(task);
                dropListener(editText, buttonDrop);
            }
        });

    }

    //show Drop layout
    @SuppressLint("NotifyDataSetChanged")
    public void showDrop(){

        //clear ArrayList for Drop, add current tasks from data and notify adapter (in case they have been altered in another layout)
        droppedTasks.clear();
        droppedTasks.addAll(data.getDropped());
        dropAdapter.notifyDataSetChanged();

        //enable all components of Drop layout (setVisibility = VISIBLE)
        enableLayout(drop);

        //disable components of all other layouts (setVisibility = GONE)
        disableLayout(pick);
        disableLayout(focus);
        disableLayout(lists);
        //disableLayout(settings);

        //set background color of Drop to chosen, all others to toolbar default
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        //toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
    }

    //initialize Focus layout
    public void initializeFocus(){

        //get tasks for focus from data
        focusTasks = data.getFocus();

        //initialize the adapter for the ListView to show the tasks to focus on
        focusAdapter = new FocusTaskListAdapter(this,data,focusTasks);

        //assign RecyclerView
        RecyclerView listView = findViewById(R.id.list_view_focus);

        //set adapter for Recyclerview
        listView.setAdapter(focusAdapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        //allows items to be moved and reordered in RecyclerView
        ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(focusAdapter);

        //create ItemTouchHelper and assign to RecyclerView
        ItemTouchHelper iTouchHelper = new ItemTouchHelper(callback);
        iTouchHelper.attachToRecyclerView(listView);

    }

    //show Focus layout
    @SuppressLint("NotifyDataSetChanged")
    public void showFocus(){

        //clear ArrayList for Focus, add current tasks from data and notify adapter (in case they have been altered in another layout)
        focusTasks.clear();
        focusTasks.addAll(data.getFocus());
        focusAdapter.notifyDataSetChanged();

        //enable all components in the Focus layout (setVisibility = VISIBLE)
        enableLayout(focus);

        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(lists);
        disableLayout(pick);
        //disableLayout(settings);

        //set background color of Focus to chosen, all others to toolbar default
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        //toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

    }

    //initialize Lists layout
    public void initializeLists() {

        //initialize ListView to show lists available
        ListView listView = findViewById(R.id.list_view_lists);
        RecyclerView recyclerView = findViewById(R.id.recycle_view_lists);

        //TextView that shows the list your currently in as a header
        TextView header = findViewById(R.id.text_view_lists_header);

        //initialize adapter: each item represents a button that when pressed initializes a TaskListAdapter
        //with all the tasks of the list (see ListsListAdapter.java)
        listNames = data.getLists();
        listsListAdapter = new ListsListAdapter(this, listView, recyclerView, header, data, listNames);

        //set adapter
        listView.setAdapter(listsListAdapter);

    }

    //show Lists layout
    public void showLists(){

        //clear ArrayList for Lists, add current tasks from data and notify adapter (in case they have been altered in another layout)
        listNames.clear();
        listNames.addAll(data.getLists());
        listsListAdapter.notifyDataSetChanged();

        //enable all components in the Lists layout (setVisibility = VISIBLE)
        enableLayout(lists);

        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(pick);
        disableLayout(focus);
        //disableLayout(settings);

        //set color of Lists-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        //toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        //header for showing name of chosen list
        TextView header = findViewById(R.id.text_view_lists_header);

        //is only visible after list has been chosen in adapter TODO move to adapter
        header.setVisibility(View.GONE);

        //recyclerView for showing list items
        RecyclerView recyclerView = findViewById(R.id.recycle_view_lists);

        //is only visible after list has been chosen in adapter TODO move to adapter
        recyclerView.setVisibility(View.GONE);
    }

    /*
    public void showSettings(){
        //enable all components in the Lists layout (setVisibility = VISIBLE)
        enableLayout(settings);
        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(pick);
        disableLayout(focus);
        disableLayout(lists);

        //set color of Lists-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));

    }*/

    //set Visibility of all first generation children of layout to VISIBLE and bring layout to front
    public void enableLayout(LinearLayout layout){

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setVisibility(View.VISIBLE);
        }
        layout.bringToFront();

    }

    //set Visibility of all first generation children of layout to GONE
    public void disableLayout(LinearLayout layout){

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setVisibility(View.GONE);
        }
    }

    //closes the keyboard, code from: https://www.geeksforgeeks.org/how-to-programmatically-hide-android-soft-keyboard/
    private void closeKeyboard()
    {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {

            // now assign the system
            // service to InputMethodManager
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }

}