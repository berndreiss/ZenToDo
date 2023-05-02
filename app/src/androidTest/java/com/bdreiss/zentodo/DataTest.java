package com.bdreiss.zentodo;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.dataManipulation.database.DbHelperV1;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class DataTest {

    private static Context appContext;

    private static final String DATABASE_NAME = "TEST.db";

    @Before
    public void setup(){
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    //Test if the Data object is instantiated correctly
    @Test
    public void constructor(){

        Data data = new Data(appContext, DATABASE_NAME);

        DbHelperV1 db = new DbHelperV1(appContext, DATABASE_NAME);

        //assert all main data structures are empty
        assert(db.loadEntries().size() == 0);
        assert(db.loadLists().size() == 0);
        assert(data.getEntries().size() == 0);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //Tests if tasks are added correctly
    @Test
    public void add(){

        //Test data that contains all commonly used special characters and in particular "'", since
        //there have been problems adding tasks containing "'", have been causing problems for
        //SQL commands. Should be fixed now but better safe than sorry.
        String[] stringData = DbHelper_V1Test.stringTestData;

        Data data = new Data(appContext, DATABASE_NAME);

        for (int i = 0; i < stringData.length; i++){

            data.add(stringData[i]);

            //get entries of Data instance and assert results
            Entry entry = data.getEntries().get(i);

            assert(entry.getTask().equals(stringData[i]));
            assert(entry.getId() == i);
            assert(entry.getPosition() == i);

            //get new instance with data from the save file and assert results
            DbHelperV1 db = new DbHelperV1(appContext, DATABASE_NAME);
            entry = db.loadEntries().get(i);

            assert(entry.getTask().equals(stringData[i]));
            assert(entry.getId() == i);
            assert(entry.getPosition() == i);

        }


        appContext.deleteDatabase(DATABASE_NAME);
    }

    //test if ids are generated correctly. The id will always be the lowest number still not used
    //by a task starting from 0, i.e. the first task will be 0, the second 1 and so on. If the
    //first task is removed however, the third one will be 0 again, but the fourth will be 2.
    @Test
    public void idGeneration(){

        //strings representing ids for creating test tasks
        String[] strings = {"0","1","2","3"};

        //instantiate test class
        TestClass test = new TestClass(appContext, strings);
        //set test data to initial values
        test.set();
        Data data = test.getData();

        //assert ids match task
        for (Entry e: data.getEntries())
            assert(e.getId() == Integer.parseInt(e.getTask()));

        //remove second task with expected result: "0": 0, "2": 2, "3": 3}
        data.remove(1);

        //add task with expected result: {"0": 0, "2": 2, "3": 3, "4": 1}
        data.add("4");

        //assert that added task has expected id
        for (Entry e : data.getEntries())
            assert !e.getTask().equals("4") || (e.getId() == 1);

        //remove third task with expected result: {"0": 0, "3": 3, "4": 1}
        data.remove(2);

        //add task with expected result: {"0": 0, "3": 3, "4": 1, "5": 2}
        data.add("5");

        //assert that task has expected id
        for (Entry e : data.getEntries())
            assert !e.getTask().equals("5") || (e.getId() == 2);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //tests the removal of tasks from entryStrings via their ids
    //for each consecutive test the tasks are reset, so all tests are run on the same data
    //besides tasks being removed, also the position of other tasks might change
    @Test
    public void remove(){

        //ids to be removed
        int[][] tests = {{},{0}, {1}, {2},{0,1},{1,2},{0,2},{0,1,2}};

        //expected entries after removal
        int[][] results = {{0,1,2},{1,2},{0,2},{0,1},{2},{0},{1},{},};

        //expected positions of tasks after removal
        int[][] resultsPosition = {{0,1,2}, {0,1}, {0,1}, {0,1}, {0},{0},{0},{}};

        //dummy tasks
        String[] entryStrings = {"0","1","2"};

        //instantiate test class
        TestClass test = new TestClass(appContext, entryStrings);

        for (int i = 0; i < tests.length; i++){

            //set test data to initial values
            test.set();
            Data data = test.getData();

            //remove tasks
            for (int j = 0; j < tests[i].length; j++)
                data.remove(tests[i][j]);

            //assert results
            for (int j = 0; j < data.getEntries().size(); j++) {
                assert (data.getEntries().get(j).getId() == results[i][j]);
                assert (data.getEntries().get(j).getPosition() == resultsPosition[i][j]);
            }
        }

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //tests swapping tasks
    @Test
    public void swap(){

        //ids being swapped
        int[][][] tests = {{{0,1}}, {{1,2}}, {{0,2}}, {{1,0}}, {{2,1}}, {{2,0}},
                            {{0,1},{1,0}}, {{0,2},{1,2}},{{0,1},{1,2}}
                            };

        //expected results in entries
        int[][] results = {{1,0,2}, {0,2,1}, {2,1,0}, {1,0,2}, {0,2,1}, {2,1,0},
                            {0,1,2}, {1,2,0}, {2,0,1}
                            };

        //dummy test data
        String[] entryStrings = {"0","1","2"};

        //instantiate test class
        TestClass test = new TestClass(appContext, entryStrings);

        for (int i = 0; i < tests.length; i++){

            //set test data to initial values
            test.set();
            Data data = test.getData();

            //swap tasks
            for (int j = 0; j < tests[i].length; j++)
                data.swap(tests[i][j][0],tests[i][j][1]);

            //assert results
            for (int j = 0; j < data.getEntries().size(); j++)
                assert(data.getEntries().get(j).getId() == results[i][j]);

        }

        appContext.deleteDatabase(DATABASE_NAME);

    }


    //tests swapping items in lists: when items in a list are swapped, not only the Entry.listPosition
    //but also Entry.position
    @Test
    public void swapList(){

        //dummy test data
        String[] stringData = {"0","1","2","3","4","5"};

        //lists assigned to tasks in stringData
        String[] tests = {"0", "1", "0", "1", "0", "1"};

        //ids being swapped
        int[][][] swaps = {{{0,2}},{{0,4}}, {{2,4}},{{2,0}},{{4,0}}, {{4,2}},
                {{0,2},{2,4}},{{0,2},{2,4},{0,4}},{{0,2},{2,4},{0,4},{2,4}}
        };

        //expected results in list (which is generated dynamically)
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

        //expected results in Data.entries
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

        //instantiate test class
        TestClass test = new TestClass(appContext, stringData, tests);

        for (int i = 0; i < swaps.length; i++){

            //set test data to initial values
            test.set();

            Data data = test.getData();

            //swap items
            for (int j = 0; j < swaps[i].length; j++)
                data.swapList(swaps[i][j][0],swaps[i][j][1]);

            //assert results
            for (Entry e : data.getEntries()) {
                assert (e.getListPosition() == resultsListPosition[i][e.getId()]);
                assert (e.getPosition() == resultsPosition[i][e.getId()]);
            }
        }


    }

    //tests if the position of tasks in Data.entries is returned correctly
    @Test
    public void getPosition(){

        //dummy test data
        String[] taskStrings = {"0","1","2","3"};

        //instantiate test class
        TestClass test = new TestClass(appContext, taskStrings);
        //set test data to initial values
        test.set();

        //assert results
        for (int i = 0; i < taskStrings.length; i++)
            assert(test.getData().getPosition(i)==i);

    }

    //tests if task is set properly
    @Test
    public void setTask(){

        //dummy test data
        String[] stringData = {"0","1","2"};

        //instantiate test class
        TestClass test = new TestClass(appContext, stringData);
        //set test data to initial values
        test.set();
        Data data = test.getData();

        //set every task to "TEST"
        for (int i = 0; i < stringData.length; i++){
            data.setTask(i, "TEST");
        }

        //assert that every task has been set to "TEST"
        for (Entry e : data.getEntries())
            assert(e.getTask().equals("TEST"));

        //assert that all changes have been made persistent
        for (Entry e : new DbHelperV1(appContext, DATABASE_NAME).loadEntries())
            assert(e.getTask().equals("TEST"));

    }

    //test the function to set Entry.focus
    //first sets all tasks focus to true and asserts, than to false and asserts
    @Test
    public void setFocus(){

        //dummy tasks
        String[] stringData = {"0","1","2"};

        //instantiate test class
        TestClass test = new TestClass(appContext, stringData);
        //set test data to initial values
        test.set();
        Data data = test.getData();

        //set all tasks focus to true
        for (int i = 0; i < stringData.length; i++){
            data.setFocus(i, true);
        }

        //assert results
        for (Entry e : data.getEntries())
            assert(e.getFocus());

        //assert results are persistent
        for (Entry e : new DbHelperV1(appContext, DATABASE_NAME).loadEntries())
            assert(e.getFocus());

        //set all tasks focus to false
        for (int i = 0; i < stringData.length; i++){
            data.setFocus(i, false);
        }

        //assert results
        for (Entry e : data.getEntries())
            assert(!e.getFocus());

        //assert results are persistent
        for (Entry e : new DbHelperV1(appContext, DATABASE_NAME).loadEntries())
            assert(!e.getFocus());
    }

    //test the function to set Entry.dropped
    //first sets all tasks focus to true and asserts, than to false and asserts
    @Test
    public void setDropped(){

        //dummy test data
        String[] stringData = {"0","1","2"};

        //instantiate test class
        TestClass test = new TestClass(appContext, stringData);
        //set test data to initial values
        test.set();
        Data data = test.getData();

        //set all tasks dropped to true
        for (int i = 0; i < stringData.length; i++){
            data.setDropped(i, true);
        }

        //assert results
        for (Entry e : data.getEntries())
            assert(e.getDropped());

        //assert results are persistent
        for (Entry e : new DbHelperV1(appContext, DATABASE_NAME).loadEntries())
            assert(e.getDropped());

        //set all tasks dropped to false
        for (int i = 0; i < stringData.length; i++){
            data.setDropped(i, false);
        }

        //assert results
        for (Entry e : data.getEntries())
            assert(!e.getDropped());

        //assert results are persistent
        for (Entry e : new DbHelperV1(appContext, DATABASE_NAME).loadEntries())
            assert(!e.getDropped());
    }

    //tests setting a reminder date
    @Test
    public void editReminderDate(){

        //dummy test tasks
        String[] taskStrings = {"0","1","2","3"};

        //test data = results
        LocalDate[] tests = {null, LocalDate.of(2011,3,11), null, LocalDate.of(2000,1,1)};

        //instantiate test class
        TestClass test = new TestClass(appContext, taskStrings);
        //set test data to initial values
        test.set();
        Data data = test.getData();

        //set respective date for each task
        for (int i = 0; i < taskStrings.length; i++)
            data.editReminderDate(i,tests[i]);

        ArrayList<Entry> entries = new DbHelperV1(appContext, DATABASE_NAME).loadEntries();

        //assert results from save file
        for (int i = 0; i < taskStrings.length; i++)
            assert(entries.get(0).getReminderDate()== tests[entries.get(0).getId()]);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //tests setting recurrence
    @Test
    public void editRecurrence(){

        //dummy test tasks
        String[] taskStrings = {"0","1","2","3"};

        //test data
        String[] tests = {"w2", "d999", "y88", "m3"};

        //instantiate test class
        TestClass test = new TestClass(appContext, taskStrings);
        //set test data to initial values
        test.set();
        Data data = test.getData();

        //set recurrence for each task
        for (int i = 0; i < taskStrings.length; i++)
            data.editRecurrence(i,tests[i]);

        ArrayList<Entry> entries = new DbHelperV1(appContext, DATABASE_NAME).loadEntries();

        //assert results from save file
        for (int i = 0; i < taskStrings.length; i++)
            assert(entries.get(0).getRecurrence().equals(tests[entries.get(0).getId()]));

        //set all tasks recurrence to null
        for (int i = 0; i < tests.length; i++)
            data.editRecurrence(i,null);

        entries = new DbHelperV1(appContext, DATABASE_NAME).loadEntries();

        //assert results from save file
        for (Entry e : entries)
            assert(e.getRecurrence()==null);

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //tests editing list names is implemented correctly and list positions are assigned correctly
    @Test
    public void editList(){

        //dummy task names
        String[] stringData = {"0","1","2","3","4","5"};

        //lists, tasks are assigned to
        String[] tests = {"0", "1", "0", "1", "0", "1"};

        //expected lists name for every task
        String[] resultsList = {"0", "1", "0", "1", "0", "1"};

        //expected position in respective list for every task
        int[] resultsPosition = {0,0,1,1,2,2};

        //instantiate test class
        TestClass test = new TestClass(appContext, stringData, tests);
        //set test data to initial values
        test.set();
        Data data = test.getData();

        //load entries from save file to check whether changes were made persistent
        ArrayList<Entry> entries = new DbHelperV1(appContext,DATABASE_NAME).loadEntries();

        //assert results
        for (int i = 0; i < entries.size(); i++) {
            assert(entries.get(i).getList().equals(resultsList[i]));
            assert(entries.get(i).getListPosition() == resultsPosition[i]);
        }

        //assert lists are returned by data.getLists()
        for (String s : tests) {
            assert (data.getLists().contains(s));
            assert(new DbHelperV1(appContext,DATABASE_NAME).loadLists().containsKey(s));
        }

        /*
        *   remove list of first task: originally the list contains 0,2,4
        *   the list positions are:
        *   0:0
        *   2:1
        *   4:2
        *   if the first item is removed, the positions are:
        *   2:0
        *   4:1
        *   and so on.
        */
        data.editList(0,null);

        //assert list positions are correct
        assert(data.getEntries().get(2).getListPosition() == 0);
        assert(data.getEntries().get(4).getListPosition() == 1);

        //remove list of second task
        data.editList(2,null);

        //assert list position of item in same list changed
        assert(data.getEntries().get(4).getListPosition() == 0);

        //remove list of fourth task
        data.editList(4,null);

        //assert that list was removed (2 lists are added, for No List and ALL TASKS
        assert(data.getLists().size() == 3);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //test function for editing list color
    @Test
    public void editListColor(){

        DbHelperV1 db = new DbHelperV1(appContext,DATABASE_NAME);

        //dummy lists and colors
        String[] lists = {"0", "1", "2"};
        String[] initialColors = {"WHITE", "RED", "BLUE"};

        //tests and expected results
        String[] results = {"BLUE", "WHITE", "RED"};

        //add lists with initial color
        for (int i = 0; i < lists.length; i++)
            db.addList(lists[i],initialColors[i]);

        Data data = new Data(appContext,DATABASE_NAME);

        //set test colors in lists
        for (int i = 0; i < lists.length;i++)
            data.editListColor(lists[i],results[i]);

        //assert results
        for (int i = 0; i < lists.length; i++)
            assert(data.getListColor(lists[i]).equals(results[i]));

        appContext.deleteDatabase(DATABASE_NAME);


    }

    //test getting ListColor
    @Test
    public void getListColor(){

        DbHelperV1 db = new DbHelperV1(appContext,DATABASE_NAME);

        //dummy lists
        String[] lists = {"0", "1", "2"};

        //tests and results
        String[] colors = {"WHITE", "RED", "BLUE"};

        //assigns lists with colors
        for (int i = 0; i < lists.length; i++)
            db.addList(lists[i],colors[i]);

        Data data = new Data(appContext,DATABASE_NAME);

        //assert results
        for (int i = 0; i < lists.length; i++)
            assert(data.getListColor(lists[i]).equals(colors[i]));

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //TODO test for incrementListPositionCount

    /*  tests whether positions of tasks in lists are decremented properly
    *   the principle is similar to that of the overall positions
    *   i.e. given a list with task:list position:
    *
    *   0:0
    *   1:1
    *   2:2
    *
    *   if task 1 was removed, the list should look like this:
    *
    *   0:0
    *   2:1
    */

    @Test
    public void decrementListPositionCount(){

        //dummy tasks
        String[] tasks = {"0","1","2", "3", "4", "5"};

        //lists being assigned according to tasks above
        String[] lists = {"0","0", "0", "1", "1", "1"};

        //task ids to be removed for testing purposes
        int[][] tests = {{2},{1},{0},
                {2,1},{1,0},{2,0},
                {1,2},{0,1},{0,2},
                {2,1,0}, {1,2,0}, {0,1,2}
        };

        //expected results
        int[][] results = {{0, 1, -1, 0, 1, 2},{0, -1, 1, 0, 1, 2},{-1, 0, 1, 0, 1, 2},
                {0, -1, -1, 0, 1, 2},{-1, -1, 0, 0, 1, 2},{-1, 0, -1, 0, 1, 2},
                {0, -1, -1, 0, 1, 2},{-1, -1, 0, 0, 1, 2},{-1, 0, -1, 0, 1, 2},
                {-1, -1, -1, 0, 1, 2},{-1, -1, -1, 0, 1, 2},{-1, -1, -1, 0, 1, 2}

        };

        //instantiate test class
        TestClass test = new TestClass(appContext, tasks,lists);

        //iterate through tests
        for (int i = 0; i < tests.length; i++){

            //set test data to initial values
            test.set();
            Data data = test.getData();

            //set list for test tasks to null to remove it
            for (int j = 0; j < tests[i].length; j++)
                data.editList(tests[i][j],null);

            //assert results
            for (Entry e : data.getEntries())
                assert (e.getListPosition() == results[i][e.getId()]);


        }

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //check whether lists are returned as String[] correctly
    @Test
    public void returnListAsArray(){

        //dummy tasks
        String[] tasks = {"0","1","2","3"};

        //dummy lists
        String[] lists = {"0","1","2","3"};

        //instantiate test class
        TestClass test = new TestClass(appContext,tasks,lists);

        //set test data to initial values
        test.set();
        Data data = test.getData();

        String[] returnedLists = data.returnListsAsArray();

        //check if returnedLists is the size of lists plus items "No list" and "ALL TASKS"
        assert(returnedLists.length == lists.length+2);

        //assert whether returnedLists contains every item in lists
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

    //tests getting lists as ArrayList<String>
    @Test
    public void getLists(){

        //dummy tasks
        String[] tasks = {"0","1","2","3"};

        //lists -> expected to be in ArrayList
        String[] lists = {"0","1","2","3"};

        //instantiate test class
        TestClass test = new TestClass(appContext,tasks,lists);

        //set data to initial values
        test.set();
        Data data = test.getData();

        //get lists
        ArrayList<String> returnedLists = data.getLists();

        //assert size is lists + "No List" + "ALL TASKS"
        assert(returnedLists.size() == lists.length+2);

        //assert all lists are contained in ArrayList
        for (String s : lists) {
            assert(returnedLists.contains(s));
        }

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //test function to get tasks that are due today
    @Test
    public void getTasksToPick(){

        //dummy tasks
        String[] tasks = {"0","1","2","3"};

        //test dates: only tasks that have a reminder date older than or equal to today
        //should be returned
        LocalDate[] dates = {LocalDate.now().minusDays(1),LocalDate.now().plusDays(1),LocalDate.now(),LocalDate.now().plusDays(1)};

        //expected results
        String[] results = {"0","2"};

        //instantiate test class and set test data
        TestClass test = new TestClass(appContext, tasks);
        test.set();
        Data data = test.getData();

        //tasks that are due today
        ArrayList<Entry> tasksToPick = data.getTasksToPick();

        //assert that all tasks are returned, since no reminder date has been set
        for (int i=0; i< tasksToPick.size();i++)
            assert(tasksToPick.get(i).getTask().equals(tasks[i]));

        //set test dates
        for (int i = 0; i < tasks.length; i++)
            data.editReminderDate(Integer.parseInt(tasks[i]), dates[i]);

        //get tasks that are due today according to new reminder dates
        tasksToPick = data.getTasksToPick();

        //assert that returned dates are equal to expected results
        for (int i = 0; i < tasksToPick.size(); i++)
            assert(tasksToPick.get(i).getTask().equals(results[i]));


        appContext.deleteDatabase(DATABASE_NAME);

    }

    //test function to get all tasks assigned to a certain list
    @Test
    public void getList(){

        //dummy tasks
        String[] tasks = {"0","1","2","3","4","5"};

        //lists assigned according to tasks above
        String[] lists = {"0","0","0","1","1","1"};

        //instantiate test class and set data
        TestClass test = new TestClass(appContext,tasks,lists);
        test.set();
        Data data = test.getData();

        //get tasks in first list
        ArrayList<Entry> returnedList = data.getList("0");

        //assert tasks in list correspond to assigned tasks: this is achieved via comparing
        // the task name to the position
        for (int i = 0; i < returnedList.size(); i++){
            assert(returnedList.get(i).getTask().equals(String.valueOf(i)));
        }

        //get tasks in second list
        returnedList = data.getList("1");

        //assert tasks in list correspond to assigned tasks: this is achieved via comparing
        // the task name to the position
        for (int i = 0; i < returnedList.size(); i++){
            assert(returnedList.get(i).getTask().equals(String.valueOf(i+3)));
        }

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //tests getting tasks that have been freshly dropped
    @Test
    public void getDropped(){

        //dummy tasks
        String[] tasks = {"0","1","2"};

        //instantiate test class and set initial data
        TestClass test = new TestClass(appContext, tasks);
        test.set();
        Data data = test.getData();

        //get freshly dropped tasks
        ArrayList<Entry> dropped = data.getDropped();

        //assert all new tasks have been returned
        for (int i = 0; i < tasks.length; i++)
            assert(dropped.get(i).getTask().equals(tasks[i]));


        //starting from the back one by one set all tasks dropped status to false
        //then check whether it is returned anymore
        for (int i = 0; i < tasks.length; i++) {

            //set last item in list to dropped == false
            data.getEntries().get(tasks.length-1-i).setDropped(false);

            //get dropped
            dropped = data.getDropped();

            //assert dropped has shrunken
            assert(dropped.size()==tasks.length-1-i);

            //assert right tasks are in dropped
            for (int j = 0; j < dropped.size();j++)
                assert(dropped.get(j).getTask().equals(tasks[j]));
        }

        //make tasks empty array
        tasks = new String[0];

        //instantiate test class with empty data
        test = new TestClass(appContext, tasks);
        test.set();
        data = test.getData();

        //get dropped
        dropped = data.getDropped();

        //assert dropped is empty
        assert(dropped.size()==0);

        appContext.deleteDatabase(DATABASE_NAME);
    }


    //test function to get all tasks where focus == true
    //however, if a task focus attribute is false but the task is recurring and the reminder date fits,
    //it should also be shown in FOCUS
    //if the task has been removed from FOCUS and therefore is in the ArrayList
    //recurringButRemovedFromToday in Data the task should not be returned
    @Test
    public void getFocus(){

        //dummy tasks
        String[] tasks = {"0","1","2","3"};

        //focus assigned to tasks above
        boolean[] focus = {true,false,false,false};

        //expected results
        String[] results = {"0","2"};

        //instantiate new test class and set to initial values
        TestClass test = new TestClass(appContext, tasks);
        test.set();

        Data data = test.getData();

        //set task 2 to recurring, so it should be returned
        data.editRecurrence(2, "w2");
        //set task 3 ro recurring, but also add it to recurringButRemovedFromToday
        //so it should not be returned
        data.editRecurrence(3, "w2");
        data.addToRecurringButRemoved(3);

        //set focus values for all tasks
        for (int i = 0; i < tasks.length; i++)
            data.setFocus(i,focus[i]);

        //get data
        ArrayList<Entry> focused = data.getFocus();

        //assert results
        for (int i = 0; i < focused.size(); i++)
            assert(focused.get(i).getTask().equals(results[i]));

        //remove task 3 from recurringButRemovedFromToday so it should be returned
        data.removeFromRecurringButRemoved(3);

        //get data
        focused = data.getFocus();

        //copy old results into new Array and add task 3 as being expected to be returned
        String[] resultsNew = new String[results.length+1];
        System.arraycopy(results, 0, resultsNew, 0, results.length);
        resultsNew[results.length] = "3";

        //assert results
        for (int i = 0; i < focused.size(); i++)
            assert(focused.get(i).getTask().equals(resultsNew[i]));


        appContext.deleteDatabase(DATABASE_NAME);

    }

    //tests function to get all tasks without a list assigned
    @Test
    public void getNoList(){

        //dummy tasks
        String[] tasks = {"0","1","2","3"};

        //lists assigned to tasks above
        String[] lists = {"0","1",null,null};

        //expected results
        String[] results = {"2","3"};

        //instantiate test class and set to initial values
        TestClass test = new TestClass(appContext,tasks, lists);
        test.set();
        Data data = test.getData();

        //get tasks without list
        ArrayList<Entry> noList = data.getNoList();

        //assert results
        for (int i=0;i<noList.size();i++)
            assert(noList.get(i).getTask().equals(results[i]));

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //tests whether entries are returned ordered by date
    @Test
    public void getEntriesOrderedByDate(){

        //dummy tasks
        String[] tasks = {"0","1","2"};

        //tests: each task is assigned a date on each successive run
        LocalDate[][] tests = {{null,LocalDate.of(2022,12,31),LocalDate.of(2023,01,10)},
                {LocalDate.of(2022,12,31),LocalDate.of(2023,01,10),null},
                {LocalDate.of(2023,01,10),LocalDate.of(2022,12,31),null},
                {null,LocalDate.of(2023,01,10),LocalDate.of(2022,12,31)},
                {LocalDate.of(2022,12,31),null,LocalDate.of(2023,01,10)},
                {LocalDate.of(2023,01,10),null,LocalDate.of(2022,12,31)}
                            };

        //expected results
        String[][] results = {{"0","1","2"},{"2","0","1"},{"2","1","0"},
                                {"0","2","1"},{"1","0","2"},{"1","2","0"}
                                };

        //instantiate test class
        TestClass test = new TestClass(appContext, tasks);

        //run tests
        for (int i = 0; i < tests.length; i++) {

            //set test data for run
            test.set();
            Data data = test.getData();

            //set reminder dates
            for (int j = 0; j < tests[i].length; j++)
                data.editReminderDate(j, tests[i][j]);

            //get entries
            ArrayList<Entry> entries = data.getEntriesOrderedByDate();

            //assert results
            for (int j = 0; j < entries.size(); j++)
                assert(entries.get(j).getTask().equals(results[i][j]));

        }

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //tests setting recurring attribute of tasks.
    @Test
    public void setRecurring(){

        //different dates representing today.
        LocalDate[] today = {LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,01,31),
                LocalDate.of(2023,02,28),
                LocalDate.of(2024,02,28),
                LocalDate.of(2023,12,31),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,30),
                LocalDate.of(2023,12,31),
                LocalDate.of(2023,01,01),
                LocalDate.of(2023,01,01),
                LocalDate.of(2023,12,01),
                LocalDate.of(2023,01,01),
                LocalDate.of(2023,01,01)
        };

        //intervals in which tasks reoccur. the letter represents days/weeks/months/years
        //i.e.: w2 -> reoccurs every two weeks, m3 -> reoccurs every three months, d1 -> reoccurs every day
        String[] intervals = {"d1","d2","d3","d4","d5","d6","d7",
                        "d1","d1","d1","d1",
                        "w1","w2","w1","w1","w1",
                        "m1","m2","m1",
                        "y1","y2"
        };

        //reminder dates for tasks -> the interval above should be added to the initial reminder dates
        //unless reminder date + interval <= today -> in that case the interval is added x times so
        // that date + x * interval > today
        LocalDate[] tests ={LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15), //adding one to seven days
                LocalDate.of(2023,1,31), //adding one day and incrementing month
                LocalDate.of(2023,2,28), //adding one day and incrementing month in February
                LocalDate.of(2024,2,28), //adding one day in leap year therefore not incrementing month
                LocalDate.of(2023,12,31), //adding one day and incrementing month + year
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15), //adding one and two weeks
                LocalDate.of(2023,10,8), //adding one week and today is 2023-10-15 -> another week is added
                LocalDate.of(2023,10,25), //adding one week and month is incremented too
                LocalDate.of(2023,12,4), //adding one week and today is 2023-12-31 -> four weeks are added and month/year increment
                LocalDate.of(2023,1,1),
                LocalDate.of(2023,1,1), //adding one and two months
                LocalDate.of(2023,1,1), //adding one month but today is 2023-12-01 -> year is incremented too
                LocalDate.of(2023,1,1),
                LocalDate.of(2022,1,1) //adding one and two years
        };

        //expected results
        LocalDate[] results = {
                LocalDate.of(2023,10,16),
                LocalDate.of(2023,10,17),
                LocalDate.of(2023,10,18),
                LocalDate.of(2023,10,19),
                LocalDate.of(2023,10,20),
                LocalDate.of(2023,10,21),
                LocalDate.of(2023,10,22),
                LocalDate.of(2023,02,01),
                LocalDate.of(2023,03,01),
                LocalDate.of(2024,02,29),
                LocalDate.of(2024,01,01),
                LocalDate.of(2023,10,22),
                LocalDate.of(2023,10,29),
                LocalDate.of(2023,10,22),
                LocalDate.of(2023,11,01),
                LocalDate.of(2024,01,01),
                LocalDate.of(2023,02,01),
                LocalDate.of(2023,03,01),
                LocalDate.of(2024,01,01),
                LocalDate.of(2024,1,1),
                LocalDate.of(2024,01,01)
        };


        Data data = new Data(appContext, DATABASE_NAME);

        //add single test task
        data.add("Test");

        //run tests
        for (int i = 0; i < tests.length; i++){

            //set reminder date
            data.editReminderDate(0,tests[i]);

            //set recurrence interval
            data.getEntries().get(0).setRecurrence(intervals[i]);

            //increment tasks recurrence
            data.setRecurring(0,today[i]);

            //assert results
            assert(data.getEntries().get(0).getReminderDate().equals(results[i]));

        }

        appContext.deleteDatabase(DATABASE_NAME);

    }


    @After
    public void cleanUp(){

        appContext.deleteDatabase(DATABASE_NAME);
    }

}
