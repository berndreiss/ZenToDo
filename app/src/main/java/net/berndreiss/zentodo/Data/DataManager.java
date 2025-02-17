package net.berndreiss.zentodo.Data;
import android.content.Context;

import net.berndreiss.zentodo.adapters.ListTaskListAdapter;

import java.time.LocalDate;
import java.util.Collections;

import java.util.List;
import java.util.Map;

public class DataManager {

    //recurring tasks are automatically added to FOCUS
    //when they are removed however, the ids are stored in this ArrayList and the tasks are not shown until the next day
    //see also: FocusTaskListAdapter
    private static List<Integer> recurringButRemovedFromToday = null;


    public static Map<String, String> listColors = null;

    //adds an Entry to Data and the Database
    public static void add(Context context, List<Entry> entries, String task) {

        try (SQLiteHelper db = new SQLiteHelper(context)) {

            //write changes to database
            entries.add(db.addEntry(task));
        }


    }


    //removes an id from Data and the Database
    public static void remove(Context context, List<Entry> entries, Entry entry) {

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.removeEntry(entry);
        }

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


    //swap entries if item is moved by drag and drop
    public static void swap(Context context, List<Entry> entries, Entry entry1, Entry entry2) {

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            //swap position in Database
            db.swapEntries(entry1, entry2.getPosition());
        }
        //swap items in entries
        Collections.swap(entries, entry1.getPosition(), entry2.getPosition());

