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

    //The adapters fill the RecyclerViews of layouts above and are all derived from TaskListAdapter
    //for more information on the implementation see the according java files in adapters
    public PickTaskListAdapter pickAdapter;
    public PickTaskListAdapter doNowAdapter;
    public PickTaskListAdapter doLaterAdapter;
    public PickTaskListAdapter moveToListAdapter;
    public DropTaskListAdapter dropAdapter;
    public TaskListAdapter focusAdapter;
    public ListsListAdapter listsListAdapter;
    public ListTaskListAdapter listAdapter;
    public ListTaskListAdapter listsTaskListAdapter;//adapter for items in lists (items can be moved and get removed when list of task is changed)
    public AllTaskListAdapter allTasksAdapter;//adapter for showing all tasks (items can't be moved and do not get removed when list of task is changed)
    public NoListTaskListAdapter noListAdapter;//adapter for showing all tasks (items can't be moved and do not get removed when list of task is changed) TODO implement own adapter for removing items when list is changed
    public boolean itemIsInMotion = false;

    public SharedData(Context context){
        this.context = context;
        this.database = new ZenSQLiteHelper(context);
        this.uiOperationHandler = new UIOperationHandler(this);
    }
}

