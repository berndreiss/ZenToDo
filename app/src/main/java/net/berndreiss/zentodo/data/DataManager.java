package net.berndreiss.zentodo.data;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
import net.berndreiss.zentodo.exceptions.PositionOutOfBoundException;
import net.berndreiss.zentodo.util.ClientStub;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Manages data manipulation in the application.
 */
public class DataManager {

    /** recurring tasks are automatically added to FOCUS. When they are removed however, the ids
     * are stored in this ArrayList and the tasks are not shown until the next day
     * (see also: FocusTaskListAdapter.java)
     */
    private static List<Long> recurringButRemovedFromToday = null;

    /** Keeps track of list colors */
    public static Map<Long, String> listColors = null;

    /**
     * Initializes the client stub with the email address provided
     * @param sharedData the shared data holding the client stub
     * @param email the mail address for the user
     * @throws InterruptedException thrown when there are problems logging the user in
     */
    public static void initClientStub(SharedData sharedData, String email) throws InterruptedException {
        sharedData.clientStub = new ClientStub( sharedData.database.getDatabase());
        //Communicating important messages to the user (e.g., "User logged in", "There was a problem
        //sending data to the server" etc.)
        Consumer<String> messagePrinter = message -> {
            Looper.prepare();
            new Handler(Looper.getMainLooper()).post(()-> Toast.makeText(sharedData.context, message, Toast.LENGTH_LONG).show());
        };
        //TODO change this
        //sharedData.clientStub.setMessagePrinter(messagePrinter);
        sharedData.clientStub.setMessagePrinter(System.out::println);
        //The uiOperationHandler handles the interaction with the views
        sharedData.clientStub.addOperationHandler(sharedData.uiOperationHandler);
        //Start the client stub in a new thread since Android does not allow network interactions on
        //the main thread
        Thread thread = new Thread(() -> {
            try {
                //TODO get password properly
                sharedData.clientStub.init(email, null, () -> "Test1234!?");
            } catch (IOException _) {}
        });
        thread.start();
        thread.join();
    }

