package com.bdreiss.zentodo.dataManipulation;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;

import com.bdreiss.zentodo.R;

import java.util.Collections;
import java.util.ArrayList;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Data{
/*
*   Creates an instance of all relevant data, stored in several lists
*   Data manipulation includes:
*       -saving (save()) and loading (load()) data,
*       -adding (add(String task,String list,int due)) and removing entries
*       -returning tasks that are due (getPotentials()) and updating todays tasks (setTodays(List<Entry> todays))
*/

    protected static final ArrayList<Entry> entries = new ArrayList<>(); //list of all current tasks, which are also always present in the save file
    protected static final ArrayList<Entry> entriesOrderedByDue = new ArrayList<>();
    protected static final ArrayList<Entry> dropped = new ArrayList<>();
    protected static final ArrayList<Entry> focus = new ArrayList<>();
    protected static final ArrayList<Entry> tasksToPick = new ArrayList<>();
    protected static final ArrayList<String> listNames = new ArrayList<>();
    protected static final ArrayList<Pair<String,ArrayList<Entry>>> lists = new ArrayList<>();
    protected static ArrayList<Integer> ids = new ArrayList<>();

    private final Context context;

    private final DbHelper db;

    public Data(Context context){
        //initialize instance of Data, set id to 0, create save file and load data from save file
        this.context=context;
        this.db = new DbHelper(context);
        this.load();

    }

    public void load(){
        ArrayList<Entry> newEntries = db.loadEntries();
        entries.clear();

        for (Entry e: newEntries)
            ids.add(e.getId());

        Collections.sort(ids);

        entries.addAll(newEntries);

        initDropped();
        initFocus();
        initLists();
        initTasksToPick();
        initOrderedByDue();
    }

    public void add(String task){
        Entry entry = new Entry(generateId(),task); //generate ID and create entry

        entries.add(entry); //add entry to this.entries
        dropped.add(entry);//add entry to this.dropped
        tasksToPick.add(entry);
        db.addEntry(entry); //write changes to save file
        initOrderedByDue();
    }

    public void remove(int id){
        //remove entry from database
        entries.remove(getPosition(id));
        for (int i = 0; i < ids.size();i++){
            if (ids.get(i) == id){
                ids.remove(i);
                break;
            }
        }
        initDropped();
        initFocus();
        initLists();
        initTasksToPick();
        initOrderedByDue();
        db.removeEntry(id);
    }

    public void swap(int i, int j){
        Collections.swap(entries,i,j);
        initDropped();
        initFocus();
        initLists();
        initTasksToPick();
        initOrderedByDue();
    }

    public void editTask(int id, String newTask){
        entries.get(getPosition(id)).setTask(newTask);
        db.updateEntry(DbHelper.TASK_COL,id, newTask);
    }

    //Get position of entry by id, returns -1 if id not found
    public int getPosition(int id){

        for (int i=0;i<entries.size();i++){

            if (entries.get(i).getId() == id){
                return i;

            }
        }
        return -1;

    }

    /* the following functions edit different fields of entries by their id */

    public void setFocus(int id, Boolean focus){
        entries.get(getPosition(id)).setFocus(focus);
        initFocus();
        db.updateEntry(DbHelper.FOCUS_COL, id, DbHelper.boolToInt(focus));

    }

    public void setDropped(int id, Boolean dropped){
        entries.get(getPosition(id)).setDropped(dropped);
        initDropped();
        db.updateEntry(DbHelper.DROPPED_COL, id, DbHelper.boolToInt(dropped));

    }

    public void setRecurring(int id){
        Entry entry = entries.get(getPosition(id));

        String recurrence = entry.getRecurrence();
        char mode = recurrence.charAt(0);
        StringBuilder offSetStr = new StringBuilder();
        offSetStr.append(recurrence.charAt(1));

        if (recurrence.length() > 2) {

            for (int i = 2;i<recurrence.length();i++){
                offSetStr.append(recurrence.charAt(i));
            }
        }

        int offSet = Integer.parseInt(offSetStr.toString());

        int date = entry.getDue();

        int today = getToday();

        if (date == 0){
            date = today;
        }

        int[] dateArray = new int[3];
        dateArray[0] = date%100;
        dateArray[1] = ((date-dateArray[0])/100)%100;
        dateArray[2] = ((date-dateArray[1]*100-dateArray[0])/10000);


        while (date <= today){
            date = incrementRecurring(mode, dateArray, offSet);
        }

        entry.setDue(date);
        initOrderedByDue();
    }

    private int incrementRecurring(char mode, int[] dateArray,int offSet){


        if (mode =='d' || mode =='w'){

            if (mode == 'w'){
                dateArray[0] += offSet * 7;

            }else {
                dateArray[0] += offSet;
            }

            int daysOfTheMonth = returnDaysOfTheMonth(dateArray[1],dateArray[2]);
            while (daysOfTheMonth < dateArray[0]){
                dateArray[0] -= daysOfTheMonth;
                dateArray[1]++;

                if (dateArray[1] > 12){
                    dateArray[2]++;
                    dateArray[1] -= 12;
                }
                daysOfTheMonth = returnDaysOfTheMonth(dateArray[1],dateArray[2]);
            }

        }

        if (mode == 'm'){
            dateArray[1] += offSet;
            if (dateArray[1] > 12){
                dateArray[2]++;
                dateArray[1] -= 12;
            }

        }

        if (mode == 'y'){

            dateArray[2]+=offSet;

        }


        return dateArray[0] + dateArray[1]*100 + dateArray[2]*10000;
    }

    private int returnDaysOfTheMonth(int month,int year){
        switch (month){
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 2: return isLeapYear(year) ? 29 : 28;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            default: return 0;
        }


    }

    private Boolean isLeapYear(int year){
        if (year%4 == 0){
            return year % 400 != 0;
        }
        return false;
    }

    public void editDue(int id, int date){
        entries.get(getPosition(id)).setDue(date);
        initOrderedByDue();
        db.updateEntry(DbHelper.DUE_COL, id, date);
    }

    public void editRecurrence(int id, String recurrence){
        entries.get(getPosition(id)).setRecurrence(recurrence);
        db.updateEntry(DbHelper.RECURRENCE_COL, id, recurrence);
    }

    public void editList(int id, String list){
        entries.get(getPosition(id)).setList(list);
        initLists();
        db.updateEntry(DbHelper.LIST_COL, id, list);
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
        return listNames;
    }

    private void initLists(){

        listNames.clear();
        lists.clear();

        for(Entry e : entries){
            String list = e.getList();

            if (list!=null) {

                if (!listNames.contains(list)) {

                    listNames.add(list);
                    ArrayList<Entry> newList = new ArrayList<>();
                    newList.add(e);
                    Pair<String,ArrayList<Entry>> newPair = new Pair<>(list, newList);
                    lists.add(newPair);
                } else{

                    for (Pair<String,ArrayList<Entry>> p : lists){
                        if (p.first.equals(list)){
                            p.second.add(e);
                        }
                    }
                }
            }
        }
        Collections.sort(listNames);

        listNames.add(context.getResources().getString(R.string.allTasks));
    }

    private void initTasksToPick(){
        //return a list of all entries for which the due date is <=today

        tasksToPick.clear();

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

    }

    public ArrayList<Entry> getTasksToPick(){return tasksToPick;}

    public ArrayList<Entry> getFromList(String list){

        ArrayList<Entry> listArray = new ArrayList<>();
        for (Pair<String,ArrayList<Entry>> p: lists){
            if (p.first.equals(list)){
                return p.second;
            }
        }

        return listArray;
    }

    public ArrayList<Entry> getDropped(){

        return dropped;

    }
    public ArrayList<Entry> getFocus(){

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
    
    public ArrayList<Entry> getEntriesOrderedByDate(){

        return entriesOrderedByDue;

    }

    public void initDropped(){
        dropped.clear();
        for (Entry e : entries){
            if(e.getDropped()){
                dropped.add(e);
            }
        }

    }

    public void initFocus(){
        focus.clear();
        for (Entry e: entries){
            if (e.getFocus()){
                focus.add(e);
            }
        }
    }

    public void initOrderedByDue(){
        entriesOrderedByDue.clear();
        entriesOrderedByDue.addAll(entries);
        MergeSortByDue sort = new MergeSortByDue(entriesOrderedByDue);

        sort.sort();


    }


}