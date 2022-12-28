package com.bdreiss.zentodo;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.dataManipulation.database.DbHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class DataTest {

    class TestClass{

        private String[] entryStrings;

        private Data data;

        TestClass(String[] entryStrings) {
            this.entryStrings = entryStrings;
        }

        public void set(){

            appContext.deleteDatabase(DATABASE_NAME);

            data = new Data(appContext,DATABASE_NAME);

            for (String s : entryStrings)
                data.add(s);


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
    public void constructor(){
        Data data = new Data(appContext, DATABASE_NAME);

        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        assert(db.loadEntries().size() == 0);
        assert(db.loadLists().size() == 0);
        assert(data.getEntries().size() == 0);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void add(){

        String[] stringData = DbHelper_V1Test.stringTestData;

        Data data = new Data(appContext, DATABASE_NAME);

        for (int i = 0; i < stringData.length; i++){
            data.add(stringData[i]);

            Entry entry = data.getEntries().get(i);

            assert(entry.getTask().equals(stringData[i]));
            assert(entry.getId() == i);
            assert(entry.getPosition() == i);

            DbHelper db = new DbHelper(appContext, DATABASE_NAME);
            entry = db.loadEntries().get(i);

            assert(entry.getTask().equals(stringData[i]));
            assert(entry.getId() == i);
            assert(entry.getPosition() == i);

        }


        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void idGeneration(){
        String[] strings = {"0","1","2","3"};

        TestClass test = new TestClass(strings);

        test.set();

        Data data = test.getData();

        data.remove(1);

        data.add("4");

        for (Entry e : data.getEntries())
            assert !e.getTask().equals("4") || (e.getId() == 1);

        data.remove(2);

        data.add("5");

        for (Entry e : data.getEntries())
            assert !e.getTask().equals("5") || (e.getId() == 2);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void remove(){


        int[][] tests = {{},{0}, {1}, {2},{0,1},{1,2},{0,2},{0,1,2}};

        int[][] results = {{0,1,2},{1,2},{0,2},{0,1},{2},{0},{1},{},};

        int[][] resultsPosition = {{0,1,2}, {0,1}, {0,1}, {0,1}, {0},{0},{0},{}};

        String[] entryStrings = {"0","1","2"};
        TestClass test = new TestClass(entryStrings);

        for (int i = 0; i < tests.length; i++){

            test.set();

            Data data = test.getData();

            for (int j = 0; j < tests[i].length; j++)
                data.remove(tests[i][j]);

            for (int j = 0; j < data.getEntries().size(); j++) {
                assert (data.getEntries().get(j).getId() == results[i][j]);
                assert (data.getEntries().get(j).getPosition() == resultsPosition[i][j]);
            }
        }

        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void swap(){


        int[][][] tests = {{{0,1}}, {{1,2}}, {{0,2}}, {{1,0}}, {{2,1}}, {{2,0}},
                            {{0,1},{1,0}}, {{0,2},{1,2}},{{0,1},{1,2}}
                            };

        int[][] results = {{1,0,2}, {0,2,1}, {2,1,0}, {1,0,2}, {0,2,1}, {2,1,0},
                            {0,1,2}, {1,2,0}, {2,0,1}
                            };

        String[] entryStrings = {"0","1","2"};

        TestClass test = new TestClass(entryStrings);

        for (int i = 0; i < tests.length; i++){

            test.set();

            Data data = test.getData();

            for (int j = 0; j < tests[i].length; j++)
                data.swap(tests[i][j][0],tests[i][j][1]);

            for (int j = 0; j < data.getEntries().size(); j++)
                assert(data.getEntries().get(j).getId() == results[i][j]);

        }

        appContext.deleteDatabase(DATABASE_NAME);


    }

    @Test
    public void swapList(){

    }

    @Test
    public void getPosition(){

    }

    @Test
    public void setTask(){

        String[] stringData = {"0","1","2"};

        TestClass test = new TestClass(stringData);

        test.set();

        Data data = test.getData();

        for (int i = 0; i < stringData.length; i++){
            data.setTask(i, "TEST");
        }

        for (Entry e : data.getEntries())
            assert(e.getTask().equals("TEST"));

        for (Entry e : new DbHelper(appContext, DATABASE_NAME).loadEntries())
            assert(e.getTask().equals("TEST"));

    }

    @Test
    public void setFocus(){

        String[] stringData = {"0","1","2"};

        TestClass test = new TestClass(stringData);

        test.set();

        Data data = test.getData();

        for (int i = 0; i < stringData.length; i++){
            data.setFocus(i, true);
        }

        for (Entry e : data.getEntries())
            assert(e.getFocus());

        for (Entry e : new DbHelper(appContext, DATABASE_NAME).loadEntries())
            assert(e.getFocus());


        for (int i = 0; i < stringData.length; i++){
            data.setFocus(i, false);
        }

        for (Entry e : data.getEntries())
            assert(!e.getFocus());

        for (Entry e : new DbHelper(appContext, DATABASE_NAME).loadEntries())
            assert(!e.getFocus());
    }

    @Test
    public void setDropped(){

        String[] stringData = {"0","1","2"};

        TestClass test = new TestClass(stringData);

        test.set();

        Data data = test.getData();

        for (int i = 0; i < stringData.length; i++){
            data.setDropped(i, true);
        }

        for (Entry e : data.getEntries())
            assert(e.getDropped());

        for (Entry e : new DbHelper(appContext, DATABASE_NAME).loadEntries())
            assert(e.getDropped());


        for (int i = 0; i < stringData.length; i++){
            data.setDropped(i, false);
        }

        for (Entry e : data.getEntries())
            assert(!e.getDropped());

        for (Entry e : new DbHelper(appContext, DATABASE_NAME).loadEntries())
            assert(!e.getDropped());
    }

    @Test
    public void setReminderDate(){

    }

    @Test
    public void editRecurrence(){

    }

    @Test
    public void editList(){

        String[] stringData = {"0","1","2","3","4","5"};

        TestClass test = new TestClass(stringData);

        String[] tests = {"0", "1"};

        String[] resultsList = {"0", "1", "0", "1", "0", "1"};

        int[] resultsPosition = {0,0,1,1,2,2};

        test.set();

        Data data = test.getData();

        for (int i = 0; i < data.getEntries().size(); i++) {
            data.editList(i, tests[i % 2]);
        }

        ArrayList<Entry> entries = new DbHelper(appContext,DATABASE_NAME).loadEntries();
        for (int i = 0; i < entries.size(); i++) {
            assert(entries.get(i).getList().equals(resultsList[i]));
            assert(entries.get(i).getListPosition() == resultsPosition[i]);
        }

        for (String s : tests) {
            assert (data.getLists().contains(s));
            assert(new DbHelper(appContext,DATABASE_NAME).loadLists().containsKey(s));
        }

        data.editList(0,null);

        assert(data.getEntries().get(2).getListPosition() == 0);
        assert(data.getEntries().get(4).getListPosition() == 1);

        data.editList(2,null);

        assert(data.getEntries().get(4).getListPosition() == 0);

        data.editList(4,null);

        assert(data.getLists().size() == 3);

        appContext.deleteDatabase(DATABASE_NAME);
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
