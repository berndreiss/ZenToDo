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

    private final String delimiter = "------";    //Delimiter for fields of entries in save file
    private final String lineDelimiter = "%%%%%%%"; //Delimiter for entries in save file

    private final ArrayList<Entry> entries = new ArrayList<>(); //list of all current tasks, which are also always present in the save file
    private final ArrayList<Entry> dropped = new ArrayList<>();
    private final ArrayList<Entry> focus = new ArrayList<>();
    private final ArrayList<String> listNames = new ArrayList<>();
    private final ArrayList<Pair<String,ArrayList<Entry>>> lists = new ArrayList<>();
    private int id; //running id, which is initialized at 0 upon loading and incremented by one for each task
    private final Context context;

    public SaveFile saveFile;//TODO reset to private

    public Data(Context context){
        //initialize instance of Data, set id to 0, create save file and load data from save file
        this.id=0;
        this.context=context;
        this.saveFile = new SaveFile(context);
        this.load();

    }


    public void save(){
        //saves all entries in this.entries to save file
        StringBuilder text = new StringBuilder();

        for (Entry e : entries){//gets all the fields of every entry except for id, which is generated programmatically upon loading

            text.append(e.getTask()).append(delimiter).append(e.getFocus()).append(delimiter).append(e.getDropped()).append(delimiter).append(e.getList()).append(delimiter).append(e.getListPosition()).append(delimiter).append(e.getDue()).append(delimiter).append(e.getRecurrence()).append(lineDelimiter);
        }

        saveFile.save(text.toString()); //Writes contents to file

    }

    public void load(){
        String data = saveFile.load();
        String[] lines = data.split(lineDelimiter);

        for (String line : lines) {//loop through lines to retrieve fields for entry
            String[] fields = line.split(delimiter);
            int fieldsLength = fields.length;

            if (fieldsLength == 7) {//loop through fields of entry and add them to this.entries
                Entry entry = new Entry(generateId(),//generate id
                                        fields[0],//task
                                        Boolean.parseBoolean(fields[1]),//isToday
                                        Boolean.parseBoolean(fields[2]),//isToday
                                        fields[3], //list
                                        Integer.parseInt(fields[4]),//listPosition
                                        Integer.parseInt(fields[5]),//due
                                        fields[6]);//recurrence
                entries.add(entry);//add entry
            }
        }

        initDropped();
        initFocus();
        initLists();

    }

    public void add(String task){
        Entry entry = new Entry(generateId(),task,false,true, " ", -1, 0," "); //generate ID and create entry
        entries.add(entry); //add entry to this.entries
        initDropped();
        save(); //write changes to save file
    }


    public void add(String task, int position){
        //add new entry to database
        Entry entry = new Entry(generateId(),task,false,true, " ", -1, 0," "); //generate ID and create entry
        entries.add(entry); //add entry to this.entries
        initDropped();
        save(); //write changes to save file
    }

    public void remove(int id){
        //remove entry from database
        entries.remove(getPosition(id));
        initDropped();
        initFocus();
        initLists();
        save(); //write changes to save file

    }



    public void editTask(int id, String newTask){
        entries.get(getPosition(id)).setTask(newTask);
        save();
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
        save();

    }

    public void setDropped(int id, Boolean dropped){
        entries.get(getPosition(id)).setDropped(dropped);
        initDropped();
        initFocus();
        save();

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
        save();
    }

    public void editRecurrence(int id, String recurrence){
        entries.get(getPosition(id)).setRecurrence(recurrence);
        save();
    }

    public void editList(int id, String list){
        entries.get(getPosition(id)).setList(list);
        initLists();
        save();
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

    public void initLists(){

        listNames.clear();
        lists.clear();

        for(Entry e : entries){
            String list = e.getList();

            if (!list.equals(" ")) {

                if (!listNames.contains(list)) {

                    listNames.add(list);
                    ArrayList<Entry> newList = new ArrayList<>();
                    newList.add(e);
                    Pair newPair = new Pair(list, newList);
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

    public ArrayList<Entry> getTasksToPick(){
        //return a list of all entries for which the due date is <=today

        ArrayList<Entry> potentials = new ArrayList<>();//create new list

        int date = getToday();//get current date as "yyyyMMdd"

        for (Entry e : entries){//loop through this.entries

            if (e.getFocus()){
                potentials.add(e);
            } else {
                if (e.getDue() == 0) {
                    if (e.getDropped() || e.getList().equals(" ")) {
                        potentials.add(e);
                    }
                } else {
                    if (e.getDue() <= date) {//add entry if it is due
                        potentials.add(e);
                    }
                }
            }

        }
        return potentials;

    }

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
        id += 1;
        return id-1;
    }

    public int getToday(){
        //returns current date as "yyyyMMdd"

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        Date date = new Date();

        return Integer.parseInt(df.format(date));


    }

    //the following functions simply return the associated lists
    public ArrayList<Entry> getEntries(){

        return entries;

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



}