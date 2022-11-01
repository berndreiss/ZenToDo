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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DbHelper  extends SQLiteOpenHelper{

    private static final String DB_NAME = "Data.db";

    private static final int DB_VERSION = 1;

    public static final String TABLE_ENTRIES = "entries";

    public static final String ID_COL = "id";

    public static final String TASK_COL = "task";

    public static final String FOCUS_COL = "focus";

    public static final String DROPPED_COL = "dropped";

    public static final String LIST_COL = "list";

    public static final String LIST_POSITION_COL = "listPosition";

    public static final String DUE_COL = "due";

    public static final String RECURRENCE_COL = "recurrence";

    public DbHelper(Context context){
        super(context,DB_NAME,null, DB_VERSION);
    }

    //Create new table for entries onCreate
    public void onCreate(SQLiteDatabase db){
        String query = "CREATE TABLE " + TABLE_ENTRIES + " ("
                + ID_COL + " INTEGER , " + TASK_COL + " TEXT, " + FOCUS_COL + " INTEGER, " + DROPPED_COL + " INTEGER, "
                + LIST_COL + " TEXT, " + LIST_POSITION_COL + " INTEGER, " + DUE_COL + " INTEGER, " + RECURRENCE_COL
                + " TEXT)";
        db.execSQL(query);
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

        db.insert(TABLE_ENTRIES,null,values);
        db.close();
    }

    //removes entry from database by id
    public void removeEntry(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLE_ENTRIES + " WHERE " + ID_COL + "=" + id;

        db.execSQL(query);

        db.close();
    }

    //update entry by id using String
    public void updateEntry(String field, int id, String value){
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

            Entry entry = new Entry(id,task);
            entry.setFocus(focus);
            entry.setDropped(dropped);
            if (!(list==null))
                entry.setList(list);
            entry.setListPosition(listPosition);
            entry.setDue(due);
            if (!(recurrence==null))
                entry.setRecurrence(recurrence);

            entries.add(entry);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return entries;


    }

    //converts Integer to Boolean (false if 0, true otherwise)
    static boolean intToBool(int i){
        return i!=0;
    }

    //converts Boolean to Integer (0 if false, 1 if true)
    static int boolToInt(boolean b){
        return b ? 1 : 0;
    }

}
