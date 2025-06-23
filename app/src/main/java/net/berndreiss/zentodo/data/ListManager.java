package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.berndreiss.zentodo.exceptions.DuplicateIdException;
import net.berndreiss.zentodo.exceptions.InvalidActionException;
import net.berndreiss.zentodo.exceptions.PositionOutOfBoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation of the ListManagerI interface using SQLite and the ZenSQLiteHelper.
 */
public class ListManager implements ListManagerI{

    private final ZenSQLiteHelper zenSqLiteHelper;

    /**
     * Crate new instance of ListManager.
     * @param zenSqLiteHelper the helper for interacting with the database
     */
    public ListManager(ZenSQLiteHelper zenSqLiteHelper){
        this.zenSqLiteHelper = zenSqLiteHelper;
    }

    @Override
    public synchronized TaskList addList(long id, String name, String color) throws InvalidActionException, DuplicateIdException {
        if (getList(id).isPresent())
            throw new DuplicateIdException("List with id already exists.");
        if (name == null)
            throw new InvalidActionException("List name must not be null.");
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ID", id);
        values.put("NAME", name);
        values.put("COLOR", color);
        db.insert("LISTS", null, values);
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
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("USER", user);
        values.put("PROFILE", profile);
        values.put("LIST", listId);
        db.insert("PROFILE_LIST", null, values);
    }

