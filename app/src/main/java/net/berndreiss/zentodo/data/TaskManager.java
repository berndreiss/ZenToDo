package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.berndreiss.zentodo.exceptions.DuplicateIdException;
import net.berndreiss.zentodo.exceptions.InvalidActionException;
import net.berndreiss.zentodo.exceptions.PositionOutOfBoundException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class TaskManager implements TaskManagerI {


    private SQLiteHelper sqLiteHelper;

    public TaskManager(SQLiteHelper sqLiteHelper) {
        this.sqLiteHelper = sqLiteHelper;
    }

    private Task getById(Long userId, int profile, long id) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE ID=?", new String[]{String.valueOf(id)});

        List<Task> tasks = getListOfTasks(cursor);

        cursor.close();

        if (tasks.isEmpty())
            return null;

        return tasks.get(0);
    }

    /**
     * @param task
     * @return
     */
    @Override
    public synchronized Task addNewTask(long userId, int profile, String task) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT 1 FROM TASKS", null);

        cursor.moveToFirst();

        int maxPosition = -1;
        if (!cursor.isAfterLast()) {

            cursor.close();

            cursor = db.rawQuery("SELECT MAX(POSITION) FROM TASKS", null);

            cursor.moveToFirst();


            if (!cursor.isAfterLast())
                maxPosition = cursor.getInt(0);
        }
        cursor.close();

        Task Task = null;
        try {
            Task = addNewTask(userId, profile, task, maxPosition + 1);
        } catch (PositionOutOfBoundException _) {}
        return Task;
    }

    @Override
    public synchronized Task addNewTask(long userId, int profile, String task, int position) throws PositionOutOfBoundException {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        Random random = new Random();
        long id = random.nextInt();
        while (id == 0)
            id = random.nextInt();

        while (true) {
            Cursor cursorId = db.rawQuery("SELECT ID FROM TASKS WHERE ID= ? AND USER = ? AND PROFILE = ?",
                    new String[]{String.valueOf(id), String.valueOf(userId), String.valueOf(profile)});

            cursorId.moveToFirst();

            if (!cursorId.isAfterLast()) {
                do {
                    id = random.nextInt();
                } while (id == 0);
                cursorId.close();
            } else {
                cursorId.close();
                break;
            }
        }
        Task Task = null;
        try {
            Task = addNewTask(userId, profile, id, task, position);
        } catch (DuplicateIdException | InvalidActionException _) {
        }
        return Task;

    }

    //adds new Task to TASKS
    @Override
    public synchronized Task addNewTask(long userId, int profile, long id, String task, int position) throws DuplicateIdException, PositionOutOfBoundException, InvalidActionException {
        if (id == 0)
            throw new InvalidActionException("Id of Task must not be null.");
        List<Task> entries = getTasks(userId, profile);
        if (entries.size() < position)
            throw new PositionOutOfBoundException("Position is out of bound: position" + position);
        if (entries.stream().map(Task::getId).toList().contains(id))
            throw new DuplicateIdException("Task with id already exists: id " + id);

        ContentValues values = new ContentValues();
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        values.put("ID", id);
        values.put("USER", userId);
        values.put("TASK", task);
        values.put("POSITION", position);
        values.put("PROFILE", profile);

        db.execSQL("UPDATE TASKS SET POSITION=POSITION+1 WHERE POSITION >=?", new String[]{String.valueOf(position)});
        long result = db.insert("TASKS", null, values);


        return new Task(userId, profile, id, task, position);
    }

    /**
     * @param Task
     */
    synchronized void removeTask(Task Task) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        if (Task.getUserId() == null)
            db.delete("TASKS", "ID=? AND USER IS NULL AND PROFILE=?", new String[]{String.valueOf(Task.getId()), String.valueOf(Task.getProfile())});
        else
            db.delete("TASKS", "ID=? AND USER=? AND PROFILE=?", new String[]{String.valueOf(Task.getId()), String.valueOf(Task.getUserId()), String.valueOf(Task.getProfile())});
        db.execSQL("UPDATE TASKS SET POSITION=POSITION-1 WHERE POSITION >?", new String[]{String.valueOf(Task.getPosition())});
        if (Task.getList() != null)
            db.execSQL("UPDATE TASKS SET LIST_POSITION=LIST_POSITION-1 WHERE LIST=? AND LIST_POSITION>?", new String[]{String.valueOf(Task.getList()), String.valueOf(Task.getListPosition())});
        ;
    }


    @Override
    public synchronized void removeTask(long userId, int profile, long id) {

        Task Task = getById(userId, profile, id);

        if (Task == null)
            return;

        removeTask(Task);
    }

    public synchronized void updateReminderDate(long userId, int profile, Task Task, Instant value) {
        updateReminderDate(userId, profile, Task.getId(), value);
    }

    @Override
    public synchronized void updateReminderDate(long userId, int profile, long id, Instant instant) {
        ContentValues values = new ContentValues();
        values.put("REMINDER_DATE", instant.toEpochMilli());
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.update("TASKS", values, "ID=?", new String[]{String.valueOf(id)});
        ;
    }

    synchronized void updateTask(long userId, int profile, Task Task, String value) {
        updateTask(userId, profile, Task.getId(), value);
    }

    @Override
    public synchronized void updateTask(long userId, int profile, long id, String value) {
        ContentValues values = new ContentValues();
        values.put("TASK", value == null ? "" : value);
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.update("TASKS", values, "ID=?", new String[]{String.valueOf(id)});
        ;
    }

    void updateRecurrence(Long userId, int profile, Task Task, String value) {
        updateRecurrence(userId, profile, Task.getId(), Task.getReminderDate(), value);
    }

    public synchronized void updateRecurrence(long userId, int profile, long id, Instant reminderDate, String value) {
        ContentValues values = new ContentValues();
        values.put("RECURRENCE", value);
        if (reminderDate == null)
            values.put("REMINDER_DATE", SQLiteHelper.dateToEpoch(Instant.now()));
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.update("TASKS", values, "ID=?", new String[]{String.valueOf(id)});
        ;

    }

    @Override
    public synchronized void updateRecurrence(long userId, int profile, long id, String value) {

        updateRecurrence(userId, profile, id, null, value);
    }


    void updateFocus(long userId, int profile, Task Task, boolean valueBool) {

        updateFocus(userId, profile, Task.getId(), valueBool);
    }

    @Override
    public synchronized void updateFocus(long userId, int profile, long id, boolean value) {

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.execSQL("UPDATE TASKS SET FOCUS=?, DROPPED = ? WHERE ID=?;", new String[]{String.valueOf(value ? 1 : 0), String.valueOf(0), String.valueOf(id)});
        ;
    }

    void updateDropped(Long userId, int profile, Task Task, boolean valueBool) {
        updateDropped(userId, profile, Task.getId(), valueBool);
    }

    @Override
    public synchronized void updateDropped(long userId, int profile, long id, boolean value) {

        //convert bool to int
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.execSQL("UPDATE TASKS SET DROPPED=? WHERE ID=?;", new String[]{String.valueOf(value ? 1 : 0), String.valueOf(id)});
        ;
    }


    @Override
    public Optional<Task> getTask(long userId, int profile, long id) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(profile), String.valueOf(id)});
        List<Task> entries = getListOfTasks(cursor);

        cursor.close();
        if (entries.size() > 1)
            throw new RuntimeException("Multiple entries with same id found for user " + userId);

        return !entries.isEmpty() ? Optional.of(entries.get(0)) : Optional.empty();
    }

    @Override
    public List<Task> getTasks(long userId, int profile) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ?", new String[]{String.valueOf(userId), String.valueOf(profile)});
        List<Task> entries = getListOfTasks(cursor);
        cursor.close();
        return entries.stream().sorted(Comparator.comparing(Task::getPosition)).toList();
    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public List<Task> loadEntries(long userId, int profile) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? ORDER BY POSITION", new String[]{String.valueOf(userId)});
        List<Task> entries = getListOfTasks(cursor);
        cursor.close();
        return entries;
    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public List<Task> loadFocus(long userId, int profile) {

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        long epoch = SQLiteHelper.dateToEpoch(Instant.now());

        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE FOCUS>0 OR (RECURRENCE IS NOT NULL AND REMINDER_DATE <= " + epoch + ")" +
                " ORDER BY POSITION", null);

        List<Task> entries = getListOfTasks(cursor);

        List<Long> removed = loadRecurring();

        cursor.close();
        ;

        return new ArrayList<>(entries.stream().filter(e -> e.getFocus() || !removed.contains(e.getId())).toList());
    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    @Override
    public List<Task> loadDropped(long userID, int profile) {

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ? AND DROPPED>0 ORDER BY POSITION",
                new String[]{String.valueOf(userID), String.valueOf(profile)});

        List<Task> entries = getListOfTasks(cursor);

        cursor.close();

        return entries;
    }

    /**
     * TODO DESCRIBE
     *
     * @param name
     * @return
     */
    public List<Task> loadList(Long userId, int profile, String name) {

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE LIST=? ORDER BY LIST_POSITION", new String[]{name});

        List<Task> entries = getListOfTasks(cursor);

        cursor.close();
        ;

        return entries;
    }

    //TODO COMMENT
    public static List<Task> getListOfTasks(Cursor cursor) {
        List<Task> entries = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            long userId = cursor.getLong(0);
            int profile = cursor.getInt(1);
            long id = cursor.getLong(2);
            String task = cursor.getString(3);
            boolean focus = SQLiteHelper.intToBool(cursor.getInt(4));
            boolean dropped = SQLiteHelper.intToBool(cursor.getInt(5));
            Long list = cursor.isNull(6) ? null : cursor.getLong(6);
            Integer listPosition = cursor.getInt(7);
            long reminderDateEpoch = cursor.getLong(8);
            String recurrence = cursor.getString(9);
            int position = cursor.getInt(10);

            //TODO ADD USERID!!!
            Task Task = new Task(userId, profile, id, task, position);
            Task.setFocus(focus);
            Task.setDropped(dropped);

            if (!(list == null)) {
                Task.setList(list);
                Task.setListPosition(listPosition);
            }


            if (reminderDateEpoch != 0) {
                Instant reminderDate = SQLiteHelper.epochToDate(reminderDateEpoch);

                if (reminderDate != null)
                    Task.setReminderDate(reminderDate);
            } else
                Task.setReminderDate(null);

            if (!(recurrence == null))
                Task.setRecurrence(recurrence);

            entries.add(Task);
            cursor.moveToNext();
        }

        return entries;

    }


    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public List<String> getLists() {

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT DISTINCT LIST FROM TASKS WHERE LIST IS NOT NULL ORDER BY LIST", null);

        cursor.moveToFirst();

        List<String> lists = new ArrayList<>();

        while (!cursor.isAfterLast()) {
            lists.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        ;

        return lists;

    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public Map<String, TaskList> loadLists() {
        Map<String, TaskList> lists = new Hashtable<>();

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM LISTS", null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            long id = cursor.getLong(0);
            String listName = cursor.getString(1);
            String color = cursor.getString(2);

            TaskList list = new TaskList(id, listName, color);
            lists.put(listName, list);
            cursor.moveToNext();
        }


        cursor.close();
        ;
        return lists;
    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public Map<String, String> getListColors() {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT NAME, COLOR FROM LISTS", null);

        cursor.moveToFirst();

        Map<String, String> map = new HashMap<>();

        while (!cursor.isAfterLast()) {
            map.put(cursor.getString(0), cursor.getString(1));
            cursor.moveToNext();
        }

        cursor.close();
        ;

        return map;
    }

    @Override
    public synchronized void swapTasks(long userId, int profile, long id, int position) throws PositionOutOfBoundException {

        swapTasks(getById(userId, profile, id), position);
    }

    synchronized void swapTasks(Task Task, int pos) throws PositionOutOfBoundException {
        if (Task == null)
            return;
        List<Task> entries = getTasks(Task.getUserId(), Task.getProfile());
        if (entries.size() <= pos)
            throw new PositionOutOfBoundException("Position is out of bound: position" + pos);

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        ContentValues values1 = new ContentValues();
        values1.put("POSITION", String.valueOf(Task.getPosition()));
        db.update("TASKS", values1, "USER = ? AND PROFILE = ? AND POSITION=?",
                new String[]{String.valueOf(Task.getUserId()), String.valueOf(Task.getProfile()), String.valueOf(pos)});

        ContentValues values0 = new ContentValues();
        values0.put("POSITION", String.valueOf(pos));
        db.update("TASKS", values0, "USER = ? AND PROFILE = ? AND ID=?",
                new String[]{String.valueOf(Task.getUserId()), String.valueOf(Task.getProfile()), String.valueOf(Task.getId())});

        //clean up
        ;
    }

    /**
     * TODO DESCRIBE
     *
     * @param arrayList
     */
    void saveRecurring(List<Long> arrayList) {
        ByteBuffer buffer = ByteBuffer.allocate(arrayList.size() * Long.BYTES);
        for (long i : arrayList)
            buffer.putLong(i);

        try {
            Files.write(Paths.get(sqLiteHelper.getContext().getFilesDir() + "/" + LocalDate.now()), buffer.array());
        } catch (IOException ignored) {
        }

    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public List<Long> loadRecurring() {

        Context context = sqLiteHelper.getContext();
        //get all file names in files directory
        String[] fileNames = context.getFilesDir().list();

        //get them as File[]
        File[] files = context.getFilesDir().listFiles();

        //loop through all files and delete obsolete ones
        for (int i = 0; i < Objects.requireNonNull(fileNames).length; i++) {

            //ignore file that stores information whether app is in test mode
            if (fileNames[i].equals("mode"))
                continue;

            try {
                //if file name (which is a date) is smaller than today, delete it
                if (LocalDate.parse(fileNames[i]).isBefore(LocalDate.now())) {
                    assert files != null;
                    if (files[i] != null) files[i].delete();
                }

                // in old versions the file was stored under a different naming logic this block removes old files
            } catch (DateTimeParseException e) {
                assert files != null;
                files[i].delete();
            }

        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(context.getFilesDir() + "/" + LocalDate.now()));
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            List<Long> list = new ArrayList<>();
            while (buffer.hasRemaining())
                list.add(buffer.getLong());
            return list;

        } catch (IOException ignored) {
            return new ArrayList<>();
        }

    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public List<Task> getEntriesOrderedByDate() {

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM TASKS ORDER BY REMINDER_DATE", null);

        List<Task> entries = getListOfTasks(cursor);
        cursor.close();
        ;

        return entries;

    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public List<Task> getNoList() {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE LIST IS NULL ORDER BY POSITION", null);

        List<Task> entries = getListOfTasks(cursor);
        cursor.close();
        ;

        return entries;
    }

    /**
     * TODO DESCRIBE
     *
     * @return
     */
    public List<Task> loadTasksToPick() {

        List<Task> tasksToPick;

        long epoch = SQLiteHelper.dateToEpoch(Instant.now());

        /*
         *  for all entries:
         *
         *  1. check if task is in focus, if yes -> add
         *
         *  2. else check if task has a date
         *
         *      2.1 if yes and it has been dropped or no list is set -> add
         *          (the list part is necessary because tasks might have been edited and are neither
         *           dropped, focused or have a date. So they would never show up)
         *
         *  3. else check if reminder date <= today, if yes -> add
         */

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();


        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE RECURRENCE IS NULL AND (FOCUS>0 OR " +
                "(REMINDER_DATE IS NULL AND (DROPPED>0 OR LIST=NULL)) OR " +
                "(REMINDER_DATE IS NOT NULL AND REMINDER_DATE<=" + epoch + ")) ORDER BY POSITION", null);


        tasksToPick = getListOfTasks(cursor);

        cursor.close();
        ;

        return tasksToPick;
    }

    @Override
    public synchronized void updateId(long userId, int profile, long Task, long id) throws DuplicateIdException {

        if (getTask(userId, profile, id).isPresent())
            throw new DuplicateIdException("New id for Task already exists: id " + id);
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ID", id);
        db.update("TASKS", values, "USER = ? AND PROFILE = ? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(profile), String.valueOf(Task)});
    }

    @Override
    public void postTask(Task Task) {

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        System.out.println("POST " + Task.getId());
        values.put("ID", Task.getId());
        values.put("USER", Task.getUserId());
        values.put("PROFILE", Task.getProfile());
        values.put("TASK", Task.getTask());
        values.put("POSITION", Task.getPosition());
        values.put("FOCUS", SQLiteHelper.boolToInt(Task.getFocus()));
        values.put("DROPPED", SQLiteHelper.boolToInt(Task.getDropped()));
        values.put("LIST", Task.getList());
        values.put("LIST_POSITION", Task.getListPosition());
        values.put("REMINDER_DATE", Task.getReminderDate() == null ? null : Task.getReminderDate().toEpochMilli());
        values.put("RECURRENCE", Task.getRecurrence());
        db.insert("TASKS", "", values);
    }
}

