package net.berndreiss.zentodo.data;

public class DatabaseOps implements DatabaseOpsI{

    private SQLiteHelper sqLiteHelper;

    public DatabaseOps(SQLiteHelper sqLiteHelper){
        this.sqLiteHelper = sqLiteHelper;
    }
    @Override
    public synchronized void setTimeDelay(long l) {

        //TODO implement
    }
}
