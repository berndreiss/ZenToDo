package net.berndreiss.zentodo;

/*
*   A simple To-Do-List.
*
*   The idea is, that you drop tasks you need to do, so you don't have to think about them anymore.
*   When you want to make your to-do-list for the day, you pick out of all the task that have been
*   dropped. In this step you decide, which tasks you want to do now. For all the tasks you don't
*   want to get done right away, you can set a reminder date or add them to a list.
*
*   Basically the app has the following structure implemented in different layouts:
*
*   -DROP -> PICK -> FOCUS
*
*   -Lists: shows all the lists and containing tasks assigned to it.
*
*   These layouts consist of RecyclerViews. Each item in these RecyclerViews represents a task (dataManipulation.Entry).
*   It shows the description of the task, a checkbox and a menu button. The checkbox removes a task.
*
*   The menu consist of the following buttons:
*       moveToFocus -> adds task to RecyclerView in FOCUS
*       editTask -> option to edit description of the task
*       setReminderDate -> set a reminder date for the task
*       setRecurrence -> option to make task repeatable
*       editList -> assign task to a list
*
*   These are the basic functionalities for the buttons and the checkbox. They may vary from
*   layout to layout. A precise description of these functions can be found in adapters.
*
*   The adapters handle the functionality of the buttons. Each layouts RecyclerView has its own
*   adapter although all are derived from TaskListAdapter. I.e. the layout FOCUS is handled by
*   FocusTaskListAdapter.
*
*   If you got any questions/suggestions feel free to email me: bd_reiss@yahoo.de
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import net.berndreiss.zentodo.adapters.DropTaskListAdapter;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.adapters.recyclerViewHelper.CustomItemTouchHelperCallback;
import net.berndreiss.zentodo.adapters.FocusTaskListAdapter;
import net.berndreiss.zentodo.adapters.ListsListAdapter;
import net.berndreiss.zentodo.adapters.PickTaskListAdapter;
import net.berndreiss.zentodo.Data.DataManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;

import net.berndreiss.zentodo.api.Entry;
import net.berndreiss.zentodo.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final String DATABASE_NAME = "Data.db";
    public static final int DATABASE_VERSION = 1;

    private LinearLayout pick;//Layout to pick tasks that have been dropped and show them in focus
    private LinearLayout drop;//Layout to drop new tasks
    private LinearLayout focus;//Layout to show tasks to do today
    private LinearLayout lists;//Layout for all lists with tasks in it

    //the following Buttons are components of the toolbar to switch between the different layouts
    private Button toolbarPick;
    private Button toolbarDrop;
    private Button toolbarFocus;
    private Button toolbarLists;

    //floating action button on the bottom right corner
    private FloatingActionButton fab;

    //The adapters fill the RecyclerViews of layouts above and are all derived from TaskListAdapter
    //for more information on the implementation see the according java files in adapters
    PickTaskListAdapter pickAdapter;
    PickTaskListAdapter doNowAdapter;
    PickTaskListAdapter doLaterAdapter;
    PickTaskListAdapter moveToListAdapter;
    DropTaskListAdapter dropAdapter;
    TaskListAdapter focusAdapter;
    ListsListAdapter listsListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        net.berndreiss.zentodo.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Assign views to variables
        drop = findViewById(R.id.layout_drop);
        pick = findViewById(R.id.layout_pick);
        focus = findViewById(R.id.layout_focus);
        lists = findViewById(R.id.layout_lists);

        toolbarDrop = findViewById(R.id.toolbar_drop);
        toolbarPick = findViewById(R.id.toolbar_pick);
        toolbarFocus = findViewById(R.id.toolbar_focus);
        toolbarLists = findViewById(R.id.toolbar_lists);

        //Floating action button that shows help on press
        fab = findViewById(R.id.fab);
        //the fab will be gone by default and only be shown when user interacts with the app.
        //This is achieved via OnTouchListeners on the RecyclerViews (see class ShowFab in this file).
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

        //layouts are initialized
        initializeDrop();
        initializePick();
        initializeFocus();
        initializeLists();


        //Drop is shown when app starts
        showDrop();

    }

    /**
     * TODO DESCRIBE
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializePick() {

        //initialize adapter for RecyclerView with all tasks that have been dropped, have been in Focus or are due today
        pickAdapter = new PickTaskListAdapter(this, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * pickAdapter.entries.size());
                findViewById(R.id.list_view_pick).setLayoutParams(params);
            }
        };

        //initialize empty adapters for the other RecyclerViews
        doNowAdapter = new PickTaskListAdapter(this, true){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_doNow).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * doNowAdapter.entries.size());
                findViewById(R.id.list_view_pick_doNow).setLayoutParams(params);
            }
        };

        doLaterAdapter = new PickTaskListAdapter(this, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_doLater).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * doLaterAdapter.entries.size());
                findViewById(R.id.list_view_pick_doLater).setLayoutParams(params);
            }
        };

        moveToListAdapter = new PickTaskListAdapter(this, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_list).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * moveToListAdapter.entries.size());
                findViewById(R.id.list_view_pick_list).setLayoutParams(params);
            }
        };


        //Set up ItemTouchHelper to move tasks around and connect View to each other
        initializeRecyclerView(findViewById(R.id.list_view_pick), pickAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_doNow), doNowAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_doLater), doLaterAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_list), moveToListAdapter);

        //button to pick tasks that have been checked
        Button pickButton = findViewById(R.id.button_pick);

        pickButton.setTextColor(ContextCompat.getColor(this,R.color.color_primary));

        //if pressed remove tasks from Drop and add to Focus
        pickButton.setOnClickListener(view -> {

            //Continue if all tasks have been categorized, show hint otherwise
            if (!pickAdapter.entries.isEmpty()){
                Helper.showPickHelper(this);
            }
            else {

                //if checked reset dropped and focus attributes of all task in tasksToDoNow
                for (Entry e : doNowAdapter.entries) {
                    DataManager.setFocus(this, e, true);
                    DataManager.setDropped(this, e, false);
                }

                //set focus to false for all tasks in tasksToDoLater
                for (Entry e : doLaterAdapter.entries)
                        DataManager.setFocus(this, e, false);

                //set focus to false for all tasks in tasksToMoveToList
                for (Entry e : moveToListAdapter.entries)
                        DataManager.setFocus(this, e, false);

                //show Focus layout
                showFocus();
            }
        });

    }

    /**
     * TODO DESCRIBE
     */
    @SuppressLint("NotifyDataSetChanged")
    public void showPick(){

        //Reset and Update Adapters to reflect changes
        //Also update itemCountChanged, so that RecyclerViews get resized properly
        pickAdapter.reset();

        //enable components of Pick layout (setVisibility = VISIBLE)
        enableLayout(pick);

        //set fab to show help according to layout
        fab.setOnClickListener(Helper.getPickListener(this));

        //disable components of all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(focus);
        disableLayout(lists);

        //set color of Pick-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarPick.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary_accent));
        toolbarPick.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarPick.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));

        toolbarDrop.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarDrop.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        toolbarFocus.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarFocus.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarFocus.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        toolbarLists.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarLists.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

    }

    /**
     * TODO DESCRIBE
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializeDrop(){

        //initialize adapter for ListView that shows tasks dropped
        dropAdapter = new DropTaskListAdapter(this);

        //assign ListView
        RecyclerView listView = findViewById(R.id.list_view_drop);

        //show fab when anything is being touched in listViews
        listView.setOnTouchListener(new ShowFab());

        //show fab when anything is being touched in layout
        drop.setOnTouchListener(new ShowFab());

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

        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(getResources().getInteger(R.integer.task_max_char))});

        //button to drop task in EditText
        Button buttonDrop = findViewById(R.id.button_drop);

        //set Button BackgroundColor to toolbar default color
        buttonDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary));

        //set listener for drop button
        dropListener(editText, buttonDrop);
    }

    /**
     * TODO DESCRIBE
     * @param editText
     * @param buttonDrop
     */
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

    /**
     * TODO DESCRIBE
     */
    @SuppressLint("NotifyDataSetChanged")
    public void showDrop(){

        //clear ArrayList for Drop, add current tasks from data and notify adapter (in case they have been altered in another layout)
        dropAdapter.reset();

        //enable all components of Drop layout (setVisibility = VISIBLE)
        enableLayout(drop);

        //set fab to show help according to layout
        fab.setOnClickListener(Helper.getDropListener(this));

        //disable components of all other layouts (setVisibility = GONE)
        disableLayout(pick);
        disableLayout(focus);
        disableLayout(lists);

        //set background color of Drop to chosen, all others to toolbar default
        toolbarDrop.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary_accent));
        toolbarDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarDrop.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));

        toolbarPick.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarPick.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarPick.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        toolbarFocus.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarFocus.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarFocus.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        toolbarLists.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarLists.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
    }

    /**
     * TODO DESCRIBE
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializeFocus(){

        //initialize the adapter for the ListView to show the tasks to focus on
        focusAdapter = new FocusTaskListAdapter(this);

        //assign RecyclerView
        RecyclerView listView = findViewById(R.id.list_view_focus);

        //show fab when anything is being touched in listView
        listView.setOnTouchListener(new ShowFab());

        //show fab when anything is being touched in layout
        focus.setOnTouchListener(new ShowFab());

        //set adapter for Recyclerview
        listView.setAdapter(focusAdapter);
        listView.setLayoutManager(new LinearLayoutManager(this));

        //allows items to be moved and reordered in RecyclerView
        ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(focusAdapter);

        //create ItemTouchHelper and assign to RecyclerView
        ItemTouchHelper iTouchHelper = new ItemTouchHelper(callback);
        iTouchHelper.attachToRecyclerView(listView);

    }

    /**
     * TODO DESCRIBE
     */
    @SuppressLint("NotifyDataSetChanged")
    public void showFocus(){

        //clear ArrayList for Focus, add current tasks from data and notify adapter (in case they have been altered in another layout)
        focusAdapter.reset();

        //enable all components in the Focus layout (setVisibility = VISIBLE)
        enableLayout(focus);

        //set fab to show help according to layout
        fab.setOnClickListener(Helper.getFocusListener(this));

        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(lists);
        disableLayout(pick);

        //set background color of Focus to chosen, all others to toolbar default
        toolbarFocus.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary_accent));
        toolbarFocus.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarFocus.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));

        toolbarDrop.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarDrop.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        toolbarPick.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarPick.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarPick.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        toolbarLists.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarLists.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarLists.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

    }

    /**
     * TODO DESCRIBE
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializeLists() {

        //initialize ListView to show lists available
        ListView listView = findViewById(R.id.list_view_lists);
        RecyclerView recyclerView = findViewById(R.id.recycle_view_lists);

        //show fab when anything is being touched in listView
        listView.setOnTouchListener(new ShowFab());

        //show fab when anything is being touched in layout
        recyclerView.setOnTouchListener(new ShowFab());

        //Items that show the list your currently in as a header and the button for choosing a color
        LinearLayout headerLayout = findViewById(R.id.header);
        TextView headerTextView = findViewById(R.id.text_view_lists_header);
        Button headerButton = findViewById(R.id.button_header);

        listsListAdapter = new ListsListAdapter(this, listView, recyclerView, headerLayout, headerTextView, headerButton);

        //set adapter
        listView.setAdapter(listsListAdapter);

    }

    /**
     * TODO DESCRIBE
     */
    public void showLists(){

        //clear ArrayList for Lists, add current tasks from data and notify adapter (in case they have been altered in another layout)
        listsListAdapter.reset();

        //enable all components in the Lists layout (setVisibility = VISIBLE)
        enableLayout(lists);

        //set fab to show help according to layout
        fab.setOnClickListener(Helper.getListListener(this));

        //disable all components in all other layouts (setVisibility = GONE)
        disableLayout(drop);
        disableLayout(pick);
        disableLayout(focus);

        //set color of Lists-Button in toolbar to chosen, set all other Buttons to toolbar default
        toolbarLists.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary_accent));
        toolbarLists.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarLists.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));

        toolbarDrop.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarDrop.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarDrop.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        toolbarPick.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarPick.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarPick.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        toolbarFocus.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
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

    /**
     * TODO DESCRIBE
     * @param layout
     */
    //set Visibility of all first generation children of layout to VISIBLE and bring layout to front
    public void enableLayout(LinearLayout layout){

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setVisibility(View.VISIBLE);
        }
        layout.bringToFront();

    }

    /**
     * TODO DESCRIBE
     * @param layout
     */
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

    //custom onTouchListener that hides fab until View is touched upon. If view is being touched
    //show fab for 3 seconds
    private class ShowFab implements View.OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            fab.setVisibility(View.VISIBLE);
            fab.postDelayed(() -> fab.setVisibility(View.GONE), 3000);
            return false;
        }

    }

    //initialize RecyclerView with according adapter
    @SuppressLint("ClickableViewAccessibility")
    private void initializeRecyclerView(RecyclerView view, PickTaskListAdapter adapter){

        //show fab when view is being touched
        view.setOnTouchListener(new ShowFab());

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

        //sets references to other adapters in adapter
        adapter.setPickAdapter(pickAdapter);
        adapter.setDoNowAdapter(doNowAdapter);
        adapter.setDoLaterAdapter(doLaterAdapter);
        adapter.setMoveToListAdapter(moveToListAdapter);


    }

}