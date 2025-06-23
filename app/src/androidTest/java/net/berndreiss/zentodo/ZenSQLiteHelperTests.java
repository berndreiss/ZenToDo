package net.berndreiss.zentodo;

import androidx.test.platform.app.InstrumentationRegistry;

import net.berndreiss.zentodo.data.ZenSQLiteHelper;
import net.berndreiss.zentodo.tests.DatabaseTestSuite;

import org.junit.BeforeClass;

public class ZenSQLiteHelperTests extends DatabaseTestSuite{

    private static final String DATABASE_NAME = "Data.db";

    @BeforeClass
    public static void run(){
        databaseSupplier = () -> {
            SharedData sharedData = new SharedData(InstrumentationRegistry.getInstrumentation().getTargetContext());
            sharedData.database = new ZenSQLiteHelper(sharedData.context);
            return sharedData.database.getDatabase();
        };
    }
}
