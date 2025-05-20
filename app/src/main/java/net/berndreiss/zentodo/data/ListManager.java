package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ListManager implements ListManagerI{

    private SQLiteHelper sqLiteHelper;

    public ListManager(SQLiteHelper sqLiteHelper){
        this.sqLiteHelper = sqLiteHelper;
    }

    @Override
    public TaskList addList(long l, String s, String s1) {
        return null;
    }

    @Override
    public void addUserProfileToList(long l, int i, long l1) {

    }

    @Override
    public void removeUserProfileFromList(long l, int i, long l1) {

    }

    @Override
    public void removeList(long l) {

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
    public Long updateId(long l, long l1) {
        return 0L;
    }

    @Override
    public void updateListName(long l, String s) {

    }

    @Override
    public List<Entry> getListEntries(long user, int profile, Long listId) {
        return Collections.emptyList();
    }

    @Override
    public Optional<TaskList> getList(long l) {
        return Optional.empty();
    }

    @Override
    public Optional<TaskList> getListByName(long l, int i, String s) {
        return Optional.empty();
    }

    @Override
    public List<TaskList> getListsForUser(long l, int i) {
        return Collections.emptyList();
    }

    @Override
    public List<TaskList> getLists() {
        return Collections.emptyList();
    }

    public synchronized void updateList(Entry entry, Long list){

        if (entry == null)
            return;

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();


        int position = 0;
        if (list != null) {
            Cursor cursor = db.rawQuery("SELECT 1 FROM ENTRIES WHERE LIST=?", new String[]{String.valueOf(list)});

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                cursor.close();


                cursor = db.rawQuery("SELECT MAX(LIST_POSITION) FROM ENTRIES WHERE LIST=?", new String[]{String.valueOf(list)});

                cursor.moveToFirst();

                if (!cursor.isAfterLast()) {
                    position = cursor.getInt(0) + 1;
                }
            }
            cursor.close();
        }

        values.put("LIST", list);
        values.put("LIST_POSITION", position);
        entry.setListPosition(position);
        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(entry.getId())});

        if (entry.getList() != null && !Objects.equals(entry.getList(), list)) {
            db.execSQL("UPDATE ENTRIES SET LIST_POSITION=LIST_POSITION - 1 WHERE LIST=? AND LIST_POSITION >?", new String[]{String.valueOf(entry.getList()), String.valueOf(entry.getListPosition())});
            db.execSQL("DELETE FROM LISTS WHERE NOT EXISTS ( SELECT 1 FROM ENTRIES WHERE LIST=ID AND LIST=?)", new String[]{String.valueOf(entry.getList())});
        }

        ;
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
}
