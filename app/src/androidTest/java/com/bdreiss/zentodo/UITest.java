package com.bdreiss.zentodo;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
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

/*
    //Tests if dropping tasks functions properly
    @Test
    public void testDrop(){

        //test data
        String[] tests = {"","0","1","Test","'","'Test","T'e'st","Test'"};

        //expected results in data.entries
        String[][] results = {{},{"0"},{"0","1"},{"0","1","Test"},{"0","1","Test","'"},
                                {"0","1","Test","'","'Test"},
                                {"0","1","Test","'","'Test","T'e'st"},
                                {"0","1","Test","'","'Test","T'e'st","Test'"}};

        for (int i = 0; i < tests.length; i++) {
            drop(tests[i]);

            DropTaskListAdapter adapter = new DropTaskListAdapter(appContext, new Data(appContext, DATABASE_NAME));

            for (int j = 0; j < results[i].length; j++)
                assert(adapter.entries.get(j).getTask().equals(results[i][j]));

        }
    }

    //tests the functionality of the calendar in DROP: when a date is set, tasks should disappear
    @Test
    public void testCalendarDrop(){

        int year = Year.now().getValue();
        int month = MonthDay.now().getMonthValue();
        int day = MonthDay.now().getDayOfMonth();

        //dummy test Strings for adding tasks to perform tests on
        String[] strings = {"Test", "Test1"};

        //test data
        int[][] tests = {{year,month,day+1},{0,0,0}};

        //expected sizes of data.entries
        int[] results = {0,1};

        //button1 == OK, button2 == No Date
        int[] buttons = {android.R.id.button1, android.R.id.button2};

        for (int i = 0; i < strings.length; i++) {
            drop(strings[i]);

            //open calendar and set test date
            new RecyclerAction(R.id.list_view_drop, R.id.button_menu, 0);
            new RecyclerAction(R.id.list_view_drop, R.id.button_calendar, 0);
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());

            //assert results
            Data data = new Data(appContext, DATABASE_NAME);

            assert (data.getEntries().get(i).getReminderDate() == tests[i][0] * 10000 + tests[i][1] * 100 + tests[i][2]);
            assert (data.getDropped().size() == results[i]);
        }
    }

    //test the functionality of the calendar in FOCUS: when a date is set, tasks should disappear
    @Test
    public void testCalendarFocus(){

        int year = Year.now().getValue();
        int month = MonthDay.now().getMonthValue();
        int day = MonthDay.now().getDayOfMonth();

        //dummy test Strings for adding tasks
        String[] strings = {"Test", "Test1"};

        //test data
        int[][] tests = {{year,month,day+1},{0,0,0}};

        //expected sizes of data.getFocus()
        int[] results = {0,1};

        //button1 == OK, button2 == No Date
        int[] buttons = {android.R.id.button1, android.R.id.button2};

        for (int i = 0; i < strings.length; i++) {

            //switch to DROP and drop tasks
            onView(withId(R.id.toolbar_drop)).perform(click());
            drop(strings[i]);

            //switch to No List in LISTS -> since we want to test tasks, that already have a date,
            //we need to set that in lists, so tasks don't disappear from the list and
            //we can still set focused = true
            onView(withId(R.id.toolbar_lists)).perform(click());
            onData(hasToString("No list")).inAdapterView(withId(R.id.list_view_lists)).atPosition(0).perform(click());

            //set date and set focused = true
            new RecyclerAction(R.id.recycle_view_lists, R.id.button_menu, i);
            new RecyclerAction(R.id.recycle_view_lists, R.id.button_calendar, i);
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(1988, 11, 3));
            onView(withId(buttons[i])).perform(click());

            new RecyclerAction(R.id.recycle_view_lists, R.id.button_menu, i);
            new RecyclerAction(R.id.recycle_view_lists, R.id.button_focus, i);

            //switch to FOCUS and start calendar tests
            onView(withId(R.id.toolbar_focus)).perform(click());

            new RecyclerAction(R.id.list_view_focus, R.id.button_menu, 0);
            new RecyclerAction(R.id.list_view_focus, R.id.button_calendar, 0);

            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());

            //assert results
            Data data = new Data(appContext, DATABASE_NAME);

            assert (data.getEntries().get(i).getReminderDate() == tests[i][0] * 10000 + tests[i][1] * 100 + tests[i][2]);
            assert (data.getFocus().size() == results[i]);
        }
    }

    //test calendar function for items in no list in LIST
    @Test
    public void testCalendarListNoList(){

        //get current dates
        int year = Year.now().getValue();
        int month = MonthDay.now().getMonthValue();
        int day = MonthDay.now().getDayOfMonth();

        //dummy task names for test data
        String[] strings = {"Test", "Test1"};

        //test data
        int[][] tests = {{year,month,day+1},{0,0,0}};

        //expected number of tasks in data.getNoList() and data.getDropped()
        int[][] results = {{1,0},{2,1}};

        //button1 == OK, button2 == No Date
        int[] buttons = {android.R.id.button1, android.R.id.button2};

        for (int i = 0; i < strings.length; i++) {

            //switch to DROP and drop test data
            onView(withId(R.id.toolbar_drop)).perform(click());
            drop(strings[i]);

            //switch to LISTS and select no list
            onView(withId(R.id.toolbar_lists)).perform(click());
            onData(hasToString(appContext.getString(R.string.noList))).inAdapterView(withId(R.id.list_view_lists)).atPosition(0).perform(click());

            //set date
            new RecyclerAction(R.id.recycle_view_lists, R.id.button_menu, i);
            new RecyclerAction(R.id.recycle_view_lists, R.id.button_calendar, i);
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());

            //assert results
            Data data = new Data(appContext, DATABASE_NAME);

            assert (data.getEntries().get(i).getReminderDate() == tests[i][0] * 10000 + tests[i][1] * 100 + tests[i][2]);
            assert(data.getNoList().size()== results[i][0]);
            assert (data.getDropped().size() == results[i][1]);
        }
    }

    //tests the calendar function in a list in LISTS
    @Test
    public void testCalendarList(){

        //get current dates
        int year = Year.now().getValue();
        int month = MonthDay.now().getMonthValue();
        int day = MonthDay.now().getDayOfMonth();

        //dummy task name for test data
        String[] strings = {"Test"};

        //dummy list name for test data
        String[] lists = {"Test"};

        //test dates
        int[][] tests = {{year,month,day+1}};

        //expected sizes of data.getLists() and data.getDropped()
        int[][] results = {{3,0}};

        //button1 == OK, button2 == No Date
        int[] buttons = {android.R.id.button1, android.R.id.button2};

        for (int i = 0; i < strings.length; i++) {

            //switch to DROP and drop tasks
            onView(withId(R.id.toolbar_drop)).perform(click());
            drop(strings[i]);

            //assign lists
            new RecyclerAction(R.id.list_view_drop, R.id.button_menu, 0);
            new RecyclerAction(R.id.list_view_drop,R.id.button_list,0);
            new RecyclerAction(R.id.list_view_drop,0,lists[i]);
            new RecyclerAction(R.id.list_view_drop,R.id.button_back_list,0);

            //switch to LISTS
            onView(withId(R.id.toolbar_lists)).perform(click());

            //choose first item in adapter since there is only one list
            onData(allOf(instanceOf(String.class))).atPosition(0).perform(click());

            //set dates
            new RecyclerAction(R.id.recycle_view_lists, R.id.button_menu, i);
            new RecyclerAction(R.id.recycle_view_lists, R.id.button_calendar, i);

            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());

            //assert results
            Data data = new Data(appContext, DATABASE_NAME);

            assert (data.getEntries().get(i).getReminderDate() == tests[i][0] * 10000 + tests[i][1] * 100 + tests[i][2]);
            assert(data.getLists().size() == results[i][0]);
            assert (data.getDropped().size() == results[i][1]);
        }
    }
*/

    /*
     *   the expected results for the calendar function in PICK are more complex than in the other modes
     *   the behaviour in the different adapters is as follows:
     *
     *   (1) tasksToPick:   noDate  -> task stays in adapter
     *                      setDate -> move task to doLater
     *
     *   (2) doNow:         noDate  -> task stays in adapter
     *                      setDate -> move task to doLater
     *
     *   (3) doLater:       noDate  -> if no list is assigned -> move to tasksToPick
     *                                 if list is assigned    -> move to moveToLists
     *
     *                      setDate -> task stays in adapter
     *
     *    (4) moveToLists:  noDate -> tasks stays in adapter
     *                      setDate-> move task to doLater
     *
    */
    @Test
    public void testCalendarPick(){

        //Get current date
        int year = Year.now().getValue();
        int month = MonthDay.now().getMonthValue();
        int day = MonthDay.now().getDayOfMonth();

        //dummy task
        String string = "Test";

        //test data for (1), (2) and (4)
        int[][] tests = {{0,0,0},{year,month,day+1}};

        //expected size of adapter for (1), (2) and (4)
        int[] results = {1,0};

        //button1 == OK, button2 == No Date
        int[] buttons = {android.R.id.button2, android.R.id.button1};

        //test data for (3)
        int[][] testsDoLater = {{year,month,day+1},{0,0,0}};

        //expected results for (3)
        int[] resultsDoLater = {1,0};

        //button1 == OK, button2 == No Date
        int[] buttonsDoLater = {android.R.id.button1,android.R.id.button2};

        drop(string);

        //switch to PICK
        onView(withId(R.id.toolbar_pick)).perform(click());

        //assert that dropped task is present
        onView(withId(R.id.list_view_pick)).check(new RecyclerViewCountAssertion(1));

        //Test (1): tasksToPick
        for (int i = 0; i < tests.length; i++){
            new RecyclerAction(R.id.list_view_pick,R.id.button_menu,0);
            new RecyclerAction(R.id.list_view_pick,R.id.button_calendar,0);

            //assert results
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());
            onView(withId(R.id.list_view_pick)).check(new RecyclerViewCountAssertion(results[i]));
        }

        //assert task was moved to doLater
        onView(withId(R.id.list_view_pick_doLater)).check(new RecyclerViewCountAssertion(1));

        //Test (3): doLater
        for (int i = 0; i < tests.length; i++){
            new RecyclerAction(R.id.list_view_pick_doLater,R.id.button_menu,0);
            new RecyclerAction(R.id.list_view_pick_doLater,R.id.button_calendar,0);

            //assert results
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(testsDoLater[i][0], testsDoLater[i][1], testsDoLater[i][2]));
            onView(withId(buttonsDoLater[i])).perform(click());
            onView(withId(R.id.list_view_pick_doLater)).check(new RecyclerViewCountAssertion(resultsDoLater[i]));
        }

        //assert task was moved back to tasksToPick
        onView(withId(R.id.list_view_pick)).check(new RecyclerViewCountAssertion(1));

        //assign list to task
        new RecyclerAction(R.id.list_view_pick,R.id.button_menu,0);
        new RecyclerAction(R.id.list_view_pick,R.id.button_list,0);

        new RecyclerAction(R.id.list_view_pick,0,"Test");
        new RecyclerAction(R.id.list_view_pick,R.id.button_back_list,0);

        //assert task was moved to list
        onView(withId(R.id.list_view_pick_list)).check(new RecyclerViewCountAssertion(1));

        //Test (4): moveToList
        for (int i = 0; i < tests.length; i++){
            new RecyclerAction(R.id.list_view_pick_list,R.id.button_menu,0);
            new RecyclerAction(R.id.list_view_pick_list,R.id.button_calendar,0);

            //assert results
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());
            onView(withId(R.id.list_view_pick_list)).check(new RecyclerViewCountAssertion(results[i]));
        }

        //assert task was moved to do later
        onView(withId(R.id.list_view_pick_doLater)).check(new RecyclerViewCountAssertion(1));

        //press checkbox
        new RecyclerAction(R.id.list_view_pick_doLater,R.id.checkbox,0);

        //assert task was moved to doNow
        onView(withId(R.id.list_view_pick_doNow)).check(new RecyclerViewCountAssertion(1));

        //Test (2): doNow
        for (int i = 0; i < tests.length; i++){
            new RecyclerAction(R.id.list_view_pick_doNow,R.id.button_menu,0);
            new RecyclerAction(R.id.list_view_pick_doNow,R.id.button_calendar,0);

            //assert results
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());
            onView(withId(R.id.list_view_pick_doNow)).check(new RecyclerViewCountAssertion(results[i]));
        }
    }

    //assert RecyclerViewAdapter has number of items
    public static class RecyclerViewCountAssertion implements ViewAssertion{

        private final int count;

        public RecyclerViewCountAssertion(int count){
            this.count = count;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assert adapter != null;
            assertThat(adapter.getItemCount(), is(count));
        }

    }

    //drop new task
    private static void drop(String text){
        onView(withId(R.id.edit_text_drop)).perform(typeText(text), closeSoftKeyboard());
        onView(withId(R.id.button_drop)).perform(click());

    }

    //perform action on element within item in RecyclerView
    private static class RecyclerAction {

        RecyclerAction(final int idView, final int id, final int position){
            onView(withId(idView)).perform(RecyclerViewActions.actionOnItemAtPosition(position,RecyclerAction.clickChildViewWithId(id)));

        }
        RecyclerAction(final int idView, final int position,String text){
            onView(withId(idView)).perform(RecyclerViewActions.actionOnItemAtPosition(position,typeText(text)));

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
