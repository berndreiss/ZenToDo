package com.bdreiss.zentodo;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.Buffer;

@RunWith(AndroidJUnit4.class)
public class UITest {
    private static Context appContext;

    private static String DATABASE_NAME;

    private static String TEST_MODE_FILE;

    @Before
    public void setup(){
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        TEST_MODE_FILE = appContext.getResources().getString(R.string.mode_file);

        DATABASE_NAME = appContext.getResources().getString(R.string.db_test);

        try {
            Writer w = new FileWriter(appContext.getFilesDir() + "/" + TEST_MODE_FILE);
            w.write("1");
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testDrop(){

        String text = "";

        try {

            BufferedReader br = new BufferedReader(new FileReader(appContext.getFilesDir() + "/" + TEST_MODE_FILE));

            text = br.readLine();

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.edit_text_drop)).perform(typeText(text), closeSoftKeyboard());
        onView(withId(R.id.button_drop)).perform(click());
    }

    @After
    public void cleanup(){
        //appContext.deleteDatabase(DATABASE_NAME);
        try {
            Writer w = new FileWriter(appContext.getFilesDir() + "/" + TEST_MODE_FILE);
            w.write("0");
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
