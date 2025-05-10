package net.berndreiss.zentodo;

import android.content.Context;

import net.berndreiss.zentodo.data.SQLiteHelper;
import net.berndreiss.zentodo.data.UIOperationHandler;
import net.berndreiss.zentodo.util.ClientStub;

public class SharedData {

    public final Context context;
    public SQLiteHelper database;
    public ClientStub clientStub;
    public UIOperationHandler uiOperationHandler = new UIOperationHandler();


    public SharedData(Context context){
        this.context = context;
        this.database = new SQLiteHelper(context);
    }
}