    /**
     * Add a new task.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task to be added
     */
    public static void add(SharedData sharedData, String task) {
        Thread thread = new Thread(() -> sharedData.clientStub.addNewTask(task));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove a task
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task id
     */
    public static void remove(SharedData sharedData, Task task) {
        Thread thread = new Thread(()-> sharedData.clientStub.removeTask(task.getId()));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task1 the first task
     * @param task2 the second task
     */
    public static void swap(SharedData sharedData, Task task1, Task task2) {
        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.swapTasks(task1.getId(), task2.getPosition());
            } catch (PositionOutOfBoundException e) {
                //TODO logging
                throw new RuntimeException(e);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
    }

    /**
     * Swap two tasks list positions.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task1 the first task
     * @param task2 the second task
     */
    public static void swapLists(SharedData sharedData, Task task1, Task task2) {
        Thread thread = new Thread(() -> {
            try {
                sharedData.clientStub.swapListEntries(task1.getList(), task1.getId(), task2.getListPosition());
            } catch (PositionOutOfBoundException e) {
                //TODO logging
                throw new RuntimeException(e);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the literal task.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task id
     * @param newTask the task literal
     */
    public static void setTask(SharedData sharedData, Task task, String newTask) {
        if (task==null || task.getTask().equals(newTask))
            return;
        Thread thread = new Thread(()-> sharedData.clientStub.updateTask(task.getId(), newTask));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the focus field of a task.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task id
     * @param focus the value to set
     */
    public static void setFocus(SharedData sharedData, Task task, Boolean focus) {
        if (task==null || task.getFocus() == focus)
            return;
        Thread thread = new Thread(() -> sharedData.clientStub.updateFocus(task.getId(),  focus));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
        if (task.getDropped())
            setDropped(sharedData, task, false);
    }

    /**
     * Set the dropped field of a task.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task id
     * @param dropped the value to set
     */
    public static void setDropped(SharedData sharedData, Task task, Boolean dropped) {
        if (task == null || task.getDropped() == dropped)
            return;
        Thread thread = new Thread(() -> sharedData.clientStub.updateDropped(task.getId(), dropped));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
    }

    /**
     * Edit the reminder date of a task.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task id
     * @param date the value to set
     */
    public static void editReminderDate(SharedData sharedData, Task task, Instant date) {
        if (task == null || task.getReminderDate() != null && task.getReminderDate().equals(date))
            return;
        Thread thread = new Thread(() -> sharedData.clientStub.updateReminderDate(task.getId(), date));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
        if (task.getDropped())
            setDropped(sharedData, task, false);
    }

    /**
     * Set the recurrence field of a task.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task id
     * @param recurrence the value to set
     */
    public static void editRecurrence(SharedData sharedData, Task task, String recurrence) {
        if (task == null || task.getRecurrence() != null && task.getRecurrence().equals(recurrence))
            return;
        Thread thread = new Thread(() -> sharedData.clientStub.updateRecurrence(task.getId(), recurrence));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
        if (task.getDropped() && recurrence != null)
            setDropped(sharedData, task, false);
    }

    /**
     * Edit the list of a task.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task id
     * @param list the list id
     */
    public static void editList(SharedData sharedData, Task task, String list) {
        //TODO this needs to be reworked and go through the client stub
        Optional<TaskList> taskList = sharedData.clientStub.getListByName(list);
        if (taskList.isEmpty())
            return;
        if (task == null || task.getList() != null && task.getList().equals(taskList.get().getId()))
            return;
        if (task.getList() != null)
            for (Task e : sharedData.adapter.tasks)
                if (task.getList().equals(e.getList()) && e.getListPosition() > task.getListPosition())
                    e.setListPosition(e.getListPosition()-1);
        Thread thread = new Thread(() -> sharedData.database.getListManager().updateList(task, taskList.get().getId()));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
        if (task.getDropped())
            setDropped(sharedData, task, false);
    }

    /**
     * Edit the list color.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param list the list id
     * @param color the color to set (in HEX)
     */
    public static void editListColor(SharedData sharedData, long list, String color) {
        if (listColors == null)
            listColors = sharedData.clientStub.getListColors();
        listColors.put(list, color);
        Thread thread = new Thread(() -> sharedData.clientStub.updateListColor(list, color));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
    }

    /**
     * Get color of list.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param list the list id
     * @return the color
     */
    public static String getListColor(SharedData sharedData, long list){
        if (listColors == null)
                listColors = sharedData.clientStub.getListColors();
        String color = listColors.get(list);
        return color == null ? ListTaskListAdapter.DEFAULT_COLOR : color;
    }

    /**
     * Get all dropped tasks.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @return the dropped tasks
     */
    public static List<Task> getDropped(SharedData sharedData) {
        return sharedData.clientStub.loadDropped();
    }

    /**
     * Get all lists.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @return a list of tasks plus "ALL TASKS" and "No List"
     */
    public static List<TaskList> getLists(SharedData sharedData){
        List<TaskList> lists = sharedData.clientStub.loadLists();
        lists.add(new TaskList(-1, "ALL TASKS", null));
        lists.add(new TaskList(-1, "No List", null));
        return lists;
    }

    /**
     * Get lists as a String
     * @param sharedData the shared data object containing the client stub and the adapter
     * @return the lists as an array of Strings
     */
    public static List<String> getListsAsString(SharedData sharedData){
        return new ArrayList<>(getLists(sharedData).stream().map(TaskList::getName).toList());
    }

    /**
     * Set the recurring field for the task.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task to update
     */
    public static void setRecurring(SharedData sharedData, Task task) {
        //get recurrence as <['d'/'w'/'m'/'y']['0'-'9']['0'-'9']['0'-'9']...> (i.e. "d15" means the task repeats every 15 days)
        String recurrence = task.getRecurrence();
        //get first character representing the mode of repetition (days, weeks,...)
        char mode = recurrence.charAt(0);
        //StringBuilder to store offset
        StringBuilder offSetStr = new StringBuilder();
        //get all digits of offset
        for (int i = 1; i < recurrence.length(); i++)
            offSetStr.append(recurrence.charAt(i));
        //convert offset to Integer
        int offSet = Integer.parseInt(offSetStr.toString());
        //get reminder date
        Instant date = task.getReminderDate();
        Instant todayInstant = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
        if (date == null)
            date = todayInstant;
        if (date == null)
            throw new DateTimeException("The provided date could not be converted to Instant.");
        //increment date by offset and repeat until it is greater than today
        while (!date.isAfter(todayInstant))
            date = incrementRecurring(mode, date, offSet);
        //TODO this should be moved to the UIOperations handler
        //write reminder date to tasks
        task.setReminderDate(date);
        //write reminder date to Database
        Instant finalDate = date;
        Thread thread = new Thread(() -> sharedData.clientStub.updateReminderDate(task.getId(), finalDate));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO add logging
            throw new RuntimeException(e);
        }
    }

    /**
     * Return date incremented by offset.
     * @param mode the interval to use ('d', 'w', 'm', or 'y')
     * @param date the date to increment
     * @param offSet the offset to use
     * @return the incremented date
     */
    private static Instant incrementRecurring(char mode, Instant date, int offSet) {

        ZonedDateTime zonedDateTime = date.atZone(ZoneId.systemDefault());
        return switch (mode) {
            case 'd' -> zonedDateTime.plusDays(offSet).toInstant();
            case 'w' -> zonedDateTime.plusWeeks(offSet).toInstant();
            case 'm' -> zonedDateTime.plusMonths(offSet).toInstant();
            case 'y' -> zonedDateTime.plusYears(offSet).toInstant();
            default -> date;
        };

    }

    /**
     * Add a recurring task to the tasks removed for today.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task to add
     */
    public static void addToRecurringButRemoved(SharedData sharedData, long task){
        if (recurringButRemovedFromToday == null)
            recurringButRemovedFromToday = sharedData.database.getTaskManager().loadRecurring();
        recurringButRemovedFromToday.add(task);
        sharedData.database.getTaskManager().saveRecurring(recurringButRemovedFromToday);
    }

    /**
     * Remove a recurring task from the tasks removed for today.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @param task the task to be removed from the removed tasks
     */
    public static void removeFromRecurringButRemoved(SharedData sharedData, long task){
        if (recurringButRemovedFromToday == null)
            recurringButRemovedFromToday = sharedData.database.getTaskManager().loadRecurring();
        for (int i = 0; i < recurringButRemovedFromToday.size(); i++)
            if (recurringButRemovedFromToday.get(i)==task) {
                recurringButRemovedFromToday.remove(i);
                break;
            }
        sharedData.database.getTaskManager().saveRecurring(recurringButRemovedFromToday);
    }

    /**
     * Get all tasks that are in FOCUS mode.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @return a list of tasks in FOCUS mode
     */
    public static List<Task> getFocus(SharedData sharedData) {
        return sharedData.clientStub.loadFocus();
    }

    /**
     * Get the tasks that are candidates for being picked today.
     * @param sharedData the shared data object containing the client stub and the adapter
     * @return a list of tasks for picking
     */
    public static List<Task> getTasksToPick(SharedData sharedData) {
        return sharedData.database.getTaskManager().loadTasksToPick();
    }
}