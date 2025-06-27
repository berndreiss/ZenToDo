package net.berndreiss.zentodo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import net.berndreiss.zentodo.adapters.DropTaskListAdapter;
import net.berndreiss.zentodo.adapters.recyclerViewHelper.CustomItemTouchHelperCallback;
import net.berndreiss.zentodo.adapters.FocusTaskListAdapter;
import net.berndreiss.zentodo.adapters.ListsListAdapter;
import net.berndreiss.zentodo.adapters.PickTaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;

import net.berndreiss.zentodo.data.Task;
import net.berndreiss.zentodo.databinding.ActivityMainBinding;
import net.berndreiss.zentodo.util.ClientStub;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


/**
 *   A simple To-Do-List.
 *   The idea is, that you drop tasks you need to do, so you don't have to think about them anymore.
 *   When you want to make your to-do-list for the day, you pick out of all the task that have been
 *   dropped. In this step you decide, which tasks you want to do now. For all the tasks you don't
 *   want to get done right away, you can set a reminder date or add them to a list.
 *   Basically the app has the following structure implemented in different layouts:
 *     -DROP -> PICK -> FOCUS
 *     -Lists: shows all the lists and containing tasks assigned to it.
 *   These layouts consist of RecyclerViews. Each item in these RecyclerViews represents a task (dataManipulation.Task).
 *   It shows the description of the task, a checkbox and a menu button. The checkbox removes a task.
 *   The menu consist of the following buttons:
 *       moveToFocus -> adds task to RecyclerView in FOCUS
 *       editTask -> option to edit description of the task
 *       setReminderDate -> set a reminder date for the task
 *       setRecurrence -> option to make task repeatable
 *       editList -> assign task to a list
 *   These are the basic functionalities for the buttons and the checkbox. They may vary from
 *   layout to layout. A precise description of these functions can be found in adapters.
 *   The adapters handle the functionality of the buttons. Each layouts RecyclerView has its own
 *   adapter although all are derived from TaskListAdapter. I.e. the layout FOCUS is handled by
 *   FocusTaskListAdapter.
 *   If you got any questions/suggestions feel free to email me: bd_reiss@gmx.at
 */
public class MainActivity extends AppCompatActivity {

    public static final String DATABASE_NAME = "Data.db";
    public static final int DATABASE_VERSION = 1;

    private LinearLayout pick;//Layout to pick tasks that have been dropped and show them in focus
    private LinearLayout drop;//Layout to drop new tasks
    private LinearLayout focus;//Layout to show tasks to do today
    private LinearLayout lists;//Layout for all lists with tasks in it

    //TODO add user fab
    //the following Buttons are components of the toolbar to switch between the different layouts
    private Button toolbarPick;
    private Button toolbarDrop;
    private Button toolbarFocus;
    private Button toolbarLists;
    private Button toolbarOptions;
    private LinearLayout toolbarOptionsContainer;
    private View grayOverlay;

    //floating action buttons on the bottom right corner
    private FloatingActionButton fabHelp;
    private FloatingActionButton fabSettings;
    private FloatingActionButton fabUser;

    //Object for data shared across the whole application
    private SharedData sharedData;

