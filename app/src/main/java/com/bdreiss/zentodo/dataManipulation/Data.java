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

        int dataLength = this.entries.size();

        for (int i=0; i<dataLength; i++){//gets all the fields of every entry except for id, which is generated programmatically upon loading
            Entry entry = entries.get(i);
            text.append(entry.getTask()).append(this.delimiter).append(entry.getToday()).append(this.delimiter).append(entry.getList()).append(this.delimiter).append(entry.getListPosition()).append(this.delimiter).append(entry.getDue()).append(this.delimiter).append(entry.getRecurrence()).append(this.lineDelimiter);
        }

        this.saveFile.save(text.toString()); //Writes contents to file

    }

    public void load(){
        String data = this.saveFile.load();
        String[] lines = data.split(this.lineDelimiter);

        for (String line : lines) {//loop through lines to retrieve fields for entry
            String[] fields = line.split(this.delimiter);
            int fieldsLength = fields.length;

            if (fieldsLength == 6) {//loop through fields of entry and add them to this.entries
                Entry entry = new Entry(generateId(),//generate id
                                        fields[0],//task
                                        Boolean.parseBoolean(fields[1]),//isToday
                                        fields[2], //list
                                        Integer.parseInt(fields[3]),//listPosition
                                        Integer.parseInt(fields[4]),//due
                                        fields[5]);//recurrence
                this.entries.add(entry);//add entry
            }
        }

    }

    public void add(String task){
        Entry entry = new Entry(generateId(),task,false, " ", -1, 0," "); //generate ID and create entry
        this.entries.add(entry); //add entry to this.entries
        this.save(); //write changes to save file
    }


    public void add(String task, int position){
        //add new entry to database
        Entry entry = new Entry(generateId(),task,false, " ", -1, 0," "); //generate ID and create entry
        this.entries.add(entry); //add entry to this.entries
        this.save(); //write changes to save file
    }

    public void remove(int id){
        //remove entry from database
        this.entries.remove(getPosition(id));

        this.save(); //write changes to save file
    }

    public void editTask(int id, String newTask){
        this.entries.get(getPosition(id)).setTask(newTask);
        this.save();
    }

    public int getPosition(int id){
        int dataLength = this.entries.size();

        for (int i=0;i<dataLength;i++){

            if (this.entries.get(i).getId() == id){
                return i;


            }
        }
        return -1;

    }

    public void setToday(int id, Boolean today){
        this.entries.get(getPosition(id)).setToday(today);
        this.save();

    }

    public void editDue(int id, int date){
        this.entries.get(getPosition(id)).setDue(date);
        this.save();
    }

    public void editRecurrence(int id, String recurrence){
        this.entries.get(getPosition(id)).setRecurrence(recurrence);
        this.save();
    }

    public void editList(int id, String list){
        this.entries.get(getPosition(id)).setList(list);
        this.save();
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

        for(int i=0;i<entries.size();i++){
            String list = entries.get(i).getList();

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

        int dataLength = this.entries.size();

        int date = getDate();//get current date as "yyyyMMdd"

        for (int i=0;i<dataLength;i++){//loop through this.entries

            if (this.entries.get(i).getDue() <= date){//add entry if it is due
                potentials.add(this.entries.get(i));
            }

        }
        return potentials;

    }

    //Generates a running id
    private int generateId(){
        this.id += 1;
        return this.id-1;
    }

    public int getDate(){
        //returns current date as "yyyyMMdd"

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        Date date = new Date();

        return Integer.parseInt(df.format(date));


    }

    //the following functions simply return the associated lists
    public ArrayList<Entry> getEntries(){

        return this.entries;

    }

    //Returns entries as an ArrayList<String> TODO: is this ever used?
    public ArrayList<String> getEntriesAsString(){

        ArrayList<String> items = new ArrayList<>();
        int dataLength = this.entries.size();

        for (int i=0; i<dataLength;i++){
            items.add(this.entries.get(i).getTask());
        }

        return items;
    }


}