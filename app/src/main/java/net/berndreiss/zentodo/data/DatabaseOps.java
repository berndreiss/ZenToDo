package net.berndreiss.zentodo.data;

/**
 * Implementation of the DatabaseOpsI interface using SQLite and the ZenSQLiteHelper.
 */
public class DatabaseOps implements DatabaseOpsI{

    private final ZenSQLiteHelper zenSqLiteHelper;

    /**
     * Crate new instance of DatabaseOps.
     * @param zenSqLiteHelper the helper for interacting with the database
     */
    public DatabaseOps(ZenSQLiteHelper zenSqLiteHelper){
        this.zenSqLiteHelper = zenSqLiteHelper;
    }

    @Override
    public synchronized void setTimeDelay(long l) {
        //TODO implement
    }
}
