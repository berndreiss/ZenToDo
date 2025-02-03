package com.bdreiss.zentodo;

import android.content.Context;

import net.berndreiss.zentodo.dataManipulation.Data;

public class TestClass{

    private final String DATABASE_NAME;

    private String[] entryStrings;

    private final Context appContext;

    private String[] listStrings;

    private Data data;



    public TestClass(Context appContext){
        this.appContext = appContext;
        this.DATABASE_NAME = appContext.getResources().getString(R.string.db_test);
    }

    public TestClass(Context appContext, String[] entryStrings) {
        this(appContext);
        this.entryStrings = entryStrings;

    }
    TestClass(Context appContext, String[] entryStrings, String[] listStrings) {
        this(appContext, entryStrings);
        this.listStrings = listStrings;
    }

    public void set(){

        appContext.deleteDatabase(DATABASE_NAME);

        data = new Data(appContext,DATABASE_NAME);

        for (String s : entryStrings)
            data.add(s);

        if (listStrings != null)
            for (int i = 0; i < data.getEntries().size(); i++)
                data.editList(i, listStrings[i]);

    }

    public Data getData(){return data;}

}
