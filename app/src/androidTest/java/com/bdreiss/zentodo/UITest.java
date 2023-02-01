package com.bdreiss.zentodo;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.view.View;
import android.widget.DatePicker;

import com.bdreiss.zentodo.adapters.DropTaskListAdapter;
import com.bdreiss.zentodo.dataManipulation.Data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.MonthDay;
import java.time.Year;

@RunWith(AndroidJUnit4.class)
public class UITest {
    private static Context appContext;

    private static String DATABASE_NAME;

    private static String TEST_MODE_FILE;

    //This class only serves the purpose of creating a file telling the program that it is in test mode
    //The reason for putting this code in it's own class is strictly so that it can be accessed by
    //creating an instance BEFORE any constructor of the actual test class is run.
    //Otherwise the mode will be written to the file AFTER the program has started and will run the
    //tests on the Database with real data, not the test database. This can be changed, if in the
    //future user accounts are introduced
    private static class SaveMode{

        SaveMode() {
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
    }

    SaveMode sm = new SaveMode();



    @Before
    public void setup(){


    }



    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);


    @Test
    public void testDrop(){

        String[] tests = {"","0","1","Test","'","'Test","T'e'st","Test'"};

        String[][] results = {{},{"0"},{"0","1"},{"0","1","Test"},{"0","1","Test","'"},
                                {"0","1","Test","'","'Test"},
                                {"0","1","Test","'","'Test","T'e'st"},
                                {"0","1","Test","'","'Test","T'e'st","Test'"}};

        for (int i = 0; i < tests.length; i++) {
            onView(withId(R.id.edit_text_drop)).perform(typeText(tests[i]), closeSoftKeyboard());
            onView(withId(R.id.button_drop)).perform(click());

            DropTaskListAdapter adapter = new DropTaskListAdapter(appContext, new Data(appContext, DATABASE_NAME));

            for (int j = 0; j < results[i].length; j++)
                assert(adapter.entries.get(j).getTask().equals(results[i][j]));

        }
    }

    @Test
    public void testCalendarDrop(){

        int year = Year.now().getValue();
        int month = MonthDay.now().getMonthValue();
        int day = MonthDay.now().getDayOfMonth();

        String[] strings = {"Test"};

        int[][] tests = {{year,month,day+1}};



        for (int i = 0; i < strings.length; i++) {
            drop(strings[i]);

            new RecyclerClickAction(R.id.list_view_drop, R.id.button_menu, 0);
            new RecyclerClickAction(R.id.list_view_drop, R.id.button_calendar, 0);


            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(android.R.id.button1)).perform(click());

            Data data = new Data(appContext, DATABASE_NAME);

            assert (data.getEntries().get(i).getReminderDate() == tests[i][0] * 10000 + tests[i][1] * 100 + tests[i][2]);
            assert (data.getDropped().isEmpty());
        }
    }

    private static void drop(String text){
        onView(withId(R.id.edit_text_drop)).perform(typeText(text), closeSoftKeyboard());
        onView(withId(R.id.button_drop)).perform(click());

    }

    private static class RecyclerClickAction {

        RecyclerClickAction(final int idView, final int id, final int position){
            onView(withId(idView)).perform(RecyclerViewActions.actionOnItemAtPosition(position,clickChildViewWithId(id)));

        }


        private static ViewAction clickChildViewWithId(final int id) {


            return new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "Click on a child view with specified id.";
                }

                @Override
                public void perform(UiController uiController, View view) {
                    View v = view.findViewById(id);
                    v.performClick();
                }
            };
        }

    }

    @After
    public void cleanup(){
        appContext.deleteDatabase(DATABASE_NAME);
        try {
            Writer w = new FileWriter(appContext.getFilesDir() + "/" + TEST_MODE_FILE);
            w.write("0");
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
