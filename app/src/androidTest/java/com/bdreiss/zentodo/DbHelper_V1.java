package com.bdreiss.zentodo;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.dataManipulation.database.DbHelper;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.COLUMNS_ENTRIES_V1;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.COLUMNS_LISTS_V1;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.TABLES_V1;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DbHelper_V1 {

    private static Context appContext;

    public void addTestDataNull(DbHelper db){
        insertQuery("0, \"NEW TASK\", 0, 1, NULL, -1, 0, NULL, 0", db);
//        insertQuery("1, \"NEW TASK\", 0, 1, \"null\", -1, 0, \"null\", 1");
//        insertQuery("2, \"NEW TASK\", 0, 1, \"null\", -1, 0, NULL, 2");
//        insertQuery("3, \"NEW TASK\", 0, 1, NULL, -1, 0, \"null\", 3");
    }

    public void addTestDataList(DbHelper db){
        db.getWritableDatabase().execSQL("INSERT INTO " + TABLES_V1.TABLE_LISTS + "(" +
                        COLUMNS_LISTS_V1.LIST_NAME_COL + ", " +
                        COLUMNS_LISTS_V1.LIST_COLOR_COL + ") VALUES (" +
                        "\"TEST\"" + ", " +
                        "\"WHITE\")"
                );
    }

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


    @Test
    public void createDatabase(){
        DbHelper db = new DbHelper(appContext, "TEST.db");

        assert(appContext.getDatabasePath("TEST") != null);
        assert(db.loadEntries().size() == 0);

        appContext.deleteDatabase("TEST.db");

    }

    @Test
    public void loadNewTask() {
        DbHelper db = new DbHelper(appContext, "TEST.db");

        // Context of the app under test.
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

        appContext.deleteDatabase("TEST.db");
        /*
        entry = entries.get(1);

        assert(entry.getId() == 1);
        assert(entry.getTask().equals("NEW TASK"));
        assert(!entry.getFocus());
        assert(entry.getDropped());
        assert(entry.getList() == null);
        assert(entry.getListPosition() == -1);
        assert(entry.getReminderDate() == 0);
        assert(entry.getRecurrence() == null);
        assert(entry.getPosition() == 1);

        entry = entries.get(2);

        assert(entry.getId() == 2);
        assert(entry.getTask().equals("NEW TASK"));
        assert(!entry.getFocus());
        assert(entry.getDropped());
        assert(entry.getList() == null);
        assert(entry.getListPosition() == -1);
        assert(entry.getReminderDate() == 0);
        assert(entry.getRecurrence() == null);
        assert(entry.getPosition() == 2);

        entry = entries.get(3);

        assert(entry.getId() == 3);
        assert(entry.getTask().equals("NEW TASK"));
        assert(!entry.getFocus());
        assert(entry.getDropped());
        assert(entry.getList() == null);
        assert(entry.getListPosition() == -1);
        assert(entry.getReminderDate() == 0);
        assert(entry.getRecurrence() == null);
        assert(entry.getPosition() == 3);*/

    }

    @Test
    public void loadEntries(){
        DbHelper db = new DbHelper(appContext, "TEST.db");

        db.addEntry(new Entry(0,0,"NEW TASK"));

        assert(db.loadEntries().size() == 1);

        Entry entry = db.loadEntries().get(0);

        assert(entry.getId() == 0);
        assert(entry.getTask().equals("NEW TASK"));
        assert(!entry.getFocus());
        assert(entry.getDropped());
        assert(entry.getList() == null);
        assert(entry.getListPosition() == -1);
        assert(entry.getReminderDate() == 0);
        assert(entry.getRecurrence() == null);
        assert(entry.getPosition() == 0);

        db.addEntry(new Entry(1,1,"NEW TASK"));

        assert(db.loadEntries().size() == 2);

        entry = db.loadEntries().get(1);

        assert(entry.getId() == 1);
        assert(entry.getTask().equals("NEW TASK"));
        assert(!entry.getFocus());
        assert(entry.getDropped());
        assert(entry.getList() == null);
        assert(entry.getListPosition() == -1);
        assert(entry.getReminderDate() == 0);
        assert(entry.getRecurrence() == null);
        assert(entry.getPosition() == 1);

        appContext.deleteDatabase("TEST.db");

    }

    @Test
    public void removeEntry(){
        DbHelper db = new DbHelper(appContext,"TEST.db");

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

        appContext.deleteDatabase("TEST.db");
    }

    @Test
    public void loadLists(){
        DbHelper db = new DbHelper(appContext, "TEST.db");

        assert(db.loadLists().isEmpty());

        db.addEntry(new Entry(0,0,"NEW TASK"));

        assert(db.loadLists().isEmpty());

        addTestDataList(db);

        assert(db.loadLists().containsKey("TEST"));

        assert(Objects.equals(Objects.requireNonNull(db.loadLists().get("TEST")).getColor(), "WHITE"));
        appContext.deleteDatabase("TEST.db");

    }

    @Test
    public void updateEntry(){
        DbHelper db = new DbHelper(appContext, "TEST.db");

        db.addEntry(new Entry(0,0,"NEW TASK"));

        db.updateEntry(COLUMNS_ENTRIES_V1.TASK_COL,0,"TEST");

        assert(db.loadEntries().get(0).getTask().equals("TEST"));

        db.updateEntry(COLUMNS_ENTRIES_V1.FOCUS_COL, 0, false);

        assert(!db.loadEntries().get(0).getFocus());

        db.updateEntry(COLUMNS_ENTRIES_V1.FOCUS_COL, 0, true);

        assert(db.loadEntries().get(0).getFocus());

        db.updateEntry(COLUMNS_ENTRIES_V1.DROPPED_COL, 0, false);

        assert(!db.loadEntries().get(0).getDropped());

        db.updateEntry(COLUMNS_ENTRIES_V1.DROPPED_COL, 0, true);

        assert(db.loadEntries().get(0).getDropped());

        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_COL, 0, null);

        assert(db.loadEntries().get(0).getList() == null);

        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_COL, 0, "TEST");

        assert(db.loadEntries().get(0).getList().equals("TEST"));

        db.updateEntry(COLUMNS_ENTRIES_V1.LIST_POSITION_COL, 0, 0);

        assert(db.loadEntries().get(0).getListPosition() == 0);

        db.updateEntry(COLUMNS_ENTRIES_V1.REMINDER_DATE_COL, 0, 2000);

        assert(db.loadEntries().get(0).getReminderDate() == 2000);

        db.updateEntry(COLUMNS_ENTRIES_V1.RECURRENCE_COL, 0, null);

        assert(db.loadEntries().get(0).getRecurrence() == null);

        db.updateEntry(COLUMNS_ENTRIES_V1.RECURRENCE_COL, 0, "TEST");

        assert(db.loadEntries().get(0).getRecurrence().equals("TEST"));

        db.updateEntry(COLUMNS_ENTRIES_V1.POSITION_COL, 0, 99);

        assert(db.loadEntries().get(0).getPosition() == 99);


        appContext.deleteDatabase("TEST.db");
    }

    @AfterClass
    public static void tearDown(){

    }

}