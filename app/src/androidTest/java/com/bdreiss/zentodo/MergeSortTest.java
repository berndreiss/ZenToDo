package com.bdreiss.zentodo;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class MergeSortTest {

    private static Context appContext;

    private static String DATABASE_NAME;

    @Before
    public void setup(){
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DATABASE_NAME = appContext.getResources().getString(R.string.db_test);
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
