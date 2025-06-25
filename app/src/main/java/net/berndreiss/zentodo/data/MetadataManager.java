package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Implementation of the DatabaseOpsI interface using SQLite and the ZenSQLiteHelper.
 */
public class MetadataManager implements MetadataManagerI{

    private final ZenSQLiteHelper zenSqLiteHelper;

    /**
     * Crate new instance of DatabaseOps.
     * @param zenSqLiteHelper the helper for interacting with the database
     */
    public MetadataManager(ZenSQLiteHelper zenSqLiteHelper){
        this.zenSqLiteHelper = zenSqLiteHelper;
    }

    @Override
    public synchronized void setTimeDelay(long delay) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TIME_DELAY", delay);
        db.update("METADATA",values, "ID=0", null);
    }

    @Override
    public long getTimeDelay() {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT TIME_DELAY FROM METADATA WHERE ID=0", null);
        long lastUser = cursor.getLong(0);
        cursor.close();
        return lastUser;
    }

    @Override
    public void setLastUser(long user) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("LAST_USER", user);
        db.update("METADATA",values, "ID=0", null);
    }

    @Override
    public long getLastUser() {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT LAST_USER FROM METADATA WHERE ID=0", null);
        long lastUser = cursor.getLong(0);
        cursor.close();
        return lastUser;
    }

    @Override
    public void setVersion(String version) {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("VERSION", version);
        db.update("METADATA",values, "ID=0", null);
    }

    @Override
    public String getVersion() {
        SQLiteDatabase db = zenSqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT VERSION FROM METADATA WHERE ID=0", null);
        String version = cursor.getString(0);
        cursor.close();
        return version;
    }

}
