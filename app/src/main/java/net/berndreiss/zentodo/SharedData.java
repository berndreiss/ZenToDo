package net.berndreiss.zentodo;

import android.content.Context;

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
    /** The current adapter holding tasks */
    public TaskListAdapter adapter;

    public SharedData(Context context){
        this.context = context;
        this.database = new ZenSQLiteHelper(context);
        this.uiOperationHandler = new UIOperationHandler(this);
    }
}

