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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.bdreiss.zentodo.adapters.DropTaskListAdapter;
import com.bdreiss.zentodo.adapters.TaskListAdapter;
import com.bdreiss.zentodo.adapters.recyclerViewHelper.CustomItemTouchHelperCallback;
import com.bdreiss.zentodo.adapters.FocusTaskListAdapter;
import com.bdreiss.zentodo.adapters.ListsListAdapter;
import com.bdreiss.zentodo.adapters.PickTaskListAdapter;
import com.bdreiss.zentodo.dataManipulation.Data;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MotionEvent;
import android.view.View;

import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

//    private final static String TEXT_COLOR = "#90000000";



    //Layouts for different modes
    private LinearLayout pick;//Layout to pick tasks that have been dropped and show them in focus
    private LinearLayout drop;//Layout to drop new tasks
    private LinearLayout focus;//Layout to show tasks to do today
    private LinearLayout lists;//Layout for all lists with tasks in it
    //private LinearLayout settings;1

    //the following Buttons are components of the toolbar to switch between the different layouts
    private Button toolbarPick;
    private Button toolbarDrop;
    private Button toolbarFocus;
    private Button toolbarLists;
    //private Button toolbarSettings;

    private FloatingActionButton fab;

    //Data-object that stores all tasks, reminders, lists etc. (See Data.java and Entry.java)
    private Data data;

    //The adapters fill the RecyclerViews of layouts above and are all derived from TaskListAdapter
    //for more information on the implementation see the according java files
    PickTaskListAdapter pickAdapter;
    PickTaskListAdapter doNowAdapter;
    PickTaskListAdapter doLaterAdapter;
    PickTaskListAdapter moveToListAdapter;
    DropTaskListAdapter dropAdapter;
    FocusTaskListAdapter focusAdapter;
    ListsListAdapter listsListAdapter;

    //ArrayLists for adapters above
    ArrayList<Entry> tasksToPick;
    ArrayList<Entry> tasksToDoNow;
    ArrayList<Entry> tasksToDoLater;
    ArrayList<Entry> tasksToMoveToList;
    ArrayList<Entry> droppedTasks;
    ArrayList<Entry> focusTasks;
    ArrayList<String> listNames;
    ArrayList<Integer> recurringButRemovedFromFocus;

    @RequiresApi(api = Build.VERSION_CODES.M)
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

        fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        //OnClickListeners for buttons in toolbar which show according layout onClick
        toolbarPick.setOnClickListener(view -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showPick();//show Pick layout
        });

        toolbarDrop.setOnClickListener(view -> {
            showDrop();//show Drop layout
        });

        toolbarFocus.setOnClickListener(view -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showFocus();//show Focus layout
        });

        toolbarLists.setOnClickListener(view -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showLists();//show Lists layout
        });

        /*
        toolbarSettings.setOnClickListener(view -> {
            showSettings();
            toolbarListenerSettings();
        });
        */

        //layouts are initialized
        initializeDrop();
        initializePick();
        initializeFocus();
        initializeLists();


        //Drop is shown when app starts
        showDrop();


    }

    //initialize Pick layout
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initializePick() {

        //get tasks to pick from data
        tasksToPick = new ArrayList<>();

        tasksToDoNow = new ArrayList<>();
        tasksToDoLater = new ArrayList<>();
        tasksToMoveToList = new ArrayList<>();

        recurringButRemovedFromFocus = new ArrayList<>();

        //initialize adapter for RecyclerView with all tasks that have been dropped, have been in Focus or are due today
        pickAdapter = new PickTaskListAdapter(this, data, tasksToPick, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * pickAdapter.entries.size());
                findViewById(R.id.list_view_pick).setLayoutParams(params);
            }
        };

        doNowAdapter = new PickTaskListAdapter(this, data, tasksToDoNow, true){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_doNow).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * doNowAdapter.entries.size());
                findViewById(R.id.list_view_pick_doNow).setLayoutParams(params);
            }
        };
        doLaterAdapter = new PickTaskListAdapter(this, data, tasksToDoLater, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_doLater).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * doLaterAdapter.entries.size());
                findViewById(R.id.list_view_pick_doLater).setLayoutParams(params);
            }
        };
        moveToListAdapter = new PickTaskListAdapter(this, data,tasksToMoveToList, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_list).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * moveToListAdapter.entries.size());
                findViewById(R.id.list_view_pick_list).setLayoutParams(params);
            }
        };



        initializeRecyclerView(findViewById(R.id.list_view_pick), pickAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_doNow), doNowAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_doLater), doLaterAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_list), moveToListAdapter);

        //button to pick tasks that have been checked
        Button pickButton = findViewById(R.id.button_pick);

        pickButton.setTextColor(ContextCompat.getColor(this,R.color.color_primary));

        //if pressed remove tasks from Drop and add to Focus
        pickButton.setOnClickListener(view -> {

            if (tasksToPick.size() != 0){
                Helper.showPickHelper(this);
            }
            else {


                //if checked reset dropped and focus attributes of all task in list above
                for (Entry e : tasksToDoNow) {
                    data.setFocus(e.getId(), true);
                    data.setDropped(e.getId(), false);
                }

                for (Entry e : tasksToDoLater) {
                    if (data.getEntries().get(data.getPosition(e.getId())).getFocus())
                        data.setFocus(e.getId(), false);
                }

                for (Entry e : tasksToMoveToList) {
                    if (data.getEntries().get(data.getPosition(e.getId())).getFocus())
                        data.setFocus(e.getId(), false);
                }

                //show Focus layout
                showFocus();
            }
        });

    }

    //show Pick layout
    @SuppressLint("NotifyDataSetChanged")
    public void showPick(){

        //clear ArrayList for Pick, add current tasks from data and notify adapter (in case they have been altered in another layout)
        tasksToPick.clear();
        tasksToPick.addAll(data.getTasksToPick());

        pickAdapter.notifyDataSetChanged();
        pickAdapter.itemCountChanged();

        tasksToDoNow.clear();
        doNowAdapter.notifyDataSetChanged();
        doNowAdapter.itemCountChanged();

        tasksToDoLater.clear();
        doLaterAdapter.notifyDataSetChanged();
        doLaterAdapter.itemCountChanged();

        tasksToMoveToList.clear();
        moveToListAdapter.notifyDataSetChanged();
        moveToListAdapter.itemCountChanged();

        //enable components of Pick layout (setVisibility = VISIBLE)
        enableLayout(pick);

        fab.setOnClickListener(Helper.getPickListener(this));

        //disable components of all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(focus);
        disableLayout(lists);
        //disableLayout(settings);

        //set color of Pick-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.color_primary_accent));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.color_primary));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.color_primary));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.color_primary));
        //toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar));


        toolbarPick.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarPick.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));
        toolbarDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarDrop.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarFocus.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarFocus.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

    }

    //initialize Drop layout
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initializeDrop(){

        //get dropped tasks from data
        droppedTasks = data.getDropped();

        //initialize adapter for ListView that shows tasks dropped
        dropAdapter = new DropTaskListAdapter(this, data, droppedTasks);

        //assign ListView
        RecyclerView listView = findViewById(R.id.list_view_drop);


        listView.setOnTouchListener(new ShowHelp());

        drop.setOnTouchListener(new ShowHelp());

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

        buttonDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        //set Button BackgroundColor to toolbar default color

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

        fab.setOnClickListener(Helper.getDropListener(this));

        //disable components of all other layouts (setVisibility = GONE)
        disableLayout(pick);
        disableLayout(focus);
        disableLayout(lists);
        //disableLayout(settings);

        //set background color of Drop to chosen, all others to toolbar default
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.color_primary_accent));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.color_primary));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.color_primary));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.color_primary));
        //toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        toolbarDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarDrop.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));
        toolbarPick.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarPick.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarFocus.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarFocus.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
    }

    //initialize Focus layout
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initializeFocus(){

        //get tasks for focus from data
        focusTasks = data.getFocus(recurringButRemovedFromFocus);

        //initialize the adapter for the ListView to show the tasks to focus on
        focusAdapter = new FocusTaskListAdapter(this,data,focusTasks, recurringButRemovedFromFocus);

        //assign RecyclerView
        RecyclerView listView = findViewById(R.id.list_view_focus);

        listView.setOnTouchListener(new ShowHelp());

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
        focusTasks.addAll(data.getFocus(recurringButRemovedFromFocus));
        focusAdapter.notifyDataSetChanged();

        //enable all components in the Focus layout (setVisibility = VISIBLE)
        enableLayout(focus);

        fab.setOnClickListener(Helper.getFocusListener(this));

        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(lists);
        disableLayout(pick);
        //disableLayout(settings);

        //set background color of Focus to chosen, all others to toolbar default
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.color_primary_accent));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.color_primary));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.color_primary));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.color_primary));
        //toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        toolbarFocus.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarFocus.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));
        toolbarDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarDrop.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarPick.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarPick.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

    }

    //initialize Lists layout
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initializeLists() {

        //initialize ListView to show lists available
        ListView listView = findViewById(R.id.list_view_lists);
        RecyclerView recyclerView = findViewById(R.id.recycle_view_lists);


        listView.setOnTouchListener(new ShowHelp());
        recyclerView.setOnTouchListener(new ShowHelp());


        //Items that show the list your currently in as a header and the button for choosing a color
        LinearLayout headerLayout = findViewById(R.id.header);
        TextView headerTextView = findViewById(R.id.text_view_lists_header);
        Button headerButton = findViewById(R.id.button_header);

        //initialize adapter: each item represents a button that when pressed initializes a TaskListAdapter
        //with all the tasks of the list (see ListsListAdapter.java)
        listNames = data.getLists();
        listsListAdapter = new ListsListAdapter(this, listView, recyclerView, headerLayout, headerTextView, headerButton, data, listNames, recurringButRemovedFromFocus);

        //set adapter
        listView.setAdapter(listsListAdapter);

    }

    //show Lists layout
    public void showLists(){

        listsListAdapter.setHeaderGone();

        //clear ArrayList for Lists, add current tasks from data and notify adapter (in case they have been altered in another layout)
        listNames.clear();
        listNames.addAll(data.getLists());
        listsListAdapter.notifyDataSetChanged();

        //enable all components in the Lists layout (setVisibility = VISIBLE)
        enableLayout(lists);

        fab.setOnClickListener(Helper.getListListener(this));

        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(pick);
        disableLayout(focus);
        //disableLayout(settings);

        //set color of Lists-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.color_primary_accent));
        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.color_primary));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.color_primary));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.color_primary));
        //toolbarSettings.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        toolbarLists.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarLists.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));
        toolbarDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarDrop.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarPick.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarPick.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarFocus.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarFocus.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public class ShowHelp implements View.OnTouchListener {



        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            fab.setVisibility(View.VISIBLE);
            fab.postDelayed(() -> fab.setVisibility(View.GONE), 3000);
            return false;
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initializeRecyclerView(RecyclerView view, PickTaskListAdapter adapter){

        //shows Floating Action Button on
        view.setOnTouchListener(new ShowHelp());

        //set adapter
        view.setAdapter(adapter);

        //set layoutManager
        view.setLayoutManager(new LinearLayoutManager(this));

        view.setNestedScrollingEnabled(false);

        //allows items to be moved and reordered in RecyclerView
        ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(adapter);

        //create ItemTouchHelper and assign to RecyclerView
        ItemTouchHelper iTouchHelper = new ItemTouchHelper(callback);
        iTouchHelper.attachToRecyclerView(view);

        adapter.setPickAdapter(pickAdapter);
        adapter.setDoNowAdapter(doNowAdapter);
        adapter.setDoLaterAdapter(doLaterAdapter);
        adapter.setMoveToListAdapter(moveToListAdapter);


    }


}