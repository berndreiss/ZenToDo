package net.berndreiss.zentodo.data;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.test.espresso.remote.EspressoRemoteMessage;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
import net.berndreiss.zentodo.util.ClientStub;
import net.berndreiss.zentodo.util.Status;
import net.berndreiss.zentodo.util.ZenServerMessage;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DataManager {

    //recurring tasks are automatically added to FOCUS
    //when they are removed however, the ids are stored in this ArrayList and the tasks are not shown until the next day
    //see also: FocusTaskListAdapter
    private static List<Long> recurringButRemovedFromToday = null;
    private static ClientStub clientStub;

    /** TODO COMMENT */
    public static Map<String, String> listColors = null;

    public static void initClientStub(SharedData sharedData, String email) throws InterruptedException {

        clientStub = new ClientStub(email, sharedData.database);


        boolean userExists = sharedData.database.getUserByEmail(email) != null;

        Consumer<String> messagePrinter = message -> {
            Looper.prepare();
            new Handler(Looper.getMainLooper()).post(()-> Toast.makeText(sharedData.context, message, Toast.LENGTH_LONG).show());
        };

        clientStub.setMessagePrinter(messagePrinter);
        clientStub.addOperationHandler(sharedData.uiOperationHandler);

        Thread thread = new Thread(() -> {
            clientStub.init(() -> "test");
        });

        thread.start();
        thread.join();

        //for some strange reason Android refuses to establish a connection with the server
        //when the user is created locally for the first time, so we have to init again
        if (!userExists) {
            Log.v("TEST", "THREAD2");
            thread = new Thread(() -> {
                clientStub.init(() -> "test");
            });
            thread.start();
        }

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entries
     * @param task
     */
    public static void add(SharedData sharedData, List<Entry> entries, String task) {


        //write changes to database
        Entry entry = sharedData.database.addEntry(task);
        entries.add(entry);
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
     * @param entries
     * @param entry
     */
    public static void remove(SharedData sharedData, List<Entry> entries, Entry entry) {

            sharedData.database.removeEntry(entry);

        entries.remove(entry);

        for (Entry e: entries){
            if (e.getPosition() > entry.getPosition())
                e.setPosition(e.getPosition()-1);
            if (e.getList() != null && entry.getList() != null &&
                    e.getList().equals(entry.getList()) &&
                    e.getListPosition() > entry.getListPosition())
                e.setListPosition(e.getListPosition()-1);
        }

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entries
     * @param entry1
     * @param entry2
     */
    public static void swap(SharedData sharedData, List<Entry> entries, Entry entry1, Entry entry2) {

            //swap position in Database
            sharedData.database.swapEntries(entry1, entry2.getPosition());
        //get positions of items
        int pos1 = getPosition(entries, entry1.getId());
        int pos2 = getPosition(entries, entry2.getId());

        //swap items in entries
        Collections.swap(entries, pos1, pos2);

        //swap position in both Entries
        int posTemp = entry1.getPosition();
        entry1.setPosition(entry2.getPosition());
        entry2.setPosition(posTemp);

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entries
     * @param entry1
     * @param entry2
     */
    public static void swapLists(SharedData sharedData, List<Entry> entries, Entry entry1, Entry entry2) {

            //swap position in Database
            sharedData.database.swapListEntries(entry1, entry2.getListPosition());

        //get positions of items
        int pos1 = getPosition(entries, entry1.getId());
        int pos2 = getPosition(entries, entry2.getId());

        //swap items in entries
        Collections.swap(entries, pos1, pos2);

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

        sharedData.database.updateTask(entry, newTask);
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

        sharedData.database.updateFocus(entry, focus);

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

        sharedData.database.updateDropped(entry, dropped);
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

        sharedData.database.updateReminderDate(entry, date);

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

        sharedData.database.updateRecurrence(entry, recurrence);

        entry.setRecurrence(recurrence);
        if (entry.getReminderDate() == null)
            entry.setReminderDate(Instant.now());

        if (entry.getDropped() && recurrence != null)
            setDropped(sharedData, entry, false);
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param entries
     * @param entry
     * @param list
     */
    public static void editList(SharedData sharedData, List<Entry> entries, Entry entry, String list) {
        if (entry == null || entry.getList() != null && entry.getList().equals(list))
            return;

        if (entry.getList() != null) {
            for (Entry e : entries) {
                if (entry.getList().equals(e.getList()) && e.getListPosition() > entry.getListPosition())
                    e.setListPosition(e.getListPosition()-1);
            }
        }


        sharedData.database.updateList(entry, list);

        entry.setList(list);

        if (entry.getDropped())
            setDropped(sharedData, entry, false);

    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param list
     * @param color
     */
    public static void editListColor(SharedData sharedData, String list, String color) {
        if (listColors == null) {
            listColors = sharedData.database.getListColors();
        }

        listColors.put(list, color);

        sharedData.database.updateListColor(list, color);
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @param list
     * @return
     */
    public static String getListColor(SharedData sharedData, String list){

        if (listColors == null){
                listColors = sharedData.database.getListColors();
        }

        String color = listColors.get(list);
        return color == null ? ListTaskListAdapter.DEFAULT_COLOR : color;
    }

    public static List<Entry> getDropped(SharedData sharedData) {
        return sharedData.database.loadDropped();
    }

    /**
     * TODO DESCRIBE
     * @param sharedData
     * @return
     */
    public static List<String> getLists(SharedData sharedData){

        List<String> lists;

        lists = sharedData.database.getLists();
        lists.add("ALL TASKS");
        lists.add("No list");
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
        sharedData.database.updateReminderDate(entry, date);

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
            recurringButRemovedFromToday = sharedData.database.loadRecurring();
        recurringButRemovedFromToday.add(id);

        sharedData.database.saveRecurring(recurringButRemovedFromToday);

    }

    //remove ids from recurringButRemovedFromToday and make a call to the database making changes permanent
    public static void removeFromRecurringButRemoved(SharedData sharedData, long id){
        if (recurringButRemovedFromToday == null)
            recurringButRemovedFromToday = sharedData.database.loadRecurring();
        for (int i = 0; i < recurringButRemovedFromToday.size(); i++)
            if (recurringButRemovedFromToday.get(i)==id) {
                recurringButRemovedFromToday.remove(i);
                break;
            }

        sharedData.database.saveRecurring(recurringButRemovedFromToday);

    }

    public static List<Entry> getFocus(SharedData sharedData) {
        return sharedData.database.loadFocus();
    }

    public static List<Entry> getTasksToPick(SharedData sharedData) {
        return sharedData.database.loadTasksToPick();
    }
}