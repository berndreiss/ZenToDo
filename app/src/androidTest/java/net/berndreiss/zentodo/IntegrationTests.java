package net.berndreiss.zentodo;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import net.berndreiss.zentodo.adapters.DropTaskListAdapter;
import net.berndreiss.zentodo.data.DataManager;
import net.berndreiss.zentodo.data.Task;
import net.berndreiss.zentodo.data.User;
import net.berndreiss.zentodo.data.ZenSQLiteHelper;
import net.berndreiss.zentodo.data.TaskList;
import net.berndreiss.zentodo.exceptions.DuplicateIdException;
import net.berndreiss.zentodo.exceptions.InvalidActionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class IntegrationTests {

    private static SharedData sharedData;

    private static final String DATABASE_NAME = "Data.db";

    @Before
    public void setup() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(DATABASE_NAME);
        sharedData = new SharedData(context);
        DataManager.initClientStub(sharedData, "test@test.net");
    }

    @Test
    public void constructor() {
        ZenSQLiteHelper db = new ZenSQLiteHelper(sharedData.context);

        //assert all main data structures are empty
        assert (sharedData.clientStub.loadTasks().isEmpty());
        assert (sharedData.clientStub.loadLists().isEmpty());

        db.close();
    }

    @Test
    public void add() {

        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        DataManager.add(sharedData, "0");
        DataManager.add(sharedData, "1");
        DataManager.add(sharedData, "2");

        assert (adapter.tasks.size() == 3);

        assert (adapter.tasks.get(0).getTask().equals("0"));
        assert (adapter.tasks.get(1).getTask().equals("1"));
        assert (adapter.tasks.get(2).getTask().equals("2"));

        Collection<? extends Task> tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.size() == 3);

        int counter = 0;

        for (Task t : tasksDB) {

            assert (t.getId() == adapter.tasks.get(counter).getId());
            assert (t.getTask().equals(String.valueOf(counter)));
            assert (!t.getFocus());
            assert (t.getDropped());
            assert (t.getList() == null);
            assert (t.getListPosition() == null);
            assert (t.getReminderDate() == null);
            assert (t.getRecurrence() == null);
            assert (t.getPosition() == counter);

            counter++;
        }

        Collection<? extends Task> droppedList = sharedData.clientStub.loadDropped();

        assert (droppedList.size() == 3);
    }


    @Test
    public void remove() throws InvalidActionException, InterruptedException, DuplicateIdException {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        TaskList newList = sharedData.clientStub.addNewList("0", null);
        DataManager.add(sharedData, "0");
        DataManager.add(sharedData, "1");
        DataManager.add(sharedData, "2");

        DataManager.editList(sharedData, adapter.tasks.get(0), "0");
        DataManager.editList(sharedData, adapter.tasks.get(1), "0");
        DataManager.editList(sharedData, adapter.tasks.get(2), "0");

        DataManager.remove(sharedData, adapter.tasks.get(0));
        Thread.sleep(500);//wait for changes to take effect as remove is not synchronous

        assert (adapter.tasks.size() == 2);
        assert (adapter.tasks.getFirst().getTask().equals("1"));
        assert (adapter.tasks.getFirst().getPosition() == 0);
        assert (adapter.tasks.getFirst().getListPosition() == 0);
        assert (adapter.tasks.get(1).getTask().equals("2"));
        assert (adapter.tasks.get(1).getPosition() == 1);
        assert (adapter.tasks.get(1).getListPosition() == 0);

        Collection<? extends Task> tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.size() == 2);

        int counter = 0;

        for (Task t : tasksDB) {

            assert (t.getId() == adapter.tasks.get(counter).getId());
            assert (t.getTask().equals(String.valueOf(counter + 1)));
            assert (t.getListPosition() == 0);
            assert (t.getPosition() == counter);

            counter++;
        }
    }

    @Test
    public void lists() {

        try (ZenSQLiteHelper db = new ZenSQLiteHelper(sharedData.context)) {
            DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
            sharedData.adapter = adapter;
            DataManager.add(sharedData, "0");
            DataManager.editList(sharedData, adapter.tasks.getFirst(), "0");

            //check original entry is being edited
            assert (adapter.tasks.getFirst().getListPosition() == 0);
            assert (adapter.tasks.getFirst().getList().equals("0"));

            DataManager.add(sharedData, "1");
            DataManager.editList(sharedData, adapter.tasks.get(1), "0");

            DataManager.add(sharedData, "2");
            DataManager.editList(sharedData, adapter.tasks.get(2), "1");

            DataManager.add(sharedData, "3");
            DataManager.editList(sharedData, adapter.tasks.get(3), "1");

            assert (sharedData.clientStub.loadDropped().isEmpty());

            List<Task> tasksDB = sharedData.clientStub.loadTasks();

            int counter = 0;

            for (Task e : tasksDB) {

                assert (e.getList().equals(String.valueOf(counter / 2)));
                assert (e.getListPosition() == counter % 2);

                counter++;
            }

            List<String> lists = DataManager.getListsAsString(sharedData);

            assert (lists.size() == 2);

            counter = 0;

            for (String l : lists) {
                assert (l.equals(String.valueOf(counter)));
                counter++;
            }

            Optional<TaskList> taskList = sharedData.clientStub.getListByName("0");
            Assert.assertTrue(taskList.isPresent());
            List<Task> list0 = sharedData.clientStub.loadList(taskList.get().getId());

            counter = 0;

            assert (list0.size() == 2);

            for (Task e : list0) {
                assert (e.getId() == adapter.tasks.get(counter).getId());
                counter++;
            }

            //check nothing changes when list is set to the same value
            DataManager.editList(sharedData, adapter.tasks.get(0), "0");

            assert (adapter.tasks.get(0).getListPosition() == 0);
            assert (adapter.tasks.get(1).getListPosition() == 1);

            tasksDB = sharedData.clientStub.loadTasks();

            assert (tasksDB.get(0).getListPosition() == 0);
            assert (tasksDB.get(1).getListPosition() == 1);

            DataManager.editList(sharedData, adapter.tasks.get(0), null);
            DataManager.editList(sharedData, adapter.tasks.get(1), null);

            lists = DataManager.getListsAsString(sharedData);

            assert (lists.size() == 1);

            User user = sharedData.clientStub.user;
            List<Task> listNone = db.getTaskManager().getNoList(user.getId(), user.getProfile());

            counter = 0;

            assert (listNone.size() == 2);

            for (Task e : listNone) {
                assert (e.getId() == adapter.tasks.get(counter).getId());
                counter++;
            }
        }
    }

    //swap tasks if item is moved by drag and drop
    @Test
    public void swapLists() {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        DataManager.add(sharedData, "0");
        DataManager.editList(sharedData, adapter.tasks.get(0), "0");

        DataManager.add(sharedData, "1");
        DataManager.editList(sharedData, adapter.tasks.get(1), "0");

        DataManager.add(sharedData, "2");
        DataManager.editList(sharedData, adapter.tasks.get(2), "1");

        DataManager.add(sharedData, "3");
        DataManager.editList(sharedData, adapter.tasks.get(3), "1");

        DataManager.swapLists(sharedData, adapter.tasks.get(0), adapter.tasks.get(1));

        assert (adapter.tasks.get(0).getTask().equals("1"));
        assert (adapter.tasks.get(0).getPosition() == 1);
        assert (adapter.tasks.get(0).getListPosition() == 0);
        assert (adapter.tasks.get(1).getTask().equals("0"));
        assert (adapter.tasks.get(1).getPosition() == 0);
        assert (adapter.tasks.get(1).getListPosition() == 1);
        assert (adapter.tasks.get(2).getListPosition() == 0);
        assert (adapter.tasks.get(3).getListPosition() == 1);

        List<Task> tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.get(1).getTask().equals("1"));
        assert (tasksDB.get(1).getPosition() == 1);
        assert (tasksDB.get(1).getListPosition() == 0);
        assert (tasksDB.get(0).getTask().equals("0"));
        assert (tasksDB.get(0).getPosition() == 0);
        assert (tasksDB.get(0).getListPosition() == 1);
        assert (tasksDB.get(2).getListPosition() == 0);
        assert (tasksDB.get(3).getListPosition() == 1);

    }

    //swap tasks if item is moved by drag and drop
    @Test
    public void swap() {

        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        DataManager.add(sharedData, "0");
        DataManager.editList(sharedData, adapter.tasks.get(0), "0");

        DataManager.add(sharedData, "1");
        DataManager.editList(sharedData, adapter.tasks.get(1), "0");

        DataManager.add(sharedData, "2");
        DataManager.editList(sharedData, adapter.tasks.get(2), "1");

        DataManager.add(sharedData, "3");
        DataManager.editList(sharedData, adapter.tasks.get(3), "1");

        DataManager.swap(sharedData, adapter.tasks.get(0), adapter.tasks.get(1));

        assert (adapter.tasks.get(0).getTask().equals("1"));
        assert (adapter.tasks.get(0).getPosition() == 0);
        assert (adapter.tasks.get(0).getListPosition() == 1);
        assert (adapter.tasks.get(1).getTask().equals("0"));
        assert (adapter.tasks.get(1).getPosition() == 1);
        assert (adapter.tasks.get(1).getListPosition() == 0);

        List<Task> taskDB = sharedData.clientStub.loadTasks();

        assert (taskDB.get(0).getTask().equals("1"));
        assert (taskDB.get(0).getListPosition() == 1);
        assert (taskDB.get(1).getTask().equals("0"));
        assert (taskDB.get(1).getListPosition() == 0);

    }

    /*
     *   The following functions edit different fields of tasks by their id.
     */

    @Test
    public void setTask() {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        DataManager.add(sharedData, "0");
        DataManager.setTask(sharedData, adapter.tasks.getFirst(), "1");

        assert (adapter.tasks.getFirst().getTask().equals("1"));

        List<Task> tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.getFirst().getTask().equals("1"));
    }


    @Test
    public void setFocus() {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;

        DataManager.add(sharedData, "0");
        DataManager.setFocus(sharedData, adapter.tasks.getFirst(), true);

        assert (adapter.tasks.getFirst().getFocus());
        assert (!adapter.tasks.getFirst().getDropped());

        List<Task> tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.getFirst().getFocus());
        assert (!tasksDB.getFirst().getDropped());

        List<Task> tasksFocus = sharedData.clientStub.loadFocus();

        assert (tasksFocus.size() == 1);
    }


    @Test
    public void setDropped() {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;

        DataManager.add(sharedData, "0");
        DataManager.setDropped(sharedData, adapter.tasks.getFirst(), false);

        assert (!adapter.tasks.getFirst().getDropped());

        List<Task> tasksDB = sharedData.clientStub.loadTasks();

        assert (!tasksDB.getFirst().getDropped());

        List<Task> tasksFocus = sharedData.clientStub.loadDropped();

        assert (tasksFocus.isEmpty());
    }

    @Test
    public void editReminderDate() {

        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;

        LocalDate date = LocalDate.now();
        DataManager.add(sharedData, "0");
        DataManager.editReminderDate(sharedData, adapter.tasks.getFirst(), date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        assert (adapter.tasks.getFirst().getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(date));
        assert (sharedData.clientStub.loadDropped().isEmpty());

        List<Task> tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.getFirst().getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(date));

        DataManager.editReminderDate(sharedData, adapter.tasks.getFirst(), null);

        assert (adapter.tasks.getFirst().getReminderDate() == null);

        tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.getFirst().getReminderDate() == null);
    }

    @Test
    public void editRecurrence() {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        String recurrence = "m1";
        DataManager.add(sharedData, "0");
        DataManager.editRecurrence(sharedData, adapter.tasks.getFirst(), recurrence);

        assert (adapter.tasks.getFirst().getRecurrence().equals(recurrence));
        assert (sharedData.clientStub.loadDropped().isEmpty());

        List<Task> tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.getFirst().getRecurrence().equals(recurrence));

        DataManager.editRecurrence(sharedData, adapter.tasks.getFirst(), null);

        assert (adapter.tasks.getFirst().getRecurrence() == null);

        tasksDB = sharedData.clientStub.loadTasks();

        assert (tasksDB.getFirst().getRecurrence() == null);
    }

    @Test
    public void listColors() {

        try (ZenSQLiteHelper db = new ZenSQLiteHelper(sharedData.context)) {
            DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
            sharedData.adapter = adapter;
            DataManager.add(sharedData, "0");
            DataManager.editList(sharedData, adapter.tasks.getFirst(), "0");
            DataManager.editListColor(sharedData, 0L, "BLUE");

            if ((!DataManager.getListColor(sharedData, 0L).equals("BLUE")))
                throw new AssertionError();

            Map<Long, String> colorMap = sharedData.clientStub.getListColors();

            assert (colorMap.containsKey("0"));
            assert (Objects.equals(colorMap.get("0"), "BLUE"));
        }
    }

    @Test
    public void getLists() {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        DataManager.add(sharedData, "0");
        DataManager.editList(sharedData, adapter.tasks.getFirst(), "0");

        List<String> lists = DataManager.getListsAsString(sharedData);

        assert (lists.size() == 3);

        assert (lists.get(0).equals("0"));
        assert (lists.get(1).equals("ALL TASKS"));
        assert (lists.get(2).equals("No list"));
    }


    @Test
    public void getFocus() {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        DataManager.add(sharedData, "0");
        DataManager.add(sharedData, "1");
        DataManager.add(sharedData, "2");
        DataManager.add(sharedData, "3");

        DataManager.setFocus(sharedData, adapter.tasks.get(0), true);
        DataManager.editRecurrence(sharedData, adapter.tasks.get(1), "w2");
        DataManager.editRecurrence(sharedData, adapter.tasks.get(2), "w2");
        DataManager.addToRecurringButRemoved(sharedData, adapter.tasks.get(2).getId());
        try (ZenSQLiteHelper db = new ZenSQLiteHelper(sharedData.context)) {
            //get data
            List<Task> focused = sharedData.clientStub.loadFocus();

            assert (focused.size() == 2);
            assert (focused.get(0).getTask().equals("0"));
            assert (focused.get(1).getTask().equals("1"));

            DataManager.removeFromRecurringButRemoved(sharedData, adapter.tasks.get(2).getId());
            focused = sharedData.clientStub.loadFocus();

            assert (focused.size() == 3);
            assert (focused.get(2).getTask().equals("2"));

        }
    }

    @Test
    public void getTasksToPick() {
        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        DataManager.add(sharedData, "0");
        DataManager.add(sharedData, "1");

        List<Task> tasksToPick = DataManager.getTasksToPick(sharedData);

        assert (tasksToPick.size() == adapter.tasks.size());

        DataManager.editReminderDate(sharedData, adapter.tasks.get(0), LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        DataManager.editReminderDate(sharedData, adapter.tasks.get(1), LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        tasksToPick = DataManager.getTasksToPick(sharedData);

        assert (tasksToPick.size() == 1);
        assert (tasksToPick.getFirst().getTask().equals("0"));
    }

    //tests whether ArrayList<Integer> recurringButRemovedFromToday (see documentation in FocusTaskListAdapter.java, Data.java, DbHelper.java)
    //is saved/loaded correctly
    @Test
    public void saveLoadRecurring() {
        try (ZenSQLiteHelper db = new ZenSQLiteHelper(sharedData.context)) {
            //get date of today
            String filename = LocalDate.now().toString();

            assert (db.getTaskManager().loadRecurring().isEmpty());

            DataManager.addToRecurringButRemoved(sharedData, 0);
            //add first element, save and assert results upon load
            List<Long> recurring = db.getTaskManager().loadRecurring();
            assert (recurring.size() == 1);
            assert (recurring.getFirst() == 0);

            DataManager.addToRecurringButRemoved(sharedData, 1);
            DataManager.addToRecurringButRemoved(sharedData, 2);
            for (int i = 0; i < 3; i++)
                assert (db.getTaskManager().loadRecurring().get(i) == i);

            //remove second element, save and assert results upon load
            DataManager.removeFromRecurringButRemoved(sharedData, 1);
            assert (db.getTaskManager().loadRecurring().get(0) == 0);
            assert (db.getTaskManager().loadRecurring().get(1) == 2);

            //clean up
            new File(sharedData.context.getFilesDir() + "/" + filename).delete();
        }
    }

    @Test
    public void setRecurring() {
        //different dates representing today.
        LocalDate[] today = {LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 1, 31),
                LocalDate.of(2023, 2, 28),
                LocalDate.of(2024, 2, 28),
                LocalDate.of(2023, 12, 31),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 30),
                LocalDate.of(2023, 12, 31),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 1),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 1)
        };

        //intervals in which tasks reoccur. the letter represents days/weeks/months/years
        //i.e.: w2 -> reoccurs every two weeks, m3 -> reoccurs every three months, d1 -> reoccurs every day
        String[] intervals = {"d1", "d2", "d3", "d4", "d5", "d6", "d7",
                "d1", "d1", "d1", "d1",
                "w1", "w2", "w1", "w1", "w1",
                "m1", "m2", "m1",
                "y1", "y2"
        };

        //reminder dates for tasks -> the interval above should be added to the initial reminder dates
        //unless reminder date + interval <= today -> in that case the interval is added x times so
        // that date + x * interval > today
        LocalDate[] tests = {LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15), //adding one to seven days
                LocalDate.of(2023, 1, 31), //adding one day and incrementing month
                LocalDate.of(2023, 2, 28), //adding one day and incrementing month in February
                LocalDate.of(2024, 2, 28), //adding one day in leap year therefore not incrementing month
                LocalDate.of(2023, 12, 31), //adding one day and incrementing month + year
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15), //adding one and two weeks
                LocalDate.of(2023, 10, 8), //adding one week and today is 2023-10-15 -> another week is added
                LocalDate.of(2023, 10, 25), //adding one week and month is incremented too
                LocalDate.of(2023, 12, 4), //adding one week and today is 2023-12-31 -> four weeks are added and month/year increment
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 1), //adding one and two months
                LocalDate.of(2023, 1, 1), //adding one month but today is 2023-12-01 -> year is incremented too
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2022, 1, 1) //adding one and two years
        };

        //expected results
        LocalDate[] results = {
                LocalDate.of(2023, 10, 16),
                LocalDate.of(2023, 10, 17),
                LocalDate.of(2023, 10, 18),
                LocalDate.of(2023, 10, 19),
                LocalDate.of(2023, 10, 20),
                LocalDate.of(2023, 10, 21),
                LocalDate.of(2023, 10, 22),
                LocalDate.of(2023, 2, 1),
                LocalDate.of(2023, 3, 1),
                LocalDate.of(2024, 2, 29),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2023, 10, 22),
                LocalDate.of(2023, 10, 29),
                LocalDate.of(2023, 10, 22),
                LocalDate.of(2023, 11, 1),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2023, 2, 1),
                LocalDate.of(2023, 3, 1),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 1)
        };


        DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);
        sharedData.adapter = adapter;
        DataManager.add(sharedData, "0");

        //run tests
        for (int i = 0; i < tests.length; i++) {

            //set reminder date
            DataManager.editReminderDate(sharedData, adapter.tasks.getFirst(), tests[i].atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            DataManager.editRecurrence(sharedData, adapter.tasks.getFirst(), intervals[i]);

            //TODO today cannot be passed anymore
            //DataManager.setRecurring(sharedData, adapter.tasks.getFirst(), today[i]);

            //assert results
            assert (adapter.tasks.getFirst().getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(results[i]));

        }

        sharedData.context.deleteDatabase(DATABASE_NAME);
    }

}
