package net.berndreiss.zentodo;

import android.content.Context;

import net.berndreiss.zentodo.adapters.AllTaskListAdapter;
import net.berndreiss.zentodo.adapters.DropTaskListAdapter;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
import net.berndreiss.zentodo.adapters.ListsListAdapter;
import net.berndreiss.zentodo.adapters.NoListTaskListAdapter;
import net.berndreiss.zentodo.adapters.PickTaskListAdapter;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.data.ZenSQLiteHelper;
import net.berndreiss.zentodo.data.UIOperationHandler;
import net.berndreiss.zentodo.util.ClientStub;

/**
 * An object for data shared across the whole application.
 */
public class SharedData {

    /** The context of the application */
    public final Context context;
    /** The database in use */
    public ZenSQLiteHelper database;
    /** The client stub in use */
    public ClientStub clientStub;
    /** The operation handler for interacting with views */
    public UIOperationHandler uiOperationHandler;
    /** The current mode */
    public Mode mode;

    //The adapters hold all tasks and fill the RecyclerViews of the different layouts .
    //All derived from TaskListAdapter; for more information on the implementation see the according
    //.java files in adapters
    /** The adapter for the general list in the PICK view */
    public PickTaskListAdapter pickAdapter;
    /** The adapter for the do-now-list in the PICK view */
    public PickTaskListAdapter doNowAdapter;
    /** The adapter for the do-later-list in the PICK view */
    public PickTaskListAdapter doLaterAdapter;
    /** The adapter for the move-to-list-list in the PICK view */
    public PickTaskListAdapter moveToListAdapter;
    /** The adapter for the DROP view */
    public DropTaskListAdapter dropAdapter;
    /** The adapter for the FOCUS view */
    public TaskListAdapter focusAdapter;
    /** The adapter for the LISTS overview view */
    public ListsListAdapter listsListAdapter;
    /** The adapter for the LISTS view for a specific list */
    public ListTaskListAdapter listAdapter;
    /** The adapter for the "ALL TASKS" list in the LISTS view */
    public AllTaskListAdapter allTasksAdapter;
    /** The adapter for the "No List" list in the LISTS view */
    public NoListTaskListAdapter noListAdapter;
    public boolean itemIsInMotion = false;

    public SharedData(Context context){
        this.context = context;
        this.database = new ZenSQLiteHelper(context);
        this.uiOperationHandler = new UIOperationHandler(this);
    }
}

