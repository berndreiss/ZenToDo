package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class ListManager implements ListManagerI{

    private SQLiteHelper sqLiteHelper;

    public ListManager(SQLiteHelper sqLiteHelper){
        this.sqLiteHelper = sqLiteHelper;
    }

    @Override
    public synchronized TaskList addList(long id, String name, String color) throws InvalidActionException {
        if (getList(id).isPresent())
            return null;
        if (name == null)
            throw new InvalidActionException("List name must not be null.");
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ID", id);
        values.put("NAME", name);
        values.put("COLOR", color);
        db.insert("LISTS", null, values);
        db.close();
        return new TaskList(id, name, color);
    }

    @Override
    public synchronized void addUserProfileToList(long user, int profile, long listId) throws InvalidActionException {
        Optional<TaskList> list = getList(listId);
        if (list.isEmpty())
            return;
        List<TaskList> lists = getListsForUser(user, profile);
        if (lists.stream().anyMatch(l -> l.getName().equals(list.get().getName())))
            throw new InvalidActionException("List with same name already exists for the user.");
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("USER", user);
        values.put("PROFILE", profile);
        values.put("LIST", listId);
        db.insert("PROFILE_LIST", null, values);
        db.close();
    }

    @Override
    public synchronized void removeUserProfileFromList(long user, int profile, long list) {

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        db.delete("PROFILE_LIST", "USER = ? AND PROFILE = ? AND LIST = ?",
                new String[]{String.valueOf(user), String.valueOf(profile), String.valueOf(list)});
        ContentValues contentValues = new ContentValues();
        contentValues.put("LIST", (Long) null);
        contentValues.put("LIST_POSITION", (Integer) null);
        db.update("ENTRIES", contentValues, "USER = ? AND PROFILE = ? AND LIST = ?", new String[]{
                String.valueOf(user), String.valueOf(profile), String.valueOf(list)
        });
        db.close();
    }

    @Override
    public synchronized void removeList(long id) {

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.delete("LISTS", "ID = ?", new String[]{String.valueOf(id)});
        db.delete("PROFILE_LIST", "LIST = ?", new String[]{String.valueOf(id)});
        ContentValues values = new ContentValues();
        values.put("LIST", (Long) null);
        values.put("LIST_POSITION", (Long) null);
        db.update("ENTRIES", values, "LIST = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    @Override
    public synchronized void updateList(long userId, int profile, long id, Long listId){
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE USER = ? AND PROFILE = ? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(profile), String.valueOf(id)});
        List<Entry> entries = EntryManager.getListOfEntries(cursor);
        cursor.close();
        if (entries.size() != 1)
            return;
        updateList(entries.get(0), listId);
    }

    @Override
    public synchronized Long updateId(long list, long newId) {
        Long existingId = null;
        Optional<TaskList> existingList = getList(newId);
        if (existingList.isPresent()){
            existingId = getUniqueUserId();
            updateId(newId, existingId);
        }

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ID", newId);
        db.update("LISTS", values, "ID = ?", new String[]{String.valueOf(list)});

        values = new ContentValues();
        values.put("LIST", newId);
        db.update("PROFILE_LIST", values, "LIST = ?", new String[]{String.valueOf(list)});
        db.update("ENTRIES", values, "LIST = ?", new String[]{String.valueOf(list)});

        db.close();
        return existingId;
    }

    @Override
    public synchronized void updateListName(long list, String name) throws InvalidActionException {
        if (name == null)
            throw new InvalidActionException("Name must not be null.");
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("NAME", name);
        db.update("LISTS", values, "ID = ?", new String[]{String.valueOf(list)});
        db.close();

    }

    @Override
    public List<Entry> getListEntries(long user, int profile, Long listId) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        Cursor cursor;
        if (listId == null)
            cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE USER = ? AND PROFILE = ? AND LIST IS NULL",
                    new String[]{String.valueOf(user), String.valueOf(profile)});
        else
            cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE USER = ? AND PROFILE = ? AND LIST = ?",
                    new String[]{String.valueOf(user), String.valueOf(profile), String.valueOf(listId)});
        List<Entry> entries = EntryManager.getListOfEntries(cursor);
        cursor.close();
        db.close();
        return entries;
    }

    @Override
    public Optional<TaskList> getList(long id) {
        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM LISTS WHERE ID = ?", new String[]{String.valueOf(id)});
        List<TaskList> lists = getListOfTaskList(cursor);
        cursor.close();
        database.close();
        if (lists.size() != 1)
            return Optional.empty();
        return Optional.of(lists.get(0));
    }

    @Override
    public Optional<TaskList> getListByName(long user, int profile, String name) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM LISTS JOIN PROFILE_LIST ON ID = LIST WHERE USER = ? AND PROFILE = ? AND NAME = ?",
                new String[]{String.valueOf(user), String.valueOf(profile), String.valueOf(name)});
        List<TaskList> list = getListOfTaskList(cursor);
        cursor.close();
        db.close();
        if (list.size() != 1)
            return Optional.empty();
        return Optional.of(list.get(0));
    }

    @Override
    public List<TaskList> getListsForUser(long user, int profile) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM LISTS JOIN PROFILE_LIST ON LIST = ID WHERE USER = ? AND PROFILE = ?",
                new String[]{String.valueOf(user), String.valueOf(profile)});
        List<TaskList> list = getListOfTaskList(cursor);
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public List<TaskList> getLists() {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LISTS", null);
        List<TaskList> list = getListOfTaskList(cursor);
        cursor.close();

        db.close();
        return list;
    }

    public synchronized void updateList(Entry entry, Long list){

        if (entry == null)
            return;

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        Integer position = null;
        if (list != null) {
            Cursor cursor = db.rawQuery("SELECT 1 FROM ENTRIES WHERE LIST=?", new String[]{String.valueOf(list)});
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                cursor.close();
                cursor = db.rawQuery("SELECT MAX(LIST_POSITION) FROM ENTRIES WHERE LIST=?", new String[]{String.valueOf(list)});
                cursor.moveToFirst();
                if (!cursor.isAfterLast())
                    position = cursor.getInt(0) + 1;
            }
            cursor.close();
        }

        values.put("LIST", list);
        values.put("LIST_POSITION", position);
        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(entry.getId())});

        if (entry.getList() != null && !Objects.equals(entry.getList(), list)) {
            db.execSQL("UPDATE ENTRIES SET LIST_POSITION=LIST_POSITION - 1 WHERE LIST=? AND LIST_POSITION >?", new String[]{String.valueOf(entry.getList()), String.valueOf(entry.getListPosition())});
        }
        entry.setList(list);
        entry.setListPosition(position);
    }
    @Override
    public synchronized void swapListEntries(long userId, int profile, long list, long id, int position){

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE USER = ? AND PROFILE = ? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(profile), String.valueOf(profile)});
        List<Entry> entries = EntryManager.getListOfEntries(cursor);
        cursor.close();
        if (entries.size() != 1)
            return;
        swapListEntries(entries.get(0), position);
    }

    synchronized void swapListEntries(Entry entry, int pos){

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        ContentValues values1 = new ContentValues();
        values1.put("LIST_POSITION", String.valueOf(entry.getPosition()));
        db.update("ENTRIES", values1, "LIST=? AND LIST_POSITION=?", new String[]{String.valueOf(entry.getList()), String.valueOf(pos)});

        ContentValues values0 = new ContentValues();
        values0.put("LIST_POSITION", String.valueOf(pos));
        db.update("ENTRIES", values0, "ID=?", new String[]{String.valueOf(entry.getId())});

        //clean up
        ;

    }
    @Override
    public synchronized void updateListColor(long list, String color){

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("COLOR", color);
        int updatedRows = db.update("LISTS", values, "ID=?", new String[]{String.valueOf(list)});

        if (updatedRows==0){
            values.put("ID", list);
            db.insert("LISTS", null, values);
        }

        ;
    }
    public static List<TaskList> getListOfTaskList(Cursor cursor){

        List<TaskList> list = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){

            long id = cursor.getLong(0);
            String name = cursor.getString(1);
            String color = cursor.getString(2);
            TaskList taskList = new TaskList(id, name, color);
            list.add(taskList);
            cursor.moveToNext();
        }

        return list;
    }
    public long getUniqueUserId(){
        Random random = new Random();
        long id = random.nextLong();
        while (getList(id).isPresent())
            id++;
        return id;
    }
}
