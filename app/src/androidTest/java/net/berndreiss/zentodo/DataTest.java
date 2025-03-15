package net.berndreiss.zentodo;

import androidx.test.platform.app.InstrumentationRegistry;

import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Entry;
import net.berndreiss.zentodo.data.SQLiteHelper;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class DataTest {
    private static SharedData sharedData;

    private static final String DATABASE_NAME = "Data.db";

    @Before
    public void setup(){
        sharedData = new SharedData(InstrumentationRegistry.getInstrumentation().getTargetContext());
        sharedData.context.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void constructor(){

        SQLiteHelper db = new SQLiteHelper(sharedData.context);

        //assert all main data structures are empty
        assert(db.loadEntries().isEmpty());
        assert(db.loadLists().isEmpty());

        db.close();
    }

    @Test
    public void add() {

        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {

            List<Entry> entries = new ArrayList<>();
            DataManager.add(sharedData, entries, "0");
            DataManager.add(sharedData, entries, "1");
            DataManager.add(sharedData, entries, "2");

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
        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {

            List<Entry> entries = new ArrayList<>();
            DataManager.add(sharedData, entries, "0");
            DataManager.add(sharedData, entries, "1");
            DataManager.add(sharedData, entries, "2");

            DataManager.editList(sharedData, entries, entries.get(0), "0");
            DataManager.editList(sharedData, entries, entries.get(1), "0");
            DataManager.editList(sharedData, entries, entries.get(2), "1");

            DataManager.remove(sharedData, entries, entries.get(0));

            assert(entries.size()==2);
            assert(entries.getFirst().getTask().equals("1"));
            assert(entries.getFirst().getPosition() == 0);
            assert(entries.getFirst().getListPosition() == 0);
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

        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            List<Entry> entries = new ArrayList<>();
            DataManager.add(sharedData, entries, "0");
            DataManager.editList(sharedData, entries, entries.getFirst(), "0");

            //check original entry is being edited
            assert(entries.getFirst().getListPosition() == 0);
            assert(entries.getFirst().getList().equals("0"));

            DataManager.add(sharedData, entries, "1");
            DataManager.editList(sharedData, entries, entries.get(1), "0");

            DataManager.add(sharedData, entries, "2");
            DataManager.editList(sharedData, entries, entries.get(2), "1");

            DataManager.add(sharedData, entries, "3");
            DataManager.editList(sharedData, entries, entries.get(3), "1");

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
            DataManager.editList(sharedData, entries, entries.get(0), "0");

            assert(entries.get(0).getListPosition() == 0);
            assert(entries.get(1).getListPosition() == 1);

            entriesDB = db.loadEntries();

            assert(entriesDB.get(0).getListPosition() == 0);
            assert(entriesDB.get(1).getListPosition() == 1);

            DataManager.editList(sharedData, entries, entries.get(0), null);
            DataManager.editList(sharedData, entries, entries.get(1), null);

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
        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            List<Entry> entries = new ArrayList<>();
            DataManager.add(sharedData, entries, "0");
            DataManager.editList(sharedData, entries, entries.get(0), "0");

            DataManager.add(sharedData, entries, "1");
            DataManager.editList(sharedData, entries, entries.get(1), "0");

            DataManager.add(sharedData, entries,"2");
            DataManager.editList(sharedData, entries, entries.get(2), "1");

            DataManager.add(sharedData, entries, "3");
            DataManager.editList(sharedData, entries, entries.get(3), "1");

            DataManager.swapLists(sharedData, entries, entries.get(0), entries.get(1));

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

        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            List<Entry> entries = new ArrayList<>();
            DataManager.add(sharedData, entries, "0");
            DataManager.editList(sharedData, entries, entries.get(0), "0");

            DataManager.add(sharedData, entries, "1");
            DataManager.editList(sharedData, entries, entries.get(1), "0");

            DataManager.add(sharedData, entries,"2");
            DataManager.editList(sharedData, entries, entries.get(2), "1");

            DataManager.add(sharedData, entries, "3");
            DataManager.editList(sharedData, entries, entries.get(3), "1");

            DataManager.swap(sharedData, entries, entries.get(0), entries.get(1));

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
        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            List<Entry> entries = new ArrayList<>();

            DataManager.add(sharedData, entries, "0");
            DataManager.setTask(sharedData, entries.getFirst(), "1");

            assert (entries.getFirst().getTask().equals("1"));

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.getFirst().getTask().equals("1"));
        }
    }


    @Test
    public void setFocus() {
        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            List<Entry> entries = new ArrayList<>();

            DataManager.add(sharedData, entries, "0");
            DataManager.setFocus(sharedData, entries.getFirst(), true);

            assert(entries.getFirst().getFocus());
            assert(!entries.getFirst().getDropped());

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.getFirst().getFocus());
            assert(!entriesDB.getFirst().getDropped());

            List<Entry> entriesFocus = db.loadFocus();

            assert(entriesFocus.size() == 1);
        }
    }


    @Test
    public void setDropped() {
        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            List<Entry> entries = new ArrayList<>();

            DataManager.add(sharedData, entries, "0");
            DataManager.setDropped(sharedData, entries.getFirst(), false);

            assert(!entries.getFirst().getDropped());

            List<Entry> entriesDB = db.loadEntries();

            assert(!entriesDB.getFirst().getDropped());

            List<Entry> entriesFocus = db.loadDropped();

            assert(entriesFocus.isEmpty());
        }
    }

    @Test
    public void editReminderDate() {

        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            List<Entry> entries = new ArrayList<>();

            LocalDate date = LocalDate.now();
            DataManager.add(sharedData, entries, "0");
            DataManager.editReminderDate(sharedData, entries.getFirst(), date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

            assert (entries.getFirst().getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(date));
            assert (db.loadDropped().isEmpty());

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.getFirst().getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(date));

            DataManager.editReminderDate(sharedData, entries.getFirst(), null);

            assert (entries.getFirst().getReminderDate() == null);

            entriesDB = db.loadEntries();

            assert(entriesDB.getFirst().getReminderDate() == null);
        }
    }

    @Test
    public void editRecurrence() {
        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            List<Entry> entries = new ArrayList<>();

            String recurrence = "m1";
            DataManager.add(sharedData, entries, "0");
            DataManager.editRecurrence(sharedData, entries.getFirst(), recurrence);

            assert (entries.getFirst().getRecurrence().equals(recurrence));
            assert (db.loadDropped().isEmpty());

            List<Entry> entriesDB = db.loadEntries();

            assert(entriesDB.getFirst().getRecurrence().equals(recurrence));

            DataManager.editRecurrence(sharedData, entries.getFirst(), null);

            assert (entries.getFirst().getRecurrence() == null);

            entriesDB = db.loadEntries();

            assert(entriesDB.getFirst().getRecurrence() == null);
        }
    }

    @Test
    public void listColors() {

        try(SQLiteHelper db = new SQLiteHelper(sharedData.context)){
            List<Entry> entries = new ArrayList<>();
            DataManager.add(sharedData, entries, "0");
            DataManager.editList(sharedData, entries, entries.getFirst(), "0");
            DataManager.editListColor(sharedData, "0", "BLUE");

            if ((!DataManager.getListColor(sharedData, "0").equals("BLUE")))
                throw new AssertionError();

            Map<String, String> colorMap = db.getListColors();

            assert(colorMap.containsKey("0"));
            assert(Objects.equals(colorMap.get("0"), "BLUE"));
        }
    }

    @Test
    public void getLists(){
        List<Entry> entries = new ArrayList<>();
        DataManager.add(sharedData, entries, "0");
        DataManager.editList(sharedData, entries, entries.getFirst(), "0");

        List<String> lists = DataManager.getLists(sharedData);

        assert(lists.size() == 3);

        assert(lists.get(0).equals("0"));
        assert(lists.get(1).equals("ALL TASKS"));
        assert(lists.get(2).equals("No list"));
    }


    @Test
    public void getFocus() {
        List<Entry> entries = new ArrayList<>();
        DataManager.add(sharedData, entries, "0");
        DataManager.add(sharedData, entries, "1");
        DataManager.add(sharedData, entries, "2");
        DataManager.add(sharedData, entries, "3");

        DataManager.setFocus(sharedData, entries.get(0), true);
        DataManager.editRecurrence(sharedData, entries.get(1), "w2");
        DataManager.editRecurrence(sharedData, entries.get(2), "w2");
        DataManager.addToRecurringButRemoved(sharedData, entries.get(2).getId());
        try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
            //get data
            List<Entry> focused = db.loadFocus();

            assert(focused.size()==2);
            assert(focused.get(0).getTask().equals("0"));
            assert(focused.get(1).getTask().equals("1"));

            DataManager.removeFromRecurringButRemoved(sharedData, entries.get(2).getId());
            focused = db.loadFocus();

            assert(focused.size()==3);
            assert(focused.get(2).getTask().equals("2"));

        }
    }

    @Test
    public void getTasksToPick() {
        List<Entry> entries = new ArrayList<>();
        DataManager.add(sharedData, entries, "0");
        DataManager.add(sharedData, entries, "1");

        List<Entry> tasksToPick = DataManager.getTasksToPick(sharedData);

        assert(tasksToPick.size()==entries.size());

        DataManager.editReminderDate(sharedData, entries.get(0), LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        DataManager.editReminderDate(sharedData, entries.get(1), LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        tasksToPick = DataManager.getTasksToPick(sharedData);

        assert(tasksToPick.size()==1);
        assert(tasksToPick.getFirst().getTask().equals("0"));
    }
    //tests whether ArrayList<Integer> recurringButRemovedFromToday (see documentation in FocusTaskListAdapter.java, Data.java, DbHelper.java)
    //is saved/loaded correctly
    @Test
    public void saveLoadRecurring(){


        try(SQLiteHelper db = new SQLiteHelper(sharedData.context)) {



            //get date of today
            String filename = LocalDate.now().toString();

            assert (db.loadRecurring().isEmpty());

            DataManager.addToRecurringButRemoved(sharedData, 0);
            //add first element, save and assert results upon load
            List<Long> recurring = db.loadRecurring();
            assert (recurring.size() == 1);
            assert (recurring.getFirst() == 0);


            DataManager.addToRecurringButRemoved(sharedData, 1);
            DataManager.addToRecurringButRemoved(sharedData, 2);
            for (int i = 0; i < 3; i++)
                assert (db.loadRecurring().get(i) == i);

            //remove second element, save and assert results upon load
            DataManager.removeFromRecurringButRemoved(sharedData, 1);
            assert (db.loadRecurring().get(0) == 0);
            assert (db.loadRecurring().get(1) == 2);

            //clean up
            new File(sharedData.context.getFilesDir() + "/" + filename).delete();

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
        DataManager.add(sharedData, entries, "0");

        //run tests
        for (int i = 0; i < tests.length; i++){

            //set reminder date
            DataManager.editReminderDate(sharedData, entries.getFirst(), tests[i].atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            DataManager.editRecurrence(sharedData, entries.getFirst(), intervals[i]);

            DataManager.setRecurring(sharedData, entries.getFirst(), today[i]);

            //assert results
            assert(entries.getFirst().getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(results[i]));

        }

        sharedData.context.deleteDatabase(DATABASE_NAME);
    }

}
