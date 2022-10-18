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

import android.os.Bundle;

import com.bdreiss.zentodo.dataManipulation.Data;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.bdreiss.zentodo.databinding.ActivityMainBinding;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LinearLayout drop;//Layout to drop new tasks
    private LinearLayout pick;//Layout to pick tasks that have been dropped and show them in focus
    private LinearLayout focus;//Layout to show tasks to do today
    private LinearLayout lists;//Layout for all lists with tasks in it

    //the following Buttons are components of the toolbar to switch between the different layouts
    private Button toolbarDrop;
    private Button toolbarPick;
    private Button toolbarFocus;
    private Button toolbarLists;

    //Data-object that stores all tasks, reminders, lists etc. (See Data.java and Entry.java)
    private Data data;

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

        toolbarDrop = findViewById(R.id.toolbar_drop);
        toolbarPick = findViewById(R.id.toolbar_pick);
        toolbarFocus = findViewById(R.id.toolbar_focus);
        toolbarLists = findViewById(R.id.toolbar_lists);

        //Initializes the toolbar and sets an OnClickListener for every Button
        initialize();

        //App starts with the Drop layout, which is being initialized
        initializeDrop();

    }

    //Initializes the toolbar and sets an OnClickListener for every Button
    public void initialize(){
        toolbarDrop.setOnClickListener(view -> {
            initialize();
            //Initializes the Drop layout
            initializeDrop();
        });

        toolbarPick.setOnClickListener(view -> {
            initialize();
            //Initializes the Pick layout
            initializePick();
        });

        toolbarFocus.setOnClickListener(view -> {
            initialize();
            //Initializes the Focus layout
            initializeFocus();
        });

        toolbarLists.setOnClickListener(view -> {
            initialize();
            //Initializes the Lists layout
            initializeLists();
        });



    }

    //Initialize Drop layout
    public void initializeDrop(){

        //enable all components of Drop layout (setVisibility = VISIBLE)
        enableLayout(drop);
        //disable components of all other layouts (setVisibility = GONE)
        disableLayout(pick);
        disableLayout(focus);
        disableLayout(lists);

        //set background color of Drop to chosen, all others to toolbar default
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        //initialize adapter for ListView that shows tasks dropped
        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getDropped(), "dropped");
        //assign ListView
        ListView listView = findViewById(R.id.list_view_thoughts);
        //set adapter
        listView.setAdapter(adapter);

        //assign EditText to add new tasks
        final EditText editText = findViewById(R.id.edit_text_drop);
        //button to drop task in EditText
        Button buttonDrop = findViewById(R.id.button_drop);
        //set Button BackgroundColor to toolbar default color
        buttonDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        //OnClickListener that adds task in EditText to Data
        buttonDrop.setOnClickListener(view -> {
            //get task from EditText
            String task = editText.getText().toString();
            //reset EditText
            editText.setText("");

            //add task to Data if it is not empty
            if (!task.equals("")) {
                data.add(task);
                //re-initialize Drop layout
                initializeDrop();
            }
        });
    }

    //initializes Pick layout
    public void initializePick(){
        //enable components of Pick layout (setVisibility = VISIBLE)
        enableLayout(pick);
        //disable components of all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(focus);
        disableLayout(lists);

        //set color of Pick-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        //initialize adapter for ListView with all tasks that have been dropped or are due today
        TaskListAdapter adapter = new TaskListAdapter(this, data, data.getTasksToPick(), "pick");
        //assign ListView
        ListView listView = findViewById(R.id.list_view_pick);
        //set adapter
        listView.setAdapter(adapter);

        //button to pick tasks that have been checked
        Button pick = findViewById(R.id.button_pick);

        //if pressed remove tasks from Drop and add to Focus
        pick.setOnClickListener(view -> {
            //list of items that have been checked (see TaskListAdapter.java)
            ArrayList<Integer> checked= adapter.getIdsChecked();

            //if checked reset dropped and focus attributes of all task in list above
            for (int id : checked){
                data.setFocus(id, true);
                data.setDropped(id, false);
            }

            //get all tasks that have not been checked
            ArrayList<Integer> notChecked = adapter.getIdsNotChecked();

            //set those tasks focus attribute to false so they are not shown in Focus
            for (int id: notChecked){
                data.setFocus(id, false);
            }

            //re-initialize toolbar
            initialize();
            //initialize Focus layout
            initializeFocus();

        });

    }

    //initializes the Focus layout
    public void initializeFocus(){
        //enable all components in the Focus layout (setVisibility = VISIBLE)
        enableLayout(focus);
        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(lists);
        disableLayout(pick);

        //set background color of Focus to chosen, all others to toolbar default
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        //initialize the adapter for the ListView to show the tasks to focus on
        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getFocus(), "focus");
        //assign View
        ListView listView = findViewById(R.id.list_view_focus);
        //set adapter
        listView.setAdapter(adapter);


    }

    //initialize Lists layout
    public void initializeLists(){
        //enable all components in the Lists layout (setVisibility = VISIBLE)
        enableLayout(lists);
        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(pick);
        disableLayout(focus);

        //set color of Lists-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        //initialize ListView to show lists available
        ListView listView = findViewById(R.id.list_view_lists);
        //TextView that shows the list your currently in as a header
        TextView header = findViewById(R.id.text_view_lists_header);
        //initialize adapter: each item represents a button that when pressed initializes a TaskListAdapter
        //with all the tasks of the list (see ListsListAdapter.java)
        ListsListAdapter adapter = new ListsListAdapter(this,listView,header,data,data.getLists());

        //set adapter
        listView.setAdapter(adapter);

    }

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

}