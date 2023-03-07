package com.bdreiss.zentodo;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.dataManipulation.database.DbHelper;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.COLUMNS_ENTRIES_V1;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.COLUMNS_LISTS_V1;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.TABLES_V1;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Tests to ensure database interaction works flawlessly
 *
 *
 */
@RunWith(AndroidJUnit4.class)
public class DbHelper_V1Test {

    private static Context appContext;

    private static final String DATABASE_NAME = "TEST.db";

    //test data including all important special characters in particular "'"
    public static final String[] stringTestData = {"Test", ",", ".", ";", ":", "-", "_", "#",
            "'", " '", "' ", "'A", "A'", "A'A", "'A'A'",
            "*", "~", "+", "`", "´", "?", "\\", "=",
            "}", ")", "]", "(", "[", "{", "/", "&,",
            "%", "$", "§", "\"", "!", "^", "°", "<", ">", "|"
    };

    //add dummy task where all attributes are set to null
    public void addTestDataNull(DbHelper db){
        insertQuery("0, \"NEW TASK\", 0, 1, NULL, -1, 0, NULL, 0", db);
    }

    //add dummy list
    public void addTestDataList(DbHelper db, String string){
        db.getWritableDatabase().execSQL("INSERT INTO " + TABLES_V1.TABLE_LISTS + "(" +
                        COLUMNS_LISTS_V1.LIST_NAME_COL + ", " +
                        COLUMNS_LISTS_V1.LIST_COLOR_COL + ") VALUES ('" +
                        string + "', " +
                        "'WHITE')"
                );
    }

    //takes String argument containing values for the different fields
    public void insertQuery(String data, DbHelper db){
        db.getWritableDatabase().execSQL("INSERT INTO " + TABLES_V1.TABLE_ENTRIES + "(" +
                COLUMNS_ENTRIES_V1.ID_COL + ", " +
                COLUMNS_ENTRIES_V1.TASK_COL + ", " +
                COLUMNS_ENTRIES_V1.FOCUS_COL + ", " +
                COLUMNS_ENTRIES_V1.DROPPED_COL + ", " +
                COLUMNS_ENTRIES_V1.LIST_COL + ", " +
                COLUMNS_ENTRIES_V1.LIST_POSITION_COL + ", " +
                COLUMNS_ENTRIES_V1.REMINDER_DATE_COL + ", " +
                COLUMNS_ENTRIES_V1.RECURRENCE_COL + ", " +
                COLUMNS_ENTRIES_V1.POSITION_COL +
                ") VALUES (" + data + ")"
        );
    }


    @BeforeClass
    public static void setup(){
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }


