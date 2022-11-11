package com.bdreiss.zentodo.dataManipulation;
import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.bdreiss.zentodo.R;
import com.bdreiss.zentodo.dataManipulation.mergeSort.MergeSort;
import com.bdreiss.zentodo.dataManipulation.mergeSort.MergeSortByDue;
import com.bdreiss.zentodo.dataManipulation.mergeSort.MergeSortByListPosition;


import java.util.Collections;
import java.util.ArrayList;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Map;

public class Data {
    /*
     *   Creates an instance of all relevant data, stored in several lists
     *   Data manipulation includes:
     *       -saving (save()) and loading (load()) data,
     *       -adding (add(String task,String list,int due)) and removing entries
     *       -returning tasks that are due (getPotentials()) and updating todays tasks (setTodays(List<Entry> todays))
     */

    protected static final ArrayList<Entry> entries = new ArrayList<>(); //list of all current tasks, which are also always present in the save file
    protected static ArrayList<Integer> ids = new ArrayList<>();//Used to generate new ids

    protected static final Map<String, Integer> listPositionCount = new Hashtable<>();//keeps track of lists and  of number items in list: stores list position (n-1)

    private final Context context;

    private final DbHelper db;//database

    //initialize instance of Data, load content of database into entries, populate ids and listPositionCount
    public Data(Context context) {

        this.context = context;

        //get new Database-handler
        this.db = new DbHelper(context);

        //load Database-content to entries
        entries.addAll(db.loadEntries());

        //loop through all item and get ids, list Names and number of items in list
        for (Entry e : entries) {

            //add id
            ids.add(e.getId());

            //if Entry has a list assigned, increment listPosition in listPositionCount
            if (e.getList() != null && !e.getList().isEmpty()) {

                //increments counter in listPositionCount, puts item if non-existent
                incrementListHash(e.getList());
            }

        }

        //sort ids: when new ids are generated the app loops through the ArrayList and assigns
        // the first available id (see generateId()). Therefore the list has to be sorted
        Collections.sort(ids);

        //sort entries by position
        MergeSort sort = new MergeSort(entries);
        sort.sort();

    }

    //adds an Entry to Data and the Database
    public Entry add(String task) {

        //generate ID and create entry
        Entry entry = new Entry(generateId(), entries.size(), task);

        //add entry to entries
        entries.add(entry);

        //write changes to database
        db.addEntry(entry);

        return entry;
    }

