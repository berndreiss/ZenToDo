package com.bdreiss.zentodo.dataManipulation;
import android.annotation.SuppressLint;
import android.content.Context;

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
     *   Creates an instance of all relevant data, stored in an ArrayList as Entry (see Entry.java).
     *
     *   Methods serve to manipulate and retrieve data as well as communicating with the Database.
     *
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
                incrementListPositionCount(e.getList());
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
            decrementListPositionCount(entry.getList(),entry.getListPosition());

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
        Entry entry = entries.get(getPosition(id));
        entry.setFocus(focus);
        db.updateEntry(DbHelper.getFocusCol(), id, DbHelper.boolToInt(focus));
        if (entry.getDropped())
            setDropped(id, false);

    }

    public void setDropped(int id, Boolean dropped) {
        entries.get(getPosition(id)).setDropped(dropped);
        db.updateEntry(DbHelper.getDroppedCol(), id, DbHelper.boolToInt(dropped));

    }

    public void editDue(int id, int date) {
        Entry entry = entries.get(getPosition(id));
        entry.setDue(date);
        db.updateEntry(DbHelper.getDueCol(), id, date);

        if (entry.getDropped())
            setDropped(id, false);

    }

    public void editRecurrence(int id, String recurrence) {
        entries.get(getPosition(id)).setRecurrence(recurrence);
        db.updateEntry(DbHelper.getRecurrenceCol(), id, recurrence);
    }

    public int editList(int id, String list) {
        Entry entry = entries.get(getPosition(id));

        //if there was a list before and it was different to the new one, decrement the count of items in this list
        // (this counter is used to keep track of list positions -> see Data.java)
        if (entry.getList() != null && !list.equals(entry.getList()))
            decrementListPositionCount(entry.getList(), entries.get(getPosition(id)).getListPosition());

        entry.setList(list);

        entry.setListPosition(incrementListPositionCount(list));

        db.updateEntry(DbHelper.getListCol(), id, list);
        db.updateEntry(DbHelper.getListPositionCol(), id, entry.getListPosition());

        if (entry.getDropped())
            setDropped(id, false);

        return entry.getListPosition();
    }

    //function increments position counter of list and returns a new position
    private int incrementListPositionCount(String list) {

        //if no list is passed listPosition is -1 by default
        if (list == null)
            return -1;

        //if list is in listPositionCount get current position, save and return incremented position
        //return position 0 otherwise
        if (listPositionCount.get(list) != null) {

            //get current position
            @SuppressWarnings("ConstantConditions") int pos = listPositionCount.get(list);//get(list) != null is checked for

            //increment position
            listPositionCount.put(list, pos + 1);

            //return incremented position
            return pos + 1;
        } else {
            //put new list
            listPositionCount.put(list, 0);

            //return position 0
            return 0;
        }
    }

    //decrement listPositionCount and synchronize listPositions in entries
    public void decrementListPositionCount(String list, int currPosition){

        //get current position
        @SuppressWarnings("ConstantConditions") int position = listPositionCount.get(list); //since method call comes from an item that is in a list get(list) cannot be null

        //if position is 0 the last item has been removed and therefore the list must be removed from the map
        //decrement position counter otherwise
        if (position == 0)
            listPositionCount.remove(list);
        else
            listPositionCount.put(list,position-1);


        //in all entries see if listPosition was bigger than position of removed item, if so decrement
        //this is done to preserve the sequential order of the list, which is important when new items are added to it
        //if this wasn't done items could have the same listPosition
        // (i.e. <0,1,2>: counter is 2  -> 0 is removed -> <1,2>: counter is 1 -> new item is added with incremented counter -> <1,2,2>)
        for (Entry e : entries){

            //only get items with according list and larger listPosition
            if (e.getList() != null && e.getList().equals(list) && e.getListPosition() > currPosition){

                //decrement position
                int newPosition = e.getListPosition()-1;

                //write to Entry and Database
                e.setListPosition(newPosition);
                db.updateEntry(DbHelper.getListPositionCol(),e.getId(),newPosition);
            }

        }

    }

    //returns lists as an Array: is used for the autocomplete when editing list
    public String[] returnListsAsArray(){

        //get lists and initialize array
        ArrayList<String> lists = getLists();
        String[] listsAsArray = new String[lists.size()];

        //copy to array
        for (int i=0;i<lists.size();i++){
            listsAsArray[i] = lists.get(i);
        }

        return listsAsArray;

    }

    //return an ArrayList of all lists
    public ArrayList<String> getLists() {

        //ArrayList to be returned
        ArrayList<String> lists = new ArrayList<>();

        //for all entries add list to ArrayList
        for (Entry e : entries){

            //if there is a list assigned check if it is in lists already, add otherwise
            if (e.getList() != null && !e.getList().isEmpty()){
                if (!lists.contains(e.getList()))
                    lists.add(e.getList());
            }
        }

        //sort lists and add noList and allTasks tokens, these are elements that are always
        //represented in the list of lists so that users can find tasks and edit them easily
        Collections.sort(lists);
        lists.add(context.getResources().getString(R.string.noList));
        lists.add(context.getResources().getString(R.string.allTasks));

        return lists;
    }

    //get entries that can be picked today. These include tasks that are due, tasks that have been dropped,
    //or tasks that have already been in focus-
    public ArrayList<Entry> getTasksToPick(){

        ArrayList<Entry> tasksToPick= new ArrayList<>();

        //get current date as "yyyyMMdd" to check if task is due
        int date = getToday();


        /*
         *  for all entries:
         *
         *  1. check if task is in focus, if yes -> add
         *
         *
         *  2. else check if task has a date
         *
         *      2.1 if yes and it has been dropped or no list is set -> add
         *          (the list part is necessary because tasks might have been edited and are neither
         *           dropped, focused or have a date. So they would never show up)
         *
         *  3. else check if due date <= today, if yes -> add
        */

        for (Entry e : entries){

            //1. check focus
            if (e.getFocus()){
                tasksToPick.add(e);
            } else {

                //2. check date set
                if (e.getDue() == 0) {

                    //2.1 check dropped and list set
                    if (e.getDropped() || e.getList()==null) {
                        tasksToPick.add(e);
                    }
                } else {

                    //3. check task is due
                    if (e.getDue() <= date) {
                        tasksToPick.add(e);
                    }
                }
            }

        }
        return  tasksToPick;
    }

    //get entries assigned to a certain list
    public ArrayList<Entry> getList(String list){

        ArrayList<Entry> listArray = new ArrayList<>();

        //add all entries that match the list passed
        for (Entry e : entries){

            if (e.getList() != null && e.getList().equals(list)){

                listArray.add(e);

            }
        }

        //sort list by field entry.listPosition
        MergeSortByListPosition sort = new MergeSortByListPosition(listArray);
        sort.sort();

        return listArray;
    }

    //get entries where dropped == true
    public ArrayList<Entry> getDropped(){

        ArrayList<Entry> dropped = new ArrayList<>();

        for (Entry e : entries){
            if (e.getDropped())
                dropped.add(e);

        }
        return dropped;

    }

    //get entries where focus == true
    public ArrayList<Entry> getFocus(){
        ArrayList<Entry> focus = new ArrayList<>();

        for (Entry e : entries){

            if (e.getFocus())
                focus.add(e);

        }
        return focus;
    }


    //generates a running id: basically looks through ids and gets the next free number
    //i.e.: <0,1,2>: next id is 3 -> 1 is removed -> <0,2>: next id is 1
    private int generateId(){

        //keeping track
        int id = 0;

        //if id is in ids, increment, assign id otherwise
        //id increments in parallel as we loop through ids, since ids are sorted both numbers grow the same
        //if a number is missing in ids the two numbers don't match anymore and the new id is found
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
        //if new "holes" where found in ids assign new id as (last id in ids) + 1
        ids.add(id);
        Collections.sort(ids);
        return id;
    }

    //return current date as "yyyyMMdd"
    public int getToday(){

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        Date date = new Date();

        return Integer.parseInt(df.format(date));

    }

    //return entries that have no list assigned
    public ArrayList<Entry> getNoList(){
        ArrayList<Entry> noList = new ArrayList<>();

        //get all items with no list
        for (Entry e : entries){
            if (e.getList() == null)
                noList.add(e);
        }

        //sort tasks by their field entry.getDue()
        MergeSortByDue sort = new MergeSortByDue(noList);
        sort.sort();

        return noList;


    }

    public ArrayList<Entry> getEntries(){

        return entries;

    }

    //return ArrayList of entries ordered by when they are due
    public ArrayList<Entry> getEntriesOrderedByDate(){

        ArrayList<Entry> entriesOrderedByDue = new ArrayList<>(entries);

        //sort entries by the field entry.getDue()
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