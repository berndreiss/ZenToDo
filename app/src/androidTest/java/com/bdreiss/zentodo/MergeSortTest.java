package com.bdreiss.zentodo;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bdreiss.zentodo.dataManipulation.Data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class MergeSortTest {

    static class TestClass{

        private final String[] entryStrings;

        private String[] listStrings;

        private Data data;

        TestClass(String[] entryStrings) {
            this.entryStrings = entryStrings;
        }
        TestClass(String[] entryStrings, String[] listStrings) {

            this.entryStrings = entryStrings;

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

    private static Context appContext;

    private static final String DATABASE_NAME = "TEST.db";

    @Before
    public void setup(){
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void mergeSort(){


        appContext.deleteDatabase(DATABASE_NAME);
    }



    @After
    public void cleanUp(){

        appContext.deleteDatabase(DATABASE_NAME);
    }

}
