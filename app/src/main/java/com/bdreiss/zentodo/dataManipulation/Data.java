package com.bdreiss.zentodo.dataManipulation;
import android.content.Context;

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
    private ArrayList<Entry> todays = new ArrayList<>(); //list of tasks who are todo today -> set by this.setTodays
    private ArrayList<List<Entry>> lists = new ArrayList<>(); //list of all lists in which are tasks categorized in
    private ArrayList<String> listNames = new ArrayList<>(); //list of all the names of the lists in this.lists (this.lists and this.listNames are in the same order)
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
            text.append(entry.getTask()).append(this.delimiter).append(entry.getList()).append(this.delimiter).append(entry.getDue()).append(this.delimiter).append(entry.getRecurrence()).append(this.lineDelimiter);
        }

        this.saveFile.save(text.toString()); //Writes contents to file

    }

    public void load(){
        String data = this.saveFile.load();
        String[] lines = data.split(this.lineDelimiter);

        for (String line : lines) {//loop through lines to retrieve fields for entry
            String[] fields = line.split(this.delimiter);
            int fieldsLength = fields.length;

            if (fieldsLength == 4) {//loop through fields of entry and add them to this.entries
                Entry entry = new Entry(this.createId(),//create ID and increment counter
                        fields[0], fields[1], Integer.parseInt(fields[2]), fields[3]);//create entry
                this.entries.add(entry);//add entry
                this.addToList(entry);//add entry to according list in this.lists
            }
        }

    }



    public void add(String task,String list,int due,String recurrence, int position){
        //add new entry to database
        Entry entry = new Entry(this.createId(),task,list,due,recurrence); //generate ID and create entry
        if (position < 0){
            this.entries.add(position, entry);
        }
        else {
            this.entries.add(entry); //add entry to this.entries
        }
        this.addToList(entry); //add entry to according list in this.lists
        this.save(); //write changes to save file
    }

    public void remove(int id){
        //remove entry from database

        int dataLength = this.entries.size();

        for (int i=0; i<dataLength;i++){//get entry in this.entries by ID
            Entry entry = this.entries.get(i);
            if (entry.getID()==id){//remove from this.entries if ID matches
                this.removeFromList(entry); //remove entry from this.lists
                this.entries.remove(i);
                i=dataLength;//jump out of loop
            }
        }

        this.save(); //write changes to save file
    }

    public void addToList(Entry entry){
        //add entry to this.lists

        int listLength = this.listNames.size();

        boolean added = false;


        for (int i=0;i<listLength;i++){//loop through this.listNames until name matches list in entry

            if (this.listNames.get(i).equals(entry.getList())){

                this.lists.get(i).add(entry);//add entry to according list
                added = true;//set added flag
            }

        }

        if (!added){//if entry has not been added it means, there was no according list and it has to be created
            List<Entry> newList = new ArrayList<>();//create new list
            newList.add(entry);//add the entry
            this.lists.add(newList);//add new list to this.lists
            this.listNames.add(entry.getList());//add the name of the new list to this.listNames
        }


    }

    public void removeFromList(Entry entry){
        //removes an entry from this.lists

        int listsLength = this.lists.size();

        for (int i=0;i<listsLength;i++){//loop through this.lists until the right list is found

            if (this.listNames.get(i).equals(entry.getList())){//true if this.listName equals the list of the entry

                int listLength = this.lists.get(i).size();


                for (int j=0;j<listLength;j++){//loop through individual list in this.lists until ID of list entry matches sought entry

                    if (this.lists.get(i).get(j).getID() == entry.getID()){

                        this.lists.get(i).remove(j);//remove entry from list
                        if (this.lists.get(i).size() == 0){//remove list if it is empty

                            this.lists.remove(i);

                        }
                        j = listLength;//jump out of inner loop
                        i = listsLength;//jump out of outer loop
                    }
                }
            }

        }

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

    public void setTodays(ArrayList<Entry> todays){
        //sets the ArrayList<Entry> todays
        this.todays = todays;

    }

    public int createId(){
        //returns id and increments the counter
        this.id++;
        return this.id;

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

    public ArrayList<Entry> getTodays(){

        return this.todays;

    }

    public ArrayList<String> getEntriesAsString(){

        ArrayList<String> items = new ArrayList<>();
        int dataLength = this.entries.size();

        for (int i=0; i<dataLength;i++){
            items.add(this.entries.get(i).getTask());
        }

        return items;
    }

    public List<List<Entry>> getLists(){

        return this.lists;

    }

    public List<String> getListNames(){

        return this.listNames;
    }
}