    //removes an id from Data and the Database
    public void remove(int id) {

        //retrieve entry
        Entry entry = entries.get(getPosition(id));

        //if list was assigned decrement counter in listPositionCount
        if (entry.getList() != null)
            decrementListHashPositionCount(entry.getList(),entry.getListPosition());

        //get position of task
        int position = entry.getPosition();

        //for all tasks in entries: if position is higher than that of item, then decrement
        for (Entry e: entries){

            if (e.getPosition()>position){

                //new position of item
                int newPosition = e.getPosition()-1;

                //write new position to Data and Database
                e.setPosition(newPosition);
                db.updateEntry(DbHelper.getPositionCol(),e.getId(),newPosition);
            }

        }

        //remove entry from Data and Database
        entries.remove(getPosition(id));
        db.removeEntry(id);

        //remove id from ids
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == id) {
                ids.remove(i);
                break;
            }
        }

    }

    //swap entries if item is moved by drag and drop
    public void swap(int id1, int id2) {

        //get positions of items
        int pos1 = getPosition(id1);
        int pos2 = getPosition(id2);

        //swap items in entries
        Collections.swap(entries, pos1, pos2);

        //swap position in both Entries
        int posTemp = entries.get(pos1).getPosition();
        entries.get(pos1).setPosition(entries.get(pos2).getPosition());
        entries.get(pos2).setPosition(posTemp);

        //swap position in Database
        db.swapEntries(DbHelper.getPositionCol(), id1, id2);

    }

    //swaps positions in list and synchronizes positions in entries
    public void swapList(int id1, int id2) {

        //get position of items
        int pos1 = getPosition(id1);
        int pos2 = getPosition(id2);

        //get listPositions
        int listPos1 = entries.get(pos1).getListPosition();
        int listPos2 = entries.get(pos2).getListPosition();

        //swap listPosition
        entries.get(pos1).setListPosition(listPos2);
        entries.get(pos2).setListPosition(listPos1);

        //swap listPositions in Database
        db.swapEntries(DbHelper.getListPositionCol(), id1, id2);

        //if relative position of items in entries is different swap them too
        if ((pos1 < pos2 && listPos1 < listPos2) || (pos1 > pos2 && listPos1 > listPos2))
            swap(id1,id2);
    }

    //Get position of entry by id, returns -1 if id not found
    public int getPosition(int id) {

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

    public void setTask(int id, String newTask) {
        entries.get(getPosition(id)).setTask(newTask);
        db.updateEntry(DbHelper.getTaskCol(), id, newTask);
    }


    public void setFocus(int id, Boolean focus) {
        entries.get(getPosition(id)).setFocus(focus);
        db.updateEntry(DbHelper.getFocusCol(), id, DbHelper.boolToInt(focus));

    }

    public void setDropped(int id, Boolean dropped) {
        entries.get(getPosition(id)).setDropped(dropped);
        db.updateEntry(DbHelper.getDroppedCol(), id, DbHelper.boolToInt(dropped));

    }

    public void editDue(int id, int date) {
        entries.get(getPosition(id)).setDue(date);
        db.updateEntry(DbHelper.getDueCol(), id, date);
    }

    public void editRecurrence(int id, String recurrence) {
        entries.get(getPosition(id)).setRecurrence(recurrence);
        db.updateEntry(DbHelper.getRecurrenceCol(), id, recurrence);
    }

    public int editList(int id, String list) {
        Entry entry = entries.get(getPosition(id));
        entry.setList(list);

        entry.setListPosition(incrementListHash(list));

        db.updateEntry(DbHelper.getListCol(), id, list);
        db.updateEntry(DbHelper.getListPositionCol(), id, entry.getListPosition());

        return entry.getListPosition();
    }

    private int incrementListHash(String list) {

        if (list == null)
            return -1;

        if (listPositionCount.get(list) != null) {

            @SuppressWarnings("ConstantConditions") int pos = listPositionCount.get(list);//get(list) != null is checked for
            listPositionCount.put(list, pos + 1);
            return pos + 1;
        } else {
            listPositionCount.put(list, 0);
            return 0;
        }
    }

    public void decrementListHashPositionCount(String list, int currPosition){

        @SuppressWarnings("ConstantConditions") int position = listPositionCount.get(list); //since method call comes from an item that is in a list get(list) cannot be null

        if (position == 0)
            listPositionCount.remove(list);
        else
            listPositionCount.put(list,position-1);

        for (Entry e : entries){

            if (e.getList() != null && e.getList().equals(list) && e.getListPosition() > currPosition){
                int newPosition = e.getListPosition()-1;
                e.setListPosition(newPosition);
                db.updateEntry(DbHelper.getListPositionCol(),e.getId(),newPosition);
            }

        }

    }

    public String[] returnListsAsArray(){

        ArrayList<String> lists = getLists();
        String[] listsAsArray = new String[lists.size()];

        for (int i=0;i<lists.size();i++){
            listsAsArray[i] = lists.get(i);
        }

        return listsAsArray;

    }

    public ArrayList<String> getLists() {
        ArrayList<String> lists = new ArrayList<>();

        for (Entry e : entries){
            if (e.getList() != null && !e.getList().isEmpty()){
                if (!lists.contains(e.getList()))
                    lists.add(e.getList());
            }
        }

        Collections.sort(lists);
        lists.add(context.getResources().getString(R.string.noList));
        lists.add(context.getResources().getString(R.string.allTasks));

        return lists;
    }


    public ArrayList<Entry> getTasksToPick(){

        ArrayList<Entry> tasksToPick= new ArrayList<>();

        int date = getToday();//get current date as "yyyyMMdd"

        for (Entry e : entries){//loop through this.entries

            if (e.getFocus()){
                tasksToPick.add(e);
            } else {
                if (e.getDue() == 0) {
                    if (e.getDropped() || e.getList()==null) {
                        tasksToPick.add(e);
                    }
                } else {
                    if (e.getDue() <= date) {//add entry if it is due
                        tasksToPick.add(e);
                    }
                }
            }

        }
        return  tasksToPick;
    }

    public ArrayList<Entry> getList(String list){

        ArrayList<Entry> listArray = new ArrayList<>();

        for (Entry e : entries){

            if (e.getList() != null && e.getList().equals(list)){

                listArray.add(e);

            }
        }

        MergeSortByListPosition sort = new MergeSortByListPosition(listArray);

        sort.sort();

        return listArray;
    }

    public ArrayList<Entry> getDropped(){

        ArrayList<Entry> dropped = new ArrayList<>();

        for (Entry e : entries){
            if (e.getDropped())
                dropped.add(e);

        }
        return dropped;

    }
    public ArrayList<Entry> getFocus(){
        ArrayList<Entry> focus = new ArrayList<>();

        for (Entry e : entries){

            if (e.getFocus())
                focus.add(e);

        }
        return focus;
    }


    //Generates a running id
    private int generateId(){
        int id = 0;

        for (int i : ids){
            if (i == id){
                id++;
            }
            else{
                ids.add(id);
                Collections.sort(ids);
                return id;
            }
        }
        ids.add(id);
        Collections.sort(ids);
        return id;
    }

    public int getToday(){
        //returns current date as "yyyyMMdd"

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        Date date = new Date();

        return Integer.parseInt(df.format(date));


    }

    public ArrayList<Entry> getNoList(){
        ArrayList<Entry> noList = new ArrayList<>();

        for (Entry e : entries){
            if (e.getList() == null || e.getList().isEmpty())
                noList.add(e);
        }

        MergeSortByDue sort = new MergeSortByDue(noList);
        sort.sort();
        return noList;


    }
    public ArrayList<Entry> getEntriesOrderedByDate(){
        ArrayList<Entry> entriesOrderedByDue = new ArrayList<>(entries);

        MergeSortByDue sort = new MergeSortByDue(entriesOrderedByDue);

        sort.sort();

        return entriesOrderedByDue;

    }


    //this routine calculates a new due date for recurring tasks
    public int setRecurring(int id) {

        //get Entry
        Entry entry = entries.get(getPosition(id));

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

        //get due date
        int date = entry.getDue();

        //get todays date and set due to if it is not set
        int today = getToday();

        if (date == 0) {
            date = today;
        }

        //decode due date and store in array: <[day][month][year]>
        int[] dateArray = new int[3];

        //get day
        dateArray[0] = date % 100;

        //get month
        dateArray[1] = ((date - dateArray[0]) / 100) % 100;

        //get year
        dateArray[2] = ((date - dateArray[1] * 100 - dateArray[0]) / 10000);

        //increment date by offset and repeat until it is greater than today
        while (date <= today) {
            date = incrementRecurring(mode, dateArray, offSet);
        }

        //write due date to entries
        entry.setDue(date);

        //write due date to Database
        db.updateEntry(DbHelper.getDueCol(),id,date);

        return date;
    }

    //return date incremented by offset
    private int incrementRecurring(char mode, int[] dateArray, int offSet) {

        //if day and week ist incremented month and year may be incremented too
        if (mode == 'd' || mode == 'w') {

            //if offset is measured in weeks increment day by offset*7, increment by offSet otherwise
            // (because it is simply measured in days)
            if (mode == 'w') {
                dateArray[0] += offSet * 7;

            } else {
                dateArray[0] += offSet;
            }

            //get days of the current month
            int daysOfTheMonth = returnDaysOfTheMonth(dateArray[1], dateArray[2]);

            //in a loop check if day is bigger than day of the month increment month and even year if necessary
            while (daysOfTheMonth < dateArray[0]) {

                //increment month
                dateArray[1]++;

                //reset day
                dateArray[0] -= daysOfTheMonth;


                //if month is bigger than 12 increment year too
                while (dateArray[1] > 12) {
                    dateArray[2]++;
                    dateArray[1] -= 12;
                }

                //recalculate days of the month for next month
                daysOfTheMonth = returnDaysOfTheMonth(dateArray[1], dateArray[2]);
            }

        }

        //if month is incremented, year may be incremented too
        if (mode == 'm') {

            //increment month by offset
            dateArray[1] += offSet;

            //if month is bigger than 12 increment year too
            while (dateArray[1] > 12) {
                dateArray[2]++;
                dateArray[1] -= 12;
            }

        }

        //if offset is measured in years, just increment years by offset
        if (mode == 'y') {

            dateArray[2] += offSet;

        }

        //return date in format yyyymmdd
        return dateArray[0] + dateArray[1] * 100 + dateArray[2] * 10000;
    }

    //returns days of the month
    private int returnDaysOfTheMonth(int month, int year) {
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 2:
                return isLeapYear(year) ? 29 : 28;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            default:
                return 0;
        }


    }

    //calculates if year is leap year
    private Boolean isLeapYear(int year) {
        if (year % 4 == 0) {
            return year % 400 != 0;
        }
        return false;
    }
}