    @Override
    public synchronized void removeUserProfileFromList(long user, int profile, long list) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        db.delete("PROFILE_LIST", "USER = ? AND PROFILE = ? AND LIST = ?",
                new String[]{String.valueOf(user), String.valueOf(profile), String.valueOf(list)});
        ContentValues contentValues = new ContentValues();
        contentValues.put("LIST", (Long) null);
        contentValues.put("LIST_POSITION", (Integer) null);
        db.update("TASKS", contentValues, "USER = ? AND PROFILE = ? AND LIST = ?", new String[]{
                String.valueOf(user), String.valueOf(profile), String.valueOf(list)
        });
    }

    @Override
    public synchronized void removeList(long id) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        db.delete("LISTS", "ID = ?", new String[]{String.valueOf(id)});
        db.delete("PROFILE_LIST", "LIST = ?", new String[]{String.valueOf(id)});
        ContentValues values = new ContentValues();
        values.put("LIST", (Long) null);
        values.put("LIST_POSITION", (Long) null);
        db.update("TASKS", values, "LIST = ?", new String[]{String.valueOf(id)});
    }

    @Override
    public synchronized void updateList(long userId, int profile, long id, Long listId){
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(profile), String.valueOf(id)});
        List<Task> entries = TaskManager.getListOfTasks(cursor);
        cursor.close();
        if (entries.size() != 1)
            return;
        updateList(entries.getFirst(), listId);
    }

    @Override
    public synchronized void updateId(long list, long newId) throws DuplicateIdException {
        Optional<TaskList> existingList = getList(newId);
        if (existingList.isPresent())
            throw new DuplicateIdException("List id already exists: id + " + newId);
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ID", newId);
        db.update("LISTS", values, "ID = ?", new String[]{String.valueOf(list)});
        values = new ContentValues();
        values.put("LIST", newId);
        db.update("PROFILE_LIST", values, "LIST = ?", new String[]{String.valueOf(list)});
        db.update("TASKS", values, "LIST = ?", new String[]{String.valueOf(list)});
    }

    @Override
    public synchronized void updateListName(long list, String name) throws InvalidActionException {
        if (name == null)
            throw new InvalidActionException("Name must not be null.");
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", name);
        db.update("LISTS", values, "ID = ?", new String[]{String.valueOf(list)});
    }

    @Override
    public List<Task> getListEntries(long user, int profile, Long listId) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Cursor cursor;
        if (listId == null)
            cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ? AND LIST IS NULL",
                    new String[]{String.valueOf(user), String.valueOf(profile)});
        else
            cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ? AND LIST = ?",
                    new String[]{String.valueOf(user), String.valueOf(profile), String.valueOf(listId)});
        List<Task> entries = TaskManager.getListOfTasks(cursor);
        cursor.close();
        if (listId != null)
            return entries.stream().sorted(Comparator.comparing(Task::getListPosition)).toList();
        return entries.stream().sorted(Comparator.comparing(Task::getPosition)).toList();
    }

    @Override
    public Optional<TaskList> getList(long id) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LISTS WHERE ID = ?", new String[]{String.valueOf(id)});
        List<TaskList> lists = getListOfTaskList(cursor);
        cursor.close();
        if (lists.size() != 1)
            return Optional.empty();
        return Optional.of(lists.getFirst());
    }

    @Override
    public Optional<TaskList> getListByName(long user, int profile, String name) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LISTS JOIN PROFILE_LIST ON ID = LIST WHERE USER = ? AND PROFILE = ? AND NAME = ?",
                new String[]{String.valueOf(user), String.valueOf(profile), String.valueOf(name)});
        List<TaskList> list = getListOfTaskList(cursor);
        cursor.close();
        if (list.size() != 1)
            return Optional.empty();
        return Optional.of(list.getFirst());
    }

    @Override
    public List<TaskList> getListsForUser(long user, int profile) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LISTS JOIN PROFILE_LIST ON LIST = ID WHERE USER = ? AND PROFILE = ?",
                new String[]{String.valueOf(user), String.valueOf(profile)});
        List<TaskList> list = getListOfTaskList(cursor);
        cursor.close();
        return list;
    }

    @Override
    public List<TaskList> getLists() {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LISTS", null);
        List<TaskList> list = getListOfTaskList(cursor);
        cursor.close();
        return list;
    }

    public synchronized void updateList(Task task, Long list){
        if (task == null)
            return;
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Integer position = null;
        if (list != null) {
            Cursor cursor = db.rawQuery("SELECT 1 FROM TASKS WHERE LIST=?", new String[]{String.valueOf(list)});
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                cursor.close();
                cursor = db.rawQuery("SELECT MAX(LIST_POSITION) FROM TASKS WHERE LIST=?", new String[]{String.valueOf(list)});
                cursor.moveToFirst();
                if (!cursor.isAfterLast())
                    position = cursor.getInt(0) + 1;
            }
            cursor.close();
        }
        values.put("LIST", list);
        values.put("LIST_POSITION", position);
        db.update("TASKS", values, "ID=?", new String[]{String.valueOf(task.getId())});
        if (task.getList() != null && !Objects.equals(task.getList(), list)) {
            db.execSQL("UPDATE TASKS SET LIST_POSITION=LIST_POSITION - 1 WHERE LIST=? AND LIST_POSITION >?", new String[]{String.valueOf(task.getList()), String.valueOf(task.getListPosition())});
        }
        task.setList(list);
        task.setListPosition(position);
    }
    @Override
    public synchronized void swapListEntries(long userId, int profile, long list, long id, int position) throws PositionOutOfBoundException {
        List<Task> listEntries = getListEntries(userId, profile, list);
        if (position >= listEntries.size())
            throw new PositionOutOfBoundException("List position is out of bounds.");
        Optional<Task> task = listEntries.stream().filter(e -> e.getId() == id).findFirst();
        Optional<Task> taskOther = listEntries.stream().filter(e -> e.getListPosition() == position).findFirst();
        if (task.isEmpty() || taskOther.isEmpty())
            return;
        swapListEntries(task.get(), task.get().getListPosition(), taskOther.get(), position);
    }

    synchronized void swapListEntries(Task task, int posOld, Task taskOther, int pos){
        List<Task> listOld = getListEntries(task.getUserId(), task.getProfile(), task.getList());
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values1 = new ContentValues();
        values1.put("LIST_POSITION", posOld);
        db.update("TASKS", values1, "LIST = ? AND ID = ?",
                new String[]{String.valueOf(task.getList()), String.valueOf(taskOther.getId())});
        ContentValues values0 = new ContentValues();
        values0.put("LIST_POSITION", pos);
        db.update("TASKS", values0, "LIST = ? AND ID = ?",
                new String[]{String.valueOf(task.getList()), String.valueOf(task.getId())});
    }
    @Override
    public synchronized void updateListColor(long list, String color){
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("COLOR", color);
        int updatedRows = db.update("LISTS", values, "ID=?", new String[]{String.valueOf(list)});

        if (updatedRows==0){
            values.put("ID", list);
            db.insert("LISTS", null, values);
        }
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
