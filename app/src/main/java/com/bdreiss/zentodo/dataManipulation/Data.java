package com.bdreiss.zentodo.dataManipulation;
import android.content.Context;

import java.util.Collections;
import java.util.List;
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

    private final String saveFilePath = "Data"; //Path for save file
    private final String delimiter = "------";    //Delimiter for fields of entries in save file
    private final String lineDelimiter = "%%%%%%%"; //Delimiter for entries in save file

    private ArrayList<Entry> entries = new ArrayList<>(); //list of all current tasks, which are also always present in the save file
    private ArrayList<Entry> dropped = new ArrayList<>();
    private ArrayList<Entry> todays = new ArrayList<>();
    private int id; //running id, which is initialized at 0 upon loading and incremented by one for each task

    public SaveFile saveFile;//TODO reset to private


    public Data(Context context){
        //initialize instance of Data, set id to 0, create save file and load data from save file
        this.id=0;
        this.saveFile = new SaveFile(context, this.saveFilePath);
        this.load();

    }


    public void save(){
        //saves all entries in this.entries to save file
        StringBuilder text = new StringBuilder();

        for (Entry e : entries){//gets all the fields of every entry except for id, which is generated programmatically upon loading

            text.append(e.getTask()).append(delimiter).append(e.getToday()).append(delimiter).append(e.getDropped()).append(delimiter).append(e.getList()).append(delimiter).append(e.getListPosition()).append(delimiter).append(e.getDue()).append(delimiter).append(e.getRecurrence()).append(lineDelimiter);
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
        initTodays();

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
        initTodays();
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

    public void setToday(int id, Boolean today){
        entries.get(getPosition(id)).setToday(today);
        initTodays();
        save();

    }

    public void setDropped(int id, Boolean dropped){
        entries.get(getPosition(id)).setDropped(dropped);
        initDropped();
        initTodays();
        save();

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

    public ArrayList<String> getLists(){

        ArrayList<String> lists = new ArrayList<>();

        for(Entry e : entries){
            String list = e.getList();

            if (!list.equals(" ") && !lists.contains(list)){
                lists.add(list);
            }

        }
        Collections.sort(lists);

        return lists;
    }

    public ArrayList<Entry> getPotentials(){
        //return a list of all entries for which the due date is <=today

        ArrayList<Entry> potentials = new ArrayList<>();//create new list

        int date = getDate();//get current date as "yyyyMMdd"

        for (Entry e : entries){//loop through this.entries

            if (e.getDue() == 0){
                if (e.getDropped() || e.getList().equals(" ")){
                    potentials.add(e);
                }
            }
            else {
                if (e.getDue() <= date) {//add entry if it is due
                    potentials.add(e);
                }
            }

        }
        return potentials;

    }

    public ArrayList<Entry> getDropped(){

        return dropped;

    }
    public ArrayList<Entry> getTodays(){

        return todays;
    }


    //Generates a running id
    private int generateId(){
        id += 1;
        return id-1;
    }

    public int getDate(){
        //returns current date as "yyyyMMdd"

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

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

    public void initTodays(){
        todays.clear();
        for (Entry e: entries){
            if (e.getToday()){
                todays.add(e);
            }
        }
    }

    //Returns entries as an ArrayList<String> TODO: is this ever used?
    public ArrayList<String> getEntriesAsString(){

        ArrayList<String> items = new ArrayList<>();

        for (Entry e : entries){
            items.add(e.getTask());
        }

        return items;
    }


}