package com.bdreiss.zentodo;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Data {

    private static Context appContext;

    private static final String DATABASE_NAME = "TEST.db";

    @Before
    public void setup(){
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void constructor(){

    }

    @Test
    public void add(){

    }

    @Test
    public void remove(){

    }

    @Test
    public void swap(){

    }

    @Test
    public void swapList(){

    }

    @Test
    public void getPosition(){

    }

    @Test
    public void setTask(){

    }

    @Test
    public void setFocus(){

    }

    @Test
    public void setDropped(){

    }

    @Test
    public void setReminderDate(){

    }

    @Test
    public void editRecurrence(){

    }

    @Test
    public void editList(){

    }

    @Test
    public void editListColor(){

    }

    @Test
    public void getListColor(){

    }

    //test for incrementListPositionCount

    @Test
    public void decrementListPositionCount(){

    }

    @Test
    public void returnListAsArray(){

    }

    @Test
    public void getLists(){

    }

    @Test
    public void getTasksToPick(){

    }

    @Test
    public void getList(){

    }

    @Test
    public void getDropped(){

    }

    @Test
    public void getFocus(){

    }

    //Test for generate id

    @Test
    public void getToday(){

    }

    @Test
    public void getNoList(){

    }

    @Test
    public void getEntries(){

    }

    @Test
    public void getEntriesOrderedByDate(){

    }

    @Test
    public void setRecurring(){

    }

    //test for incrementRecurring

    //test for return days of the month

    //test for isLeapYear



    @After
    public void cleanUp(){

        appContext.deleteDatabase(DATABASE_NAME);
    }

}
