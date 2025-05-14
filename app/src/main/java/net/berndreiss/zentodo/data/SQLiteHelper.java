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
public class SQLiteHelper extends SQLiteOpenHelper {

    private final Context context;

    private final Database database;

    public SQLiteHelper(Context context, String databaseName){
        super(context, databaseName == null ? MainActivity.DATABASE_NAME : databaseName,null, MainActivity.DATABASE_VERSION);
        this.context = context;
        this.database = new Database(new EntryManager(this), new UserManager(this), new DatabaseOps(this));
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

    public Database getDatabase(){
        return database;
    }

    public Context getContext() {
        return  context;
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

}
