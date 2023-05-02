/*
*
*     Methods to interact with SQLite database:
*
*     public void addEntry(Entry entry) -> adds entry to database
*     public void removeEntry(int id) -> removes entry from database by id
*     public void updateEntry(String field, int id, String value) -> updates entry via id using String
*     public void updateEntry(String field, int id, int value) -> update entry via id using Integer
*     public ArrayList<Entry> loadEntries() -> returns all entries as ArrayList
*     static boolean intToBool(int i) -> converts Integer to Boolean (false if 0, true otherwise)
*     static int boolToInt(boolean b) -> converts Boolean to Integer (0 if false, 1 if true)
*
*/

package com.bdreiss.zentodo.dataManipulation.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.dataManipulation.TaskList;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.COLUMNS_ENTRIES_V1;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.COLUMNS_LISTS_V1;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.TABLES_V1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

public class DbHelperV1 extends SQLiteOpenHelper{

    private static final int DB_VERSION = 1;

    private final Context context;

    public DbHelperV1(Context context, String DB_NAME){
        super(context,DB_NAME,null, DB_VERSION);
        this.context = context;
    }


    //Create TABLE_ENTRIES for entries TABLE_LISTS for lists onCreate
    public void onCreate(SQLiteDatabase db){
        String query = "CREATE TABLE " + TABLES_V1.TABLE_ENTRIES + " ("
                + COLUMNS_ENTRIES_V1.ID_COL + " INTEGER , "
                + COLUMNS_ENTRIES_V1.TASK_COL + " TEXT, "
                + COLUMNS_ENTRIES_V1.FOCUS_COL + " INTEGER, "
                + COLUMNS_ENTRIES_V1.DROPPED_COL + " INTEGER, "
                + COLUMNS_ENTRIES_V1.LIST_COL + " TEXT, "
                + COLUMNS_ENTRIES_V1.LIST_POSITION_COL + " INTEGER, "
                + COLUMNS_ENTRIES_V1.REMINDER_DATE_COL + " TEXT,"
                + COLUMNS_ENTRIES_V1.RECURRENCE_COL + " TEXT,"
                + COLUMNS_ENTRIES_V1.POSITION_COL + " INTEGER "
                + ")";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLES_V1.TABLE_LISTS + " ("
                + COLUMNS_LISTS_V1.LIST_NAME_COL + " TEXT, "
                + COLUMNS_LISTS_V1.LIST_COLOR_COL + " TEXT"
                + ")";
        db.execSQL(query);

    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String query = "DROP TABLE IF EXISTS " + TABLES_V1.TABLE_ENTRIES;
        db.execSQL(query);
        onCreate(db);
    }


    //adds new entry to TABLE_ENTRIES
    public void addEntry(Entry entry) {

        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();

        values.put(COLUMNS_ENTRIES_V1.ID_COL.toString(),entry.getId());
        values.put(COLUMNS_ENTRIES_V1.TASK_COL.toString(),entry.getTask());
        values.put(COLUMNS_ENTRIES_V1.FOCUS_COL.toString(),entry.getFocus());
        values.put(COLUMNS_ENTRIES_V1.DROPPED_COL.toString(),entry.getDropped());
        values.put(COLUMNS_ENTRIES_V1.LIST_COL.toString(),entry.getList());
        values.put(COLUMNS_ENTRIES_V1.LIST_POSITION_COL.toString(),entry.getListPosition());

        values.put(COLUMNS_ENTRIES_V1.REMINDER_DATE_COL.toString(),
                entry.getReminderDate()== null ? null : entry.getReminderDate().toString() );
        values.put(COLUMNS_ENTRIES_V1.RECURRENCE_COL.toString(),entry.getRecurrence());
        values.put(COLUMNS_ENTRIES_V1.POSITION_COL.toString(), entry.getPosition());

        db.insert(TABLES_V1.TABLE_ENTRIES.toString(),null,values);
        db.close();
    }


    //adds List to TABLE_LISTS
    public void addList(String list, String color){
        ContentValues values = new ContentValues();

        SQLiteDatabase db = this.getWritableDatabase();

        values.put(COLUMNS_LISTS_V1.LIST_NAME_COL.toString(),list);
        values.put(COLUMNS_LISTS_V1.LIST_COLOR_COL.toString(),color);

        db.insert(TABLES_V1.TABLE_LISTS.toString(), null,values);
        db.close();
    }


    //takes id of entry and removes it from TABLE_ENTRIES
    public void removeEntry(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLES_V1.TABLE_ENTRIES + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id;

        db.execSQL(query);

        db.close();
    }

