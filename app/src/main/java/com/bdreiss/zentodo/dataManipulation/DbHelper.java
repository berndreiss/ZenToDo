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

package com.bdreiss.zentodo.dataManipulation;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class DbHelper  extends SQLiteOpenHelper{

   // private static final String DB_NAME = "Data.db";

    private static final String DB_NAME = "Data.db";

    private static final int DB_VERSION = 1;

    private static final String TABLE_ENTRIES = "entries";

    private static final String ID_COL = "id";

    private static final String TASK_COL = "task";

    private static final String FOCUS_COL = "focus";

    private static final String DROPPED_COL = "dropped";

    private static final String LIST_COL = "list";

    private static final String LIST_POSITION_COL = "listPosition";

    private static final String DUE_COL = "due";

    private static final String RECURRENCE_COL = "recurrence";

    private static final String POSITION_COL = "position";

    private static final String TABLE_LISTS = "lists";

    private static final String LIST_NAME_COL = "list";

    private static final String LIST_COLOR_COL = "color";

    public DbHelper(Context context){super(context,DB_NAME,null, DB_VERSION);}

    public void migrate(SQLiteDatabase db) {
        String query = "DROP TABLE IF EXISTS " + TABLE_ENTRIES;
        db.execSQL(query);
        onCreate(db);


    }
    //Create new table for entries onCreate
    public void onCreate(SQLiteDatabase db){
        String query = "CREATE TABLE " + TABLE_ENTRIES + " ("
                + ID_COL + " INTEGER , "
                + TASK_COL + " TEXT, "
                + FOCUS_COL + " INTEGER, "
                + DROPPED_COL + " INTEGER, "
                + LIST_COL + " TEXT, "
                + LIST_POSITION_COL + " INTEGER, "
                + DUE_COL + " INTEGER, "
                + RECURRENCE_COL + " TEXT,"
                + POSITION_COL + " INTEGER "
                + ")";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_LISTS + " ("
                + LIST_NAME_COL + " TEXT, "
                + LIST_COLOR_COL + " TEXT"
                + ")";
        db.execSQL(query);

    }

    public void makeListTable(){

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "CREATE TABLE " + TABLE_LISTS + " ("
                + LIST_NAME_COL + " TEXT, "
                + LIST_COLOR_COL + " TEXT"
                + ")";
        db.execSQL(query);

        db.close();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        String query = "DROP TABLE IF EXISTS " + TABLE_ENTRIES;
        db.execSQL(query);
        onCreate(db);
    }

    //adds new entry to database
    public void addEntry(Entry entry) {

        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();

        values.put(ID_COL,entry.getId());
        values.put(TASK_COL,entry.getTask());
        values.put(FOCUS_COL,entry.getFocus());
        values.put(DROPPED_COL,entry.getDropped());
        values.put(LIST_COL,entry.getList());
        values.put(LIST_POSITION_COL,entry.getListPosition());
        values.put(DUE_COL,entry.getDue());
        values.put(RECURRENCE_COL,entry.getRecurrence());
        values.put(POSITION_COL, entry.getPosition());

        db.insert(TABLE_ENTRIES,null,values);
        db.close();
    }

    public void addList(String list, String color){
        ContentValues values = new ContentValues();

        list = checkStringForApostrophe(list);


        Log.d("CHECK", list);
        SQLiteDatabase db = this.getWritableDatabase();

        values.put(LIST_NAME_COL,list);
        values.put(LIST_COLOR_COL,color);

        db.insert(TABLE_LISTS,null,values);
        db.close();
    }

    //removes entry from database by id
    public void removeEntry(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLE_ENTRIES + " WHERE " + ID_COL + "=" + id;

        db.execSQL(query);

        db.close();
    }

    public void removeList(String list){
        SQLiteDatabase db = this.getWritableDatabase();

        list = checkStringForApostrophe(list);

        String query = "DELETE FROM " + TABLE_LISTS + " WHERE " + LIST_NAME_COL + "='" + list + "'";

        db.execSQL(query);

        db.close();
    }

    //update entry by id using String
    public void updateEntry(String field, int id, String value){
        value = checkStringForApostrophe(value);
        String query = "UPDATE " + TABLE_ENTRIES + " SET " + field + "='" + value + "' WHERE " + ID_COL + "=" + id + ";";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    //update entry by id using Integer
    public void updateEntry(String field, int id, int value){
        String query = "UPDATE " + TABLE_ENTRIES + " SET " + field + "=" + value + " WHERE " + ID_COL + "=" + id + ";";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void updateList(String list, String color){
        list = checkStringForApostrophe(list);
        String query = "UPDATE " + TABLE_LISTS + " SET " + LIST_COLOR_COL + "='" + color + "' WHERE " + LIST_NAME_COL + "='" + list + "';";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void updateAllFields(int id, Entry entry){
        String query = "UPDATE " + TABLE_ENTRIES + " SET "
                + TASK_COL + "='" + checkStringForApostrophe(entry.getTask()) + "', "
                + FOCUS_COL + "=" + entry.getFocus() + ","
                + RECURRENCE_COL  + "='" + entry.getRecurrence() + "',"
                + LIST_COL  + "='" + checkStringForApostrophe(entry.getList()) + "',"
                + LIST_POSITION_COL  + "=" + entry.getListPosition() + ","
                + DROPPED_COL  + "=" + entry.getDropped() + ","
                + DUE_COL + "=" + entry.getDue() + ", "
                + POSITION_COL + "=" + entry.getPosition()
                + " WHERE " + ID_COL + "=" + id;

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();

    }

    //load all entries from database and return as ArrayList
    public ArrayList<Entry> loadEntries(){
        ArrayList<Entry> entries = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ENTRIES, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){

            int id = cursor.getInt(0);
            String task = cursor.getString(1);
            boolean focus = intToBool(cursor.getInt(2));
            boolean dropped = intToBool(cursor.getInt(3));
            String list = cursor.getString(4);
            int listPosition = cursor.getInt(5);
            int due = cursor.getInt(6);
            String recurrence = cursor.getString(7);
            int position = cursor.getInt(8);

            Entry entry = new Entry(id, position, task);
            entry.setFocus(focus);
            entry.setDropped(dropped);
            if (!(list==null) && !list.equals("null"))
                entry.setList(list);
            entry.setListPosition(listPosition);
            entry.setDue(due);
            if (!(recurrence==null) && !recurrence.equals("null"))
                entry.setRecurrence(recurrence);

            entries.add(entry);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return entries;


    }

    public Map<String, Data.List> loadLists(){
        Map<String, Data.List> lists = new Hashtable<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LISTS, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            String listName = cursor.getString(0);
            String color = cursor.getString(1);

            Data.List list = new Data.List(-1,color);
            lists.put(listName, list);
            cursor.moveToNext();
        }


        cursor.close();
        db.close();
        return lists;
    }


    public void swapEntries(String positionCol, int id1, int id2){
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT " + positionCol + " FROM " + TABLE_ENTRIES + " WHERE " + ID_COL + "=" + id1, null);
        cursor.moveToFirst();

        int pos1 = cursor.getInt(0);

        cursor = db.rawQuery("SELECT " + positionCol + " FROM " + TABLE_ENTRIES + " WHERE " + ID_COL + "=" + id2, null);
        cursor.moveToFirst();

        int pos2 = cursor.getInt(0);

        cursor.close();
        db.close();
        updateEntry(positionCol, id1,pos2);
        updateEntry(positionCol, id2,pos1);

    }

    public static String getTaskCol(){return TASK_COL;}

    public static String getFocusCol(){return FOCUS_COL;}

    public static String getDroppedCol(){return DROPPED_COL;}

    public static String getPositionCol(){
        return POSITION_COL;
    }

    public static String getDueCol() {return DUE_COL;}

    public static String getRecurrenceCol(){return RECURRENCE_COL;}

    public static String getListCol() {return LIST_COL;}

    public static String getListPositionCol(){
        return LIST_POSITION_COL;
    }


    //converts Integer to Boolean (false if 0, true otherwise)
    static boolean intToBool(int i){
        return i!=0;
    }

    //converts Boolean to Integer (0 if false, 1 if true)
    static int boolToInt(boolean b){
        return b ? 1 : 0;
    }

    private String checkStringForApostrophe(String string){
        String[] sArray = string.split("'");

        StringBuilder sb = new StringBuilder();

        Log.d("CHECK", String.valueOf(sArray.length));
        if (sArray.length == 0)
            return "''";

        if (!(string.charAt(0) == '\''))
            sb.append(sArray[0]);

        for (int i = 1; i < sArray.length; i++)
            sb.append("''").append(sArray[i]);

        if (string.charAt(string.length()-1) == '\'')
            sb.append("''");


        return sb.toString();
    }

}
