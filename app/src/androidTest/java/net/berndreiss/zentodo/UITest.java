package net.berndreiss.zentodo;

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

import android.view.View;
import android.widget.DatePicker;

import net.berndreiss.zentodo.adapters.DropTaskListAdapter;
import net.berndreiss.zentodo.data.Entry;
import net.berndreiss.zentodo.data.SQLiteHelper;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class UITest {
    private static SharedData sharedData;



    @Before
    public void setup(){

        sharedData = new SharedData(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }



    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);


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

            DropTaskListAdapter adapter = new DropTaskListAdapter(sharedData);

            for (int j = 0; j < results[i].length; j++)
                assert(adapter.entries.get(j).getTask().equals(results[i][j]));

        }
        sharedData.context.deleteDatabase(MainActivity.DATABASE_NAME);
    }

    //tests the functionality of the calendar in DROP: when a date is set, tasks should disappear
    @Test
    public void testCalendarDrop(){

        //dummy test Strings for adding tasks to perform tests on
        String[] strings = {"Test", "Test1"};

        //test data
        LocalDate[] tests = {LocalDate.now(),null};

        //expected sizes of data.entries
        int[] results = {0,1};

        //button1 == OK, button2 == No Date
        int[] buttons = {android.R.id.button1, android.R.id.button2};

        for (int i = 0; i < strings.length; i++) {
            drop(strings[i]);

            //open calendar and set test date
            new RecyclerActionTest(R.id.list_view_drop, R.id.button_menu, 0);
            new RecyclerActionTest(R.id.list_view_drop, R.id.button_calendar, 0);
            if (tests[i]!=null)
                onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i].getYear(),tests[i].getMonthValue(),tests[i].getDayOfMonth()));
            onView(withId(buttons[i])).perform(click());

            //assert results
            try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
                List<Entry> entries = db.loadEntries(null, 0);

                if (tests[i] == null)
                    assert (entries.get(i).getReminderDate() == null);
                else
                    assert (entries.get(i).getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(tests[i]));
                assert (db.loadDropped().size() == results[i]);
            }
        }
        sharedData.context.deleteDatabase(MainActivity.DATABASE_NAME);
    }

    //test the functionality of the calendar in FOCUS: when a date is set, tasks should disappear
    @Test
    public void testCalendarFocus(){

        //dummy test Strings for adding tasks
        String[] strings = {"Test", "Test1"};

        //test data
        LocalDate[] tests = {LocalDate.now().plusDays(1),LocalDate.now().minusDays(1)};

        //expected sizes of data.getFocus()
        int[] results = {0,1};

        //button1 == OK, button2 == No Date
        int[] buttons = {android.R.id.button1, android.R.id.button1};

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
            new RecyclerActionTest(R.id.recycle_view_lists, R.id.button_menu, i);
            new RecyclerActionTest(R.id.recycle_view_lists, R.id.button_calendar, i);
            if (tests[i]!=null)
                onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i].getYear(),tests[i].getMonthValue(),tests[i].getDayOfMonth()));
            onView(withId(buttons[i])).perform(click());

            new RecyclerActionTest(R.id.recycle_view_lists, R.id.button_menu, i);
            new RecyclerActionTest(R.id.recycle_view_lists, R.id.button_focus, i);

            //switch to FOCUS and start calendar tests
            onView(withId(R.id.toolbar_focus)).perform(click());

            new RecyclerActionTest(R.id.list_view_focus, R.id.button_menu, 0);
            new RecyclerActionTest(R.id.list_view_focus, R.id.button_calendar, 0);


            if (tests[i]!=null)
                onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i].getYear(), tests[i].getMonthValue(), tests[i].getDayOfMonth()));

            onView(withId(buttons[i])).perform(click());

            //assert results
            try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {
                List<Entry> entries = db.loadEntries(null, 0);

                if (entries.get(i).getReminderDate() == null) {
                    continue;
                }
                if (tests[i] == null)
                    assert (entries.get(i).getReminderDate() == null);
                else
                    assert (entries.get(i).getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(tests[i]));
                assert (db.loadFocus(null, 0).size() == results[i]);
            }
        }
        sharedData.context.deleteDatabase(MainActivity.DATABASE_NAME);
    }

    //test calendar function for items in no list in LIST
    @Test
    public void testCalendarListNoList(){

        //dummy task names for test data
        String[] strings = {"Test", "Test1"};

        //test data
        LocalDate[] tests = {LocalDate.now(),null};


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
            onData(hasToString(sharedData.context.getString(R.string.noList))).inAdapterView(withId(R.id.list_view_lists)).atPosition(0).perform(click());

            //set date
            new RecyclerActionTest(R.id.recycle_view_lists, R.id.button_menu, i);
            new RecyclerActionTest(R.id.recycle_view_lists, R.id.button_calendar, i);
            if (tests[i]!=null)
                onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i].getYear(), tests[i].getMonthValue(), tests[i].getDayOfMonth()));



            onView(withId(buttons[i])).perform(click());

            //assert results
            try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {

                List<Entry> entries = db.loadEntries(null, 0);

                if (tests[i] == null)
                    assert (entries.get(i).getReminderDate() == null);
                else
                    assert (entries.get(i).getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(tests[i]));
                assert (db.getNoList().size() == results[i][0]);
                assert (db.loadDropped().size() == results[i][1]);
            }
        }
        sharedData.context.deleteDatabase(MainActivity.DATABASE_NAME);
    }

    //tests the calendar function in a list in LISTS
    @Test
    public void testCalendarList(){

        //dummy task name for test data
        String[] strings = {"Test"};

        //dummy list name for test data
        String[] lists = {"Test"};

        //test dates
        LocalDate[] tests = {LocalDate.now().plusDays(1)};

        //expected sizes of data.getLists() and data.getDropped()
        int[][] results = {{1,0}};

        //button1 == OK, button2 == No Date
        int[] buttons = {android.R.id.button1, android.R.id.button2};

        for (int i = 0; i < strings.length; i++) {

            //switch to DROP and drop tasks
            onView(withId(R.id.toolbar_drop)).perform(click());
            drop(strings[i]);

            //assign lists
            new RecyclerActionTest(R.id.list_view_drop, R.id.button_menu, 0);
            new RecyclerActionTest(R.id.list_view_drop,R.id.button_list,0);
            new RecyclerActionTest(R.id.list_view_drop,0,lists[i]);
            new RecyclerActionTest(R.id.list_view_drop,R.id.button_back_list,0);

            //switch to LISTS
            onView(withId(R.id.toolbar_lists)).perform(click());

            //choose first item in adapter since there is only one list
            onData(allOf(instanceOf(String.class))).atPosition(0).perform(click());

            //set dates
            new RecyclerActionTest(R.id.recycle_view_lists, R.id.button_menu, i);
            new RecyclerActionTest(R.id.recycle_view_lists, R.id.button_calendar, i);

            if (tests[i]!=null)
                onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i].getYear(), tests[i].getMonthValue(), tests[i].getDayOfMonth()));
            onView(withId(buttons[i])).perform(click());

            //assert results
            try (SQLiteHelper db = new SQLiteHelper(sharedData.context)) {

                List<Entry> entries = db.loadEntries(null, 0);
                if (tests[i] == null)
                    assert (entries.get(i).getReminderDate() == null);
                else
                    assert (entries.get(i).getReminderDate().atZone(ZoneId.systemDefault()).toLocalDate().equals(tests[i]));
                assert (db.getLists().size() == results[i][0]);
                assert (db.loadDropped().size() == results[i][1]);
            }
        }
        sharedData.context.deleteDatabase(MainActivity.DATABASE_NAME);
    }


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
        onView(withId(R.id.list_view_pick)).check(new RecyclerViewCountAssertionTest(1));

        //Test (1): tasksToPick
        for (int i = 0; i < tests.length; i++){
            new RecyclerActionTest(R.id.list_view_pick,R.id.button_menu,0);
            new RecyclerActionTest(R.id.list_view_pick,R.id.button_calendar,0);

            //assert results
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());
            onView(withId(R.id.list_view_pick)).check(new RecyclerViewCountAssertionTest(results[i]));
        }

        //assert task was moved to doLater
        onView(withId(R.id.list_view_pick_doLater)).check(new RecyclerViewCountAssertionTest(1));

        //Test (3): doLater
        for (int i = 0; i < tests.length; i++){
            new RecyclerActionTest(R.id.list_view_pick_doLater,R.id.button_menu,0);
            new RecyclerActionTest(R.id.list_view_pick_doLater,R.id.button_calendar,0);

            //assert results
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(testsDoLater[i][0], testsDoLater[i][1], testsDoLater[i][2]));
            onView(withId(buttonsDoLater[i])).perform(click());
            onView(withId(R.id.list_view_pick_doLater)).check(new RecyclerViewCountAssertionTest(resultsDoLater[i]));
        }

        //assert task was moved back to tasksToPick
        onView(withId(R.id.list_view_pick)).check(new RecyclerViewCountAssertionTest(1));

        //assign list to task
        new RecyclerActionTest(R.id.list_view_pick,R.id.button_menu,0);
        new RecyclerActionTest(R.id.list_view_pick,R.id.button_list,0);

        new RecyclerActionTest(R.id.list_view_pick,0,"Test");
        new RecyclerActionTest(R.id.list_view_pick,R.id.button_back_list,0);

        //assert task was moved to list
        onView(withId(R.id.list_view_pick_list)).check(new RecyclerViewCountAssertionTest(1));

        //Test (4): moveToList
        for (int i = 0; i < tests.length; i++){
            new RecyclerActionTest(R.id.list_view_pick_list,R.id.button_menu,0);
            new RecyclerActionTest(R.id.list_view_pick_list,R.id.button_calendar,0);

            //assert results
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());
            onView(withId(R.id.list_view_pick_list)).check(new RecyclerViewCountAssertionTest(results[i]));
        }

        //assert task was moved to do later
        onView(withId(R.id.list_view_pick_doLater)).check(new RecyclerViewCountAssertionTest(1));

        //press checkbox
        new RecyclerActionTest(R.id.list_view_pick_doLater,R.id.checkbox,0);

        //assert task was moved to doNow
        onView(withId(R.id.list_view_pick_doNow)).check(new RecyclerViewCountAssertionTest(1));

        //Test (2): doNow
        for (int i = 0; i < tests.length; i++){
            new RecyclerActionTest(R.id.list_view_pick_doNow,R.id.button_menu,0);
            new RecyclerActionTest(R.id.list_view_pick_doNow,R.id.button_calendar,0);

            //assert results
            onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(tests[i][0], tests[i][1], tests[i][2]));
            onView(withId(buttons[i])).perform(click());
            onView(withId(R.id.list_view_pick_doNow)).check(new RecyclerViewCountAssertionTest(results[i]));
        }
        sharedData.context.deleteDatabase(MainActivity.DATABASE_NAME);
    }

    //assert RecyclerViewAdapter has number of items
    public static class RecyclerViewCountAssertionTest implements ViewAssertion{

        private final int count;

        public RecyclerViewCountAssertionTest(int count){
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
    private static class RecyclerActionTest {

        RecyclerActionTest(final int idView, final int id, final int position){
            onView(withId(idView)).perform(RecyclerViewActions.actionOnItemAtPosition(position, RecyclerActionTest.clickChildViewWithId(id)));

        }
        RecyclerActionTest(final int idView, final int position, String text){
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
        sharedData.context.deleteDatabase(MainActivity.DATABASE_NAME);
    }
}