    //TODO comment
    private boolean isFabMenuOpen = false;

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (sharedData != null)
            sharedData.database.close();
    }
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
        toolbarOptions = findViewById(R.id.toolbar_options);
        toolbarOptionsContainer = findViewById(R.id.toolbar_options_container);
        grayOverlay = findViewById(R.id.gray_overlay);

        //Floating action button that shows help on press
        fabHelp = findViewById(R.id.fabHelp);
        //Floating action button that shows settings on press
        fabSettings = findViewById(R.id.fabSettings);
        //Floating action button that shows user settings on press
        fabUser = findViewById(R.id.fabUser);

        toolbarOptions.setOnClickListener((View _) ->toggleFabMenu());
        toolbarOptions.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarOptionsContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
        toolbarOptions.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
        toolbarOptions.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));

        //OnClickListeners for buttons in toolbar which show according layout onClick
        toolbarPick.setOnClickListener(_ -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showPick();//show Pick layout
        });

        toolbarDrop.setOnClickListener(_ -> {
            showDrop();//show Drop layout
        });

        toolbarFocus.setOnClickListener(_ -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showFocus();//show Focus layout
        }); findViewById(R.id.fabSettings);

        toolbarLists.setOnClickListener(_ -> {
            closeKeyboard();//closes keyboard that might still be opened from editText in Drop layout
            showLists();//show Lists layout
        });

        deleteDatabase(MainActivity.DATABASE_NAME);
        sharedData = new SharedData(this);

        //initialize client stub
        sharedData.clientStub = new ClientStub( sharedData.database.getDatabase());
        //Communicating important messages to the user (e.g., "User logged in", "There was a problem
        //sending data to the server" etc.)
        Consumer<String> messagePrinter = message -> {
            Looper.prepare();
            new Handler(Looper.getMainLooper()).post(()-> Toast.makeText(sharedData.context, message, Toast.LENGTH_LONG).show());
        };
        sharedData.clientStub.setMessagePrinter(messagePrinter);
        //The uiOperationHandler handles the interaction with the views
        sharedData.clientStub.addOperationHandler(sharedData.uiOperationHandler);

        try {
            DataManager.initClientStub(sharedData, "bd_reiss@yahoo.de");
            //DataManager.initClientStub(sharedData, null);
        } catch (InterruptedException e) {
            //TODO logging
            System.out.println(e.getMessage());
        }

        //layouts are initialized
        initializeDrop(sharedData);
        initializePick(sharedData);
        initializeFocus(sharedData);
        initializeLists(sharedData);

        //Drop is shown when app starts
        showDrop();
    }

    private void toggleFabMenu() {
        if (isFabMenuOpen) {
            grayOverlay.setVisibility(View.GONE);
            toolbarOptions.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
            toolbarOptionsContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
            toolbarOptions.setTextColor(ContextCompat.getColor(this, R.color.color_primary_variant));
            toolbarOptions.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary_variant));
            fabHelp.hide();
            fabSettings.hide();
            fabUser.hide();
        } else {
            grayOverlay.setVisibility(View.VISIBLE);
            toolbarOptions.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary_accent));
            toolbarOptionsContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary_accent));
            toolbarOptions.setTextColor(ContextCompat.getColor(this, R.color.color_primary));
            toolbarOptions.getCompoundDrawables()[1].setTint(ContextCompat.getColor(this, R.color.color_primary));
            fabHelp.show();
            fabSettings.show();
            fabUser.show();
        }
        isFabMenuOpen = !isFabMenuOpen;
    }
    /**
     * Initialize the PICK view.
     * @param sharedData the shared data object
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializePick(SharedData sharedData) {

        //initialize adapter for RecyclerView with all tasks that have been dropped, have been in Focus or are due today
        sharedData.pickAdapter = new PickTaskListAdapter(sharedData, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * pickAdapter.tasks.size());
                findViewById(R.id.list_view_pick).setLayoutParams(params);
            }
        };

        //initialize empty adapters for the other RecyclerViews
        sharedData.doNowAdapter = new PickTaskListAdapter(sharedData, true){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_doNow).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * doNowAdapter.tasks.size());
                findViewById(R.id.list_view_pick_doNow).setLayoutParams(params);
            }
        };

        sharedData.doLaterAdapter = new PickTaskListAdapter(sharedData, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_doLater).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * doLaterAdapter.tasks.size());
                findViewById(R.id.list_view_pick_doLater).setLayoutParams(params);
            }
        };

        sharedData.moveToListAdapter = new PickTaskListAdapter(sharedData, false){
            @Override
            public void itemCountChanged(){
                ViewGroup.LayoutParams params = findViewById(R.id.list_view_pick_list).getLayoutParams();
                params.height = (int) (getResources().getDimension(R.dimen.row_height) * moveToListAdapter.tasks.size());
                findViewById(R.id.list_view_pick_list).setLayoutParams(params);
            }
        };

        //Set up ItemTouchHelper to move tasks around and connect View to each other
        initializeRecyclerView(findViewById(R.id.list_view_pick), sharedData.pickAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_doNow), sharedData.doNowAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_doLater), sharedData.doLaterAdapter);
        initializeRecyclerView(findViewById(R.id.list_view_pick_list), sharedData.moveToListAdapter);

        //button to pick tasks that have been checked
        Button pickButton = findViewById(R.id.button_pick);

        pickButton.setTextColor(ContextCompat.getColor(this,R.color.color_primary));

        //if pressed remove tasks from Drop and add to Focus
        pickButton.setOnClickListener(_ -> {
            //Continue if all tasks have been categorized, show hint otherwise
            if (!sharedData.pickAdapter.tasks.isEmpty()){
                Helper.showPickHelper(this);
            }
            else {
                //Copy list because it will be changed during loop
                List<Task> doNowCopy = new ArrayList<>(sharedData.doNowAdapter.tasks);
                //if checked reset dropped and focus attributes of all task in tasksToDoNow
                for (Task t : doNowCopy) {
                    DataManager.setFocus(sharedData, t, true);
                    DataManager.setDropped(sharedData, t, false);
                }
                //set focus to false for all tasks in tasksToDoLater
                for (long t : sharedData.doLaterAdapter.dateMap.keySet()) {
                    Optional<Task> task = sharedData.clientStub.getTask(t);
                    if (task.isEmpty())
                        continue;
                    DataManager.editReminderDate(sharedData, task.get(), sharedData.doLaterAdapter.dateMap.get(t));
                    //DataManager.setFocus(sharedData, t, false);
                }


                //set focus to false for all tasks in tasksToMoveToList
                for (long t : sharedData.moveToListAdapter.listMap.keySet()){
                    Optional<Task> task = sharedData.clientStub.getTask(t);
                    if (task.isEmpty())
                        continue;
                    DataManager.editList(sharedData, task.get(), sharedData.moveToListAdapter.listMap.get(t));
                }
                //show Focus layout
                showFocus();
            }
        });
    }

    /**
     * Show the pick view.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void showPick(){
        //Set the mode
        sharedData.mode = Mode.PICK;
        //Reset and Update Adapters to reflect changes
        //Also update itemCountChanged, so that RecyclerViews get resized properly
        sharedData.pickAdapter.reset();
        //enable components of Pick layout (setVisibility = VISIBLE)
        enableLayout(pick);
        //set fab to show help according to layout
        fabHelp.setOnClickListener(Helper.getPickListener(this));
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
     * Initialize the DROP view.
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializeDrop(SharedData sharedData){
        //initialize adapter for ListView that shows tasks dropped
        sharedData.dropAdapter = new DropTaskListAdapter(sharedData);
        //assign ListView
        RecyclerView listView = findViewById(R.id.list_view_drop);
        //show fab when anything is being touched in listViews
        listView.setOnTouchListener(new ShowFab());
        //show fab when anything is being touched in layout
        drop.setOnTouchListener(new ShowFab());
        //set adapter
        listView.setAdapter(sharedData.dropAdapter);
        //set layoutManager
        listView.setLayoutManager(new LinearLayoutManager(this));
        //allows items to be moved and reordered in RecyclerView
        ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(sharedData.dropAdapter, listView);
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
        setDropListener(editText, buttonDrop);
    }

    /**
     * Set a listener for dropping tasks.
     * @param editText the text field for adding tasks
     * @param buttonDrop the DROP button
     */
    public void setDropListener(EditText editText, Button buttonDrop){
        //OnClickListener that adds task in EditText to Data
        buttonDrop.setOnClickListener(_ -> {
            //get task from EditText
            String task = editText.getText().toString();
            //reset EditText
            editText.setText("");
            //add task to Data if it is not empty
            if (!task.trim().isEmpty())
                sharedData.dropAdapter.add(task);
        });
    }

    /**
     * Show the DROP view.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void showDrop(){
        //Set the mode
        sharedData.mode = Mode.DROP;
        //clear ArrayList for Drop, add current tasks from data and notify adapter (in case they have been altered in another layout)
        sharedData.dropAdapter.reset();
        //enable all components of Drop layout (setVisibility = VISIBLE)
        enableLayout(drop);
        //set fab to show help according to layout
        fabHelp.setOnClickListener(Helper.getDropListener(this));
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
     * Initialize the FOCUS view.
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializeFocus(SharedData sharedData){
        //initialize the adapter for the ListView to show the tasks to focus on
        sharedData.focusAdapter = new FocusTaskListAdapter(sharedData);
        //assign RecyclerView
        RecyclerView listView = findViewById(R.id.list_view_focus);
        //show fab when anything is being touched in listView
        listView.setOnTouchListener(new ShowFab());
        //show fab when anything is being touched in layout
        focus.setOnTouchListener(new ShowFab());
        //set adapter for Recyclerview
        listView.setAdapter(sharedData.focusAdapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        //allows items to be moved and reordered in RecyclerView
        ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(sharedData.focusAdapter, listView);
        //create ItemTouchHelper and assign to RecyclerView
        ItemTouchHelper iTouchHelper = new ItemTouchHelper(callback);
        iTouchHelper.attachToRecyclerView(listView);
    }

    /**
     * Show the FOCUS view.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void showFocus(){
        //Set the mode
        sharedData.mode = Mode.FOCUS;
        //clear ArrayList for Focus, add current tasks from data and notify adapter (in case they have been altered in another layout)
        sharedData.focusAdapter.reset();
        //enable all components in the Focus layout (setVisibility = VISIBLE)
        enableLayout(focus);
        //set fab to show help according to layout
        fabHelp.setOnClickListener(Helper.getFocusListener(this));
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
     * Initialize the LISTS view.
     */
    @SuppressLint("ClickableViewAccessibility")
    public void initializeLists(SharedData sharedData) {
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
        sharedData.listsListAdapter = new ListsListAdapter(sharedData, listView, recyclerView, headerLayout, headerTextView, headerButton);
        //set adapter
        listView.setAdapter(sharedData.listsListAdapter);
    }

    /**
     * Show the LISTS view.
     */
    public void showLists(){
        //set the mode
        sharedData.mode = Mode.LIST_OF_LISTS;
        //clear ArrayList for Lists, add current tasks from data and notify adapter (in case they have been altered in another layout)
        sharedData.listsListAdapter.reset();
        //enable all components in the Lists layout (setVisibility = VISIBLE)
        enableLayout(lists);
        //set fab to show help according to layout
        fabHelp.setOnClickListener(Helper.getListListener(this));
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
     * Set Visibility of all first generation children of layout to VISIBLE and bring layout to front.
     * @param layout the layout to enable.
     */
    public void enableLayout(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setVisibility(View.VISIBLE);
        }
        layout.bringToFront();
    }

    /**
     * Set Visibility of all first generation children of layout to GONE.
     * @param layout the layout to disable
     */
    public void disableLayout(LinearLayout layout){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setVisibility(View.GONE);
        }
    }

    //closes the keyboard, code from: https://www.geeksforgeeks.org/how-to-programmatically-hide-android-soft-keyboard/
    private void closeKeyboard()
    {
        // this will give us the view which is currently focus in this layout
        View view = this.getCurrentFocus();
        // if nothing is currently focused then this will protect the app from crashing
        if (view != null) {
            // now assign the system service to InputMethodManager
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
            //fabMain.setVisibility(View.GONE);
            //fabMain.postDelayed(() -> fabMain.setVisibility(View.VISIBLE), 3000);
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
        ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(adapter, view);
        //create ItemTouchHelper and assign to RecyclerView
        ItemTouchHelper iTouchHelper = new ItemTouchHelper(callback);
        iTouchHelper.attachToRecyclerView(view);
        //TODO we may be able to simplify this
        //sets references to other adapters in adapter
        adapter.setPickAdapter(sharedData.pickAdapter);
        adapter.setDoNowAdapter(sharedData.doNowAdapter);
        adapter.setDoLaterAdapter(sharedData.doLaterAdapter);
        adapter.setMoveToListAdapter(sharedData.moveToListAdapter);
    }

}