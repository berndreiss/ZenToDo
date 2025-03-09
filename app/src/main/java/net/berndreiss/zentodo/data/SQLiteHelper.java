package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import net.berndreiss.zentodo.MainActivity;
import net.berndreiss.zentodo.OperationType;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenMessage;
import net.berndreiss.zentodo.util.ZenServerMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaBuilder;

/**
 * TODO DESCRIBE
 */
public class SQLiteHelper extends SQLiteOpenHelper implements ClientOperationHandler {

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
                "CLOCK TEXT NOT NULL" +
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

    private Entry getById(long id){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE ID=?", new String[]{String.valueOf(id)});

        List<Entry> entries = getList(cursor);

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
    Entry addEntry(String task){
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

        return addNewEntry(id, task, null, maxPosition + 1);
    }

    //adds new entry to TABLE_ENTRIES
    @Override
    public Entry addNewEntry(long id, String task, Long userId, int position) {

        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();


        values.put("ID",id);
        if (userId != null)
            values.put("USER", userId);
        values.put("TASK",task);
        values.put("POSITION", position);

        db.execSQL("UPDATE ENTRIES SET POSITION=POSITION+1 WHERE POSITION >=?", new String[]{String.valueOf(position)});
        db.insert("ENTRIES",null,values);

        return new Entry(id, task, null, position);
    }

    /**
     *
     * @param entry
     */
    void removeEntry(Entry entry){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ENTRIES", "ID=?", new String[]{String.valueOf(entry.getId())});
        db.execSQL("UPDATE ENTRIES SET POSITION=POSITION-1 WHERE POSITION >?", new String[]{String.valueOf(entry.getPosition())});
        if (entry.getList() != null)
            db.execSQL("UPDATE ENTRIES SET LIST_POSITION=LIST_POSITION-1 WHERE LIST=? AND LIST_POSITION>?", new String[]{entry.getList(), String.valueOf(entry.getListPosition())});
        ;
    }


    @Override
    public void delete(long id){

        Entry entry = getById(id);

        if (entry == null)
            return;

        removeEntry(entry);
    }

    public void updateReminderDate(Entry entry, Instant value){
        updateReminderDate(entry.getId(), value == null ? null : value.getEpochSecond());
    }

    @Override
    public void updateReminderDate(long id, Long value){
        ContentValues values = new ContentValues();
        values.put("REMINDER_DATE", value);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(id)});
        ;
    }

    void updateTask(Entry entry, String value){
        updateTask(entry.getId(), value);
    }

    @Override
    public void updateTask(long id, String value){
        ContentValues values = new ContentValues();
        values.put("TASK", value == null ? "" : value);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(id)});
        ;
    }

    void updateRecurrence(Entry entry, String value){
        updateRecurrence(entry.getId(), entry.getReminderDate() == null ? null : dateToEpoch(entry.getReminderDate()), value);
    }

    @Override
    public void updateRecurrence(long id, Long reminderDate, String value){
        ContentValues values = new ContentValues();
        values.put("RECURRENCE", value);
        if (reminderDate == null)
            values.put("REMINDER_DATE", dateToEpoch(Instant.now()));
        SQLiteDatabase db = this.getWritableDatabase();
        db.update("ENTRIES", values, "ID=?", new String[]{String.valueOf(id)});
        ;

    }

    @Override
    public void updateList(long id, String name, int position){
        updateList(getById(id), name, position);
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

    void updateFocus(Entry entry, boolean valueBool){

        updateFocus(entry.getId(), boolToInt(valueBool));
    }

    @Override
    public void updateFocus(long id, int value){

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ENTRIES SET FOCUS=? WHERE ID=?;", new String[]{String.valueOf(value), String.valueOf(id)});
        ;
    }

    void updateDropped(Entry entry, boolean valueBool){
        updateDropped(entry.getId(), boolToInt(valueBool));
    }

    @Override
    public void updateDropped(long id, int value){

        //convert bool to int
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ENTRIES SET DROPPED=? WHERE ID=?;",  new String[]{String.valueOf(value), String.valueOf(id)});
        ;
    }

    @Override
    public void updateListColor(String list, String color){

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
    public void updateUserName(long l, String s) {

    }

    @Override
    public boolean updateEmail(long l, String s) {
        return false;
    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Entry> loadEntries(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES ORDER BY POSITION", null);

        List<Entry> entries = getList(cursor);

        cursor.close();
        ;

        return entries;
    }

    /**
     * TODO DESCRIBE
     * @return
     */
    public List<Entry> loadFocus(){

        SQLiteDatabase db = this.getReadableDatabase();

        long epoch = dateToEpoch(Instant.now());

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE FOCUS>0 OR (RECURRENCE IS NOT NULL AND REMINDER_DATE <= " + epoch +  ")" +
                " ORDER BY POSITION", null);

        List<Entry> entries = getList(cursor);

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

        List<Entry> entries = getList(cursor);

        cursor.close();
        ;

        return entries;
    }

    /**
     * TODO DESCRIBE
     * @param name
     * @return
     */
    public List<Entry> loadList(String name){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM ENTRIES WHERE LIST=? ORDER BY LIST_POSITION", new  String[]{name});

        List<Entry> entries = getList(cursor);

        cursor.close();
        ;

        return entries;
    }

    //TODO COMMENT
    private List<Entry> getList(Cursor cursor){
        List<Entry> entries = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){

            long id = cursor.getInt(1);
            String task = cursor.getString(2);
            boolean focus = intToBool(cursor.getInt(3));
            boolean dropped = intToBool(cursor.getInt(4));
            String list = cursor.getString(5);
            Integer listPosition = cursor.getInt(6);
            long reminderDateEpoch = cursor.getLong(7);
            String recurrence = cursor.getString(8);
            int position = cursor.getInt(9);

            Entry entry = new Entry(id, task, null, position);
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
    public void swapEntries(long id, int position){

        swapEntries(getById(id), position);
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
    public void swapListEntries(long id, int position){

        swapListEntries(getById(id), position);
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
        ByteBuffer buffer = ByteBuffer.allocate(arrayList.size()* Integer.BYTES);
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

        List<Entry> entries = getList(cursor);
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

        List<Entry> entries = getList(cursor);
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


        tasksToPick = getList(cursor);

        cursor.close();
        ;

        return  tasksToPick;
    }


    @Override
    public void updateId(long entry, long id){

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
    public void clearQueue() {

        SQLiteDatabase db= this.getWritableDatabase();

        db.delete("QUEUE", "", null);
    }

    @Override
    public User addUser(long id, String email, String userName, long device) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ID", id);
        values.put("MAIL", email);
        values.put("NAME", userName);
        values.put("DEVICE", device);
        VectorClock clock = new VectorClock(device);
        values.put("CLOCK", clock.jsonify());

        db.insert("USERS", null, values);

        User user = new User(email, userName, device);
        user.setId(id);
        return user;

    }

    @Override
    public void removeUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("USERS", "MAIL=?", new String[]{email});

    }

    @Override
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE MAIL=?", new String[]{email});

        User user = null;

        cursor.moveToFirst();

        if (!cursor.isAfterLast()){

            long id = cursor.getLong(0);
            String mail = cursor.getString(1);
            String name = cursor.getString(2);
            boolean enabled = intToBool(cursor.getInt(3));
            long device = cursor.getLong(4);
            String clock = cursor.getString(5);

            user = new User(mail, name, device);
            user.setId(id);
            user.setClock(clock);
            user.setEnabled(enabled);
        }

        cursor.close();

        return user;
    }

    @Override
    public boolean userExists(String email) {
        return false;
    }

    @Override
    public boolean isEnabled(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT ENABLED FROM USERS WHERE MAIL=?", new String[]{email});

        cursor.moveToFirst();

        boolean enabled = false;

        if (!cursor.isAfterLast())
            enabled = intToBool(cursor.getInt(0));

        cursor.close();
        ;

        return enabled;
    }

    @Override
    public void enableUser(String email) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ENABLED", 1);

        db.update("USERS", values, "MAIL=?", new String[]{email});
        ;
    }

    @Override
    public void setDevice(String s, long l) {

    }

    @Override
    public void setClock(String email, VectorClock vectorClock) {


        try (SQLiteDatabase db = this.getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put("CLOCK", vectorClock.jsonify());
            db.update("USERS", values, "MAIL=?", new String[]{email});
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
