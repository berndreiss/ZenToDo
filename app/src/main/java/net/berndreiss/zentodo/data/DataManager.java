package net.berndreiss.zentodo.data;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.exceptions.DuplicateUserIdException;
import net.berndreiss.zentodo.exceptions.InvalidUserActionException;
import net.berndreiss.zentodo.exceptions.PositionOutOfBoundException;
import net.berndreiss.zentodo.util.ClientStub;

import java.io.IOException;
import java.net.ConnectException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class DataManager {

    //recurring tasks are automatically added to FOCUS
    //when they are removed however, the ids are stored in this ArrayList and the tasks are not shown until the next day
    //see also: FocusTaskListAdapter
    private static List<Long> recurringButRemovedFromToday = null;

    /** TODO COMMENT */
    public static Map<Long, String> listColors = null;

    public static void initClientStub(SharedData sharedData, String email) throws InterruptedException {

        sharedData.clientStub = new ClientStub( sharedData.database.getDatabase());

        Consumer<String> messagePrinter = message -> {
            Looper.prepare();
            new Handler(Looper.getMainLooper()).post(()-> Toast.makeText(sharedData.context, message, Toast.LENGTH_LONG).show());
        };

        //sharedData.clientStub.setMessagePrinter(messagePrinter);
        sharedData.clientStub.setMessagePrinter(System.out::println);
        sharedData.clientStub.addOperationHandler(sharedData.uiOperationHandler);
        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.init("bd_reiss@yahoo.de", null, () -> "Test1234!?");
            } catch (IOException | DuplicateUserIdException | InvalidUserActionException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();
        thread.join();

        //for some strange reason Android refuses to establish a connection with the server
        //when the user is created locally for the first time, so we have to init again
        /*
        if (!userExists) {
            Log.v("TEST", "THREAD2");
            thread = new Thread(() -> {
                sharedData.clientStub.init(() -> "Test123!?");
            });
            thread.start();
        }*/

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param task
     */
    public static void add(SharedData sharedData, TaskListAdapter adapter, String task) {

        //write changes to database
        Thread thread = new Thread(() -> {
            Entry entry = null;
            try {
                entry = sharedData.clientStub.addNewEntry(task);
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
            adapter.entries.add(entry);
            ((Activity) sharedData.context).runOnUiThread(() ->adapter.notifyDataSetChanged());
        });
        thread.start();

        //Log.v("TEST", "USER NULL: " + String.valueOf(clientStub.getUser() == null));
        /*
        if (clientStub != null && clientStub.getUser() != null) {
            Thread thread = new Thread(() -> {
                clientStub.addNewEntry(
                        entry.getId(),
                        entry.getTask(),
                        clientStub.getUser().getId(),
                        entry.getPosition());
            });
            thread.start();
        }*/

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry
     */
    public static void remove(SharedData sharedData, TaskListAdapter adapter, Entry entry) {

        Thread thread = new Thread(()->{
            try {
                sharedData.clientStub.removeEntry(entry.getId());
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        adapter.entries.remove(entry);
        for (Entry e: adapter.entries) {
            if (e.getPosition() > entry.getPosition())
                e.setPosition(e.getPosition() - 1);
            if (e.getList() != null && entry.getList() != null &&
                    e.getList().equals(entry.getList()) &&
                    e.getListPosition() > entry.getListPosition())
                e.setListPosition(e.getListPosition() - 1);
        }
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry1
     * @param entry2
     */
    public static void swap(SharedData sharedData, TaskListAdapter adapter, Entry entry1, Entry entry2) {
        Thread thread = new Thread(() -> {

            //swap position in Database
            try {
                sharedData.clientStub.swapEntries(entry1.getId(), entry2.getPosition());
            } catch (PositionOutOfBoundException | ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        //get positions of items
        int pos1 = getPosition(adapter.entries, entry1.getId());
        int pos2 = getPosition(adapter.entries, entry2.getId());

        //swap items in entries
        Collections.swap(adapter.entries, pos1, pos2);

        //swap position in both Entries
        int posTemp = entry1.getPosition();
        entry1.setPosition(entry2.getPosition());
        entry2.setPosition(posTemp);

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry1
     * @param entry2
     */
    public static void swapLists(SharedData sharedData, TaskListAdapter adapter, Entry entry1, Entry entry2) {

        Thread thread = new Thread(() -> {
            //swap position in Database
            try {
                sharedData.clientStub.swapListEntries(entry1.getList(), entry1.getId(), entry2.getListPosition());
            } catch (PositionOutOfBoundException | ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        //get positions of items
        int pos1 = getPosition(adapter.entries, entry1.getId());
        int pos2 = getPosition(adapter.entries, entry2.getId());

        //swap items in entries
        Collections.swap(adapter.entries, pos1, pos2);

        //swap position in both Entries
        int posTemp = entry1.getListPosition();
        entry1.setListPosition(entry2.getListPosition());
        entry2.setListPosition(posTemp);
    }

    //Get position of entry by id, returns -1 if id not found
    private static int getPosition(List<Entry> entries, long id) {

        //loop through entries and return position if id matches
        for (int i = 0; i < entries.size(); i++) {

            if (entries.get(i).getId() == id) {
                return i;

            }
        }

        return -1;
    }


    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry
     * @param newTask
     */
    public static void setTask(SharedData sharedData, Entry entry, String newTask) {
        if (entry==null || entry.getTask().equals(newTask))
            return;

        Thread thread = new Thread(()-> {
            try {
                sharedData.clientStub.updateTask(entry.getId(), newTask);
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        entry.setTask(newTask);
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry
     * @param focus
     */
    public static void setFocus(SharedData sharedData, Entry entry, Boolean focus) {
        if (entry==null || entry.getFocus() == focus)
            return;

        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.updateFocus(entry.getId(),  focus);
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();

        entry.setFocus(focus);

        if (entry.getDropped())
            setDropped(sharedData, entry, false);

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry
     * @param dropped
     */
    public static void setDropped(SharedData sharedData, Entry entry, Boolean dropped) {
        if (entry == null || entry.getDropped() == dropped)
            return;

        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.updateDropped(entry.getId(), dropped);
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        entry.setDropped(dropped);
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry
     * @param date
     */
    public static void editReminderDate(SharedData sharedData, Entry entry, Instant date) {
        if (entry == null || entry.getReminderDate() != null && entry.getReminderDate().equals(date))
            return;

        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.updateReminderDate(entry.getId(), date);
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        if (entry.getDropped() && entry.getReminderDate() != date)
            setDropped(sharedData, entry, false);

        entry.setReminderDate(date);
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry
     * @param recurrence
     */
    public static void editRecurrence(SharedData sharedData, Entry entry, String recurrence) {
        if (entry == null || entry.getRecurrence() != null && entry.getRecurrence().equals(recurrence))
            return;

        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.updateRecurrence(entry.getId(), recurrence);
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        entry.setRecurrence(recurrence);
        if (entry.getReminderDate() == null)
            entry.setReminderDate(Instant.now());

        if (entry.getDropped() && recurrence != null)
            setDropped(sharedData, entry, false);
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry
     * @param list
     */
    public static void editList(SharedData sharedData, TaskListAdapter adapter, Entry entry, String list) {
        Optional<TaskList> taskList = sharedData.clientStub.getListByName(list);
        if (taskList.isEmpty())
            return;
        if (entry == null || entry.getList() != null && entry.getList().equals(taskList.get().getId()))
            return;

        if (entry.getList() != null) {
            for (Entry e : adapter.entries) {
                if (entry.getList().equals(e.getList()) && e.getListPosition() > entry.getListPosition())
                    e.setListPosition(e.getListPosition()-1);
            }
        }


        Thread thread = new Thread(() -> sharedData.database.getListManager().updateList(entry, taskList.get().getId()));
        thread.start();

        entry.setList(taskList.get().getId());

        if (entry.getDropped())
            setDropped(sharedData, entry, false);

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param list
     * @param color
     */
    public static void editListColor(SharedData sharedData, long list, String color) {
        if (listColors == null) {

            listColors = sharedData.clientStub.getListColors();
        }

        listColors.put(list, color);

        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.updateListColor(list, color);
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param list
     * @return
     */
    public static String getListColor(SharedData sharedData, long list){

        if (listColors == null){
                listColors = sharedData.clientStub.getListColors();
        }

        String color = listColors.get(list);
        return color == null ? ListTaskListAdapter.DEFAULT_COLOR : color;
    }

    public static List<Entry> getDropped(SharedData sharedData) {
        return sharedData.clientStub.loadDropped();
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @return
     */
    public static List<TaskList> getLists(SharedData sharedData){


        List<TaskList> lists = sharedData.clientStub.loadLists();

        lists.add(new TaskList(-1, "ALL TASKS", null));
        lists.add(new TaskList(-1, "No List", null));

        return lists;
    }

    public static List<String> getListsAsString(SharedData sharedData){

        List<String> lists = new ArrayList<>(getLists(sharedData).stream().map(TaskList::getName).toList());
        return lists;
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entry
     * @param today
     */
    public static void setRecurring(SharedData sharedData, Entry entry, LocalDate today) {

        //get recurrence as <['d'/'w'/'m'/'y']['0'-'9']['0'-'9']['0'-'9']...> (i.e. "d15" means the task repeats every 15 days)
        String recurrence = entry.getRecurrence();

        //get first character representing the mode of repetition (days, weeks,...)
        char mode = recurrence.charAt(0);

        //StringBuilder to store offset
        StringBuilder offSetStr = new StringBuilder();

        //get all digits of offset
        for (int i = 1; i < recurrence.length(); i++) {
            offSetStr.append(recurrence.charAt(i));
        }

        //convert offset to Integer
        int offSet = Integer.parseInt(offSetStr.toString());

        //get reminder date
        Instant date = entry.getReminderDate();

        Instant todayInstant = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();

        if (date == null) {
            date = todayInstant;
        }

        if (date == null)
            throw new DateTimeException("The provided date could not be converted to Instant.");

        //increment date by offset and repeat until it is greater than today
        while (!date.isAfter(todayInstant)) {
            date = incrementRecurring(mode, date, offSet);
        }

        //write reminder date to entries
        entry.setReminderDate(date);

        //write reminder date to Database
        Instant finalDate = date;
        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.updateReminderDate(entry.getId(), finalDate);
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

    }

    //return date incremented by offset
    private static Instant incrementRecurring(char mode, Instant date, int offSet) {

        ZonedDateTime zonedDateTime = date.atZone(ZoneId.systemDefault());
        return switch (mode) {
            case 'd' -> zonedDateTime.plusDays(offSet).toInstant();
            case 'w' -> zonedDateTime.plusWeeks(offSet).toInstant();
            case 'm' -> zonedDateTime.plusMonths(offSet).toInstant();
            case 'y' -> zonedDateTime.plusYears(offSet).toInstant();
            default -> date;
        };

    }




    //add ids to recurringButRemovedFromToday and make a call to the database making changes permanent
    public static void addToRecurringButRemoved(SharedData sharedData, long id){

        if (recurringButRemovedFromToday == null)
            recurringButRemovedFromToday = sharedData.database.getEntryManager().loadRecurring();
        recurringButRemovedFromToday.add(id);

        sharedData.database.getEntryManager().saveRecurring(recurringButRemovedFromToday);

    }

    //remove ids from recurringButRemovedFromToday and make a call to the database making changes permanent
    public static void removeFromRecurringButRemoved(SharedData sharedData, long id){
        if (recurringButRemovedFromToday == null)
            recurringButRemovedFromToday = sharedData.database.getEntryManager().loadRecurring();
        for (int i = 0; i < recurringButRemovedFromToday.size(); i++)
            if (recurringButRemovedFromToday.get(i)==id) {
                recurringButRemovedFromToday.remove(i);
                break;
            }

        sharedData.database.getEntryManager().saveRecurring(recurringButRemovedFromToday);

    }

    public static List<Entry> getFocus(SharedData sharedData) {
        return sharedData.clientStub.loadFocus();
    }

    public static List<Entry> getTasksToPick(SharedData sharedData) {
        return sharedData.database.getEntryManager().loadTasksToPick();
    }
}