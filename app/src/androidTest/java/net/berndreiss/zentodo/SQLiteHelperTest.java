package net.berndreiss.zentodo;

import androidx.test.platform.app.InstrumentationRegistry;

import net.berndreiss.zentodo.data.Database;
import net.berndreiss.zentodo.data.SQLiteHelper;
import net.berndreiss.zentodo.tests.DatabaseTestSuite;

import org.junit.BeforeClass;
import org.junit.Test;

public class SQLiteHelperTest extends DatabaseTestSuite{

    private static final String DATABASE_NAME = "Data.db";

    @BeforeClass
    public static void run(){
        databaseSupplier = () -> {
            SharedData sharedData = new SharedData(InstrumentationRegistry.getInstrumentation().getTargetContext());
            sharedData.database = new SQLiteHelper(sharedData.context);
            return sharedData.database.getDatabase();
        };
    }
}
