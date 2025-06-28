package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation of the TaskManagerI interface using SQLite and the ZenSQLiteHelper.
 */
public class TaskManager implements TaskManagerI {


    private final ZenSQLiteHelper zenSqLiteHelper;

    /**
     * Crate new instance of TaskManager.
     * @param zenSqLiteHelper the helper for interacting with the database
     */
    public TaskManager(ZenSQLiteHelper zenSqLiteHelper) {
        this.zenSqLiteHelper = zenSqLiteHelper;
    }

    /**
     * Get the task by id.
     * @param user the user id
     * @param profile the profile id
     * @param task the task id
     * @return the task
     */
    private Optional<Task> getById(long user, int profile, long task) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER=? AND PROFILE=? AND ID=?", new String[]{
                String.valueOf(user),
                String.valueOf(profile),
                String.valueOf(task)
        });
        List<Task> tasks = getListOfTasks(cursor);
        cursor.close();
        if (tasks.isEmpty())
            return Optional.empty();
        return Optional.of(tasks.get(0));
    }

    @Override
    public synchronized Task addNewTask(long userId, int profile, String task) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM TASKS WHERE USER=? AND PROFILE=?", new String[]{
                String.valueOf(userId),
                String.valueOf(profile)
        });
        cursor.moveToFirst();
        int maxPosition = -1;
        if (!cursor.isAfterLast()) {
            cursor.close();
            cursor = db.rawQuery("SELECT MAX(POSITION) FROM TASKS WHERE USER=? AND PROFILE=?", new String[]{
                    String.valueOf(userId),
                    String.valueOf(profile)
            });
            cursor.moveToFirst();
            if (!cursor.isAfterLast())
                maxPosition = cursor.getInt(0);
        }
        cursor.close();
        Task newTask = null;
        try {
            newTask = addNewTask(userId, profile, task, maxPosition + 1);
        } catch (PositionOutOfBoundException _) {
            System.out.println("OUT OF BOUNDS");
        }
        return newTask;
    }

    @Override
    public synchronized Task addNewTask(long userId, int profile, String task, int position) throws PositionOutOfBoundException {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Random random = new Random();
        long id = random.nextInt();
        while (id == 0) //id cannot be 0
            id = random.nextInt();
        //while there is task with id for the user profile get new ids
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
        Task newTask = null;
        try {
            newTask = addNewTask(userId, profile, id, task, position);
        } catch (DuplicateIdException | InvalidActionException e) {
            Log.e("ZenToDo", "There was an exception when adding a new task", e);
        }
        return newTask;
    }

    @Override
    public synchronized Task addNewTask(long userId, int profile, long id, String task, int position) throws DuplicateIdException, PositionOutOfBoundException, InvalidActionException {
        if (id == 0)
            throw new InvalidActionException("Id of Task must not be null.");
        List<Task> tasks = getTasks(userId, profile);
        if (tasks.size() < position-1)
            throw new PositionOutOfBoundException("Position is out of bound: position" + position);
        if (tasks.stream().map(Task::getId).toList().contains(id))
            throw new DuplicateIdException("Task with id already exists: id " + id);
        ContentValues values = new ContentValues();
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        values.put("ID", id);
        values.put("USER", userId);
        values.put("TASK", task);
        values.put("POSITION", position);
        values.put("PROFILE", profile);
        db.execSQL("UPDATE TASKS SET POSITION=POSITION+1 WHERE USER=? AND PROFILE=? AND POSITION >=?", new String[]{
                String.valueOf(userId),
                String.valueOf(profile),
                String.valueOf(position)
        });
        db.insert("TASKS", null, values);
        return new Task(userId, profile, id, task, position);
    }

    /**
     * Remove a task.
     * @param task task to remove
     */
    synchronized void removeTask(Task task) {
        //TODO DECREMENT POSITION DOES NOT WORK
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        db.delete("TASKS", "ID=? AND USER=? AND PROFILE=?", new String[]{String.valueOf(
                task.getId()),
                String.valueOf(task.getUserId()),
                String.valueOf(task.getProfile())});
        //adjust positions for tasks having position greater than the task removed
        db.execSQL("UPDATE TASKS SET POSITION=POSITION-1 WHERE USER=? AND PROFILE=? AND POSITION >?", new String[]{
                String.valueOf(task.getUserId()),
                String.valueOf(task.getProfile()),
                String.valueOf(task.getPosition())
        });
        //adjust list positions if list exists
        if (task.getList() != null)
            db.execSQL("UPDATE TASKS SET LIST_POSITION=LIST_POSITION-1 WHERE LIST=? AND LIST_POSITION>?", new String[]{
                    String.valueOf(task.getList()),
                    String.valueOf(task.getListPosition())
            });
    }

    @Override
    public synchronized void removeTask(long userId, int profile, long id) {
        Optional<Task> task = getById(userId, profile, id);
        if (task.isEmpty())
            return;
        removeTask(task.get());
    }

    @Override
    public synchronized void updateReminderDate(long user, int profile, long id, Instant instant) {
        if (instant == null)
            return;
        ContentValues values = new ContentValues();
        values.put("REMINDER_DATE", instant.toEpochMilli());
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        db.update("TASKS", values, "USER=? AND PROFILE=? AND ID=?", new String[]{
                String.valueOf(user),
                String.valueOf(profile),
                String.valueOf(id)
        });
    }

    @Override
    public synchronized void updateTask(long user, int profile, long id, String value) {
        ContentValues values = new ContentValues();
        values.put("TASK", value == null ? "" : value);
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        db.update("TASKS", values, "USER=? AND PROFILE=? AND ID=?", new String[]{
                String.valueOf(user),
                String.valueOf(profile),
                String.valueOf(id)
        });
    }

    /**
     * Update the reminder date and the recurrence field of a task.
     * @param user the user id
     * @param profile the profile id
     * @param task the task id
     * @param reminderDate the reminder date
     * @param recurrence the recurrence
     */
    public synchronized void updateRecurrenceAndReminderDate(long user, int profile, long task, Instant reminderDate, String recurrence) {
        ContentValues values = new ContentValues();
        values.put("RECURRENCE", recurrence);
        if (reminderDate == null)
            values.put("REMINDER_DATE", ZenSQLiteHelper.dateToEpoch(Instant.now()));
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        db.update("TASKS", values, "USER=? AND PROFILE=? AND ID=?", new String[]{
                String.valueOf(user),
                String.valueOf(profile),
                String.valueOf(task)
        });
    }

    @Override
    public synchronized void updateRecurrence(long userId, int profile, long id, String value) {
        updateRecurrenceAndReminderDate(userId, profile, id, null, value);
    }

    @Override
    public synchronized void updateFocus(long userId, int profile, long id, boolean value) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        db.execSQL("UPDATE TASKS SET FOCUS=?, DROPPED = ? WHERE USER=? AND PROFILE=? AND ID=?;", new String[]{
                String.valueOf(value ? 1 : 0),
                String.valueOf(0),
                String.valueOf(userId),
                String.valueOf(profile),
                String.valueOf(id)
        });
    }

    @Override
    public synchronized void updateDropped(long user, int profile, long task, boolean value) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        db.execSQL("UPDATE TASKS SET DROPPED=? WHERE USER=? AND PROFILE=? AND ID=?;", new String[]{
                String.valueOf(value ? 1 : 0),
                String.valueOf(user),
                String.valueOf(profile),
                String.valueOf(task)
        });
    }

    @Override
    public Optional<Task> getTask(long user, int profile, long task) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ? AND ID = ?", new String[]{
                String.valueOf(user),
                String.valueOf(profile),
                String.valueOf(task)
        });
        List<Task> tasks = getListOfTasks(cursor);
        cursor.close();
        if (tasks.size() > 1)
            throw new RuntimeException("Multiple tasks with same id found for user " + user);
        return !tasks.isEmpty() ? Optional.of(tasks.get(0)) : Optional.empty();
    }

    @Override
    public List<Task> getTasks(long user, int profile) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ?", new String[]{
                String.valueOf(user),
                String.valueOf(profile)
        });
        List<Task> tasks = getListOfTasks(cursor);
        cursor.close();
        return tasks.stream().sorted(Comparator.comparing(Task::getPosition)).toList();
    }

    /**
     * Load all tasks for the user profile.
     * @param user the user id
     * @param profile the profile id
     * @return a list of tasks for the user profile
     */
    public List<Task> loadEntries(long user, int profile) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER=? AND PROFILE=? ORDER BY POSITION", new String[]{
                String.valueOf(user),
                String.valueOf(profile)
        });
        List<Task> tasks = getListOfTasks(cursor);
        cursor.close();
        return tasks;
    }

    /**
     * Load all tasks that are in FOCUS mode.
     * @param user the user id
     * @param profile the profile id
     * @return a list of tasks in FOCUS mode for the user profile
     */
    public List<Task> loadFocus(long user, int profile) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        long epoch = ZenSQLiteHelper.dateToEpoch(Instant.now());
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER=? AND PROFILE=? AND " +
                "(FOCUS>0 OR (RECURRENCE IS NOT NULL AND REMINDER_DATE <= " + epoch + "))" +
                " ORDER BY POSITION", new String[]{
                String.valueOf(user),
                String.valueOf(profile)
        });
        List<Task> tasks = getListOfTasks(cursor);
        List<Long> removed = loadRecurring();
        cursor.close();
        //Remove all tasks that are recurring but have been removed
        return new ArrayList<>(tasks.stream().filter(e -> e.getFocus() || !removed.contains(e.getId())).toList());
    }

    @Override
    public List<Task> loadDropped(long userID, int profile) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER = ? AND PROFILE = ? AND DROPPED>0 ORDER BY POSITION",
                new String[]{String.valueOf(userID), String.valueOf(profile)});
        List<Task> tasks = getListOfTasks(cursor);
        cursor.close();
        return tasks;
    }


    /**
     * Retrieve a list of tasks from a cursor.
     * @param cursor the cursor to read from
     * @return a list of tasks
     */
    public static List<Task> getListOfTasks(Cursor cursor) {
        List<Task> tasks = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long userId = cursor.getLong(0);
            int profile = cursor.getInt(1);
            long id = cursor.getLong(2);
            String task = cursor.getString(3);
            boolean focus = ZenSQLiteHelper.intToBool(cursor.getInt(4));
            boolean dropped = ZenSQLiteHelper.intToBool(cursor.getInt(5));
            Long list = cursor.isNull(6) ? null : cursor.getLong(6);
            Integer listPosition = cursor.getInt(7);
            long reminderDateEpoch = cursor.getLong(8);
            String recurrence = cursor.getString(9);
            int position = cursor.getInt(10);
            Task Task = new Task(userId, profile, id, task, position);
            Task.setFocus(focus);
            Task.setDropped(dropped);
            if (!(list == null)) {
                Task.setList(list);
                Task.setListPosition(listPosition);
            }
            if (reminderDateEpoch != 0) {
                Instant reminderDate = ZenSQLiteHelper.epochToDate(reminderDateEpoch);
                if (reminderDate != null)
                    Task.setReminderDate(reminderDate);
            } else
                Task.setReminderDate(null);
            if (!(recurrence == null))
                Task.setRecurrence(recurrence);
            tasks.add(Task);
            cursor.moveToNext();
        }
        return tasks;
    }

    @Override
    public synchronized void swapTasks(long userId, int profile, long id, int position) throws PositionOutOfBoundException {
        Optional<Task> task = getById(userId, profile, id);
        if (task.isEmpty())
            return;
        swapTasks(task.get(), position);
    }

    synchronized void swapTasks(Task Task, int pos) throws PositionOutOfBoundException {
        if (Task == null)
            return;
        List<Task> tasks = getTasks(Task.getUserId(), Task.getProfile());
        if (tasks.size() <= pos)
            throw new PositionOutOfBoundException("Position is out of bound: position" + pos);
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values1 = new ContentValues();
        values1.put("POSITION", String.valueOf(Task.getPosition()));
        db.update("TASKS", values1, "USER = ? AND PROFILE = ? AND POSITION=?",
                new String[]{String.valueOf(Task.getUserId()), String.valueOf(Task.getProfile()), String.valueOf(pos)});
        ContentValues values0 = new ContentValues();
        values0.put("POSITION", String.valueOf(pos));
        db.update("TASKS", values0, "USER = ? AND PROFILE = ? AND ID=?",
                new String[]{String.valueOf(Task.getUserId()), String.valueOf(Task.getProfile()), String.valueOf(Task.getId())});
        db.close();
    }

    /**
     * Save recurring tasks that have been deselected.
     * @param arrayList list of ids to save
     */
    void saveRecurring(List<Long> arrayList) {
        ByteBuffer buffer = ByteBuffer.allocate(arrayList.size() * Long.BYTES);
        for (long i : arrayList)
            buffer.putLong(i);
        try {
            Files.write(Paths.get(zenSqLiteHelper.getContext().getFilesDir() + "/" + LocalDate.now()), buffer.array());
        } catch (IOException _) {}
    }

    /**
     * Load recurring tasks that have been deselected.
     * @return list of ids of tasks
     */
    public List<Long> loadRecurring() {
        Context context = zenSqLiteHelper.getContext();
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
     * Get tasks ordered by date.
     * @param user the user id
     * @param profile the profile id
     * @return a list of tasks ordered by date
     */
    public List<Task> getEntriesOrderedByDate(long user, int profile) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER=? AND PROFILE=? ORDER BY REMINDER_DATE", new String[]{
                String.valueOf(user),
                String.valueOf(profile)
        });
        List<Task> tasks = getListOfTasks(cursor);
        cursor.close();
        return tasks;
    }

    /**
     * Get all task that have no list.
     * @param user the user id
     * @param profile the profile id
     * @return a list of tasks without a list
     */
    public List<Task> getNoList(long user, int profile) {
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE LIST IS NULL AND USER=? AND PROFILE=? ORDER BY POSITION", new String[]{
                String.valueOf(user),
                String.valueOf(profile)
        });
        List<Task> tasks = getListOfTasks(cursor);
        cursor.close();
        return tasks;
    }

    /**
     * Retrieve all tasks for PICK.
     * @return a list of tasks that can be picked
     */
    public List<Task> loadTasksToPick(long user, int profile) {
        List<Task> tasksToPick;
        long epoch = ZenSQLiteHelper.dateToEpoch(Instant.now());
        /*
         *  for all tasks:
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
        SQLiteDatabase db = zenSqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER=? AND PROFILE=? AND " +
                "RECURRENCE IS NULL AND (FOCUS>0 OR " +
                "(REMINDER_DATE IS NULL AND (DROPPED>0 OR LIST=NULL)) OR " +
                "(REMINDER_DATE IS NOT NULL AND REMINDER_DATE<=" + epoch + ")) ORDER BY POSITION", new String[]{
                        String.valueOf(user),
                        String.valueOf(profile)
        });
        tasksToPick = getListOfTasks(cursor);
        cursor.close();
        return tasksToPick;
    }

    @Override
    public synchronized void updateId(long userId, int profile, long task, long id) throws DuplicateIdException {
        if (getTask(userId, profile, id).isPresent())
            throw new DuplicateIdException("New id for Task already exists: id " + id);
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ID", id);
        db.update("TASKS", values, "USER = ? AND PROFILE = ? AND ID = ?", new String[]{
                String.valueOf(userId),
                String.valueOf(profile),
                String.valueOf(task)});
    }

    @Override
    public void postTask(Task task) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        System.out.println("POST " + task.getId());
        values.put("ID", task.getId());
        values.put("USER", task.getUserId());
        values.put("PROFILE", task.getProfile());
        values.put("TASK", task.getTask());
        values.put("POSITION", task.getPosition());
        values.put("FOCUS", ZenSQLiteHelper.boolToInt(task.getFocus()));
        values.put("DROPPED", ZenSQLiteHelper.boolToInt(task.getDropped()));
        values.put("LIST", task.getList());
        values.put("LIST_POSITION", task.getListPosition());
        values.put("REMINDER_DATE", task.getReminderDate() == null ? null : task.getReminderDate().toEpochMilli());
        values.put("RECURRENCE", task.getRecurrence());
        db.insert("TASKS", "", values);
    }
}

