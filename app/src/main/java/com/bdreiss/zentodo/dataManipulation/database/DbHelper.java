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
import android.util.Log;

import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.dataManipulation.TaskList;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.COLUMNS_ENTRIES_V1;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.COLUMNS_LISTS_V1;
import com.bdreiss.zentodo.dataManipulation.database.valuesV1.TABLES_V1;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class DbHelper extends SQLiteOpenHelper{

    private static final int DB_VERSION = 1;

    public DbHelper(Context context, String DB_NAME){
        super(context,DB_NAME,null, DB_VERSION);
    }

    //Create new table for entries onCreate
    public void onCreate(SQLiteDatabase db){
        String query = "CREATE TABLE " + TABLES_V1.TABLE_ENTRIES + " ("
                + COLUMNS_ENTRIES_V1.ID_COL + " INTEGER , "
                + COLUMNS_ENTRIES_V1.TASK_COL + " TEXT, "
                + COLUMNS_ENTRIES_V1.FOCUS_COL + " INTEGER, "
                + COLUMNS_ENTRIES_V1.DROPPED_COL + " INTEGER, "
                + COLUMNS_ENTRIES_V1.LIST_COL + " TEXT, "
                + COLUMNS_ENTRIES_V1.LIST_POSITION_COL + " INTEGER, "
                + COLUMNS_ENTRIES_V1.REMINDER_DATE_COL + " INTEGER, "
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

    //adds new entry to database
    public void addEntry(Entry entry) {

        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();

        values.put(COLUMNS_ENTRIES_V1.ID_COL.toString(),entry.getId());
        values.put(COLUMNS_ENTRIES_V1.TASK_COL.toString(),entry.getTask());
        values.put(COLUMNS_ENTRIES_V1.FOCUS_COL.toString(),entry.getFocus());
        values.put(COLUMNS_ENTRIES_V1.DROPPED_COL.toString(),entry.getDropped());
        values.put(COLUMNS_ENTRIES_V1.LIST_COL.toString(),entry.getList());
        values.put(COLUMNS_ENTRIES_V1.LIST_POSITION_COL.toString(),entry.getListPosition());
        values.put(COLUMNS_ENTRIES_V1.REMINDER_DATE_COL.toString(),entry.getReminderDate());
        values.put(COLUMNS_ENTRIES_V1.RECURRENCE_COL.toString(),entry.getRecurrence());
        values.put(COLUMNS_ENTRIES_V1.POSITION_COL.toString(), entry.getPosition());

        db.insert(TABLES_V1.TABLE_ENTRIES.toString(),null,values);
        db.close();
    }

    public void addList(String list, String color){
        ContentValues values = new ContentValues();

        Log.d("CHECK", list);
        SQLiteDatabase db = this.getWritableDatabase();

        values.put(COLUMNS_LISTS_V1.LIST_NAME_COL.toString(),list);
        values.put(COLUMNS_LISTS_V1.LIST_COLOR_COL.toString(),color);

        db.insert(TABLES_V1.TABLE_LISTS.toString(), null,values);
        db.close();
    }

    //removes entry from database by id
    public void removeEntry(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLES_V1.TABLE_ENTRIES + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id;

        db.execSQL(query);

        db.close();
    }

    public void removeList(String list){
        SQLiteDatabase db = this.getWritableDatabase();

        list = checkStringForApostrophe(list);

        String query = "DELETE FROM " + TABLES_V1.TABLE_LISTS + " WHERE " + COLUMNS_LISTS_V1.LIST_NAME_COL + "='" + list + "'";

        db.execSQL(query);

        db.close();
    }

    //update entry by id using String
    public void updateEntry(COLUMNS_ENTRIES_V1 field, int id, String value){
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

    //update entry by id using Integer
    public void updateEntry(COLUMNS_ENTRIES_V1 field, int id, int value){
        String query = "UPDATE " + TABLES_V1.TABLE_ENTRIES + " SET " + field + "=" + value + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id + ";";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void updateEntry(COLUMNS_ENTRIES_V1 field, int id, boolean valueBool){
        int value = boolToInt(valueBool);
        String query = "UPDATE " + TABLES_V1.TABLE_ENTRIES + " SET " + field + "=" + value + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id + ";";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void updateList(String list, String color){
        list = checkStringForApostrophe(list);
        String query = "UPDATE " + TABLES_V1.TABLE_LISTS + " SET " + COLUMNS_LISTS_V1.LIST_COLOR_COL + "='" + color + "' WHERE " + COLUMNS_LISTS_V1.LIST_NAME_COL + "='" + list + "';";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    //load all entries from database and return as ArrayList
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
            int reminderDate = cursor.getInt(6);
            String recurrence = cursor.getString(7);
            int position = cursor.getInt(8);

            Entry entry = new Entry(id, position, task);
            entry.setFocus(focus);
            entry.setDropped(dropped);
            if (!(list==null))
                entry.setList(list);
            entry.setListPosition(listPosition);
            entry.setReminderDate(reminderDate);
            if (!(recurrence==null))
                entry.setRecurrence(recurrence);

            entries.add(entry);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return entries;


    }

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

    public void swapEntries(COLUMNS_ENTRIES_V1 positionCol, int id1, int id2){
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT " + positionCol + " FROM " + TABLES_V1.TABLE_ENTRIES + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id1, null);
        cursor.moveToFirst();

        int pos1 = cursor.getInt(0);

        cursor = db.rawQuery("SELECT " + positionCol + " FROM " + TABLES_V1.TABLE_ENTRIES + " WHERE " + COLUMNS_ENTRIES_V1.ID_COL + "=" + id2, null);
        cursor.moveToFirst();

        int pos2 = cursor.getInt(0);

        cursor.close();
        db.close();
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

    private String checkStringForApostrophe(String string){

        if (string == null){
            return null;
        }

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