        //swap position in both Entries
        int posTemp = entry1.getPosition();
        entry1.setPosition(entry2.getPosition());
        entry2.setPosition(posTemp);

    }

    //swap entries if item is moved by drag and drop
    public static void swapLists(Context context, List<Entry> entries, Entry entry1, Entry entry2) {

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            //swap position in Database
            db.swapListEntries(entry1, entry2.getListPosition());
        }

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
    private static int getPosition(List<Entry> entries, int id) {

        //loop through entries and return position if id matches
        for (int i = 0; i < entries.size(); i++) {

            if (entries.get(i).getId() == id) {
                return i;

            }
        }

        return -1;
    }


    /*
    *   The following functions edit different fields of entries by their id.
    */

    public static void setTask(Context context, Entry entry, String newTask) {
        if (entry==null || entry.getTask().equals(newTask))
            return;

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.updateTask(entry, newTask);
        }
        entry.setTask(newTask);
    }


    public static void setFocus(Context context, Entry entry, Boolean focus) {
        if (entry==null || entry.getFocus() == focus)
            return;

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.updateFocus(entry, focus);
        }

        entry.setFocus(focus);

        if (entry.getDropped())
            setDropped(context, entry, false);

    }


    public static void setDropped(Context context, Entry entry, Boolean dropped) {
        if (entry == null || entry.getDropped() == dropped)
            return;

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.updateDropped(entry, dropped);
        }
        entry.setDropped(dropped);
    }

    public static void editReminderDate(Context context, Entry entry, LocalDate date) {
        if (entry == null || entry.getReminderDate() != null && entry.getReminderDate().equals(date))
            return;

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.updateReminderDate(entry, date);
        }


        if (entry.getDropped() && entry.getReminderDate() != date)
            setDropped(context, entry, false);

        entry.setReminderDate(date);
    }


    public static void editRecurrence(Context context, Entry entry, String recurrence) {
        if (entry == null || entry.getRecurrence() != null && entry.getRecurrence().equals(recurrence))
            return;

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.updateRecurrence(entry, recurrence);
        }

        entry.setRecurrence(recurrence);
        if (entry.getReminderDate() == null)
            entry.setReminderDate(LocalDate.now());

        if (entry.getDropped() && recurrence != null)
            setDropped(context, entry, false);
    }


    public static void editList(Context context, List<Entry> entries, Entry entry, String list) {
        if (entry == null || entry.getList() != null && entry.getList().equals(list))
            return;

        if (entry.getList() != null) {
            for (Entry e : entries) {
                if (entry.getList().equals(e.getList()) && e.getListPosition() > entry.getListPosition())
                    e.setListPosition(e.getListPosition()-1);
            }
        }

        try (SQLiteHelper db = new SQLiteHelper(context)) {

            db.updateList(entry, list);
        }

        entry.setList(list);

        if (entry.getDropped())
            setDropped(context, entry, false);

    }


    public static void editListColor(Context context, String list, String color) {
        if (listColors == null) {
            try (SQLiteHelper db = new SQLiteHelper(context)) {
                listColors = db.getListColors();
            }
        }

        listColors.put(list, color);

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.updateListColor(list, color);
        }
    }

    public static String getListColor(Context context, String list){

        if (listColors == null){
            try (SQLiteHelper db = new SQLiteHelper(context)) {
                listColors = db.getListColors();
            }
        }

        String color = listColors.get(list);
        return color == null ? ListTaskListAdapter.DEFAULT_COLOR : color;
    }

    public static List<Entry> getDropped(Context context) {
        try (SQLiteHelper db = new SQLiteHelper(context)){
            return db.loadDropped();
        }
    }

    public static List<String> getLists(Context context){

        List<String> lists;

        try (SQLiteHelper db = new SQLiteHelper(context)){
            lists = db.getLists();
        }
        lists.add("ALL TASKS");
        lists.add("No list");
        return lists;
    }

    //this routine calculates a new reminder date for recurring tasks
    public static void setRecurring(Context context, Entry entry, LocalDate today) {

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
        LocalDate date = entry.getReminderDate();

        if (date == null) {
            date = today;
        }

        //increment date by offset and repeat until it is greater than today
        while (!date.isAfter(today)) {
            date = incrementRecurring(mode, date, offSet);
        }

        //write reminder date to entries
        entry.setReminderDate(date);

        try(SQLiteHelper db = new SQLiteHelper(context)) {
            //write reminder date to Database
            db.updateReminderDate(entry, date);
        }

    }


    //return date incremented by offset
    private static LocalDate incrementRecurring(char mode, LocalDate date, int offSet) {

        switch(mode){
            case 'd':
                return date.plusDays(offSet);
            case 'w':
                return date.plusWeeks(offSet);
            case 'm':
                return date.plusMonths(offSet);
            case 'y':
                return date.plusYears(offSet);
            default:
                return date;
        }

    }




    //add ids to recurringButRemovedFromToday and make a call to the database making changes permanent
    public static void addToRecurringButRemoved(Context context, int id){

        if (recurringButRemovedFromToday == null)
            try(SQLiteHelper db = new SQLiteHelper(context)){
                recurringButRemovedFromToday = db.loadRecurring();
            }
        recurringButRemovedFromToday.add(id);

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.saveRecurring(recurringButRemovedFromToday);
        }

    }

    //remove ids from recurringButRemovedFromToday and make a call to the database making changes permanent
    public static void removeFromRecurringButRemoved(Context context, int id){
        if (recurringButRemovedFromToday == null)
            try(SQLiteHelper db = new SQLiteHelper(context)){
                recurringButRemovedFromToday = db.loadRecurring();
            }
        for (int i = 0; i < recurringButRemovedFromToday.size(); i++)
            if (recurringButRemovedFromToday.get(i)==id) {
                recurringButRemovedFromToday.remove(i);
                break;
            }

        try (SQLiteHelper db = new SQLiteHelper(context)) {
            db.saveRecurring(recurringButRemovedFromToday);
        }

    }

    public static List<Entry> getFocus(Context context) {
        try(SQLiteHelper db = new SQLiteHelper(context)){
            return db.loadFocus();
        }
    }

    public static List<Entry> getTasksToPick(Context context) {
        try (SQLiteHelper db = new SQLiteHelper(context)){
           return db.loadTasksToPick();
        }
    }
}