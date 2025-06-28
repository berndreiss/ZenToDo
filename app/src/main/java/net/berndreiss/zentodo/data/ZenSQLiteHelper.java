package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.berndreiss.zentodo.MainActivity;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
import net.berndreiss.zentodo.exceptions.DuplicateIdException;
import net.berndreiss.zentodo.exceptions.InvalidActionException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A helper for interaction with the SQLite database.
 * Defines the database schema and database updates.
 * The actual implementation of the Database interface is stored in the field "database".
 */
public class ZenSQLiteHelper extends SQLiteOpenHelper {

    /** The context of the application */
    private final Context context;

    /** The database  */
    private final Database database;

    /**
     * Get a new SQLite helper
     * @param context the application context
     * @param databaseName the file name for the database
     */
    public ZenSQLiteHelper(Context context, String databaseName){
        super(context, databaseName == null ? MainActivity.DATABASE_NAME : databaseName,null, MainActivity.DATABASE_VERSION);
        this.context = context;
        this.database = new Database(new TaskManager(this), new UserManager(this), new ListManager(this), new MetadataManager(this));
        Optional<User> user = database.getUserManager().getUser(0L);
        if (user.isPresent()) {
            List<Profile> profiles = getUserManager().getProfiles(0);
            if (profiles.isEmpty()) {
                try {
                    getUserManager().addProfile(0);
                } catch (InvalidActionException _) {
                    //TODO add logging
                }
            }//this should never happen
            return;
        }
        try {
            database.getUserManager().addUser(0, "test@adf03ruasflkjeoijdfmnasdfdkljshfsl.net", "Default user", 0);
        } catch (DuplicateIdException | InvalidActionException _) {}
    }

    /**
     * Get a new SQLite helper with the default database file name defined in MainActivity.java.
     * @param context the application context
     */
    public ZenSQLiteHelper(Context context){
        this(context, null);
    }


    /**
     * Create the relevant tables for the database.
     * @param db the database
     */
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE TASKS ("
                + "USER INTEGER NOT NULL,"
                + "PROFILE INTEGER NOT NULL,"
                + "ID INTEGER NOT NULL, "
                + "TASK TEXT NOT NULL, "
                + "FOCUS INTEGER DEFAULT 0, "
                + "DROPPED INTEGER DEFAULT 1, "
                + "LIST INTEGER DEFAULT NULL, "
                + "LIST_POSITION INTEGER DEFAULT NULL, "
                + "REMINDER_DATE INTEGER DEFAULT NULL,"
                + "RECURRENCE TEXT DEFAULT NULL,"
                + "POSITION INTEGER NOT NULL,"
                + "PRIMARY KEY (ID, USER),"
                + "FOREIGN KEY (LIST) REFERENCES LISTS(ID),"
                + "FOREIGN KEY (USER) REFERENCES USERS(MAIL)"
                + ")");

        String query = "CREATE TABLE LISTS (" +
                "ID INTEGER PRIMARY KEY," +
                "NAME TEXT NOT NULL, " +
                "COLOR TEXT DEFAULT '" + ListTaskListAdapter.DEFAULT_COLOR + "'" +
                ")";
        db.execSQL(query);

        query = "CREATE TABLE USERS (" +
                "ID INTEGER PRIMARY KEY, " +
                "MAIL TEXT NOT NULL, " +
                "NAME TEXT DEFAULT NULL, " +
                "ENABLED INTEGER DEFAULT 0, " +
                "DEVICE INTEGER, " +
                "PROFILE INTEGER NOT NULL, " +
                "CLOCK TEXT NOT NULL" +
                ")";
        db.execSQL(query);


        query = "CREATE TABLE PROFILES (" +
                "ID INTEGER NOT NULL, " +
                "NAME TEXT DEFAULT NULL, " +
                "USER INTEGER NOT NULL," +
                "PRIMARY KEY (ID, USER)," +
                "FOREIGN KEY (USER) REFERENCES USERS(ID)" +
                ")";
        db.execSQL(query);

        query = "CREATE TABLE PROFILE_LIST (" +
                "USER INTEGER NOT NULL," +
                "PROFILE INTEGER NOT NULL, " +
                "LIST INTEGER NOT NULL, " +
                "PRIMARY KEY (USER, PROFILE, LIST)," +
                "FOREIGN KEY (USER) REFERENCES USERS(ID)," +
                "FOREIGN KEY (USER, PROFILE) REFERENCES PROFILES(USER, ID)," +
                "FOREIGN KEY (LIST) REFERENCES LISTS(ID)" +
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

        query = "CREATE TABLE METADATA (" +
                "ID INTEGER PRIMARY KEY, " +
                "LAST_USER INTEGER," +
                "TIME_DELAY INTEGER," +
                "VERSION TEXT" +
                ")";
        db.execSQL(query);

        ContentValues values = new ContentValues();
        values.put("ID", 0);
        values.put("LAST_USER", 0);
        values.put("TIME_DELAY", 0);
        values.put("VERSION", "1.0");
        db.insert("METADATA", null, values);

        query = "CREATE INDEX IDX_FOCUS ON TASKS(FOCUS)";
        db.execSQL(query);
        query = "CREATE INDEX IDX_DROPPED ON TASKS(DROPPED)";
        db.execSQL(query);
        query = "CREATE INDEX IDX_LIST ON TASKS(LIST, LIST_POSITION)";
        db.execSQL(query);
        query = "CREATE INDEX IDX_REMINDER_DATE ON TASKS(REMINDER_DATE)";
        db.execSQL(query);
        query = "CREATE INDEX IDX_POSITION ON TASKS(POSITION)";
        db.execSQL(query);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //TODO implement version control
    }

    public Database getDatabase(){return database;}

    public Context getContext() {return  context;}

    /**
     * Convert an epoch to an Instant.
     * @param epoch the epoch to convert
     * @return the instant
     */
    public static Instant epochToDate(long epoch){
        return Instant.ofEpochMilli(epoch);
    }

    /**
     * Convert a date (Instant) to an epoch.
     * @param date the date to convert
     * @return the epoch
     */
    public static long dateToEpoch(Instant date){
        return date.toEpochMilli();
    }

    /**
     * Convert an integer to a boolean.
     * @param i the integer to convert
     * @return the boolean
     */
    public static boolean intToBool(int i){
        return i!=0;
    }

    /**
     * Convert a boolean to an integer.
     * @param b the boolean to convert
     * @return the integer
     */
    public static int boolToInt(boolean b){
        return b ? 1 : 0;
    }

    /**
     * Get the TaskManagerI casted as TaskManager.
     * @return the SQLite task manager
     */
    public TaskManager getTaskManager(){
        return (TaskManager) database.getTaskManager();
    }

    /**
     * Get the UserManagerI casted as UserManager.
     * @return the SQLite user manager
     */
    public UserManager getUserManager(){
        return (UserManager) database.getUserManager();
    }

    /**
     * Get the ListManagerI casted as ListManager.
     * @return the SQLite list manager
     */
    public ListManager getListManager(){
        return (ListManager) database.getListManager();
    }

    /**
     * Get the MetadataManagerI casted as MetadataManager.
     * @return the SQLite database operations class instance
     */
    public MetadataManager getDatabaseOps(){
        return (MetadataManager) database.getMetadataManager();
    }
}
