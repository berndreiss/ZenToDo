package net.berndreiss.zentodo.data;

public class DatabaseOps implements DatabaseOpsI{

    private SQLiteHelper sqLiteHelper;

    public DatabaseOps(SQLiteHelper sqLiteHelper){
        this.sqLiteHelper = sqLiteHelper;
    }
    @Override
    public void setTimeDelay(long l) {

        //TODO implement
    }
}
