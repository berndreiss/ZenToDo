package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Path;
import android.hardware.camera2.CameraExtensionSession;
import android.provider.ContactsContract;
import android.util.Log;

import net.berndreiss.zentodo.MainActivity;
import net.berndreiss.zentodo.OperationType;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenServerMessage;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.SequencedSet;

/**
 * TODO DESCRIBE
 */
public class SQLiteHelper extends SQLiteOpenHelper implements Database {

    private final Context context;

    public SQLiteHelper(Context context, String databaseName){
        super(context, databaseName == null ? MainActivity.DATABASE_NAME : databaseName,null, MainActivity.DATABASE_VERSION);
        this.context = context;
    }

    public SQLiteHelper(Context context){
        this(context, null);
    }

    //Create TABLE_ENTRIES for entries TABLE_LISTS for lists onCreate
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE ENTRIES ("
                + "USER INTEGER DEFAULT NULL,"
                + "PROFILE INTEGER NOT NULL,"
                + "ID INTEGER NOT NULL, "
                + "TASK TEXT NOT NULL, "
                + "FOCUS INTEGER DEFAULT 0, "
                + "DROPPED INTEGER DEFAULT 1, "
                + "LIST TEXT DEFAULT NULL, "
                + "LIST_POSITION INTEGER DEFAULT NULL, "
                + "REMINDER_DATE INTEGER DEFAULT NULL,"
                + "RECURRENCE TEXT DEFAULT NULL,"
                + "POSITION INTEGER NOT NULL,"
                + "PRIMARY KEY (ID, USER),"
                + "FOREIGN KEY (LIST) REFERENCES LISTS(NAME),"
                + "FOREIGN KEY (USER) REFERENCES USERS(MAIL)"
                + ")");

        String query = "CREATE TABLE LISTS (" +
                "NAME TEXT PRIMARY KEY, " +
                "COLOR TEXT DEFAULT '" + ListTaskListAdapter.DEFAULT_COLOR + "'" +
                ")";
        db.execSQL(query);

        query = "CREATE TABLE USERS (" +
                "ID INTEGER PRIMARY KEY, " +
                "MAIL TEXT NOT NULL, " +
                "NAME TEXT DEFAULT NULL, " +
                "ENABLED INTEGER DEFAULT 0, " +
                "DEVICE INTEGER NOT NULL, " +
                "PROFILE INTEGER NOT NULL, " +
                "CLOCK TEXT NOT NULL" +
                ")";
        db.execSQL(query);


        query = "CREATE TABLE PROFILES (" +
                "ID INTEGER PRIMARY KEY, " +
                "NAME TEXT DEFAULT 'Default', " +
                "USER INTEGER DEFAULT NULL," +
                "FOREIGN KEY (USER) REFERENCES USERS(ID)" +
                ")";
        db.execSQL(query);

        query = "CREATE TABLE QUEUE (" +
                "TYPE INTEGER NOT NULL, " +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TIMESTAMP INTEGER NOT NULL, " +
                "USER_ID INTEGER NOT NULL, " +
                "ARGUMENTS TEXT NOT NULL," +
                "CLOCK TEXT NOT NULL" +
                ")";
        db.execSQL(query);

        query = "CREATE TABLE TOKENS (" +
                "USER INTEGER PRIMARY KEY, " +
                "TOKEN TEXT NOT NULL" +
                ")";
        db.execSQL(query);

        query = "CREATE INDEX IDX_FOCUS ON ENTRIES(FOCUS)";
        db.execSQL(query);
        query = "CREATE INDEX IDX_DROPPED ON ENTRIES(DROPPED)";
        db.execSQL(query);
        query = "CREATE INDEX IDX_LIST ON ENTRIES(LIST, LIST_POSITION)";
        db.execSQL(query);
        query = "CREATE INDEX IDX_REMINDER_DATE ON ENTRIES(REMINDER_DATE)";
        db.execSQL(query);
        query = "CREATE INDEX IDX_POSITION ON ENTRIES(POSITION)";
        db.execSQL(query);


