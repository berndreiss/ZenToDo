package net.berndreiss.zentodo;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import net.berndreiss.zentodo.Data.DataManager;
import net.berndreiss.zentodo.api.Entry;
import net.berndreiss.zentodo.Data.SQLiteHelper;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class DataTest {
    private static Context appContext;

    private static final String DATABASE_NAME = "Data.db";

    @Before
    public void setup(){
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        appContext.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void constructor(){

        SQLiteHelper db = new SQLiteHelper(appContext);

        //assert all main data structures are empty
        assert(db.loadEntries().isEmpty());
        assert(db.loadLists().isEmpty());

        db.close();
    }

    @Test
    public void add() {

        try (SQLiteHelper db = new SQLiteHelper(appContext)) {

            List<Entry> entries = new ArrayList<>();
            DataManager.add(appContext, entries, "0");
            DataManager.add(appContext, entries, "1");
            DataManager.add(appContext, entries, "2");

            assert(entries.size() == 3);

            assert(entries.get(0).getTask().equals("0"));
            assert(entries.get(1).getTask().equals("1"));
            assert(entries.get(2).getTask().equals("2"));

            Collection<? extends Entry> entriesDB = db.loadEntries();

            assert(entriesDB.size()==3);

            int counter = 0;

            for (Entry e: entriesDB){

                assert(e.getId() == entries.get(counter).getId());
                assert(e.getTask().equals(String.valueOf(counter)));
                assert(!e.getFocus());
                assert(e.getDropped());
                assert(e.getList() == null);
                assert(e.getListPosition() == null);
                assert(e.getReminderDate() == null);
                assert(e.getRecurrence() == null);
                assert(e.getPosition() == counter);

                counter++;
            }

            Collection<? extends  Entry> droppedList = db.loadDropped();

            assert(droppedList.size() == 3);
        }
    }


    @Test
    public void remove() {
        try (SQLiteHelper db = new SQLiteHelper(appContext)) {

            List<Entry> entries = new ArrayList<>();
            DataManager.add(appContext, entries, "0");
            DataManager.add(appContext, entries, "1");
            DataManager.add(appContext, entries, "2");

            DataManager.editList(appContext, entries, entries.get(0), "0");
            DataManager.editList(appContext, entries, entries.get(1), "0");
            DataManager.editList(appContext, entries, entries.get(2), "1");

            DataManager.remove(appContext, entries, entries.get(0));

            assert(entries.size()==2);
            assert(entries.get(0).getTask().equals("1"));
            assert(entries.get(0).getPosition() == 0);
            assert(entries.get(0).getListPosition() == 0);
            assert(entries.get(1).getTask().equals("2"));
            assert(entries.get(1).getPosition() == 1);
            assert(entries.get(1).getListPosition() == 0);

            Collection<? extends Entry> entriesDB = db.loadEntries();

            assert(entriesDB.size()==2);

            int counter = 0;

            for (Entry e: entriesDB){

                assert(e.getId() == entries.get(counter).getId());
                assert(e.getTask().equals(String.valueOf(counter+1)));
                assert(e.getListPosition() == 0);
                assert(e.getPosition() == counter);

                counter++;
            }
        }
    }

    @Test
    public void lists() {

        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            List<Entry> entries = new ArrayList<>();
            DataManager.add(appContext, entries, "0");
            DataManager.editList(appContext, entries, entries.get(0), "0");

            //check original entry is being edited
            assert(entries.get(0).getListPosition() == 0);
            assert(entries.get(0).getList().equals("0"));

            DataManager.add(appContext, entries, "1");
            DataManager.editList(appContext, entries, entries.get(1), "0");

            DataManager.add(appContext, entries, "2");
            DataManager.editList(appContext, entries, entries.get(2), "1");

            DataManager.add(appContext, entries, "3");
            DataManager.editList(appContext, entries, entries.get(3), "1");

            assert(db.loadDropped().isEmpty());

            List<Entry> entriesDB = db.loadEntries();

            int counter = 0;

            for (Entry e: entriesDB) {

                assert(e.getList().equals(String.valueOf(counter/2)));
                assert(e.getListPosition() == counter%2);

                counter++;
            }

            List<String> lists = db.getLists();

            assert(lists.size() == 2);

            counter = 0;

            for (String l: lists){
                assert(l.equals(String.valueOf(counter)));
                counter++;
            }

            List<Entry> list0 = db.loadList("0");

            counter = 0;

            assert(list0.size()==2);

            for (Entry e: list0){
                assert(e.getId()==entries.get(counter).getId());
                counter++;
            }

            //check nothing changes when list is set to the same value
            DataManager.editList(appContext, entries, entries.get(0), "0");

            assert(entries.get(0).getListPosition() == 0);
            assert(entries.get(1).getListPosition() == 1);

            entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getListPosition() == 0);
            assert(entriesDB.get(1).getListPosition() == 1);

            DataManager.editList(appContext, entries, entries.get(0), null);
            DataManager.editList(appContext, entries, entries.get(1), null);

            lists = db.getLists();

            assert(lists.size()==1);


            List<Entry> listNone = db.getNoList();

            counter = 0;

            assert(listNone.size()==2);

            for (Entry e: listNone){
                assert(e.getId()==entries.get(counter).getId());
                counter++;
            }
        }
    }

    //swap entries if item is moved by drag and drop
    @Test
    public void swapLists() {
        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            List<Entry> entries = new ArrayList<>();
            DataManager.add(appContext, entries, "0");
            DataManager.editList(appContext, entries, entries.get(0), "0");

            DataManager.add(appContext, entries, "1");
            DataManager.editList(appContext, entries, entries.get(1), "0");

            DataManager.add(appContext, entries,"2");
            DataManager.editList(appContext, entries, entries.get(2), "1");

            DataManager.add(appContext, entries, "3");
            DataManager.editList(appContext, entries, entries.get(3), "1");

            DataManager.swapLists(appContext, entries, entries.get(0), entries.get(1));

            assert(entries.get(0).getTask().equals("1"));
            assert(entries.get(0).getPosition() == 1);
            assert(entries.get(0).getListPosition() == 0);
            assert(entries.get(1).getTask().equals("0"));
            assert(entries.get(1).getPosition() == 0);
            assert(entries.get(1).getListPosition() == 1);
            assert(entries.get(2).getListPosition() == 0);
            assert(entries.get(3).getListPosition() == 1);

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.get(1).getTask().equals("1"));
            assert(entriesDB.get(1).getPosition() == 1);
            assert(entriesDB.get(1).getListPosition() == 0);
            assert(entriesDB.get(0).getTask().equals("0"));
            assert(entriesDB.get(0).getPosition() == 0);
            assert(entriesDB.get(0).getListPosition() == 1);
            assert(entriesDB.get(2).getListPosition() == 0);
            assert(entriesDB.get(3).getListPosition() == 1);

        }
    }
    //swap entries if item is moved by drag and drop
    @Test
    public void swap() {

        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            List<Entry> entries = new ArrayList<>();
            DataManager.add(appContext, entries, "0");
            DataManager.editList(appContext, entries, entries.get(0), "0");

            DataManager.add(appContext, entries, "1");
            DataManager.editList(appContext, entries, entries.get(1), "0");

            DataManager.add(appContext, entries,"2");
            DataManager.editList(appContext, entries, entries.get(2), "1");

            DataManager.add(appContext, entries, "3");
            DataManager.editList(appContext, entries, entries.get(3), "1");

            DataManager.swap(appContext, entries, entries.get(0), entries.get(1));

            assert(entries.get(0).getTask().equals("1"));
            assert(entries.get(0).getPosition() == 0);
            assert(entries.get(0).getListPosition() == 1);
            assert(entries.get(1).getTask().equals("0"));
            assert(entries.get(1).getPosition() == 1);
            assert(entries.get(1).getListPosition() == 0);

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getTask().equals("1"));
            assert(entriesDB.get(0).getListPosition() == 1);
            assert(entriesDB.get(1).getTask().equals("0"));
            assert(entriesDB.get(1).getListPosition() == 0);

        }
    }

    /*
     *   The following functions edit different fields of entries by their id.
     */

    @Test
    public void setTask() {
        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            List<Entry> entries = new ArrayList<>();

            DataManager.add(appContext, entries, "0");
            DataManager.setTask(appContext, entries.get(0), "1");

            assert (entries.get(0).getTask().equals("1"));

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getTask().equals("1"));
        }
    }


    @Test
    public void setFocus() {
        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            List<Entry> entries = new ArrayList<>();

            DataManager.add(appContext, entries, "0");
            DataManager.setFocus(appContext, entries.get(0), true);

            assert(entries.get(0).getFocus());
            assert(!entries.get(0).getDropped());

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getFocus());
            assert(!entriesDB.get(0).getDropped());

            List<Entry> entriesFocus = db.loadFocus();

            assert(entriesFocus.size() == 1);
        }
    }


    @Test
    public void setDropped() {
        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            List<Entry> entries = new ArrayList<>();

            DataManager.add(appContext, entries, "0");
            DataManager.setDropped(appContext, entries.get(0), false);

            assert(!entries.get(0).getDropped());

            List<Entry> entriesDB = db.loadEntries();

            assert(!entriesDB.get(0).getDropped());

            List<Entry> entriesFocus = db.loadDropped();

            assert(entriesFocus.isEmpty());
        }
    }

    @Test
    public void editReminderDate() {

        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            List<Entry> entries = new ArrayList<>();

            LocalDate date = LocalDate.now();
            DataManager.add(appContext, entries, "0");
            DataManager.editReminderDate(appContext, entries.get(0), date);

            assert (entries.get(0).getReminderDate().equals(date));
            assert (db.loadDropped().isEmpty());

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getReminderDate().equals(date));

            DataManager.editReminderDate(appContext, entries.get(0), null);

            assert (entries.get(0).getReminderDate() == null);

            entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getReminderDate() == null);
        }
    }

    @Test
    public void editRecurrence() {
        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            List<Entry> entries = new ArrayList<>();

            String recurrence = "m1";
            DataManager.add(appContext, entries, "0");
            DataManager.editRecurrence(appContext, entries.get(0), recurrence);

            assert (entries.get(0).getRecurrence().equals(recurrence));
            assert (db.loadDropped().isEmpty());

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getRecurrence().equals(recurrence));

            DataManager.editRecurrence(appContext, entries.get(0), null);

            assert (entries.get(0).getRecurrence() == null);

            entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getRecurrence() == null);
        }
    }

    @Test
    public void listColors() {

        try(SQLiteHelper db = new SQLiteHelper(appContext)){
            List<Entry> entries = new ArrayList<>();
            DataManager.add(appContext, entries, "0");
            DataManager.editList(appContext, entries, entries.get(0), "0");
            DataManager.editListColor(appContext, "0", "BLUE");

            if ((!DataManager.getListColor(appContext, "0").equals("BLUE")))
                throw new AssertionError();

            Map<String, String> colorMap = db.getListColors();

            assert(colorMap.containsKey("0"));
            assert(Objects.equals(colorMap.get("0"), "BLUE"));
        }
    }

    @Test
    public void getLists(){
        List<Entry> entries = new ArrayList<>();
        DataManager.add(appContext, entries, "0");
        DataManager.editList(appContext, entries, entries.get(0), "0");

        List<String> lists = DataManager.getLists(appContext);

        assert(lists.size() == 3);

        assert(lists.get(0).equals("0"));
        assert(lists.get(1).equals("ALL TASKS"));
        assert(lists.get(2).equals("No list"));
    }


    @Test
    public void getFocus() {
        List<Entry> entries = new ArrayList<>();
        DataManager.add(appContext, entries, "0");
        DataManager.add(appContext, entries, "1");
        DataManager.add(appContext, entries, "2");
        DataManager.add(appContext, entries, "3");

        DataManager.setFocus(appContext, entries.get(0), true);
        DataManager.editRecurrence(appContext, entries.get(1), "w2");
        DataManager.editRecurrence(appContext, entries.get(2), "w2");
        DataManager.addToRecurringButRemoved(appContext, entries.get(2).getId());
        try (SQLiteHelper db = new SQLiteHelper(appContext)) {
            //get data
            List<Entry> focused = db.loadFocus();

            assert(focused.size()==2);
            assert(focused.get(0).getTask().equals("0"));
            assert(focused.get(1).getTask().equals("1"));

            DataManager.removeFromRecurringButRemoved(appContext, entries.get(2).getId());
            focused = db.loadFocus();

            assert(focused.size()==3);
            assert(focused.get(2).getTask().equals("2"));

        }
    }

    @Test
    public void getTasksToPick() {
        List<Entry> entries = new ArrayList<>();
        DataManager.add(appContext, entries, "0");
        DataManager.add(appContext, entries, "1");

        List<Entry> tasksToPick = DataManager.getTasksToPick(appContext);

        assert(tasksToPick.size()==entries.size());

        DataManager.editReminderDate(appContext, entries.get(0), LocalDate.now());
        DataManager.editReminderDate(appContext, entries.get(1), LocalDate.now().plusDays(1));

        tasksToPick = DataManager.getTasksToPick(appContext);

        assert(tasksToPick.size()==1);
        assert(tasksToPick.get(0).getTask().equals("0"));
    }
    //tests whether ArrayList<Integer> recurringButRemovedFromToday (see documentation in FocusTaskListAdapter.java, Data.java, DbHelper.java)
    //is saved/loaded correctly
    @Test
    public void saveLoadRecurring(){


        try(SQLiteHelper db = new SQLiteHelper(appContext)) {



            //get date of today
            String filename = LocalDate.now().toString();

            assert (db.loadRecurring().isEmpty());

            DataManager.addToRecurringButRemoved(appContext, 0);
            //add first element, save and assert results upon load
            List<Integer> recurring = db.loadRecurring();
            assert (recurring.size() == 1);
            assert (recurring.get(0) == 0);


            DataManager.addToRecurringButRemoved(appContext, 1);
            DataManager.addToRecurringButRemoved(appContext, 2);
            for (int i = 0; i < 3; i++)
                assert (db.loadRecurring().get(i) == i);

            //remove second element, save and assert results upon load
            DataManager.removeFromRecurringButRemoved(appContext, 1);
            assert (db.loadRecurring().get(0) == 0);
            assert (db.loadRecurring().get(1) == 2);

            //clean up
            new File(appContext.getFilesDir() + "/" + filename).delete();

        }
    }

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
                LocalDate.of(2023,1,31),
                LocalDate.of(2023,2,28),
                LocalDate.of(2024,2,28),
                LocalDate.of(2023,12,31),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,15),
                LocalDate.of(2023,10,30),
                LocalDate.of(2023,12,31),
                LocalDate.of(2023,1,1),
                LocalDate.of(2023,1,1),
                LocalDate.of(2023,12,1),
                LocalDate.of(2023,1,1),
                LocalDate.of(2023,1,1)
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
                LocalDate.of(2023,2,1),
                LocalDate.of(2023,3,1),
                LocalDate.of(2024,2,29),
                LocalDate.of(2024,1,1),
                LocalDate.of(2023,10,22),
                LocalDate.of(2023,10,29),
                LocalDate.of(2023,10,22),
                LocalDate.of(2023,11,1),
                LocalDate.of(2024,1,1),
                LocalDate.of(2023,2,1),
                LocalDate.of(2023,3,1),
                LocalDate.of(2024,1,1),
                LocalDate.of(2024,1,1),
                LocalDate.of(2024,1,1)
        };


        List<Entry> entries = new ArrayList<>();
        DataManager.add(appContext, entries, "0");

        //run tests
        for (int i = 0; i < tests.length; i++){

            //set reminder date
            DataManager.editReminderDate(appContext, entries.get(0), tests[i]);
            DataManager.editRecurrence(appContext, entries.get(0), intervals[i]);

            DataManager.setRecurring(appContext, entries.get(0), today[i]);

            //assert results
            assert(entries.get(0).getReminderDate().equals(results[i]));

        }

        appContext.deleteDatabase(DATABASE_NAME);
    }

}