    //takes a list name and removes it from TABLE_LISTS
    public void removeList(String list){
        SQLiteDatabase db = this.getWritableDatabase();

        list = checkStringForApostrophe(list);

        String query = "DELETE FROM " + TABLES_V1.TABLE_LISTS + " WHERE " + COLUMNS_LISTS_V1.LIST_NAME_COL + "='" + list + "'";

        db.execSQL(query);

        db.close();
    }
    //takes a column name (see COLUMNS_ENTRIES_V1), id and string and updates field of entry in TABLE_ENTRIES
    public void updateEntry(COLUMNS_ENTRIES_V1 field, int id, LocalDate value){

        String query;

        if (value == null)
            query = "UPDATE " + TABLES_V1.TABLE_ENTRIES + " SET " + field + "= NULL WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id + ";";
        else
            query = "UPDATE " + TABLES_V1.TABLE_ENTRIES + " SET " + field + "='" + value + "' WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id + ";";

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }


    //takes a column name (see COLUMNS_ENTRIES_V1), id and string and updates field of entry in TABLE_ENTRIES
    public void updateEntry(COLUMNS_ENTRIES_V1 field, int id, String value){

        //handle potential "'" so database interaction works
        value = checkStringForApostrophe(value);

        String query;

        if (value == null)
            query = "UPDATE " + TABLES_V1.TABLE_ENTRIES + " SET " + field + "= NULL WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id + ";";
        else
            query = "UPDATE " + TABLES_V1.TABLE_ENTRIES + " SET " + field + "='" + value + "' WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id + ";";

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }


    //takes a column name (see COLUMNS_ENTRIES_V1), id and int and updates field of entry in TABLE_ENTRIES
    public void updateEntry(COLUMNS_ENTRIES_V1 field, int id, int value){
        String query = "UPDATE " + TABLES_V1.TABLE_ENTRIES + " SET " + field + "=" + value + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id + ";";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    //takes a column name (see COLUMNS_ENTRIES_V1), id and boolean and updates field of entry in TABLE_ENTRIES
    public void updateEntry(COLUMNS_ENTRIES_V1 field, int id, boolean valueBool){

        //convert bool to int
        int value = boolToInt(valueBool);
        String query = "UPDATE " + TABLES_V1.TABLE_ENTRIES + " SET " + field + "=" + value + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id + ";";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    //takes list name and color as Strings and updates color in entry in TABLE_LISTS
    public void updateList(String list, String color){

        //handle potential "'" so database interaction works
        list = checkStringForApostrophe(list);

        String query = "UPDATE " + TABLES_V1.TABLE_LISTS + " SET " + COLUMNS_LISTS_V1.LIST_COLOR_COL + "='" + color + "' WHERE " + COLUMNS_LISTS_V1.LIST_NAME_COL + "='" + list + "';";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }


    //load all entries from TABLE_ENTRIES and return as ArrayList
    public ArrayList<Entry> loadEntries(){
        ArrayList<Entry> entries = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLES_V1.TABLE_ENTRIES, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){

            int id = cursor.getInt(0);
            String task = cursor.getString(1);
            boolean focus = intToBool(cursor.getInt(2));
            boolean dropped = intToBool(cursor.getInt(3));
            String list = cursor.getString(4);
            int listPosition = cursor.getInt(5);
            String reminderDate = cursor.getString(6);
            String recurrence = cursor.getString(7);
            int position = cursor.getInt(8);

            Entry entry = new Entry(id, position, task);
            entry.setFocus(focus);
            entry.setDropped(dropped);
            if (!(list==null))
                entry.setList(list);
            entry.setListPosition(listPosition);

            //this try/catch block is necessary, as in older versions of this database the date was stored as an
            //Integer represented by YYYYMMDD and not as LocalDate
            if (reminderDate != null){
                try {
                    entry.setReminderDate(reminderDate == null ? null : LocalDate.parse(reminderDate));

                }catch (DateTimeParseException e){

                    int reminderDateInt = Integer.parseInt(reminderDate);

                    if (reminderDateInt == 0)
                        entry.setReminderDate(null);
                    else {
                        int year = reminderDateInt / 10000;
                        int month = reminderDateInt % 10000 / 100;
                        int day = reminderDateInt % 100;

                        entry.setReminderDate(LocalDate.of(year, month, day));
                    }
                }
            }
            if (!(recurrence==null))
                entry.setRecurrence(recurrence);

            entries.add(entry);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return entries;

    }

    //return all entries in TABLE_LISTS as Map<String,TaskList> (see TaskList.java)
    public Map<String, TaskList> loadLists(){
        Map<String, TaskList> lists = new Hashtable<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLES_V1.TABLE_LISTS, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            String listName = cursor.getString(0);
            String color = cursor.getString(1);

            TaskList list = new TaskList(-1,color);
            lists.put(listName, list);
            cursor.moveToNext();
        }


        cursor.close();
        db.close();
        return lists;
    }


    //swaps two entries in the database: the function looks up the ids in the database and swaps
    //values of the position/list position attributes
    public void swapEntries(COLUMNS_ENTRIES_V1 positionCol, int id1, int id2){

        SQLiteDatabase db = this.getWritableDatabase();

        //get cursor for all elements with id == id1 (which is exactly ONE element)
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT " + positionCol + " FROM " + TABLES_V1.TABLE_ENTRIES + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id1, null);

        //go to first (and ONLY) element
        cursor.moveToFirst();

        //get position of first element
        int pos1 = cursor.getInt(0);

        //repeat process with second element
        cursor = db.rawQuery("SELECT " + positionCol + " FROM " + TABLES_V1.TABLE_ENTRIES + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id2, null);
        cursor.moveToFirst();
        int pos2 = cursor.getInt(0);

        //clean up
        cursor.close();
        db.close();

        //swap positions
        updateEntry(positionCol, id1,pos2);
        updateEntry(positionCol, id2,pos1);

    }


    //converts Integer to Boolean (false if 0, true otherwise)
    public static boolean intToBool(int i){
        return i!=0;
    }


    //converts Boolean to Integer (0 if false, 1 if true)
    public static int boolToInt(boolean b){
        return b ? 1 : 0;
    }

    //checks whether String contains "'" and turns it into "''" so the database request is represented properly
    private String checkStringForApostrophe(String string){

        //return if string is null
        if (string == null){
            return null;
        }

        //get string as Array split by "'"
        String[] sArray = string.split("'");

        StringBuilder sb = new StringBuilder();

        //if the array is empty, the string is simply "'", so "''" is being returned
        if (sArray.length == 0)
            return "''";

        //case "'" is at the beginning
        if (!(string.charAt(0) == '\''))
            sb.append(sArray[0]);

        //add all other string parts in the array concatenated by "''"
        for (int i = 1; i < sArray.length; i++)
            sb.append("''").append(sArray[i]);

        //append "''" in the end if last character was "'"
        if (string.charAt(string.length()-1) == '\'')
            sb.append("''");

        //return string
        return sb.toString();

    }

    //recurring tasks are automatically added to FOCUS
    //when they are removed however, the ids are stored in this ArrayList and the tasks are not shown until the next day
    //this function makes changes permanent storing the data as todays date
    //see also: Data.java && FocusTaskListAdapter
    public void saveRecurring(ArrayList<Integer> arrayList){

        try {
            FileOutputStream fos = new FileOutputStream(context.getFilesDir() + "/" +  LocalDate.now());
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(arrayList);

            fos.close();
            oos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //loads ids stored via saveRecurring(ArrayList<Integer>) that have been saved today
    //deletes all files stored vie saveRecurring that are older
    public ArrayList<Integer> loadRecurring(){

        ArrayList<Integer> toReturn = new ArrayList<>();

        //get name of save file
        File saveFile = new File(context.getFilesDir() + "/" + LocalDate.now());

        //get all file names in files directory
        String[] fileNames = context.getFilesDir().list();

        //get them as File[]
        File[] files = context.getFilesDir().listFiles();

        //loop through all files and delete obsolete ones
        for (int i = 0; i < Objects.requireNonNull(fileNames).length; i++){

            //ignore file that stores information whether app is in test mode
            if (fileNames[i].equals("mode"))
                continue;

            try {
                //if file name (which is a date) is smaller than today, delete it
                if (LocalDate.parse(fileNames[i]).compareTo(LocalDate.now()) < 0) {
                    assert files != null;
                    if (files[i] != null) files[i].delete();
                }

                // in old versions the file was stored under a different naming logic this block removes old files
            } catch (DateTimeParseException e){
                files[i].delete();
            }

        }

        //if save file exists return contents as ArrayList<Integer>
        if (saveFile.exists()){

            try {
                FileInputStream fis = new FileInputStream(saveFile);
                ObjectInputStream ois = new ObjectInputStream(fis);

                toReturn = (ArrayList<Integer>) ois.readObject();

                fis.close();
                ois.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return toReturn;
    }

}