        //TODO ADD METADATA TABLE WITH VERSION AND TIMEDELAY
        //TODO ADD QUEUE TABLE FOR CHANGES
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
    }

    private Entry getById(Long userId, long profile, long id){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE ID=?", new String[]{String.valueOf(id)});

        List<Entry> entries = getListOfEntries(cursor);

        cursor.close();

        if (entries.isEmpty())
            return null;

        return entries.get(0);
    }

    @Override
    public void post(List<Entry> entries){

        //TODO implement
    }


    /**
     *
     * @param task
     * @return
     */
    @Override
    public Entry addNewEntry(Long userId, long profile, String task){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT 1 FROM ENTRIES", null);

        cursor.moveToFirst();

        int maxPosition = -1;
        if (!cursor.isAfterLast()){

            cursor.close();

            cursor = db.rawQuery("SELECT MAX(POSITION) FROM ENTRIES", null);

            cursor.moveToFirst();


            if (!cursor.isAfterLast())
                maxPosition = cursor.getInt(0);
        }
        cursor.close();

        return addNewEntry(userId, profile, task, maxPosition + 1);
    }
    @Override
    public Entry addNewEntry(Long userId, long profile, String task, int position){
        SQLiteDatabase db = this.getWritableDatabase();
        Random random = new Random();
        long id = random.nextInt();

        while (true){
            Cursor cursorId = db.rawQuery("SELECT ID FROM ENTRIES WHERE ID=" + id, null);

            cursorId.moveToFirst();

            if (!cursorId.isAfterLast()) {
                id = random.nextInt();
                cursorId.close();
            }
            else {
                cursorId.close();
                break;
            }
        }
        return addNewEntry(userId, profile, id, task, position);

    }

    //adds new entry to TABLE_ENTRIES
    @Override
    public Entry addNewEntry(Long userId, long profile, long id, String task, int position) {

        System.out.println("ADDING ENTRY " + task + " WITH ID " + id + " FOR USER " + userId + " FOR PROFILE " + profile);

        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();


        values.put("ID",id);
        if (userId != null)
            values.put("USER", userId);
        values.put("TASK",task);
        values.put("POSITION", position);
        values.put("PROFILE", profile);

        db.execSQL("UPDATE ENTRIES SET POSITION=POSITION+1 WHERE POSITION >=?", new String[]{String.valueOf(position)});
        db.insert("ENTRIES",null,values);

        return new Entry(userId, profile, id, task, position);
    }

    /**
     *
     * @param entry
     */
    void removeEntry(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (entry.getUserId() == null)
            db.delete("ENTRIES", "ID=? AND USER IS NULL AND PROFILE=?", new String[]{String.valueOf(entry.getId()), String.valueOf(entry.getProfile())});
        else
            db.delete("ENTRIES", "ID=? AND USER=? AND PROFILE=?", new String[]{String.valueOf(entry.getId()), String.valueOf(entry.getUserId()), String.valueOf(entry.getProfile())});
        db.execSQL("UPDATE ENTRIES SET POSITION=POSITION-1 WHERE POSITION >?", new String[]{String.valueOf(entry.getPosition())});
        if (entry.getList() != null)
            db.execSQL("UPDATE ENTRIES SET LIST_POSITION=LIST_POSITION-1 WHERE LIST=? AND LIST_POSITION>?", new String[]{entry.getList(), String.valueOf(entry.getListPosition())});
        ;
    }


    @Override
    public void removeEntry(Long userId, long profile, long id){

        Entry entry = getById(userId, profile, id);

        if (entry == null)
            return;

        removeEntry(entry);
    }

    public void updateReminderDate(Long userId, long profile, Entry entry, Instant value){
        updateReminderDate(userId, profile, entry.getId(), value == null ? null : value.getEpochSecond());
    }

    @Override
    public void updateReminderDate(Long userId, long profile, long id, Long value){
        ContentValues values = new ContentValues();
        values.put("REMINDER_DATE", value);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(id)});
        ;
    }

    void updateTask(Long userId, long profile, Entry entry, String value){
        updateTask(userId, profile, entry.getId(), value);
    }

    @Override
    public void updateTask(Long userId, long profile, long id, String value){
        ContentValues values = new ContentValues();
        values.put("TASK", value == null ? "" : value);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(id)});
        ;
    }

    void updateRecurrence(Long userId, long profile, Entry entry, String value){
        updateRecurrence(userId, profile, entry.getId(), entry.getReminderDate() == null ? null : dateToEpoch(entry.getReminderDate()), value);
    }

    @Override
    public void updateRecurrence(Long userId, long profile, long id, Long reminderDate, String value){
        ContentValues values = new ContentValues();
        values.put("RECURRENCE", value);
        if (reminderDate == null)
            values.put("REMINDER_DATE", dateToEpoch(Instant.now()));
        SQLiteDatabase db = this.getWritableDatabase();
        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(id)});
        ;

    }

    @Override
    public void updateList(Long userId, long profile, long id, String name, int position){
        updateList(getById(userId, profile, id), name, position);
    }

    public void updateList(Entry entry, String name){
        updateList(entry, name, null);
    }

    public void updateList(Entry entry, String name, Integer position){

        if (entry == null)
            return;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (position != null)
            db.execSQL("UPDATE ENTRIES SET LIST_POSITION = LIST_POSITION + 1 WHERE LIST=?", new String[]{name});

        if (name != null && position == null) {
            position = 0;
            Cursor cursor = db.rawQuery("SELECT 1 FROM ENTRIES WHERE LIST=?", new String[]{name});

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                cursor.close();


                cursor = db.rawQuery("SELECT MAX(LIST_POSITION) FROM ENTRIES WHERE LIST=?", new String[]{name});

                cursor.moveToFirst();

                if (!cursor.isAfterLast()) {
                    position = cursor.getInt(0) + 1;
                }
            }
            cursor.close();
        }

        values.put("LIST", name);
        values.put("LIST_POSITION", position);

        entry.setListPosition(position);

        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(entry.getId())});


        if (entry.getList() != null && !Objects.equals(entry.getList(), name)) {
            db.execSQL("UPDATE ENTRIES SET LIST_POSITION=LIST_POSITION - 1 WHERE LIST=? AND LIST_POSITION >?", new String[]{entry.getList(), String.valueOf(entry.getListPosition())});
            db.execSQL("DELETE FROM LISTS WHERE NOT EXISTS ( SELECT 1 FROM ENTRIES WHERE LIST=NAME AND LIST=?)", new String[]{entry.getList()});
        }

        ;
    }

    void updateFocus(Long userId, long profile, Entry entry, boolean valueBool){

        updateFocus(userId, profile, entry.getId(), boolToInt(valueBool));
    }

    @Override
    public void updateFocus(Long userId, long profile, long id, int value){

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ENTRIES SET FOCUS=? WHERE ID=?;", new String[]{String.valueOf(value), String.valueOf(id)});
        ;
    }

    void updateDropped(Long userId, long profile, Entry entry, boolean valueBool){
        updateDropped(userId, profile,  entry.getId(), boolToInt(valueBool));
    }

    @Override
    public void updateDropped(Long userId, long profile, long id, int value){

        //convert bool to int
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ENTRIES SET DROPPED=? WHERE ID=?;",  new String[]{String.valueOf(value), String.valueOf(id)});
        ;
    }

    @Override
    public void updateListColor(Long userid, long profile, String list, String color){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("COLOR", color);
        int updatedRows = db.update("LISTS", values, "NAME=?", new String[]{list});

        if (updatedRows==0){
            values.put("NAME", list);
            db.insert("LISTS", null, values);
        }

        ;
    }

    @Override
    public void updateUserName(Long l, String s) {

    }

    @Override
    public boolean updateEmail(Long userId, String s) {
        return false;
    }

    @Override
    public List<User> getUsers() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM USERS", null);

        List<User> users = getListOfUsers(cursor);

        cursor.close();
        ;

        return users;
    }

    @Override
    public List<Profile> getProfiles(Long aLong) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Entry> getEntry(Long userId, long profile, long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        System.out.println("LOOKING FOR ENTRY WITH ID " + id + " USER " + userId + " PROFILE " + profile);

        Cursor cursor;
        if (userId == null)
            cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE USER IS NULL AND PROFILE = ? AND ID = ?", new String[]{String.valueOf(profile), String.valueOf(id)});
        else
            cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE USER = ? AND PROFILE = ? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(profile), String.valueOf(id)});
        List<Entry> entries = getListOfEntries(cursor);

        System.out.println("SIZE ENTRIES: " + entries.size());
        cursor.close();
        if (entries.size() > 1)
            throw new RuntimeException("Multiple entries with same id found for user " + userId);

        return !entries.isEmpty() ? Optional.of(entries.get(0)) : Optional.empty();
    }

    @Override
    public List<Entry> getEntries(Long userId, long profile) {
        return Collections.emptyList();
    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Entry> loadEntries(Long userId, long profile){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE USER = ? ORDER BY POSITION", new String[]{String.valueOf(userId)});

        List<Entry> entries = getListOfEntries(cursor);

        cursor.close();
        ;

        return entries;
    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Entry> loadFocus(Long userId, long profile){

        SQLiteDatabase db = this.getReadableDatabase();

        long epoch = dateToEpoch(Instant.now());

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE FOCUS>0 OR (RECURRENCE IS NOT NULL AND REMINDER_DATE <= " + epoch +  ")" +
                " ORDER BY POSITION", null);

        List<Entry> entries = getListOfEntries(cursor);

        List<Long> removed = loadRecurring();

        cursor.close();
        ;

        return new ArrayList<>(entries.stream().filter(e -> e.getFocus() || !removed.contains(e.getId())).toList());
    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Entry> loadDropped(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE DROPPED>0 ORDER BY POSITION", null);

        List<Entry> entries = getListOfEntries(cursor);

        cursor.close();
        ;

        return entries;
    }

    /**
     * TODO DESCRIBE
     * @param name
     * @return
     */
    public List<Entry> loadList(Long userId, long profile, String name){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE LIST=? ORDER BY LIST_POSITION", new  String[]{name});

        List<Entry> entries = getListOfEntries(cursor);

        cursor.close();
        ;

        return entries;
    }

    //TODO COMMENT
    private List<Entry> getListOfEntries(Cursor cursor){
        List<Entry> entries = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){

            Long userId = cursor.getLong(0);
            long profile = cursor.getLong(1);
            long id = cursor.getInt(2);
            String task = cursor.getString(3);
            boolean focus = intToBool(cursor.getInt(4));
            boolean dropped = intToBool(cursor.getInt(5));
            String list = cursor.getString(6);
            Integer listPosition = cursor.getInt(7);
            long reminderDateEpoch = cursor.getLong(8);
            String recurrence = cursor.getString(9);
            int position = cursor.getInt(10);

            //TODO ADD USERID!!!
            Entry entry = new Entry(userId, profile, id, task, position);
            entry.setFocus(focus);
            entry.setDropped(dropped);

            if (!(list==null)) {
                entry.setList(list);
                entry.setListPosition(listPosition);
            }


            if (reminderDateEpoch != 0) {
                Instant reminderDate = epochToDate(reminderDateEpoch);

                if (reminderDate != null)
                    entry.setReminderDate(reminderDate);
            }else
                entry.setReminderDate(null);

            if (!(recurrence==null))
                entry.setRecurrence(recurrence);

            entries.add(entry);
            cursor.moveToNext();
        }

        return entries;

    }

    private List<User> getListOfUsers(Cursor cursor){
        List<User> users = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){

            long id = cursor.getInt(0);
            String email = cursor.getString(1);
            String userName = cursor.getString(2);
            boolean enabled = intToBool(cursor.getInt(3));
            int device = cursor.getInt(4);
            long profile = cursor.getLong(5);

            //TODO ADD USERID!!!
            User user = new User(email, userName, device);
            user.setId(id);
            user.setEnabled(enabled);
            user.setProfile(profile);
            users.add(user);
            cursor.moveToNext();
        }

        return users;

    }
    private List<Profile> getListOfProfiles(Cursor cursor){
        List<Profile> profiles = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){

            long id = cursor.getLong(0);
            String name = cursor.getString(1);
            Profile profile = new Profile(name);
            profile.setId(id);

            profiles.add(profile);
            cursor.moveToNext();
        }

        return profiles;

    }
    /**
     * TODO DESCRIBE
     * @return
     */
    public List<String> getLists(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT DISTINCT LIST FROM ENTRIES WHERE LIST IS NOT NULL ORDER BY LIST", null);

        cursor.moveToFirst();

        List<String> lists = new ArrayList<>();

        while(!cursor.isAfterLast()) {
            lists.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        ;

        return lists;

    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public Map<String, TaskList> loadLists(){
        Map<String, TaskList> lists = new Hashtable<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM LISTS", null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            String listName = cursor.getString(0);
            String color = cursor.getString(1);

            TaskList list = new TaskList(color);
            lists.put(listName, list);
            cursor.moveToNext();
        }


        cursor.close();
        ;
        return lists;
    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public Map<String, String> getListColors(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT NAME, COLOR FROM LISTS", null);

        cursor.moveToFirst();

        Map<String, String> map = new HashMap<>();

        while (!cursor.isAfterLast()){
            map.put(cursor.getString(0), cursor.getString(1));
            cursor.moveToNext();
        }

        cursor.close();
        ;

        return map;
    }

    @Override
    public void swapEntries(Long userId, long profile, long id, int position){

        swapEntries(getById(userId,profile, id), position);
    }

    void swapEntries(Entry entry, int pos){

        if (entry == null)
            return;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values1 = new ContentValues();
        values1.put("POSITION", String.valueOf(entry.getPosition()));
        db.update("ENTRIES", values1, "POSITION=?", new String[]{String.valueOf(pos)});

        ContentValues values0 = new ContentValues();
        values0.put("POSITION", String.valueOf(pos));
        db.update("ENTRIES", values0, "ID=?", new String[]{String.valueOf(entry.getId())});

        //clean up
        ;
    }

    @Override
    public void swapListEntries(Long userId, long profile, long id, int position){

        swapListEntries(getById(userId, profile, id), position);
    }

    void swapListEntries(Entry entry, int pos){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values1 = new ContentValues();
        values1.put("LIST_POSITION", String.valueOf(entry.getPosition()));
        db.update("ENTRIES", values1, "LIST=? AND LIST_POSITION=?", new String[]{entry.getList(), String.valueOf(pos)});

        ContentValues values0 = new ContentValues();
        values0.put("LIST_POSITION", String.valueOf(pos));
        db.update("ENTRIES", values0, "ID=?", new String[]{String.valueOf(entry.getId())});

        //clean up
        ;

    }

    /**
     * TODO DESRIBE
     * @param i
     * @return
     */
    public static boolean intToBool(int i){
        return i!=0;
    }

    /**
     * TODO DESCRIBE
     * @param b
     * @return
     */
    public static int boolToInt(boolean b){
        return b ? 1 : 0;
    }

    /**
     * TODO DESCRIBE
     * @param epoch
     * @return
     */
    public static Instant epochToDate(long epoch){
        return Instant.ofEpochSecond(epoch);
    }

    /**
     * TODO DESCRIBE
     * @param date
     * @return
     */
    public static long dateToEpoch(Instant date){
        return date.getEpochSecond();
    }

    /**
     * TODO DESCRIBE
     * @param arrayList
     */
    void saveRecurring(List<Long> arrayList){
        ByteBuffer buffer = ByteBuffer.allocate(arrayList.size()* Long.BYTES);
        for (long i: arrayList)
            buffer.putLong(i);

        try {
            Files.write(Paths.get(context.getFilesDir() + "/" + LocalDate.now()), buffer.array());
        } catch (IOException ignored) {}

    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Long> loadRecurring(){

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
                if (LocalDate.parse(fileNames[i]).isBefore(LocalDate.now())) {
                    assert files != null;
                    if (files[i] != null) files[i].delete();
                }

                // in old versions the file was stored under a different naming logic this block removes old files
            } catch (DateTimeParseException e){
                assert files != null;
                files[i].delete();
            }

        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(context.getFilesDir() + "/" + LocalDate.now()));
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            List<Long> list = new ArrayList<>();
            while(buffer.hasRemaining())
                list.add(buffer.getLong());
            return list;

        } catch(IOException ignored){
            return new ArrayList<>();
        }

    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Entry> getEntriesOrderedByDate() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES ORDER BY REMINDER_DATE", null);

        List<Entry> entries = getListOfEntries(cursor);
        cursor.close();
        ;

        return  entries;

    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Entry> getNoList() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE LIST IS NULL ORDER BY POSITION", null);

        List<Entry> entries = getListOfEntries(cursor);
        cursor.close();
        ;

        return  entries;
    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Entry> loadTasksToPick() {

        List<Entry> tasksToPick;

        long epoch = dateToEpoch(Instant.now());

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

        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE RECURRENCE IS NULL AND (FOCUS>0 OR " +
                "(REMINDER_DATE IS NULL AND (DROPPED>0 OR LIST=NULL)) OR " +
                "(REMINDER_DATE IS NOT NULL AND REMINDER_DATE<=" + epoch + ")) ORDER BY POSITION", null );


        tasksToPick = getListOfEntries(cursor);

        cursor.close();
        ;

        return  tasksToPick;
    }


    @Override
    public void updateId(Long userId, long profile, long entry, long id){

        //TODO implement
    }

    @Override
    public void setTimeDelay(long l) {

        //TODO implement
    }

    @Override
    public void addToQueue(User user, ZenServerMessage message) {

        Log.v("TEST", "INSERT INTO QUEUE");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        StringBuilder sb = new StringBuilder();

        sb.append("{");

        if (!message.arguments.isEmpty()) {
            sb.append(message.arguments.get(0).toString());
            message.arguments.stream().skip(1).forEach(o -> sb.append(",").append(o.toString()));
        }
        sb.append("}");
        String arguments = sb.toString();

        values.put("TYPE", String.valueOf(message.type.ordinal()));
        values.put("ARGUMENTS", arguments);
        values.put("TIMESTAMP", dateToEpoch(message.timeStamp));
        values.put("USER_ID", user.getId());
        values.put("CLOCK", message.clock.jsonify());

        db.insert("QUEUE", null, values);

    }

    @Override
    public List<ZenServerMessage> getQueued(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT TYPE, ARGUMENTS, CLOCK, TIMESTAMP FROM QUEUE WHERE USER_ID=?", new String[]{String.valueOf(userId)});

        cursor.moveToFirst();

        List<ZenServerMessage> result = new ArrayList<>();

        while (!cursor.isAfterLast()){
            OperationType type = OperationType.values()[cursor.getInt(0)];
            Log.v("GET QUEUE", String.valueOf(type));
            String argsString = cursor.getString(1);
            Log.v("GET QUEUE", String.valueOf(argsString));
            VectorClock clock = new VectorClock(cursor.getString(2));
            Log.v("GET QUEUE", String.valueOf(clock.jsonify()));
            Instant timeStamp = Instant.ofEpochSecond(cursor.getLong(3));
            Log.v("GET QUEUE", String.valueOf(timeStamp));

            String[] argsSplit = argsString.substring(1, argsString.length()-1).split(",");

            List<Object> args = new ArrayList<>(Arrays.asList(argsSplit));

            result.add(new ZenServerMessage(type, args, clock, timeStamp));
            cursor.moveToNext();
        }

        cursor.close();
        return result;
    }

    @Override
    public void clearQueue(long userId) {

        SQLiteDatabase db= this.getWritableDatabase();

        db.delete("QUEUE", "", null);
    }

    @Override
    public User addUser(long id, String email, String userName, long device) {

        SQLiteDatabase db = this.getWritableDatabase();


        Random random = new Random();
        long profileId = random.nextInt();

        while (true){
            Cursor cursorId = db.rawQuery("SELECT ID FROM PROFILES WHERE ID=" + id, null);

            cursorId.moveToFirst();

            if (!cursorId.isAfterLast()) {
                id = random.nextInt();
                cursorId.close();
            }
            else {
                cursorId.close();
                break;
            }
        }
        ContentValues profileValues = new ContentValues();
        profileValues.put("ID", profileId);
        profileValues.put("USER", id);
        db.insert("PROFILES", null, profileValues);

        ContentValues values = new ContentValues();
        values.put("ID", id);
        values.put("MAIL", email);
        values.put("NAME", userName);
        values.put("DEVICE", device);
        VectorClock clock = new VectorClock(device);
        values.put("CLOCK", clock.jsonify());
        values.put("PROFILE", profileId);

        db.insert("USERS", null, values);

        User user = new User(email, userName, device);
        user.setId(id);
        user.setClock(clock.jsonify());

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setId(profileId);
        user.getProfiles().add(profile);
        user.setProfile(profileId);

        System.out.println("NEW USER " + id + " WITH PROFILE " + user.getProfile());
        return user;

    }

    @Override
    public void removeUser(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("USERS", "ID=?", new String[]{String.valueOf(userId)});
    }

    @Override
    public void removeProfile(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete("PROFILES", "ID = ?", new String[]{String.valueOf(id)});
    }

    @Override
    public Optional<User> getUser(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE ID=?", new String[]{String.valueOf(id)});
        List<User> users = getListOfUsers(cursor);
        cursor.close();
        System.out.println("GETTING USER " + id);
        System.out.println(users.size());
        if(users.isEmpty())
            return Optional.empty();

        return Optional.of(users.get(0));
    }
    @Override
    public Optional<User> getUserByEmail(String email) {
        System.out.println("GET USER BY MAIL " + email);
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE MAIL=?", new String[]{email});

        List<User> users = getListOfUsers(cursor);

        cursor.close();

        if (users.size() > 1)
            throw new RuntimeException("Two users with same email exist");
        if (users.isEmpty())
            return Optional.empty();

        System.out.println("USER " + users.get(0).getId() + " WITH PROFILE " + users.get(0).getProfile());
        return Optional.of(users.get(0));

    }

    @Override
    public Optional<Profile> getProfile(Long userId, long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM PROFILES WHERE USER=? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(id)});

        System.out.println("GETTING PROFILE " + id + " FOR USER " + userId);
        List<Profile> profiles = getListOfProfiles(cursor);

        cursor.close();
        System.out.println("SIZE " + profiles.size());
        if (profiles.size() > 1)
            System.out.println("Profiles with identical id for user " + userId);
        if (profiles.isEmpty())
            return Optional.empty();

        Optional<User> user = getUser(userId);

        System.out.println("EMPTY USER " + user.isEmpty());
        if (user.isEmpty())
            return Optional.empty();

        System.out.println("HERE");
        Profile profile = profiles.get(0);
        profile.setUser(user.get());
        return Optional.of(profile);
    }

    @Override
    public boolean userExists(long userId) {
        return false;
    }

    @Override
    public boolean isEnabled(long userId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT ENABLED FROM USERS WHERE ID=?", new String[]{String.valueOf(userId)});

        cursor.moveToFirst();

        boolean enabled = false;

        if (!cursor.isAfterLast())
            enabled = intToBool(cursor.getInt(0));

        cursor.close();
        ;

        return enabled;
    }

    @Override
    public void enableUser(long userId) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ENABLED", 1);

        db.update("USERS", values, "ID=?", new String[]{String.valueOf(userId)});
        ;
    }

    @Override
    public void setDevice(long userId, long id) {

    }

    @Override
    public void setClock(long userId, VectorClock vectorClock) {


        try (SQLiteDatabase db = this.getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put("CLOCK", vectorClock.jsonify());
            db.update("USERS", values, "ID=?", new String[]{String.valueOf(userId)});
        }
    }

    @Override
    public String getToken(long user) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {

            try(Cursor cursor = db.rawQuery("SELECT TOKEN FROM TOKENS WHERE USER=?", new String[]{String.valueOf(user)})) {

                cursor.moveToFirst();
                if (cursor.isAfterLast())
                    return null;
                return cursor.getString(0);
            }
        }
    }

    @Override
    public void setToken(long user, String token) {

        try (SQLiteDatabase db = this.getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put("USER", user);
            values.put("TOKEN", token);
            db.insertWithOnConflict("TOKENS", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

}
