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

        String[] stringData = {"0","1","2","3","4","5"};

        String[] tests = {"0", "1", "0", "1", "0", "1"};

        int[][][] swaps = {{{0,2}},{{0,4}}, {{2,4}},{{2,0}},{{4,0}}, {{4,2}},
                {{0,2},{2,4}},{{0,2},{2,4},{0,4}},{{0,2},{2,4},{0,4},{2,4}}
        };

        int[][] resultsListPosition = {
                {1,0,0,1,2,2},
                {2,0,1,1,0,2},
                {0,0,2,1,1,2},
                {1,0,0,1,2,2},
                {2,0,1,1,0,2},
                {0,0,2,1,1,2},
                {1,0,2,1,0,2},
                {0,0,2,1,1,2},
                {0,0,1,1,2,2}
        };

        int[][] resultsPosition = {
                {2,1,0,3,4,5},
                {4,1,2,3,0,5},
                {0,1,4,3,2,5},
                {2,1,0,3,4,5},
                {4,1,2,3,0,5},
                {0,1,4,3,2,5},
                {2,1,4,3,0,5},
                {0,1,4,3,2,5},
                {0,1,2,3,4,5}

        };

        TestClass test = new TestClass(stringData, tests);


        for (int i = 0; i < swaps.length; i++){
            test.set();

            Data data = test.getData();

            for (int j = 0; j < swaps[i].length; j++)
                data.swapList(swaps[i][j][0],swaps[i][j][1]);

            for (Entry e : data.getEntries()) {
                assert (e.getListPosition() == resultsListPosition[i][e.getId()]);
                assert (e.getPosition() == resultsPosition[i][e.getId()]);
            }
        }


    }

    @Test
    public void getPosition(){

        String[] taskStrings = {"0","1","2","3"};

        TestClass test = new TestClass(taskStrings);

        test.set();

        for (int i = 0; i < taskStrings.length; i++)
            assert(test.getData().getPosition(i)==i);

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
    public void editReminderDate(){

        String[] taskStrings = {"0","1","2","3"};

        int[] tests = {0, 20110311, 0, 2000};

        TestClass test = new TestClass(taskStrings);

        test.set();

        Data data = test.getData();

        for (int i = 0; i < taskStrings.length; i++)
            data.editReminderDate(i,tests[i]);

        ArrayList<Entry> entries = new DbHelper(appContext, DATABASE_NAME).loadEntries();

        for (int i = 0; i < taskStrings.length; i++)
            assert(entries.get(0).getReminderDate()== tests[entries.get(0).getId()]);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void editRecurrence(){

        String[] taskStrings = {"0","1","2","3"};

        String[] tests = {"w2", "d999", "y88", "m3"};

        TestClass test = new TestClass(taskStrings);

        test.set();

        Data data = test.getData();

        for (int i = 0; i < taskStrings.length; i++)
            data.editRecurrence(i,tests[i]);

        ArrayList<Entry> entries = new DbHelper(appContext, DATABASE_NAME).loadEntries();

        for (int i = 0; i < taskStrings.length; i++)
            assert(entries.get(0).getRecurrence().equals(tests[entries.get(0).getId()]));

        for (int i = 0; i < tests.length; i++)
            data.editRecurrence(i,null);

        entries = new DbHelper(appContext, DATABASE_NAME).loadEntries();

        for (Entry e : entries)
            assert(e.getRecurrence()==null);

        appContext.deleteDatabase(DATABASE_NAME);

    }

    @Test
    public void editList(){

        String[] stringData = {"0","1","2","3","4","5"};

        String[] tests = {"0", "1", "0", "1", "0", "1"};

        String[] resultsList = {"0", "1", "0", "1", "0", "1"};

        int[] resultsPosition = {0,0,1,1,2,2};

        TestClass test = new TestClass(stringData, tests);

        test.set();

        Data data = test.getData();

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

        DbHelper db = new DbHelper(appContext,DATABASE_NAME);

        String[] lists = {"0", "1", "2"};
        String[] initialColors = {"WHITE", "RED", "BLUE"};
        String[] results = {"BLUE", "WHITE", "RED"};
        for (int i = 0; i < lists.length; i++)
            db.addList(lists[i],initialColors[i]);

        Data data = new Data(appContext,DATABASE_NAME);

        for (int i = 0; i < lists.length;i++)
            data.editListColor(lists[i],results[i]);

        for (int i = 0; i < lists.length; i++)
            assert(data.getListColor(lists[i]).equals(results[i]));

        appContext.deleteDatabase(DATABASE_NAME);


    }

    @Test
    public void getListColor(){

        DbHelper db = new DbHelper(appContext,DATABASE_NAME);

        String[] lists = {"0", "1", "2"};
        String[] colors = {"WHITE", "RED", "BLUE"};

        for (int i = 0; i < lists.length; i++)
            db.addList(lists[i],colors[i]);

        Data data = new Data(appContext,DATABASE_NAME);

        for (int i = 0; i < lists.length; i++)
            assert(data.getListColor(lists[i]).equals(colors[i]));

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //test for incrementListPositionCount

    @Test
    public void decrementListPositionCount(){
        String[] tasks = {"0","1","2", "3", "4", "5"};
        String[] lists = {"0","0", "0", "1", "1", "1"};


        String[][][] tests = {{{"2",null}},{{"1",null}},{{"0", null}},
                {{"2", null},{"1",null}},{{"1", null},{"0",null}},{{"2", null},{"0",null}},
                {{"1", null},{"2",null}},{{"0", null},{"1",null}},{{"0", null},{"2",null}},
                {{"2", null},{"1",null},{"0",null}}, {{"1", null},{"2",null},{"0",null}}, {{"0", null},{"1",null},{"2",null}}
        };

        int[][] results = {{0, 1, -1, 0, 1, 2},{0, -1, 1, 0, 1, 2},{-1, 0, 1, 0, 1, 2},
                {0, -1, -1, 0, 1, 2},{-1, -1, 0, 0, 1, 2},{-1, 0, -1, 0, 1, 2},
                {0, -1, -1, 0, 1, 2},{-1, -1, 0, 0, 1, 2},{-1, 0, -1, 0, 1, 2},
                {-1, -1, -1, 0, 1, 2},{-1, -1, -1, 0, 1, 2},{-1, -1, -1, 0, 1, 2}

        };

        TestClass test = new TestClass(tasks,lists);

        for (int i = 0; i < tests.length; i++){

            test.set();

            Data data = test.getData();

            for (int j = 0; j < tests[i].length; j++)
                data.editList(Integer.parseInt(tests[i][j][0]),tests[i][j][1]);

            for (Entry e : data.getEntries())
                assert (e.getListPosition() == results[i][e.getId()]);


        }

        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void returnListAsArray(){

        String[] tasks = {"0","1","2","3"};

        String[] lists = {"0","1","2","3"};

        TestClass test = new TestClass(tasks,lists);

        test.set();

        Data data = test.getData();

        String[] returnedLists = data.returnListsAsArray();

        assert(returnedLists.length == lists.length+2);

        for (String s : lists) {
            boolean contains = false;
            for (String rs :returnedLists)
                if (rs.equals(s)) {
                    contains = true;
                    break;
                }
            assert (contains);
        }

        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void getLists(){

        String[] tasks = {"0","1","2","3"};

        String[] lists = {"0","1","2","3"};

        TestClass test = new TestClass(tasks,lists);

        test.set();

        Data data = test.getData();

        ArrayList<String> returnedLists = data.getLists();

        assert(returnedLists.size() == lists.length+2);

        for (String s : lists) {
            boolean contains = false;
            for (String rs :returnedLists)
                if (rs.equals(s)) {
                    contains = true;
                    break;
                }
            assert (contains);
        }

        appContext.deleteDatabase(DATABASE_NAME);

    }

    @Test
    public void getTasksToPick(){

        String[] tasks = {"0","1","2","3"};

        int[] dates = {Data.getToday()-1,Data.getToday()+1,Data.getToday(),Data.getToday()+1};

        String[] results = {"0","2"};

        TestClass test = new TestClass(tasks);

        test.set();

        Data data = test.getData();

        ArrayList<Entry> tasksToPick = data.getTasksToPick();

        for (int i=0; i< tasksToPick.size();i++)
            assert(tasksToPick.get(i).getTask().equals(tasks[i]));

        for (int i = 0; i < tasks.length; i++)
            data.editReminderDate(Integer.parseInt(tasks[i]), dates[i]);

        tasksToPick = data.getTasksToPick();

        assert(tasksToPick.size() == results.length);

        for (int i = 0; i < tasksToPick.size(); i++)
            assert(tasksToPick.get(i).getTask().equals(results[i]));


        appContext.deleteDatabase(DATABASE_NAME);

    }

    @Test
    public void getList(){
        String[] tasks = {"0","1","2","3","4","5"};

        String[] lists = {"0","0","0","1","1","1"};

        TestClass test = new TestClass(tasks,lists);

        test.set();

        Data data = test.getData();

        ArrayList<Entry> returnedList = data.getList("0");

        for (int i = 0; i < returnedList.size(); i++){
            assert(returnedList.get(i).getTask().equals(String.valueOf(i)));
        }


        returnedList = data.getList("1");

        for (int i = 0; i < returnedList.size(); i++){
            assert(returnedList.get(i).getTask().equals(String.valueOf(i+3)));
        }

        appContext.deleteDatabase(DATABASE_NAME);

    }

    @Test
    public void getDropped(){

        String[] tasks = {"0","1","2"};

        TestClass test = new TestClass(tasks);

        test.set();

        Data data = test.getData();

        ArrayList<Entry> dropped = data.getDropped();

        for (int i = 0; i < tasks.length; i++)
            assert(dropped.get(i).getTask().equals(tasks[i]));

        tasks = new String[0];

        test = new TestClass(tasks);

        test.set();

        data = test.getData();

        dropped = data.getDropped();

        assert(dropped.size()==0);

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
