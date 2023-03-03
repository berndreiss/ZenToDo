package com.bdreiss.zentodo;

import android.content.Context;
import android.util.Log;

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

    @Test
    public void loadEntries(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        for (int i = 0; i < 3; i++) {

            db.addEntry(new Entry(i, i, "NEW TASK"));

            Log.d("SIZE ENTRIES: ", String.valueOf(db.loadEntries().size()));
//            assert (db.loadEntries().size() == i+1);
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

    @Test
    public void removeEntry(){
        DbHelper db = new DbHelper(appContext,DATABASE_NAME);

        db.addEntry(new Entry(0,0,"NEW TASK"));

        db.removeEntry(0);

        assert(db.loadEntries().size() == 0);

        Entry entry0 = new Entry(0,0,"NEW TASK");
        db.addEntry(entry0);

        Entry entry1 = new Entry(1,1,"NEW TASK");
        db.addEntry(entry1);

        db.removeEntry(0);

        assert(db.loadEntries().size() == 1);
        assert(!db.loadEntries().contains(entry0));

        db.addEntry(entry0);
        db.removeEntry(0);

        assert(db.loadEntries().size() == 1);
        assert(!db.loadEntries().contains(entry0));

        db.addEntry(entry0);
        db.addEntry(new Entry(2,2, "NEW TASK"));
        db.removeEntry(0);

        assert(db.loadEntries().size() == 2);
        assert(!db.loadEntries().contains(entry0));

        db.removeEntry(1);

        assert(db.loadEntries().size() == 1);
        assert(!db.loadEntries().contains(entry1));

        db.removeEntry(2);

        assert(db.loadEntries().size() == 0);

        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void loadLists(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        assert(db.loadLists().isEmpty());

        db.addEntry(new Entry(0,0,"NEW TASK"));

        assert(db.loadLists().isEmpty());

        addTestDataList(db, "TEST");

        assert (db.loadLists().containsKey("TEST"));
        assert (Objects.equals(Objects.requireNonNull(db.loadLists().get("TEST")).getColor(), "WHITE"));

        appContext.deleteDatabase(DATABASE_NAME);

    }

    @Test
    public void addList(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        db.addList("|", "WHITE");

        assert(db.loadLists().containsKey("|"));


        for (String stringTestDatum : stringTestData) db.addList(stringTestDatum, "WHITE");

        for (String stringTestDatum : stringTestData) {
            assert (db.loadLists().containsKey(stringTestDatum));
            assert (Objects.equals(Objects.requireNonNull(db.loadLists().get(stringTestDatum)).getColor(), "WHITE"));
        }
        appContext.deleteDatabase(DATABASE_NAME);

    }

    @Test
    public void removeList(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        addTestDataList(db, "TEST");

        db.removeList("TEST");

        assert(db.loadLists().isEmpty());

        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void updateEntry(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        db.addEntry(new Entry(0,0,"NEW TASK"));


        for (String stringTestDatum : stringTestData) {


            db.updateEntry(COLUMNS_ENTRIES_V1.TASK_COL, 0, stringTestDatum);

            assert (db.loadEntries().get(0).getTask().equals(stringTestDatum));


            db.updateEntry(COLUMNS_ENTRIES_V1.LIST_COL, 0, stringTestDatum);

            assert (db.loadEntries().get(0).getList().equals(stringTestDatum));


            db.updateEntry(COLUMNS_ENTRIES_V1.RECURRENCE_COL, 0, stringTestDatum);

            assert (db.loadEntries().get(0).getRecurrence().equals(stringTestDatum));

        }

        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_COL, 0, null);

        assert (db.loadEntries().get(0).getList() == null);

        db.updateEntry(COLUMNS_ENTRIES_V1.RECURRENCE_COL, 0, null);

        assert (db.loadEntries().get(0).getRecurrence() == null);

        db.updateEntry(COLUMNS_ENTRIES_V1.FOCUS_COL, 0, false);

        assert(!db.loadEntries().get(0).getFocus());

        db.updateEntry(COLUMNS_ENTRIES_V1.FOCUS_COL, 0, true);

        assert(db.loadEntries().get(0).getFocus());

        db.updateEntry(COLUMNS_ENTRIES_V1.DROPPED_COL, 0, false);

        assert(!db.loadEntries().get(0).getDropped());

        db.updateEntry(COLUMNS_ENTRIES_V1.DROPPED_COL, 0, true);

        assert(db.loadEntries().get(0).getDropped());

        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_POSITION_COL, 0, 0);

        assert(db.loadEntries().get(0).getListPosition() == 0);

        db.updateEntry(COLUMNS_ENTRIES_V1.REMINDER_DATE_COL, 0, 2000);

        assert(db.loadEntries().get(0).getReminderDate() == 2000);

        db.updateEntry(COLUMNS_ENTRIES_V1.POSITION_COL, 0, 99);

        assert(db.loadEntries().get(0).getPosition() == 99);


        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void updateList(){
        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        for (String stringTestDatum : stringTestData) {
            db.addList(stringTestDatum, "WHITE");

            db.updateList(stringTestDatum, "RED");

            assert (Objects.requireNonNull(db.loadLists().get(stringTestDatum)).getColor().equals("RED"));

        }
        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void swapEntries(){

        class Test{
            final COLUMNS_ENTRIES_V1[] fields = {COLUMNS_ENTRIES_V1.POSITION_COL, COLUMNS_ENTRIES_V1.LIST_POSITION_COL};
            final int[][][] testData = {{{0,1},{0,2}, {1,2}},{{0,1},{0,2}, {1,2}}};
            final int[][][] results = {{{1,0,2},{2,0,1},{2,1,0}},{{1,0,2},{2,0,1},{2,1,0}}};
        }

        Test test = new Test();

        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        for (int i = 0; i < test.results[0][0].length; i++){
            db.addEntry(new Entry(i,i,"NEW TASK"));
            db.updateEntry(COLUMNS_ENTRIES_V1.LIST_POSITION_COL, i,i);

        }


        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_POSITION_COL, 1,1);
        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_POSITION_COL, 2,2);

        for (int i = 0; i < test.fields.length; i++)
            for (int j = 0; j < test.testData[i].length; j++) {
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

    @Test
    public void saveLoadRecurring(){

        DbHelper db = new DbHelper(appContext, DATABASE_NAME);

        int filename = Data.getToday();

        ArrayList<Integer> ids = new ArrayList<>();

        db.saveRecurring(ids);

        assert(db.loadRecurring().size()==0);

        ids.add(0);

        db.saveRecurring(ids);

        assert(db.loadRecurring().get(0)==0);

        ids.add(1);

        ids.add(2);

        db.saveRecurring(ids);

        for (int i=0; i<ids.size();i++)
            assert(db.loadRecurring().get(i)==i);

        ids.remove(1);

        db.saveRecurring(ids);

        assert(db.loadRecurring().get(0)==0);
        assert(db.loadRecurring().get(1)==2);

        appContext.deleteDatabase(DATABASE_NAME);

        new File(appContext.getFilesDir() + "/" + filename).delete();


    }

    @After
    public void cleanUp(){

        appContext.deleteDatabase(DATABASE_NAME);

    }

}