    //test whether empty database is created
    @Test
    public void createDatabase(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        assert(appContext.getDatabasePath("TEST") != null);
        assert(db.loadEntries().size() == 0);

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //adds new task and checks whether it is loaded correctly
    @Test
    public void loadNewTask() {
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        addTestDataNull(db);

        ArrayList<Entry> entries = db.loadEntries();

        Entry entry = entries.get(0);

        assert(entry.getId() == 0);
        assert(entry.getTask().equals("NEW TASK"));
        assert(!entry.getFocus());
        assert(entry.getDropped());
        assert(entry.getList() == null);
        assert(entry.getListPosition() == -1);
        assert(entry.getReminderDate() == 0);
        assert(entry.getRecurrence() == null);
        assert(entry.getPosition() == 0);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //add entries with test Strings and check whether they are entered into the database correctly
    @Test
    public void addEntry(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        String[] tasks = stringTestData;

        for (int i = 0; i < tasks.length; i++)
            db.addEntry(new Entry(i,i,tasks[i]));

        for (int i = 0; i < tasks.length; i++)
            assert(db.loadEntries().get(i).getTask().equals(tasks[i]));

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //test whether entries are loaded correctly
    @Test
    public void loadEntries(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        for (int i = 0; i < 3; i++) {

            db.addEntry(new Entry(i, i, "NEW TASK"));

            Entry entry = db.loadEntries().get(i);

            assert(entry.getId() == i);
            assert(entry.getTask().equals("NEW TASK"));
            assert(!entry.getFocus());
            assert(entry.getDropped());
            assert(entry.getList() == null);
            assert(entry.getListPosition() == -1);
            assert(entry.getReminderDate() == 0);
            assert(entry.getRecurrence() == null);
            assert(entry.getPosition() == i);
        }
        appContext.deleteDatabase(DATABASE_NAME);

    }

    //test whether entries are removed properly
    @Test
    public void removeEntry(){
        DbHelper db = new DbHelper(appContext,DATABASE_NAME);

        //add and remove entry and assert result
        db.addEntry(new Entry(0,0,"NEW TASK"));
        db.removeEntry(0);
        assert(db.loadEntries().size() == 0);

        //add two entries
        Entry entry0 = new Entry(0,0,"NEW TASK");
        db.addEntry(entry0);
        Entry entry1 = new Entry(1,1,"NEW TASK");
        db.addEntry(entry1);

        //remove the first one
        db.removeEntry(0);

        //assert list size is 1 and list does not contain entry
        assert(db.loadEntries().size() == 1);
        assert(!db.loadEntries().contains(entry0));


        //add first entry and third one
        db.addEntry(entry0);
        db.addEntry(new Entry(2,2, "NEW TASK"));

        //remove first one again
        db.removeEntry(0);

        //assert list size is 2 and does not contain removed entry
        assert(db.loadEntries().size() == 2);
        assert(!db.loadEntries().contains(entry0));

        //remove second entry
        db.removeEntry(1);

        //assert list size is 1 and list does not contain second entry
        assert(db.loadEntries().size() == 1);
        assert(!db.loadEntries().contains(entry1));

        //remove third entry
        db.removeEntry(2);

        //assert list is empty
        assert(db.loadEntries().size() == 0);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //test whether lists are loaded properly
    @Test
    public void loadLists(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        //assert not list was created when database was instantiated
        assert(db.loadLists().isEmpty());

        //add entry to check whether list is being created automatically
        db.addEntry(new Entry(0,0,"NEW TASK"));

        //assert no list has been created
        assert(db.loadLists().isEmpty());

        //add dummy test list
        addTestDataList(db, "TEST");

        //assert list is loaded and contains right default color
        assert (db.loadLists().containsKey("TEST"));
        assert (Objects.equals(Objects.requireNonNull(db.loadLists().get("TEST")).getColor(), "WHITE"));

        appContext.deleteDatabase(DATABASE_NAME);

    }

    //tests whether lists are added correctly
    @Test
    public void addList(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        //add dummy list with default color white
        db.addList("|", "WHITE");

        //assert list is returned
        assert(db.loadLists().containsKey("|"));

        //add a list for every String in stringTestData containing most widely used special characters
        for (String stringTestDate : stringTestData) db.addList(stringTestDate, "WHITE");

        //assert lists have been added with color attribute set to "WHITE"
        for (String stringTestDate : stringTestData) {
            assert (db.loadLists().containsKey(stringTestDate));
            assert (Objects.equals(Objects.requireNonNull(db.loadLists().get(stringTestDate)).getColor(), "WHITE"));
        }
        appContext.deleteDatabase(DATABASE_NAME);

    }

    //tests whether lists are removed properly
    @Test
    public void removeList(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        //add dummy list
        addTestDataList(db, "TEST");

        //remove dummy list
        db.removeList("TEST");

        //assert no lists are loaded
        assert(db.loadLists().isEmpty());

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //tests whether entries are updated properly in database
    @Test
    public void updateEntry(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        //add dummy task
        db.addEntry(new Entry(0,0,"NEW TASK"));

        //update that tasks name, list and recurrence attributes for all Strings in stringTestData
        for (String stringTestDate : stringTestData) {

            //change task name and assert
            db.updateEntry(COLUMNS_ENTRIES_V1.TASK_COL, 0, stringTestDate);
            assert (db.loadEntries().get(0).getTask().equals(stringTestDate));

            //change list name and assert
            db.updateEntry(COLUMNS_ENTRIES_V1.LIST_COL, 0, stringTestDate);
            assert (db.loadEntries().get(0).getList().equals(stringTestDate));

            //change recurrence and assert
            db.updateEntry(COLUMNS_ENTRIES_V1.RECURRENCE_COL, 0, stringTestDate);
            assert (db.loadEntries().get(0).getRecurrence().equals(stringTestDate));

        }

        //set list to null and assert
        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_COL, 0, null);
        assert (db.loadEntries().get(0).getList() == null);

        //set recurrence to null and assert
        db.updateEntry(COLUMNS_ENTRIES_V1.RECURRENCE_COL, 0, null);
        assert (db.loadEntries().get(0).getRecurrence() == null);

        //set focus to false/true and assert
        db.updateEntry(COLUMNS_ENTRIES_V1.FOCUS_COL, 0, false);
        assert(!db.loadEntries().get(0).getFocus());
        db.updateEntry(COLUMNS_ENTRIES_V1.FOCUS_COL, 0, true);
        assert(db.loadEntries().get(0).getFocus());

        //set dropped to false/true and assert
        db.updateEntry(COLUMNS_ENTRIES_V1.DROPPED_COL, 0, false);
        assert(!db.loadEntries().get(0).getDropped());
        db.updateEntry(COLUMNS_ENTRIES_V1.DROPPED_COL, 0, true);
        assert(db.loadEntries().get(0).getDropped());

        //set position to 99/0 and assert
        db.updateEntry(COLUMNS_ENTRIES_V1.POSITION_COL, 0, 99);
        assert(db.loadEntries().get(0).getPosition() == 99);
        db.updateEntry(COLUMNS_ENTRIES_V1.POSITION_COL, 0, 0);
        assert(db.loadEntries().get(0).getPosition() == 0);

        //set list position to 99/0 and assert
        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_POSITION_COL, 0, 99);
        assert(db.loadEntries().get(0).getListPosition() == 99);
        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_POSITION_COL, 0, 0);
        assert(db.loadEntries().get(0).getListPosition() == 0);

        //set reminder date to 2000/0 and assert
        db.updateEntry(COLUMNS_ENTRIES_V1.REMINDER_DATE_COL, 0, 2000);
        assert(db.loadEntries().get(0).getReminderDate() == 2000);
        db.updateEntry(COLUMNS_ENTRIES_V1.REMINDER_DATE_COL, 0, 0);
        assert(db.loadEntries().get(0).getReminderDate() == 0);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //tests whether lists color is updated correctly
    @Test
    public void updateList(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        //add lists for all test cases in stringTestData
        for (String stringTestDate : stringTestData) {
            db.addList(stringTestDate, "WHITE");

            //update color and assert result
            db.updateList(stringTestDate, "RED");
            assert (Objects.requireNonNull(db.loadLists().get(stringTestDate)).getColor().equals("RED"));

        }
        appContext.deleteDatabase(DATABASE_NAME);
    }

    //tests function to swap entries in TABLE_ENTRIES:
    //takes the ids of two tasks as parameters and swaps the values of the positions/list positions of the two tasks
    @Test
    public void swapEntries(){

        //class containing test runs for columns, test data and expected results
        class Test{
            //columns to be tested
            final COLUMNS_ENTRIES_V1[] fields = {COLUMNS_ENTRIES_V1.POSITION_COL, COLUMNS_ENTRIES_V1.LIST_POSITION_COL};
            //test data representing swaps of tasks represented by their ids
            final int[][][] testData = {{{0,1},{0,2}, {1,2}},{{0,1},{0,2}, {1,2}}};
            //expected results represented by tasks ids
            final int[][][] results = {{{1,0,2},{2,0,1},{2,1,0}},{{1,0,2},{2,0,1},{2,1,0}}};
        }

        //instantiate new Test
        Test test = new Test();

        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        //add dummy tasks and set list
        for (int i = 0; i < test.results[0][0].length; i++){
            db.addEntry(new Entry(i,i,"NEW TASK"));
            db.updateEntry(COLUMNS_ENTRIES_V1.LIST_POSITION_COL, i,i);

        }

        //run tests for column
        for (int i = 0; i < test.fields.length; i++)

            //run test cases
            for (int j = 0; j < test.testData[i].length; j++) {

                //do swaps and assert results according to column
                db.swapEntries(test.fields[i], test.testData[i][j][0], test.testData[i][j][1]);
                switch (test.fields[i]){
                    case POSITION_COL:
                        for (int k = 0; k < test.results[0][0].length; k++ )
                            assert(db.loadEntries().get(k).getPosition() == test.results[i][j][k]);
                        break;
                    case LIST_POSITION_COL:
                        for (int k = 0; k < test.results[0][0].length; k++ )
                            assert(db.loadEntries().get(k).getListPosition() == test.results[i][j][k]);
                        break;

                }

            }

        appContext.deleteDatabase(DATABASE_NAME);
    }

    //tests whether ArrayList<Integer> recurringButRemovedFromToday (see documentation in FocusTaskListAdapter.java, Data.java, DbHelper.java)
    //is saved/loaded correctly
    @Test
    public void saveLoadRecurring(){

        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        //get date of today
        int filename = Data.getToday();

        ArrayList<Integer> ids = new ArrayList<>();

        //save empty ArrayList and assert load returns empty
        db.saveRecurring(ids);
        assert(db.loadRecurring().size()==0);

        //add first element, save and assert results upon load
        ids.add(0);
        db.saveRecurring(ids);
        assert(db.loadRecurring().get(0)==0);

        //add second and third element, save and assert results upon load
        ids.add(1);
        ids.add(2);
        db.saveRecurring(ids);
        for (int i=0; i<ids.size();i++)
            assert(db.loadRecurring().get(i)==i);

        //remove second element, save and assert results upon load
        ids.remove(1);
        db.saveRecurring(ids);
        assert(db.loadRecurring().get(0)==0);
        assert(db.loadRecurring().get(1)==2);

        //clean up
        appContext.deleteDatabase(DATABASE_NAME);
        new File(appContext.getFilesDir() + "/" + filename).delete();


    }

    @After
    public void cleanUp(){

        appContext.deleteDatabase(DATABASE_NAME);

    }

}