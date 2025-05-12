package net.berndreiss.zentodo;

import androidx.test.platform.app.InstrumentationRegistry;

import net.berndreiss.zentodo.data.Database;
import net.berndreiss.zentodo.data.Entry;
import net.berndreiss.zentodo.data.SQLiteHelper;
import net.berndreiss.zentodo.util.DatabaseTest;

import org.junit.Assert;
import org.junit.Test;

public class SQLiteHelperTest extends DatabaseTest {

    private static final String DATABASE_NAME = "Data.db";

    public Database createDatabase(){
        SharedData sharedData = new SharedData(InstrumentationRegistry.getInstrumentation().getTargetContext());
        sharedData.database = new SQLiteHelper(sharedData.context);
        return sharedData.database;
    }
    @Test
    public void dummyTest(){

    }
}
