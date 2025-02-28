package net.berndreiss.zentodo;

import android.content.Context;

import net.berndreiss.zentodo.data.SQLiteHelper;

public class SharedData {

    public final Context context;
    public SQLiteHelper database;


    public SharedData(Context context){
        this.context = context;
        this.database = new SQLiteHelper(context);
    }
}

